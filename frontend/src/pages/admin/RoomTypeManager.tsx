import { useCallback, useEffect, useState } from 'react'
import { Edit, Plus, Trash2 } from 'lucide-react'
import { hotelsApi } from '@/api/hotels'
import type { RoomTypeRequest, RoomTypeResponse } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Spinner } from '@/components/shared/Spinner'
import { formatCurrency, getErrorMessage } from '@/lib/utils'

/** Form values as typed (strings), converted to a RoomTypeRequest on save. */
interface RoomTypeForm {
  name: string
  description: string
  maxCapacity: string
  area: string
  basePrice: string
  totalRooms: string
  amenities: string
  images: string
}

const EMPTY_FORM: RoomTypeForm = {
  name: '',
  description: '',
  maxCapacity: '2',
  area: '',
  basePrice: '',
  totalRooms: '1',
  amenities: '',
  images: '',
}

function toForm(rt: RoomTypeResponse): RoomTypeForm {
  return {
    name: rt.name,
    description: rt.description ?? '',
    maxCapacity: String(rt.maxCapacity),
    area: rt.area != null ? String(rt.area) : '',
    basePrice: String(rt.basePrice),
    totalRooms: String(rt.totalRooms),
    amenities: rt.amenities.join(', '),
    images: rt.images.join('\n'),
  }
}

function toRequest(form: RoomTypeForm): RoomTypeRequest {
  const list = (value: string, separator: RegExp) =>
    value.split(separator).map((v) => v.trim()).filter(Boolean)
  return {
    name: form.name.trim(),
    description: form.description.trim(),
    maxCapacity: Number(form.maxCapacity),
    area: form.area ? Number(form.area) : undefined,
    basePrice: Number(form.basePrice),
    totalRooms: Number(form.totalRooms),
    amenities: list(form.amenities, /,/),
    images: list(form.images, /\n/),
  }
}

/** Lists a hotel's room types and lets an admin add, edit and delete them. */
export function RoomTypeManager({ hotelId }: { hotelId: number }) {
  const [roomTypes, setRoomTypes] = useState<RoomTypeResponse[]>([])
  const [loading, setLoading] = useState(true)
  // null = dialog closed, 'new' = creating, otherwise the room type being edited.
  const [editing, setEditing] = useState<RoomTypeResponse | 'new' | null>(null)
  const [form, setForm] = useState<RoomTypeForm>(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const fetchRoomTypes = useCallback(
    () =>
      hotelsApi
        .getRoomTypes(hotelId)
        .then(setRoomTypes)
        .finally(() => setLoading(false)),
    [hotelId]
  )

  useEffect(() => {
    fetchRoomTypes()
  }, [fetchRoomTypes])

  function openDialog(target: RoomTypeResponse | 'new') {
    setEditing(target)
    setForm(target === 'new' ? EMPTY_FORM : toForm(target))
    setError('')
  }

  function update(field: keyof RoomTypeForm) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      setForm((prev) => ({ ...prev, [field]: e.target.value }))
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const request = toRequest(form)
      if (editing === 'new') {
        await hotelsApi.createRoomType(hotelId, request)
      } else if (editing) {
        await hotelsApi.updateRoomType(editing.id, request)
      }
      setEditing(null)
      await fetchRoomTypes()
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to save room type'))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(roomType: RoomTypeResponse) {
    if (!confirm(`Delete room type "${roomType.name}"?`)) return
    try {
      await hotelsApi.deleteRoomType(roomType.id)
      await fetchRoomTypes()
    } catch (err: unknown) {
      alert(getErrorMessage(err, 'Failed to delete room type'))
    }
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>Room Types</CardTitle>
        <Button type="button" size="sm" onClick={() => openDialog('new')}>
          <Plus className="h-4 w-4 mr-1" /> Add Room Type
        </Button>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex justify-center py-6"><Spinner /></div>
        ) : roomTypes.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4">
            No room types yet. Guests cannot book this hotel until you add one.
          </p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-muted-foreground border-b border-border">
                <th className="py-2 font-medium">Name</th>
                <th className="py-2 font-medium">Guests</th>
                <th className="py-2 font-medium">Area</th>
                <th className="py-2 font-medium">Price / night</th>
                <th className="py-2 font-medium">Rooms</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {roomTypes.map((rt) => (
                <tr key={rt.id} className="border-b border-border last:border-0">
                  <td className="py-2 font-medium">{rt.name}</td>
                  <td className="py-2">{rt.maxCapacity}</td>
                  <td className="py-2">{rt.area != null ? `${rt.area} m²` : '—'}</td>
                  <td className="py-2">{formatCurrency(rt.basePrice)}</td>
                  <td className="py-2">{rt.totalRooms}</td>
                  <td className="py-2 text-right whitespace-nowrap">
                    <Button type="button" variant="ghost" size="sm" aria-label={`Edit ${rt.name}`} onClick={() => openDialog(rt)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      aria-label={`Delete ${rt.name}`}
                      className="text-destructive hover:text-destructive"
                      onClick={() => handleDelete(rt)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </CardContent>

      <Dialog open={editing !== null} onOpenChange={(open) => !open && setEditing(null)}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{editing === 'new' ? 'Add Room Type' : 'Edit Room Type'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSave} className="space-y-4 mt-4">
            {error && (
              <div className="rounded-lg bg-destructive/10 border border-destructive/20 px-3 py-2 text-sm text-destructive">
                {error}
              </div>
            )}
            <div className="space-y-1.5">
              <Label htmlFor="rt-name">Name</Label>
              <Input id="rt-name" value={form.name} onChange={update('name')} required maxLength={255} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rt-description">Description</Label>
              <Textarea id="rt-description" value={form.description} onChange={update('description')} rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="rt-price">Price / night (USD)</Label>
                <Input id="rt-price" type="number" min="0.01" step="0.01" value={form.basePrice} onChange={update('basePrice')} required />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="rt-rooms">Total rooms</Label>
                <Input id="rt-rooms" type="number" min="1" value={form.totalRooms} onChange={update('totalRooms')} required />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="rt-capacity">Max guests</Label>
                <Input id="rt-capacity" type="number" min="1" value={form.maxCapacity} onChange={update('maxCapacity')} required />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="rt-area">Area (m²)</Label>
                <Input id="rt-area" type="number" min="1" step="0.1" value={form.area} onChange={update('area')} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rt-amenities">Amenities (comma separated)</Label>
              <Input id="rt-amenities" value={form.amenities} onChange={update('amenities')} placeholder="Wifi, Air conditioning, Minibar" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rt-images">Image URLs (one per line)</Label>
              <Textarea id="rt-images" value={form.images} onChange={update('images')} rows={2} placeholder="https://..." />
            </div>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => setEditing(null)}>Cancel</Button>
              <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
