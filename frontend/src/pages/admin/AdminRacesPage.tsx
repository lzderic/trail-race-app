import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import {
  deleteRace,
  fetchRaces,
  selectRaces,
  selectRacesError,
  selectRacesLoading,
} from '../../store/slices/racesSlice'
import { useConfirmDelete } from '../../hooks/useConfirmDelete'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { EmptyState } from '../../components/common/EmptyState'
import { BoltIcon } from '../../components/common/icons'
import { RacesTable } from '../../components/races/RacesTable'
import { DeleteRaceDialog } from '../../components/races/DeleteRaceDialog'
import { RaceFormModal } from './RaceFormModal'
import type { Race } from '../../types'

export function AdminRacesPage() {
  const dispatch = useAppDispatch()
  const races = useAppSelector(selectRaces)
  const loading = useAppSelector(selectRacesLoading)
  const error = useAppSelector(selectRacesError)

  const [showCreate, setShowCreate] = useState(false)
  const [editRace, setEditRace] = useState<Race | null>(null)
  const deletion = useConfirmDelete<Race>((id) => dispatch(deleteRace(id)))

  useEffect(() => {
    dispatch(fetchRaces())
  }, [dispatch])

  const renderContent = () => {
    if (loading) return <Spinner />
    if (races.length === 0) {
      return (
        <EmptyState
          icon={<BoltIcon />}
          title="No races yet"
          description='Click "Add Race" to create the first race.'
        />
      )
    }
    return <RacesTable races={races} onEdit={setEditRace} onDelete={deletion.request} />
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

      {renderContent()}

      {showCreate && <RaceFormModal onClose={() => setShowCreate(false)} />}
      {editRace && <RaceFormModal race={editRace} onClose={() => setEditRace(null)} />}
      <DeleteRaceDialog deletion={deletion} />
    </>
  )
}
