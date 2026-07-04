<template>
  <article
    class="card"
    :class="{ selected }"
    tabindex="0"
    @click="$emit('select')"
    @keydown.enter="$emit('select')"
  >
    <h3>{{ note.title }}</h3>
    <p class="meta">{{ formatDate(note.updatedAt) }}</p>
    <p v-if="excerpt" class="excerpt">{{ excerpt }}</p>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  note: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})

defineEmits(['select'])

const excerpt = computed(() => {
  const text = props.note.content.trim()
  return text.length > 120 ? text.slice(0, 120) + '…' : text
})

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}
</script>

<style scoped>
.card {
  border: 1px solid var(--border);
  border-left: 3px solid transparent;
  border-radius: var(--r-md);
  background: var(--surface);
  padding: var(--s4) var(--s5);
  cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-ink);
}

.card:hover {
  border-color: var(--accent);
}

.card.selected {
  border-left-color: var(--accent);
}

h3 {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h2);
  margin: 0 0 var(--s1);
}

.meta {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0 0 var(--s2);
}

.excerpt {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
