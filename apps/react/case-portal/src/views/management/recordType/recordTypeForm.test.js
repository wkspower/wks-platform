import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { RecordTypeForm } from './recordTypeForm'

const mockBuilder = { props: null }
jest.mock('@formio/react', () => ({
  FormBuilder: (props) => {
    mockBuilder.props = props
    return null
  },
}))

jest.mock('plugins/storage', () => ({ StorageService: class {} }))
jest.mock('services', () => ({
  RecordTypeService: {
    create: jest.fn(() => Promise.resolve()),
    update: jest.fn(() => Promise.resolve()),
    remove: jest.fn(() => Promise.resolve()),
  },
  MenuEventService: { triggerMenuUpdate: jest.fn() },
}))

const { RecordTypeService } = jest.requireMock('services')

const renderForm = (recordType) =>
  render(
    <RecordTypeForm
      open
      recordType={recordType}
      handleClose={jest.fn()}
      handleInputChange={jest.fn()}
    />,
  )

const designField = (field) =>
  act(() =>
    mockBuilder.props.onChange({}, { components: [field], display: 'form' }),
  )

beforeEach(() => {
  mockBuilder.props = null
  RecordTypeService.create.mockClear()
  RecordTypeService.update.mockClear()
})

describe('RecordTypeForm', () => {
  it('saves the fields designed in the builder on a new record type', async () => {
    const user = userEvent.setup()
    renderForm({
      id: 'customer',
      fields: { components: [], display: 'form' },
      mode: 'new',
    })

    designField({ key: 'email', type: 'email' })
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, payload] = RecordTypeService.create.mock.calls[0]
    expect(payload.fields.components).toEqual([{ key: 'email', type: 'email' }])
  })

  it('saves the fields edited in the builder on an existing record type', async () => {
    const user = userEvent.setup()
    renderForm({
      id: 'customer',
      fields: { components: [{ key: 'email' }], display: 'form' },
    })

    designField({ key: 'phone' })
    await user.click(screen.getByRole('button', { name: 'Save' }))

    const [, id, payload] = RecordTypeService.update.mock.calls[0]
    expect(id).toBe('customer')
    expect(payload.fields.components).toEqual([{ key: 'phone' }])
  })

  it('passes an onChange to the builder, its only outbound path', () => {
    renderForm({ id: 'customer', fields: { components: [], display: 'form' } })
    expect(typeof mockBuilder.props.onChange).toBe('function')
  })
})
