import { useState, useEffect } from 'react'
import { adminApi } from '@/api/admin'
import type { DashboardStats } from '@/types'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { StatusBadge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency } from '@/lib/utils'
import { Hotel, BookOpen, DollarSign, TrendingUp } from 'lucide-react'
import type { BookingStatus } from '@/types'

export function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    adminApi
      .getDashboardStats()
      .then(setStats)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageSpinner />
  if (!stats) return null

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Revenue This Month</p>
                <p className="text-2xl font-bold mt-1">{formatCurrency(Number(stats.revenueThisMonth))}</p>
              </div>
              <DollarSign className="h-8 w-8 text-accent opacity-80" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Bookings This Month</p>
                <p className="text-2xl font-bold mt-1">{stats.totalBookingsThisMonth}</p>
              </div>
              <BookOpen className="h-8 w-8 text-primary opacity-80" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total Hotels</p>
                <p className="text-2xl font-bold mt-1">{stats.totalHotels}</p>
              </div>
              <Hotel className="h-8 w-8 text-emerald-500 opacity-80" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Avg. Revenue/Day</p>
                <p className="text-2xl font-bold mt-1">
                  {/* Average over the days of this month so far. */}
                  {formatCurrency(Number(stats.revenueThisMonth) / new Date().getDate())}
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-blue-500 opacity-80" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Bookings */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Bookings</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-left">
                  <th className="pb-3 font-medium text-muted-foreground">Booking ID</th>
                  <th className="pb-3 font-medium text-muted-foreground">Guest</th>
                  <th className="pb-3 font-medium text-muted-foreground">Hotel</th>
                  <th className="pb-3 font-medium text-muted-foreground">Status</th>
                  <th className="pb-3 font-medium text-muted-foreground text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {stats.recentBookings.map((b) => (
                  <tr key={b.bookingCode}>
                    <td className="py-3 font-mono text-xs">{b.bookingCode}</td>
                    <td className="py-3">{b.guestName}</td>
                    <td className="py-3 text-muted-foreground">{b.hotelName}</td>
                    <td className="py-3">
                      <StatusBadge status={b.status as BookingStatus} />
                    </td>
                    <td className="py-3 text-right font-medium">{formatCurrency(b.totalPrice)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
