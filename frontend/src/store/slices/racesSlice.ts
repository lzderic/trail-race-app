import { createAsyncThunk, createSlice, isAnyOf } from '@reduxjs/toolkit'
import type { Race, RacesState, RaceRequest, RacePatchRequest } from '../../types'
import type { RootState } from '../rootReducer'
import { racesApi } from '../../api/racesApi'
import { DEFAULT_ERROR_MESSAGE, getApiErrorMessage } from '../../api/errors'

const initialState: RacesState = {
  items: [],
  loading: false,
  error: null,
}

export const fetchRaces = createAsyncThunk<Race[], void, { rejectValue: string }>(
  'races/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await racesApi.getAll()
      return data
    } catch (error) {
      return rejectWithValue(getApiErrorMessage(error))
    }
  },
)

export const createRace = createAsyncThunk<Race, RaceRequest, { rejectValue: string }>(
  'races/create',
  async (request, { rejectWithValue }) => {
    try {
      const { data } = await racesApi.create(request)
      return data
    } catch (error) {
      return rejectWithValue(getApiErrorMessage(error))
    }
  },
)

export const patchRace = createAsyncThunk<
  Race,
  { id: string; data: RacePatchRequest },
  { rejectValue: string }
>('races/patch', async ({ id, data: request }, { rejectWithValue }) => {
  try {
    const { data } = await racesApi.patch(id, request)
    return data
  } catch (error) {
    return rejectWithValue(getApiErrorMessage(error))
  }
})

export const deleteRace = createAsyncThunk<string, string, { rejectValue: string }>(
  'races/delete',
  async (id, { rejectWithValue }) => {
    try {
      await racesApi.delete(id)
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
const racesSlice = createSlice({
  name: 'races',
  initialState,
  reducers: {
    clearRaceError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchRaces.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchRaces.fulfilled, (state, action) => {
        state.loading = false
        state.items = action.payload
      })
      .addCase(fetchRaces.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload ?? DEFAULT_ERROR_MESSAGE
      })
      .addCase(createRace.fulfilled, (state, action) => {
        state.items = [...state.items, action.payload]
      })
      .addCase(patchRace.fulfilled, (state, action) => {
        state.items = state.items.map((race) => (race.id === action.payload.id ? action.payload : race))
      })
      .addCase(deleteRace.fulfilled, (state, action) => {
        state.items = state.items.filter((race) => race.id !== action.payload)
      })
      .addMatcher(
        isAnyOf(createRace.rejected, patchRace.rejected, deleteRace.rejected),
        (state, action) => {
          state.error = action.payload ?? DEFAULT_ERROR_MESSAGE
        },
      )
  },
})

export const { clearRaceError } = racesSlice.actions
export default racesSlice.reducer

export const selectRaces = (state: RootState) => state.races.items
export const selectRacesLoading = (state: RootState) => state.races.loading
export const selectRacesError = (state: RootState) => state.races.error
