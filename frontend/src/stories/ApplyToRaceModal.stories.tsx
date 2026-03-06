import type { Meta, StoryObj } from '@storybook/react-vite'
import { ApplyToRaceModal } from '../pages/races/ApplyToRaceModal'
import { withProviders } from './decorators'

const meta: Meta<typeof ApplyToRaceModal> = {
  title: 'Modals/ApplyToRaceModal',
  component: ApplyToRaceModal,
  tags: ['autodocs'],
  decorators: [withProviders],
  argTypes: {
    onClose: { action: 'closed' },
  },
}

export default meta
type Story = StoryObj<typeof ApplyToRaceModal>

export const FiveK: Story = {
  args: {
    race: {
      id: 'b0000000-0000-0000-0000-000000000001',
      name: 'Mountain Trail 5K',
      distance: '5k',
    },
    onClose: () => {},
  },
}

export const Marathon: Story = {
  args: {
    race: {
      id: 'b0000000-0000-0000-0000-000000000004',
      name: 'Alpine Marathon',
      distance: 'Marathon',
    },
    onClose: () => {},
  },
}

export const HalfMarathon: Story = {
  args: {
    race: {
      id: 'b0000000-0000-0000-0000-000000000003',
      name: 'Coastal Half Marathon',
      distance: 'HalfMarathon',
    },
    onClose: () => {},
  },
}
