import { useState, useEffect } from 'react'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse, BookingStatus } from '@/types'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, formatDate } from '@/lib/utils'

const STATUSES: { label: string; value: BookingStatus | undefined }[] = [
  { label: 'All', value: undefined },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export function AdminBookingsPage() {
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [status, setStatus] = useState<BookingStatus | undefined>(undefined)
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)
  const SIZE = 20

  useEffect(() => {
    fetchBookings()
  }, [status, page])

  async function fetchBookings() {
    setLoading(true)
    try {
      const res = await bookingsApi.adminGetAll({ status, page, size: SIZE })
      setBookings(res.content)
      setTotal(res.totalElements)
    } finally {
      setLoading(false)
    }
  }

  async function handleConfirm(id: number) {
    await bookingsApi.adminConfirm(id)
    fetchBookings()
  }

  async function handleCancel(id: number) {
    if (!confirm('Cancel this booking?')) return
    await bookingsApi.adminCancel(id)
    fetchBookings()
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Bookings</h1>

      <div className="flex gap-1 bg-secondary rounded-lg p-1 w-fit">
        {STATUSES.map((s) => (
          <button
            key={s.label}
            onClick={() => { setStatus(s.value); setPage(0) }}
            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
              status === s.value ? 'bg-card text-foreground shadow-sm' : 'text-muted-foreground'
            }`}
          >
            {s.label}
          </button>
        ))}
      </div>

      {loading ? (
        <PageSpinner />
      ) : (
        <div className="bg-card border border-border rounded-xl overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-secondary/50">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">ID</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Guest</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Hotel / Room</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Dates</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Status</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Amount</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {bookings.map((b) => (
                <tr key={b.id} className="hover:bg-secondary/30">
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{b.bookingCode}</td>
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{b.guestName}</p>
                      <p className="text-xs text-muted-foreground">{b.guestEmail}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{b.roomType.hotelName}</p>
                      <p className="text-xs text-muted-foreground">{b.roomType.name}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">
                    {formatDate(b.checkIn)} → {formatDate(b.checkOut)}
                  </td>
                  <td className="px-4 py-3"><StatusBadge status={b.status} /></td>
                  <td className="px-4 py-3 text-right font-medium">{formatCurrency(b.totalPrice)}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-2">
                      {b.status === 'PENDING' && (
                        <Button size="sm" onClick={() => handleConfirm(b.id)}>Confirm</Button>
                      )}
                      {(b.status === 'PENDING' || b.status === 'CONFIRMED') && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="text-destructive border-destructive/30"
                          onClick={() => handleCancel(b.id)}
                        >
                          Cancel
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {bookings.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">
                    No bookings found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {total > SIZE && (
        <div className="flex items-center justify-center gap-3">
          <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {page + 1} of {Math.ceil(total / SIZE)}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={(page + 1) * SIZE >= total}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  )
}
