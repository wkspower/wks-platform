import Avatar from '@mui/material/Avatar'
import Chip from '@mui/material/Chip'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import Typography from '@mui/material/Typography'
import { useTheme } from '@mui/material/styles'
import PropTypes from 'prop-types'
import { useDispatch, useSelector } from 'react-redux'
import { useEffect } from 'react'
import { activeItem } from 'store/reducers/menu'
// import useSafeNavigate from ''
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Button from '@mui/material/Button'
import { useSafeNavigate } from './useSafeNavigate'
import { Tooltip } from '../../../../../../node_modules/@mui/material/index'
// import { setIsBlocked } from 'store/reducers/dataGridStore'
import { useLocation } from 'react-router-dom'
const NavItem = ({ item, level }) => {
  const theme = useTheme()
  const dispatch = useDispatch()
  const menu = useSelector((state) => state.menu)
  const { drawerOpen, openItem } = menu
  const { safeNavigate, confirmLeave, setDialogOpen, dialogOpen, itemHandler } =
    useSafeNavigate()
  const location = useLocation()

  // const [openDialog, setOpenDialog] = useState(false)

  const handleClick = (id) => {
    // console.log(item)
    if (item.requiresConfirmation) {
      setDialogOpen(true)
    } else {
      dispatch(activeItem({ openItem: [id] }))
      itemHandler(id)
      safeNavigate(item.url)
    }
  }

  const stayOnPage = () => {
    // dispatch(setIsBlocked(false))
    setDialogOpen(false)
    // dispatch(activeItem({ openItem: [item.id] }))
    // safeNavigate(item.url)

    // console.log(item)
  }

  // const cancelNavigation = () => {
  //   setDialogOpen(false)
  // }

  const Icon = item.icon
  const itemIcon = Icon ? (
    <Icon style={{ fontSize: drawerOpen ? '1rem' : '1.25rem' }} />
  ) : null
  console.log(openItem)
  const isSelected = openItem.findIndex((id) => id === item.id) > -1

  useEffect(() => {
    const currentIndex = location.pathname
      .toString()
      .split('/')
      .findIndex((id) => id === item.id)
    if (currentIndex > -1) {
      dispatch(activeItem({ openItem: [item.id] }))
    }
  }, [])
  useEffect(() => {
    // runs on every URL change
    if (location.pathname.split('/').includes(item.id)) {
      dispatch(activeItem({ openItem: [item.id] }))
    }
  }, [location.pathname, item.id, dispatch])

  const textColor = 'text.primary'

  return (
    <>
      <ListItemButton
        disabled={item.disabled}
        // onClick={() => {
        //   itemHandler(item.id)
        //   handleClick()
        // }}
        onClick={() => handleClick(item.id)}
        selected={isSelected}
        sx={{
          zIndex: 1201,
          pl: drawerOpen ? `${level * 28}px` : 1.5,
          // pl: 2.5,
          py: !drawerOpen && level === 1 ? 1.25 : 1,
          ...(drawerOpen && {
            '&:hover': {
              bgcolor: '#0100cb',
              color: 'white',
            },
            '&.Mui-selected': {
              bgcolor: '#0100cb',
              borderRight: `2px solid ${theme.palette.primary.main}`,
              color: 'white',
              borderRadius: 0,
              '&:hover': {
                bgcolor: '#0100cb',
                color: 'white',
              },
            },
          }),
          ...(!drawerOpen && {
            '&:hover': {
              bgcolor: '#0100cb',
            },
            '&.Mui-selected': {
              bgcolor: '#0100cb',
              '&:hover': {
                bgcolor: '#0100cb',
              },
            },
          }),
        }}
      >
        {itemIcon && (
          <ListItemIcon
            sx={{
              minWidth: 28,
              color: isSelected ? 'white' : textColor,
              ...(!drawerOpen && {
                borderRadius: 1.5,
                width: 36,
                height: 36,
                alignItems: 'center',
                justifyContent: 'center',
                '&:hover': {
                  bgcolor: 'secondary.lighter',
                },
              }),
              ...(!drawerOpen &&
                isSelected && {
                  bgcolor: '#0100cb',
                  '&:hover': {
                    bgcolor: '#0100cb',
                  },
                }),
            }}
          >
            {itemIcon}
          </ListItemIcon>
        )}
        {(drawerOpen || (!drawerOpen && level !== 1)) && (
          <ListItemText
            primary={
              // <Tooltip title={item.title} arrow>
              <Typography
                variant='h6'
                sx={{
                  color: isSelected ? 'white' : textColor,
                  '&:hover': {
                    color: 'white',
                    bgcolor: '#0100cb',
                  },
                  whiteSpace: 'normal', // Allows the text to wrap
                  wordBreak: 'break-word', // Breaks long words
                  display: '-webkit-box', // Uses a flexible box layout for text
                  WebkitLineClamp: 2, // Limits to 2 lines of text
                  WebkitBoxOrient: 'vertical', // Makes the text vertical
                  overflow: 'hidden', // Hides overflowing text
                  textOverflow: 'ellipsis', // Adds ellipsis (...) for overflowed text
                }}
              >
                {item.title}
              </Typography>
              // </Tooltip>
            }
          />
        )}
        {(drawerOpen || (!drawerOpen && level !== 1)) && item.chip && (
          <Chip
            color={item.chip.color}
            variant={item.chip.variant}
            size={item.chip.size}
            label={item.chip.label}
            avatar={item.chip.avatar && <Avatar>{item.chip.avatar}</Avatar>}
          />
        )}
      </ListItemButton>

      {/* Confirmation Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Unsaved Changes</DialogTitle>
        <DialogContent>
          You have unsaved changes. Are you sure you want to leave this page?
        </DialogContent>
        <DialogActions>
          <Button onClick={stayOnPage} color='error'>
            Stay
          </Button>
          <Button
            onClick={() => confirmLeave(item.id)}
            color='primary'
            autoFocus
          >
            Leave
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

NavItem.propTypes = {
  item: PropTypes.object,
  level: PropTypes.number,
}

export default NavItem
