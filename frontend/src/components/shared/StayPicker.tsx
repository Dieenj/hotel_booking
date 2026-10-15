import { Calendar, Users } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { todayIso, type Stay } from '@/lib/stay'

interface StayPickerProps {
  value: Stay
  onChange: (stay: Stay) => void
}

/** Check-in / check-out / guests inputs. */
export function StayPicker({ value, onChange }: StayPickerProps) {
  const set = (field: keyof Stay) => (e: React.ChangeEvent<HTMLInputElement>) =>
    onChange({ ...value, [field]: e.target.value })

  return (
    <>
      <label className="flex-1 text-left">
        <span className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
          <Calendar className="h-3 w-3" /> Check-in
        </span>
        <Input type="date" value={value.checkIn} min={todayIso()} onChange={set('checkIn')} />
      </label>
      <label className="flex-1 text-left">
        <span className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
          <Calendar className="h-3 w-3" /> Check-out
        </span>
        <Input type="date" value={value.checkOut} min={value.checkIn || todayIso()} onChange={set('checkOut')} />
      </label>
      <label className="w-24 text-left">
        <span className="text-xs font-medium text-muted-foreground mb-1 flex items-center gap-1">
          <Users className="h-3 w-3" /> Guests
        </span>
        <Input type="number" min="1" max="20" value={value.guests} placeholder="2" onChange={set('guests')} />
      </label>
    </>
  )
}
