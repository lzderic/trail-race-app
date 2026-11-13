import { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import {
  deleteApplication,
  fetchApplications,
  selectApplications,
  selectApplicationsError,
  selectApplicationsLoading,
} from '../../store/slices/applicationsSlice'
import { fetchRaces, selectRaces } from '../../store/slices/racesSlice'
import { useConfirmDelete } from '../../hooks/useConfirmDelete'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { EmptyState } from '../../components/common/EmptyState'
import { CalendarIcon } from '../../components/common/icons'
import { ApplicationsTable } from '../../components/applications/ApplicationsTable'
import { DeleteApplicationDialog } from '../../components/applications/DeleteApplicationDialog'
import type { Application } from '../../types'

export function AdminApplicationsPage() {
  const dispatch = useAppDispatch()
  const applications = useAppSelector(selectApplications)
  const loading = useAppSelector(selectApplicationsLoading)
  const error = useAppSelector(selectApplicationsError)
  const races = useAppSelector(selectRaces)
  const deletion = useConfirmDelete<Application>((id) => dispatch(deleteApplication(id)))

  useEffect(() => {
    dispatch(fetchApplications())
    dispatch(fetchRaces())
  }, [dispatch])

  const renderContent = () => {
    if (loading) return <Spinner />
    if (applications.length === 0) {
      return (
        <EmptyState
          icon={<CalendarIcon />}
          title="No applications yet"
          description="Applications submitted by participants will appear here."
        />
      )
    }
    return <ApplicationsTable applications={applications} races={races} onDelete={deletion.request} />
  }

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">All Applications</h1>
        <span className="text-muted" style={{ fontSize: '0.9rem' }}>
          {applications.length} total
        </span>
      </div>

      {error && <Alert message={error} />}

      {renderContent()}

      <DeleteApplicationDialog deletion={deletion} />
    </>
  )
}
