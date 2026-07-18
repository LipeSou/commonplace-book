<template>
  <div ref="host" class="md-editor"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { EditorView, keymap, placeholder as cmPlaceholder, Decoration, ViewPlugin, WidgetType } from '@codemirror/view'
import { EditorState, Compartment } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { markdown, markdownLanguage } from '@codemirror/lang-markdown'
import { syntaxTree } from '@codemirror/language'

/*
 * Live preview estilo Obsidian sobre CodeMirror 6.
 * O documento é SEMPRE a string crua — a formatação é decoração visual;
 * nada aqui transforma o texto (o content sagrado sai byte a byte).
 */

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  rawMode: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'open-link'])

const host = ref(null)
let view = null
const modeConfig = new Compartment()

class TextWidget extends WidgetType {
  constructor(text, className) {
    super()
    this.text = text
    this.className = className
  }
  eq(other) {
    return other.text === this.text && other.className === this.className
  }
  toDOM() {
    const span = document.createElement('span')
    span.textContent = this.text
    span.className = this.className
    return span
  }
}

const bulletWidget = new TextWidget('•', 'cm-bullet')
const kakkoWidget = new TextWidget('「', 'cm-kakko')

// linhas tocadas por alguma seleção/cursor: ali o cru se revela
function activeLines(state) {
  const lines = new Set()
  for (const range of state.selection.ranges) {
    const from = state.doc.lineAt(range.from).number
    const to = state.doc.lineAt(range.to).number
    for (let n = from; n <= to; n++) lines.add(n)
  }
  return lines
}

const WIKILINK = /\[\[([^\[\]\n]+)\]\]/g

function buildDecorations(view) {
  const { state } = view
  const active = lines => {
    for (let n = lines[0]; n <= lines[1]; n++) if (act.has(n)) return true
    return false
  }
  const act = activeLines(state)
  const ranges = []
  const add = (from, to, deco) => ranges.push(deco.range(from, to))
  const lineSpan = (from, to) => [state.doc.lineAt(from).number, state.doc.lineAt(to).number]

  // esconde a marca, ou — na linha ativa — mostra em cinza discreto
  const mark = (from, to, hidden) => {
    if (from >= to) return
    if (hidden) add(from, to, Decoration.replace({}))
    else add(from, to, Decoration.mark({ class: 'cm-syntax' }))
  }

  for (const { from, to } of view.visibleRanges) {
    syntaxTree(state).iterate({
      from,
      to,
      enter(node) {
        const hidden = !active(lineSpan(node.from, node.to))

        switch (node.name) {
          case 'ATXHeading1':
          case 'ATXHeading2':
          case 'ATXHeading3':
          case 'ATXHeading4':
          case 'ATXHeading5':
          case 'ATXHeading6': {
            const level = Math.min(Number(node.name.slice(-1)), 3)
            add(state.doc.lineAt(node.from).from, state.doc.lineAt(node.from).from,
              Decoration.line({ class: `cm-h${level}` }))
            break
          }
          case 'HeaderMark': {
            // engole o espaço depois do `#`
            const end = state.doc.sliceString(node.to, node.to + 1) === ' ' ? node.to + 1 : node.to
            mark(node.from, end, hidden)
            break
          }
          case 'StrongEmphasis':
            add(node.from, node.to, Decoration.mark({ class: 'cm-strong' }))
            break
          case 'Emphasis':
            add(node.from, node.to, Decoration.mark({ class: 'cm-em' }))
            break
          case 'EmphasisMark':
            mark(node.from, node.to, hidden)
            break
          case 'InlineCode':
            add(node.from, node.to, Decoration.mark({ class: 'cm-inline-code' }))
            break
          case 'CodeMark': {
            // crases do código inline (as cercas de bloco são tratadas em FencedCode)
            if (node.matchContext(['InlineCode'])) mark(node.from, node.to, hidden)
            break
          }
          case 'Blockquote': {
            const [first, last] = lineSpan(node.from, node.to)
            for (let n = first; n <= last; n++) {
              const line = state.doc.line(n)
              add(line.from, line.from, Decoration.line({ class: 'cm-quote' }))
            }
            break
          }
          case 'QuoteMark': {
            const end = state.doc.sliceString(node.to, node.to + 1) === ' ' ? node.to + 1 : node.to
            const isFirst = state.doc.lineAt(node.from).number === lineSpan(node.parent?.from ?? node.from, node.from)[0]
            if (hidden && isFirst) add(node.from, end, Decoration.replace({ widget: kakkoWidget }))
            else mark(node.from, end, hidden)
            break
          }
          case 'ListMark': {
            const isBullet = /[-+*]/.test(state.doc.sliceString(node.from, node.to))
            if (hidden && isBullet) add(node.from, node.to, Decoration.replace({ widget: bulletWidget }))
            else add(node.from, node.to, Decoration.mark({ class: 'cm-syntax' }))
            break
          }
          case 'Link':
            add(node.from, node.to, Decoration.mark({ class: 'cm-mdlink' }))
            break
          case 'LinkMark':
            mark(node.from, node.to, hidden)
            break
          case 'URL': {
            // dentro de [texto](url) a URL some; autolinks soltos ficam
            if (node.matchContext(['Link'])) mark(node.from, node.to, hidden)
            else add(node.from, node.to, Decoration.mark({ class: 'cm-mdlink' }))
            break
          }
          case 'FencedCode': {
            const [first, last] = lineSpan(node.from, node.to)
            const blockActive = active([first, last])
            for (let n = first; n <= last; n++) {
              const line = state.doc.line(n)
              add(line.from, line.from, Decoration.line({ class: 'cm-codeblock' }))
              const isFence = n === first || n === last
              if (isFence && !blockActive && line.length > 0) {
                add(line.from, line.to, Decoration.replace({}))
              }
            }
            return false // não desce: nada de decorar dentro do código
          }
        }
      }
    })

    // [[wikilinks]] — o lezer não conhece; regex por cima do texto visível
    const text = state.doc.sliceString(from, to)
    for (const m of text.matchAll(WIKILINK)) {
      const start = from + m.index
      const end = start + m[0].length
      const hidden = !active(lineSpan(start, end))
      mark(start, start + 2, hidden)
      add(start + 2, end - 2, Decoration.mark({ class: 'cm-mdlink' }))
      mark(end - 2, end, hidden)
    }
  }

  return Decoration.set(ranges, true)
}

