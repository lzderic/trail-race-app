import type { Meta, StoryObj } from '@storybook/react-vite'
import { ConfirmDialog } from './ConfirmDialog'

const meta: Meta<typeof ConfirmDialog> = {
  title: 'Common/ConfirmDialog',
  component: ConfirmDialog,
  tags: ['autodocs'],
  argTypes: {
    onConfirm: { action: 'confirmed' },
    onCancel: { action: 'cancelled' },
  },
  decorators: [
    (Story) => (
      <div style={{ minHeight: 300 }}>
        <Story />
      </div>
    ),
  ],
}

export default meta
type Story = StoryObj<typeof ConfirmDialog>

export const Default: Story = {
  args: {
    title: 'Delete Application',
    message: 'Are you sure you want to delete this application? This action cannot be undone.',
    onConfirm: () => {},
    onCancel: () => {},
  },
}

export const CustomLabels: Story = {
  args: {
    title: 'Remove Race',
    message: 'This will permanently remove the race and all associated applications.',
    confirmLabel: 'Yes, remove it',
    cancelLabel: 'Keep it',
    onConfirm: () => {},
    onCancel: () => {},
  },
}

export const Loading: Story = {
  args: {
    title: 'Delete Application',
    message: 'Are you sure you want to delete this application?',
    loading: true,
    onConfirm: () => {},
    onCancel: () => {},
  },
}
