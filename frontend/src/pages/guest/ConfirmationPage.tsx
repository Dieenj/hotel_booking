import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router'
import { CheckCircle } from 'lucide-react'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, formatDate, calcNights } from '@/lib/utils'

export function ConfirmationPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!bookingId) return
    bookingsApi
      .getById(parseInt(bookingId))
      .then(setBooking)
      .catch(() => navigate('/'))
      .finally(() => setLoading(false))
  }, [bookingId])

  if (loading) return <PageSpinner />
  if (!booking) return null

  const nights = calcNights(booking.checkIn, booking.checkOut)

  return (
    <div className="max-w-xl mx-auto px-4 py-16 text-center">
      <div className="flex justify-center mb-4">
        <CheckCircle className="h-16 w-16 text-emerald-500" />
      </div>
      <h1 className="text-3xl font-bold text-foreground mb-2">Booking Confirmed!</h1>
      <p className="text-muted-foreground mb-6">
        Your reservation has been successfully processed.
      </p>

      <div className="bg-card border border-border rounded-xl p-6 text-left space-y-3 mb-6">
        <div className="text-center mb-4">
          <span className="font-mono text-sm bg-secondary px-3 py-1.5 rounded-full">
            {booking.bookingCode}
          </span>
        </div>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Hotel</span>
            <span className="font-medium">{booking.roomType.hotelName}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Room</span>
            <span>{booking.roomType.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Check-in</span>
            <span>{formatDate(booking.checkIn)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Check-out</span>
            <span>{formatDate(booking.checkOut)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Duration</span>
            <span>{nights} night{nights !== 1 ? 's' : ''}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Guests</span>
            <span>{booking.guests}</span>
          </div>
          <div className="border-t border-border pt-2 flex justify-between font-bold">
            <span>Total Paid</span>
            <span className="text-primary">{formatCurrency(booking.totalPrice)}</span>
          </div>
        </div>
      </div>

      <p className="text-sm text-muted-foreground mb-6">
        A confirmation has been sent to <strong>{booking.guestEmail}</strong>
      </p>

      <div className="flex gap-3 justify-center">
        <Button asChild>
          <Link to="/my-bookings">View My Bookings</Link>
        </Button>
        <Button variant="outline" asChild>
          <Link to="/">Back to Home</Link>
        </Button>
      </div>
    </div>
  )
}
