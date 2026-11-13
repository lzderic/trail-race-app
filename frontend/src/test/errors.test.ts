import { describe, it, expect } from 'vitest'
import { AxiosError, AxiosHeaders, type InternalAxiosRequestConfig } from 'axios'
import { DEFAULT_ERROR_MESSAGE, NETWORK_ERROR_MESSAGE, getApiErrorMessage } from '../api/errors'

function axiosErrorWith(data: unknown, status = 400): AxiosError {
  const config = { url: '/api/test', headers: new AxiosHeaders() } as InternalAxiosRequestConfig
  return new AxiosError('Request failed', 'ERR_BAD_REQUEST', config, undefined, {
    status,
    statusText: 'Error',
    headers: {},
    config,
    data,
  })
}

describe('getApiErrorMessage', () => {
  it('uses the Problem Details detail field', () => {
    const error = axiosErrorWith({ status: 404, title: 'Not Found', detail: 'Race not found with id: 123' })
    expect(getApiErrorMessage(error)).toBe('Race not found with id: 123')
  })

  it('lists validation errors field by field', () => {
    const error = axiosErrorWith({
      status: 400,
      detail: 'Validation failed',
      errors: { name: 'must not be blank', distance: 'must not be null' },
    })
    expect(getApiErrorMessage(error)).toBe('name must not be blank. distance must not be null')
  })

  it('falls back to the title when there is no detail', () => {
    expect(getApiErrorMessage(axiosErrorWith({ status: 500, title: 'Internal Server Error' }))).toBe(
      'Internal Server Error',
    )
  })

  it('returns a plain-text body as is', () => {
    expect(getApiErrorMessage(axiosErrorWith('Invalid email or password', 401))).toBe('Invalid email or password')
  })

  it('returns the supplied fallback for an unrecognised body', () => {
    expect(getApiErrorMessage(axiosErrorWith(42), 'Custom fallback')).toBe('Custom fallback')
    expect(getApiErrorMessage(axiosErrorWith({}))).toBe(DEFAULT_ERROR_MESSAGE)
  })

  it('explains network failures', () => {
    const error = new AxiosError('Network Error', 'ERR_NETWORK')
    expect(getApiErrorMessage(error)).toBe(NETWORK_ERROR_MESSAGE)
  })

  it('uses the message of non-HTTP errors', () => {
    expect(getApiErrorMessage(new Error('Invalid JWT'))).toBe('Invalid JWT')
    expect(getApiErrorMessage('weird', 'Fallback')).toBe('Fallback')
  })
})
