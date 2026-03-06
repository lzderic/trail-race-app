import { describe, it, expect } from 'vitest'
import { configureStore } from '@reduxjs/toolkit'
import racesReducer, { clearRaceError, clearSelectedRace } from '../store/slices/racesSlice'

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function makeStore(preloadedState?: any) {
  return configureStore({
    reducer: { races: racesReducer },
    preloadedState,
  })
}
describe('racesSlice — extended', () => {
  it('clearSelectedRace sets selectedRace to null', () => {
    const store = makeStore({
      races: {
        items: [],
        loading: false,
        error: null,
        selectedRace: { id: 'r1', name: 'Test', distance: '5k' },
      },
    })
    store.dispatch(clearSelectedRace())
    expect(store.getState().races.selectedRace).toBeNull()
  })
  it('clearRaceError sets error to null', () => {
    const store = makeStore({
      races: { items: [], loading: false, error: 'Some error', selectedRace: null },
    })
    store.dispatch(clearRaceError())
    expect(store.getState().races.error).toBeNull()
  })
  it('initial state has empty items and no error', () => {
    const store = makeStore()
    const state = store.getState().races
    expect(state.items).toEqual([])
    expect(state.loading).toBe(false)
    expect(state.error).toBeNull()
    expect(state.selectedRace).toBeNull()
  })
})
