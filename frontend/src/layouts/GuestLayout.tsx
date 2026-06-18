import { Outlet } from 'react-router'
import { Navbar } from '@/components/shared/Navbar'

export function GuestLayout() {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main>
        <Outlet />
      </main>
    </div>
  )
}
