import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="empty-state" style={{ paddingTop: '5rem' }}>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 8v4m0 4h.01" />
      </svg>
      <h3 style={{ fontSize: '2rem' }}>404</h3>
      <p>Page not found.</p>
      <Link to="/" className="btn btn-primary" style={{ marginTop: '1rem', display: 'inline-flex' }}>
        Go Home
      </Link>
    </div>
  )
}

