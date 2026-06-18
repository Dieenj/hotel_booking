import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useAuth } from '@/context/AuthContext'
import { authApi } from '@/api/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'

export function ProfilePage() {
  const { user, logout, refreshUser } = useAuth()
  const navigate = useNavigate()

  const [editing, setEditing] = useState(false)
  const [fullName, setFullName] = useState(user?.fullName || '')
  const [phone, setPhone] = useState(user?.phone || '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function handleSave() {
    setSaving(true)
    setError('')
    try {
      await authApi.updateProfile({ fullName, phone })
      await refreshUser()
      setEditing(false)
    } catch {
      setError('Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  function handleLogout() {
    logout()
    navigate('/')
  }

  if (!user) return null

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold mb-6">My Profile</h1>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-16 w-16">
              <AvatarFallback className="text-xl bg-primary text-primary-foreground">
                {user.fullName.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div>
              <CardTitle className="text-xl">{user.fullName}</CardTitle>
              <p className="text-sm text-muted-foreground capitalize">{user.role.toLowerCase()}</p>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {error && (
            <div className="rounded-lg bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
              {error}
            </div>
          )}

          <div className="space-y-1.5">
            <Label>Full Name</Label>
            {editing ? (
              <Input value={fullName} onChange={(e) => setFullName(e.target.value)} />
            ) : (
              <p className="text-foreground py-2">{user.fullName}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label>Email</Label>
            <p className="text-muted-foreground py-2">{user.email}</p>
          </div>

          <div className="space-y-1.5">
            <Label>Phone</Label>
            {editing ? (
              <Input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="Not provided"
              />
            ) : (
              <p className="text-foreground py-2">{user.phone || '—'}</p>
            )}
          </div>

          <div className="flex gap-3 pt-2">
            {editing ? (
              <>
                <Button onClick={handleSave} disabled={saving}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
                <Button variant="outline" onClick={() => setEditing(false)}>
                  Cancel
                </Button>
              </>
            ) : (
              <Button variant="outline" onClick={() => setEditing(true)}>
                Edit Profile
              </Button>
            )}
            <Button
              variant="ghost"
              className="ml-auto text-destructive hover:text-destructive hover:bg-destructive/10"
              onClick={handleLogout}
            >
              Sign Out
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
