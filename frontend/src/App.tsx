import { BrowserRouter, Routes, Route, Navigate } from 'react-router'
import { Toaster } from 'sonner'
import { AuthProvider } from '@/context/AuthContext'
import { GuestLayout } from '@/layouts/GuestLayout'
import { AdminLayout } from '@/layouts/AdminLayout'
import { ProtectedRoute } from '@/layouts/ProtectedRoute'

import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'

import { HomePage } from '@/pages/guest/HomePage'
import { SearchPage } from '@/pages/guest/SearchPage'
import { HotelDetailPage } from '@/pages/guest/HotelDetailPage'
import { BookingPage } from '@/pages/guest/BookingPage'
import { PaymentPage } from '@/pages/guest/PaymentPage'
import { ConfirmationPage } from '@/pages/guest/ConfirmationPage'
import { MyBookingsPage } from '@/pages/guest/MyBookingsPage'
import { BookingDetailPage } from '@/pages/guest/BookingDetailPage'
import { ProfilePage } from '@/pages/guest/ProfilePage'
import { WriteReviewPage } from '@/pages/guest/WriteReviewPage'

import { DashboardPage } from '@/pages/admin/DashboardPage'
import { HotelsPage } from '@/pages/admin/HotelsPage'
import { HotelFormPage } from '@/pages/admin/HotelFormPage'
import { AdminBookingsPage } from '@/pages/admin/AdminBookingsPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Auth pages (no layout) */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Guest layout */}
          <Route element={<GuestLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/hotels/:id" element={<HotelDetailPage />} />

            {/* Protected guest routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/booking" element={<BookingPage />} />
              <Route path="/payment/:bookingId" element={<PaymentPage />} />
              <Route path="/confirmation/:bookingId" element={<ConfirmationPage />} />
              <Route path="/my-bookings" element={<MyBookingsPage />} />
              <Route path="/bookings/:id" element={<BookingDetailPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/write-review/:bookingId" element={<WriteReviewPage />} />
            </Route>
          </Route>

          {/* Admin layout */}
          <Route element={<ProtectedRoute requireAdmin />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin" element={<DashboardPage />} />
              <Route path="/admin/hotels" element={<HotelsPage />} />
              <Route path="/admin/hotels/new" element={<HotelFormPage />} />
              <Route path="/admin/hotels/:id/edit" element={<HotelFormPage />} />
              <Route path="/admin/bookings" element={<AdminBookingsPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        <Toaster position="top-right" richColors />
      </AuthProvider>
    </BrowserRouter>
  )
}
