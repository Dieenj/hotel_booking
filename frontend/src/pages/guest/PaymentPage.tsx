import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router'
import { Shield, CreditCard } from 'lucide-react'
import { bookingsApi } from '@/api/bookings'
import type { BookingResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, calcNights } from '@/lib/utils'

export function PaymentPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()

  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  const [card, setCard] = useState({
    number: '',
    name: '',
    expiry: '',
    cvv: '',
  })

  useEffect(() => {
    if (!bookingId) return
    bookingsApi
      .getById(parseInt(bookingId))
      .then(setBooking)
      .catch(() => navigate('/my-bookings'))
      .finally(() => setLoading(false))
  }, [bookingId])

  function updateCard(field: string) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setCard((prev) => ({ ...prev, [field]: e.target.value }))
  }

  async function handlePay(e: React.FormEvent) {
    e.preventDefault()
    if (!bookingId) return
    setSubmitting(true)
    try {
      await bookingsApi.confirmPayment(parseInt(bookingId))
      navigate(`/confirmation/${bookingId}`)
    } catch {
      navigate(`/confirmation/${bookingId}`)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <PageSpinner />
  if (!booking) return null

  const nights = calcNights(booking.checkIn, booking.checkOut)

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-2">Secure Payment</h1>
      <div className="flex items-center gap-1.5 text-sm text-muted-foreground mb-6">
        <Shield className="h-4 w-4 text-emerald-500" />
        <span>Your payment is secured with SSL encryption</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        <form onSubmit={handlePay} className="lg:col-span-3 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" />
                Card Details
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1.5">
                <Label>Card Number</Label>
                <Input
                  placeholder="1234 5678 9012 3456"
                  value={card.number}
                  onChange={updateCard('number')}
                  maxLength={19}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label>Cardholder Name</Label>
                <Input
                  placeholder="Name as on card"
                  value={card.name}
                  onChange={updateCard('name')}
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <Label>Expiry Date</Label>
                  <Input
                    placeholder="MM/YY"
                    value={card.expiry}
                    onChange={updateCard('expiry')}
                    maxLength={5}
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>CVV</Label>
                  <Input
                    placeholder="123"
                    value={card.cvv}
                    onChange={updateCard('cvv')}
                    maxLength={4}
                    required
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          <Button type="submit" size="lg" className="w-full" disabled={submitting}>
            {submitting ? 'Processing...' : `Pay Now — ${formatCurrency(booking.totalPrice)}`}
          </Button>

          <p className="text-xs text-muted-foreground text-center">
            This is a demo payment — no real charges will be made.
          </p>
        </form>

        {/* Summary */}
        <div className="lg:col-span-2">
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div>
                <p className="font-medium">{booking.roomType.hotelName}</p>
                <p className="text-muted-foreground">{booking.roomType.name}</p>
              </div>
              <div className="space-y-1.5">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Booking ID</span>
                  <span className="font-mono text-xs">{booking.bookingCode}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Check-in</span>
                  <span>{booking.checkIn}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Check-out</span>
                  <span>{booking.checkOut}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Nights</span>
                  <span>{nights}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Guests</span>
                  <span>{booking.guests}</span>
                </div>
              </div>
              <div className="border-t border-border pt-3 flex justify-between font-bold text-base">
                <span>Total</span>
                <span className="text-primary">{formatCurrency(booking.totalPrice)}</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
