import { Navbar } from './Navbar'
import { Outlet } from 'react-router-dom'

export function Layout() {
  return (
    <div className="layout">
      <Navbar />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

