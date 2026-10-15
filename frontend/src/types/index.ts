// ── Auth ──────────────────────────────────────────────────────────────────────

/** Shape returned by the backend for login/register: the profile plus a JWT. */
export interface UserWithTokenResponse extends UserProfile {
  token: string
}

export interface AuthResponse {
  token: string
  user: UserProfile
}

export interface UserProfile {
  id: number
  email: string
  fullName: string
  phone: string | null
  role: 'GUEST' | 'ADMIN'
}

export interface RegisterRequest {
  fullName: string
  email: string
  phone?: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

// ── Hotel ─────────────────────────────────────────────────────────────────────

export interface HotelResponse {
  id: number
  name: string
  address: string
  city: string
  country: string
  description: string
  starRating: number
  amenities: string[]
  images: string[]
  active: boolean
  averageRating?: number | null
  reviewCount?: number | null
  /** Cheapest active room type's nightly price; only set in search results. */
  minPrice?: number | null
  createdAt: string
}

export interface HotelRequest {
  name: string
  address: string
  city: string
  country: string
  description: string
  starRating: number
  amenities: string[]
  images: string[]
}

// ── Room Type ─────────────────────────────────────────────────────────────────

export interface RoomTypeResponse {
  id: number
  name: string
  description: string
  maxCapacity: number
  area: number | null
  basePrice: number
  totalRooms: number
  active: boolean
  amenities: string[]
  images: string[]
  availableRooms?: number | null
  hotelId: number
  hotelName: string
}

export interface RoomTypeRequest {
  name: string
  description: string
  maxCapacity: number
  area?: number
  basePrice: number
  totalRooms: number
  amenities: string[]
  images: string[]
}

// ── Booking ───────────────────────────────────────────────────────────────────

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED'

export interface BookingResponse {
  id: number
  bookingCode: string
  roomTypeId: number
  roomTypeName: string
  hotelId: number
  hotelName: string
  hotelAddress: string
  checkIn: string
  checkOut: string
  nights: number
  guests: number
  totalPrice: number
  status: BookingStatus
  specialRequest: string | null
  guestName: string
  guestEmail: string
  guestPhone: string | null
  createdAt: string
}

export interface BookingRequest {
  roomTypeId: number
  checkIn: string
  checkOut: string
  guests: number
  specialRequest?: string
  guestName: string
  guestEmail: string
  guestPhone?: string
}

// ── Review ────────────────────────────────────────────────────────────────────

export interface ReviewResponse {
  id: number
  userId: number
  userName: string
  hotelId: number
  bookingId: number
  rating: number
  cleanlinessRating: number | null
  serviceRating: number | null
  locationRating: number | null
  valueRating: number | null
  comment: string | null
  createdAt: string
}

export interface ReviewRequest {
  bookingId: number
  rating: number
  cleanlinessRating?: number
  serviceRating?: number
  locationRating?: number
  valueRating?: number
  comment?: string
}

// ── Admin ─────────────────────────────────────────────────────────────────────

export interface DashboardStats {
  totalBookingsThisMonth: number
  revenueThisMonth: number
  totalHotels: number
  recentBookings: RecentBooking[]
}

export interface RecentBooking {
  bookingCode: string
  guestName: string
  hotelName: string
  status: string
  totalPrice: number
  createdAt: string
}

// ── Pagination ────────────────────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiResponse<T> {
  success: boolean
  message?: string
  error?: string
  data: T
}
