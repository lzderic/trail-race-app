import type { Race } from '../../types'
import { DistanceBadge } from '../common/DistanceBadge'

interface RacesTableProps {
  races: Race[]
  /** Shown to applicants: opens the application form for a race. */
  onApply?: (race: Race) => void
  /** Shown to administrators: opens the edit form for a race. */
  onEdit?: (race: Race) => void
  /** Shown to administrators: asks for confirmation before deleting a race. */
  onDelete?: (race: Race) => void
}

export function RacesTable({ races, onApply, onEdit, onDelete }: RacesTableProps) {
  const hasActions = Boolean(onApply || onEdit || onDelete)

  return (
    <div className="card">
      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Distance</th>
              {hasActions && <th style={{ width: 200 }}>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {races.map((race) => (
              <tr key={race.id}>
                <td>
                  <strong>{race.name}</strong>
                </td>
                <td>
                  <DistanceBadge distance={race.distance} />
                </td>
                {hasActions && (
                  <td>
                    <div className="table-actions">
                      {onApply && (
                        <button className="btn btn-primary btn-sm" onClick={() => onApply(race)}>
                          Apply
                        </button>
                      )}
                      {onEdit && (
                        <button className="btn btn-secondary btn-sm" onClick={() => onEdit(race)}>
                          Edit
                        </button>
                      )}
                      {onDelete && (
                        <button className="btn btn-danger btn-sm" onClick={() => onDelete(race)}>
                          Delete
                        </button>
                      )}
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
