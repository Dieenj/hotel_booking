import { cn } from '@/lib/utils'

export function Spinner({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        'h-8 w-8 animate-spin rounded-full border-4 border-primary/20 border-t-primary',
        className
      )}
    />
  )
}

export function PageSpinner() {
  return (
    <div className="flex min-h-[400px] items-center justify-center">
      <Spinner />
    </div>
  )
}
