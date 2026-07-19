<template>
  <div class="actions">
    <button class="btn-text" :disabled="busy" @click="filesInput.click()">Importar arquivos</button>
    <span class="dot">·</span>
    <button class="btn-text" :disabled="busy" @click="dirInput.click()">Importar pasta</button>
    <span class="dot">·</span>
    <button class="btn-text" :disabled="busy" @click="exportAll">Exportar</button>

    <input
      ref="filesInput"
      type="file"
      accept=".md,.markdown"
      multiple
      hidden
      @change="onPick"
    />
    <input
      ref="dirInput"
      type="file"
      webkitdirectory
      hidden
      @change="onPick"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createNote, EXPORT_URL } from '../api/notes.js'

const emit = defineEmits(['done', 'exported', 'failed'])

const filesInput = ref(null)
const dirInput = ref(null)
const busy = ref(false)

const MD = /\.(md|markdown)$/i

function titleFrom(name) {
  const base = name.replace(MD, '').trim()
  return base || 'Sem título'
}

async function onPick(event) {
  const input = event.target
  const picked = Array.from(input.files ?? [])
    .filter(f => MD.test(f.name))
    .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'))

  input.value = '' // permite re-selecionar os mesmos arquivos depois

  if (picked.length === 0) {
    emit('done', { imported: 0, failed: [] })
    return
  }

  busy.value = true
  let imported = 0
  const failed = []

  for (const file of picked) {
    try {
      const content = await file.text() // o texto do arquivo, intacto
      await createNote({ title: titleFrom(file.name), content })
      imported++
    } catch (e) {
      failed.push(`${file.name}: ${e.message}`)
    }
  }

  busy.value = false
  emit('done', { imported, failed })
}

// baixa o zip de .md — a nota sai daqui do jeito que entrou, e vai pra onde você quiser
async function exportAll() {
  busy.value = true
  try {
    const res = await fetch(EXPORT_URL)
    if (!res.ok) throw new Error(`A API respondeu ${res.status}`)
    const blob = await res.blob()
    const name = fileNameFrom(res.headers.get('content-disposition'))

    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = name
    link.click()
    URL.revokeObjectURL(url)

    emit('exported', name)
  } catch (e) {
    emit('failed', `Exportação falhou: ${e.message}`)
  } finally {
    busy.value = false
  }
}

function fileNameFrom(disposition) {
  const match = /filename="?([^";]+)"?/.exec(disposition ?? '')
  return match ? match[1] : 'commonplace-book.zip'
}
</script>

<style scoped>
.actions {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: var(--s2);
}

.btn-text {
  background: none;
  border: none;
  color: var(--text-muted);
  font-family: var(--f-body);
  font-size: var(--fs-small);
  cursor: pointer;
  padding: var(--s1) 0;
  transition: color var(--dur-fast) var(--ease-ink);
}

.btn-text:hover:enabled {
  color: var(--text);
}

.btn-text:disabled {
  opacity: 0.5;
  cursor: default;
}

.dot {
  color: var(--text-muted);
  font-size: var(--fs-small);
}
</style>
