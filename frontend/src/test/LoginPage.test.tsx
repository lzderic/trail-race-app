import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import { MemoryRouter } from 'react-router-dom'
import authReducer from '../store/slices/authSlice'
import racesReducer from '../store/slices/racesSlice'
import applicationsReducer from '../store/slices/applicationsSlice'
import { LoginPage } from '../pages/LoginPage'

const storeReducer = { auth: authReducer, races: racesReducer, applications: applicationsReducer }

/* eslint-disable @typescript-eslint/no-explicit-any */
function makeStore(preloadedState?: any) {
  return configureStore({ reducer: storeReducer, preloadedState } as any)
}
/* eslint-enable @typescript-eslint/no-explicit-any */
function renderLogin(store?: ReturnType<typeof makeStore>) {
  const s = store || makeStore()
  return render(
    <Provider store={s}>
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    </Provider>,
  )
}
describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear()
  })
  it('renders login form with email and password fields', () => {
    renderLogin()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
  })
  it('renders sign in button', () => {
    renderLogin()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })
  it('disables button when fields are empty', () => {
    renderLogin()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeDisabled()
  })
  it('enables button when both fields have input', async () => {
    renderLogin()
    await userEvent.type(screen.getByLabelText(/email/i), 'admin@test.com')
    await userEvent.type(screen.getByLabelText(/password/i), 'pass123')
    expect(screen.getByRole('button', { name: /sign in/i })).toBeEnabled()
  })
  it('displays error message from state', () => {
    const store = makeStore({
      auth: { token: null, user: null, loading: false, error: 'Invalid credentials' },
      races: { items: [], loading: false, error: null, selectedRace: null },
      applications: { items: [], loading: false, error: null, selectedApplication: null },
    })
    renderLogin(store)
    expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
  })
  it('shows loading state on button', () => {
    const store = makeStore({
      auth: { token: null, user: null, loading: true, error: null },
      races: { items: [], loading: false, error: null, selectedRace: null },
      applications: { items: [], loading: false, error: null, selectedApplication: null },
    })
    renderLogin(store)
    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled()
  })
  it('renders the app title', () => {
    renderLogin()
    expect(screen.getByText(/trail race app/i)).toBeInTheDocument()
  })
})
