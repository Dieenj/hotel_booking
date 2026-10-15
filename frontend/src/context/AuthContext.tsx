import { useEffect, useState } from 'react'
import type { UserProfile } from '@/types'
import { authApi } from '@/api/auth'
import { AuthContext } from './useAuth'

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  // Only a stored token needs resolving into a user; without one there is nothing to wait for.
  const [isLoading, setIsLoading] = useState(() => !!localStorage.getItem('token'))

  useEffect(() => {
    if (!token || user) return
    authApi
      .getMe()
      .then(setUser)
      .catch(() => {
        localStorage.removeItem('token')
        setToken(null)
      })
      .finally(() => setIsLoading(false))
  }, [token, user])

  function login(newToken: string, newUser: UserProfile) {
    localStorage.setItem('token', newToken)
    setToken(newToken)
    setUser(newUser)
    setIsLoading(false)
  }

  function logout() {
    localStorage.removeItem('token')
    setToken(null)
    setUser(null)
  }

  async function refreshUser() {
    const updated = await authApi.getMe()
    setUser(updated)
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}
