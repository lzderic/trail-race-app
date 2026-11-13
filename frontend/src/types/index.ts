export type Distance = '5k' | '10k' | 'HalfMarathon' | 'Marathon'
export type Role = 'Applicant' | 'Administrator'

export interface Race {
  id: string
  name: string
  distance: Distance
}

export interface Application {
  id: string
  firstName: string
  lastName: string
  club: string | null
  raceId: string
  userId: string | null
}

export interface User {
  id: string
  firstName: string
  lastName: string
  email: string
  role: Role
}

/** A signed-in user together with the bearer token that authenticates them. */
export interface AuthSession {
  token: string
  user: User
}

export interface AuthState {
  token: string | null
  user: User | null
  loading: boolean
  error: string | null
}

export interface RacesState {
  items: Race[]
  loading: boolean
  error: string | null
}

export interface ApplicationsState {
  items: Application[]
  loading: boolean
  error: string | null
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RaceRequest {
  name: string
  distance: Distance
}

export interface RacePatchRequest {
  name?: string
  distance?: Distance
}

export interface ApplicationRequest {
  firstName: string
  lastName: string
  club?: string
  raceId: string
}
