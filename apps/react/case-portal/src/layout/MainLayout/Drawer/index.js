import PropTypes from 'prop-types'
import { useMemo } from 'react'
import { useTheme } from '@mui/material/styles'
import Box from '@mui/material/Box'
import Drawer from '@mui/material/Drawer'
import DrawerHeader from './DrawerHeader'
import DrawerContent from './DrawerContent'
import { drawerWidth } from 'config'

const MainDrawer = ({ open, handleDrawerToggle, window }) => {
  const theme = useTheme()
  const container =
    window !== undefined ? () => window().document.body : undefined

  const drawerContent = useMemo(() => <DrawerContent />, [])
  const drawerHeader = useMemo(() => <DrawerHeader open={open} />, [open])

  return (
    <Box
      component='nav'
      sx={{ flexShrink: 0, zIndex: 1300 }}
      aria-label='sidebar'
    >
      <Drawer
        container={container}
        variant='temporary'
        open={open}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true,
        }}
        sx={{
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            borderRight: `1px solid ${theme.palette.divider}`,
            backgroundImage: 'none',
            boxShadow: theme.shadows[6],
            overflowX: 'hidden',
          },
        }}
      >
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
      </Drawer>
    </Box>
  )
}

MainDrawer.propTypes = {
  open: PropTypes.bool,
  handleDrawerToggle: PropTypes.func,
  window: PropTypes.object,
}

export default MainDrawer
