import { useCallback, useState } from 'react'

interface Identifiable {
  id: string
}

export interface ConfirmDelete<T extends Identifiable> {
  /** The item awaiting confirmation, or null when no dialog should be shown. */
  target: T | null
  /** True while the deletion request is in flight. */
  loading: boolean
  /** Opens the confirmation for the given item. */
  request: (item: T) => void
  /** Closes the confirmation without deleting. Ignored while a deletion is in flight. */
  cancel: () => void
  /** Runs the deletion for the current target and closes the confirmation afterwards. */
  confirm: () => Promise<void>
}

/**
 * Drives the "click delete → confirm → run" flow shared by every list page.
 * `performDelete` receives the item id and resolves once the request has finished,
 * whether it succeeded or failed; the caller surfaces failures through its own error state.
 */
export function useConfirmDelete<T extends Identifiable>(
  performDelete: (id: string) => Promise<unknown>,
): ConfirmDelete<T> {
  const [target, setTarget] = useState<T | null>(null)
  const [loading, setLoading] = useState(false)

  const request = useCallback((item: T) => setTarget(item), [])

  const cancel = useCallback(() => {
    if (!loading) setTarget(null)
  }, [loading])

  const confirm = useCallback(async () => {
    if (!target) return
    setLoading(true)
    try {
      await performDelete(target.id)
    } finally {
      setLoading(false)
      setTarget(null)
    }
  }, [target, performDelete])

  return { target, loading, request, cancel, confirm }
}
