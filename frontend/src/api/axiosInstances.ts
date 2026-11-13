import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'

const COMMAND_BASE_URL = import.meta.env.VITE_COMMAND_API_URL || 'http://localhost:8081'
const QUERY_BASE_URL = import.meta.env.VITE_QUERY_API_URL || 'http://localhost:8082'
const LOGIN_PATH = '/api/auth/login'

export interface ApiClientHooks {
  /** Returns the current access token, or null when nobody is signed in. */
  getAccessToken: () => string | null
  /** Called when a protected request is rejected with 401, i.e. the token expired or was revoked. */
  onUnauthorized: () => void
}

let hooks: ApiClientHooks = {
  getAccessToken: () => null,
  onUnauthorized: () => undefined,
}

/**
 * Connects the HTTP clients to application state. The store calls this once at start-up,
 * so the clients never read browser storage or navigate on their own.
 */
export function configureApiClients(nextHooks: ApiClientHooks): void {
  hooks = nextHooks
}

function attachAccessToken(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
  const token = hooks.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

function isLoginRequest(error: AxiosError): boolean {
  return (error.config?.url ?? '').includes(LOGIN_PATH)
}

function reportUnauthorized(error: unknown): Promise<never> {
  if (axios.isAxiosError(error) && error.response?.status === 401 && !isLoginRequest(error)) {
    hooks.onUnauthorized()
  }
  return Promise.reject(error)
}

function createApiClient(baseURL: string): AxiosInstance {
  const client = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
  })
  client.interceptors.request.use(attachAccessToken)
  client.interceptors.response.use((response) => response, reportUnauthorized)
  return client
}

/** Write side (create / update / delete + login). */
export const commandApi = createApiClient(COMMAND_BASE_URL)

/** Read side (list / get). */
export const queryApi = createApiClient(QUERY_BASE_URL)
