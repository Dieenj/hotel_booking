import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserProfile,
  UserWithTokenResponse,
} from '@/types'
import client from './client'

function toAuthResponse({ token, ...user }: UserWithTokenResponse): AuthResponse {
  return { token, user }
}

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const res = await client.post<ApiResponse<UserWithTokenResponse>>('/auth/register', data)
    return toAuthResponse(res.data.data)
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const res = await client.post<ApiResponse<UserWithTokenResponse>>('/auth/login', data)
    return toAuthResponse(res.data.data)
  },

  getMe: async (): Promise<UserProfile> => {
    const res = await client.get<ApiResponse<UserProfile>>('/auth/me')
    return res.data.data
  },

  updateProfile: async (data: { fullName: string; phone?: string }): Promise<UserProfile> => {
    const res = await client.put<ApiResponse<UserProfile>>('/auth/profile', data)
    return res.data.data
  },
}
