import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Box,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Typography,
  CircularProgress,
  Divider,
} from '@mui/material'
import AddIcon from '@mui/icons-material/AddCircleOutline'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import CommentIcon from '@mui/icons-material/Comment'
import HistoryIcon from '@mui/icons-material/History'
import WarningIcon from '@mui/icons-material/Warning'
import { CaseService } from '../../services'

export const CaseHistory = ({ businessKey, keycloak }) => {
  const { t } = useTranslation()
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    if (!businessKey) {
      setLoading(false)
      return
    }

    CaseService.getCaseHistory(keycloak, businessKey)
      .then((data) => {
        if (data) {
          setHistory(data)
        } else {
          setError(true)
        }
        setLoading(false)
      })
      .catch((err) => {
        console.error('Error fetching history:', err)
        setError(true)
        setLoading(false)
      })
  }, [businessKey, keycloak])

  const getEventStyles = (eventType) => {
    switch (eventType) {
      case 'CASE_CREATED':
        return {
          icon: <AddIcon />,
          color: 'success.main',
          bgColor: '#e8f5e9', // Light green
        }
      case 'CASE_UPDATED':
      case 'CASE_PATCHED':
        return {
          icon: <EditIcon />,
          color: 'warning.main',
          bgColor: '#fff3e0', // Light orange
        }
      case 'CASE_DELETED':
        return {
          icon: <DeleteIcon />,
          color: 'error.main',
          bgColor: '#ffebee', // Light red
        }
      case 'COMMENT_ADDED':
        return {
          icon: <CommentIcon />,
          color: 'info.main',
          bgColor: '#e3f2fd', // Light blue
        }
      case 'COMMENT_UPDATED':
        return {
          icon: <EditIcon />,
          color: 'info.main',
          bgColor: '#e3f2fd',
        }
      case 'COMMENT_DELETED':
        return {
          icon: <DeleteIcon />,
          color: 'error.main',
          bgColor: '#ffebee',
        }
      default:
        return {
          icon: <HistoryIcon />,
          color: 'grey.600',
          bgColor: '#f5f5f5', // Light grey
        }
    }
  }

  const getEventText = (event) => {
    const key = `pages.caseform.history.events.${event.eventType}`
    const translated = t(key, { user: event.userId })
    if (translated === key) {
      return `${event.eventType.replace('_', ' ')} by ${event.userId}`
    }
    return translated
  }

  const renderDiffs = (payloadStr) => {
    try {
      const payload = JSON.parse(payloadStr)
      if (!payload) return null

      // If comment body is logged
      if (payload.body) {
        if (typeof payload.body === 'object') {
          return (
            <Typography
              variant='body2'
              color='textSecondary'
              sx={{
                mt: 0.5,
                fontStyle: 'italic',
                pl: 1,
                borderLeft: '3px solid #ccc',
              }}
            >
              {t('pages.caseform.history.diff.comment', {
                before: payload.body.before || '',
                after: payload.body.after || '',
              })}
            </Typography>
          )
        }

        return (
          <Typography
            variant='body2'
            color='textSecondary'
            sx={{
              mt: 0.5,
              fontStyle: 'italic',
              pl: 1,
              borderLeft: '3px solid #ccc',
            }}
          >
            &quot;{payload.body}&quot;
          </Typography>
        )
      }

      // Check if it has status, stage or queueId changes
      const changes = []
      if (
        payload.status &&
        (payload.status.before !== undefined ||
          payload.status.after !== undefined)
      ) {
        changes.push(
          t('pages.caseform.history.diff.status', {
            before: payload.status.before || '-',
            after: payload.status.after || '-',
          }),
        )
      }
      if (
        payload.stage &&
        (payload.stage.before !== undefined ||
          payload.stage.after !== undefined)
      ) {
        changes.push(
          t('pages.caseform.history.diff.stage', {
            before: payload.stage.before || '-',
            after: payload.stage.after || '-',
          }),
        )
      }
      if (
        payload.queueId &&
        (payload.queueId.before !== undefined ||
          payload.queueId.after !== undefined)
      ) {
        changes.push(
          t('pages.caseform.history.diff.queueId', {
            before: payload.queueId.before || '-',
            after: payload.queueId.after || '-',
          }),
        )
      }

      if (changes.length > 0) {
        return (
          <Box sx={{ mt: 0.5, pl: 1 }}>
            {changes.map((change, idx) => (
              <Typography
                key={idx}
                variant='caption'
                display='block'
                color='textSecondary'
              >
                • {change}
              </Typography>
            ))}
          </Box>
        )
      }
    } catch (e) {
      if (
        payloadStr &&
        typeof payloadStr === 'string' &&
        payloadStr.trim() !== '{}'
      ) {
        return (
          <Typography
            variant='caption'
            display='block'
            color='textSecondary'
            sx={{ mt: 0.5 }}
          >
            {payloadStr}
          </Typography>
        )
      }
    }
    return null
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress size={24} />
      </Box>
    )
  }

  if (error) {
    return (
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          p: 2,
          color: 'text.secondary',
        }}
      >
        <WarningIcon color='action' />
        <Typography variant='body2'>
          {t('pages.caseform.history.unavailable')}
        </Typography>
      </Box>
    )
  }

  if (history.length === 0) {
    return (
      <Box sx={{ p: 2, color: 'text.secondary', textAlign: 'center' }}>
        <Typography variant='body2'>
          {t('pages.caseform.history.empty')}
        </Typography>
      </Box>
    )
  }

  return (
    <Box>
      <List sx={{ width: '100%', bgcolor: 'background.paper', py: 0 }}>
        {history.map((event, index) => {
          const styles = getEventStyles(event.eventType)
          return (
            <React.Fragment key={event.id || index}>
              <ListItem alignItems='flex-start' sx={{ px: 1, py: 1.5 }}>
                <ListItemAvatar sx={{ minWidth: 48 }}>
                  <Avatar
                    sx={{
                      bgcolor: styles.bgColor,
                      color: styles.color,
                      width: 32,
                      height: 32,
                    }}
                  >
                    {styles.icon}
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary={
                    <Typography
                      variant='subtitle2'
                      component='div'
                      sx={{ fontWeight: 600 }}
                    >
                      {getEventText(event)}
                    </Typography>
                  }
                  secondary={
                    <React.Fragment>
                      <Typography
                        variant='caption'
                        color='textSecondary'
                        display='block'
                        sx={{ mt: 0.2 }}
                      >
                        {new Date(event.timestamp).toLocaleString()}
                      </Typography>
                      {renderDiffs(event.payload)}
                    </React.Fragment>
                  }
                />
              </ListItem>
              {index < history.length - 1 && (
                <Divider variant='inset' component='li' sx={{ ml: 6 }} />
              )}
            </React.Fragment>
          )
        })}
      </List>
    </Box>
  )
}
