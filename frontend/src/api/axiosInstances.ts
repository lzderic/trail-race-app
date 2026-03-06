import axios from 'axios'

const COMMAND_BASE = import.meta.env.VITE_COMMAND_API_URL || 'http://localhost:8081'
const QUERY_BASE = import.meta.env.VITE_QUERY_API_URL || 'http://localhost:8082'

export const commandApi = axios.create({
  baseURL: COMMAND_BASE,
  headers: { 'Content-Type': 'application/json' },
})

export const queryApi = axios.create({
  baseURL: QUERY_BASE,
  headers: { 'Content-Type': 'application/json' },
})

const attachToken = (config: import('axios').InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('jwt_token')
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}

commandApi.interceptors.request.use(attachToken)
queryApi.interceptors.request.use(attachToken)

const handleUnauthorized = (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401) {
    const url = error.config?.url ?? ''
    if (!url.includes('/auth/login')) {
      localStorage.removeItem('jwt_token')
      localStorage.removeItem('jwt_user')
      window.location.href = '/login'
    }
  }
  return Promise.reject(error)
}

commandApi.interceptors.response.use((r) => r, handleUnauthorized)
queryApi.interceptors.response.use((r) => r, handleUnauthorized)
