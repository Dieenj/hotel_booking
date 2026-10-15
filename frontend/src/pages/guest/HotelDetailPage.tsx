import { useState, useEffect } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router'
import { MapPin, Wifi, Car, Dumbbell, UtensilsCrossed, Waves, ChevronLeft, ChevronRight } from 'lucide-react'
import { hotelsApi } from '@/api/hotels'
import { reviewsApi } from '@/api/reviews'
import { useAuth } from '@/context/AuthContext'
import type { HotelResponse, RoomTypeResponse, ReviewResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { StarRating } from '@/components/shared/StarRating'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency, formatDate } from '@/lib/utils'

const AMENITY_ICONS: Record<string, React.ReactNode> = {
  Wifi: <Wifi className="h-4 w-4" />,
  Parking: <Car className="h-4 w-4" />,
  Gym: <Dumbbell className="h-4 w-4" />,
  Restaurant: <UtensilsCrossed className="h-4 w-4" />,
  Pool: <Waves className="h-4 w-4" />,
}

export function HotelDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [hotel, setHotel] = useState<HotelResponse | null>(null)
  const [roomTypes, setRoomTypes] = useState<RoomTypeResponse[]>([])
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [galleryIndex, setGalleryIndex] = useState(0)

  const checkIn = searchParams.get('checkIn') || ''
  const checkOut = searchParams.get('checkOut') || ''

  useEffect(() => {
    if (!id) return
    const hotelId = parseInt(id)
    Promise.all([
      hotelsApi.getById(hotelId),
      hotelsApi.getRoomTypes(hotelId, {
        checkIn: checkIn || undefined,
        checkOut: checkOut || undefined,
      }),
      reviewsApi.getByHotel(hotelId, { page: 0, size: 5 }),
    ])
      .then(([h, rt, rv]) => {
        setHotel(h)
        setRoomTypes(rt)
        setReviews(rv.content)
      })
      .finally(() => setLoading(false))
  }, [id, checkIn, checkOut])

  function handleBookNow(roomType: RoomTypeResponse) {
    if (!user) {
      navigate('/login', { state: { from: { pathname: `/hotels/${id}` } } })
      return
    }
    const params = new URLSearchParams()
    params.set('roomTypeId', String(roomType.id))
    if (checkIn) params.set('checkIn', checkIn)
    if (checkOut) params.set('checkOut', checkOut)
    navigate(`/booking?${params.toString()}`)
  }

  if (loading) return <PageSpinner />
  if (!hotel) return <div className="text-center py-16">Hotel not found</div>

  const images = hotel.images.length > 0 ? hotel.images : [
    'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&h=500&fit=crop&auto=format',
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Gallery */}
      <div className="relative rounded-2xl overflow-hidden h-80 sm:h-96 mb-8 bg-secondary">
        <img
          src={images[galleryIndex]}
          alt={hotel.name}
          className="w-full h-full object-cover"
        />
        {images.length > 1 && (
          <>
            <button
              onClick={() => setGalleryIndex((i) => (i - 1 + images.length) % images.length)}
              className="absolute left-3 top-1/2 -translate-y-1/2 bg-black/50 text-white rounded-full p-2 hover:bg-black/70"
            >
              <ChevronLeft className="h-5 w-5" />
            </button>
            <button
              onClick={() => setGalleryIndex((i) => (i + 1) % images.length)}
              className="absolute right-3 top-1/2 -translate-y-1/2 bg-black/50 text-white rounded-full p-2 hover:bg-black/70"
            >
              <ChevronRight className="h-5 w-5" />
            </button>
            <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5">
              {images.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setGalleryIndex(i)}
                  className={`w-2 h-2 rounded-full transition-colors ${i === galleryIndex ? 'bg-white' : 'bg-white/50'}`}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">{hotel.name}</h1>
          <p className="text-muted-foreground flex items-center gap-1 mt-1">
            <MapPin className="h-4 w-4" /> {hotel.address}, {hotel.city}, {hotel.country}
          </p>
          {hotel.averageRating && (
            <div className="flex items-center gap-2 mt-2">
              <StarRating rating={hotel.averageRating} size="md" showValue />
              <span className="text-sm text-muted-foreground">({hotel.reviewCount} reviews)</span>
            </div>
          )}
        </div>
        <div className="flex items-center gap-1 shrink-0">
          {Array.from({ length: hotel.starRating }).map((_, i) => (
            <span key={i} className="text-amber-400 text-xl">★</span>
          ))}
        </div>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="overview">
        <TabsList className="mb-6">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="rooms">Rooms</TabsTrigger>
          <TabsTrigger value="reviews">Reviews</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <p className="text-muted-foreground leading-relaxed mb-6">{hotel.description}</p>
          <h3 className="font-semibold mb-3">Amenities</h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {hotel.amenities.map((amenity) => (
              <div key={amenity} className="flex items-center gap-2 bg-secondary rounded-lg px-3 py-2">
                {AMENITY_ICONS[amenity] || null}
                <span className="text-sm">{amenity}</span>
              </div>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="rooms">
          {roomTypes.length === 0 ? (
            <p className="text-muted-foreground text-center py-8">No room types available</p>
          ) : (
            <div className="space-y-4">
              {roomTypes.map((room) => (
                <div key={room.id} className="bg-card border border-border rounded-xl overflow-hidden flex flex-col sm:flex-row">
                  <div className="sm:w-64 h-48 sm:h-auto shrink-0">
                    <img
                      src={room.images[0] || 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&h=300&fit=crop'}
                      alt={room.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 p-5 flex flex-col justify-between">
                    <div>
                      <div className="flex items-start justify-between">
                        <h3 className="font-semibold text-lg">{room.name}</h3>
                        {room.availableRooms != null && room.availableRooms > 0 && room.availableRooms <= 3 && (
                          <span className="text-xs text-destructive font-medium bg-destructive/10 px-2 py-1 rounded-full">
                            Only {room.availableRooms} left!
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground mt-1">{room.description}</p>
                      <div className="flex gap-4 mt-2 text-sm text-muted-foreground">
                        <span>👥 Up to {room.maxCapacity} guests</span>
                        {room.area != null && <span>📐 {room.area} m²</span>}
                      </div>
                      <div className="flex flex-wrap gap-1 mt-3">
                        {room.amenities.map((a) => (
                          <span key={a} className="text-xs bg-secondary px-2 py-0.5 rounded-full">{a}</span>
                        ))}
                      </div>
                    </div>
                    <div className="flex items-center justify-between mt-4">
                      <div>
                        <span className="text-2xl font-bold text-primary">
                          {formatCurrency(room.basePrice)}
                        </span>
                        <span className="text-sm text-muted-foreground">/night</span>
                      </div>
                      <Button
                        onClick={() => handleBookNow(room)}
                        disabled={room.availableRooms === 0}
                      >
                        {room.availableRooms === 0 ? 'Unavailable' : 'Book Now'}
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="reviews">
          {reviews.length === 0 ? (
            <p className="text-muted-foreground text-center py-8">No reviews yet</p>
          ) : (
            <div className="space-y-4">
              {reviews.map((review) => (
                <div key={review.id} className="bg-card border border-border rounded-xl p-4">
                  <div className="flex items-start justify-between">
                    <div>
                      <p className="font-medium">{review.userName}</p>
                      <p className="text-xs text-muted-foreground">{formatDate(review.createdAt)}</p>
                    </div>
                    <StarRating rating={review.rating} size="sm" showValue />
                  </div>
                  {review.comment && (
                    <p className="text-sm text-muted-foreground mt-2">{review.comment}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
