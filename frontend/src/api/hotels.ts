import type { ApiResponse, HotelRequest, HotelResponse, PageResponse, RoomTypeRequest, RoomTypeResponse } from '@/types'
import client from './client'

export const hotelsApi = {
  search: async (params: {
    city?: string
    name?: string
    minRating?: number
    page?: number
    size?: number
    sort?: string
  }): Promise<PageResponse<HotelResponse>> => {
    const res = await client.get<ApiResponse<PageResponse<HotelResponse>>>('/hotels', { params })
    return res.data.data
  },

  getById: async (id: number): Promise<HotelResponse> => {
    const res = await client.get<ApiResponse<HotelResponse>>(`/hotels/${id}`)
    return res.data.data
  },

  create: async (data: HotelRequest): Promise<HotelResponse> => {
    const res = await client.post<ApiResponse<HotelResponse>>('/hotels', data)
    return res.data.data
  },

  update: async (id: number, data: HotelRequest): Promise<HotelResponse> => {
    const res = await client.put<ApiResponse<HotelResponse>>(`/hotels/${id}`, data)
    return res.data.data
  },

  delete: async (id: number): Promise<void> => {
    await client.delete(`/hotels/${id}`)
  },

  getRoomTypes: async (
    hotelId: number,
    params?: { checkIn?: string; checkOut?: string }
  ): Promise<RoomTypeResponse[]> => {
    const res = await client.get<ApiResponse<RoomTypeResponse[]>>(
      `/hotels/${hotelId}/room-types`,
      { params }
    )
    return res.data.data
  },

  createRoomType: async (hotelId: number, data: RoomTypeRequest): Promise<RoomTypeResponse> => {
    const res = await client.post<ApiResponse<RoomTypeResponse>>(
      `/hotels/${hotelId}/room-types`,
      data
    )
    return res.data.data
  },

  updateRoomType: async (roomTypeId: number, data: RoomTypeRequest): Promise<RoomTypeResponse> => {
    const res = await client.put<ApiResponse<RoomTypeResponse>>(
      `/room-types/${roomTypeId}`,
      data
    )
    return res.data.data
  },

  deleteRoomType: async (roomTypeId: number): Promise<void> => {
    await client.delete(`/room-types/${roomTypeId}`)
  },

  getRoomTypeById: async (roomTypeId: number): Promise<RoomTypeResponse> => {
    const res = await client.get<ApiResponse<RoomTypeResponse>>(`/room-types/${roomTypeId}`)
    return res.data.data
  },
}
