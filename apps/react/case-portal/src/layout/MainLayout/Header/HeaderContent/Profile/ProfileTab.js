import PropTypes from 'prop-types'
import { useTranslation } from 'react-i18next'
import { useTheme } from '@mui/material/styles'
import {
  Divider,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material'
import LogoutOutlined from '@ant-design/icons/LogoutOutlined'

import LanguageTab from './LanguageTab'

const ProfileTab = ({ handleLogout }) => {
  const theme = useTheme()
  const { t } = useTranslation()

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
      <LanguageTab />
      <Divider />
      <ListItemButton onClick={handleLogout}>
        <ListItemIcon>
          <LogoutOutlined />
        </ListItemIcon>
        <ListItemText primary={t('menu.logout')} />
      </ListItemButton>
    </List>
  )
}

ProfileTab.propTypes = {
  handleLogout: PropTypes.func,
}

export default ProfileTab
