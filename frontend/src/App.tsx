import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RacesListPage } from './pages/races/RacesListPage'
import { ApplicationsListPage } from './pages/applications/ApplicationsListPage'
import { AdminRacesPage } from './pages/admin/AdminRacesPage'
import { NotFoundPage } from './pages/NotFoundPage'

function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected — any authenticated user */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<RacesListPage />} />
          <Route path="/applications" element={<ApplicationsListPage />} />
        </Route>
      </Route>

      {/* Protected — Administrator only */}
      <Route element={<ProtectedRoute requiredRole="Administrator" />}>
        <Route element={<Layout />}>
          <Route path="/admin/races" element={<AdminRacesPage />} />
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  )
}

export default App

