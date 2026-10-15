import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router'
import { hotelsApi } from '@/api/hotels'
import type { HotelRequest } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageSpinner } from '@/components/shared/Spinner'
import { getErrorMessage } from '@/lib/utils'
import { RoomTypeManager } from './RoomTypeManager'

const ALL_AMENITIES = ['Wifi', 'Pool', 'Gym', 'Restaurant', 'Spa', 'Parking', 'Beach', 'Bar', 'Room Service', 'Laundry']

export function HotelFormPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isEdit = !!id

  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState<HotelRequest>({
    name: '',
    address: '',
    city: '',
    country: 'Vietnam',
    description: '',
    starRating: 3,
    amenities: [],
    images: [],
  })

  const [imageUrl, setImageUrl] = useState('')

  useEffect(() => {
    if (!isEdit) return
    hotelsApi
      .getById(parseInt(id!))
      .then((hotel) => {
        setForm({
          name: hotel.name,
          address: hotel.address,
          city: hotel.city,
          country: hotel.country,
          description: hotel.description,
          starRating: hotel.starRating,
          amenities: hotel.amenities,
          images: hotel.images,
        })
      })
      .finally(() => setLoading(false))
  }, [id, isEdit])

  function update(field: keyof HotelRequest) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
      setForm((prev) => ({ ...prev, [field]: e.target.value }))
  }

  function toggleAmenity(amenity: string) {
    setForm((prev) => ({
      ...prev,
      amenities: prev.amenities.includes(amenity)
        ? prev.amenities.filter((a) => a !== amenity)
        : [...prev.amenities, amenity],
    }))
  }

  function addImage() {
    if (!imageUrl.trim()) return
    setForm((prev) => ({ ...prev, images: [...prev.images, imageUrl.trim()] }))
    setImageUrl('')
  }

  function removeImage(idx: number) {
    setForm((prev) => ({ ...prev, images: prev.images.filter((_, i) => i !== idx) }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const payload = { ...form, starRating: parseInt(String(form.starRating)) }
      if (isEdit) {
        await hotelsApi.update(parseInt(id!), payload)
        navigate('/admin/hotels')
      } else {
        // Continue on the edit page so room types can be added right away.
        const created = await hotelsApi.create(payload)
        navigate(`/admin/hotels/${created.id}/edit`, { replace: true })
      }
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to save hotel'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <PageSpinner />

  return (
    <div className="max-w-3xl space-y-6">
      <div className="flex items-center gap-2">
        <button onClick={() => navigate('/admin/hotels')} className="text-muted-foreground hover:text-foreground text-sm">
          Hotels
        </button>
        <span className="text-muted-foreground">/</span>
        <span className="text-sm">{isEdit ? 'Edit Hotel' : 'Add Hotel'}</span>
      </div>

      <h1 className="text-2xl font-bold">{isEdit ? 'Edit Hotel' : 'Add New Hotel'}</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="rounded-lg bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
            {error}
          </div>
        )}

        <Card>
          <CardHeader><CardTitle>Basic Information</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label>Hotel Name</Label>
              <Input value={form.name} onChange={update('name')} required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Address</Label>
                <Input value={form.address} onChange={update('address')} required />
              </div>
              <div className="space-y-1.5">
                <Label>City</Label>
                <Input value={form.city} onChange={update('city')} required />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Country</Label>
                <Input value={form.country} onChange={update('country')} required />
              </div>
              <div className="space-y-1.5">
                <Label>Star Rating</Label>
                <select
                  className="w-full h-10 rounded-lg border border-border bg-input-background px-3 text-sm"
                  value={form.starRating}
                  onChange={update('starRating')}
                >
                  {[1, 2, 3, 4, 5].map((s) => (
                    <option key={s} value={s}>{s} Star{s > 1 ? 's' : ''}</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>Description</Label>
              <Textarea value={form.description} onChange={update('description')} rows={4} required />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Amenities</CardTitle></CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 sm:grid-cols-5 gap-2">
              {ALL_AMENITIES.map((amenity) => (
                <label key={amenity} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.amenities.includes(amenity)}
                    onChange={() => toggleAmenity(amenity)}
                    className="accent-primary"
                  />
                  <span className="text-sm">{amenity}</span>
                </label>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Images</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input
                placeholder="https://..."
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
              />
              <Button type="button" variant="outline" onClick={addImage}>Add</Button>
            </div>
            <div className="grid grid-cols-3 gap-2">
              {form.images.map((img, idx) => (
                <div key={idx} className="relative rounded-lg overflow-hidden h-24">
                  <img src={img} alt="" className="w-full h-full object-cover" />
                  <button
                    type="button"
                    onClick={() => removeImage(idx)}
                    className="absolute top-1 right-1 bg-black/60 text-white rounded-full w-5 h-5 text-xs hover:bg-black/80"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <div className="flex gap-3">
          <Button type="submit" disabled={saving}>
            {saving ? 'Saving...' : isEdit ? 'Save Changes' : 'Create Hotel'}
          </Button>
          <Button type="button" variant="outline" onClick={() => navigate('/admin/hotels')}>
            Cancel
          </Button>
        </div>
      </form>

      {isEdit && <RoomTypeManager hotelId={parseInt(id!)} />}
    </div>
  )
}
