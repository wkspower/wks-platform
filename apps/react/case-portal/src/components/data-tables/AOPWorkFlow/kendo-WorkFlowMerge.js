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
import { AOPWorkFlowService } from 'services/AOPWorkFlowService'
// import { TaskService } from 'services/TaskService'
import { useSession } from 'SessionStoreContext'
import postmanData from '../../../assets/postmandata.json'

import {
  Button,
  Stack,
  Tab,
  Tabs,
  Typography,
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
import PlantContributionLastFourYears from '../Reports-kendo/kendo-PlantContribution-Last-Four-Years'

import BestAchievedReport from '../Reports/BestAchievedReport'
import MonthWiseRawData from '../Reports/MonthWiseRawData'
import FurnaceRawData from '../Reports/FurnaceRawData'
import OptimizerReport from '../Reports/OptimizerReport'
import TurnaroundReportCracker from '../Reports/TurnaroundReportCracker'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import SpecificConsumptionNorm from '../Reports-kendo/SpecificConsumptionnorm'
import { getRoleName } from 'services/role-service'
const WorkFlowMerge = () => {
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
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
  const [loadingCalculate, setLoadingCalculate] = useState(false)
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

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [businessKey, setBusinessKey] = useState('')
  const [masterSteps, setMasterSteps] = useState([])
  const [workflowDto, setWorkFlowDto] = useState({})
  const [status, setStatus] = useState('')
  const [caseId, setCaseId] = useState('')
  const [role, setRole] = useState('')
  // UI feedback
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [tabIndex, setTabIndex] = useState(0)
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
    setTabIndex(0)
    fetchData()
  }, [PLANT_ID, AOP_YEAR])

  const handleExport = () => {
    handleExportAll()
  }

  const handleCalculate = async () => {
    try {
      setLoadingCalculate(true)

      if (!PLANT_ID || !AOP_YEAR) {
        throw new Error('PLANT_ID or AOP_YEAR not found ')
      }

      const [data, res1, res2, res3, res4, res5, res6, res7, res8, res9] =
        await Promise.all([
          AOPWorkFlowService.handleCalculateAnnualAopCostMiisContribution(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.handleCalculateProductionVolData2(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.handleCalculatePlantProductionData(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.handleCalculateMonthwiseProduction(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.calculateTurnAroundPlanReportData(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.calculateAnnualProductionPlanData(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.handleCalculatePlantConsumptionData(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.calculatePlantContributionReportData(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),

          AOPWorkFlowService.calculatePlantContributionSummaryYearly(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),

          AOPWorkFlowService.calculatePlantContributionBusinessDemand(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),
          AOPWorkFlowService.calculateGradeSpecificConsumptionNorm(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          ),

          Promise.resolve(null),
        ])

      const responses = [
        data,
        res1,
        res2,
        res3,
        res4,
        res5,
        res6,
        res7,
        res8,
        res9,
      ]

      const allSuccess = responses.every(
        (res) => res !== null && res !== undefined,
      )

      if (allSuccess) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoadingCalculate(false)
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Failed!',
          severity: 'error',
        })
        setLoadingCalculate(false)
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      setLoadingCalculate(false)
      console.error('Error!', error)
    } finally {
      // setLoadingCalculate(false)
      // console.log('false 1')
    }
  }

  const handleExportAll = async () => {
    try {
      setLoading(true)

      if (!PLANT_ID || !AOP_YEAR) {
        throw new Error('PLANT_ID or AOP_YEAR not found')
      }

      const payload = postmanData

      // Await the API call here to ensure completion
      const data = await AOPWorkFlowService.getExcel(
        keycloak,
        payload,
        PLANT_ID,
        AOP_YEAR,
      )

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
    if (READ_ONLY) return
    // do not delete commented code
    // try {
    //   const cases = await AOPWorkFlowService.getCaseId(keycloak)
    //   console.log(cases?.workflowList?.length)
    //   if (cases?.workflowList?.length !== 0) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
    // } catch (err) {
    //   console.error('Error fetching case', err)
    // }
  }

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

  function getNumericKeysInAllRows(rows = []) {
    if (!Array.isArray(rows) || rows.length === 0) return []

    // collect every key that appears in any row
    const allKeys = Array.from(
      rows.reduce((set, row) => {
        if (row && typeof row === 'object') {
          Object.keys(row).forEach((k) => set.add(k))
        }
        return set
      }, new Set()),
    )

    return allKeys.filter((key) =>
      rows.every((row) => {
        const v = row?.[key]
        // ignore missing / null / empty-string values (they don't disqualify the key)
        if (v === undefined || v === null || String(v).trim() === '')
          return true

        const n = Number(String(v).trim())
        return Number.isFinite(n)
      }),
    )
  }

  const VALUE_FORMATOR = ValueFormatterProduction()

  const generateColumns = (data, numericKeys, handleRemarkCellClick) => {
    const cols = data.headers.map((header, i) => {
      const field = data.keys[i]
      const isNumeric = numericKeys.includes(field)
      return {
        field,
        headerName: header,
        // minWidth: i === 0 ? 300 : 150,
        flex: 1,
        ...(i === 0 && {
          renderHeader: (p) => <div>{p.colDef.headerName}</div>,
        }),
        ...(isNumeric && {
          type: 'number',
          format: VALUE_FORMATOR,
        }),
      }
    })

    const remarkIdx = cols.findIndex((c) => c.field === 'remark')
    if (remarkIdx > -1) {
      cols[remarkIdx] = remarkColumn(handleRemarkCellClick)
    }

    return cols
    // The column is considered numeric if:
    // - It's a valid number (including empty values)
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      const { headers, keys, results } = await AOPWorkFlowService.getWorkflowData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const numericKeys = getNumericKeysInAllRows(results)
      const formatted = results.map((row, idx) => ({
        id: idx,
        ...row,
        ...Object.fromEntries(
          Object.entries(row).map(([k, v]) => [
            k,
            numericKeys.includes(k) && v !== '' ? Number(v) : v,
          ]),
        ),
      }))

      setRows(formatted)
      setColumns(
        generateColumns({ headers, keys }, numericKeys, handleRemarkCellClick),
      )
    } catch (err) {
      console.error('Error fetching grid', err)
      setRows([])
      setColumns([])
    } finally {
      // setLoading(false)
      // console.log('false 3')
    }
  }

  const getCaseId = async () => {
    if (!PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID) return
    try {
      const cases = await AOPWorkFlowService.getCaseId(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )
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
      // setLoading(false)
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
          year: AOP_YEAR,
          plantFkId: PLANT_ID,
          caseDefId: caseId || caseData.caseDefinitionId,
          // caseId: result.businessKey,
          siteFKId: SITE_ID,
          verticalFKId: VERTICAL_ID,
        },
        variables: caseData.attributes,
        // allData: rows,
        workflowYearDTO: rows,
      }
      const result = await AOPWorkFlowService.submitWorkFlow(payload, keycloak)
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
    getCaseId()
  }, [PLANT_ID, AOP_YEAR])

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
      await AOPWorkFlowService.completeTask(keycloak, payloadOfCompleteTask)
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
      await AOPWorkFlowService.saveAnnualWorkFlowData(keycloak, rows, PLANT_ID)
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

  // Define tab sets
  const defaultTabs = [
    'Annual AOP Cost',
    'Plant Production Summary',
    'Month Wise Production Plan',
    'Month Wise Raw Data',
    'Turnaround Report',
    'Annual Production Plan',
    'Plant Contribution',
    'Plant Contribution Summary (T-22)',
  ]
  const customPETabs = [
    'Annual AOP Cost',
    'Plant Production Summary (T-14)',
    'Month Wise Production Plan (T-16)',
    'Month Wise Raw Data(T-18)',
    'Turnaround Report(T-19A)',
    'Annual Production Plan(T-15)',
    'Plant Contribution(T-21)',
    'Plant Contribution Summary (T-22)',
    'Specific Consumption Norms',
  ]

  const customPPTabs = [
    'Annual AOP Cost',
    'Plant Production Summary (T-14)',
    'Month Wise Production Plan (T-16)',
    'Month Wise Raw Data(T-18)',
    'Turnaround Report(T-19A)',
    'Annual Production Plan(T-15)',
    'Plant Contribution(T-21)',
    'Plant Contribution Summary (T-22)',
    'Specific Consumption Norms',
  ]
  const PETabs = [
    'Annual AOP Cost',
    'Plant Production Summary',
    'Month Wise Production Plan',
    'Month Wise Raw Data',
    'Turnaround Report',
    'Annual Production Plan',
    'Plant Contribution',
    'Plant Contribution Summary ',
  ]

  const PPTabs = [
    'Annual AOP Cost',
    'Plant Production Summary',
    'Month Wise Production Plan',
    'Month Wise Raw Data',
    'Turnaround Report',
    'Annual Production Plan',
    'Plant Contribution',
    'Plant Contribution Summary',
  ]

  const crackerTabs = [
    'Optimizer Input / Output',
    'Month Wise Production Plan',
    'Month Wise Norms',
    'Furnace Data',
    'Turnaround',
    'Plant Contribution (T-21)',
    'Plant Contribution Summary (T-22)',
  ]

  const elastomerTabs = [
    'Annual AOP Cost',
    'Plant Production Summary',
    'Month Wise Production Plan',
    'Month Wise Consumption',
    'Turnaround Report',
    'Annual Production Plan',
    'Plant Contribution',
    'Plant Contribution Summary (T-22)',
  ]

  // Pick tabs based on vertical
  // Pick tabs based on vertical

  let activeTabs = defaultTabs
  if (lowerVertName === 'cracker') {
    activeTabs = crackerTabs
  } else if (
    lowerVertName === 'elastomer' ||
    lowerVertName === 'aromatics' ||
    lowerVertName === 'pta' ||
    lowerVertName === 'vcm'
  ) {
    activeTabs = elastomerTabs
  } else if (lowerVertName === 'pe') {
    activeTabs = customPETabs
  } else if (lowerVertName === 'pp') {
    activeTabs = customPPTabs
  }
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
        marginTop: '-20px',
      }}
    >
      <Box>
        <Stepper
          activeStep={activeStep}
          alternativeLabel
          sx={{
            marginBottom: '10px',
            '& .MuiStepLabel-label': {
              fontWeight: 'normal',
            },
            '& .MuiStepLabel-label.Mui-active': {
              fontWeight: 'bold',
              color: '#000',
            },
            '& .MuiStepLabel-alternativeLabel': {
              marginTop: '3px !important',
            },
          }}
        >
          {masterSteps?.map((step) => (
            <Step
              key={step.displayName}
              completed={step.status === 'completed'}
              sx={{
                cursor: 'pointer',
                '& .MuiStepIcon-root.Mui-active': {
                  color: '#0100cb',
                },
              }}
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

        <Typography
          component='div'
          className='text-note'
          style={{ marginTop: 24 }}
        >
          * Prices - MIIS BPC (Last Budget Year), Actual Values - MIIS
          Contribution (YTD).
        </Typography>

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
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 10px 0px',
              minHeight: '28px',
            }}
            textColor='primary'
            indicatorColor='primary'
          >
            {activeTabs.map((label, idx) => (
              <Tab
                key={idx}
                label={label}
                sx={{
                  border: '1px solid #ADD8E6',
                  borderBottom: '1px solid #ADD8E6',
                  fontSize: '0.75rem',
                  padding: '9px',
                  minHeight: '12px',
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

        {/* For OTHER verticals */}
        {(lowerVertName === 'meg' ||
          lowerVertName === 'pe' ||
          lowerVertName === 'pp') && (
          <>
            {tabIndex === 0 && (
              <ProductionAopView
                handleCalculate={handleCalculate}
                handleExport={handleExport}
                fetchSecondGridData={fetchData}
              />
            )}
            {tabIndex === 0 && (
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
                loading={loadingCalculate}
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
                  saveBtn: !isOldYear,
                  saveBtnForWorkflow: true,
                  remarksEditable: true,
                  showCreateCasebutton: showCreateCasebutton,
                  showTitle: true,
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
            )}
            {tabIndex === 1 && <PlantsProductionSummary />}
            {tabIndex === 2 && <MonthwiseProduction />}
            {tabIndex === 3 && <MonthwiseRawMaterial />}
            {tabIndex === 4 && <TurnaroundReport />}
            {tabIndex === 5 && <AnnualProductionPlan />}
            {tabIndex === 6 && <PlantContribution />}
            {tabIndex === 7 && <PlantContributionLastFourYears />}
            {(lowerVertName === 'pe' || lowerVertName === 'pp') && (
              <>{tabIndex === 8 && <SpecificConsumptionNorm />}</>
            )}

            <Notification
              open={snackbarOpen}
              message={snackbarData.message}
              severity={snackbarData.severity}
              onClose={() => setSnackbarOpen(false)}
            />
          </>
        )}

        {/* For CRACKER */}
        {lowerVertName === 'cracker' && (
          <>
            {/* {tabIndex === 0 && <BestAchievedReport />}
            {tabIndex === 1 && <MonthWiseRawData />}
            {tabIndex === 2 && <FurnaceRawData />}
            {tabIndex === 3 && <PlantContribution />}
            {tabIndex === 4 && <PlantContributionLastFourYears />}
 */}

            {tabIndex === 0 && <OptimizerReport />}
            {tabIndex === 1 && <BestAchievedReport />}
            {tabIndex === 2 && <MonthWiseRawData />}
            {tabIndex === 3 && <FurnaceRawData />}
            {tabIndex === 4 && <TurnaroundReportCracker />}

            {tabIndex === 5 && <PlantContribution />}
            {tabIndex === 6 && <PlantContributionLastFourYears />}

            <Notification
              open={snackbarOpen}
              message={snackbarData.message}
              severity={snackbarData.severity}
              onClose={() => setSnackbarOpen(false)}
            />
          </>
        )}

        {/* For ELASTOMER */}
        {(lowerVertName === 'elastomer' ||
          lowerVertName === 'aromatics' ||
          lowerVertName === 'pta' ||
          lowerVertName === 'vcm') && (
          <>
            {tabIndex === 0 && (
              <ProductionAopView
                handleCalculate={handleCalculate}
                handleExport={handleExport}
                fetchSecondGridData={fetchData}
              />
            )}
            {tabIndex === 0 && (
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
                loading={loadingCalculate}
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
                  saveBtn: !isOldYear,
                  saveBtnForWorkflow: true,
                  remarksEditable: true,
                  showCreateCasebutton: showCreateCasebutton,
                  showTitle: true,
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
            )}

            {tabIndex === 1 && <PlantsProductionSummary />}
            {tabIndex === 2 && <MonthwiseProduction />}
            {tabIndex === 3 && <MonthwiseRawMaterial />}
            {tabIndex === 4 && <TurnaroundReport />}
            {tabIndex === 5 && <AnnualProductionPlan />}
            {tabIndex === 6 && <PlantContribution />}
            {tabIndex === 7 && <PlantContributionLastFourYears />}

            <Notification
              open={snackbarOpen}
              message={snackbarData.message}
              severity={snackbarData.severity}
              onClose={() => setSnackbarOpen(false)}
            />
          </>
        )}
      </Box>
    </div>
  )
}

export default WorkFlowMerge
