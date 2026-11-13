import { createAsyncThunk, createSlice, isAnyOf } from '@reduxjs/toolkit'
import type { Application, ApplicationsState, ApplicationRequest } from '../../types'
import type { RootState } from '../rootReducer'
import { applicationsApi } from '../../api/applicationsApi'
import { DEFAULT_ERROR_MESSAGE, getApiErrorMessage } from '../../api/errors'

const initialState: ApplicationsState = {
  items: [],
  loading: false,
  error: null,
}

/** Loads the applications visible to the current user (all for administrators, own for applicants). */
export const fetchApplications = createAsyncThunk<Application[], void, { rejectValue: string }>(
  'applications/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await applicationsApi.getAll()
      return data
    } catch (error) {
      return rejectWithValue(getApiErrorMessage(error))
    }
  },
)

export const createApplication = createAsyncThunk<
  Application,
  ApplicationRequest,
  { rejectValue: string }
>('applications/create', async (request, { rejectWithValue }) => {
  try {
    const { data } = await applicationsApi.create(request)
    return data
  } catch (error) {
    return rejectWithValue(getApiErrorMessage(error))
  }
})

export const deleteApplication = createAsyncThunk<string, string, { rejectValue: string }>(
  'applications/delete',
  async (id, { rejectWithValue }) => {
    try {
      await applicationsApi.delete(id)
      return id
    } catch (error) {
      return rejectWithValue(getApiErrorMessage(error))
    }
  },
)

/*
 * Reducers receive an Immer draft, so the assignments below describe the next state and
 * Redux Toolkit produces a new immutable object; the previous state is never changed.
 */
const applicationsSlice = createSlice({
  name: 'applications',
  initialState,
  reducers: {
    clearApplicationError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchApplications.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchApplications.fulfilled, (state, action) => {
        state.loading = false
        state.items = action.payload
      })
      .addCase(fetchApplications.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload ?? DEFAULT_ERROR_MESSAGE
      })
      .addCase(createApplication.fulfilled, (state, action) => {
        state.items = [...state.items, action.payload]
      })
      .addCase(deleteApplication.fulfilled, (state, action) => {
        state.items = state.items.filter((application) => application.id !== action.payload)
      })
      .addMatcher(
        isAnyOf(createApplication.rejected, deleteApplication.rejected),
        (state, action) => {
          state.error = action.payload ?? DEFAULT_ERROR_MESSAGE
        },
      )
  },
})

export const { clearApplicationError } = applicationsSlice.actions
export default applicationsSlice.reducer

export const selectApplications = (state: RootState) => state.applications.items
export const selectApplicationsLoading = (state: RootState) => state.applications.loading
export const selectApplicationsError = (state: RootState) => state.applications.error
