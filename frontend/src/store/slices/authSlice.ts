import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import type { AuthState, LoginRequest, User } from '../../types'
import { authApi } from '../../api/authApi'
import { jwtDecode } from '../../utils/jwtDecode'

const storedToken = localStorage.getItem('jwt_token')
const storedUser = localStorage.getItem('jwt_user')

const initialState: AuthState = {
  token: storedToken,
  user: storedUser ? (JSON.parse(storedUser) as User) : null,
  loading: false,
  error: null,
}

export const login = createAsyncThunk(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const res = await authApi.login(credentials)
      const token = res.data.token
      const user = jwtDecode(token)
      localStorage.setItem('jwt_token', token)
      localStorage.setItem('jwt_user', JSON.stringify(user))
      return { token, user }
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Invalid email or password. Please try again.'
      return rejectWithValue(message)
    }
  },
)

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.token = null
      state.user = null
      localStorage.removeItem('jwt_token')
      localStorage.removeItem('jwt_user')
    },
    clearError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false
        state.token = action.payload.token
        state.user = action.payload.user
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
  },
})

export const { logout, clearError } = authSlice.actions
export default authSlice.reducer
