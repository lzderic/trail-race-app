import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import type { AuthSession, AuthState, LoginRequest } from '../../types'
import type { RootState } from '../rootReducer'
import { authApi } from '../../api/authApi'
import { getApiErrorMessage } from '../../api/errors'
import { jwtDecode } from '../../utils/jwtDecode'
import { loadAuthSession } from '../../utils/authSession'

export const LOGIN_FAILED_MESSAGE = 'Invalid email or password. Please try again.'
export const SESSION_EXPIRED_MESSAGE = 'Your session has expired. Please sign in again.'

/** Restores a previously persisted session so a page refresh keeps the user signed in. */
function createInitialState(): AuthState {
  const session = loadAuthSession()
  return {
    token: session?.token ?? null,
    user: session?.user ?? null,
    loading: false,
    error: null,
  }
}

export const login = createAsyncThunk<AuthSession, LoginRequest, { rejectValue: string }>(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const { data } = await authApi.login(credentials)
      return { token: data.token, user: jwtDecode(data.token) }
    } catch (error) {
      return rejectWithValue(getApiErrorMessage(error, LOGIN_FAILED_MESSAGE))
    }
  },
)

/*
 * Reducers receive an Immer draft, so the assignments below describe the next state and
 * Redux Toolkit produces a new immutable object; the previous state is never changed.
 * Reducers are pure: persisting the session to storage happens in the auth persistence
 * middleware, never here.
 */
const authSlice = createSlice({
  name: 'auth',
  initialState: createInitialState,
  reducers: {
    logout(state) {
      state.token = null
      state.user = null
      state.error = null
    },
    sessionExpired(state) {
      state.token = null
      state.user = null
      state.error = SESSION_EXPIRED_MESSAGE
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
        state.error = action.payload ?? LOGIN_FAILED_MESSAGE
      })
  },
})

export const { logout, sessionExpired, clearError } = authSlice.actions
export default authSlice.reducer

export const selectCurrentUser = (state: RootState) => state.auth.user
export const selectIsAuthenticated = (state: RootState) => state.auth.token !== null && state.auth.user !== null
export const selectIsAdministrator = (state: RootState) => state.auth.user?.role === 'Administrator'
