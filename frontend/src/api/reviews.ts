import type { ApiResponse, PageResponse, ReviewRequest, ReviewResponse } from '@/types'
import client from './client'

export const reviewsApi = {
  getByHotel: async (
    hotelId: number,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ReviewResponse>> => {
    const res = await client.get<ApiResponse<PageResponse<ReviewResponse>>>(
      `/hotels/${hotelId}/reviews`,
      { params }
    )
    return res.data.data
  },

  getMyReviews: async (params?: {
    page?: number
    size?: number
  }): Promise<PageResponse<ReviewResponse>> => {
    const res = await client.get<ApiResponse<PageResponse<ReviewResponse>>>('/reviews/my', {
      params,
    })
    return res.data.data
  },

  create: async (data: ReviewRequest): Promise<ReviewResponse> => {
    const res = await client.post<ApiResponse<ReviewResponse>>('/reviews', data)
    return res.data.data
  },
}
