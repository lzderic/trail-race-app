import { useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { createRace, patchRace } from '../../store/slices/racesSlice'
import { Alert } from '../../components/common/Alert'
import { DISTANCE_OPTIONS } from '../../utils/distance'
import type { Race, Distance } from '../../types'

interface Props {
  race?: Race
  onClose: () => void
}

export function RaceFormModal({ race, onClose }: Props) {
  const dispatch = useAppDispatch()
  const { error } = useAppSelector((s) => s.races)

  const [name, setName] = useState(race?.name ?? '')
  const [distance, setDistance] = useState<Distance>(race?.distance ?? '5k')
  const [loading, setLoading] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [success, setSuccess] = useState(false)

  const isEdit = !!race

  const validate = () => {
    const errs: Record<string, string> = {}
    if (!name.trim()) errs.name = 'Race name is required'
    if (name.length > 255) errs.name = 'Name must be at most 255 characters'
    return errs
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) { setFieldErrors(errs); return }
    setFieldErrors({})
    setLoading(true)

    let result
    if (isEdit) {
      result = await dispatch(patchRace({ id: race.id, data: { name, distance } }))
    } else {
      result = await dispatch(createRace({ name, distance }))
    }

    setLoading(false)
    if (createRace.fulfilled.match(result) || patchRace.fulfilled.match(result)) {
      setSuccess(true)
      setTimeout(onClose, 900)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h2 className="modal-title">{isEdit ? 'Edit Race' : 'New Race'}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">×</button>
        </div>

        {success && <Alert message={isEdit ? 'Race updated!' : 'Race created!'} type="success" />}
        {error && <Alert message={error} type="error" />}

        {!success && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label className="form-label" htmlFor="race-name">Race Name *</label>
              <input
                id="race-name"
                className={`form-control${fieldErrors.name ? ' error' : ''}`}
                value={name}
                onChange={(e) => setName(e.target.value)}
                maxLength={255}
                placeholder="e.g. Mountain Classic"
                autoFocus
              />
              {fieldErrors.name && <p className="form-error">{fieldErrors.name}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="race-distance">Distance *</label>
              <select
                id="race-distance"
                className="form-control"
                value={distance}
                onChange={(e) => setDistance(e.target.value as Distance)}
              >
                {DISTANCE_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Race'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

