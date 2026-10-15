import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router'
import { Star } from 'lucide-react'
import { bookingsApi } from '@/api/bookings'
import { reviewsApi } from '@/api/reviews'
import type { BookingResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageSpinner } from '@/components/shared/Spinner'
import { cn, getErrorMessage } from '@/lib/utils'

function RatingInput({ label, value, onChange }: { label: string; value: number; onChange: (v: number) => void }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-muted-foreground">{label}</span>
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((i) => (
          <button key={i} type="button" onClick={() => onChange(i)}>
            <Star className={cn('h-5 w-5', i <= value ? 'fill-amber-400 text-amber-400' : 'text-stone-300')} />
          </button>
        ))}
      </div>
    </div>
  )
}

export function WriteReviewPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()

  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const [rating, setRating] = useState(5)
  const [cleanlinessRating, setCleanlinessRating] = useState(5)
  const [serviceRating, setServiceRating] = useState(5)
  const [locationRating, setLocationRating] = useState(5)
  const [valueRating, setValueRating] = useState(5)
  const [comment, setComment] = useState('')

  useEffect(() => {
    if (!bookingId) return
    bookingsApi
      .getById(parseInt(bookingId))
      .then(setBooking)
      .catch(() => navigate('/my-bookings'))
      .finally(() => setLoading(false))
  }, [bookingId, navigate])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!bookingId) return
    setSubmitting(true)
    setError('')
    try {
      await reviewsApi.create({
        bookingId: parseInt(bookingId),
        rating,
        cleanlinessRating,
        serviceRating,
        locationRating,
        valueRating,
        comment: comment || undefined,
      })
      navigate('/my-bookings')
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to submit review'))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <PageSpinner />
  if (!booking) return null

  return (
    <div className="max-w-xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-6">Write a Review</h1>

      <Card className="mb-4">
        <CardContent className="pt-4">
          <p className="font-medium">{booking.hotelName}</p>
          <p className="text-sm text-muted-foreground">{booking.roomTypeName}</p>
          <p className="text-xs text-muted-foreground mt-1">
            {booking.checkIn} → {booking.checkOut}
          </p>
        </CardContent>
      </Card>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>Your Rating</CardTitle>
          </CardHeader>
          <CardContent className="space-y-5">
            {error && (
              <div className="rounded-lg bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
                {error}
              </div>
            )}

            <div>
              <Label className="mb-2 block">Overall Rating</Label>
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((i) => (
                  <button key={i} type="button" onClick={() => setRating(i)}>
                    <Star className={cn('h-7 w-7', i <= rating ? 'fill-amber-400 text-amber-400' : 'text-stone-300')} />
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-3 pt-2 border-t border-border">
              <RatingInput label="Cleanliness" value={cleanlinessRating} onChange={setCleanlinessRating} />
              <RatingInput label="Service" value={serviceRating} onChange={setServiceRating} />
              <RatingInput label="Location" value={locationRating} onChange={setLocationRating} />
              <RatingInput label="Value for Money" value={valueRating} onChange={setValueRating} />
            </div>

            <div className="space-y-1.5">
              <Label>Your Experience</Label>
              <Textarea
                placeholder="Share your experience with other travelers..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={4}
              />
            </div>

            <div className="flex gap-3">
              <Button type="submit" disabled={submitting}>
                {submitting ? 'Submitting...' : 'Submit Review'}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-bookings')}>
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  )
}
