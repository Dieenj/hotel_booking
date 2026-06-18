import { useState, useEffect } from 'react'
import { adminApi } from '@/api/admin'
import type { DashboardStats } from '@/types'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { StatusBadge } from '@/components/ui/badge'
import { PageSpinner } from '@/components/shared/Spinner'
import { formatCurrency } from '@/lib/utils'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { Hotel, BookOpen, DollarSign, TrendingUp } from 'lucide-react'
import type { BookingStatus } from '@/types'

const REVENUE_DATA = [
  { month: 'Jan', revenue: 42 }, { month: 'Feb', revenue: 38 }, { month: 'Mar', revenue: 55 },
  { month: 'Apr', revenue: 61 }, { month: 'May', revenue: 72 }, { month: 'Jun', revenue: 68 },
  { month: 'Jul', revenue: 89 }, { month: 'Aug', revenue: 95 },
]

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
                  {formatCurrency(Number(stats.revenueThisMonth) / 30)}
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-blue-500 opacity-80" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Monthly Revenue</CardTitle>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={240}>
            <AreaChart data={REVENUE_DATA}>
              <defs>
                <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#1B3A5C" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#1B3A5C" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(27,58,92,0.08)" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip formatter={(v) => [`$${v}k`, 'Revenue']} />
              <Area type="monotone" dataKey="revenue" stroke="#1B3A5C" fill="url(#colorRevenue)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

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
