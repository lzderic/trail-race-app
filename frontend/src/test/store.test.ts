import { describe, it, expect } from 'vitest'
import { configureStore } from '@reduxjs/toolkit'
import authReducer, { logout } from '../store/slices/authSlice'
import racesReducer, { clearRaceError } from '../store/slices/racesSlice'
import applicationsReducer, { clearApplicationError } from '../store/slices/applicationsSlice'

function makeStore() {
  return configureStore({
    reducer: { auth: authReducer, races: racesReducer, applications: applicationsReducer }
  })
}

describe('authSlice', () => {
  it('has null token initially (no localStorage)', () => {
    localStorage.clear()
    const store = makeStore()
    expect(store.getState().auth.token).toBeNull()
  })

  it('clears user on logout', () => {
    const store = makeStore()
    store.dispatch(logout())
    expect(store.getState().auth.token).toBeNull()
    expect(store.getState().auth.user).toBeNull()
  })
})

describe('racesSlice', () => {
  it('starts with empty items', () => {
    const store = makeStore()
    expect(store.getState().races.items).toEqual([])
  })

  it('clearRaceError sets error to null', () => {
    const store = makeStore()
    store.dispatch(clearRaceError())
    expect(store.getState().races.error).toBeNull()
  })
})

describe('applicationsSlice', () => {
  it('starts with empty items', () => {
    const store = makeStore()
    expect(store.getState().applications.items).toEqual([])
  })

  it('clearApplicationError sets error to null', () => {
    const store = makeStore()
    store.dispatch(clearApplicationError())
    expect(store.getState().applications.error).toBeNull()
  })
})

