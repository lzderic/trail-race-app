import { configureStore } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import racesReducer from './slices/racesSlice'
import applicationsReducer from './slices/applicationsSlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    races: racesReducer,
    applications: applicationsReducer
  }
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

