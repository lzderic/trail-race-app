import axios from 'axios'

export const DEFAULT_ERROR_MESSAGE = 'Something went wrong. Please try again.'
export const NETWORK_ERROR_MESSAGE = 'Unable to reach the server. Please check your connection.'

/** RFC 9457 Problem Details body, which both backend services return for every error. */
export interface ProblemDetails {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  /** Field name → validation message, present on 400 validation failures. */
  errors?: Record<string, string>
}

function isProblemDetails(value: unknown): value is ProblemDetails {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function describeProblem(problem: ProblemDetails): string | undefined {
  if (problem.errors && Object.keys(problem.errors).length > 0) {
    return Object.entries(problem.errors)
      .map(([field, message]) => `${field} ${message}`)
      .join('. ')
  }
  return problem.detail || problem.title || undefined
}

/**
 * Turns any error thrown by an API call into a message that can be shown to the user.
 * Prefers the backend's Problem Details, then falls back to the supplied default.
 */
export function getApiErrorMessage(error: unknown, fallback: string = DEFAULT_ERROR_MESSAGE): string {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error && error.message ? error.message : fallback
  }
  if (!error.response) {
    return NETWORK_ERROR_MESSAGE
  }

  const body: unknown = error.response.data
  if (typeof body === 'string' && body.trim()) {
    return body
  }
  if (isProblemDetails(body)) {
    return describeProblem(body) ?? fallback
  }
  return fallback
}
