import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import {
  deleteRace,
  fetchRaces,
  selectRaces,
  selectRacesError,
  selectRacesLoading,
} from '../../store/slices/racesSlice'
import { selectIsAdministrator } from '../../store/slices/authSlice'
import { useConfirmDelete } from '../../hooks/useConfirmDelete'
import { Spinner } from '../../components/common/Spinner'
import { Alert } from '../../components/common/Alert'
import { EmptyState } from '../../components/common/EmptyState'
import { InfoIcon } from '../../components/common/icons'
import { RacesTable } from '../../components/races/RacesTable'
import { DeleteRaceDialog } from '../../components/races/DeleteRaceDialog'
import { ApplyToRaceModal } from './ApplyToRaceModal'
import { RaceFormModal } from '../admin/RaceFormModal'
import type { Race } from '../../types'

export function RacesListPage() {
  const dispatch = useAppDispatch()
  const races = useAppSelector(selectRaces)
  const loading = useAppSelector(selectRacesLoading)
  const error = useAppSelector(selectRacesError)
  const isAdmin = useAppSelector(selectIsAdministrator)

  const [applyRace, setApplyRace] = useState<Race | null>(null)
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
          icon={<InfoIcon />}
          title="No races available"
          description={
            isAdmin
              ? 'Click "+ Add Race" to create the first race.'
              : 'Check back later or ask an administrator to add races.'
          }
        />
      )
    }
    return isAdmin ? (
      <RacesTable races={races} onEdit={setEditRace} onDelete={deletion.request} />
    ) : (
      <RacesTable races={races} onApply={setApplyRace} />
    )
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

      {renderContent()}

      {applyRace && <ApplyToRaceModal race={applyRace} onClose={() => setApplyRace(null)} />}
      {showCreate && <RaceFormModal onClose={() => setShowCreate(false)} />}
      {editRace && <RaceFormModal race={editRace} onClose={() => setEditRace(null)} />}
      <DeleteRaceDialog deletion={deletion} />
    </>
  )
}
