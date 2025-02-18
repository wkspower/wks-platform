import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'

const Notification = ({
  open,
  message = 'An error occurred. Please try again.',
  severity = 'info',
  duration = 3000,
  onClose,
}) => {
  return (
    <Snackbar
      open={open}
      autoHideDuration={duration}
      onClose={onClose}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      sx={{ top: '85px !important' }}
    >
      <Alert onClose={onClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  )
}

export default Notification
