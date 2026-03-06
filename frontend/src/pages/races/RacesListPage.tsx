import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { fetchRaces, deleteRace } from '../../store/slices/racesSlice'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'
import type { Race } from '../../types'
import { ApplyToRaceModal } from './ApplyToRaceModal'
import { RaceFormModal } from '../admin/RaceFormModal'

export function RacesListPage() {
  const dispatch = useAppDispatch()
  const { items: races, loading, error } = useAppSelector((s) => s.races)
  const user = useAppSelector((s) => s.auth.user)

  const [applyRace, setApplyRace] = useState<Race | null>(null)
  const [showCreate, setShowCreate] = useState(false)
  const [editRace, setEditRace] = useState<Race | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    dispatch(fetchRaces())
  }, [dispatch])

  const isAdmin = user?.role === 'Administrator'

  const handleDelete = async () => {
    if (!deleteId) return
    setDeleteLoading(true)
    await dispatch(deleteRace(deleteId))
    setDeleteLoading(false)
    setDeleteId(null)
  }

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">Available Races</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            + Add Race
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
          <p>
            {isAdmin
              ? 'Click "+ Add Race" to create the first race.'
              : 'Check back later or ask an administrator to add races.'}
          </p>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Distance</th>
                  <th style={{ width: isAdmin ? 200 : 160 }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {races.map((race) => (
                  <tr key={race.id}>
                    <td>
                      <strong>{race.name}</strong>
                    </td>
                    <td>
                      <span className={distanceBadgeClass(race.distance)}>
                        {DISTANCE_LABELS[race.distance]}
                      </span>
                    </td>
                    <td>
                      {isAdmin ? (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => setEditRace(race)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => setDeleteId(race.id)}
                          >
                            Delete
                          </button>
                        </div>
                      ) : (
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

      {applyRace && <ApplyToRaceModal race={applyRace} onClose={() => setApplyRace(null)} />}

      {showCreate && <RaceFormModal onClose={() => setShowCreate(false)} />}

      {editRace && <RaceFormModal race={editRace} onClose={() => setEditRace(null)} />}

      {deleteId && (
        <ConfirmDialog
          title="Delete Race"
          message="Are you sure you want to delete this race? All associated applications may be affected."
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          loading={deleteLoading}
        />
      )}
    </>
  )
}
