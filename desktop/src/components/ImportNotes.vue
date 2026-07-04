<template>
  <div class="import">
    <button class="btn-text" :disabled="busy" @click="filesInput.click()">Importar arquivos</button>
    <span class="dot">·</span>
    <button class="btn-text" :disabled="busy" @click="dirInput.click()">Importar pasta</button>

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
import { createNote } from '../api/notes.js'

const emit = defineEmits(['done'])

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
</script>

<style scoped>
.import {
  display: flex;
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
