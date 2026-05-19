import CloseIcon from '@mui/icons-material/Close'
import {
  AppBar,
  Box,
  Button,
  Dialog,
  IconButton,
  Slide,
  Stack,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material'
import MainCard from 'components/MainCard'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { TaskService } from 'services'
import { useSession } from '../../SessionStoreContext'

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const AdHocTaskForm = ({ open, handleClose, task }) => {
  const [claimed, setClaimed] = useState(false)
  const [assignee, setAssignee] = useState(null)
  const [completionNotes, setCompletionNotes] = useState('')
  const { t } = useTranslation()
  const keycloak = useSession()

  useEffect(() => {
    setClaimed(task?.assignee != null)
    setAssignee(task?.assignee ?? null)
    setCompletionNotes('')
  }, [open, task])

  const handleClaim = () => {
    TaskService.claim(keycloak, task.id)
      .then(() => {
        setClaimed(true)
        setAssignee(keycloak.idTokenParsed.given_name)
      })
      .catch((err) => console.log(err.message))
  }

  const handleUnclaim = () => {
    TaskService.unclaim(keycloak, task.id)
      .then(() => {
        setClaimed(false)
        setAssignee(null)
      })
      .catch((err) => console.log(err.message))
  }

  const handleComplete = () => {
    const variables = completionNotes
      ? [
          {
            name: 'completionNotes',
            value: completionNotes,
            type: 'String',
          },
        ]
      : []

    TaskService.complete(keycloak, task.id, variables)
      .then(() => handleClose())
      .catch((err) => console.log(err.message))
  }

  return (
    <Dialog
      fullScreen
      open={open}
      onClose={handleClose}
      TransitionComponent={Transition}
    >
      <AppBar sx={{ position: 'relative' }}>
        <Toolbar>
          <IconButton
            edge='start'
            color='inherit'
            onClick={handleClose}
            aria-label='close'
          >
            <CloseIcon />
          </IconButton>
          <Typography sx={{ ml: 2, flex: 1 }} component='div'>
            <div>{task?.name}</div>
            <div style={{ fontSize: '13px' }}>{task?.caseInstanceId}</div>
            <div style={{ fontSize: '10px' }}>{task?.id}</div>
          </Typography>
          {!claimed ? (
            <Button color='inherit' onClick={handleClaim}>
              {t('pages.taskform.claim')}
            </Button>
          ) : (
            <Button color='inherit' onClick={handleUnclaim}>
              <div>
                {assignee} <sup style={{ fontSize: '10px' }}>x</sup>
              </div>
            </Button>
          )}
          {claimed && (
            <Button color='inherit' onClick={handleComplete}>
              {t('pages.taskform.complete')}
            </Button>
          )}
        </Toolbar>
      </AppBar>

      <Box sx={{ p: 2 }}>
        <MainCard>
          <Stack spacing={2}>
            <Stack direction='row' spacing={4}>
              <Box>
                <Typography variant='caption' color='text.secondary'>
                  {t('pages.tasklist.datagrid.columns.due')}
                </Typography>
                <Typography>{task?.due || '—'}</Typography>
              </Box>
              <Box>
                <Typography variant='caption' color='text.secondary'>
                  {t('pages.tasklist.datagrid.columns.assignee')}
                </Typography>
                <Typography>{assignee || '—'}</Typography>
              </Box>
              <Box>
                <Typography variant='caption' color='text.secondary'>
                  {t('pages.tasklist.datagrid.columns.created')}
                </Typography>
                <Typography>{task?.created || '—'}</Typography>
              </Box>
            </Stack>

            <Box>
              <Typography variant='caption' color='text.secondary'>
                {t('pages.tasklist.newTask.description')}
              </Typography>
              <Typography sx={{ whiteSpace: 'pre-wrap' }}>
                {task?.description || '—'}
              </Typography>
            </Box>

            <TextField
              label='Completion notes'
              multiline
              minRows={4}
              fullWidth
              value={completionNotes}
              onChange={(e) => setCompletionNotes(e.target.value)}
              disabled={!claimed}
              helperText={
                claimed
                  ? 'Optional. Saved with the task on completion.'
                  : 'Claim the task to add notes and complete it.'
              }
            />
          </Stack>
        </MainCard>
      </Box>
    </Dialog>
  )
}
