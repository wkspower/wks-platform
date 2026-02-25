import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord'
import Collapse from '@mui/material/Collapse'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import Typography from '@mui/material/Typography'
import { useTheme } from '@mui/material/styles'
import { IconChevronDown, IconChevronUp } from '@tabler/icons-react'
import { verticalEnums } from 'enums/verticalEnums'
import PropTypes from 'prop-types'
import { useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import NavItem from './NavItem'

const NavCollapse = ({ menu, level }) => {
  const theme = useTheme()
  const [open, setOpen] = useState(true)
  const [selected, setSelected] = useState(menu.id)
  const { plantID, verticalChange, siteObject } = useSelector(
    (state) => state.dataGridStore,
  )
  const plantName = plantID?.plantName

  const SITE_NAME = siteObject?.name?.toLowerCase()

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const handleClick = () => {
    setOpen(!open)
    setSelected(!selected ? menu.id : null)
  }

  const menus = useMemo(() => {
    if (!menu?.children) return []

    const renderMenuItem = (item) => {
      const props = { key: item.id, level: level + 1 }

      switch (item.type) {
        case 'collapse':
          return <NavCollapse menu={item} {...props} />
        case 'item':
          return <NavItem item={item} {...props} />
        default:
          return (
            <Typography key={item.id} variant='h6' color='error' align='center'>
              Menu Items Error
            </Typography>
          )
      }
    }

    const shouldFilterSlowdown =
      (lowerVertName === verticalEnums.PE && plantName === 'LDPE') ||
      (verticalEnums.PE && SITE_NAME === 'dmd')

    const menuItems = shouldFilterSlowdown
      ? menu.children.filter((item) => item.id !== 'slowdown-norms')
      : menu.children

    return menuItems.map(renderMenuItem)
  }, [menu?.children, lowerVertName, plantName, level])

  const Icon = menu.icon
  const menuIcon = menu.icon ? (
    <Icon
      strokeWidth={1.5}
      size='1.2rem'
      style={{ marginTop: 'auto', marginBottom: 'auto' }}
    />
  ) : (
    <FiberManualRecordIcon
      sx={{
        width: selected === menu.id ? 6 : 5,
        height: selected === menu.id ? 6 : 5,
      }}
      fontSize={level > 0 ? 'inherit' : 'medium'}
    />
  )

  return (
    <>
      <ListItemButton
        sx={{
          mb: 0.1,
          alignItems: 'flex-start',
          backgroundColor: level > 1 ? 'transparent !important' : 'inherit',
          py: level > 1 ? 1 : 1.25,
          pl: 1,
        }}
        selected={selected === menu.id}
        onClick={handleClick}
      >
        <ListItemIcon sx={{ my: 'auto', minWidth: !menu.icon ? 8 : 26 }}>
          {menuIcon}
        </ListItemIcon>
        <ListItemText
          primary={
            <Typography
              variant={selected === menu.id ? 'h6' : 'body1'}
              color='inherit'
              className='side-menu'
            >
              {menu.title}
            </Typography>
          }
          secondary={
            menu.caption && (
              <Typography
                variant='caption'
                sx={{ ...theme.typography.subMenuCaption }}
                display='block'
                gutterBottom
              >
                {menu.caption}
              </Typography>
            )
          }
        />
        {open ? (
          <IconChevronUp
            stroke={1.5}
            size='1rem'
            style={{ marginTop: 'auto', marginBottom: 'auto' }}
          />
        ) : (
          <IconChevronDown
            stroke={1.5}
            size='1rem'
            style={{ marginTop: 'auto', marginBottom: 'auto' }}
          />
        )}
      </ListItemButton>
      <Collapse in={open} timeout='auto' unmountOnExit>
        <List
          component='div'
          disablePadding
          sx={{
            position: 'relative',
            '&:after': {
              content: "''",
              position: 'absolute',
              left: '10px',
              top: 0,
              height: '100%',
              width: '1px',
              opacity: 1,
              background: theme.palette.primary.light,
            },
          }}
        >
          {menus}
        </List>
      </Collapse>
    </>
  )
}

NavCollapse.propTypes = {
  menu: PropTypes.object,
  level: PropTypes.number,
}

export default NavCollapse
