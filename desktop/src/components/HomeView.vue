<template>
  <div class="home">
    <JournalCard :sections="sections" :error="journalError" @open="id => $emit('open', id)" />

    <Timeline
      ref="timeline"
      :backlinks="backlinks"
      @open="id => $emit('open', id)"
      @pick-tag="tag => $emit('pick-tag', tag)"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import JournalCard from './JournalCard.vue'
import Timeline from './Timeline.vue'
import { getJournal } from '../api/notes.js'

/*
 * A home: o passado voltando no topo (jornal), o presente descendo embaixo (timeline).
 */

defineProps({
  backlinks: { type: Object, default: () => ({}) }
})

defineEmits(['open', 'pick-tag'])

const sections = ref([])
const journalError = ref('')
const timeline = ref(null)

async function loadJournal() {
  try {
    sections.value = (await getJournal()).sections
    journalError.value = ''
  } catch (e) {
    journalError.value = e.message
  }
}

onMounted(loadJournal)

defineExpose({
  reload: () => {
    loadJournal()
    timeline.value?.reload()
  }
})
</script>

<style scoped>
.home {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--s7);
  min-width: 0;
  overflow-y: auto;
}
</style>
