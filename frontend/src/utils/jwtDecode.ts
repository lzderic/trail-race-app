import type { User } from '../types'

interface JwtPayload {
  sub?: string
  email?: string
  firstName?: string
  lastName?: string
  role?: string
  roles?: string[]
  id?: string
  [key: string]: unknown
}

/**
 * Decode a JWT token (no signature verification — client side only).
 * Maps standard Spring Security claims produced by the backend.
 */
export function jwtDecode(token: string): User {
  const parts = token.split('.')
  if (parts.length !== 3) throw new Error('Invalid JWT')
  const payload: JwtPayload = JSON.parse(
    atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
  )

  // Support both flat role field and Spring-style "roles" array
  const rawRole: string =
    payload.role ??
    (Array.isArray(payload.roles) ? payload.roles[0] : undefined) ??
    ''

  const role = rawRole.replace(/^ROLE_/, '') as User['role']

  return {
    id: (payload.id ?? payload.sub ?? '') as string,
    email: (payload.sub ?? payload.email ?? '') as string,
    firstName: (payload.firstName ?? '') as string,
    lastName: (payload.lastName ?? '') as string,
    role
  }
}

