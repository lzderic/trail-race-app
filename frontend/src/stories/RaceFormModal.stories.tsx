import type { Meta, StoryObj } from '@storybook/react-vite'
import { RaceFormModal } from '../pages/admin/RaceFormModal'
import { withProviders } from './decorators'

const meta: Meta<typeof RaceFormModal> = {
  title: 'Modals/RaceFormModal',
  component: RaceFormModal,
  tags: ['autodocs'],
  decorators: [withProviders],
  argTypes: {
    onClose: { action: 'closed' },
  },
}

export default meta
type Story = StoryObj<typeof RaceFormModal>

export const CreateNew: Story = {
  args: {
    onClose: () => {},
  },
}

export const EditExisting: Story = {
  args: {
    race: {
      id: 'b0000000-0000-0000-0000-000000000001',
      name: 'Mountain Trail 5K',
      distance: '5k',
    },
    onClose: () => {},
  },
}
