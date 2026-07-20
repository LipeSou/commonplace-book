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

      <nav class="views">
        <button class="btn-text" :class="{ current: view === 'home' }" @click="showHome">
          始 início
        </button>
        <span class="dot">·</span>
        <button class="btn-text" :class="{ current: view === 'graph' }" @click="view = 'graph'">
          縁 grafo
        </button>
      </nav>

      <SearchField v-model="query" :busy="searching" />

      <FileActions
        @done="onImport"
        @exported="name => flash(`Exportado: ${name}`)"
        @failed="message => flash(message)"
      />

      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loadError" class="error">{{ loadError }}</p>

      <TagPanel :tags="tags" :active="activeTag" @pick="onPickTag" />

      <p v-if="query.trim()" class="filter">
        {{ searchTotal }} {{ searchTotal === 1 ? 'nota encontrada' : 'notas encontradas' }}
      </p>

      <p v-if="activeTag" class="filter">
        mostrando <span class="filter-tag">#{{ activeTag }}</span>
        <button class="clear" @click="clearFilter" aria-label="Limpar filtro">✕</button>
      </p>

      <div class="cards">
        <NoteCard
          v-for="note in visibleNotes"
          :key="note.id"
          :note="note"
          :selected="note.id === selected?.id"
          :backlinks="backlinkCount(note.id)"
          @select="select(note)"
          @pick-tag="filterByTag"
        />
        <button v-if="searchHasMore" class="more btn-text" @click="searchMore">
          carregar mais
        </button>
        <p v-if="activeTag && notes.length === 0" class="empty-filter">
          Nenhuma nota com essa tag.
        </p>
        <p v-if="query.trim() && !searching && searchResults.length === 0" class="empty-filter">
          Nada por aqui. Tente outra palavra — ou uma <code>"frase exata"</code>.
        </p>
      </div>
    </aside>

    <main class="pane">
      <GraphView
        v-if="view === 'graph'"
        :nodes="graph.nodes"
        :edges="graph.edges"
        :current-id="selected?.id ?? null"
        @open="openNote"
      />
      <EmptyState v-else-if="showEmpty" @create="startNew" />
      <HomeView
        v-else-if="view === 'home'"
        ref="home"
        :backlinks="backlinkCounts"
        @open="openNote"
        @pick-tag="filterByTag"
      />
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
import { ref, computed, watch, onMounted } from 'vue'
import {
  listNotes, getNote, createNote, updateNote, deleteNote,
  listTags, listBacklinks, getGraph, searchNotes
} from './api/notes.js'
import { useTheme } from './composables/useTheme.js'
import NoteCard from './components/NoteCard.vue'
import NoteEditor from './components/NoteEditor.vue'
import EmptyState from './components/EmptyState.vue'
import FileActions from './components/FileActions.vue'
import TagPanel from './components/TagPanel.vue'
import GraphView from './components/GraphView.vue'
import SearchField from './components/SearchField.vue'
import HomeView from './components/HomeView.vue'

const { theme, toggle } = useTheme()
const themeLabel = computed(() => (theme.value === 'ink' ? 'Trocar para papel' : 'Trocar para tinta'))

const notes = ref([])
const tags = ref([])
const graph = ref({ nodes: [], edges: [] })
const backlinks = ref([])
const view = ref('home')   // 'home' | 'note' | 'graph' — abrir o app é chegar em casa
const home = ref(null)
const selected = ref(null)      // null = nada aberto; { id: undefined } = rascunho novo
const editing = ref(false)
const loadError = ref('')
const editorError = ref('')
const notice = ref('')
const activeTag = ref('')
const query = ref('')
const searching = ref(false)
const searchResults = ref([])
const searchTotal = ref(0)
const searchHasMore = ref(false)
let noticeTimer = null
let searchTimer = null
let searchRun = 0

const PAGE = 20

// com busca ativa, a lista da sidebar é o resultado; sem ela, as notas de sempre
const visibleNotes = computed(() => (query.value.trim() ? searchResults.value : notes.value))

