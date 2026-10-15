/** Dates and guest count of a stay, carried in the URL from search through to booking. */
export interface Stay {
  checkIn: string
  checkOut: string
  guests: string
}

/** Same limit as the backend (StayPolicy.MAX_NIGHTS). */
export const MAX_NIGHTS = 30

/** Today as YYYY-MM-DD in the user's time zone (toISOString() would give the UTC date). */
export function todayIso(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export function readStay(params: URLSearchParams): Stay {
  return {
    checkIn: params.get('checkIn') || '',
    checkOut: params.get('checkOut') || '',
    guests: params.get('guests') || '',
  }
}

/** Copies the stay into the params, dropping empty values. */
export function writeStay(params: URLSearchParams, stay: Stay): URLSearchParams {
  for (const key of ['checkIn', 'checkOut', 'guests'] as const) {
    if (stay[key]) params.set(key, stay[key])
    else params.delete(key)
  }
  return params
}

/** "?checkIn=…&checkOut=…&guests=…", or "" when the stay is empty. */
export function stayQuery(stay: Stay): string {
  const query = writeStay(new URLSearchParams(), stay).toString()
  return query ? `?${query}` : ''
}

/** A message describing what is wrong with the dates, or null when they are usable. */
export function stayError(stay: Stay): string | null {
  if (!stay.checkIn || !stay.checkOut) return 'Select check-in and check-out dates'
  if (stay.checkIn < todayIso()) return 'Check-in cannot be in the past'
  const nights = (Date.parse(stay.checkOut) - Date.parse(stay.checkIn)) / 86_400_000
  if (nights <= 0) return 'Check-out must be after check-in'
  if (nights > MAX_NIGHTS) return `Stays are limited to ${MAX_NIGHTS} nights`
  return null
}
