import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import {
  clearApplicationError,
  createApplication,
  selectApplicationsError,
} from '../../store/slices/applicationsSlice'
import { selectCurrentUser } from '../../store/slices/authSlice'
import { Alert } from '../../components/common/Alert'
import { DistanceBadge } from '../../components/common/DistanceBadge'
import type { Race } from '../../types'

interface ApplyToRaceModalProps {
  race: Race
  onClose: () => void
}

interface ApplicationFormErrors {
  firstName?: string
  lastName?: string
}

const MAX_FIELD_LENGTH = 255
const SUCCESS_REDIRECT_DELAY_MS = 1200

function validateApplicant(firstName: string, lastName: string): ApplicationFormErrors {
  const errors: ApplicationFormErrors = {}
  if (!firstName.trim()) errors.firstName = 'First name is required'
  if (!lastName.trim()) errors.lastName = 'Last name is required'
  return errors
}

export function ApplyToRaceModal({ race, onClose }: ApplyToRaceModalProps) {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const error = useAppSelector(selectApplicationsError)
  const user = useAppSelector(selectCurrentUser)

  const [firstName, setFirstName] = useState(user?.firstName ?? '')
  const [lastName, setLastName] = useState(user?.lastName ?? '')
  const [club, setClub] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<ApplicationFormErrors>({})
  const [success, setSuccess] = useState(false)
  const redirectTimer = useRef<ReturnType<typeof setTimeout>>()

  // Start with a clean error banner and make sure neither the error nor the pending
  // redirect outlives the modal.
  useEffect(() => {
    dispatch(clearApplicationError())
    return () => {
      dispatch(clearApplicationError())
      clearTimeout(redirectTimer.current)
    }
  }, [dispatch])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const errors = validateApplicant(firstName, lastName)
    setFieldErrors(errors)
    if (errors.firstName || errors.lastName) return

    setSubmitting(true)
    const result = await dispatch(
      createApplication({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        club: club.trim() || undefined,
        raceId: race.id,
      }),
    )
    setSubmitting(false)

    if (createApplication.fulfilled.match(result)) {
      setSuccess(true)
      redirectTimer.current = setTimeout(() => {
        onClose()
        navigate('/applications')
      }, SUCCESS_REDIRECT_DELAY_MS)
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
          <DistanceBadge distance={race.distance} />
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
                maxLength={MAX_FIELD_LENGTH}
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
                maxLength={MAX_FIELD_LENGTH}
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
                maxLength={MAX_FIELD_LENGTH}
                placeholder="e.g. Trail Blazers"
              />
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-ghost" onClick={onClose} disabled={submitting}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Submit Application'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
