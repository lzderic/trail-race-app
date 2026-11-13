import { combineReducers } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import racesReducer from './slices/racesSlice'
import applicationsReducer from './slices/applicationsSlice'

export const rootReducer = combineReducers({
  auth: authReducer,
  races: racesReducer,
  applications: applicationsReducer,
})

export type RootState = ReturnType<typeof rootReducer>
