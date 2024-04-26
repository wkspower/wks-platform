import React, { useEffect, useState } from 'react'
import DeleteIcon from '@mui/icons-material/Delete'
import Checkbox from '@mui/material/Checkbox'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import Button from '@mui/material/Button'
import ListItem from '@mui/material/ListItem'
import ListItemSecondaryAction from '@mui/material/ListItemSecondaryAction'
import ListItemText from '@mui/material/ListItemText'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import { ProcessDefService } from 'services/ProcessDefService'
import { useSession } from 'SessionStoreContext'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'

export const CaseDefStageProcesses = ({
  open,
  handleClose,
  stage,
  updateProcesses,
}) => {
  const keycloak = useSession()
  const [processes, setProcesses] = React.useState(
    stage.processesDefinitions || [],
  )
  const [newProcess, setNewProcess] = React.useState({
    definitionKey: '',
    autoStart: false,
  })

  const [processesDefinitions, setProcessesDefinitions] = useState()

  useEffect(() => {
    ProcessDefService.find(keycloak)
      .then((data) => {
        setProcessesDefinitions(data)
      })
      .catch((err) => {
        setProcessesDefinitions(null)
        console.log(err.message)
      })
  }, [open])

  const handleAddProcess = () => {
    if (newProcess.definitionKey) {
      setProcesses([...processes, newProcess])
      setNewProcess({ definitionKey: '', autoStart: false })
    }
  }

  const handleDeleteProcess = (index) => {
    const updatedProcesses = [...processes]
    updatedProcesses.splice(index, 1)
    setProcesses(updatedProcesses)
  }

  const handleSave = () => {
    updateProcesses(stage.id, processes)
    handleClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth>
      <DialogTitle>Processes for {stage.name}</DialogTitle>
      <DialogContent>
        <List>
          {processes.map((process, index) => (
            <ListItem key={index}>
              <ListItemText
                primary={`Definition Key: ${process.definitionKey}`}
                secondary={`Auto Start: ${process.autoStart ? 'Yes' : 'No'}`}
              />
              <ListItemSecondaryAction>
                <IconButton
                  edge='end'
                  onClick={() => handleDeleteProcess(index)}
                  title='Delete Process'
                >
                  <DeleteIcon />
                </IconButton>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
        <div style={{ marginTop: '20px' }}>
          {processesDefinitions && (
            <FormControl
              key='ctrlStagesLCProcess'
              variant='outlined'
              sx={{ mt: 3 }}
              fullWidth
            >
              <InputLabel id='processDefinitionlId'>
                Process Definition
              </InputLabel>
              <Select
                labelId='processDefinitionId'
                label='Process Definition'
                id='selectProcessDefinition'
                value={newProcess.definitionKey}
                onChange={(e) =>
                  setNewProcess({
                    ...newProcess,
                    definitionKey: e.target.value,
                  })
                }
              >
                <MenuItem key='processDefEmptyOptionKey' value='null'>
                  &nbsp;
                </MenuItem>
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
          )}
          <div
            style={{ display: 'flex', alignItems: 'center', marginTop: '10px' }}
          >
            <Checkbox
              checked={newProcess.autoStart}
              onChange={(e) =>
                setNewProcess({ ...newProcess, autoStart: e.target.checked })
              }
              color='primary'
            />
            <span>Auto Start</span>
          </div>
          <Button
            variant='contained'
            color='primary'
            style={{ marginTop: '10px' }}
            onClick={handleAddProcess}
          >
            Add Process
          </Button>
        </div>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={handleClose}>
          Cancel
        </Button>
        <Button variant='contained' color='primary' onClick={handleSave}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  )
}
