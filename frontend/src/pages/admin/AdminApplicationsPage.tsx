import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { fetchApplications, deleteApplication } from '../../store/slices/applicationsSlice'
import { fetchRaces } from '../../store/slices/racesSlice'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import { DISTANCE_LABELS, distanceBadgeClass } from '../../utils/distance'

export function AdminApplicationsPage() {
  const dispatch = useAppDispatch()
  const { items: applications, loading, error } = useAppSelector((s) => s.applications)
  const { items: races } = useAppSelector((s) => s.races)

  const [deleteId, setDeleteId] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    dispatch(fetchApplications())
    dispatch(fetchRaces())
  }, [dispatch])

  const getRace = (raceId: string) => races.find((r) => r.id === raceId)

  const handleDelete = async () => {
    if (!deleteId) return
    setDeleteLoading(true)
    await dispatch(deleteApplication(deleteId))
    setDeleteLoading(false)
    setDeleteId(null)
  }

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">All Applications</h1>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          {applications.length} total
        </span>
      </div>

      {error && <Alert message={error} />}

      {loading ? (
        <Spinner />
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <path d="M16 2v4M8 2v4M3 10h18" />
          </svg>
          <h3>No applications yet</h3>
          <p>Applications submitted by participants will appear here.</p>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Applicant</th>
                  <th>Club</th>
                  <th>Race</th>
                  <th>Distance</th>
                  <th style={{ width: 100 }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {applications.map((app) => {
                  const race = getRace(app.raceId)
                  return (
                    <tr key={app.id}>
                      <td>
                        <strong>
                          {app.firstName} {app.lastName}
                        </strong>
                      </td>
                      <td>{app.club ?? <span style={{ color: 'var(--text-muted)' }}>—</span>}</td>
                      <td>
                        {race?.name ?? (
                          <span style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>
                            Deleted Race
                          </span>
                        )}
                      </td>
                      <td>
                        {race ? (
                          <span className={distanceBadgeClass(race.distance)}>
                            {DISTANCE_LABELS[race.distance]}
                          </span>
                        ) : (
                          <span style={{ color: 'var(--text-muted)' }}>—</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => setDeleteId(app.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {deleteId && (
        <ConfirmDialog
          title="Delete Application"
          message="Are you sure you want to delete this application? This action cannot be undone."
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          loading={deleteLoading}
        />
      )}
    </>
  )
}
