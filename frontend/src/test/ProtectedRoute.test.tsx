import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '../components/layout/ProtectedRoute'
import type { Role, User } from '../types'
import { adminUser, applicantUser, authStateFor, makeTestStore, renderWithProviders } from './testUtils'

function renderRoute(user: User | null, requiredRole?: Role) {
  const store = makeTestStore({ auth: authStateFor(user) })
  return renderWithProviders(
    <Routes>
      <Route element={<ProtectedRoute requiredRole={requiredRole} />}>
        <Route path="/protected" element={<div>Protected Content</div>} />
      </Route>
      <Route path="/login" element={<div>Login Page</div>} />
      <Route path="/" element={<div>Home Page</div>} />
    </Routes>,
    { store, initialEntries: ['/protected'] },
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
