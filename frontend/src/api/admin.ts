import type { ApiResponse, DashboardStats } from '@/types'
import client from './client'

export const adminApi = {
  getDashboardStats: async (): Promise<DashboardStats> => {
    const res = await client.get<ApiResponse<DashboardStats>>('/admin/stats')
    return res.data.data
  },
}
