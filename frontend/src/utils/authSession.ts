import type { AuthSession, User } from '../types'

const TOKEN_KEY = 'jwt_token'
const USER_KEY = 'jwt_user'

/**
 * Persistence for the signed-in session. These helpers are the only code that touches
 * browser storage; the Redux store remains the single in-memory source of truth and the
 * reducers stay free of side effects.
 */
export function loadAuthSession(): AuthSession | null {
  try {
    const token = localStorage.getItem(TOKEN_KEY)
    const rawUser = localStorage.getItem(USER_KEY)
    if (!token || !rawUser) return null
    return { token, user: JSON.parse(rawUser) as User }
  } catch {
    // Storage unavailable or corrupted: start signed out rather than crash on boot.
    return null
  }
}

export function saveAuthSession({ token, user }: AuthSession): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } catch {
    // Storage unavailable (private mode, quota): the session still lives in the store.
  }
}

export function clearAuthSession(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  } catch {
    // Nothing to clean up when storage is unavailable.
  }
}
