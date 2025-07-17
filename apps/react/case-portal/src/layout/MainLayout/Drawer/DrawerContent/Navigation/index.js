import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import NavGroup from './NavGroup'

import { useMenuContext } from 'menu/menuProvider'
import { useSession } from 'SessionStoreContext'

const Navigation = () => {
  // const menu = useMenu()
  const keycloak = useSession()
  const { items: menuItems } = useMenuContext()
  const menu = { items: [...menuItems] }
  const isPlantManager = keycloak?.realmAccess?.roles?.includes('plant_manager')

  const filterMenuByRole = (menuItems, hasPlantManagerRole) => {
    return menuItems.map((item) => {
      if (item.type === 'group' && item.children) {
        const filteredChildren = item.children.filter((child) => {
          if (child.id === 'user-management' && !hasPlantManagerRole) {
            return false
          }
          return true
        })

        return {
          ...item,
          children: filteredChildren,
        }
      }
      return item
    })
  }

  const filteredMenu = {
    ...menu,
    items: filterMenuByRole(menu?.items || [], isPlantManager),
  }
  const navGroups = filteredMenu?.items?.map((item, index) => {
    switch (item.type) {
      case 'group':
        return <NavGroup key={`${item.id}-${index}`} item={item} />
      default:
        return (
          <Typography
            key={`${item.id}-${index}`}
            variant='h6'
            color='error'
            align='center'
          >
            Fix - Navigation Group
          </Typography>
        )
    }
  })

  return (
    <Box
      sx={{
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          overflowY: 'auto',
          overflowX: 'hidden',
          flex: 1,
          pr: 1,
        }}
      >
        {navGroups}
      </Box>

      {/* <Box sx={{ p: 2, mb: 2.5 }}>
        <LogoBottom />
      </Box> */}
    </Box>
  )
}

export default Navigation
