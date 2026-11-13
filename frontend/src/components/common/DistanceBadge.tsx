import type { Distance } from '../../types'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'

interface DistanceBadgeProps {
  distance: Distance
}

export function DistanceBadge({ distance }: DistanceBadgeProps) {
  return <span className={distanceBadgeClass(distance)}>{DISTANCE_LABELS[distance]}</span>
}
