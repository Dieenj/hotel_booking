import { useNavigate } from 'react-router'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { getErrorMessage } from '@/lib/utils'

interface BookingActionsProps {
  booking: BookingResponse
  /** Called after the booking changed (e.g. cancelled) so the caller can reload. */
  onChanged: () => void
  showDetails?: boolean
}

/** The actions a guest can take on a booking in its current state. */
export function BookingActions({ booking, onChanged, showDetails = false }: BookingActionsProps) {
  const navigate = useNavigate()

  async function handleCancel() {
    if (!confirm('Are you sure you want to cancel this booking?')) return
    try {
      await bookingsApi.cancel(booking.id)
      onChanged()
    } catch (err: unknown) {
      alert(getErrorMessage(err, 'Failed to cancel booking'))
    }
  }

  return (
    <>
      {showDetails && (
        <Button variant="outline" size="sm" onClick={() => navigate(`/bookings/${booking.id}`)}>
          View Details
        </Button>
      )}
      {booking.status === 'PENDING' && (
        <Button size="sm" onClick={() => navigate(`/payment/${booking.id}`)}>
          Pay now
        </Button>
      )}
      {booking.status === 'COMPLETED' && !booking.reviewed && (
        <Button variant="outline" size="sm" onClick={() => navigate(`/write-review/${booking.id}`)}>
          Write Review
        </Button>
      )}
      {booking.cancellable && (
        <Button
          variant="ghost"
          size="sm"
          className="text-destructive hover:text-destructive hover:bg-destructive/10"
          onClick={handleCancel}
        >
          Cancel
        </Button>
      )}
    </>
  )
}
