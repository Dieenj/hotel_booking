import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import { Plus, Edit, Trash2, Star } from 'lucide-react'
import { hotelsApi } from '@/api/hotels'
import type { HotelResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'

export function HotelsPage() {
  const navigate = useNavigate()
  const [hotels, setHotels] = useState<HotelResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchHotels()
  }, [])

  async function fetchHotels() {
    setLoading(true)
    try {
      const res = await hotelsApi.search({ page: 0, size: 50 })
      setHotels(res.content)
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: number, name: string) {
    if (!confirm(`Delete "${name}"? This will remove all associated room types.`)) return
    try {
      await hotelsApi.delete(id)
      fetchHotels()
    } catch {
      alert('Failed to delete hotel')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Hotels</h1>
        <Button onClick={() => navigate('/admin/hotels/new')}>
          <Plus className="h-4 w-4 mr-2" />
          Add Hotel
        </Button>
      </div>

      {loading ? (
        <PageSpinner />
      ) : (
        <div className="bg-card border border-border rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-secondary/50">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Hotel</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Location</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Stars</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Status</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {hotels.map((hotel) => (
                <tr key={hotel.id} className="hover:bg-secondary/30">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <img
                        src={hotel.images[0] || 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=80&h=60&fit=crop'}
                        alt={hotel.name}
                        className="w-12 h-9 object-cover rounded-md"
                      />
                      <span className="font-medium">{hotel.name}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{hotel.city}, {hotel.country}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-0.5">
                      {Array.from({ length: hotel.starRating }).map((_, i) => (
                        <Star key={i} className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <Badge variant={hotel.active ? 'confirmed' : 'cancelled'}>
                      {hotel.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate(`/admin/hotels/${hotel.id}/rooms`)}
                        title="Manage Rooms"
                      >
                        <span className="text-xs">Rooms</span>
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate(`/admin/hotels/${hotel.id}/edit`)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-destructive hover:text-destructive"
                        onClick={() => handleDelete(hotel.id, hotel.name)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
              {hotels.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-muted-foreground">
                    No hotels yet.{' '}
                    <button onClick={() => navigate('/admin/hotels/new')} className="text-primary hover:underline">
                      Add one
                    </button>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
