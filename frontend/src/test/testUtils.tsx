import type { ReactElement } from 'react'
import { render } from '@testing-library/react'
import { configureStore } from '@reduxjs/toolkit'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { rootReducer, type RootState } from '../store/rootReducer'
import type { AuthState, User } from '../types'

export function makeTestStore(preloadedState?: Partial<RootState>) {
  return configureStore({ reducer: rootReducer, preloadedState })
}

export type TestStore = ReturnType<typeof makeTestStore>

export const applicantUser: User = {
  id: 'u1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@test.com',
  role: 'Applicant',
}

export const adminUser: User = {
  id: 'u2',
  firstName: 'Admin',
  lastName: 'User',
  email: 'admin@test.com',
  role: 'Administrator',
}

export function authStateFor(user: User | null): AuthState {
  return { token: user ? 'test-token' : null, user, loading: false, error: null }
}

interface RenderOptions {
  store?: TestStore
  initialEntries?: string[]
}

export function renderWithProviders(
  ui: ReactElement,
  { store = makeTestStore(), initialEntries = ['/'] }: RenderOptions = {},
) {
  return {
    store,
    ...render(
      <Provider store={store}>
        <MemoryRouter initialEntries={initialEntries}>{ui}</MemoryRouter>
      </Provider>,
    ),
  }
}
