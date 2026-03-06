import { describe, it, expect } from 'vitest'
import { render } from '@testing-library/react'
import { Spinner } from '../components/common/Spinner'
describe('Spinner', () => {
  it('renders a spinner wrapper', () => {
    const { container } = render(<Spinner />)
    expect(container.querySelector('.spinner-wrapper')).toBeTruthy()
  })
  it('renders spinner element inside wrapper', () => {
    const { container } = render(<Spinner />)
    expect(container.querySelector('.spinner')).toBeTruthy()
  })
})
