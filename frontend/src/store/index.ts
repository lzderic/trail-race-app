import { configureStore } from '@reduxjs/toolkit'
import { rootReducer } from './rootReducer'
import { authPersistenceMiddleware } from './authPersistence'
import { sessionExpired } from './slices/authSlice'
import { configureApiClients } from '../api/axiosInstances'

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().prepend(authPersistenceMiddleware.middleware),
})

// The HTTP layer reads the token from the store and reports expired sessions back to it,
// so authentication state has exactly one owner.
configureApiClients({
  getAccessToken: () => store.getState().auth.token,
  onUnauthorized: () => {
    store.dispatch(sessionExpired())
  },
})

export type { RootState } from './rootReducer'
export type AppDispatch = typeof store.dispatch
