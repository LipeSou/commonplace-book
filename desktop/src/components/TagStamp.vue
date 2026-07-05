<template>
  <button
    class="stamp"
    :class="{ muted }"
    type="button"
    @click.stop="$emit('pick', name)"
  >#{{ name }}</button>
</template>

<script setup>
defineProps({
  name: { type: String, required: true },
  muted: { type: Boolean, default: false }
})

defineEmits(['pick'])
</script>

<style scoped>
/* carimbo hanko: contorno + sombra-carimbo, nunca pílula preenchida */
.stamp {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--accent);
  background: none;
  border: 1px solid var(--accent);
  border-radius: var(--r-sm);
  box-shadow: 1.5px 1.5px 0 var(--accent);
  padding: 0 var(--s2);
  cursor: pointer;
  /* só o transform anima — box-shadow com var() fora da transição p/ re-resolver o --accent na troca de tema */
  transition: transform var(--dur-fast) var(--ease-ink);
}

.stamp:hover {
  /* o carimbo "assenta" ao passar o mouse */
  transform: translate(1.5px, 1.5px);
  box-shadow: 0 0 0 var(--accent);
}

/* variante discreta: contorno de borda, sem tinta nem sombra */
.stamp.muted {
  color: var(--text-muted);
  border-color: var(--border);
  box-shadow: none;
}

.stamp.muted:hover {
  transform: none;
  color: var(--text);
}
</style>
