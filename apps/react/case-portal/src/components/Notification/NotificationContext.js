import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from 'react'
import { Alert, Snackbar } from '@mui/material'

/**
 * App-wide notification (toast) provider. Gives components a single, consistent
 * way to surface success/error/info messages instead of the ad-hoc per-component
 * Snackbars (or silent console.log catch blocks) scattered across the portal.
 *
 * Usage:
 *   const { notifyError, notifySuccess } = useNotification()
 *   ...catch (e) { notifyError(e.message) }
 */
const NotificationContext = createContext(null)

export function NotificationProvider({ children }) {
  const [state, setState] = useState({
    open: false,
    message: '',
    severity: 'info',
  })

  const notify = useCallback((message, severity = 'info') => {
    setState({ open: true, message: String(message ?? ''), severity })
  }, [])

  const handleClose = useCallback((_event, reason) => {
    if (reason === 'clickaway') return
    setState((s) => ({ ...s, open: false }))
  }, [])

  const value = useMemo(
    () => ({
      notify,
      notifySuccess: (m) => notify(m, 'success'),
      notifyError: (m) => notify(m, 'error'),
      notifyInfo: (m) => notify(m, 'info'),
      notifyWarning: (m) => notify(m, 'warning'),
    }),
    [notify],
  )

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <Snackbar
        open={state.open}
        autoHideDuration={6000}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={handleClose}
          severity={state.severity}
          variant='filled'
          sx={{ width: '100%' }}
        >
          {state.message}
        </Alert>
      </Snackbar>
    </NotificationContext.Provider>
  )
}

/**
 * Access the notification API. Falls back to console-based no-ops if used
 * outside a provider, so a stray call can never crash a render.
 */
export function useNotification() {
  const ctx = useContext(NotificationContext)
  if (!ctx) {
    return {
      notify: (m) => console.warn('[notify]', m),
      notifySuccess: (m) => console.info('[notify:success]', m),
      notifyError: (m) => console.error('[notify:error]', m),
      notifyInfo: (m) => console.info('[notify:info]', m),
      notifyWarning: (m) => console.warn('[notify:warning]', m),
    }
  }
  return ctx
}

export default NotificationContext
