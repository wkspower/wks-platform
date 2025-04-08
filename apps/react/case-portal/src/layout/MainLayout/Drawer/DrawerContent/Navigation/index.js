import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import NavGroup from './NavGroup'
import { useMenu } from 'SessionStoreContext'

const Navigation = () => {
  const menu = useMenu()


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

  return <Box sx={{ pt: 2 }}>{navGroups}</Box>
}

export default Navigation
