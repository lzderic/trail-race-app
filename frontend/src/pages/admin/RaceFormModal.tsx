import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import {
  clearRaceError,
  createRace,
  patchRace,
  selectRacesError,
} from '../../store/slices/racesSlice'
import { Alert } from '../../components/common/Alert'
import { DISTANCE_OPTIONS } from '../../utils/distance'
import type { Race, Distance } from '../../types'

interface RaceFormModalProps {
  /** When provided the form edits this race; otherwise it creates a new one. */
  race?: Race
  onClose: () => void
}

interface RaceFormErrors {
  name?: string
}

const MAX_NAME_LENGTH = 255
const SUCCESS_CLOSE_DELAY_MS = 900

function validateRaceName(name: string): RaceFormErrors {
  const trimmed = name.trim()
  if (!trimmed) return { name: 'Race name is required' }
  if (trimmed.length > MAX_NAME_LENGTH) return { name: `Name must be at most ${MAX_NAME_LENGTH} characters` }
  return {}
}

export function RaceFormModal({ race, onClose }: RaceFormModalProps) {
  const dispatch = useAppDispatch()
  const error = useAppSelector(selectRacesError)

  const [name, setName] = useState(race?.name ?? '')
  const [distance, setDistance] = useState<Distance>(race?.distance ?? '5k')
  const [submitting, setSubmitting] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<RaceFormErrors>({})
  const [success, setSuccess] = useState(false)
  const closeTimer = useRef<ReturnType<typeof setTimeout>>()

  const isEdit = race !== undefined

  // Start with a clean error banner and make sure neither the error nor the pending
  // auto-close outlives the modal.
  useEffect(() => {
    dispatch(clearRaceError())
    return () => {
      dispatch(clearRaceError())
      clearTimeout(closeTimer.current)
    }
  }, [dispatch])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const errors = validateRaceName(name)
    setFieldErrors(errors)
    if (errors.name) return

    const payload = { name: name.trim(), distance }
    setSubmitting(true)
    const result = race
      ? await dispatch(patchRace({ id: race.id, data: payload }))
      : await dispatch(createRace(payload))
    setSubmitting(false)

    if (createRace.fulfilled.match(result) || patchRace.fulfilled.match(result)) {
      setSuccess(true)
      closeTimer.current = setTimeout(onClose, SUCCESS_CLOSE_DELAY_MS)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Race' : 'New Race'}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        {success && <Alert message={isEdit ? 'Race updated!' : 'Race created!'} type="success" />}
        {error && <Alert message={error} type="error" />}

        {!success && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label className="form-label" htmlFor="race-name">
                Race Name *
              </label>
              <input
                id="race-name"
                className={`form-control${fieldErrors.name ? ' error' : ''}`}
                value={name}
                onChange={(e) => setName(e.target.value)}
                maxLength={MAX_NAME_LENGTH}
                placeholder="e.g. Mountain Classic"
                autoFocus
              />
              {fieldErrors.name && <p className="form-error">{fieldErrors.name}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="race-distance">
                Distance *
              </label>
              <select
                id="race-distance"
                className="form-control"
                value={distance}
                onChange={(e) => setDistance(e.target.value as Distance)}
              >
                {DISTANCE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-ghost" onClick={onClose} disabled={submitting}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Race'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
