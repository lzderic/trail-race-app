import { useMemo } from 'react'
import type { Application, Race } from '../../types'
import { DistanceBadge } from '../common/DistanceBadge'

interface ApplicationsTableProps {
  applications: Application[]
  /** Races used to resolve each application's race name and distance. */
  races: Race[]
  /** Asks for confirmation before deleting an application. */
  onDelete: (application: Application) => void
}

const NOT_AVAILABLE = <span className="text-muted">—</span>

export function ApplicationsTable({ applications, races, onDelete }: ApplicationsTableProps) {
  const racesById = useMemo(() => new Map(races.map((race) => [race.id, race])), [races])

  return (
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
            {applications.map((application) => {
              const race = racesById.get(application.raceId)
              return (
                <tr key={application.id}>
                  <td>
                    <strong>
                      {application.firstName} {application.lastName}
                    </strong>
                  </td>
                  <td>{application.club || NOT_AVAILABLE}</td>
                  <td>
                    {race ? race.name : <span className="text-muted text-italic">Deleted Race</span>}
                  </td>
                  <td>{race ? <DistanceBadge distance={race.distance} /> : NOT_AVAILABLE}</td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={() => onDelete(application)}>
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
  )
}
