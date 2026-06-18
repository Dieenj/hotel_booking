import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '@/types'
import client from './client'

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const res = await client.post<ApiResponse<AuthResponse>>('/auth/register', data)
    return res.data.data
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const res = await client.post<ApiResponse<AuthResponse>>('/auth/login', data)
    return res.data.data
  },

  getMe: async (): Promise<UserProfile> => {
    const res = await client.get<ApiResponse<UserProfile>>('/auth/me')
    return res.data.data
  },

  updateProfile: async (data: { fullName: string; phone?: string }): Promise<UserProfile> => {
    const res = await client.put<ApiResponse<UserProfile>>('/auth/me', data)
    return res.data.data
  },
}
