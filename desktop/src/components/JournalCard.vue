<template>
  <article class="journal">
    <header class="masthead">
      <h2>温故知新</h2>
      <p class="today">{{ today }}</p>
      <span class="hanko" aria-hidden="true">閲</span>
    </header>

    <p v-if="error" class="silence">{{ error }}</p>

    <p v-else-if="sections.length === 0" class="silence">
      Nenhuma lembrança para hoje. Escreva algo que o seu eu de daqui a um ano vai reler.
    </p>

    <section v-for="section in sections" :key="section.label" class="memory">
      <p class="when">
        {{ section.label }}
        <span class="when-date">· {{ formatDay(section.date) }}</span>
      </p>
      <div class="columns" :class="{ two: section.notes.length > 1 }">
        <article v-for="note in section.notes" :key="note.id" class="piece">
          <button class="headline" @click="$emit('open', note.id)">{{ note.title }}</button>
          <p class="lede">{{ excerpt(note.content) }}</p>
        </article>
      </div>
    </section>
  </article>
</template>

<script setup>
import { computed } from 'vue'

/*
 * 温故知新 (onkochishin) — "revisitar o velho para conhecer o novo".
 * Um jornal no sentido japonês (新聞): Mincho, filetes, monocromia, um selo.
 * Nenhuma decoração além do filete: quem separa as seções é o espaço.
 */

defineProps({
  sections: { type: Array, default: () => [] },
  error: { type: String, default: '' }
})

defineEmits(['open'])

const today = computed(() =>
  capitalize(new Date().toLocaleDateString('pt-BR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  }))
)

// a data vem do servidor como AAAA-MM-DD; parsear direto evita o susto do fuso
function formatDay(iso) {
  const [year, month, day] = iso.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString('pt-BR', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}

function excerpt(content) {
  const text = content.trim().replace(/\s+/g, ' ')
  return text.length > 160 ? text.slice(0, 160) + '…' : text
}

function capitalize(text) {
  return text.charAt(0).toUpperCase() + text.slice(1)
}
</script>

<style scoped>
.journal {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  padding: var(--s6) var(--s7);
}

/* masthead: o nome, a data e o selo — a única cor da tela */
.masthead {
  display: flex;
  align-items: baseline;
  gap: var(--s4);
  border-bottom: 3px double var(--border);
  padding-bottom: var(--s3);
}

h2 {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h1);
  letter-spacing: .18em;
  margin: 0;
}

.today {
  flex: 1;
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0;
}

.hanko {
  font-family: var(--f-display);
  font-size: var(--fs-small);
  color: var(--accent);
  border: 1.5px solid var(--accent);
  border-radius: var(--r-sm);
  padding: 2px var(--s2);
}

.silence {
  color: var(--text-muted);
  margin: var(--s5) 0 0;
  max-width: 46ch;
}

.memory {
  padding-top: var(--s5);
}

.memory + .memory {
  border-top: 1px solid var(--border);
  margin-top: var(--s5);
}

.when {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0 0 var(--s3);
}

.when-date {
  opacity: .7;
}

/* duas colunas quando o dia rendeu mais de uma nota — como a página de um jornal */
.columns.two {
  columns: 2;
  column-gap: var(--s7);
  column-rule: 1px solid var(--border);
}

.piece {
  break-inside: avoid;
}

.piece + .piece {
  margin-top: var(--s4);
}

.headline {
  display: block;
  background: none;
  border: none;
  padding: 0;
  text-align: left;
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h2);
  color: var(--text);
  cursor: pointer;
  transition: color var(--dur-fast) var(--ease-ink);
}

.headline:hover {
  color: var(--link);
}

.lede {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: var(--s1) 0 0;
}

@media (max-width: 900px) {
  .journal {
    padding: var(--s5);
  }

  .columns.two {
    columns: 1;
  }
}
</style>
