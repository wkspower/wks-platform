import { useState, useEffect, useRef } from 'react'
import { useRoutes, useLocation, useNavigate } from 'react-router-dom'
import { MainRoutes } from './MainRoutes'
import { useDispatch, useSelector } from 'react-redux'
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogActions from '@mui/material/DialogActions'
import Button from '@mui/material/Button'
import { setIsBlocked } from 'store/reducers/dataGridStore'

export const ThemeRoutes = ({
  keycloak,
  authenticated,
  recordsTypes,
  casesDefinitions,
}) => {
  // Get the isBlocked flag from Redux
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { isBlocked } = dataGridStore

  const location = useLocation()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const [open, setOpen] = useState(false)
  const [pendingLocation, setPendingLocation] = useState(null)
  const lastLocation = useRef(location.pathname)

  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (isBlocked) {
        e.preventDefault()
        e.returnValue = '' // required by Chrome
      }
    }
    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  }, [isBlocked])

  useEffect(() => {
    if (isBlocked && location.pathname !== lastLocation.current) {
      setPendingLocation(location.pathname)
      setOpen(true)
      navigate(lastLocation.current, { replace: true })
    } else {
      lastLocation.current = location.pathname
    }
  }, [location, isBlocked, navigate])

  const handleStay = () => {
    console.log('true block')
    setOpen(false)
    // setIsBlocked(true)
  }

  const handleLeave = () => {
    console.log('go anyway')
    dispatch(setIsBlocked(false))
    setOpen(false)
    if (pendingLocation) {
      lastLocation.current = pendingLocation
      navigate(pendingLocation, { replace: true })
      setPendingLocation(null)
    }
  }

  return (
    <>
      <Dialog open={open} onClose={handleStay}>
        <DialogTitle>
          You have unsaved changes. Do you really want to leave?
        </DialogTitle>
        <DialogActions>
          <Button onClick={handleStay} color='primary'>
            Stay
          </Button>
          <Button onClick={handleLeave} color='primary' autoFocus>
            Leave
          </Button>
        </DialogActions>
      </Dialog>

      {useRoutes([
        MainRoutes(keycloak, authenticated, recordsTypes, casesDefinitions),
      ])}
    </>
  )
}

export default ThemeRoutes
