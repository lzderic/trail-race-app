import { commandApi } from './axiosInstances'
import { queryApi } from './axiosInstances'
import type { Race, RaceRequest, RacePatchRequest } from '../types'

export const racesApi = {
  getAll: () => queryApi.get<Race[]>('/api/races'),
  getById: (id: string) => queryApi.get<Race>(`/api/races/${id}`),
  create: (data: RaceRequest) => commandApi.post<Race>('/api/races', data),
  patch: (id: string, data: RacePatchRequest) => commandApi.patch<Race>(`/api/races/${id}`, data),
  delete: (id: string) => commandApi.delete(`/api/races/${id}`),
}
