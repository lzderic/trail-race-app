import { commandApi } from './axiosInstances'
import { queryApi } from './axiosInstances'
import type { Application, ApplicationRequest } from '../types'

export const applicationsApi = {
  getAll: () => queryApi.get<Application[]>('/api/applications'),
  getById: (id: string) => queryApi.get<Application>(`/api/applications/${id}`),
  create: (data: ApplicationRequest) => commandApi.post<Application>('/api/applications', data),
  delete: (id: string) => commandApi.delete(`/api/applications/${id}`),
}
