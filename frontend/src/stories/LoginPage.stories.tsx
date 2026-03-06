import type { Meta, StoryObj } from '@storybook/react-vite'
import { LoginPage } from '../pages/LoginPage'
import { withProviders } from './decorators'

const meta: Meta<typeof LoginPage> = {
  title: 'Pages/LoginPage',
  component: LoginPage,
  tags: ['autodocs'],
  decorators: [withProviders],
}

export default meta
type Story = StoryObj<typeof LoginPage>

export const Default: Story = {}
