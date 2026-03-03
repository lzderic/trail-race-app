import { Navigate, Outlet } from 'react-router-dom'
import { useAppSelector } from '../../store/hooks'

interface ProtectedRouteProps {
  requiredRole?: 'Administrator' | 'Applicant'
}

export function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
  const { token, user } = useAppSelector((s) => s.auth)

  if (!token || !user) {
    return <Navigate to="/login" replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

