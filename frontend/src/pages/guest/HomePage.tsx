import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Search, MapPin, Calendar, Users, Star } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { todayIso } from '@/lib/stay'

const FEATURED_HOTELS = [
  {
    id: 1,
    name: 'The Grand Palace Hotel',
    city: 'Hanoi',
    rating: 4.8,
    reviews: 1247,
    price: 189,
    image: 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Pool', 'Gym'],
  },
  {
    id: 2,
    name: 'Lotus Bay Resort',
    city: 'Da Nang',
    rating: 4.6,
    reviews: 892,
    price: 245,
    image: 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Beach', 'Spa'],
  },
  {
    id: 3,
    name: 'Saigon Heritage Hotel',
    city: 'Ho Chi Minh City',
    rating: 4.5,
    reviews: 2103,
    price: 156,
    image: 'https://images.unsplash.com/photo-1551882547-ff40c63fe2f7?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Pool', 'Restaurant'],
  },
  {
    id: 4,
    name: 'Hoi An Ancient Boutique',
    city: 'Hoi An',
    rating: 4.9,
    reviews: 634,
    price: 132,
    image: 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Pool', 'Bikes'],
  },
  {
    id: 5,
    name: 'Pearl Bay Resort',
    city: 'Phu Quoc',
    rating: 4.7,
    reviews: 418,
    price: 320,
    image: 'https://images.unsplash.com/photo-1529290130-4ca3753253ae?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Beach', 'Spa'],
  },
  {
    id: 6,
    name: 'Ha Long Horizon Hotel',
    city: 'Ha Long',
    rating: 4.4,
    reviews: 756,
    price: 198,
    image: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=600&h=400&fit=crop&auto=format',
    amenities: ['Wifi', 'Restaurant', 'Gym'],
  },
]

const DESTINATIONS = [
  { name: 'Hanoi', hotels: 142, image: 'https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=400&h=300&fit=crop&auto=format' },
  { name: 'Ho Chi Minh City', hotels: 218, image: 'https://images.unsplash.com/photo-1583417267826-aebc4d1542e1?w=400&h=300&fit=crop&auto=format' },
  { name: 'Da Nang', hotels: 96, image: 'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=400&h=300&fit=crop&auto=format' },
  { name: 'Hoi An', hotels: 74, image: 'https://images.unsplash.com/photo-1528127269322-539801943592?w=400&h=300&fit=crop&auto=format' },
]

export function HomePage() {
  const navigate = useNavigate()
  const [city, setCity] = useState('')
  const [checkIn, setCheckIn] = useState('')
  const [checkOut, setCheckOut] = useState('')
  const [guests, setGuests] = useState('2')

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    const params = new URLSearchParams()
    if (city) params.set('city', city)
    if (checkIn) params.set('checkIn', checkIn)
    if (checkOut) params.set('checkOut', checkOut)
    if (guests) params.set('guests', guests)
    navigate(`/search?${params.toString()}`)
  }

  return (
    <div>
      {/* Hero */}
      <section
        className="relative min-h-[520px] flex items-center justify-center bg-cover bg-center"
        style={{ backgroundImage: 'url(https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1600&h=800&fit=crop&auto=format)' }}
      >
        <div className="absolute inset-0 bg-primary/60" />
        <div className="relative z-10 text-center px-4 w-full max-w-4xl mx-auto">
          <h1 className="text-4xl sm:text-5xl font-bold text-white mb-3">
            Find Your Perfect Stay
          </h1>
          <p className="text-white/80 text-lg mb-8">
            Discover amazing hotels across Vietnam at the best prices
          </p>

          {/* Search Bar */}
          <form
            onSubmit={handleSearch}
            className="bg-card rounded-2xl p-4 shadow-xl flex flex-col sm:flex-row gap-3 items-end"
          >
            <div className="flex-1 text-left">
              <label className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
                <MapPin className="h-3 w-3" /> Destination
              </label>
              <Input
                placeholder="City or hotel name"
                value={city}
                onChange={(e) => setCity(e.target.value)}
              />
            </div>
            <div className="flex-1 text-left">
              <label className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
                <Calendar className="h-3 w-3" /> Check-in
              </label>
              <Input
                type="date"
                value={checkIn}
                onChange={(e) => setCheckIn(e.target.value)}
                min={todayIso()}
              />
            </div>
            <div className="flex-1 text-left">
              <label className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
                <Calendar className="h-3 w-3" /> Check-out
              </label>
              <Input
                type="date"
                value={checkOut}
                onChange={(e) => setCheckOut(e.target.value)}
                min={checkIn || todayIso()}
              />
            </div>
            <div className="w-28 text-left">
              <label className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
                <Users className="h-3 w-3" /> Guests
              </label>
              <Input
                type="number"
                min="1"
                max="20"
                value={guests}
                onChange={(e) => setGuests(e.target.value)}
              />
            </div>
            <Button type="submit" size="lg" className="px-6 shrink-0">
              <Search className="h-4 w-4 mr-2" />
              Search
            </Button>
          </form>
        </div>
      </section>

      {/* Featured Hotels */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-semibold">Featured Hotels</h2>
          <Button variant="outline" size="sm" onClick={() => navigate('/search')}>
            View all
          </Button>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURED_HOTELS.map((hotel) => (
            <div
              key={hotel.id}
              onClick={() => navigate(`/hotels/${hotel.id}`)}
              className="bg-card rounded-xl overflow-hidden border border-border shadow-sm hover:shadow-md transition-shadow cursor-pointer group"
            >
              <div className="relative h-48 overflow-hidden">
                <img
                  src={hotel.image}
                  alt={hotel.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
              </div>
              <div className="p-4">
                <h3 className="font-semibold text-foreground truncate">{hotel.name}</h3>
                <p className="text-sm text-muted-foreground flex items-center gap-1 mt-0.5">
                  <MapPin className="h-3 w-3" /> {hotel.city}
                </p>
                <div className="flex items-center justify-between mt-3">
                  <div className="flex items-center gap-1">
                    <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                    <span className="text-sm font-medium">{hotel.rating}</span>
                    <span className="text-xs text-muted-foreground">({hotel.reviews})</span>
                  </div>
                  <div className="text-right">
                    <span className="text-lg font-bold text-primary">${hotel.price}</span>
                    <span className="text-xs text-muted-foreground">/night</span>
                  </div>
                </div>
                <div className="flex flex-wrap gap-1 mt-2">
                  {hotel.amenities.map((a) => (
                    <span key={a} className="text-xs bg-secondary text-secondary-foreground px-2 py-0.5 rounded-full">
                      {a}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="bg-secondary/50 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-2xl font-semibold mb-6">Popular Destinations</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {DESTINATIONS.map((dest) => (
              <div
                key={dest.name}
                onClick={() => navigate(`/search?city=${dest.name}`)}
                className="relative rounded-xl overflow-hidden h-48 cursor-pointer group"
              >
                <img
                  src={dest.image}
                  alt={dest.name}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
                <div className="absolute bottom-3 left-3 text-white">
                  <p className="font-semibold">{dest.name}</p>
                  <p className="text-xs text-white/80">{dest.hotels} hotels</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
