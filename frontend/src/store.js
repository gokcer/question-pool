import { reactive } from 'vue'

// Who is signed in. "loaded" turns true after the first /api/me call.
export const session = reactive({ user: null, loaded: false })

export function isInstructor() {
  return session.user?.role === 'INSTRUCTOR'
}
