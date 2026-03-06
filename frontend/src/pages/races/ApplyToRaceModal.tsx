import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { createApplication } from '../../store/slices/applicationsSlice'
import { Alert } from '../../components/common/Alert'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'
import type { Race } from '../../types'

interface Props {
  race: Race
  onClose: () => void
}

export function ApplyToRaceModal({ race, onClose }: Props) {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const { loading, error } = useAppSelector((s) => s.applications)
  const user = useAppSelector((s) => s.auth.user)

  const [firstName, setFirstName] = useState(user?.firstName ?? '')
  const [lastName, setLastName] = useState(user?.lastName ?? '')
  const [club, setClub] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [success, setSuccess] = useState(false)

  const validate = () => {
    const errs: Record<string, string> = {}
    if (!firstName.trim()) errs.firstName = 'First name is required'
    if (!lastName.trim()) errs.lastName = 'Last name is required'
    return errs
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) {
      setFieldErrors(errs)
      return
    }
    setFieldErrors({})

    const result = await dispatch(
      createApplication({ firstName, lastName, club: club || undefined, raceId: race.id }),
    )
    if (createApplication.fulfilled.match(result)) {
      setSuccess(true)
      setTimeout(() => {
        onClose()
        navigate('/applications')
      }, 1200)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h2 className="modal-title">Apply to Race</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div style={{ marginBottom: '1.25rem' }}>
          <p style={{ fontWeight: 600, fontSize: '1rem' }}>{race.name}</p>
          <span className={distanceBadgeClass(race.distance)}>
            {DISTANCE_LABELS[race.distance]}
          </span>
        </div>

        {success && <Alert message="Application submitted successfully!" type="success" />}
        {error && <Alert message={error} type="error" />}

        {!success && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label className="form-label" htmlFor="apply-fname">
                First Name *
              </label>
              <input
                id="apply-fname"
                className={`form-control${fieldErrors.firstName ? ' error' : ''}`}
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                maxLength={255}
              />
              {fieldErrors.firstName && <p className="form-error">{fieldErrors.firstName}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="apply-lname">
                Last Name *
              </label>
              <input
                id="apply-lname"
                className={`form-control${fieldErrors.lastName ? ' error' : ''}`}
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                maxLength={255}
              />
              {fieldErrors.lastName && <p className="form-error">{fieldErrors.lastName}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="apply-club">
                Club (optional)
              </label>
              <input
                id="apply-club"
                className="form-control"
                value={club}
                onChange={(e) => setClub(e.target.value)}
                maxLength={255}
                placeholder="e.g. Trail Blazers"
              />
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Submitting…' : 'Submit Application'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
