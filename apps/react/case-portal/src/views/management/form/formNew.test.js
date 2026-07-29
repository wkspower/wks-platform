import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FormNew } from './formNew'

// Stand in for @formio/react's builder, which only reports edits through
// onChange — see useFormBuilderSchema.test.js for the contract this mirrors.
const mockBuilder = { props: null }
jest.mock('@formio/react', () => ({
  FormBuilder: (props) => {
    mockBuilder.props = props
    return null
  },
}))

jest.mock('plugins/storage', () => ({ StorageService: class {} }))
jest.mock('services', () => ({
  FormService: { create: jest.fn(() => Promise.resolve()) },
}))

const { FormService } = jest.requireMock('services')

const designComponent = (component) =>
  act(() =>
    mockBuilder.props.onChange(
      {},
      { components: [component], display: 'form' },
    ),
  )

beforeEach(() => {
  mockBuilder.props = null
  FormService.create.mockClear()
})

describe('FormNew', () => {
  it('saves the components designed in the builder', async () => {
    const user = userEvent.setup()
    render(<FormNew open handleClose={jest.fn()} />)

    designComponent({ key: 'firstName', type: 'textfield' })
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, payload] = FormService.create.mock.calls[0]
    expect(payload.structure.components).toEqual([
      { key: 'firstName', type: 'textfield' },
    ])
  })

  it('keeps the display chosen in the toolbar rather than the builder copy', async () => {
    const user = userEvent.setup()
    render(<FormNew open handleClose={jest.fn()} />)

    await user.click(screen.getByRole('combobox', { name: 'Display' }))
    await user.click(screen.getByRole('option', { name: 'Wizard' }))
    designComponent({ key: 'firstName' })
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, payload] = FormService.create.mock.calls[0]
    expect(payload.structure.display).toBe('wizard')
    expect(payload.structure.components).toEqual([{ key: 'firstName' }])
  })

  it('passes an onChange to the builder, its only outbound path', () => {
    render(<FormNew open handleClose={jest.fn()} />)
    expect(typeof mockBuilder.props.onChange).toBe('function')
  })
})
