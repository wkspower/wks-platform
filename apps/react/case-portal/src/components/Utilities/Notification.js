import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'
import { green, amber, red, blueGrey } from '@mui/material/colors'
import { styled } from '@mui/material/styles'

const StyledAlert = styled(Alert)(({ theme, severity }) => ({
  ...(severity === 'success' && {
    backgroundColor: green[600],
    color: theme.palette.common.white,
  }),
  ...(severity === 'warning' && {
    backgroundColor: amber[700],
    color: theme.palette.common.black, // Darker text for better contrast
  }),
  ...(severity === 'error' && {
    backgroundColor: red[700],
    color: theme.palette.common.white,
  }),
  ...(severity === 'info' && {
    backgroundColor: blueGrey[700],
    color: theme.palette.common.white,
  }),
  '& .MuiAlert-icon': {
    color: theme.palette.common.white, // Ensure icon color is consistent
  },
}))

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
      sx={{ top: '65px !important' }}
    >
      <StyledAlert onClose={onClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </StyledAlert>
    </Snackbar>
  )
}

export default Notification
