import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box, Step, StepLabel, Stepper } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { CaseService } from 'services/CaseService'
import { DataService } from 'services/DataService'
import { TaskService } from 'services/TaskService'
import { useSession } from 'SessionStoreContext'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Tab,
  Tabs,
  TextField,
} from '../../../../node_modules/@mui/material/index'
import AuditTrail from './AuditTrail'
import DataGridTable from '../ASDataGrid'
// import '../data-tables/data-grid-css.css'
// import { CaseService } from 'services/CaseService'
// import { TaskService } from 'services/TaskService'
// import { useSession } from 'SessionStoreContext'
import { remarkColumn } from 'components/Utilities/remarkColumn'
// import Notification from 'components/Utilities/Notification'
import './jio-grid-style.css'
// import { usePlan } from 'menu/new-plan'
// import { useScreens } from 'menu/userscreen'
// import { Box } from '../../../../node_modules/@mui/material/index'
import ProductionAopView from 'components/data-tables-views/DataTable-production-aop'
import PlantsProductionSummary from '../Reports/PlantsProductionData'
import MonthwiseProduction from '../Reports/MonthwiseProduction'
import MonthwiseRawMaterial from '../Reports/MonthwiseRawMaterial'
import TurnaroundReport from '../Reports/TurnaroundReport'
import AnnualProductionPlan from '../Reports/AnnualProductionPlan'
const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))
const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

const WorkFlowMerge = () => {
  const keycloak = useSession()
  // const [steps, setSteps] = useState([])
  const [activeStep, setActiveStep] = useState(0)
  // const [openRejectDialog, setOpenRejectDialog] = useState(false)
  // const [status, setStatus] = useState('')
  // const [text, setText] = useState('')
  // const [role, setRole] = useState('plant_manager')
  // const [showTextBox, setShowTextBox] = useState(false)
  // const [loading, setLoading] = useState(false)
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
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
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
  useEffect(() => {
    fetchData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])
  const handleCalculate = () => {
    if (lowerVertName == 'meg') {
      handleCalculateMeg()
    } else {
      // handleCalculatePe()
    }
  }
  // const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const handleCalculateMeg = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculateProductionVolData2(
        plantId,
        year,
        keycloak,
      )

      if (data || data == 0) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    }
  }

  const handleRemarkCellClick = async (row) => {
    try {
      const cases = await DataService.getCaseId(keycloak)
      // setCaseId(cases?.workflowMasterDTO?.casedefId || '')
      // console.log(isEdit)
      // console.log(showCreateCasebutton)
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
      var formatted = data.results.map((row, idx) => {
        const out = { id: idx }
        Object.entries(row).forEach(([k, v]) => {
          out[k] = !isNaN(v) && v !== '' ? Number(v).toFixed(2) : v
        })
        return out
      })
      // console.log(formatted)

      formatted = formatted?.map((item) => ({
        ...item,
        isEditable: false,
      }))

      setRows(formatted)
      setColumns(generateColumns(data))
    } catch (err) {
      console.error('Error fetching grid', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }
  // console.log(columns, 'columns')
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
      if (cases?.workflowList.length > 0) {
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
  // console.log(rows)

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
        // allData: rows,
        workflowYearDTO: rows,
      }
      const result = await DataService.submitWorkFlow(payload, keycloak)
      console.log(result)
      setSnackbarData({
        message: 'Workflow instance created successfully',
        severity: 'success',
      })
      setLoading(true)
      getCaseId()
      fetchData()
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
    // fetchData()
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
  const defaultCustomHeight = { mainBox: '43vh', otherBox: '118%' }
  const [tabIndex, setTabIndex] = useState(0)

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
        marginTop: '0px',
      }}
    >
      <Box>
        <Stepper activeStep={activeStep} alternativeLabel>
          {masterSteps?.map((step) => (
            <Step
              key={step.displayName}
              completed={step.status === 'completed'}
            >
              <StepLabel
                error={step.status === 'error'}
                StepIconProps={{
                  sx: {
                    color: step.status === 'completed' ? '#0100cb' : 'grey',
                  },
                }}
              >
                {' '}
                {step.displayName}
              </StepLabel>
            </Step>
          ))}
        </Stepper>

        <Stack
          direction='row'
          alignItems='center'
          justifyContent='space-between' // push children to extremes
          sx={{ mt: 0, mb: '-5px' }}
        >
          {/* LEFT: Tabs */}
          <Tabs
            value={tabIndex}
            onChange={(e, newIndex) => setTabIndex(newIndex)}
            sx={{
              borderBottom: 0,
              '.MuiTabs-indicator': { display: 'none' },
              // you can tweak px/margin if you need more breathing room
            }}
            textColor='primary'
            indicatorColor='primary'
          >
            {[
              'Annual AOP Cost',
              'Plant Production Summary',
              'Month Wise Production Plan',
              'Month Wise Raw Data',
              'Turnaround Report',
              'Annual Production Plan'
            ].map((label, idx) => (
              <Tab
                key={idx}
                label={label}
                sx={{
                  border: tabIndex === idx ? '1px solid' : 'none',
                  borderBottom: '1px solid',
                  mr: 1, // small gap between tabs
                  minWidth: '150px', // optional for consistent width
                }}
              />
            ))}
          </Tabs>

          {/* RIGHT: Buttons */}
          <Stack direction='row' spacing={1}>
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
              sx={{ color: '#0100cb', border: '1px solid' }}
              onClick={handleAuditOpen}
            >
              Audit Trail
            </Button>
          </Stack>
        </Stack>

        {tabIndex === 0 && (
          <div>
            <div>
              <CustomAccordion defaultExpanded disableGutters>
                <CustomAccordionSummary
                  aria-controls='meg-grid-content'
                  id='meg-grid-header'
                >
                  <Typography component='span' className='grid-title'>
                    Production Data
                  </Typography>
                </CustomAccordionSummary>
                <CustomAccordionDetails>
                  <Box>
                    <ProductionAopView />
                  </Box>
                </CustomAccordionDetails>
              </CustomAccordion>
            </div>
            <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
              Annual AOP Cost
            </Typography>
            <div style={{ minHeight: 'fit-content', maxHeight: 'max-content' }}>
              <DataGridTable
                rows={rows}
                setRows={setRows}
                onRowUpdate={(updatedRow) =>
                  console.log('Row Updated:', updatedRow)
                }
                columns={columns}
                className='jio-data-grid'
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
                handleCalculate={handleCalculate}
                permissions={{
                  customHeight: defaultCustomHeight,
                  // saveBtn: true,
                  showCalculate: true,
                  remarksEditable: true,
                  // approveBtn: false,
                }}
              />
            </div>

            {showCreateCasebutton && (
              <Button
                variant='contained'
                onClick={createCase}
                disabled={isCreatingCase || !showCreateCasebutton}
                className='btn-save'
              >
                {isCreatingCase ? 'Submitting…' : 'Submit'}
              </Button>
            )}

            {/* Reject Dialog (Comments) */}
            <Dialog open={openRejectDialog} onClose={handleRejectCancel}>
              <DialogTitle>Please provide remarks on the changes?</DialogTitle>
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
          </div>
        )}

        {tabIndex === 1 && <PlantsProductionSummary />}

        {tabIndex === 2 && <MonthwiseProduction />}
        {tabIndex === 3 && <MonthwiseRawMaterial />}
        {tabIndex === 4 && <TurnaroundReport />}
           {tabIndex === 5&& <AnnualProductionPlan />}
      </Box>
    </div>
  )
}

export default WorkFlowMerge
