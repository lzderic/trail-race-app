import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import type { RacesState, RaceRequest, RacePatchRequest } from '../../types'
import { racesApi } from '../../api/racesApi'

const initialState: RacesState = {
  items: [],
  loading: false,
  error: null,
  selectedRace: null
}

export const fetchRaces = createAsyncThunk(
  'races/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const res = await racesApi.getAll()
      return res.data
    } catch (err: unknown) {
      return rejectWithValue(extractMessage(err))
    }
  }
)

export const fetchRaceById = createAsyncThunk(
  'races/fetchById',
  async (id: string, { rejectWithValue }) => {
    try {
      const res = await racesApi.getById(id)
      return res.data
    } catch (err: unknown) {
      return rejectWithValue(extractMessage(err))
    }
  }
)

export const createRace = createAsyncThunk(
  'races/create',
  async (data: RaceRequest, { rejectWithValue }) => {
    try {
      const res = await racesApi.create(data)
      return res.data
    } catch (err: unknown) {
      return rejectWithValue(extractMessage(err))
    }
  }
)

export const patchRace = createAsyncThunk(
  'races/patch',
  async (
    { id, data }: { id: string; data: RacePatchRequest },
    { rejectWithValue }
  ) => {
    try {
      const res = await racesApi.patch(id, data)
      return res.data
    } catch (err: unknown) {
      return rejectWithValue(extractMessage(err))
    }
  }
)

export const deleteRace = createAsyncThunk(
  'races/delete',
  async (id: string, { rejectWithValue }) => {
    try {
      await racesApi.delete(id)
      return id
    } catch (err: unknown) {
      return rejectWithValue(extractMessage(err))
    }
  }
)

function extractMessage(err: unknown): string {
  return (
    (err as { response?: { data?: { message?: string } } })?.response?.data
      ?.message ?? 'An error occurred'
  )
}

const racesSlice = createSlice({
  name: 'races',
  initialState,
  reducers: {
    clearRaceError(state) {
      state.error = null
    },
    clearSelectedRace(state) {
      state.selectedRace = null
    }
  },
  extraReducers: (builder) => {
    builder
      // fetchAll
      .addCase(fetchRaces.pending, (state) => { state.loading = true; state.error = null })
      .addCase(fetchRaces.fulfilled, (state, action) => {
        state.loading = false; state.items = action.payload
      })
      .addCase(fetchRaces.rejected, (state, action) => {
        state.loading = false; state.error = action.payload as string
      })
      // fetchById
      .addCase(fetchRaceById.pending, (state) => { state.loading = true; state.error = null })
      .addCase(fetchRaceById.fulfilled, (state, action) => {
        state.loading = false; state.selectedRace = action.payload
      })
      .addCase(fetchRaceById.rejected, (state, action) => {
        state.loading = false; state.error = action.payload as string
      })
      // create
      .addCase(createRace.fulfilled, (state, action) => {
        state.items.push(action.payload)
      })
      .addCase(createRace.rejected, (state, action) => {
        state.error = action.payload as string
      })
      // patch
      .addCase(patchRace.fulfilled, (state, action) => {
        const idx = state.items.findIndex((r) => r.id === action.payload.id)
        if (idx !== -1) state.items[idx] = action.payload
        if (state.selectedRace?.id === action.payload.id)
          state.selectedRace = action.payload
      })
      .addCase(patchRace.rejected, (state, action) => {
        state.error = action.payload as string
      })
      // delete
      .addCase(deleteRace.fulfilled, (state, action) => {
        state.items = state.items.filter((r) => r.id !== action.payload)
        if (state.selectedRace?.id === action.payload) state.selectedRace = null
      })
      .addCase(deleteRace.rejected, (state, action) => {
        state.error = action.payload as string
      })
  }
})

export const { clearRaceError, clearSelectedRace } = racesSlice.actions
export default racesSlice.reducer

