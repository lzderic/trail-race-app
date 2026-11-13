import { createListenerMiddleware, isAnyOf } from '@reduxjs/toolkit'
import { login, logout, sessionExpired } from './slices/authSlice'
import { clearAuthSession, saveAuthSession } from '../utils/authSession'

/**
 * Keeps browser storage in sync with the auth slice. Doing this in middleware keeps the
 * reducers pure and makes the store the single source of truth for the session.
 */
export const authPersistenceMiddleware = createListenerMiddleware()

authPersistenceMiddleware.startListening({
  actionCreator: login.fulfilled,
  effect: (action) => {
    saveAuthSession(action.payload)
  },
})

authPersistenceMiddleware.startListening({
  matcher: isAnyOf(logout, sessionExpired),
  effect: () => {
    clearAuthSession()
  },
})
