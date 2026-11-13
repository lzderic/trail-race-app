import { Navigate, Outlet } from 'react-router-dom'
import { useAppSelector } from '../../store/hooks'
import { selectCurrentUser, selectIsAuthenticated } from '../../store/slices/authSlice'
import type { Role } from '../../types'

interface ProtectedRouteProps {
  /** When set, only users with this role may enter; others are sent to the home page. */
  requiredRole?: Role
}

export function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
  const isAuthenticated = useAppSelector(selectIsAuthenticated)
  const user = useAppSelector(selectCurrentUser)

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
