import { Component } from 'react'
import { Alert, AlertTitle, Box, Button } from '@mui/material'

/**
 * Catches render/lifecycle errors in its subtree and shows a recoverable
 * fallback instead of letting the whole app white-screen. Place one at the app
 * root and one per route so a single view's crash stays contained.
 *
 * Props:
 *  - fallback: optional custom node/render-fn (error, reset) => node
 *  - onReset: optional callback invoked when the user clicks "Try again"
 *  - title:   optional heading for the default fallback
 */
class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null }
    this.reset = this.reset.bind(this)
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    // Keep a console trail for diagnostics; the UI degrades gracefully below.
    console.error('ErrorBoundary caught an error:', error, info?.componentStack)
  }

  reset() {
    this.setState({ error: null })
    if (this.props.onReset) this.props.onReset()
  }

  render() {
    const { error } = this.state
    if (!error) return this.props.children

    const { fallback, title = 'Something went wrong' } = this.props
    if (fallback) {
      return typeof fallback === 'function'
        ? fallback(error, this.reset)
        : fallback
    }

    return (
      <Box sx={{ p: 3, maxWidth: 720, mx: 'auto' }}>
        <Alert
          severity='error'
          action={
            <Button color='inherit' size='small' onClick={this.reset}>
              Try again
            </Button>
          }
        >
          <AlertTitle>{title}</AlertTitle>
          {error?.message || 'An unexpected error occurred.'}
        </Alert>
      </Box>
    )
  }
}

export default ErrorBoundary
