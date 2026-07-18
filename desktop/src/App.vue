<template>
  <div class="app washi-grain">
    <aside class="sidebar">
      <header>
        <h1>Commonplace Book</h1>
        <button class="theme-toggle" @click="toggle" :title="themeLabel">
          {{ theme === 'ink' ? '紙' : '墨' }}
        </button>
      </header>

      <button class="new-note btn-secondary" @click="startNew">Nova nota</button>

      <button class="btn-text graph-toggle" @click="showGraph = !showGraph">
        {{ showGraph ? 'voltar à nota' : '縁 ver o grafo' }}
      </button>

      <ImportNotes @done="onImport" />

      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loadError" class="error">{{ loadError }}</p>

      <TagPanel :tags="tags" :active="activeTag" @pick="onPickTag" />

      <p v-if="activeTag" class="filter">
        mostrando <span class="filter-tag">#{{ activeTag }}</span>
        <button class="clear" @click="clearFilter" aria-label="Limpar filtro">✕</button>
      </p>

      <div class="cards">
        <NoteCard
          v-for="note in notes"
          :key="note.id"
          :note="note"
          :selected="note.id === selected?.id"
          :backlinks="backlinkCount(note.id)"
          @select="select(note)"
          @pick-tag="filterByTag"
        />
        <p v-if="activeTag && notes.length === 0" class="empty-filter">
          Nenhuma nota com essa tag.
        </p>
      </div>
    </aside>

    <main class="pane">
      <GraphView
        v-if="showGraph"
        :nodes="graph.nodes"
        :edges="graph.edges"
        :current-id="selected?.id ?? null"
        @open="openNote"
      />
      <EmptyState v-else-if="showEmpty" @create="startNew" />
      <NoteEditor
        v-else
        :note="selected"
        :backlinks="backlinks"
        :error="editorError"
        @save="save"
        @delete="remove"
        @pick-tag="filterByTag"
        @open-note="openNote"
        @open-link="openByTitle"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  listNotes, getNote, createNote, updateNote, deleteNote,
  listTags, listBacklinks, getGraph
} from './api/notes.js'
import { useTheme } from './composables/useTheme.js'
import NoteCard from './components/NoteCard.vue'
import NoteEditor from './components/NoteEditor.vue'
import EmptyState from './components/EmptyState.vue'
import ImportNotes from './components/ImportNotes.vue'
import TagPanel from './components/TagPanel.vue'
import GraphView from './components/GraphView.vue'

const { theme, toggle } = useTheme()
const themeLabel = computed(() => (theme.value === 'ink' ? 'Trocar para papel' : 'Trocar para tinta'))

const notes = ref([])
const tags = ref([])
const graph = ref({ nodes: [], edges: [] })
const backlinks = ref([])
const showGraph = ref(false)
const selected = ref(null)      // null = nada aberto; { id: undefined } = rascunho novo
const editing = ref(false)
const loadError = ref('')
const editorError = ref('')
const notice = ref('')
const activeTag = ref('')
let noticeTimer = null

function flash(message) {
  notice.value = message
  clearTimeout(noticeTimer)
  noticeTimer = setTimeout(() => (notice.value = ''), 4000)
}

async function onImport({ imported, failed }) {
  if (imported > 0) {
    const plural = imported === 1 ? 'nota importada' : 'notas importadas'
    flash(`${imported} ${plural}${failed.length ? ` · ${failed.length} falharam` : ''}`)
    await reload()
  } else if (failed.length > 0) {
    flash(`Nenhuma importada · ${failed.length} falharam`)
  } else {
    flash('Nenhum arquivo .md selecionado')
  }
}

const showEmpty = computed(() => notes.value.length === 0 && !editing.value && !activeTag.value)

async function reload() {
  loadError.value = ''
  try {
    notes.value = await listNotes(activeTag.value || undefined)
    tags.value = await listTags()
    graph.value = await getGraph()
    await loadBacklinks()
  } catch (e) {
    loadError.value = e.message
  }
}

