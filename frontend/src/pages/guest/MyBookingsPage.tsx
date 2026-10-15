import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse, BookingStatus } from '@/types'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, formatDate } from '@/lib/utils'

const TABS: { label: string; value: BookingStatus | undefined }[] = [
  { label: 'All', value: undefined },
  { label: 'Upcoming', value: 'CONFIRMED' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export function MyBookingsPage() {
  const navigate = useNavigate()
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<BookingStatus | undefined>(undefined)

  const fetchBookings = useCallback(async () => {
    try {
      const res = await bookingsApi.getMyBookings({ status: activeTab, page: 0, size: 20 })
      setBookings(res.content)
    } finally {
      setLoading(false)
    }
  }, [activeTab])

  useEffect(() => {
    fetchBookings()
  }, [fetchBookings])

  async function handleCancel(bookingId: number) {
    if (!confirm('Are you sure you want to cancel this booking?')) return
    try {
      await bookingsApi.cancel(bookingId)
      fetchBookings()
    } catch {
      alert('Failed to cancel booking')
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-6">My Bookings</h1>

      {/* Tabs */}
      <div className="flex gap-1 bg-secondary rounded-lg p-1 mb-6 w-fit">
        {TABS.map((tab) => (
          <button
            key={tab.label}
            onClick={() => setActiveTab(tab.value)}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
              activeTab === tab.value
                ? 'bg-card text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <PageSpinner />
      ) : bookings.length === 0 ? (
        <div className="text-center py-16 text-muted-foreground">
          <p className="font-medium">No bookings found</p>
          <Button className="mt-4" onClick={() => navigate('/search')}>
            Browse Hotels
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="bg-card border border-border rounded-xl p-5 flex flex-col sm:flex-row gap-4"
            >
              <div className="flex-1">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <p className="font-semibold">{booking.hotelName}</p>
                    <p className="text-sm text-muted-foreground">{booking.roomTypeName}</p>
                  </div>
                  <StatusBadge status={booking.status} />
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-sm mt-3">
                  <div>
                    <p className="text-muted-foreground text-xs">Check-in</p>
                    <p className="font-medium">{formatDate(booking.checkIn)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground text-xs">Check-out</p>
                    <p className="font-medium">{formatDate(booking.checkOut)}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground text-xs">Booking ID</p>
                    <p className="font-mono text-xs">{booking.bookingCode}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground text-xs">Total</p>
                    <p className="font-bold text-primary">{formatCurrency(booking.totalPrice)}</p>
                  </div>
                </div>
              </div>

              <div className="flex flex-col gap-2 sm:items-end justify-center shrink-0">
                <Button variant="outline" size="sm" onClick={() => navigate(`/bookings/${booking.id}`)}>
                  View Details
                </Button>
                {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    onClick={() => handleCancel(booking.id)}
                  >
                    Cancel
                  </Button>
                )}
                {booking.status === 'COMPLETED' && (
                  <Button variant="outline" size="sm" onClick={() => navigate(`/write-review/${booking.id}`)}>
                    Write Review
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
