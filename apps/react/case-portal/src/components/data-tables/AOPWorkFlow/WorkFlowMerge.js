import React, { useEffect, useState } from 'react'
import {
  Stepper,
  Step,
  StepLabel,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'
import AuditTrail from './AuditTrail'
import DataGridTable from '../ASDataGrid'
import { DataService } from 'services/DataService'
// import { CaseService } from 'services/CaseService'
// import { TaskService } from 'services/TaskService'
import { useSession } from 'SessionStoreContext'
import { remarkColumn } from 'components/Utilities/remarkColumn'
import Notification from 'components/Utilities/Notification'
import './jio-grid-style.css'
// import { usePlan } from 'menu/new-plan'
// import { useScreens } from 'menu/userscreen'
import { Box } from '../../../../node_modules/@mui/material/index'

const WorkFlowMerge = () => {
  const keycloak = useSession()
  // const [steps, setSteps] = useState([])
  const [activeStep, setActiveStep] = useState(0)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
  const [loading, setLoading] = useState(false)
  const [isCreatingCase, setIsCreatingCase] = useState(false)
  const [showCreateCasebutton, setShowCreateCasebutton] = useState(false)
  // const [isEdit, setIsEdit] = useState(false)

  // remark dialog state
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // audit trail state
  const [openAuditPopup, setOpenAuditPopup] = useState(false)
  const handleAuditOpen = () => setOpenAuditPopup(true)
  const handleAuditClose = () => setOpenAuditPopup(false)

  // reject flow state
  const [openRejectDialog, setOpenRejectDialog] = useState(false)
  const [actionDisabled, setActionDisabled] = useState(false)
  const [text, setText] = useState('')
  const [taskId, setTaskId] = useState('')

  // case + comment state
  const [businessKey, setBusinessKey] = useState('')
  const [masterSteps, setMasterSteps] = useState([])
  const [workflowDto, setWorkFlowDto] = useState({})
  const [status, setStatus] = useState('')
  const [caseId, setCaseId] = useState('')
  const [role, setRole] = useState('')
  // UI feedback
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [rowModesModel, setRowModesModel] = useState({})
  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const handleRemarkCellClick = async (row) => {
    try {
      const cases = await DataService.getCaseId(keycloak)
      console.log(cases?.workflowList?.length === 0)
      // console.log(isEdit)
      if (cases?.workflowList?.length !== 0) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
    } catch (err) {
      console.error('Error fetching case', err)
    }
  }
  // console.log(unsavedChangesRef.current, 'unsavedChangesRef')
  // console.log(rows)
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    console.log(newRow)
    console.log(oldRow)
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])
  const caseData = {
    caseDefinitionId: 'aopv5',
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
  // const screens = useScreens()
  // console.log(screens)
  // generate columns including remark column
  const generateColumns = (data) => {
    const cols = data.headers.map((header, i) => ({
      field: data.keys[i],
      headerName: header,
      minWidth: i === 0 ? 300 : 150,
      ...(i === 0 && { renderHeader: (p) => <div>{p.colDef.headerName}</div> }),
    }))
    const remarkIdx = cols.findIndex((col) => col.field === 'remark')
    if (remarkIdx !== -1) cols[remarkIdx] = remarkColumn(handleRemarkCellClick)
    return cols
  }

  // fetch workflow data for grid
  const fetchData = async () => {
    try {
      const data = await DataService.getWorkflowData(keycloak, plantId)
      const formatted = data.results.map((row, idx) => {
        const out = { id: idx }
        Object.entries(row).forEach(([k, v]) => {
          out[k] = !isNaN(v) && v !== '' ? Number(v).toFixed(2) : v
        })
        return out
      })
      // console.log(formatted)
      setRows(formatted)
      setColumns(generateColumns(data))
    } catch (err) {
      console.error('Error fetching grid', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  // fetch case, steps, and determine active step
  const getCaseId = async () => {
    try {
      const cases = await DataService.getCaseId(keycloak)
      setCaseId(cases?.workflowMasterDTO?.casedefId || '')
      // console.log(cases?.workflowList?.length === 0)
      setShowCreateCasebutton(cases?.workflowList?.length === 0)
      setTaskId(cases?.taskId || '')
      setStatus(cases?.status || '')
      setRole(cases?.role || '')
      // if (!cases?.taskId) setActionDisabled(true)
      setWorkFlowDto(cases?.workflowList[0])
      if(cases?.workflowList.length>0){
        // console.log('businessky in getcaseId ' + cases?.workflowList[0].caseId)
        setBusinessKey(cases?.workflowList[0].caseId)
      }
      

      // console.log(cases)
      const master = cases?.workflowMasterDTO

      setMasterSteps(master?.steps)
      // console.log(master?.steps, 'masterSteps')
      // auto-pick the in-progress or next step
      // setSteps(cases?.workflowMasterDTO?.steps.map((i) => i.displayName))

      const activeIdx = master.steps.findIndex((s) => s.status === 'inprogress')
      // console.log(activeIdx, 'activeIdx')
      setActiveStep(
        activeIdx > -1
          ? activeIdx
          : master.steps.findIndex((s) => s.status !== 'completed'),
      )

      // const tasks = await DataService.getTasksByBusinessKey(
      //   keycloak,
      //   cases[0].caseId,
      // )
      // if (!tasks?.length) return

      // who can act now?
      // const roles = keycloak.tokenParsed?.realm_access?.roles || []
      // const match = tasks.find((t) => roles.includes(t.assignee))
      // if (match) {
      //   setActionDisabled(false)
      //   setTaskId(match.id)
      // }
    } catch (err) {
      console.error('Error fetching case', err)
    } finally {
      setLoading(false)
    }
  }
  // useEffect(() => {
  //   if (showCreateCasebutton) {
  //     setIsEdit(true)
  //   } else {
  //     setIsEdit(false)
  //   }
  // }, [showCreateCasebutton])
  // console.log(activeStep, 'activeStep')
  // console.log(masterSteps, 'masterSteps')

  const createCase = async () => {
    // 1. Prevent double‐submit
    setIsCreatingCase(true)

    try {
      // 2. Create case + save workflow
      const payload = {
        caseInstance: {
          caseDefinitionId: caseId || caseData.caseDefinitionId,
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
        },
        workflowDTO: {
          year: localStorage.getItem('year'),
          plantFkId:
            JSON.parse(localStorage.getItem('selectedPlant'))?.id || '',
          caseDefId: caseId || caseData.caseDefinitionId,
          // caseId: result.businessKey,
          siteFKId: JSON.parse(localStorage.getItem('selectedSite'))?.id || '',
          verticalFKId: localStorage.getItem('verticalId'),
        },
        variables: caseData.attributes,
        // taskId: taskId,
      }
      const result = await DataService.submitWorkFlow(payload, keycloak)
      console.log(result)
      setSnackbarData({
        message: 'Workflow instance created successfully',
        severity: 'success',
      })
      setLoading(true)
      // setTimeout(() => {
      getCaseId()
      //   setLoading(false)
      // }, 4000)
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
      // setIsCreatingCase(false)
    }
  }

  useEffect(() => {
    fetchData()
    getCaseId()
  }, [plantId, year])

  // handle reject click
  const handleRejectClick = () => {
    setActionDisabled(true)
    setOpenRejectDialog(true)
  }
  const handleRejectCancel = () => {
    setActionDisabled(false)
    setOpenRejectDialog(false)
    setText('')
  }

  // complete task and post comment
  const handleSubmit = async () => {
    try {
      const comment = {
        body: text,
        parentId: '',
        userId: keycloak.tokenParsed.preferred_username,
        userName: keycloak.tokenParsed.given_name,
        caseId: businessKey,
        role: role,
        status: status,
      }
      const payloadOfCompleteTask = {
        taskId: taskId,
        CaseComment: comment,
        variables: caseData.attributes,
        workflowDTO: workflowDto,
      }
      await DataService.completeTask(keycloak, payloadOfCompleteTask)
      // await CaseService.addComment(keycloak, text, '', businessKey)
      setSnackbarData({
        message: 'Task completed and comment added!',
        severity: 'success',
      })
      setActionDisabled(true)
      getCaseId()
    } catch (err) {
      console.error('Error submitting', err)
      setSnackbarData({ message: err.message, severity: 'error' })
      setActionDisabled(false)
    } finally {
      setSnackbarOpen(true)
      setOpenRejectDialog(false)
      setText('')
    }
  }
  const defaultCustomHeight = { mainBox: '62vh', otherBox: '100%' }

  return (
    <Box>
    // style={{
    //   display: 'flex',
    //   flexDirection: 'column',
    //   gap: 5,
    //   marginTop: 20,
    // }}
      <Stepper activeStep={activeStep} alternativeLabel>
        {masterSteps?.map((step) => (
          <Step key={step.displayName} completed={step.status === 'completed'}>
            <StepLabel error={step.status === 'error'}>
              {step.displayName}
            </StepLabel>
          </Step>
        ))}
      </Stepper>

      <Stack
        direction='row'
        spacing={1}
        justifyContent='flex-end'
        sx={{ mt: 2 }}
      >
        {taskId && (
          <Button
            variant='contained'
            className='btn-save'
            onClick={handleRejectClick}
            disabled={actionDisabled}
          >
            Accept
          </Button>
        )}
        <Button
          variant='outlined'
          className='btn-save2'
          sx={{ color: '#0100cb', border: '1px solid ' }}
          onClick={handleAuditOpen}
          // disabled={actionDisabled}
        >
          Audit Trail
        </Button>
      </Stack>

      <Box
        sx={{
          height: '52vh',
          width: '100%',
          marginBottom: 0,
          padding: 0,
        }}
      >
        <DataGridTable
          rows={rows}
          setRows={setRows}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          columns={columns}
          loading={loading}
          processRowUpdate={processRowUpdate}
          remarkDialogOpen={remarkDialogOpen}
          unsavedChangesRef={unsavedChangesRef}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={setCurrentRowId}
          rowModesModel={rowModesModel}
          onRowModesModelChange={onRowModesModelChange}
          permissions={{ customHeight: defaultCustomHeight }}
        />
      </Box>
      {showCreateCasebutton && (
        <Button
          variant='contained'
          onClick={createCase}
          disabled={isCreatingCase || !showCreateCasebutton}
          className='btn-save'
          sx={{
            // backgroundColor: jioColors.primaryBlue,
            // color: jioColors.background,

            // borderRadius: 1,
            // padding: '8px 24px',
            // textTransform: 'none',
            // fontSize: '0.875rem',
            // fontWeight: 500,
            width: '200px',
            '&:hover': { backgroundColor: '#143B6F', boxShadow: 'none' },
          }}
        >
          {isCreatingCase ? 'Submitting…' : 'Submit for Approval'}
        </Button>
      )}

      {/* Reject Dialog (Comments) */}
      <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
        <DialogTitle>Provide remarks on the changes?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            label='Remark'
            type='text'
            fullWidth
            multiline
            rows={8}
            sx={{ width: '100%', minWidth: '600px' }}
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleRejectCancel}>Cancel</Button>
          <Button
            onClick={handleSubmit}
            variant='contained'
            disabled={!text.trim()}
          >
            Submit
          </Button>
        </DialogActions>
      </Dialog>

      {/* Audit Trail Dialog */}
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
          <Button onClick={handleAuditClose}>Close</Button>
        </DialogActions>
      </Dialog>

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default WorkFlowMerge
