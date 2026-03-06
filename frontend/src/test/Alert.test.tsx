import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Alert } from '../components/common/Alert'

describe('Alert component', () => {
  it('renders error message', () => {
    render(<Alert message="Something went wrong" type="error" />)
    expect(screen.getByText('Something went wrong')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveClass('alert-error')
  })

  it('renders success message', () => {
    render(<Alert message="Done!" type="success" />)
    expect(screen.getByRole('alert')).toHaveClass('alert-success')
  })

  it('renders info message by default type error', () => {
    render(<Alert message="Note" />)
    expect(screen.getByRole('alert')).toHaveClass('alert-error')
  })

  it('renders close button when onClose provided', () => {
    render(<Alert message="Closable" onClose={() => {}} />)
    expect(screen.getByRole('button', { name: /close/i })).toBeInTheDocument()
  })

  it('does not render close button without onClose', () => {
    render(<Alert message="No close" />)
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })
})
