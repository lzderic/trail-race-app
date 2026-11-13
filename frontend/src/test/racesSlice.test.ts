import { describe, it, expect } from 'vitest'
import racesReducer, {
  clearRaceError,
  createRace,
  deleteRace,
  fetchRaces,
  patchRace,
} from '../store/slices/racesSlice'
import type { Race, RacesState } from '../types'

const raceA: Race = { id: 'r1', name: 'Mountain Trail 5K', distance: '5k' }
const raceB: Race = { id: 'r2', name: 'Alpine Marathon', distance: 'Marathon' }

const stateWith = (items: Race[], overrides: Partial<RacesState> = {}): RacesState => ({
  items,
  loading: false,
  error: null,
  ...overrides,
})

describe('racesSlice', () => {
  it('has an empty initial state', () => {
    const state = racesReducer(undefined, { type: '@@init' })
    expect(state).toEqual({ items: [], loading: false, error: null })
  })

  it('sets loading while fetching and stores the result', () => {
    const pending = racesReducer(stateWith([], { error: 'old' }), fetchRaces.pending('req', undefined))
    expect(pending.loading).toBe(true)
    expect(pending.error).toBeNull()

    const fulfilled = racesReducer(pending, fetchRaces.fulfilled([raceA, raceB], 'req', undefined))
    expect(fulfilled.loading).toBe(false)
    expect(fulfilled.items).toEqual([raceA, raceB])
  })

  it('stores the error when fetching fails', () => {
    const state = racesReducer(stateWith([]), fetchRaces.rejected(null, 'req', undefined, 'Server down'))
    expect(state.loading).toBe(false)
    expect(state.error).toBe('Server down')
  })

  it('appends a created race without mutating the previous state', () => {
    const previous = stateWith([raceA])

    const next = racesReducer(previous, createRace.fulfilled(raceB, 'req', raceB))

    expect(next.items).toEqual([raceA, raceB])
    expect(previous.items).toEqual([raceA])
    expect(next.items).not.toBe(previous.items)
  })

  it('replaces a patched race in place', () => {
    const renamed: Race = { ...raceA, name: 'Renamed' }
    const previous = stateWith([raceA, raceB])

    const next = racesReducer(previous, patchRace.fulfilled(renamed, 'req', { id: raceA.id, data: { name: 'Renamed' } }))

    expect(next.items).toEqual([renamed, raceB])
    expect(previous.items[0]).toEqual(raceA)
  })

  it('removes a deleted race', () => {
    const next = racesReducer(stateWith([raceA, raceB]), deleteRace.fulfilled(raceA.id, 'req', raceA.id))
    expect(next.items).toEqual([raceB])
  })

  it('records the error of a failed mutation', () => {
    const next = racesReducer(stateWith([raceA]), deleteRace.rejected(null, 'req', raceA.id, 'Forbidden'))
    expect(next.items).toEqual([raceA])
    expect(next.error).toBe('Forbidden')
  })

  it('clearRaceError sets error to null', () => {
    const next = racesReducer(stateWith([], { error: 'Some error' }), clearRaceError())
    expect(next.error).toBeNull()
  })
})
