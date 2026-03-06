import type { Distance } from '../types'

export const DISTANCE_LABELS: Record<Distance, string> = {
  '5k': '5 km',
  '10k': '10 km',
  HalfMarathon: 'Half Marathon',
  Marathon: 'Marathon',
}

export const DISTANCE_OPTIONS: { value: Distance; label: string }[] = [
  { value: '5k', label: '5 km' },
  { value: '10k', label: '10 km' },
  { value: 'HalfMarathon', label: 'Half Marathon' },
  { value: 'Marathon', label: 'Marathon' },
]

export function distanceBadgeClass(distance: Distance): string {
  switch (distance) {
    case '5k':
      return 'badge badge-green'
    case '10k':
      return 'badge badge-blue'
    case 'HalfMarathon':
      return 'badge badge-orange'
    case 'Marathon':
      return 'badge badge-purple'
  }
}