// o [[wikilink]] sob o cursor do mouse, se houver (título limpo de apelido e âncora)
function wikilinkAt(state, pos) {
  const line = state.doc.lineAt(pos)
  for (const m of line.text.matchAll(WIKILINK)) {
    const from = line.from + m.index
    if (pos >= from && pos <= from + m[0].length) {
      return m[1].split(/[|#]/)[0].trim()
    }
  }
  return null
}

// Ctrl/⌘+clique num wikilink navega; clique simples continua sendo só cursor
const followLinks = EditorView.domEventHandlers({
  mousedown(event, view) {
    if (!event.ctrlKey && !event.metaKey) return false
    const pos = view.posAtCoords({ x: event.clientX, y: event.clientY })
    if (pos == null) return false
    const title = wikilinkAt(view.state, pos)
    if (!title) return false
    event.preventDefault()
    emit('open-link', title)
    return true
  }
})

const livePreview = ViewPlugin.fromClass(
  class {
    constructor(view) {
      this.decorations = buildDecorations(view)
    }
    update(update) {
      if (update.docChanged || update.selectionSet || update.viewportChanged) {
        this.decorations = buildDecorations(update.view)
      }
    }
  },
  { decorations: v => v.decorations }
)

// tema base: tudo via tokens — zero cor crua
const baseTheme = EditorView.theme({
  '&': { height: '100%' },
  '.cm-scroller': { overflow: 'auto', lineHeight: '1.8' },
  '.cm-content': { padding: 'var(--s4)', caretColor: 'var(--accent)' },
  '&.cm-focused': { outline: 'none' },
  '.cm-cursor': { borderLeftColor: 'var(--accent)' },
  '.cm-placeholder': { color: 'var(--text-muted)' },
  '.cm-syntax': { color: 'var(--text-muted)' },
  '.cm-h1': { fontFamily: 'var(--f-display)', fontWeight: '600', fontSize: 'var(--fs-h1)' },
  '.cm-h2': { fontFamily: 'var(--f-display)', fontWeight: '600', fontSize: 'var(--fs-h2)' },
  '.cm-h3': { fontFamily: 'var(--f-display)', fontWeight: '600', fontSize: 'var(--fs-body)' },
  '.cm-strong': { fontWeight: '600' },
  '.cm-em': { fontStyle: 'italic' },
  '.cm-inline-code': {
    fontFamily: 'var(--f-mono)',
    fontSize: 'var(--fs-mono)',
    background: 'var(--surface)',
    borderRadius: 'var(--r-sm)',
    padding: '0 var(--s1)'
  },
  '.cm-quote': {
    fontFamily: 'var(--f-display)',
    fontSize: 'var(--fs-h2)',
    paddingLeft: 'var(--s5)'
  },
  '.cm-kakko': { color: 'var(--accent)', fontFamily: 'var(--f-display)' },
  '.cm-bullet': { color: 'var(--text-muted)' },
  '.cm-mdlink': { color: 'var(--link)' },
  '.cm-codeblock': {
    fontFamily: 'var(--f-mono)',
    fontSize: 'var(--fs-mono)',
    background: 'var(--surface)'
  }
})

const proseTheme = EditorView.theme({
  '.cm-content': { fontFamily: 'var(--f-body)', fontWeight: '300', fontSize: 'var(--fs-body)' }
})

const monoTheme = EditorView.theme({
  '.cm-content': { fontFamily: 'var(--f-mono)', fontSize: 'var(--fs-mono)' }
})

function modeExtensions(raw) {
  return raw ? [monoTheme] : [proseTheme, livePreview]
}

onMounted(() => {
  view = new EditorView({
    parent: host.value,
    state: EditorState.create({
      doc: props.modelValue,
      extensions: [
        history(),
        keymap.of([...defaultKeymap, ...historyKeymap]),
        markdown({ base: markdownLanguage }),
        EditorView.lineWrapping,
        cmPlaceholder(props.placeholder),
        baseTheme,
        followLinks,
        modeConfig.of(modeExtensions(props.rawMode)),
        EditorView.updateListener.of(update => {
          if (update.docChanged) emit('update:modelValue', update.state.doc.toString())
        })
      ]
    })
  })
})

watch(
  () => props.modelValue,
  value => {
    if (view && value !== view.state.doc.toString()) {
      view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: value } })
    }
  }
)

watch(
  () => props.rawMode,
  raw => {
    view?.dispatch({ effects: modeConfig.reconfigure(modeExtensions(raw)) })
  }
)

onBeforeUnmount(() => view?.destroy())
</script>

<style scoped>
.md-editor {
  flex: 1;
  min-height: 0;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  background: var(--bg);
  transition: border-color var(--dur-fast) var(--ease-ink);
}

.md-editor:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--focus);
}
</style>
