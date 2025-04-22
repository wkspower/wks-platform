import { useEffect, useState } from 'react'

import { Stepper, Step, StepLabel } from '@mui/material'

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '../../../../node_modules/@mui/material/index'

import AuditTrail from './AuditTrail'
import './jio-grid-style.css'
import DataGridTable from '../ASDataGrid'
import { DataService } from 'services/DataService'
import Notification from 'components/Utilities/Notification'
import { CaseService } from 'services/CaseService'
import { TaskService } from 'services/TaskService'
import { useSession } from 'SessionStoreContext'

const jioColors = {
  primaryBlue: '#387ec3',
  // primaryBlue: 'red',
  accentRed: '#E31C3D',
  background: '#FFFFFF',
  headerBg: '#0F3CC9',
  rowEven: '#FFFFFF',
  rowOdd: '#E8F1FF',
  textPrimary: '#2D2D2D',
  border: '#D0D0D0',
  darkTransparentBlue: 'rgba(127, 147, 206, 0.8)',
}

// import DataGridTable from '../ASDataGrid'
const WorkFlowMerge = () => {
  const keycloak = useSession()
  const [activeStep, setActiveStep] = useState(0)
  const [openRejectDialog, setOpenRejectDialog] = useState(false)
  const [status, setStatus] = useState('')
  const [text, setText] = useState('')
  const [role, setRole] = useState('plant_manager')
  const [showTextBox, setShowTextBox] = useState(false)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
  const [showCreateCasebutton, setShowCreateCasebutton] = useState(false)
  const [taskId, setTaskId] = useState('')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  // const verticalId = localStorage.getItem('verticalId')
  // const siteName = JSON.parse(localStorage.getItem('selectedSite'))?.name
  const [businessKey, setBusinessKey] = useState('')
  const [isCreatingCase, setIsCreatingCase] = useState(false)
  const [actionDisabled, setActionDisabled] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const steps = [
    'Submit Plant AOP',
    'Validate Plant AOP',
    'Review Plant AOP',
    'Approve AOP',
    'Final Approval O2C AOP',
    // 'Closed',
  ]
  // const columns = [
  //   {
  //     field: 'particulars',
  //     headerName: 'Particulars',
  //     minWidth: 300,
  //     // custom header renderer
  //   },
  //   {
  //     field: 'UOM',
  //     headerName: 'UOM',
  //     minWidth: 100,
  //   },
  //   // grouped children can skip renderHeader:
  //   { field: 'firstYear', headerName: 'FY 2025-26 AOP', minWidth: 150 },
  //   { field: 'secondYear', headerName: 'FY 2025-26 Actual', minWidth: 150 },
  //   { field: 'thirdYear', headerName: 'FY 2026-27 AOP', minWidth: 150 },
  //   {
  //     field: 'remarks',
  //     headerName: 'Remarks',
  //     minWidth: 200,
  //   },
  // ]

  // const columnGroupingModel = [
  //   {
  //     groupId: 'annualAOP',
  //     headerName: 'Annual AOP Cost',
  //     children: [
  //       { field: 'fy202425AOP' },
  //       { field: 'fy202425Actual' },
  //       { field: 'fy202526AOP' },
  //     ],
  //   },
  // ]

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getWorkflowData(keycloak, plantId)
      // console.log(data)
      const formattedRows = data.results.map((row, id) => {
        // build a new row, formatting only the numeric fields
        const newRow = { id }
        Object.entries(row).forEach(([key, val]) => {
          // if val is a number string, format it
          if (!isNaN(val) && val !== '') {
            newRow[key] = Number(val).toFixed(2) // "9999.00" :contentReference[oaicite:0]{index=0}
          } else {
            newRow[key] = val
          }
        })
        return newRow
      })
      setRows(formattedRows)
      // setColumns(data?.headers)
      const generateColumns = ({ headers, keys }) => {
        return headers.map((header, idx) => ({
          field: keys[idx],
          headerName: header,
          minWidth: idx === 0 ? 300 : 150,
          // Optional: center headers except the first
          // align: idx === 0 ? 'left' : 'center',
          // headerAlign: idx === 0 ? 'left' : 'center',
          // Optional: custom header renderer for the first column
          ...(idx === 0 && {
            renderHeader: (params) => (
              <div
              // style={{
              //   display: 'flex',
              //   alignItems: 'center',
              //   justifyContent: 'center',
              //   width: '100%',
              //   height: '100%',
              // }}
              >
                {params.colDef.headerName}
              </div>
            ),
          }),
        }))
      }

      // 2. Use it in your component
      setColumns(generateColumns(data))
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setRows([]) // Clear rows on error
      setLoading(false) // Hide loading
    }
  }
  useEffect(() => {
    fetchData()
  }, [plantId])
  const defaultCustomHeight = { mainBox: '60vh', otherBox: '114%' }

  const handleRejectClick = () => {
    setActionDisabled(true)
    setOpenRejectDialog(true)
  }
  // const [audit, setAudit] = useState(true)
  const [openAuditPopup, setOpenAuditPopup] = useState(false)
  // const handleAudit = () => {
  //   setAudit((prev) => !prev)
  // }

  // Updated Audit handler: open popup dialog
  const handleAuditOpen = () => {
    setOpenAuditPopup(true)
  }

  const handleAuditClose = () => {
    setOpenAuditPopup(false)
  }

  //   const handleRejectSubmit = () => {
  //     // Perform rejection logic here (e.g., API call with rejectReason)
  //     setOpenRejectDialog(false)
  //     setRejectReason('')
  //   }

  const handleRejectCancel = () => {
    setActionDisabled(false)

    setOpenRejectDialog(false)
    // setRejectReason('')
    setText()
  }

  const caseData = {
    caseDefinitionId: 'aopv3',
    owner: {
      id: keycloak.subject || '',
      name: keycloak.idTokenParsed.name || '',
      email: keycloak.idTokenParsed.email || '',
      phone: keycloak.idTokenParsed.phone || '',
    },
    attributes: [
      { name: 'textField', value: '9', type: 'String' },
      { name: 'submit', value: false, type: 'String' },
      { name: 'submit1', value: false, type: 'String' },
    ],
  }

  useEffect(() => {
    // console.log('in the case id')
    // showCreateCasebutton = false;

    getCaseId()
  }, [])
  const getActivityPrefix = (activityId) => {
    return activityId?.split('-')[0] || ''
  }
  const getActivityStatus = async (activityId) => {
    try {
      // 1. Fetch existing cases
      const status = await TaskService.getActivityInstancesById(
        keycloak,
        activityId,
      )

      const prefix = getActivityPrefix(status[0]?.activityId)
      setStatus(prefix)
      const stepIndex = steps.findIndex((step) =>
        step.toLowerCase().includes(prefix.toLowerCase()),
      )
      if (stepIndex !== -1) {
        setActiveStep(stepIndex)
      }
      // if (!status?.length) {
      //   setShowCreateCasebutton(true)
      //   return
      // }
    } catch (error) {
      console.error('Error fetching case/tasks:', error)
    }
  }

  const getCaseId = async () => {
    try {
      // 1. Fetch existing cases
      const cases = await DataService.getCaseId(keycloak)
      // console.log(cases)
      if (!cases?.length) {
        setShowCreateCasebutton(true)
        return
      }

      // 2. Fetch tasks for the first case
      const { caseId } = cases[0]
      // console.log(caseId)
      setBusinessKey(caseId)
      const tasks = await DataService.getTasksByBusinessKey(keycloak, caseId)
      // console.log(tasks)
      getActivityStatus(tasks[0]?.processInstanceId)

      // 3. Grab realm roles (array of strings)
      const roles = keycloak.tokenParsed?.realm_access?.roles || []

      let matchingTask = tasks.find((task) => roles.includes(task.assignee))

      setRole(matchingTask?.assignee)

      // setTaskId(matchingTask.id)

      // console.log(matchingTask)
      if (matchingTask) {
        setShowTextBox(true)
        setTaskId(matchingTask.id)
        // setTimeout(() => {
        //   if (tasks[0].id) handleCompleteSubmit()
        // }, 2000)
      }
    } catch (error) {
      console.error('Error fetching case/tasks:', error)
    }
  }
  const createCase = async () => {
    // 1. Prevent double‐submit
    setIsCreatingCase(true)

    try {
      // 2. Create case + save workflow
      const result = await DataService.createCase(keycloak, caseData)
      await DataService.saveworkflow(
        {
          year: localStorage.getItem('year'),
          plantFkId:
            JSON.parse(localStorage.getItem('selectedPlant'))?.id || '',
          caseDefId: caseData.caseDefinitionId,
          caseId: result.businessKey,
          siteFKId: JSON.parse(localStorage.getItem('selectedSite'))?.id || '',
          verticalFKId: localStorage.getItem('verticalId'),
        },
        keycloak,
      )
      getCaseId()
      // setTimeout(() => {
      //   if (taskId) handleCompleteSubmit()
      // }, 2000)

      // 3. Success feedback
      setSnackbarData({
        message: 'Workflow instance created successfully',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error creating workflow:', error)
      setSnackbarData({
        message: error.message || 'Failed to create workflow',
        severity: 'error',
      })
      setIsCreatingCase(false)
    } finally {
      // 5. Show snackbar regardless
      setSnackbarOpen(true)
    }
  }

  // // 1. Complete Task (Submit)
  // const handleCompleteSubmit = async () => {
  //   try {
  //     const taskDone = await DataService.completeTask(
  //       keycloak,
  //       taskId,
  //       caseData.attributes,
  //     )

  //     if (!taskDone) throw new Error('Unexpected response completing task')

  //     setSnackbarData({
  //       message: 'Workflow instance created successfully',
  //       severity: 'success',
  //     })
  //   } catch (err) {
  //     console.error('Error completing task:', err)
  //     setSnackbarData({
  //       message: err.message || 'Failed to complete task',
  //       severity: 'error',
  //     })
  //   } finally {
  //     setSnackbarOpen(true)
  //     // you can reset or leave other state here
  //   }
  // }

  // // 2. Post Comment
  // const handleCommentSubmit = async () => {
  //   // 1. Validation
  //   if (!text.trim()) {
  //     setSnackbarData({
  //       message: 'Please enter a comment!',
  //       severity: 'warning',
  //     })
  //     setSnackbarOpen(true)
  //     return
  //   }

  //   try {
  //     // Fetch businessKey once (or reuse if stored)
  //     const cases = await DataService.getCaseId(keycloak)
  //     const businessKey = cases[0].caseId

  //     const commentPosted = await CaseService.addComment(
  //       keycloak,
  //       text,
  //       '', // parentId
  //       businessKey, // businessKey
  //       role,
  //       steps[activeStep], // status
  //     )
  //     if (!commentPosted) throw new Error('Failed to post comment')

  //     // setSnackbarData({
  //     //   message: 'Comment added successfully! 🎉',
  //     //   severity: 'success',
  //     // })
  //   } catch (err) {
  //     console.error('Error posting comment:', err)
  //     setSnackbarData({
  //       message: err.message || 'Failed to post comment',
  //       severity: 'error',
  //     })
  //   } finally {
  //     setSnackbarOpen(true)
  //     setText('')
  //   }
  // }
  // const handleSubmit = () => {
  //   // 1. Don’t submit empty text
  //   handleCompleteSubmit()
  //   handleCommentSubmit()
  // }
  const handleSubmit = async () => {
    // 1. Don’t submit empty text

    try {
      // 2. Complete the task (204 = success)
      // console.log(taskId)
      const taskDone = await DataService.completeTask(
        keycloak,
        taskId,
        caseData.attributes,
      )

      if (!taskDone) {
        throw new Error('Unexpected response completing task')
      }
      // console.log(taskDone)
      // if (
      //   (keycloak?.tokenParsed?.realm_access?.roles ?? []).includes(
      //     'plant_manager',
      //   )
      // ) {
      //   return
      // } else {
      // 3. Now post your comment
      // 1. Fetch existing cases
      const cases = await DataService.getCaseId(keycloak)
      // if (!cases?.length) {
      //   setShowCreateCasebutton(true)
      //   return
      // }
      if (
        (keycloak?.tokenParsed?.realm_access?.roles ?? []).includes(
          'plant_manager',
        )
      ) {
        // setShowCreateCasebutton(true)
        return
      }
      if (!text.trim()) {
        setSnackbarData({
          message: 'Please enter a message!',
          severity: 'warning',
        })
        setSnackbarOpen(true)
        return
      }
      // 2. Fetch tasks for the first case
      const { caseId } = cases[0]
      //    parentId can be null or a comment id if you're replying
      console.log(caseId)
      let businessKey = caseId
      const commentPosted = await CaseService.addComment(
        keycloak,
        text,
        '', // parentId, // e.g. null or some existing comment ID
        businessKey, // your business key
        role,
        status || steps[activeStep], // status
      )

      if (!commentPosted) {
        throw new Error('Failed to post comment')
      }

      // 4. Both succeeded!
      setSnackbarData({
        message: 'Task completed and comment added! 🎉',
        severity: 'success',
      })
      // }
    } catch (err) {
      console.error('Error submitting:', err)
      setSnackbarData({
        message: err.message || 'Something went wrong!',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
      setOpenRejectDialog(false)
      setText('')
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
        marginTop: '20px',
      }}
    >
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((label, index) => (
          <Step key={label} onClick={() => setActiveStep(index)}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>{' '}
      <Stack
        direction='row'
        spacing={1}
        justifyContent='right'
        sx={{ mt: 2, mb: 0 }}
      >
        {showTextBox && (
          <>
            <Button
              variant='contained'
              color='primary'
              sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
              onClick={handleRejectClick}
              disabled={actionDisabled}
            >
              Accept
            </Button>
            <Button
              variant='outlined'
              color='secondary'
              sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
              onClick={handleRejectClick}
              disabled={actionDisabled}
            >
              Reject
            </Button>
          </>
        )}
        {!(keycloak?.tokenParsed?.realm_access?.roles ?? []).includes(
          'plant_manager',
        ) && (
          <Button
            variant='outlined'
            color='primary'
            sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
            onClick={handleAuditOpen}
          >
            Audit Trail
          </Button>
        )}
      </Stack>
      <DataGridTable
        columns={columns}
        rows={rows}
        loading={loading}
        setRows={setRows}
        // columnGroupingModel={columnGroupingModel}
        className='jio-data-grid'
        permissions={{
          customHeight: defaultCustomHeight,
        }}
      />
        <Button
          variant='contained'
          color='primary'
          onClick={createCase}
          disabled={!showCreateCasebutton || isCreatingCase}
          sx={{
            backgroundColor: jioColors.primaryBlue,
            color: jioColors.background,
            borderRadius: 1,
            padding: '8px 24px',
            textTransform: 'none',
            fontSize: '0.875rem',
            fontWeight: 500,
            width: '200px',
            '&:hover': { backgroundColor: '#143B6F', boxShadow: 'none' },
          }}
        >
          {isCreatingCase ? 'Submitting…' : 'Submit for Approval'}
        </Button>
      {/* <Button
        variant='contained'
        color='primary'
        onClick={createCase}
        // sx={{ mt: 2, width: '200px' }}
        sx={{
          // marginTop: 2,
          backgroundColor: jioColors.primaryBlue,
          color: jioColors.background,
          borderRadius: 1,
          padding: '8px 24px',
          textTransform: 'none',
          fontSize: '0.875rem',
          fontWeight: 500,
          minWidth: '200px',
          width: '200px',
          '&:hover': {
            backgroundColor: '#143B6F',
            boxShadow: 'none',
          },
          // '&.Mui-disabled': {
          //   backgroundColor: jioColors.primaryBlue,
          //   color: jioColors.background,
          //   opacity: 0.7,
          // },
        }}
        disabled={!showCreateCasebutton}
      >
        Submit for Approval
      </Button> */}
      {/* Reject Dialog */}
      <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
        <DialogTitle>Please provide remarks on the changes?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            label='Remark'
            type='text'
            fullWidth
            sx={{ width: '100%', minWidth: '600px' }}
            multiline
            rows={8}
            value={text}
            // value={rejectReason}
            onChange={(e) => setText(e.target.value)}
            // onChange={(e) => setRejectReason(e.target.value)}
            variant='outlined'
          />
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'flex-end' }}>
          <Button onClick={handleRejectCancel} color='primary'>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            color='primary'
            variant='contained'
            disabled={!text?.trim()}
            // disabled={!rejectReason?.trim()}
          >
            Submit
          </Button>
        </DialogActions>
      </Dialog>
      {/* Audit Trail Popup Dialog */}
      <Dialog
        open={openAuditPopup}
        onClose={handleAuditClose}
        maxWidth='lg'
        fullWidth
      >
        <DialogTitle>Audit Trail</DialogTitle>
        <DialogContent dividers>
          <AuditTrail keycloak={keycloak} businessKey={businessKey} />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAuditClose} color='primary'>
            Close
          </Button>
        </DialogActions>
      </Dialog>
      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
    </div>
  )
}

export default WorkFlowMerge
