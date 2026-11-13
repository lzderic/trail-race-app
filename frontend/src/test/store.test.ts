import { describe, it, expect, beforeEach } from 'vitest'
import { configureStore } from '@reduxjs/toolkit'
import { rootReducer } from '../store/rootReducer'
import { authPersistenceMiddleware } from '../store/authPersistence'
import {
  clearError,
  login,
  logout,
  sessionExpired,
  LOGIN_FAILED_MESSAGE,
  SESSION_EXPIRED_MESSAGE,
} from '../store/slices/authSlice'
import { makeTestStore, applicantUser, authStateFor } from './testUtils'

const credentials = { email: 'john@test.com', password: 'secret' }

function makePersistingStore() {
  return configureStore({
    reducer: rootReducer,
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().prepend(authPersistenceMiddleware.middleware),
  })
}

describe('authSlice', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts signed out when nothing is persisted', () => {
    const store = makeTestStore()
    expect(store.getState().auth.token).toBeNull()
    expect(store.getState().auth.user).toBeNull()
  })

  it('restores a persisted session on start-up', () => {
    localStorage.setItem('jwt_token', 'stored-token')
    localStorage.setItem('jwt_user', JSON.stringify(applicantUser))

    const store = makeTestStore()

    expect(store.getState().auth.token).toBe('stored-token')
    expect(store.getState().auth.user).toEqual(applicantUser)
  })

  it('ignores a corrupted persisted session instead of crashing', () => {
    localStorage.setItem('jwt_token', 'stored-token')
    localStorage.setItem('jwt_user', '{not json')

    const store = makeTestStore()

    expect(store.getState().auth.token).toBeNull()
  })

  it('stores the session when login succeeds', () => {
    const store = makeTestStore()
    store.dispatch(login.fulfilled({ token: 'tok', user: applicantUser }, 'req-1', credentials))

    expect(store.getState().auth).toEqual({
      token: 'tok',
      user: applicantUser,
      loading: false,
      error: null,
    })
  })

  it('tracks loading and error around a failed login', () => {
    const store = makeTestStore()

    store.dispatch(login.pending('req-1', credentials))
    expect(store.getState().auth.loading).toBe(true)

    store.dispatch(login.rejected(null, 'req-1', credentials, 'Invalid email or password'))
    expect(store.getState().auth.loading).toBe(false)
    expect(store.getState().auth.error).toBe('Invalid email or password')
  })

  it('falls back to a generic message when a rejection carries no payload', () => {
    const store = makeTestStore()
    store.dispatch(login.rejected(new Error('boom'), 'req-1', credentials))
    expect(store.getState().auth.error).toBe(LOGIN_FAILED_MESSAGE)
  })

  it('clears the user on logout', () => {
    const store = makeTestStore({ auth: authStateFor(applicantUser) })
    store.dispatch(logout())
    expect(store.getState().auth.token).toBeNull()
    expect(store.getState().auth.user).toBeNull()
  })

  it('signs the user out with an explanation when the session expires', () => {
    const store = makeTestStore({ auth: authStateFor(applicantUser) })
    store.dispatch(sessionExpired())
    expect(store.getState().auth.user).toBeNull()
    expect(store.getState().auth.error).toBe(SESSION_EXPIRED_MESSAGE)
  })

  it('clearError resets the error only', () => {
    const store = makeTestStore({ auth: { ...authStateFor(applicantUser), error: 'Oops' } })
    store.dispatch(clearError())
    expect(store.getState().auth.error).toBeNull()
    expect(store.getState().auth.user).toEqual(applicantUser)
  })

  it('does not mutate the previous state object', () => {
    const store = makeTestStore({ auth: authStateFor(applicantUser) })
    const before = store.getState().auth

    store.dispatch(logout())

    expect(before.user).toEqual(applicantUser)
    expect(store.getState().auth).not.toBe(before)
  })
})

describe('auth persistence middleware', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('persists the session when login succeeds', () => {
    const store = makePersistingStore()
    store.dispatch(login.fulfilled({ token: 'tok', user: applicantUser }, 'req-1', credentials))

    expect(localStorage.getItem('jwt_token')).toBe('tok')
    expect(JSON.parse(localStorage.getItem('jwt_user') ?? 'null')).toEqual(applicantUser)
  })

  it('removes the persisted session on logout', () => {
    const store = makePersistingStore()
    store.dispatch(login.fulfilled({ token: 'tok', user: applicantUser }, 'req-1', credentials))
    store.dispatch(logout())

    expect(localStorage.getItem('jwt_token')).toBeNull()
    expect(localStorage.getItem('jwt_user')).toBeNull()
  })

  it('removes the persisted session when it expires', () => {
    const store = makePersistingStore()
    store.dispatch(login.fulfilled({ token: 'tok', user: applicantUser }, 'req-1', credentials))
    store.dispatch(sessionExpired())

    expect(localStorage.getItem('jwt_token')).toBeNull()
  })
})