// digitar não dispara uma requisição por tecla: espera a mão parar
watch(query, () => {
  clearTimeout(searchTimer)
  if (!query.value.trim()) {
    searchRun++
    searching.value = false
    searchResults.value = []
    searchTotal.value = 0
    searchHasMore.value = false
    return
  }
  activeTag.value = ''
  searchTimer = setTimeout(() => runSearch(0), 250)
})

async function runSearch(page) {
  const run = ++searchRun
  const term = query.value.trim()
  searching.value = true
  try {
    const result = await searchNotes(term, page, PAGE)
    if (run !== searchRun) return // chegou tarde: outra busca já assumiu
    const hits = result.results.map(hit => ({ ...hit.note, snippet: hit.snippet }))
    searchResults.value = page === 0 ? hits : [...searchResults.value, ...hits]
    searchTotal.value = result.total
    searchHasMore.value = result.hasMore
    loadError.value = ''
  } catch (e) {
    if (run === searchRun) loadError.value = e.message
  } finally {
    if (run === searchRun) searching.value = false
  }
}

function searchMore() {
  runSearch(Math.floor(searchResults.value.length / PAGE))
}

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

const showEmpty = computed(() =>
  notes.value.length === 0 && !editing.value && !activeTag.value && !query.value.trim()
)

async function reload() {
  loadError.value = ''
  try {
    notes.value = await listNotes(activeTag.value || undefined)
    tags.value = await listTags()
    graph.value = await getGraph()
    await loadBacklinks()
    if (query.value.trim()) await runSearch(0)
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

// as contagens em forma de mapa, para a timeline não varrer a lista a cada cartão
const backlinkCounts = computed(() =>
  Object.fromEntries(graph.value.nodes.map(n => [n.id, Number(n.backlinks) || 0]))
)

function showHome() {
  view.value = 'home'
  home.value?.reload()
}

async function openNote(id) {
  editorError.value = ''
  try {
    selected.value = await getNote(id)
    editing.value = true
    view.value = 'note'
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
  query.value = ''
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
  view.value = 'note'
  loadBacklinks()
}

function startNew() {
  selected.value = null
  editing.value = true
  editorError.value = ''
  view.value = 'note'
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
/* casco fixo: a janela não rola: quem rola são os painéis, cada um por dentro */
.app {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--s5);
  padding: var(--s6) var(--s5);
  border-right: 1px solid var(--border);
  overflow: hidden;
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

/* as duas telas que não são nota. A atual se distingue só pela tinta cheia:
   o acento fica reservado pra nota selecionada e pro selo do jornal. */
.views {
  display: flex;
  align-items: baseline;
  gap: var(--s2);
  margin-top: calc(-1 * var(--s3));
}

.views .btn-text {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  cursor: pointer;
  padding: 0;
  transition: color var(--dur-fast) var(--ease-ink);
}

.views .btn-text:hover,
.views .btn-text.current {
  color: var(--text);
}

.views .dot {
  color: var(--text-muted);
  font-size: var(--fs-small);
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

.empty-filter code {
  font-family: var(--f-mono);
  font-size: var(--fs-mono);
}

.more {
  align-self: center;
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  cursor: pointer;
  padding: var(--s2);
  transition: color var(--dur-fast) var(--ease-ink);
}

.more:hover {
  color: var(--text);
}

/* só a lista rola: busca, tags e ações ficam sempre à mão */
.cards {
  flex: 1;
  min-height: 0;
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
  overflow: hidden;
}

@media (max-width: 720px) {
  /* na tela estreita o casco cede: a página inteira volta a rolar */
  .app {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }

  .sidebar {
    width: auto;
    border-right: none;
    border-bottom: 1px solid var(--border);
    overflow: visible;
  }

  .cards {
    overflow: visible;
  }

  .pane {
    padding: var(--s5);
    overflow: visible;
  }
}
</style>
