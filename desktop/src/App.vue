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

      <p v-if="loadError" class="error">{{ loadError }}</p>

      <div class="cards">
        <NoteCard
          v-for="note in notes"
          :key="note.id"
          :note="note"
          :selected="note.id === selected?.id"
          @select="select(note)"
        />
      </div>
    </aside>

    <main class="pane">
      <EmptyState v-if="showEmpty" @create="startNew" />
      <NoteEditor
        v-else
        :note="selected"
        :error="editorError"
        @save="save"
        @delete="remove"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listNotes, createNote, updateNote, deleteNote } from './api/notes.js'
import { useTheme } from './composables/useTheme.js'
import NoteCard from './components/NoteCard.vue'
import NoteEditor from './components/NoteEditor.vue'
import EmptyState from './components/EmptyState.vue'

const { theme, toggle } = useTheme()
const themeLabel = computed(() => (theme.value === 'ink' ? 'Trocar para papel' : 'Trocar para tinta'))

const notes = ref([])
const selected = ref(null)      // null = nada aberto; { id: undefined } = rascunho novo
const editing = ref(false)
const loadError = ref('')
const editorError = ref('')

const showEmpty = computed(() => notes.value.length === 0 && !editing.value)

async function reload() {
  loadError.value = ''
  try {
    notes.value = await listNotes()
  } catch (e) {
    loadError.value = e.message
  }
}

function select(note) {
  selected.value = note
  editing.value = true
  editorError.value = ''
}

function startNew() {
  selected.value = null
  editing.value = true
  editorError.value = ''
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

.error {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
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
