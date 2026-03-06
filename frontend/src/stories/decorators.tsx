import { Provider } from 'react-redux'
import { configureStore } from '@reduxjs/toolkit'
import { MemoryRouter } from 'react-router-dom'
import authReducer from '../store/slices/authSlice'
import racesReducer from '../store/slices/racesSlice'
import applicationsReducer from '../store/slices/applicationsSlice'
import type { Decorator } from '@storybook/react-vite'

function makeStore() {
  return configureStore({
    reducer: {
      auth: authReducer,
      races: racesReducer,
      applications: applicationsReducer,
    },
  })
}

export const withProviders: Decorator = (Story) => (
  <Provider store={makeStore()}>
    <MemoryRouter>
      <Story />
    </MemoryRouter>
  </Provider>
)
