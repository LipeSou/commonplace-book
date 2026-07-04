const BASE = 'http://localhost:8080/api/notes'

async function request(url, options = {}) {
  let res
  try {
    res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    })
  } catch {
    throw new Error('A API não respondeu. O Docker está de pé? (docker compose up)')
  }
  if (res.status === 204) return null
  const body = await res.json().catch(() => null)
  if (!res.ok) {
    // erros RFC 7807: usa o detail e, se houver, os erros por campo
    const fields = body?.errors ? ' — ' + Object.values(body.errors).join('; ') : ''
    throw new Error((body?.detail ?? `Erro ${res.status}`) + fields)
  }
  return body
}

export function listNotes() {
  return request(BASE)
}

export function getNote(id) {
  return request(`${BASE}/${id}`)
}

export function createNote({ title, content }) {
  return request(BASE, { method: 'POST', body: JSON.stringify({ title, content }) })
}

export function updateNote(id, { title, content }) {
  return request(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify({ title, content }) })
}

export function deleteNote(id) {
  return request(`${BASE}/${id}`, { method: 'DELETE' })
}
