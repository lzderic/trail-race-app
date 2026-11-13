import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { clearError, login, selectIsAuthenticated } from '../store/slices/authSlice'

export function LoginPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const loading = useAppSelector((state) => state.auth.loading)
  const error = useAppSelector((state) => state.auth.error)
  const isAuthenticated = useAppSelector(selectIsAuthenticated)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true })
  }, [isAuthenticated, navigate])

  // Do not carry a stale login error over to the next visit of this page.
  useEffect(() => {
    return () => {
      dispatch(clearError())
    }
  }, [dispatch])

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    dispatch(login({ email, password }))
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-logo">
          <h1>⚡ Trail Race App</h1>
          <p>Sign in to continue</p>
        </div>

        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="email">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="form-control"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@trailrace.com"
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem' }}
            disabled={loading || !email || !password}
          >
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p
          style={{
            marginTop: '1.25rem',
            fontSize: '0.82rem',
            color: 'var(--text-muted)',
            textAlign: 'center',
          }}
        >
          Use your assigned credentials to log in.
        </p>
      </div>
    </div>
  )
}
