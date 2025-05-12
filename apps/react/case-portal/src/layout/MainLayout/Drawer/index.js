import PropTypes from 'prop-types'
import { useMemo } from 'react'
import { useTheme } from '@mui/material/styles'
import Box from '@mui/material/Box'
import Drawer from '@mui/material/Drawer'
import DrawerHeader from './DrawerHeader'
import DrawerContent from './DrawerContent'
import { drawerWidth } from 'config'

const MainDrawer = ({ open }) => {
  const theme = useTheme()

  const drawerContent = useMemo(() => <DrawerContent />, [])
  const drawerHeader = useMemo(() => <DrawerHeader open={open} />, [open])

  return (
    <Drawer
      variant='persistent'
      open={open}
      sx={{
        width: open ? drawerWidth : 0,
        flexShrink: 0,
        whiteSpace: 'nowrap',
        transition: theme.transitions.create('width', {
          easing: theme.transitions.easing.easeOut, // Using easeOut for smoother deceleration
          duration: theme.transitions.duration.standard,
        }),
        '& .MuiDrawer-paper': {
          width: open ? drawerWidth : 0,
          transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.easeOut, // Consistent easing for the paper
            duration: theme.transitions.duration.standard,
          }),
          boxSizing: 'border-box',
          borderRight: `1px solid ${theme.palette.divider}`,
          backgroundImage: 'none',
          boxShadow: theme.shadows[6],
          overflowX: 'hidden',
        },
      }}
    >
      {open && (
        <Box
          sx={{
            height: '100vh',
            overflowY: 'auto',
            overflowX: 'hidden',
          }}
          role='presentation'
        >
          {drawerHeader}
          {drawerContent}
        </Box>
      )}
    </Drawer>
  )
}

MainDrawer.propTypes = {
  open: PropTypes.bool.isRequired,
}

export default MainDrawer
