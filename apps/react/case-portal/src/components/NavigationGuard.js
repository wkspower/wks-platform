import { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogActions from '@mui/material/DialogActions'
import Button from '@mui/material/Button'

const NavigationGuard = () => {
  const { isBlocked } = useSelector((state) => state.dataGridStore)
  const location = useLocation()
  const navigate = useNavigate()
  const dispatch = useDispatch()

  // State to control modal visibility.
  const [open, setOpen] = useState(false)

  // Monitor isBlocked and open the modal if needed.
  useEffect(() => {
    if (isBlocked) {
      // Instead of immediately navigating, we trigger the modal.
      setOpen(true)
    }
  }, [navigate, location])

  // Called when user clicks "Yes"
  const handleYes = () => {
    // User confirms to leave.
    dispatch(setIsBlocked(false)) // update redux: no longer blocked
    setOpen(false)
    // Optionally, navigate away if needed:
    // navigate(newLocation, { replace: true })
  }

  // Called when user clicks "No"
  const handleNo = () => {
    // User cancels leaving. Keep isBlocked as true.
    setOpen(false)
    // Optionally, force navigation back to current page:
    navigate(location.pathname, { replace: true })
  }

  return (
    <Dialog open={open} onClose={handleNo}>
      <DialogTitle>
        You have unsaved changes. Do you really want to leave?
      </DialogTitle>
      <DialogActions>
        <Button onClick={handleNo} color='primary'>
          No
        </Button>
        <Button onClick={handleYes} color='primary' autoFocus>
          Yes
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default NavigationGuard
