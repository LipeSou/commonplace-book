<template>
  <!-- 円相 ensō: círculo de um traço, sempre aberto (a abertura fica no topo-direito) -->
  <svg :width="size" :height="size" viewBox="0 0 100 100" fill="none" aria-hidden="true">
    <path
      d="M 78 22 A 40 40 0 1 0 88 46"
      stroke="currentColor"
      stroke-width="5"
      stroke-linecap="round"
    />
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 96 }
})
</script>
