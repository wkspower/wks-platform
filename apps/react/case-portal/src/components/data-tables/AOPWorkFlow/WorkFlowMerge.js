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
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import { CaseService } from 'services/CaseService'

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
  const [activeStep, setActiveStep] = useState(0)
  const [openRejectDialog, setOpenRejectDialog] = useState(false)
  //   const [rejectReason, setRejectReason] = useState('')
  const [text, setText] = useState('')
  const keycloak = useSession()
  const [showTextBox, setShowTextBox] = useState(false)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
  const [showCreateCasebutton, setShowCreateCasebutton] = useState(false)
  const [taskId, setTaskId] = useState('')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  // const verticalId = localStorage.getItem('verticalId')
  // const siteName = JSON.parse(localStorage.getItem('selectedSite'))?.name
  // const [businessKey, setBusinessKey] = useState('')
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

  const columnGroupingModel = [
    {
      groupId: 'annualAOP',
      headerName: 'Annual AOP Cost',
      children: [
        { field: 'aop_2025_26' },
        { field: 'actual_2025_26' },
        { field: 'aop_2026_27' },
      ],
    },
  ]

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getWorkflowData(keycloak, plantId)
      console.log(data)
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
          align: idx === 0 ? 'left' : 'center',
          headerAlign: idx === 0 ? 'left' : 'center',
          // Optional: custom header renderer for the first column
          ...(idx === 0 && {
            renderHeader: (params) => (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '100%',
                  height: '100%',
                }}
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
      setLoading(false) // Hide loading
    }
  }
  useEffect(() => {
    fetchData()
  }, [])
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
    console.log('in the case id')
    // showCreateCasebutton = false;

    getCaseId()
  }, [])

  const getCaseId = async () => {
    try {
      // 1. Fetch existing cases
      const cases = await DataService.getCaseId(keycloak)
      console.log(cases)
      if (!cases?.length) {
        setShowCreateCasebutton(true)
        return
      }

      // 2. Fetch tasks for the first case
      const { caseId } = cases[0]
      const tasks = await DataService.getTasksByBusinessKey(keycloak, caseId)
      //   setBusinessKey(caseId)

      // 3. Grab realm roles (array of strings)
      const roles = keycloak.tokenParsed?.realm_access?.roles || []
      console.log('User roles:', roles)

      // 4. Find first task whose assignee matches any role
      let matchingTask = tasks.find((task) => roles.includes(task.assignee))
      // 2. If none, try matching by name
      // if (!matchingTask) {
      //   const name = 'PC and I head'
      //   matchingTask = tasks.find((task) => name.includes(task.name))
      // }

      console.log(matchingTask)
      if (matchingTask) {
        setShowTextBox(true)
        setTaskId(matchingTask.id)
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
      // 4. Re‑enable button on error
      setIsCreatingCase(false)
    } finally {
      // 5. Show snackbar regardless
      setSnackbarOpen(true)
    }
  }

  // const createCase = async () => {
  //   try {
  //     const result = await DataService.createCase(keycloak, caseData)
  //     // console.log('Response:', result)
  //     //   setBusinessKey(result?.businessKey)
  //     var year = localStorage.getItem('year')
  //     var plantId = ''
  //     var siteId = ''
  //     const storedPlant = localStorage.getItem('selectedPlant')
  //     if (storedPlant) {
  //       const parsedPlant = JSON.parse(storedPlant)
  //       plantId = parsedPlant.id
  //     }

  //     const storedSite = localStorage.getItem('selectedSite')
  //     if (storedSite) {
  //       const parsedSite = JSON.parse(storedSite)
  //       siteId = parsedSite.id
  //     }

  //     const verticalId = localStorage.getItem('verticalId')

  //     let workflowData = {
  //       year: year,
  //       plantFkId: plantId,
  //       caseDefId: caseData.caseDefinitionId,
  //       caseId: result.businessKey,
  //       siteFKId: siteId,
  //       verticalFKId: verticalId,
  //     }

  //     const workFlowResult = await DataService.saveworkflow(
  //       workflowData,
  //       keycloak,
  //     )
  //     console.log(workFlowResult)
  //     getCaseId()
  //     // alert('Submitted successfully!')
  //   } catch (error) {
  //     console.error('Error submitting:', error)
  //     // alert('Something went wrong!')
  //   }
  // }

  const handleSubmit = async () => {
    // 1. Don’t submit empty text
    if (!text.trim()) {
      setSnackbarData({
        message: 'Please enter a message!',
        severity: 'warning',
      })
      setSnackbarOpen(true)
      return
    }

    try {
      // 2. Complete the task (204 = success)
      const taskDone = await DataService.completeTask(
        keycloak,
        taskId,
        caseData.attributes,
      )

      if (!taskDone) {
        throw new Error('Unexpected response completing task')
      }
      console.log(taskDone)
      // 3. Now post your comment
      // 1. Fetch existing cases
      const cases = await DataService.getCaseId(keycloak)
      // if (!cases?.length) {
      //   setShowCreateCasebutton(true)
      //   return
      // }

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
      )

      if (!commentPosted) {
        throw new Error('Failed to post comment')
      }

      // 4. Both succeeded!
      setSnackbarData({
        message: 'Task completed and comment added! 🎉',
        severity: 'success',
      })
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
        sx={{ mt: 2, mb: 1 }}
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
        <Button
          variant='outlined'
          color='primary'
          sx={{ fontWeight: 'bold', textTransform: 'none', px: 3 }}
          onClick={handleAuditOpen}
        >
          Audit Trail
        </Button>
      </Stack>
      <DataGridTable
        columns={columns}
        rows={rows}
        loading={loading}
        setRows={setRows}
        columnGroupingModel={columnGroupingModel}
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
          <AuditTrail />
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
