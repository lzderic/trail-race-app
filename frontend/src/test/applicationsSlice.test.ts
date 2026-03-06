import { describe, it, expect } from 'vitest'
import { configureStore } from '@reduxjs/toolkit'
import applicationsReducer, {
  clearApplicationError,
  clearSelectedApplication,
} from '../store/slices/applicationsSlice'

/* eslint-disable @typescript-eslint/no-explicit-any */
function makeStore(preloadedState?: any) {
  return configureStore({
    reducer: { applications: applicationsReducer },
    preloadedState,
  } as any)
}
/* eslint-enable @typescript-eslint/no-explicit-any */
describe('applicationsSlice — extended', () => {
  it('clearSelectedApplication sets selectedApplication to null', () => {
    const store = makeStore({
      applications: {
        items: [],
        loading: false,
        error: null,
        selectedApplication: { id: 'a1', firstName: 'J', lastName: 'D', raceId: 'r1' },
      },
    })
    store.dispatch(clearSelectedApplication())
    expect(store.getState().applications.selectedApplication).toBeNull()
  })
  it('clearApplicationError sets error to null', () => {
    const store = makeStore({
      applications: { items: [], loading: false, error: 'Oops', selectedApplication: null },
    })
    store.dispatch(clearApplicationError())
    expect(store.getState().applications.error).toBeNull()
  })
  it('initial state has empty items and no error', () => {
    const store = makeStore()
    const state = store.getState().applications
    expect(state.items).toEqual([])
    expect(state.loading).toBe(false)
    expect(state.error).toBeNull()
    expect(state.selectedApplication).toBeNull()
  })
})
