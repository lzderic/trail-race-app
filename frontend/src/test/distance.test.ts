import { describe, it, expect } from 'vitest'
import { distanceBadgeClass, DISTANCE_LABELS } from '../utils/distance'
import type { Distance } from '../types'

const distances: Distance[] = ['5k', '10k', 'HalfMarathon', 'Marathon']

describe('distance utils', () => {
  it('provides a label for every distance', () => {
    distances.forEach((d) => {
      expect(DISTANCE_LABELS[d]).toBeTruthy()
    })
  })

  it('returns a css class string for every distance', () => {
    distances.forEach((d) => {
      const cls = distanceBadgeClass(d)
      expect(cls).toContain('badge')
    })
  })

  it('assigns unique classes to each distance', () => {
    const classes = distances.map(distanceBadgeClass)
    const unique = new Set(classes)
    expect(unique.size).toBe(distances.length)
  })
})
