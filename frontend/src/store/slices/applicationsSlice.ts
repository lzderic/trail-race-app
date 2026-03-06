import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import type { ApplicationsState, ApplicationRequest } from '../../types'
import { applicationsApi } from '../../api/applicationsApi'

const initialState: ApplicationsState = {
  items: [],
  loading: false,
  error: null,
  selectedApplication: null,
}

function extractMessage(err: unknown): string {
  return (
    (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    'An error occurred'
  )
}

export const fetchApplications = createAsyncThunk(
  'applications/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const res = await applicationsApi.getAll()
      return res.data
    } catch (err) {
      return rejectWithValue(extractMessage(err))
    }
  },
)

export const fetchApplicationById = createAsyncThunk(
  'applications/fetchById',
  async (id: string, { rejectWithValue }) => {
    try {
      const res = await applicationsApi.getById(id)
      return res.data
    } catch (err) {
      return rejectWithValue(extractMessage(err))
    }
  },
)

export const createApplication = createAsyncThunk(
  'applications/create',
  async (data: ApplicationRequest, { rejectWithValue }) => {
    try {
      const res = await applicationsApi.create(data)
      return res.data
    } catch (err) {
      return rejectWithValue(extractMessage(err))
    }
  },
)

export const deleteApplication = createAsyncThunk(
  'applications/delete',
  async (id: string, { rejectWithValue }) => {
    try {
      await applicationsApi.delete(id)
      return id
    } catch (err) {
      return rejectWithValue(extractMessage(err))
    }
  },
)

const applicationsSlice = createSlice({
  name: 'applications',
  initialState,
  reducers: {
    clearApplicationError(state) {
      state.error = null
    },
    clearSelectedApplication(state) {
      state.selectedApplication = null
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
        state.error = action.payload as string
      })
      .addCase(fetchApplicationById.pending, (state) => {
        state.loading = true
      })
      .addCase(fetchApplicationById.fulfilled, (state, action) => {
        state.loading = false
        state.selectedApplication = action.payload
      })
      .addCase(fetchApplicationById.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
      .addCase(createApplication.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(createApplication.fulfilled, (state, action) => {
        state.loading = false
        state.items.push(action.payload)
      })
      .addCase(createApplication.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
      .addCase(deleteApplication.pending, (state) => {
        state.error = null
      })
      .addCase(deleteApplication.fulfilled, (state, action) => {
        state.items = state.items.filter((a) => a.id !== action.payload)
        if (state.selectedApplication?.id === action.payload) state.selectedApplication = null
      })
      .addCase(deleteApplication.rejected, (state, action) => {
        state.error = action.payload as string
      })
  },
})

export const { clearApplicationError, clearSelectedApplication } = applicationsSlice.actions
export default applicationsSlice.reducer
