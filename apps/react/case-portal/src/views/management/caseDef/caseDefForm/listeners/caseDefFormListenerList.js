import {
  AddCircleOutline,
  AssignmentTurnedIn,
  DeleteOutline,
  EditOutlined,
} from '@mui/icons-material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Grid,
  IconButton,
  Typography,
} from '@mui/material'
import MainCard from 'components/MainCard'
import React, { useEffect, useState } from 'react'
import { CaseDefFormEventsForm } from './caseDefFormListenerForm'

export const CaseDefFormEvents = ({ caseDef, setCaseDef }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [hooks, setHooks] = useState([])
  const [selectedHookIndex, setSelectedHookIndex] = useState(null)

  const [isHookFormOpen, setIsHookFormOpen] = useState(false)

  const addButtonStyle = {
    position: 'fixed',
    bottom: '20px',
    right: '20px',
    zIndex: 1000,
    boxShadow: '0px 2px 5px rgba(0, 0, 0, 0.2)',
    borderRadius: '50%',
    background: '#fff',
  }

  useEffect(() => {
    setIsLoading(true)
    setHooks(caseDef.caseHooks)
    setIsLoading(false)
  }, [caseDef.caseHooks])

  const handleAddHook = (event) => {
    setCaseDef({ ...caseDef, caseHooks: [...caseDef.caseHooks, event] })
    closeHookForm()
  }

  const handleReplaceHook = (event) => {
    let hooksCopy = [...caseDef.caseHooks]
    hooksCopy[selectedHookIndex] = event
    setCaseDef({ ...caseDef, caseHooks: hooksCopy })
    closeHookForm()
  }

  const handleEditHook = (hookIndex) => {
    setSelectedHookIndex(hookIndex)
    setIsHookFormOpen(true)
  }

  const handleRemoveHook = (hookIndex) => {
    setCaseDef({
      ...caseDef,
      caseHooks: caseDef.caseHooks.filter(
        (element, index) => index !== hookIndex,
      ),
    })
    closeHookForm()
  }

  const formatActionList = (actions) => {
    if (!actions.length) return ''

    const actionList = actions.map((action) => {
      if (action.actionType === 'CASE_STAGE_UPDATE_ACTION') {
        return `Progress Case Stage to ${action.newStage}`
      } else if (action.actionType === 'CASE_QUEUE_UPDATE_ACTION') {
        return `Update Case Queue to ${action.queueId}`
      } else {
        return ''
      }
    })

    return actionList.join(' AND ')
  }

  const openHookForm = () => {
    setSelectedHookIndex(null) // Reset the selected hook index when opening the form for adding
    setIsHookFormOpen(true)
  }

  const closeHookForm = () => {
    setIsHookFormOpen(false)
  }

  return (
    <Grid rowSpacing={4.5} columnSpacing={2.75}>
      <Grid item xs={12} md={7} lg={10}>
        <MainCard sx={{ mt: 2 }} content={false}>
          {hooks ? (
            <Box>
              {isLoading ? (
                <Box
                  display='flex'
                  alignItems='center'
                  justifyContent='center'
                  height={650}
                >
                  <CircularProgress />
                </Box>
              ) : hooks.length ? (
                <div style={{ height: 650, width: '100%' }}>
                  <Grid container spacing={2} padding={2}>
                    {hooks.map((hook, index) => (
                      <Grid key={index} item xs={12} md={6} lg={4}>
                        <Card>
                          <CardContent>
                            {/* Displaying the "when-then" flow information */}
                            <Typography variant='h6' gutterBottom>
                              When task <strong>{hook.taskDefKey}</strong> in
                              process <strong>{hook.processDefKey}</strong> is
                              completed
                            </Typography>
                          </CardContent>
                          <Accordion defaultExpanded={true}>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                              <Box
                                component={AssignmentTurnedIn}
                                color='green'
                              />
                              <Typography variant='subtitle1'>Then</Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                              <Typography>
                                {formatActionList(hook.actions)
                                  .split(' AND ')
                                  .map((action, index) => (
                                    <React.Fragment key={index}>
                                      {index > 0 && (
                                        <strong>
                                          {' '}
                                          <br />
                                          AND{' '}
                                        </strong>
                                      )}
                                      {action}
                                    </React.Fragment>
                                  ))}
                              </Typography>
                            </AccordionDetails>
                          </Accordion>
                          <CardActions>
                            <IconButton
                              color='primary'
                              size='small'
                              onClick={() => handleEditHook(index)}
                            >
                              <EditOutlined />
                            </IconButton>
                            <IconButton
                              color='secondary'
                              size='small'
                              onClick={() => handleRemoveHook(index)}
                            >
                              <DeleteOutline />
                            </IconButton>
                          </CardActions>
                        </Card>
                      </Grid>
                    ))}
                  </Grid>
                </div>
              ) : (
                <Typography variant='body1'>
                  No listeners found for the selected action type.
                </Typography>
              )}
            </Box>
          ) : (
            <Typography variant='body1'>
              Select an action type to view associated listeners.
            </Typography>
          )}
        </MainCard>
      </Grid>

      <CaseDefFormEventsForm
        open={isHookFormOpen}
        onClose={closeHookForm}
        onSubmit={
          selectedHookIndex !== null ? handleReplaceHook : handleAddHook
        }
        hookData={selectedHookIndex !== null ? hooks[selectedHookIndex] : null} // Pass the selected hook data for editing
        stages={caseDef.stages}
      />

      <IconButton
        color='primary'
        size='large'
        onClick={openHookForm}
        style={addButtonStyle}
      >
        <AddCircleOutline fontSize='large' />
      </IconButton>
    </Grid>
  )
}
