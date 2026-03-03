import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { fetchRaces, deleteRace } from '../../store/slices/racesSlice'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'
import { RaceFormModal } from './RaceFormModal'
import type { Race } from '../../types'

export function AdminRacesPage() {
  const dispatch = useAppDispatch()
  const { items: races, loading, error } = useAppSelector((s) => s.races)

  const [showCreate, setShowCreate] = useState(false)
  const [editRace, setEditRace] = useState<Race | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    dispatch(fetchRaces())
  }, [dispatch])

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
        <h1 className="page-title">Manage Races</h1>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          + Add Race
        </button>
      </div>

      {error && <Alert message={error} />}

      {loading ? (
        <Spinner />
      ) : races.length === 0 ? (
        <div className="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
          </svg>
          <h3>No races yet</h3>
          <p>Click "Add Race" to create the first race.</p>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Distance</th>
                  <th style={{ width: 180 }}>Actions</th>
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
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showCreate && (
        <RaceFormModal onClose={() => setShowCreate(false)} />
      )}

      {editRace && (
        <RaceFormModal race={editRace} onClose={() => setEditRace(null)} />
      )}

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

