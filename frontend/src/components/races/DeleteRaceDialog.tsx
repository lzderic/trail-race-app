import type { Race } from '../../types'
import type { ConfirmDelete } from '../../hooks/useConfirmDelete'
import { ConfirmDialog } from '../common/ConfirmDialog'

interface DeleteRaceDialogProps {
  deletion: ConfirmDelete<Race>
}

/** Confirmation dialog for deleting a race; renders nothing until a race has been selected. */
export function DeleteRaceDialog({ deletion }: DeleteRaceDialogProps) {
  if (!deletion.target) return null

  return (
    <ConfirmDialog
      title="Delete Race"
      message={`Delete "${deletion.target.name}"? All applications submitted for this race will be removed as well.`}
      onConfirm={deletion.confirm}
      onCancel={deletion.cancel}
      loading={deletion.loading}
    />
  )
}
