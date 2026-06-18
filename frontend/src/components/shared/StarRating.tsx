import { Star } from 'lucide-react'
import { cn } from '@/lib/utils'

interface StarRatingProps {
  rating: number
  size?: 'sm' | 'md' | 'lg'
  showValue?: boolean
  interactive?: boolean
  onChange?: (rating: number) => void
}

export function StarRating({
  rating,
  size = 'sm',
  showValue = false,
  interactive = false,
  onChange,
}: StarRatingProps) {
  const sizeClass = { sm: 'w-3.5 h-3.5', md: 'w-5 h-5', lg: 'w-6 h-6' }[size]

  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((i) => (
        <Star
          key={i}
          className={cn(
            sizeClass,
            i <= Math.floor(rating)
              ? 'fill-amber-400 text-amber-400'
              : 'fill-none text-stone-300',
            interactive && 'cursor-pointer hover:text-amber-400'
          )}
          onClick={() => interactive && onChange?.(i)}
        />
      ))}
      {showValue && (
        <span className="ml-1 text-sm font-medium text-foreground">{rating.toFixed(1)}</span>
      )}
    </div>
  )
}
