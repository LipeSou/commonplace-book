<template>
  <section class="editor">
    <input
      v-model="title"
      class="title"
      type="text"
      placeholder="Título da nota"
      aria-label="Título da nota"
    />

    <p v-if="note?.id" class="meta">
      criada {{ formatDate(note.createdAt) }} · editada {{ formatDate(note.updatedAt) }}
    </p>

    <textarea
      v-model="content"
      class="content"
      placeholder="Escreva em markdown…"
      aria-label="Conteúdo em markdown"
      spellcheck="false"
    ></textarea>

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

const props = defineProps({
  note: { type: Object, default: null },
  error: { type: String, default: '' }
})

const emit = defineEmits(['save', 'delete'])

const title = ref('')
const content = ref('')
const saved = ref(false)

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

.meta {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0;
}

/* o markdown bruto, exatamente como será salvo — sempre mono */
.content {
  flex: 1;
  font-family: var(--f-mono);
  font-size: var(--fs-mono);
  line-height: 1.8;
  color: var(--text);
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: var(--s4);
  resize: none;
  transition: border-color var(--dur-fast) var(--ease-ink);
}

.content:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--focus);
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
