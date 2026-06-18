import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'
import type { BookingStatus } from '@/types'

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground',
        secondary: 'border-transparent bg-secondary text-secondary-foreground',
        destructive: 'border-transparent bg-destructive text-destructive-foreground',
        outline: 'text-foreground',
        pending: 'bg-amber-100 text-amber-800 border-amber-200',
        confirmed: 'bg-emerald-100 text-emerald-800 border-emerald-200',
        completed: 'bg-blue-100 text-blue-800 border-blue-200',
        cancelled: 'bg-red-100 text-red-800 border-red-200',
        checkedIn: 'bg-purple-100 text-purple-800 border-purple-200',
      },
    },
    defaultVariants: { variant: 'default' },
  }
)

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

export function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />
}

const STATUS_VARIANT: Record<BookingStatus, VariantProps<typeof badgeVariants>['variant']> = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  CHECKED_IN: 'checkedIn',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
}

export function StatusBadge({ status }: { status: BookingStatus }) {
  const label = status.replace('_', ' ')
  return <Badge variant={STATUS_VARIANT[status]}>{label}</Badge>
}
