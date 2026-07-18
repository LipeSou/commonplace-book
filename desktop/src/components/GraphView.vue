<template>
  <section class="graph">
    <header>
      <h2>縁 — o grafo</h2>
      <p class="meta">{{ nodes.length }} notas · {{ edges.length }} links</p>
    </header>

    <p v-if="!nodes.length" class="hint">
      Nada ainda. O grafo nasce quando uma nota cita outra com <code>[[wikilinks]]</code>.
    </p>

    <svg v-else :viewBox="`0 0 ${W} ${H}`" role="img" aria-label="Grafo das notas">
      <line
        v-for="(edge, i) in placedEdges"
        :key="'e' + i"
        :class="{ near: touches(edge) }"
        :x1="edge.x1" :y1="edge.y1" :x2="edge.x2" :y2="edge.y2"
      />
      <g
        v-for="node in placed"
        :key="node.id"
        class="node"
        :class="{ current: node.id === currentId }"
        tabindex="0"
        @click="$emit('open', node.id)"
        @keydown.enter="$emit('open', node.id)"
        @mouseenter="hovered = node.id"
        @mouseleave="hovered = null"
        @focus="hovered = node.id"
        @blur="hovered = null"
      >
        <circle :cx="node.x" :cy="node.y" :r="radius(node)" />
        <text :x="node.x" :y="node.y + radius(node) + 14">{{ node.title }}</text>
      </g>
    </svg>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

/*
 * O grafo desenhado com uma simulação de forças rodada de uma vez só (sem animação):
 * repulsão entre todas as notas, mola nas arestas, gravidade suave ao centro.
 * Determinística — o mesmo conjunto de notas cai sempre no mesmo desenho.
 */

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  edges: { type: Array, default: () => [] },
  currentId: { type: [Number, String], default: null }
})

defineEmits(['open'])

const W = 900
const H = 620
const PAD = 60
const ITERATIONS = 400

const hovered = ref(null)

const layout = computed(() => {
  const nodes = props.nodes
  const total = nodes.length
  if (!total) return []

  const at = new Map(nodes.map((n, i) => [n.id, i]))
  const springs = props.edges
    .map(e => [at.get(e.source), at.get(e.target)])
    .filter(([a, b]) => a !== undefined && b !== undefined)

  // começa num círculo: sem sorteio, o desenho é sempre o mesmo
  const p = nodes.map((n, i) => {
    const angle = (2 * Math.PI * i) / total
    return { x: Math.cos(angle) * W / 4, y: Math.sin(angle) * H / 4 }
  })

  // distância de repouso entre notas — folgada, para os rótulos respirarem (間 ma)
  const k = 1.4 * Math.sqrt((W * H) / total)
  for (let step = 0; step < ITERATIONS; step++) {
    const cool = (1 - step / ITERATIONS) * k * 0.1
    const move = p.map(() => ({ x: 0, y: 0 }))

    for (let i = 0; i < total; i++) {
      for (let j = i + 1; j < total; j++) {
        const dx = p[i].x - p[j].x
        const dy = p[i].y - p[j].y
        const d = Math.hypot(dx, dy) || 0.01
        const force = (k * k) / d
        const ux = (dx / d) * force
        const uy = (dy / d) * force
        move[i].x += ux; move[i].y += uy
        move[j].x -= ux; move[j].y -= uy
      }
    }

    for (const [a, b] of springs) {
      const dx = p[a].x - p[b].x
      const dy = p[a].y - p[b].y
      const d = Math.hypot(dx, dy) || 0.01
      const force = (d * d) / k
      const ux = (dx / d) * force
      const uy = (dy / d) * force
      move[a].x -= ux; move[a].y -= uy
      move[b].x += ux; move[b].y += uy
    }

    for (let i = 0; i < total; i++) {
      // gravidade: ninguém escapa pro infinito
      move[i].x -= p[i].x * 0.03
      move[i].y -= p[i].y * 0.03
      const d = Math.hypot(move[i].x, move[i].y) || 0.01
      const capped = Math.min(d, cool)
      p[i].x += (move[i].x / d) * capped
      p[i].y += (move[i].y / d) * capped
    }
  }

  return nodes.map((n, i) => ({ ...n, ...p[i] }))
})

