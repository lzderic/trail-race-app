import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import authReducer from '../store/slices/authSlice'
import racesReducer from '../store/slices/racesSlice'
import applicationsReducer from '../store/slices/applicationsSlice'
import { ProtectedRoute } from '../components/layout/ProtectedRoute'

const storeReducer = { auth: authReducer, races: racesReducer, applications: applicationsReducer }

/* eslint-disable @typescript-eslint/no-explicit-any */
function makeStore(preloadedState?: any) {
  return configureStore({ reducer: storeReducer, preloadedState } as any)
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
function renderRoute(
  user: typeof applicantUser | typeof adminUser | null,
  requiredRole?: 'Administrator' | 'Applicant',
) {
  const store = makeStore({
    auth: { token: user ? 'tok' : null, user, loading: false, error: null },
    races: { items: [], loading: false, error: null, selectedRace: null },
    applications: { items: [], loading: false, error: null, selectedApplication: null },
  })
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route element={<ProtectedRoute requiredRole={requiredRole} />}>
            <Route path="/protected" element={<div>Protected Content</div>} />
          </Route>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/" element={<div>Home Page</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  )
}
describe('ProtectedRoute', () => {
  it('redirects to login when not authenticated', () => {
    renderRoute(null)
    expect(screen.getByText('Login Page')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })
  it('renders outlet when authenticated with no role requirement', () => {
    renderRoute(applicantUser)
    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })
  it('renders outlet when user has required role', () => {
    renderRoute(adminUser, 'Administrator')
    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })
  it('redirects to home when user does not have required role', () => {
    renderRoute(applicantUser, 'Administrator')
    expect(screen.getByText('Home Page')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })
  it('renders outlet when applicant accesses applicant-only route', () => {
    renderRoute(applicantUser, 'Applicant')
    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })
  it('redirects admin from applicant-only route', () => {
    renderRoute(adminUser, 'Applicant')
    expect(screen.getByText('Home Page')).toBeInTheDocument()
  })
})
