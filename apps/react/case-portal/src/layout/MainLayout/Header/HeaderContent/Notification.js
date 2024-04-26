import { useRef, useState, useEffect } from 'react'
import { useTheme } from '@mui/material/styles'
import {
  Avatar,
  Badge,
  Box,
  ClickAwayListener,
  Divider,
  IconButton,
  List,
  ListItemButton,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  Paper,
  Popper,
  Typography,
  useMediaQuery,
} from '@mui/material'
import MainCard from 'components/MainCard'
import Transitions from 'components/@extended/Transitions'
import BellOutlined from '@ant-design/icons/BellOutlined'
import CloseOutlined from '@ant-design/icons/CloseOutlined'
import GiftOutlined from '@ant-design/icons/GiftOutlined'
import MessageOutlined from '@ant-design/icons/MessageOutlined'
import SettingOutlined from '@ant-design/icons/SettingOutlined'
import { NotificationService } from '../../../../services'
import { useSession } from 'SessionStoreContext'

// sx styles
const avatarSX = {
  width: 36,
  height: 36,
  fontSize: '1rem',
}

const actionSX = {
  mt: '6px',
  ml: 1,
  top: 'auto',
  right: 'auto',
  alignSelf: 'flex-start',

  transform: 'none',
}

const iconBackColorOpen = 'grey.300'
const iconBackColor = 'grey.100'

const Notification = () => {
  const theme = useTheme()
  const matchesXs = useMediaQuery(theme.breakpoints.down('md'))
  const anchorRef = useRef(null)
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([])
  const [badge, setBudget] = useState(0)
  const keycloak = useSession()

  const handleToggle = () => {
    setOpen((prevOpen) => !prevOpen)
  }

  const handleClose = (event) => {
    if (anchorRef.current && anchorRef.current.contains(event.target)) {
      return
    }
    setOpen(false)
  }

  useEffect(() => {
    let delay = 1000
    let timeout = null

    const updateNotify = () => {
      NotificationService.getNotifications(keycloak)
        .then((data) => {
          setBudget(data.length ? data[0].total : 0)
          setMessages(data)
          delay = delay * 30
          timeout = setTimeout(updateNotify, delay)
        })
        .catch((error) => {
          console.error('Could not update notify. waiting a bit...', error)
          delay = delay * 2
          timeout = setTimeout(updateNotify, delay)
        })
    }

    updateNotify()

    return () => timeout && clearTimeout(timeout)
  }, [])

  return (
    <Box sx={{ flexShrink: 0, ml: 0.75 }}>
      <IconButton
        disableRipple
        color='secondary'
        sx={{
          color: 'text.primary',
          bgcolor: open ? iconBackColorOpen : iconBackColor,
        }}
        aria-label='open profile'
        ref={anchorRef}
        aria-controls={open ? 'profile-grow' : undefined}
        aria-haspopup='true'
        onClick={handleToggle}
      >
        <Badge badgeContent={badge} color='primary'>
          <BellOutlined />
        </Badge>
      </IconButton>

      <Popper
        placement={matchesXs ? 'bottom' : 'bottom-end'}
        open={open}
        anchorEl={anchorRef.current}
        role={undefined}
        transition
        disablePortal
        popperOptions={{
          modifiers: [
            {
              name: 'offset',
              options: {
                offset: [matchesXs ? -5 : 0, 9],
              },
            },
          ],
        }}
      >
        {({ TransitionProps }) => (
          <Transitions type='fade' in={open} {...TransitionProps}>
            <Paper
              sx={{
                boxShadow: theme.customShadows.z1,
                width: '100%',
                minWidth: 450,
                maxWidth: 600,
                [theme.breakpoints.down('md')]: {
                  maxWidth: 450,
                },
              }}
            >
              <ClickAwayListener onClickAway={handleClose}>
                <MainCard
                  title='Notification'
                  elevation={0}
                  border={false}
                  content={false}
                  secondary={
                    <IconButton size='small' onClick={handleToggle}>
                      <CloseOutlined />
                    </IconButton>
                  }
                >
                  <List
                    component='nav'
                    sx={{
                      p: 0,
                      '& .MuiListItemButton-root': {
                        py: 0.5,
                        '& .MuiAvatar-root': avatarSX,
                        '& .MuiListItemSecondaryAction-root': {
                          ...actionSX,
                          position: 'relative',
                        },
                      },
                    }}
                  >
                    <NotificationList items={messages} />
                  </List>
                </MainCard>
              </ClickAwayListener>
            </Paper>
          </Transitions>
        )}
      </Popper>
    </Box>
  )
}

function NotificationList({ items }) {
  const hasMoreView = items.length ? items[0].total > 5 : false

  return (
    <>
      {items.map((data, index) => {
        return (
          <ListItemButton key={`${index}${data.businessKey}`}>
            <ListItemAvatar>
              <AvatarIcon stage={data.eventType} />
            </ListItemAvatar>

            <ListItemText primary={data.message} secondary={data.daysAgo} />

            <ListItemSecondaryAction>
              <Typography variant='caption' noWrap>
                {data.createdAt}
              </Typography>
            </ListItemSecondaryAction>
          </ListItemButton>
        )
      })}

      <Divider />

      {!hasMoreView && <br />}

      {hasMoreView && (
        <ListItemButton sx={{ textAlign: 'center', py: `${12}px !important` }}>
          <ListItemText
            primary={
              <Typography variant='h6' color='primary'>
                View All
              </Typography>
            }
          />
        </ListItemButton>
      )}
    </>
  )
}

function AvatarIcon({ stage }) {
  if (stage === 'data_collection_stg') {
    return (
      <Avatar
        sx={{
          color: 'success.main',
          bgcolor: 'success.lighter',
        }}
      >
        <GiftOutlined />
      </Avatar>
    )
  }

  if (stage === 'contract_writing_stg') {
    return (
      <Avatar
        sx={{
          color: 'primary.main',
          bgcolor: 'primary.lighter',
        }}
      >
        <MessageOutlined />
      </Avatar>
    )
  }

  if (stage === 'info_docs_analysis_stg') {
    return (
      <Avatar
        sx={{
          color: 'error.main',
          bgcolor: 'error.lighter',
        }}
      >
        <SettingOutlined />
      </Avatar>
    )
  }

  return
}

export default Notification
