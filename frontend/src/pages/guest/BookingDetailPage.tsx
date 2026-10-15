import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { ArrowLeft, MapPin } from 'lucide-react'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'
import { BookingActions } from '@/components/shared/BookingActions'
import { formatCurrency, formatDate } from '@/lib/utils'

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-4 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right">{children}</span>
    </div>
  )
}

export function BookingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchBooking = useCallback(
    () =>
      bookingsApi
        .getById(Number(id))
        .then(setBooking)
        .catch(() => navigate('/my-bookings'))
        .finally(() => setLoading(false)),
    [id, navigate]
  )

  useEffect(() => {
    fetchBooking()
  }, [fetchBooking])

  if (loading) return <PageSpinner />
  if (!booking) return null

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Button variant="ghost" size="sm" className="mb-4 -ml-2" onClick={() => navigate('/my-bookings')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> My Bookings
      </Button>

      <div className="bg-card border border-border rounded-xl p-6 space-y-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold">{booking.hotelName}</h1>
            <p className="text-sm text-muted-foreground flex items-center gap-1 mt-1">
              <MapPin className="h-4 w-4" /> {booking.hotelAddress}
            </p>
          </div>
          <StatusBadge status={booking.status} />
        </div>

        <div className="space-y-2">
          <Row label="Booking code">
            <span className="font-mono">{booking.bookingCode}</span>
          </Row>
          <Row label="Room">{booking.roomTypeName}</Row>
          <Row label="Check-in">{formatDate(booking.checkIn)}</Row>
          <Row label="Check-out">{formatDate(booking.checkOut)}</Row>
          <Row label="Nights">{booking.nights}</Row>
          <Row label="Guests">{booking.guests}</Row>
        </div>

        <div className="space-y-2 border-t border-border pt-4">
          <Row label="Guest">{booking.guestName}</Row>
          <Row label="Email">{booking.guestEmail}</Row>
          {booking.guestPhone && <Row label="Phone">{booking.guestPhone}</Row>}
          {booking.specialRequest && <Row label="Special request">{booking.specialRequest}</Row>}
        </div>

        <div className="flex justify-between border-t border-border pt-4 font-semibold">
          <span>Total</span>
          <span className="text-primary">{formatCurrency(booking.totalPrice)}</span>
        </div>

        {booking.status === 'PENDING' && (
          <p className="text-sm text-muted-foreground">
            This booking is not paid yet. Unpaid bookings are cancelled automatically after 30 minutes.
          </p>
        )}
        {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && !booking.cancellable && (
          <p className="text-sm text-muted-foreground">
            Bookings can only be cancelled before the check-in date.
          </p>
        )}

        <div className="flex flex-wrap gap-2 justify-end">
          <BookingActions booking={booking} onChanged={fetchBooking} />
        </div>
      </div>
    </div>
  )
}
