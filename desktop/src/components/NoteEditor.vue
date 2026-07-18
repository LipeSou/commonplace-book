<template>
  <section class="editor">
    <input
      v-model="title"
      class="title"
      type="text"
      placeholder="Título da nota"
      aria-label="Título da nota"
    />

    <div class="meta-row">
      <p v-if="note?.id" class="meta">
        criada {{ formatDate(note.createdAt) }} · editada {{ formatDate(note.updatedAt) }}
      </p>
      <span class="spacer"></span>
      <button class="btn-text" @click="rawMode = !rawMode" :title="rawMode ? 'Voltar ao texto vivo' : 'Ver o markdown cru'">
        {{ rawMode ? '筆 vivo' : '素 cru' }}
      </button>
    </div>

    <MarkdownEditor
      v-model="content"
      :raw-mode="rawMode"
      placeholder="Escreva em markdown…"
      @open-link="$emit('open-link', $event)"
    />

    <div v-if="note?.tags?.length" class="tags">
      <TagStamp
        v-for="tag in note.tags"
        :key="tag"
        :name="tag"
        muted
        @pick="$emit('pick-tag', tag)"
      />
    </div>

    <BacklinkPanel :notes="backlinks" @open="$emit('open-note', $event)" />

    <p v-if="error" class="error">{{ error }}</p>

    <footer>
      <button v-if="note?.id" class="btn-text" @click="$emit('delete')">Excluir</button>
      <span class="spacer"></span>
      <Transition name="hanko">
        <span v-if="saved" class="hanko" aria-hidden="true">印</span>
      </Transition>
      <button class="btn-primary" :disabled="!dirty || !title.trim()" @click="save">
        Salvar nota
      </button>
    </footer>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import MarkdownEditor from './MarkdownEditor.vue'
import TagStamp from './TagStamp.vue'
import BacklinkPanel from './BacklinkPanel.vue'

const props = defineProps({
  note: { type: Object, default: null },
  backlinks: { type: Array, default: () => [] },
  error: { type: String, default: '' }
})

const emit = defineEmits(['save', 'delete', 'pick-tag', 'open-note', 'open-link'])

const title = ref('')
const content = ref('')
const saved = ref(false)
const rawMode = ref(false)

watch(
  () => props.note,
  (n) => {
    title.value = n?.title ?? ''
    content.value = n?.content ?? ''
    saved.value = false
  },
  { immediate: true }
)

const dirty = computed(() =>
  title.value !== (props.note?.title ?? '') || content.value !== (props.note?.content ?? '')
)

function save() {
  emit('save', { title: title.value, content: content.value })
  saved.value = true
  setTimeout(() => (saved.value = false), 2000)
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}
</script>

<style scoped>
.editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--s4);
  min-width: 0;
}

.title {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h1);
  color: var(--text);
  background: var(--bg);
  border: none;
  border-bottom: 1px solid var(--border);
  padding: var(--s2) 0;
  transition: border-color var(--dur-fast) var(--ease-ink);
}

.title:focus {
  outline: none;
  border-bottom-color: var(--accent);
}

.meta-row {
  display: flex;
  align-items: baseline;
  gap: var(--s3);
}

.meta {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s2);
}

.error {
  color: var(--accent);
  font-size: var(--fs-small);
  margin: 0;
}

footer {
  display: flex;
  align-items: center;
  gap: var(--s4);
}

.spacer {
  flex: 1;
}

/* o save como carimbo: o hanko surge e assenta */
.hanko {
  font-family: var(--f-display);
  color: var(--accent);
  border: 1.5px solid var(--accent);
  border-radius: var(--r-sm);
  padding: 2px var(--s2);
  font-size: var(--fs-small);
}

.hanko-enter-active {
  transition: opacity var(--dur-slow) var(--ease-ink), transform var(--dur-slow) var(--ease-ink);
}

.hanko-leave-active {
  transition: opacity var(--dur-calm) var(--ease-ink);
}

.hanko-enter-from {
  opacity: 0;
  transform: scale(1.4);
}

.hanko-leave-to {
  opacity: 0;
}

.btn-primary {
  background: var(--accent);
  color: var(--on-accent);
  border: none;
  border-radius: var(--r-sm);
  padding: var(--s3) var(--s5);
  font-family: var(--f-body);
  font-size: var(--fs-body);
  font-weight: 500;
  cursor: pointer;
  transition: opacity var(--dur-fast) var(--ease-ink);
}

.btn-primary:hover:enabled {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.45;
  cursor: default;
}

.btn-text {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  cursor: pointer;
  padding: var(--s2);
  transition: color var(--dur-fast) var(--ease-ink);
}

.btn-text:hover {
  color: var(--text);
}
</style>
