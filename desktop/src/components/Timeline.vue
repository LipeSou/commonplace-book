<template>
  <section class="timeline">
    <div v-for="day in days" :key="day.key" class="day">
      <p class="when">{{ day.label }}</p>
      <div class="notes">
        <NoteCard
          v-for="note in day.notes"
          :key="note.id"
          :note="note"
          :backlinks="backlinkCount(note.id)"
          @select="$emit('open', note.id)"
          @pick-tag="tag => $emit('pick-tag', tag)"
        />
      </div>
    </div>

    <div ref="sentinel" class="sentinel"></div>

    <p v-if="error" class="foot">{{ error }}</p>
    <p v-else-if="loading" class="foot">carregando…</p>
    <p v-else-if="days.length === 0" class="foot">
      Nada escrito ainda. Toda prática começa no vazio.
    </p>
    <p v-else-if="!hasMore" class="foot">
      <span class="brush" aria-hidden="true">—</span> o começo do caderno
    </p>
  </section>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import NoteCard from './NoteCard.vue'
import { getTimeline } from '../api/notes.js'

/*
 * A linha do tempo: as notas descendo por data de nascimento.
 * O servidor manda a página crua; agrupar por dia é trabalho de tela.
 */

const props = defineProps({
  // contagem de backlinks por nota, vinda do grafo (mesma fonte dos cartões da sidebar)
  backlinks: { type: Object, default: () => ({}) },
  // muda quando as notas mudam lá fora (salvou, importou, excluiu) — recarrega do zero
  version: { type: Number, default: 0 }
})

defineEmits(['open', 'pick-tag'])

const notes = ref([])
const page = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const error = ref('')
const sentinel = ref(null)

const PAGE = 30
const REACH = 200   // quanto antes do fim já vale ir buscando o passado
let run = 0

const days = computed(() => {
  const groups = []
  let current = null
  for (const note of notes.value) {
    const key = dayKey(note.createdAt)
    if (!current || current.key !== key) {
      current = { key, label: dayLabel(note.createdAt), notes: [] }
      groups.push(current)
    }
    current.notes.push(note)
  }
  return groups
})

function backlinkCount(id) {
  return Number(props.backlinks[id] ?? 0)
}

function dayKey(iso) {
  return new Date(iso).toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local
}

function dayLabel(iso) {
  const date = new Date(iso)
  const key = dayKey(iso)
  const today = new Date().toLocaleDateString('sv-SE')
  if (key === today) return 'Hoje'

  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  if (key === yesterday.toLocaleDateString('sv-SE')) return 'Ontem'

  const label = date.toLocaleDateString('pt-BR', {
    weekday: 'long', day: '2-digit', month: 'short', year: 'numeric'
  })
  return label.charAt(0).toUpperCase() + label.slice(1)
}

async function load(next) {
  if (loading.value) return
  const mine = ++run
  loading.value = true
  try {
    const result = await getTimeline(next, PAGE)
    if (mine !== run) return
    notes.value = next === 0 ? result.notes : [...notes.value, ...result.notes]
    page.value = next
    hasMore.value = result.hasMore
    error.value = ''
  } catch (e) {
    if (mine === run) error.value = e.message
  } finally {
    if (mine === run) {
      loading.value = false
      // a sentinela só avisa quando entra ou sai de vista; se ela continua ali
      // (página curta, tela alta), é aqui que o passado segue vindo
      await nextTick()
      continueIfNeeded()
    }
  }
}

/** A sentinela chegou perto do fim da janela? Então há passado a carregar. */
function sentinelInReach() {
  const el = sentinel.value
  return el ? el.getBoundingClientRect().top <= window.innerHeight + REACH : false
}

function continueIfNeeded() {
  if (hasMore.value && !loading.value && sentinelInReach()) {
    load(page.value + 1)
  }
}

// scroll infinito. O listener é em captura no document: eventos de scroll não
// borbulham, e quem rola aqui é o painel da home, não a janela.
onMounted(() => {
  load(0)
  document.addEventListener('scroll', continueIfNeeded, { capture: true, passive: true })
  window.addEventListener('resize', continueIfNeeded, { passive: true })
})

onBeforeUnmount(() => {
  document.removeEventListener('scroll', continueIfNeeded, { capture: true })
  window.removeEventListener('resize', continueIfNeeded)
})

defineExpose({ reload: () => load(0) })
</script>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  gap: var(--s7);
}

.day {
  display: flex;
  flex-direction: column;
  gap: var(--s4);
}

.when {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  border-bottom: 1px solid var(--border);
  padding-bottom: var(--s2);
  margin: 0;
}

.notes {
  display: flex;
  flex-direction: column;
  gap: var(--s3);
}

.sentinel {
  height: 1px;
}

.foot {
  text-align: center;
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
}

.brush {
  color: var(--border);
}
</style>