// encaixa o desenho na moldura, com margem para os rótulos
const placed = computed(() => {
  const raw = layout.value
  if (!raw.length) return []
  const xs = raw.map(n => n.x)
  const ys = raw.map(n => n.y)
  const spanX = Math.max(...xs) - Math.min(...xs) || 1
  const spanY = Math.max(...ys) - Math.min(...ys) || 1
  // encolhe para caber, mas nunca amplia: aglomerado apertado não vira mancha
  const scale = Math.min((W - 2 * PAD) / spanX, (H - 2 * PAD) / spanY, 1)
  const offsetX = W / 2 - ((Math.max(...xs) + Math.min(...xs)) / 2) * scale
  const offsetY = H / 2 - ((Math.max(...ys) + Math.min(...ys)) / 2) * scale
  const fitted = raw.map(n => ({ ...n, x: n.x * scale + offsetX, y: n.y * scale + offsetY }))
  return separate(fitted)
})

// último passe: ninguém encosta em ninguém — rótulo colado em rótulo é ilegível
function separate(nodes) {
  const GAP = 96
  for (let step = 0; step < 60; step++) {
    let moved = false
    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        const dx = nodes[j].x - nodes[i].x
        const dy = (nodes[j].y - nodes[i].y) * 1.6 // rótulos são largos: aperta menos na vertical
        const d = Math.hypot(dx, dy) || 0.01
        if (d >= GAP) continue
        const push = (GAP - d) / 2
        const ux = (dx / d) * push
        const uy = (dy / d) * push
        nodes[i].x -= ux; nodes[i].y -= uy
        nodes[j].x += ux; nodes[j].y += uy
        moved = true
      }
    }
    if (!moved) break
  }
  return nodes.map(n => ({
    ...n,
    x: Math.min(Math.max(n.x, PAD), W - PAD),
    y: Math.min(Math.max(n.y, PAD), H - PAD)
  }))
}

const placedEdges = computed(() => {
  const byId = new Map(placed.value.map(n => [n.id, n]))
  return props.edges
    .map(e => ({ source: e.source, target: e.target, a: byId.get(e.source), b: byId.get(e.target) }))
    .filter(e => e.a && e.b)
    .map(e => ({ source: e.source, target: e.target, x1: e.a.x, y1: e.a.y, x2: e.b.x, y2: e.b.y }))
})

function radius(node) {
  return 5 + Math.min(Number(node.backlinks) || 0, 6) * 1.5
}

function touches(edge) {
  const focus = hovered.value ?? props.currentId
  return focus != null && (edge.source === focus || edge.target === focus)
}
</script>

<style scoped>
.graph {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--s4);
  min-width: 0;
}

h2 {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-h1);
  margin: 0;
}

.meta {
  font-family: var(--f-mono);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: var(--s1) 0 0;
}

.hint {
  color: var(--text-muted);
  font-size: var(--fs-body);
  margin: 0;
}

.hint code {
  font-family: var(--f-mono);
  font-size: var(--fs-mono);
}

svg {
  flex: 1;
  min-height: 0;
  width: 100%;
}

line {
  stroke: var(--border);
  stroke-width: 1;
  transition: stroke var(--dur-calm) var(--ease-ink);
}

line.near {
  stroke: var(--link);
}

.node {
  cursor: pointer;
}

circle {
  fill: var(--text-muted);
  transition: fill var(--dur-fast) var(--ease-ink);
}

.node:hover circle,
.node:focus-visible circle {
  fill: var(--link);
}

.node.current circle {
  fill: var(--accent);
}

text {
  fill: var(--text-muted);
  font-family: var(--f-display);
  font-size: var(--fs-small);
  text-anchor: middle;
  transition: fill var(--dur-fast) var(--ease-ink);
}

.node:hover text,
.node:focus-visible text,
.node.current text {
  fill: var(--text);
}

.node:focus {
  outline: none;
}

.node:focus-visible circle {
  stroke: var(--accent);
  stroke-width: 2;
}
</style>
