import { describe, it, expect } from 'vitest'
import applicationsReducer, {
  clearApplicationError,
  createApplication,
  deleteApplication,
  fetchApplications,
} from '../store/slices/applicationsSlice'
import type { Application, ApplicationsState } from '../types'

const appA: Application = {
  id: 'a1',
  firstName: 'Jane',
  lastName: 'Doe',
  club: null,
  raceId: 'r1',
  userId: 'u1',
}
const appB: Application = {
  id: 'a2',
  firstName: 'Marco',
  lastName: 'Rossi',
  club: 'Trail Blazers',
  raceId: 'r2',
  userId: 'u2',
}

const stateWith = (items: Application[], overrides: Partial<ApplicationsState> = {}): ApplicationsState => ({
  items,
  loading: false,
  error: null,
  ...overrides,
})

describe('applicationsSlice', () => {
  it('has an empty initial state', () => {
    const state = applicationsReducer(undefined, { type: '@@init' })
    expect(state).toEqual({ items: [], loading: false, error: null })
  })

  it('sets loading while fetching and stores the result', () => {
    const pending = applicationsReducer(stateWith([]), fetchApplications.pending('req', undefined))
    expect(pending.loading).toBe(true)

    const fulfilled = applicationsReducer(pending, fetchApplications.fulfilled([appA], 'req', undefined))
    expect(fulfilled.loading).toBe(false)
    expect(fulfilled.items).toEqual([appA])
  })

  it('stores the error when fetching fails', () => {
    const state = applicationsReducer(
      stateWith([]),
      fetchApplications.rejected(null, 'req', undefined, 'Server down'),
    )
    expect(state.loading).toBe(false)
    expect(state.error).toBe('Server down')
  })

  it('appends a created application without mutating the previous state', () => {
    const previous = stateWith([appA])

    const next = applicationsReducer(
      previous,
      createApplication.fulfilled(appB, 'req', { firstName: 'Marco', lastName: 'Rossi', raceId: 'r2' }),
    )

    expect(next.items).toEqual([appA, appB])
    expect(previous.items).toEqual([appA])
    expect(next.items).not.toBe(previous.items)
  })

  it('removes a deleted application', () => {
    const next = applicationsReducer(stateWith([appA, appB]), deleteApplication.fulfilled(appA.id, 'req', appA.id))
    expect(next.items).toEqual([appB])
  })

  it('records the error of a failed mutation', () => {
    const next = applicationsReducer(
      stateWith([appA]),
      deleteApplication.rejected(null, 'req', appA.id, 'You can only delete your own applications'),
    )
    expect(next.items).toEqual([appA])
    expect(next.error).toBe('You can only delete your own applications')
  })

  it('clearApplicationError sets error to null', () => {
    const next = applicationsReducer(stateWith([], { error: 'Oops' }), clearApplicationError())
    expect(next.error).toBeNull()
  })
})
