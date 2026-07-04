<template>
  <section class="empty">
    <EnsoMark class="enso" :size="88" />
    <p>O Commonplace Book está em branco.<br />Toda prática começa no vazio.</p>
    <button class="btn-primary" @click="$emit('create')">Escrever a primeira nota</button>
  </section>
</template>

<script setup>
import EnsoMark from './EnsoMark.vue'

defineEmits(['create'])
</script>

<style scoped>
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--s6);
  text-align: center;
}

.enso {
  color: var(--text-muted);
}

p {
  color: var(--text-muted);
  margin: 0;
  line-height: 1.9;
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

.btn-primary:hover {
  opacity: 0.9;
}
</style>
