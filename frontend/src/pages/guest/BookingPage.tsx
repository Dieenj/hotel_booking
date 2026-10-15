import { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router'
import { hotelsApi } from '@/api/hotels'
import { bookingsApi } from '@/api/bookings'
import { useAuth } from '@/context/AuthContext'
import type { RoomTypeResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, calcNights, getErrorMessage } from '@/lib/utils'

export function BookingPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { user } = useAuth()

  const roomTypeId = parseInt(searchParams.get('roomTypeId') || '0')
  const checkIn = searchParams.get('checkIn') || ''
  const checkOut = searchParams.get('checkOut') || ''
  const guestsParam = searchParams.get('guests') || '2'

  const [roomType, setRoomType] = useState<RoomTypeResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    guestName: user?.fullName || '',
    guestEmail: user?.email || '',
    guestPhone: user?.phone || '',
    specialRequest: '',
    checkIn,
    checkOut,
    guests: guestsParam,
  })

  const nights = form.checkIn && form.checkOut ? calcNights(form.checkIn, form.checkOut) : 0
  const total = roomType ? nights * roomType.basePrice : 0

  useEffect(() => {
    if (!roomTypeId) {
      navigate('/')
      return
    }
    hotelsApi
      .getRoomTypeById(roomTypeId)
      .then(setRoomType)
      .catch(() => navigate('/'))
      .finally(() => setLoading(false))
  }, [roomTypeId])

  useEffect(() => {
    if (user) {
      setForm((prev) => ({
        ...prev,
        guestName: user.fullName,
        guestEmail: user.email,
        guestPhone: user.phone || '',
      }))
    }
  }, [user])

  function update(field: string) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      setForm((prev) => ({ ...prev, [field]: e.target.value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (nights <= 0) {
      setError('Check-out must be after check-in')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const booking = await bookingsApi.create({
        roomTypeId,
        checkIn: form.checkIn,
        checkOut: form.checkOut,
        guests: parseInt(form.guests),
        guestName: form.guestName,
        guestEmail: form.guestEmail,
        guestPhone: form.guestPhone || undefined,
        specialRequest: form.specialRequest || undefined,
      })
      navigate(`/payment/${booking.id}`)
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to create booking. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <PageSpinner />
  if (!roomType) return null

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-6">Complete Your Booking</h1>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Form */}
        <form onSubmit={handleSubmit} className="lg:col-span-3 space-y-6">
          {error && (
            <div className="rounded-lg bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
              {error}
            </div>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Guest Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1.5">
                <Label>Full Name</Label>
                <Input value={form.guestName} onChange={update('guestName')} required />
              </div>
              <div className="space-y-1.5">
                <Label>Email</Label>
                <Input type="email" value={form.guestEmail} onChange={update('guestEmail')} required />
              </div>
              <div className="space-y-1.5">
                <Label>Phone (optional)</Label>
                <Input type="tel" value={form.guestPhone} onChange={update('guestPhone')} />
              </div>
              <div className="space-y-1.5">
                <Label>Special Requests (optional)</Label>
                <Textarea
                  placeholder="Any special requests or notes..."
                  value={form.specialRequest}
                  onChange={update('specialRequest')}
                  rows={3}
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Stay Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <Label>Check-in</Label>
                  <Input
                    type="date"
                    value={form.checkIn}
                    onChange={update('checkIn')}
                    min={new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>Check-out</Label>
                  <Input
                    type="date"
                    value={form.checkOut}
                    onChange={update('checkOut')}
                    min={form.checkIn || new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>
              </div>
              <div className="space-y-1.5">
                <Label>Number of Guests</Label>
                <Input
                  type="number"
                  min="1"
                  max={roomType.maxCapacity}
                  value={form.guests}
                  onChange={update('guests')}
                  required
                />
                <p className="text-xs text-muted-foreground">Max capacity: {roomType.maxCapacity} guests</p>
              </div>
            </CardContent>
          </Card>

          <Button type="submit" className="w-full" size="lg" disabled={submitting || nights <= 0}>
            {submitting ? 'Processing...' : `Proceed to Payment — ${formatCurrency(total)}`}
          </Button>
        </form>

        {/* Summary */}
        <div className="lg:col-span-2">
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle>Booking Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <img
                  src={roomType.images[0] || 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&h=250&fit=crop'}
                  alt={roomType.name}
                  className="w-full h-40 object-cover rounded-lg"
                />
                <p className="font-medium mt-2">{roomType.hotelName}</p>
                <p className="text-sm text-muted-foreground">{roomType.name}</p>
              </div>

              <div className="space-y-2 text-sm">
                {form.checkIn && form.checkOut && (
                  <>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Check-in</span>
                      <span>{form.checkIn}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Check-out</span>
                      <span>{form.checkOut}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Nights</span>
                      <span>{nights}</span>
                    </div>
                  </>
                )}
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Price/night</span>
                  <span>{formatCurrency(roomType.basePrice)}</span>
                </div>
              </div>

              {nights > 0 && (
                <div className="border-t border-border pt-3 flex justify-between font-bold text-lg">
                  <span>Total</span>
                  <span className="text-primary">{formatCurrency(total)}</span>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
