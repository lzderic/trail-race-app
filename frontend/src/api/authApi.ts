import { commandApi } from './axiosInstances'
import type { LoginRequest } from '../types'

export interface LoginResponse {
  token: string
}

export const authApi = {
  login: (data: LoginRequest) => commandApi.post<LoginResponse>('/api/auth/login', data),
}
