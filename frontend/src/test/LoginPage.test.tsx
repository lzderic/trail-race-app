import { describe, it, expect, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginPage } from '../pages/LoginPage'
import { makeTestStore, renderWithProviders, authStateFor } from './testUtils'

function renderLogin(store = makeTestStore()) {
  return renderWithProviders(<LoginPage />, { store })
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
    const store = makeTestStore({ auth: { ...authStateFor(null), error: 'Invalid credentials' } })
    renderLogin(store)
    expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
  })

  it('shows loading state on button', () => {
    const store = makeTestStore({ auth: { ...authStateFor(null), loading: true } })
    renderLogin(store)
    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled()
  })

  it('renders the app title', () => {
    renderLogin()
    expect(screen.getByText(/trail race app/i)).toBeInTheDocument()
  })
})