async function loadBacklinks() {
  backlinks.value = selected.value?.id ? await listBacklinks(selected.value.id) : []
}

function backlinkCount(id) {
  return Number(graph.value.nodes.find(n => n.id === id)?.backlinks ?? 0)
}

async function openNote(id) {
  editorError.value = ''
  try {
    selected.value = await getNote(id)
    editing.value = true
    showGraph.value = false
    await loadBacklinks()
  } catch (e) {
    editorError.value = e.message
  }
}

// Ctrl+clique num [[wikilink]]: abre a nota daquele título, se ela existir
function openByTitle(title) {
  const key = title.trim().toLowerCase()
  const node = graph.value.nodes.find(n => n.title.trim().toLowerCase() === key)
  if (node) openNote(node.id)
  else flash(`Nenhuma nota com o título "${title}" — o link ainda está aberto.`)
}

function filterByTag(tag) {
  activeTag.value = tag
  reload()
}

function clearFilter() {
  activeTag.value = ''
  reload()
}

// no painel: clicar na tag ativa desliga o filtro; nas outras, filtra
function onPickTag(tag) {
  if (tag === activeTag.value) clearFilter()
  else filterByTag(tag)
}

function select(note) {
  selected.value = note
  editing.value = true
  editorError.value = ''
  showGraph.value = false
  loadBacklinks()
}

function startNew() {
  selected.value = null
  editing.value = true
  editorError.value = ''
  showGraph.value = false
  backlinks.value = []
}

async function save({ title, content }) {
  editorError.value = ''
  try {
    if (selected.value?.id) {
      selected.value = await updateNote(selected.value.id, { title, content })
    } else {
      selected.value = await createNote({ title, content })
    }
    await reload()
  } catch (e) {
    editorError.value = e.message
  }
}

async function remove() {
  if (!selected.value?.id) return
  editorError.value = ''
  try {
    await deleteNote(selected.value.id)
    selected.value = null
    editing.value = false
    backlinks.value = []
    await reload()
  } catch (e) {
    editorError.value = e.message
  }
}

onMounted(reload)
</script>

<style scoped>
.app {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--s5);
  padding: var(--s6) var(--s5);
  border-right: 1px solid var(--border);
}

header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--s3);
}

h1 {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h2);
  margin: 0;
}

.theme-toggle {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-display);
  font-size: var(--fs-body);
  cursor: pointer;
  padding: var(--s1) var(--s2);
  transition: color var(--dur-fast) var(--ease-ink);
}

.theme-toggle:hover {
  color: var(--text);
}

.btn-secondary {
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  color: var(--text);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  padding: var(--s2) var(--s4);
  cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-ink);
}

.btn-secondary:hover {
  border-color: var(--text-muted);
}

.graph-toggle {
  align-self: flex-start;
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  cursor: pointer;
  padding: 0;
  margin-top: calc(-1 * var(--s3));
  transition: color var(--dur-fast) var(--ease-ink);
}

.graph-toggle:hover {
  color: var(--text);
}

.error {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
}

.notice {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
  transition: opacity var(--dur-calm) var(--ease-ink);
}

.filter {
  display: flex;
  align-items: baseline;
  gap: var(--s2);
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
}

.filter-tag {
  font-family: var(--f-mono);
  color: var(--text);
}

.clear {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: var(--fs-small);
  padding: 0 var(--s1);
  transition: color var(--dur-fast) var(--ease-ink);
}

.clear:hover {
  color: var(--accent);
}

.empty-filter {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: var(--s3) 0 0;
}

.cards {
  display: flex;
  flex-direction: column;
  gap: var(--s3);
  overflow-y: auto;
}

.pane {
  flex: 1;
  display: flex;
  padding: var(--s7) var(--s8);
  min-width: 0;
}

@media (max-width: 720px) {
  .app {
    flex-direction: column;
  }

  .sidebar {
    width: auto;
    border-right: none;
    border-bottom: 1px solid var(--border);
  }

  .pane {
    padding: var(--s5);
  }
}
</style>
