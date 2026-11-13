import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { Navbar } from '../components/layout/Navbar'
import type { User } from '../types'
import { adminUser, applicantUser, authStateFor, makeTestStore, renderWithProviders } from './testUtils'

function renderNavbar(user: User | null) {
  const store = makeTestStore({ auth: authStateFor(user) })
  return renderWithProviders(<Navbar />, { store })
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
