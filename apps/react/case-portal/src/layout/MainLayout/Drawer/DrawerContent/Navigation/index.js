import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import NavGroup from './NavGroup'
import { useMenu } from 'SessionStoreContext'
import useMenuItems from 'menu/index'
// import { usePlanMenu } from 'menu/new-plan'
// import useMenuItems from 'menu/index'
// import LogoBottom from 'components/Logo/LogoBottom'

const Navigation = () => {
  const menu = useMenu()
  // const { items: menuItems } = useMenuItems()
  // const menu = { items: [...menuItems] }

  // const { items: menuItems } = useMenuItems()
  // const menu = { items: [...menuItems] }
  // console.log(menu)

  const navGroups = menu.items.map((item, index) => {
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
