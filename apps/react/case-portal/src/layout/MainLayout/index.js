import { useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Outlet } from 'react-router-dom'
import { Box, Toolbar } from '@mui/material'
import Breadcrumbs from 'components/@extended/Breadcrumbs'
import Drawer from './Drawer'
import Header from './Header'
import { openDrawer } from 'store/reducers/menu'
import { useMenuContext } from 'menu/menuProvider'

const MainLayout = ({ keycloak, authenticated }) => {
  const dispatch = useDispatch()
  const { drawerOpen: open } = useSelector((state) => state.menu)
  // const menu = useMenu()
  const { items: menuItems } = useMenuContext()
  const menu = { items: [...menuItems] }

  const handleDrawerToggle = useCallback(() => {
    dispatch(openDrawer({ drawerOpen: !open }))
  }, [dispatch, open])

  return (
    keycloak &&
    authenticated && (
      <Box sx={{ display: 'flex', width: '100%' }}>
        <Header
          open={open}
          handleDrawerToggle={handleDrawerToggle}
          keycloak={keycloak}
        />
        <Drawer open={open} handleDrawerToggle={handleDrawerToggle} />
        <Box
          component='main'
          sx={{ width: '100%', flexGrow: 1, p: { xs: 2, sm: 3 } }}
        >
          <Toolbar />
          <Breadcrumbs navigation={menu} divider={false} />
          <Outlet />
        </Box>
      </Box>
    )
  )
}

export default MainLayout
