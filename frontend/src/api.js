import { session } from './store'

/** Calls the backend. Throws an Error whose message can be shown to the user. */
export async function api(method, path, body) {
  let res
  try {
    res = await fetch('/api' + path, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
      credentials: 'same-origin',
    })
  } catch {
    throw new Error('No connection. Check your internet and try again.')
  }
  const data = await res.json().catch(() => null)
  if (res.status === 401) {
    session.user = null
    if (!location.pathname.startsWith('/login')) location.assign('/login')
    throw new Error('Please sign in.')
  }
  if (!res.ok) throw new Error(data?.message || 'Something went wrong. Try again.')
  return data
}

export const get = (path) => api('GET', path)
export const post = (path, body = {}) => api('POST', path, body)
export const put = (path, body) => api('PUT', path, body)
export const del = (path) => api('DELETE', path)

export async function loadSession() {
  try {
    const res = await fetch('/api/me', { credentials: 'same-origin' })
    session.user = res.ok ? await res.json() : null
  } catch {
    session.user = null
  }
  session.loaded = true
}

export function when(iso) {
  return iso ? new Date(iso).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '-'
}
