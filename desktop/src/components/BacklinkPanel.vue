<template>
  <section v-if="notes.length" class="backlinks">
    <p class="count">← {{ notes.length }} {{ notes.length === 1 ? 'nota aponta' : 'notas apontam' }} para esta</p>
    <ul>
      <li v-for="note in notes" :key="note.id">
        <button class="link" @click="$emit('open', note.id)">{{ note.title }}</button>
      </li>
    </ul>
  </section>
</template>

<script setup>
defineProps({
  notes: { type: Array, default: () => [] }
})

defineEmits(['open'])
</script>

<style scoped>
.backlinks {
  border-top: 1px solid var(--border);
  padding-top: var(--s4);
}

.count {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0 0 var(--s3);
}

ul {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: var(--s2) var(--s5);
  margin: 0;
  padding: 0;
}

.link {
  background: none;
  border: none;
  padding: 0;
  color: var(--link);
  font-family: var(--f-display);
  font-size: var(--fs-body);
  cursor: pointer;
  transition: opacity var(--dur-fast) var(--ease-ink);
}

.link:hover {
  opacity: 0.75;
}
</style>
