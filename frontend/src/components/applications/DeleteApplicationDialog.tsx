import type { Application } from '../../types'
import type { ConfirmDelete } from '../../hooks/useConfirmDelete'
import { ConfirmDialog } from '../common/ConfirmDialog'

interface DeleteApplicationDialogProps {
  deletion: ConfirmDelete<Application>
}

/** Confirmation dialog for deleting an application; renders nothing until one has been selected. */
export function DeleteApplicationDialog({ deletion }: DeleteApplicationDialogProps) {
  if (!deletion.target) return null

  const { firstName, lastName } = deletion.target
  return (
    <ConfirmDialog
      title="Delete Application"
      message={`Delete the application of ${firstName} ${lastName}? This action cannot be undone.`}
      onConfirm={deletion.confirm}
      onCancel={deletion.cancel}
      loading={deletion.loading}
    />
  )
}
