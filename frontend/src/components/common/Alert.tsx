interface AlertProps {
  message: string
  type?: 'error' | 'success' | 'info'
  onClose?: () => void
}

export function Alert({ message, type = 'error', onClose }: AlertProps) {
  return (
    <div className={`alert alert-${type}`} role="alert">
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1rem' }}
          aria-label="Close"
        >
          ×
        </button>
      )}
    </div>
  )
}

