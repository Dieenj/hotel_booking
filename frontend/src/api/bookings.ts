import type { ApiResponse, BookingRequest, BookingResponse, BookingStatus, PageResponse } from '@/types'
import client from './client'

export const bookingsApi = {
  create: async (data: BookingRequest): Promise<BookingResponse> => {
    const res = await client.post<ApiResponse<BookingResponse>>('/bookings', data)
    return res.data.data
  },

  confirmPayment: async (id: number): Promise<BookingResponse> => {
    const res = await client.post<ApiResponse<BookingResponse>>(`/bookings/${id}/pay`)
    return res.data.data
  },

  cancel: async (id: number): Promise<BookingResponse> => {
    const res = await client.post<ApiResponse<BookingResponse>>(`/bookings/${id}/cancel`)
    return res.data.data
  },

  getMyBookings: async ({ statuses, ...params }: {
    statuses?: BookingStatus[]
    page?: number
    size?: number
  }): Promise<PageResponse<BookingResponse>> => {
    const res = await client.get<ApiResponse<PageResponse<BookingResponse>>>('/bookings', {
      params: { ...params, status: statuses?.join(',') },
    })
    return res.data.data
  },

  getById: async (id: number): Promise<BookingResponse> => {
    const res = await client.get<ApiResponse<BookingResponse>>(`/bookings/${id}`)
    return res.data.data
  },

  // Admin endpoints
  adminGetAll: async (params: {
    status?: BookingStatus
    hotelId?: number
    page?: number
    size?: number
  }): Promise<PageResponse<BookingResponse>> => {
    const res = await client.get<ApiResponse<PageResponse<BookingResponse>>>('/admin/bookings', {
      params,
    })
    return res.data.data
  },

  adminConfirm: async (id: number): Promise<BookingResponse> => {
    const res = await client.post<ApiResponse<BookingResponse>>(`/admin/bookings/${id}/confirm`)
    return res.data.data
  },

  adminCancel: async (id: number): Promise<BookingResponse> => {
    const res = await client.post<ApiResponse<BookingResponse>>(`/admin/bookings/${id}/cancel`)
    return res.data.data
  },
}
