import { commandApi } from './axiosInstances'
import type { LoginRequest } from '../types'

export const authApi = {
  login: (data: LoginRequest) => commandApi.post<{ token: string }>('/api/auth/login', data),
}
