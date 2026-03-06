import type { Meta, StoryObj } from '@storybook/react-vite'
import { Alert } from './Alert'

const meta: Meta<typeof Alert> = {
  title: 'Common/Alert',
  component: Alert,
  tags: ['autodocs'],
  argTypes: {
    type: { control: 'select', options: ['error', 'success', 'info'] },
    message: { control: 'text' },
    onClose: { action: 'closed' },
  },
}

export default meta
type Story = StoryObj<typeof Alert>

export const Error: Story = {
  args: { message: 'Something went wrong. Please try again.', type: 'error' },
}

export const Success: Story = {
  args: { message: 'Application submitted successfully!', type: 'success' },
}

export const Info: Story = {
  args: { message: 'Races are loaded from the query service.', type: 'info' },
}

export const WithCloseButton: Story = {
  args: { message: 'Dismissible alert', type: 'error', onClose: () => {} },
}

export const LongMessage: Story = {
  args: {
    message:
      'This is a very long error message that might wrap to multiple lines to test how the alert component handles overflow and long text content in the UI.',
    type: 'error',
  },
}
