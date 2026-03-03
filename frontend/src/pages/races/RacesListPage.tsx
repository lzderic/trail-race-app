import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { fetchRaces } from '../../store/slices/racesSlice'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'
import type { Race } from '../../types'
import { ApplyToRaceModal } from './ApplyToRaceModal'

export function RacesListPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const { items: races, loading, error } = useAppSelector((s) => s.races)
  const user = useAppSelector((s) => s.auth.user)

  const [applyRace, setApplyRace] = useState<Race | null>(null)

  useEffect(() => {
    dispatch(fetchRaces())
  }, [dispatch])

  const isAdmin = user?.role === 'Administrator'

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">Available Races</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/admin/races')}>
            + Manage Races
          </button>
        )}
      </div>

      {error && <Alert message={error} />}

      {loading ? (
        <Spinner />
      ) : races.length === 0 ? (
        <div className="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 8v4m0 4h.01" />
          </svg>
          <h3>No races available</h3>
          <p>Check back later or ask an administrator to add races.</p>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Distance</th>
                  <th style={{ width: 160 }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {races.map((race) => (
                  <tr key={race.id}>
                    <td><strong>{race.name}</strong></td>
                    <td>
                      <span className={distanceBadgeClass(race.distance)}>
                        {DISTANCE_LABELS[race.distance]}
                      </span>
                    </td>
                    <td>
                      {!isAdmin && (
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => setApplyRace(race)}
                        >
                          Apply
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {applyRace && (
        <ApplyToRaceModal
          race={applyRace}
          onClose={() => setApplyRace(null)}
        />
      )}
    </>
  )
}

