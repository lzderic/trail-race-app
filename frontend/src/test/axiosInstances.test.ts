import { describe, it, expect, beforeEach, vi } from 'vitest'
import { AxiosError, AxiosHeaders, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { commandApi, configureApiClients, queryApi } from '../api/axiosInstances'

interface RequestHandler {
  fulfilled: (config: InternalAxiosRequestConfig) => InternalAxiosRequestConfig
}
interface ResponseHandler {
  rejected: (error: unknown) => Promise<never>
}

// Axios keeps registered interceptors in an internal `handlers` array; reading it lets the
// tests exercise the interceptors without performing real HTTP requests.
function requestInterceptor(client: AxiosInstance): RequestHandler {
  return (client.interceptors.request as unknown as { handlers: RequestHandler[] }).handlers[0]
}
function responseInterceptor(client: AxiosInstance): ResponseHandler {
  return (client.interceptors.response as unknown as { handlers: ResponseHandler[] }).handlers[0]
}

function requestConfig(url = '/api/races'): InternalAxiosRequestConfig {
  return { url, headers: new AxiosHeaders() } as InternalAxiosRequestConfig
}

function httpError(status: number, url: string): AxiosError {
  const config = requestConfig(url)
  return new AxiosError('Request failed', 'ERR_BAD_REQUEST', config, undefined, {
    status,
    statusText: 'Error',
    headers: {},
    config,
    data: {},
  })
}

describe('axiosInstances', () => {
  beforeEach(() => {
    configureApiClients({ getAccessToken: () => null, onUnauthorized: vi.fn() })
  })

  it('creates one client per backend service', () => {
    expect(commandApi.defaults.baseURL).toBeDefined()
    expect(queryApi.defaults.baseURL).toBeDefined()
    expect(commandApi.defaults.headers['Content-Type']).toBe('application/json')
    expect(queryApi.defaults.headers['Content-Type']).toBe('application/json')
  })

  it.each([
    ['commandApi', () => commandApi],
    ['queryApi', () => queryApi],
  ])('%s attaches the bearer token supplied by the configured hook', (_, client) => {
    configureApiClients({ getAccessToken: () => 'test-token-123', onUnauthorized: vi.fn() })

    const result = requestInterceptor(client()).fulfilled(requestConfig())

    expect(result.headers.Authorization).toBe('Bearer test-token-123')
  })

  it('does not attach an Authorization header when there is no token', () => {
    const result = requestInterceptor(commandApi).fulfilled(requestConfig())
    expect(result.headers.Authorization).toBeUndefined()
  })

  it('reports a 401 on a protected request and re-throws the error', async () => {
    const onUnauthorized = vi.fn()
    configureApiClients({ getAccessToken: () => 'tok', onUnauthorized })
    const error = httpError(401, '/api/races')

    await expect(responseInterceptor(queryApi).rejected(error)).rejects.toBe(error)
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('does not treat a failed login as an expired session', async () => {
    const onUnauthorized = vi.fn()
    configureApiClients({ getAccessToken: () => null, onUnauthorized })

    await expect(responseInterceptor(commandApi).rejected(httpError(401, '/api/auth/login'))).rejects.toBeInstanceOf(
      AxiosError,
    )
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('ignores errors other than 401', async () => {
    const onUnauthorized = vi.fn()
    configureApiClients({ getAccessToken: () => 'tok', onUnauthorized })

    await expect(responseInterceptor(commandApi).rejected(httpError(403, '/api/races'))).rejects.toBeInstanceOf(
      AxiosError,
    )
    expect(onUnauthorized).not.toHaveBeenCalled()
  })
})
