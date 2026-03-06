import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import { MemoryRouter } from 'react-router-dom'
import authReducer from '../store/slices/authSlice'
import racesReducer from '../store/slices/racesSlice'
import applicationsReducer from '../store/slices/applicationsSlice'
import { Navbar } from '../components/layout/Navbar'

/* eslint-disable @typescript-eslint/no-explicit-any */
function makeStore(preloadedState?: any) {
  return configureStore({
    reducer: { auth: authReducer, races: racesReducer, applications: applicationsReducer },
    preloadedState,
  } as any)
}
/* eslint-enable @typescript-eslint/no-explicit-any */
const applicantUser = {
  id: 'u1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@test.com',
  role: 'Applicant' as const,
}
const adminUser = {
  id: 'u2',
  firstName: 'Admin',
  lastName: 'User',
  email: 'admin@test.com',
  role: 'Administrator' as const,
}
function renderNavbar(user: typeof applicantUser | typeof adminUser | null) {
  const store = makeStore({
    auth: { token: user ? 'tok' : null, user, loading: false, error: null },
    races: { items: [], loading: false, error: null, selectedRace: null },
    applications: { items: [], loading: false, error: null, selectedApplication: null },
  })
  return render(
    <Provider store={store}>
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    </Provider>,
  )
}
describe('Navbar', () => {
  it('renders app brand', () => {
    renderNavbar(applicantUser)
    expect(screen.getByText('Trail Race App')).toBeInTheDocument()
  })
  it('shows Races link for applicant', () => {
    renderNavbar(applicantUser)
    expect(screen.getByText('Races')).toBeInTheDocument()
  })
  it('shows My Applications link for applicant', () => {
    renderNavbar(applicantUser)
    expect(screen.getByText('My Applications')).toBeInTheDocument()
  })
  it('does not show My Applications for admin', () => {
    renderNavbar(adminUser)
    expect(screen.queryByText('My Applications')).not.toBeInTheDocument()
  })
  it('shows All Applications link for admin', () => {
    renderNavbar(adminUser)
    expect(screen.getByText('All Applications')).toBeInTheDocument()
  })
  it('does not show All Applications for applicant', () => {
    renderNavbar(applicantUser)
    expect(screen.queryByText('All Applications')).not.toBeInTheDocument()
  })
  it('shows user first name', () => {
    renderNavbar(applicantUser)
    expect(screen.getByText('John')).toBeInTheDocument()
  })
  it('shows role badge', () => {
    renderNavbar(adminUser)
    expect(screen.getByText('Administrator')).toBeInTheDocument()
  })
  it('shows logout button', () => {
    renderNavbar(applicantUser)
    expect(screen.getByText('Logout')).toBeInTheDocument()
  })
  it('does not show navigation links when not logged in', () => {
    renderNavbar(null)
    expect(screen.queryByText('Races')).not.toBeInTheDocument()
    expect(screen.queryByText('Logout')).not.toBeInTheDocument()
  })
})
