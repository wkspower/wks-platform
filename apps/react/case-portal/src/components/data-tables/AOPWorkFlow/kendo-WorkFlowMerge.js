import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box, Step, StepLabel, Stepper } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
// import { CaseService } from 'services/CaseService'
import { DataService } from 'services/DataService'
// import { TaskService } from 'services/TaskService'
import { useSession } from 'SessionStoreContext'
import postmanData from '../../../assets/postmandata.json'

import {
  Button,
  Stack,
  Tab,
  Tabs,
} from '../../../../node_modules/@mui/material/index'
// import '../data-tables/data-grid-css.css'
// import { CaseService } from 'services/CaseService'
// import { TaskService } from 'services/TaskService'
// import { useSession } from 'SessionStoreContext'
import { remarkColumn } from 'components/Utilities/remarkColumn'

import './jio-grid-style.css'

import ProductionAopView from 'components/data-tables-views/kendo-DataTable-production-aop'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import PlantsProductionSummary from '../Reports-kendo/kendo-PlantsProductionData'
import MonthwiseProduction from '../Reports-kendo/kendo-MonthwiseProduction'
import MonthwiseRawMaterial from '../Reports-kendo/kendo-MonthwiseRawMaterial'
import TurnaroundReport from '../Reports-kendo/kendo-TurnaroundReport'
import AnnualProductionPlan from '../Reports-kendo/AnnualProductionPlan'
import PlantContribution from '../Reports-kendo/kendo-PlantContribution'
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
  const [modifiedCells, setModifiedCells] = React.useState({})

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
  const handleExport = () => {
    handleExportAll()
  }
  // const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const handleCalculateMeg = async () => {
    try {
      setLoading(true)

      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      let plantId = null

      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      if (!plantId || !year) {
        throw new Error('Plant ID or year not found in localStorage')
      }

      // Wait for all API calls to complete
      const [data, res1, res2, res3, res4, res5, res6] = await Promise.all([
        DataService.handleCalculateProductionVolData2(plantId, year, keycloak),
        DataService.handleCalculatePlantProductionData(plantId, year, keycloak),
        DataService.handleCalculateMonthwiseProduction(plantId, year, keycloak),
        DataService.calculateTurnAroundPlanReportData(plantId, year, keycloak),
        DataService.calculateAnnualProductionPlanData(plantId, year, keycloak),
        DataService.handleCalculatePlantConsumptionData(
          plantId,
          year,
          keycloak,
        ),
        DataService.calculatePlantContributionReportData(
          plantId,
          year,
          keycloak,
        ),
      ])

      const allSuccess = [data, res1, res2, res3, res4, res5, res6].every(
        (res) => res !== null && res !== undefined,
      )

      if (allSuccess) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Failed!',
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
    } finally {
      setLoading(false)
    }
  }

  const handleExportAll = async () => {
    try {
      setLoading(true)

      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      let plantId = null

      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      if (!plantId || !year) {
        throw new Error('Plant ID or year not found in localStorage')
      }

      const payload = postmanData

      // Await the API call here to ensure completion
      const data = await DataService.getExcel(keycloak, payload)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Report downloaded successfully!',
        severity: 'success',
      })

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = async (row) => {
    // do not delete commented code
    // try {
    //   const cases = await DataService.getCaseId(keycloak)
    //   console.log(cases?.workflowList?.length)
    //   if (cases?.workflowList?.length !== 0) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
    // } catch (err) {
    //   console.error('Error fetching case', err)
    // }
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

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
  const generateColumns = (data, numericKeys) => {
    const cols = data.headers.map((header, i) => {
      const key = data.keys[i]
      return {
        field: key,
        headerName: header,
        // minWidth: i === 0 ? 300 : 150,
        flex: 1,
        ...(i === 0 && {
          renderHeader: (p) => <div>{p.colDef.headerName}</div>,
        }),
        ...(numericKeys.includes(key) && { align: 'right' }),
      }
    })

    const remarkIdx = cols.findIndex((col) => col.field === 'remark')
    if (remarkIdx !== -1) cols[remarkIdx] = remarkColumn(handleRemarkCellClick)

    return cols
  }

  function getNumericKeysInAllRows(data) {
    if (!Array.isArray(data) || data.length === 0) return []

    const keys = Object.keys(data[0])

    return keys.filter((key) =>
      data.every((row) => {
        const value = row[key]
        // The column is considered numeric if:
        // - It's a valid number (including empty values)
        return value === '' || !isNaN(Number(value))
      }),
    )
  }

  const fetchData = async () => {
    try {
      const data = await DataService.getWorkflowData(keycloak, plantId)
      const numericKeys = getNumericKeysInAllRows(data?.results)
      let formatted = data.results.map((row, idx) => {
        const out = { id: idx }
        Object.entries(row).forEach(([k, v]) => {
          out[k] = !isNaN(v) && v !== '' ? Number(v).toFixed(2) : v
        })
        return out
      })

      formatted = formatted.map((item) => ({
        ...item,
      }))

      setRows(formatted)
      setColumns(generateColumns(data, numericKeys)) // pass numericKeys
    } catch (err) {
      console.error('Error fetching grid', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  const getCaseId = async () => {
    try {
      const cases = await DataService.getCaseId(keycloak)
      setCaseId(cases?.workflowMasterDTO?.casedefId || '')
      setShowCreateCasebutton(cases?.workflowList?.length === 0)
      setTaskId(cases?.taskId || '')
      setStatus(cases?.status || '')
      setRole(cases?.role || '')
      setWorkFlowDto(cases?.workflowList[0])
      if (cases?.workflowList.length > 0) {
        setBusinessKey(cases?.workflowList[0].caseId)
      }
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
      // console.log(result)
      if (result) {
        console.log('Workflow instance created successfully')
      }
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
  const saveChanges = async () => {
    try {
      // console.log(rows, 'workflowDto')
      await DataService.saveAnnualWorkFlowData(keycloak, rows, plantId)
      setSnackbarData({
        message: 'Data Saved Successfully!',
        severity: 'success',
      })
      setActionDisabled(true)
      // getCaseId()
    } catch (err) {
      console.error('Error while save', err)
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
            variant='scrollable'
            scrollButtons='auto'
            sx={{
              borderBottom: 0,
              '.MuiTabs-indicator': { display: 'none' },
              maxWidth: '100%',
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
              'Annual Production Plan',
              'Plant Contribution',
            ].map((label, idx) => (
              <Tab
                key={idx}
                label={label}
                sx={{
                  border: tabIndex === idx ? '1px solid' : 'none',
                  borderBottom: '1px solid',
                  mr: 0.5,
                  minWidth: 'auto',
                  paddingX: 1,
                  fontSize: '0.75rem',
                }}
              />
            ))}
          </Tabs>

          {/* RIGHT: Buttons */}
          <Stack direction='row' spacing={1} alignItems='center'>
            {taskId && (
              <Button
                variant='contained'
                className='btn-save'
                onClick={handleRejectClick}
                disabled={actionDisabled}
                sx={{ height: 'auto' }}
              >
                Accept
              </Button>
            )}

            {/* <Button
              variant='outlined'
              className='btn-save2'
              sx={{
                color: '#0100cb',
                border: '1px solid',
                height: 'auto',
                width: 'fit-content',
              }}
              onClick={handleAuditOpen}
            >
              Audit Trail
            </Button> */}
          </Stack>
        </Stack>

        {tabIndex === 0 && (
          <div>
            <ProductionAopView
              handleCalculate={handleCalculate}
              handleExport={handleExport}
              fetchSecondGridData={fetchData}
            />

            {/* <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
              Annual AOP Cost
            </Typography> */}
            {/* <div style={{ minHeight: 'fit-content', maxHeight: 'max-content' }}> */}
            <KendoDataTablesReports
              title='Annual AOP Cost'
              modifiedCells={modifiedCells}
              autoHeight={true}
              rows={rows}
              setRows={setRows}
              onRowUpdate={(updatedRow) =>
                console.log('Row Updated:', updatedRow)
              }
              columns={columns}
              // className='jio-data-grid'
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
              handleExport={handleExport}
              isCreatingCase={isCreatingCase}
              createCase={createCase}
              saveChanges={saveChanges}
              showCreateCasebutton={showCreateCasebutton}
              permissions={{
                // customHeight: defaultCustomHeight,
                saveBtn: true,
                saveBtnForWorkflow: true,
                remarksEditable: true,
                showCreateCasebutton: showCreateCasebutton,
                showTitle: true,
                // showCalculate: true,
                showWorkFlowBtns: true,
                // approveBtn: false,
              }}
              openAuditPopup={openAuditPopup}
              handleAuditOpen={handleAuditOpen}
              handleAuditClose={handleAuditClose}
              handleRejectClick={handleRejectClick}
              openRejectDialog={openRejectDialog}
              handleRejectCancel={handleRejectCancel}
              handleRemarkCellClick={handleRemarkCellClick}
              handleSubmit={handleSubmit}
              taskId={taskId}
              text={text}
              setText={setText}
            />
            {/* </div> */}

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
        {tabIndex === 5 && <AnnualProductionPlan />}
        {tabIndex === 6 && <PlantContribution />}
      </Box>
    </div>
  )
}

export default WorkFlowMerge
