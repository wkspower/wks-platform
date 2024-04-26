import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from '@mui/material'
import React, { useEffect, useState } from 'react'
import { ProcessDefService } from 'services/ProcessDefService'
import { useSession } from 'SessionStoreContext'
import { QueueService } from 'services/QueueService'

const styles = {
  dialogContent: {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
  },
  formControl: {
    minWidth: '180px',
  },
  typeSelect: {
    minWidth: '180px',
  },
  cancelButton: {
    color: '#f44336',
  },
  saveButton: {
    color: '#4caf50',
  },
}

export const CaseDefFormEventsForm = ({
  open,
  onClose,
  onSubmit,
  hookData,
  stages,
}) => {
  const keycloak = useSession()
  const [taskDefKey, setTaskDefKey] = useState('')
  const [processDefKey, setProcessDefKey] = useState('')
  const [eventType, setEventType] = useState('')
  const [actionType, setActionType] = useState('')
  const [newStage, setNewStage] = useState('')
  const [queueId, setQueueId] = useState('')

  const [processesDefinitions, setProcessesDefinitions] = useState()
  const [queues, setQueues] = useState()

  useEffect(() => {
    ProcessDefService.find(keycloak)
      .then((data) => {
        setProcessesDefinitions(data)
      })
      .catch((err) => {
        setProcessesDefinitions(null)
        console.log(err.message)
      })

    QueueService.find(keycloak)
      .then((data) => {
        setQueues(data)
      })
      .catch((err) => {
        setQueues(null)
        console.log(err.message)
      })

    if (hookData) {
      setTaskDefKey(hookData.taskDefKey || '')
      setProcessDefKey(hookData.processDefKey || '')
      setEventType(hookData.eventType || '')
      setActionType(hookData.actions[0]?.actionType || '')
      setNewStage(hookData.actions[0]?.newStage || '')
      setQueueId(hookData.actions[0]?.queueId || '')
    } else {
      setTaskDefKey('')
      setProcessDefKey('')
      setEventType('')
      setActionType('')
      setNewStage('')
      setQueueId('')
    }
  }, [hookData])

  const handleSubmit = () => {
    let actions = []

    if (actionType === 'CASE_STAGE_UPDATE_ACTION') {
      actions = [...actions, { actionType: actionType, newStage: newStage }]
    } else if (actionType === 'CASE_QUEUE_UPDATE_ACTION') {
      actions = [...actions, { actionType: actionType, queueId: queueId }]
    }

    const newHook = {
      eventType,
      taskDefKey,
      processDefKey,
      actions,
    }
    onSubmit(newHook)
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth='sm'>
      <DialogTitle>
        {hookData ? 'Edit Listener' : 'Add New Listener'}
      </DialogTitle>
      <DialogContent>
        <Box sx={{ ...styles.dialogContent, paddingTop: '1rem' }}>
          <FormControl variant='outlined' sx={styles.formControl}>
            <InputLabel>Event Type</InputLabel>
            <Select
              value={eventType}
              onChange={(e) => setEventType(e.target.value)}
              label='Event Type'
              sx={styles.typeSelect}
            >
              <MenuItem value='TASK_COMPLETE_EVENT_TYPE'>
                Task Complete
              </MenuItem>
            </Select>
          </FormControl>
          {eventType === 'TASK_COMPLETE_EVENT_TYPE' && (
            <React.Fragment>
              <FormControl variant='outlined' sx={styles.formControl}>
                <InputLabel>Process Definition Key</InputLabel>
                <Select
                  label='Process Definition Key'
                  value={processDefKey}
                  onChange={(e) => setProcessDefKey(e.target.value)}
                  sx={styles.typeSelect}
                >
                  {processesDefinitions.map((processDefinition) => {
                    return (
                      <MenuItem
                        key={processDefinition.key}
                        value={processDefinition.key}
                      >
                        {processDefinition.name}
                      </MenuItem>
                    )
                  })}
                </Select>
              </FormControl>
              <TextField
                label='Task Definition Key'
                value={taskDefKey}
                onChange={(e) => setTaskDefKey(e.target.value)}
                variant='outlined'
                InputLabelProps={{ shrink: true }}
              />
            </React.Fragment>
          )}
          {eventType && (
            <FormControl variant='outlined' sx={styles.formControl}>
              <InputLabel>Action Type</InputLabel>
              <Select
                value={actionType}
                onChange={(e) => setActionType(e.target.value)}
                label='Action Type'
                sx={styles.typeSelect}
              >
                <MenuItem value='CASE_STAGE_UPDATE_ACTION'>
                  Progress Stage
                </MenuItem>
                <MenuItem value='CASE_QUEUE_UPDATE_ACTION'>
                  Assign Queue
                </MenuItem>
              </Select>
            </FormControl>
          )}
          {actionType === 'CASE_STAGE_UPDATE_ACTION' && (
            <FormControl variant='outlined' sx={styles.formControl}>
              <InputLabel>New Stage</InputLabel>
              <Select
                value={newStage}
                onChange={(e) => setNewStage(e.target.value)}
                label='New Stage'
                sx={styles.typeSelect}
              >
                {stages.map((stage) => {
                  return (
                    <MenuItem key={stage.index} value={stage.name}>
                      {stage.name}
                    </MenuItem>
                  )
                })}
              </Select>
            </FormControl>
          )}
          {actionType === 'CASE_QUEUE_UPDATE_ACTION' && (
            <FormControl variant='outlined' sx={styles.formControl}>
              <InputLabel>Queue</InputLabel>
              <Select
                value={queueId}
                onChange={(e) => setQueueId(e.target.value)}
                label='Queue'
                sx={styles.typeSelect}
              >
                {queues.map((queue) => {
                  return (
                    <MenuItem key={queue.id} value={queue.name}>
                      {queue.name}
                    </MenuItem>
                  )
                })}
              </Select>
            </FormControl>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} sx={styles.cancelButton}>
          Cancel
        </Button>
        <Button onClick={handleSubmit} sx={styles.saveButton}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  )
}
