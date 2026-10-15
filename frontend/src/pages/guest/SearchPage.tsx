import { useState, useEffect, useCallback } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { MapPin, Star, Search, SlidersHorizontal } from 'lucide-react'
import { hotelsApi } from '@/api/hotels'
import type { HotelResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency } from '@/lib/utils'

const STAR_OPTIONS = [5, 4, 3, 2, 1]

export function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()

  const [hotels, setHotels] = useState<HotelResponse[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)

  const [cityInput, setCityInput] = useState(searchParams.get('city') || '')
  const [nameInput, setNameInput] = useState(searchParams.get('name') || '')
  const [minRating, setMinRating] = useState<number | undefined>(undefined)
  const [sort, setSort] = useState('newest')

  const SIZE = 9

  const fetchHotels = useCallback(
    () =>
      hotelsApi
        .search({
          city: searchParams.get('city') || undefined,
          name: searchParams.get('name') || undefined,
          minRating,
          page,
          size: SIZE,
          sort,
        })
        .then((res) => {
          setHotels(res.content)
          setTotalElements(res.totalElements)
        })
        .catch(() => setHotels([]))
        .finally(() => setLoading(false)),
    [page, sort, minRating, searchParams]
  )

  useEffect(() => {
    fetchHotels()
  }, [fetchHotels])

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    const params = new URLSearchParams()
    if (cityInput) params.set('city', cityInput)
    if (nameInput) params.set('name', nameInput)
    setSearchParams(params)
    setPage(0)
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Search bar */}
      <form onSubmit={handleSearch} className="flex gap-3 mb-8">
        <div className="relative flex-1">
          <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="City"
            value={cityInput}
            onChange={(e) => setCityInput(e.target.value)}
          />
        </div>
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Hotel name"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
          />
        </div>
        <Button type="submit">Search</Button>
      </form>

      <div className="flex gap-8">
        {/* Sidebar */}
        <aside className="w-56 shrink-0">
          <div className="bg-card rounded-xl border border-border p-4">
            <div className="flex items-center gap-2 mb-4">
              <SlidersHorizontal className="h-4 w-4" />
              <span className="font-medium">Filters</span>
            </div>

            <div>
              <p className="text-sm font-medium mb-2">Min Star Rating</p>
              <div className="space-y-1">
                {STAR_OPTIONS.map((star) => (
                  <label key={star} className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="radio"
                      name="rating"
                      checked={minRating === star}
                      onChange={() => { setMinRating(star); setPage(0) }}
                      className="accent-primary"
                    />
                    <div className="flex items-center gap-0.5">
                      {Array.from({ length: star }).map((_, i) => (
                        <Star key={i} className="h-3 w-3 fill-amber-400 text-amber-400" />
                      ))}
                      <span className="text-sm ml-1">{star}+</span>
                    </div>
                  </label>
                ))}
                {minRating && (
                  <button
                    onClick={() => { setMinRating(undefined); setPage(0) }}
                    className="text-xs text-muted-foreground hover:text-destructive mt-1"
                  >
                    Clear filter
                  </button>
                )}
              </div>
            </div>

            <div className="mt-4 pt-4 border-t border-border">
              <p className="text-sm font-medium mb-2">Sort by</p>
              <select
                className="w-full text-sm border border-border rounded-lg px-2 py-1.5 bg-input-background"
                value={sort}
                onChange={(e) => { setSort(e.target.value); setPage(0) }}
              >
                <option value="newest">Newest</option>
                <option value="rating_desc">Star Rating</option>
                <option value="name_asc">Name (A–Z)</option>
              </select>
            </div>
          </div>
        </aside>

        {/* Results */}
        <div className="flex-1">
          <div className="flex items-center justify-between mb-4">
            <p className="text-sm text-muted-foreground">
              {totalElements} hotel{totalElements !== 1 ? 's' : ''} found
            </p>
          </div>

          {loading ? (
            <PageSpinner />
          ) : hotels.length === 0 ? (
            <div className="text-center py-16 text-muted-foreground">
              <Search className="h-12 w-12 mx-auto mb-3 opacity-30" />
              <p className="font-medium">No hotels found</p>
              <p className="text-sm mt-1">Try adjusting your search filters</p>
            </div>
          ) : (
            <div className="space-y-4">
              {hotels.map((hotel) => (
                <div
                  key={hotel.id}
                  onClick={() => navigate(`/hotels/${hotel.id}`)}
                  className="bg-card rounded-xl border border-border shadow-sm hover:shadow-md transition-shadow cursor-pointer flex overflow-hidden"
                >
                  <div className="w-60 shrink-0">
                    <img
                      src={hotel.images[0] || 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=400&h=250&fit=crop'}
                      alt={hotel.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 p-4 flex flex-col justify-between">
                    <div>
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="font-semibold text-foreground">{hotel.name}</h3>
                          <p className="text-sm text-muted-foreground flex items-center gap-1 mt-0.5">
                            <MapPin className="h-3 w-3" /> {hotel.city}, {hotel.country}
                          </p>
                        </div>
                        <div className="flex items-center gap-1 shrink-0">
                          <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                          <span className="text-sm font-medium">
                            {hotel.averageRating?.toFixed(1) || hotel.starRating}
                          </span>
                          {hotel.reviewCount != null && (
                            <span className="text-xs text-muted-foreground">({hotel.reviewCount})</span>
                          )}
                        </div>
                      </div>
                      <p className="text-sm text-muted-foreground mt-2 line-clamp-2">{hotel.description}</p>
                    </div>
                    <div className="flex items-center justify-between mt-3">
                      <div className="flex flex-wrap gap-1">
                        {hotel.amenities.slice(0, 4).map((a) => (
                          <span key={a} className="text-xs bg-secondary text-secondary-foreground px-2 py-0.5 rounded-full">
                            {a}
                          </span>
                        ))}
                      </div>
                      {hotel.minPrice != null && (
                        <div className="text-right shrink-0 ml-4">
                          <span className="text-xs text-muted-foreground">from </span>
                          <span className="text-xl font-bold text-primary">
                            {formatCurrency(hotel.minPrice)}
                          </span>
                          <span className="text-xs text-muted-foreground">/night</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {totalElements > SIZE && (
            <div className="flex items-center justify-center gap-2 mt-6">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {Math.ceil(totalElements / SIZE)}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={(page + 1) * SIZE >= totalElements}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
