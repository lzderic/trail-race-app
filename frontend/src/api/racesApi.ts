import { commandApi, queryApi } from './axiosInstances'
import type { Race, RaceRequest, RacePatchRequest } from '../types'

const RACES_PATH = '/api/races'

export const racesApi = {
  getAll: () => queryApi.get<Race[]>(RACES_PATH),
  getById: (id: string) => queryApi.get<Race>(`${RACES_PATH}/${id}`),
  create: (data: RaceRequest) => commandApi.post<Race>(RACES_PATH, data),
  patch: (id: string, data: RacePatchRequest) => commandApi.patch<Race>(`${RACES_PATH}/${id}`, data),
  delete: (id: string) => commandApi.delete<void>(`${RACES_PATH}/${id}`),
}
