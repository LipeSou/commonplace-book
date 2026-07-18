<template>
  <div class="search" :class="{ busy }">
    <span class="glyph" aria-hidden="true">⌕</span>
    <input
      :value="modelValue"
      type="search"
      class="field"
      placeholder="Buscar nas notas"
      aria-label="Buscar nas notas"
      @input="$emit('update:modelValue', $event.target.value)"
      @keydown.esc="$emit('update:modelValue', '')"
    />
    <button
      v-if="modelValue"
      class="clear"
      aria-label="Limpar busca"
      @click="$emit('update:modelValue', '')"
    >✕</button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  busy: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.search {
  display: flex;
  align-items: center;
  gap: var(--s2);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  background: var(--bg);
  padding: var(--s2) var(--s3);
  transition: border-color var(--dur-fast) var(--ease-ink), box-shadow var(--dur-fast) var(--ease-ink);
}

.search:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--focus);
}

.glyph {
  color: var(--text-muted);
  font-size: var(--fs-body);
  line-height: 1;
  transition: opacity var(--dur-calm) var(--ease-ink);
}

.busy .glyph {
  opacity: 0.4;
}

.field {
  flex: 1;
  min-width: 0;
  background: none;
  border: none;
  color: var(--text);
  font-family: var(--f-body);
  font-size: var(--fs-body);
}

.field:focus {
  outline: none;
}

.field::-webkit-search-cancel-button {
  display: none;
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
  color: var(--text);
}
</style>
