import { commandApi, queryApi } from './axiosInstances'
import type { Application, ApplicationRequest } from '../types'

const APPLICATIONS_PATH = '/api/applications'

export const applicationsApi = {
  /** Administrators receive every application; applicants receive only their own. */
  getAll: () => queryApi.get<Application[]>(APPLICATIONS_PATH),
  getById: (id: string) => queryApi.get<Application>(`${APPLICATIONS_PATH}/${id}`),
  create: (data: ApplicationRequest) => commandApi.post<Application>(APPLICATIONS_PATH, data),
  delete: (id: string) => commandApi.delete<void>(`${APPLICATIONS_PATH}/${id}`),
}
