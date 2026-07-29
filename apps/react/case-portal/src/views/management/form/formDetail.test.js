import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FormDetail } from './formDetail'

const mockBuilder = { props: null }
jest.mock('@formio/react', () => ({
  FormBuilder: (props) => {
    mockBuilder.props = props
    return null
  },
}))

jest.mock('plugins/storage', () => ({ StorageService: class {} }))
jest.mock('services', () => ({
  FormService: {
    update: jest.fn(() => Promise.resolve()),
    remove: jest.fn(() => Promise.resolve()),
  },
}))

const { FormService } = jest.requireMock('services')

const existingForm = () => ({
  key: 'customer',
  title: 'Customer',
  toolTip: '',
  structure: { components: [{ key: 'firstName' }], display: 'form' },
})

const renderDetail = (form = existingForm(), props = {}) =>
  render(
    <FormDetail
      open
      form={form}
      handleClose={jest.fn()}
      handleInputChange={jest.fn()}
      handleSelectDisplay={jest.fn()}
      {...props}
    />,
  )

beforeEach(() => {
  mockBuilder.props = null
  FormService.update.mockClear()
})

describe('FormDetail', () => {
  it('saves the components edited in the builder', async () => {
    const user = userEvent.setup()
    renderDetail()

    act(() =>
      mockBuilder.props.onChange(
        {},
        { components: [{ key: 'firstName' }, { key: 'lastName' }] },
      ),
    )
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, key, payload] = FormService.update.mock.calls[0]
    expect(key).toBe('customer')
    expect(payload.structure.components).toEqual([
      { key: 'firstName' },
      { key: 'lastName' },
    ])
  })

  it('saves a component removed in the builder', async () => {
    const user = userEvent.setup()
    renderDetail()

    act(() => mockBuilder.props.onChange({}, { components: [] }))
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, , payload] = FormService.update.mock.calls[0]
    expect(payload.structure.components).toEqual([])
  })

  it('leaves the structure untouched when nothing was edited', async () => {
    const user = userEvent.setup()
    const form = existingForm()
    renderDetail(form)

    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, , payload] = FormService.update.mock.calls[0]
    expect(payload.structure).toEqual(form.structure)
  })

  it('passes an onChange to the builder, its only outbound path', () => {
    renderDetail()
    expect(typeof mockBuilder.props.onChange).toBe('function')
  })
})
