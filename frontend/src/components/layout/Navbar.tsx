import { NavLink, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { logout } from '../../store/slices/authSlice'

export function Navbar() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const user = useAppSelector((s) => s.auth.user)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  const isAdmin = user?.role === 'Administrator'

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
        </svg>
        <span>Trail Race App</span>
      </div>

      {user && (
        <div className="navbar-links">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Races
          </NavLink>
          {!isAdmin && (
            <NavLink to="/applications" className={({ isActive }) => (isActive ? 'active' : '')}>
              My Applications
            </NavLink>
          )}
          {isAdmin && (
            <NavLink
              to="/admin/applications"
              className={({ isActive }) => (isActive ? 'active' : '')}
            >
              All Applications
            </NavLink>
          )}
          <span className="navbar-user">
            {user.firstName || user.email}
            <span className="role-badge">{user.role}</span>
          </span>
          <button className="nav-link" onClick={handleLogout}>
            Logout
          </button>
        </div>
      )}
    </nav>
  )
}
