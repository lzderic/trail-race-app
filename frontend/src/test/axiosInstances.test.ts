import { describe, it, expect, beforeEach } from 'vitest'
import { commandApi, queryApi } from '../api/axiosInstances'
describe('axiosInstances', () => {
  beforeEach(() => {
    localStorage.clear()
  })
  it('commandApi has correct baseURL', () => {
    expect(commandApi.defaults.baseURL).toBeDefined()
  })
  it('queryApi has correct baseURL', () => {
    expect(queryApi.defaults.baseURL).toBeDefined()
  })
  it('commandApi has JSON content type header', () => {
    expect(commandApi.defaults.headers['Content-Type']).toBe('application/json')
  })
  it('queryApi has JSON content type header', () => {
    expect(queryApi.defaults.headers['Content-Type']).toBe('application/json')
  })
  it('commandApi request interceptor attaches token from localStorage', async () => {
    localStorage.setItem('jwt_token', 'test-token-123')
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const interceptors = (commandApi.interceptors.request as any).handlers
    expect(interceptors.length).toBeGreaterThan(0)
    const config = { headers: {} as Record<string, string> }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers['Authorization']).toBe('Bearer test-token-123')
  })
  it('commandApi request interceptor does not attach token when absent', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const interceptors = (commandApi.interceptors.request as any).handlers
    const config = { headers: {} as Record<string, string> }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers['Authorization']).toBeUndefined()
  })
  it('queryApi request interceptor attaches token from localStorage', async () => {
    localStorage.setItem('jwt_token', 'query-token-456')
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const interceptors = (queryApi.interceptors.request as any).handlers
    const config = { headers: {} as Record<string, string> }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers['Authorization']).toBe('Bearer query-token-456')
  })
})
