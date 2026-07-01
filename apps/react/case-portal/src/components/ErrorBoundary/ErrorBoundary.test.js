import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ErrorBoundary from './ErrorBoundary'

function Boom({ crash }) {
  if (crash) throw new Error('kaboom')
  return <div>all good</div>
}

describe('ErrorBoundary', () => {
  let consoleError
  beforeEach(() => {
    // React logs caught errors; silence it for clean test output.
    consoleError = jest.spyOn(console, 'error').mockImplementation(() => {})
  })
  afterEach(() => consoleError.mockRestore())

  it('renders children when there is no error', () => {
    render(
      <ErrorBoundary>
        <Boom crash={false} />
      </ErrorBoundary>,
    )
    expect(screen.getByText('all good')).toBeInTheDocument()
  })

  it('renders the fallback with the error message when a child throws', () => {
    render(
      <ErrorBoundary title='Broke'>
        <Boom crash={true} />
      </ErrorBoundary>,
    )
    expect(screen.getByText('Broke')).toBeInTheDocument()
    expect(screen.getByText('kaboom')).toBeInTheDocument()
  })

  it('invokes onReset when "Try again" is clicked', async () => {
    const user = userEvent.setup()
    const onReset = jest.fn()
    render(
      <ErrorBoundary onReset={onReset}>
        <Boom crash={true} />
      </ErrorBoundary>,
    )
    await user.click(screen.getByRole('button', { name: /try again/i }))
    expect(onReset).toHaveBeenCalledTimes(1)
  })
})
