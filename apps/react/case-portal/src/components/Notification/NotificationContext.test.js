import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { NotificationProvider, useNotification } from './NotificationContext'

function Trigger() {
  const { notifyError, notifySuccess } = useNotification()
  return (
    <div>
      <button onClick={() => notifyError('something failed')}>fail</button>
      <button onClick={() => notifySuccess('all done')}>ok</button>
    </div>
  )
}

describe('NotificationProvider', () => {
  it('shows an error message when notifyError is called', async () => {
    const user = userEvent.setup()
    render(
      <NotificationProvider>
        <Trigger />
      </NotificationProvider>,
    )
    expect(screen.queryByText('something failed')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'fail' }))
    expect(await screen.findByText('something failed')).toBeInTheDocument()
  })

  it('shows a success message when notifySuccess is called', async () => {
    const user = userEvent.setup()
    render(
      <NotificationProvider>
        <Trigger />
      </NotificationProvider>,
    )
    await user.click(screen.getByRole('button', { name: 'ok' }))
    expect(await screen.findByText('all done')).toBeInTheDocument()
  })
})

describe('useNotification outside a provider', () => {
  it('returns safe no-ops that do not throw', async () => {
    const user = userEvent.setup()
    const warn = jest.spyOn(console, 'error').mockImplementation(() => {})
    // No provider wrapping — must not crash the render or the click.
    render(<Trigger />)
    await user.click(screen.getByRole('button', { name: 'fail' }))
    expect(warn).toHaveBeenCalled()
    warn.mockRestore()
  })
})
