import PropTypes from 'prop-types'
import { useTheme } from '@mui/material/styles'
import { List, ListItemButton, ListItemIcon, ListItemText } from '@mui/material'
import LogoutOutlined from '@ant-design/icons/LogoutOutlined'

const ProfileTab = ({ handleLogout }) => {
  const theme = useTheme()

  return (
    <List
      component='nav'
      sx={{
        p: 0,
        '& .MuiListItemIcon-root': {
          minWidth: 32,
          color: theme.palette.grey[500],
        },
      }}
    >
      <ListItemButton onClick={handleLogout}>
        <ListItemIcon>
          <LogoutOutlined />
        </ListItemIcon>
        <ListItemText primary='Logout' />
      </ListItemButton>
    </List>
  )
}

ProfileTab.propTypes = {
  handleLogout: PropTypes.func,
}

export default ProfileTab
