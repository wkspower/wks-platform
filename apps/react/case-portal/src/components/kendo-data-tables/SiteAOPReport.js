// Fixed Expenses grid columns (parent-child, for KendoDataTablesReports, tabIndex 9)
// MCU Capacity Utilization grid columns (for tab 11)
import { useGridApiRef } from '@mui/x-data-grid'
import React, { useEffect, useState, useMemo, useCallback } from 'react'
import { useSelector } from 'react-redux'
import getSiteAOPReportColumns from 'components/colums/SiteReportColums'
import { SiteReportDataService } from 'services/SiteReportDataService'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'
import { Box, Button, Tab, Tabs, Typography } from '@mui/material'
import { TextArea } from '@progress/kendo-react-inputs'
import FixedExpenses from './FixedExpenses'
import Capex from './Capex'
import ShutdownSlowdownPlan from './SlowdownPlan'
import TechnicalAvailability from './TechnicalAvailability'
import CrackerReportMannualEntry from './CrackerReportMannualEntry'
const SiteAOPReport = ({ permissions }) => {
  const [_plantID, set_PlantID] = useState('')
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCells2, setModifiedCells2] = useState({})
  const [modifiedCellsEnergyPerformance, setModifiedCellsEnergyPerformance] =
    useState({})
  const [modifiedCellsShutdownSlowdown, setModifiedCellsShutdownSlowdown] =
    React.useState({})
  const [modifiedCells1, setModifiedCells1] = React.useState({})
  const [allProducts, setAllProducts] = useState([])
  const [allDescriptionDrpdwn, setAllDescriptionDrpdwn] = useState([])
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
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name

  const SITE_ID = siteObject?.id
  const SITE_NAME = siteObject?.name

  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name

  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const lowerVertName = vertName?.toLowerCase()
  const lowerSiteName = SITE_NAME?.toLowerCase()
  const lowerPlantName = PLANT_NAME?.toLowerCase()
  const plantName = plantObject?.name
  const siteName = siteObject?.name
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  //const [Rowssafety, setRowssafety] = useState([])

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [deleteId1, setDeleteId1] = useState(null)
  const apiRef = useGridApiRef()
  //const [rows, setRows] = useState()
  const [rowsSlowdown, setRowsSlowdown] = useState()

  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRemark1, setCurrentRemark1] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowId1, setCurrentRowId1] = useState(null)
  const [remarkDialogOpen1, setRemarkDialogOpen1] = useState(false)
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [
    remarkDialogOpenEnergyPerformance,
    setRemarkDialogOpenEnergyPerformance,
  ] = useState(false)
  const [currentRemarkEnergyPerformance, setCurrentRemarkEnergyPerformance] =
    useState('')
  const [currentRowIdEnergyPerformance, setCurrentRowIdEnergyPerformance] =
    useState(null)

  const keycloak = useSession()
  const [rows, setRows] = useState()
  const [energyPerformance, setEnergyPerformance] = useState()
  const [performanceSummary, setPerformanceSummary] = useState('')
  const [performanceHighlightsEdited, setPerformanceHighlightsEdited] =
    useState(false)
  const [tabIndex, setTabIndex] = useState(0)
  const defaultTabs = [
    'Site Team',
    'Energy Performance',
    'Fixed Expenses',
    'Capex/PIO Plan',
    'Shutdown / Slowdown plan',
    'Technical Availability',
    'Report Manual Entry',
    // 'Safety Performance & Targets',
    // 'Contribution (Rs/ MT & Rs Crs.)',
    // 'Major Process Incidents',
    // 'Major Process Incidents FY26',
    // 'Major Incidents FY26',
    // // Major Process Incidents FY26 columns and dummy data (tabIndex 9)
    // 'Major Safety Improvement Initiative',
    // 'Production (TPH) basis',
    // 'Production',
    // 'Conversion & Variable Cost',
    // 'MCU Capacity Utilization (%)',
  ]
  function getAopShortYears(aopYear) {
    if (!aopYear) return { prev: '', next: '' }
    const match = aopYear.match(/(\d{4})-(\d{2})/)
    if (match) {
      const prev = match[1].slice(-2)
      const next = match[2]
      return { prev, next }
    }
    const year = String(aopYear).slice(-2)
    return { prev: year, next: String(Number(year) + 1).padStart(2, '0') }
  }
  const { prev, next } = getAopShortYears(AOP_YEAR)
  const valueFormat = ValueFormatterConsumption()
  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const headerMap = generateHeaderNames(AOP_YEAR)
  const IS_PE_PP_VERTICAL = lowerVertName === 'pe' || lowerVertName === 'pp'
  const columns = getSiteAOPReportColumns({ AOP_YEAR, valueFormat, prev, next })
  const handleRemarkCellClick1 = (row) => {
    setCurrentRemark1(row.remarks || '')
    setCurrentRowId1(row.id)
    setRemarkDialogOpen1(true)
  }

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClickEnergyPerformance = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkEnergyPerformance(row.remark || '')
    setCurrentRowIdEnergyPerformance(row.id)
    setRemarkDialogOpenEnergyPerformance(true)
  }
  const handleRemarkCellClickMajorInc = (row) => {
    setCurrentRemark2(row.remarks || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }
  const updateShutdownData = () => {
    // Dummy function for now
  }

  const dummyRowsSafety = [
    {
      id: 1,
      sno: 1,
      uom: 'Nos',
      bestAchieved: 0,
      aop: 0,
      actual: 0,
      remarks: '',
      isEditable: true, // <-- add this
      isdisable: false,
    },
    {
      id: 2,
      sno: 2,
      uom: 'Nos',
      bestAchieved: 0,
      aop: 0,
      actual: 0,
      remarks: '',
      isEditable: true, // <-- add this
      isdisable: false,
    },
    {
      id: 3,
      sno: 3,
      uom: 'Nos',
      bestAchieved: 0,
      aop: 0,
      actual: 0,
      remarks: '',
      isEditable: true, // <-- add this
      isdisable: false,
    },
  ]
  const [Rowssafety, setRowssafety] = useState(dummyRowsSafety)
  const majorIncidentsRows = [
    {
      id: 1,
      sno: 1,
      plant: 'Plant A',
      incidentDescription: 'Fire in storage area',
      rootCauses: 'Short circuit',
      recommendation: 'Upgrade wiring',
      targetDate: '2025-08-15',
      resp: 'John Doe',
      remarks: 'ch.shiva  ji ma ha ra j',
    },
    {
      id: 2,
      sno: 2,
      plant: 'Plant B',
      incidentDescription: 'Chemical spill',
      rootCauses: 'Valve failure',
      recommendation: 'Replace valves',
      targetDate: '2025-09-10',
      resp: 'Jane Smith',
      remarks: 'Pending review',
    },
  ]

  const [majorIncidents, setMajorIncidents] = useState(majorIncidentsRows)

  const majorSafetyInitiativeRows = [
    {
      id: 1,
      sno: 1,
      plant: 'Plant A',
      initiativeDescription: 'Install new fire suppression system',
      category: 'Fire Safety',
      outcome: 'Reduced risk',
      recommendation: 'Annual maintenance',
      targetDate: '2025-12-31',
      resp: 'A. Kumar',
    },
    {
      id: 2,
      sno: 2,
      plant: 'Plant B',
      initiativeDescription: 'Safety training for staff',
      category: 'Training',
      outcome: 'Improved awareness',
      recommendation: 'Repeat every 6 months',
      targetDate: '2026-03-15',
      resp: 'S. Mehta',
    },
  ]
  const [majorSafetyInitiative, setMajorSafetyInitiative] = useState(
    majorSafetyInitiativeRows,
  )

  const productionRows = [
    {
      id: 1,
      plant: 'Cracker',
      uom: 'KTA',
      aop: 120,
      basis: 'Historical average',
      remarks: 'Stable operation',
    },
    {
      id: 2,
      plant: 'EOEG',
      uom: 'KTA',
      aop: 95,
      basis: 'Last year actual',
      remarks: 'Increase planned',
    },
    {
      id: 3,
      plant: 'PP',
      uom: 'KTA',
      aop: 110,
      basis: 'Market demand',
      remarks: 'Expansion project',
    },
    {
      id: 4,
      plant: 'LDPE',
      uom: 'KTA',
      aop: 80,
      basis: 'Capacity',
      remarks: 'No change',
    },
    {
      id: 5,
      plant: 'LLD Tr-1',
      uom: 'KTA',
      aop: 70,
      basis: 'Forecast',
      remarks: 'Maintenance scheduled',
    },
    {
      id: 6,
      plant: 'LLD Tr-2',
      uom: 'KTA',
      aop: 75,
      basis: 'Forecast',
      remarks: 'New line',
    },
    {
      id: 7,
      plant: 'Hexene-1',
      uom: 'KTA',
      aop: 30,
      basis: 'Raw material availability',
      remarks: 'Dependent on supply',
    },
    {
      id: 8,
      plant: 'R-Pet',
      uom: 'KTA',
      aop: 50,
      basis: 'Sustainability initiative',
      remarks: 'Pilot phase',
    },
  ]
  const [production, setProduction] = useState(productionRows)
  //----------------------------------

  const conversionVariableCostRows = [
    {
      id: 1,
      plant: 'Cracker',
      costHead: 'Conversion',
      fy26Aop: 1200,
      fy26Actual: 1150,
      fy27Aop: 1250,
      rationalReasons: '** Free Text Field** - User Entry',
    },
    {
      id: 2,
      plant: 'Cracker',
      costHead: 'Variable',
      fy26Aop: 800,
      fy26Actual: 780,
      fy27Aop: 820,
      rationalReasons: 'Raw material price fluctuation',
    },
    {
      id: 3,
      plant: 'EOEG',
      costHead: 'Conversion',
      fy26Aop: 950,
      fy26Actual: 900,
      fy27Aop: 980,
      rationalReasons: 'FY26 Actual – as on 30th Nov’25',
    },
    {
      id: 4,
      plant: 'EOEG',
      costHead: 'Variable',
      fy26Aop: 600,
      fy26Actual: 620,
      fy27Aop: 640,
      rationalReasons: 'Increase in utility cost',
    },
    // ...add more rows as needed
  ]
  const [conversionVariableCost, setConversionVariableCost] = useState(
    conversionVariableCostRows,
  )
  //----------------------------------

  const fetchDataEnergyPerformance = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setModifiedCellsEnergyPerformance({})

    setLoading(true)
    try {
      var data = await SiteReportDataService.getEnergyPerformanceDetails(
        keycloak,
        SITE_ID,
        AOP_YEAR,
      )

      const formattedData = (data?.data?.Data || []).map((item, idx) => ({
        ...item,
        sno: idx + 1,
        originalRemark: item.remark, // Add serial number if not present
      }))

      setEnergyPerformance(formattedData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  //---------------------------
  const saveChangesEnergyPerformance = React.useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCellsEnergyPerformance)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['remark']

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const payload = data.map((item, index) => ({
        id: item.id || null,
        plant: item.plant,
        uom: item.uom,
        aopValue: item.aopValue,
        actualValue: item.actualValue,
        planValue: item.planValue,
        remark: item.remark,
      }))

      // 3. Save to API
      const response = await SiteReportDataService.saveEnergyPerformance(
        keycloak,
        SITE_ID,
        AOP_YEAR,
        payload,
      )

      // 4. Handle API response
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsEnergyPerformance({})
        fetchDataEnergyPerformance()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Save failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [
    modifiedCellsEnergyPerformance,
    keycloak,
    SITE_ID,
    AOP_YEAR,
    fetchDataEnergyPerformance,
  ])

  const [performanceId, setPerformanceId] = useState(null)

  const getPerformanceHighlights = async () => {
    if (!PLANT_ID || !SITE_ID || !AOP_YEAR) return

    try {
      // Clear states before fetching
      setPerformanceSummary('')
      setPerformanceId(null)

      const res = await SiteReportDataService.getPerformanceHighlightsSummary(
        keycloak,
        SITE_ID,
        AOP_YEAR,
      )

      if (res?.code === 200 && res?.data?.Data?.length > 0) {
        // Get the first record from the array
        const record = res.data.Data[0]

        // Set both the text and the ID
        setPerformanceSummary(record.summary || '')
        setPerformanceId(record.id || null)
      } else {
        setPerformanceSummary('')
        setPerformanceId(null)
      }
    } catch (error) {
      setPerformanceSummary('')
      setPerformanceId(null)
      console.error('Error fetching summary:', error)
    }
  }

  // Save summary (POST/PUT)
  // Assuming you store the ID from the GET response in a state variable

  const savePerformanceHighlightsSummary = async () => {
    try {
      // Construct the DTO list expected by the @RequestBody List<PerformanceHighlightDTO>
      const payload = [
        {
          id: performanceId, // The UUID from the GET response
          summary: performanceSummary,
          saveStatus: null,
        },
      ]

      const res = await SiteReportDataService.savePerformanceHighlightsSummary(
        keycloak,

        SITE_ID, // mapped to @RequestParam String siteId
        AOP_YEAR, // mapped to @RequestParam String year
        payload, // mapped to @RequestBody List<PerformanceHighlightDTO>
      )

      if (res?.code === 200 || res?.code === 207) {
        setSnackbarData({
          message:
            res?.code === 200
              ? 'Saved Successfully!'
              : 'Saved with minor issues',
          severity: res?.code === 200 ? 'success' : 'warning',
        })
        setPerformanceHighlightsEdited(false)
        setSnackbarOpen(true)
      } else {
        setSnackbarData({
          message: 'Save Failed!',
          severity: 'error',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      setSnackbarData({
        message: 'Error saving summary!',
        severity: 'error',
      })
      setSnackbarOpen(true)
    }
  }
  //----------
  const contributionColumns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    { field: 'plant', title: 'Plant', editable: false },
    {
      title: 'FY 26 AOP',
      children: [
        {
          field: 'fy26AopRsMt',
          title: 'Rs/ MT',
          editable: true,
          type: 'number',
        },
        {
          field: 'fy26AopRsCrs',
          title: 'Rs. Crs.',
          editable: true,
          type: 'number',
        },
      ],
    },
    {
      title: 'FY26 Actual',
      children: [
        {
          field: 'fy26ActualRsMt',
          title: 'Rs/ MT',
          editable: true,
          type: 'number',
        },
        {
          field: 'fy26ActualRsCrs',
          title: 'Rs. Crs.',
          editable: true,
          type: 'number',
        },
      ],
    },
    {
      title: 'FY27 AOP',
      children: [
        {
          field: 'fy27AopRsMt',
          title: 'Rs/ MT',
          editable: true,
          type: 'number',
        },
        {
          field: 'fy27AopRsCrs',
          title: 'Rs. Crs.',
          editable: true,
          type: 'number',
        },
      ],
    },
    { field: 'rationalReasons', title: 'Rationale/ Reasons', editable: true },
  ]
  const contributionRows = [
    {
      id: 1,
      plant: 'EOE',
      fy26AopRsMt: 1200,
      fy26AopRsCrs: 50,
      fy26ActualRsMt: 1150,
      fy26ActualRsCrs: 48,
      fy27AopRsMt: 1250,
      fy27AopRsCrs: 52,
      rationalReasons: 'Market improvement',
    },
    {
      id: 2,
      plant: 'LDPE',
      fy26AopRsMt: 900,
      fy26AopRsCrs: 40,
      fy26ActualRsMt: 880,
      fy26ActualRsCrs: 39,
      fy27AopRsMt: 920,
      fy27AopRsCrs: 41,
      rationalReasons: 'Stable demand',
    },
    {
      id: 3,
      plant: 'LLD Tr-1',
      fy26AopRsMt: 950,
      fy26AopRsCrs: 42,
      fy26ActualRsMt: 930,
      fy26ActualRsCrs: 41,
      fy27AopRsMt: 970,
      fy27AopRsCrs: 43,
      rationalReasons: 'Capacity expansion',
    },
    {
      id: 4,
      plant: 'LLD Tr-2',
      fy26AopRsMt: 960,
      fy26AopRsCrs: 43,
      fy26ActualRsMt: 940,
      fy26ActualRsCrs: 42,
      fy27AopRsMt: 980,
      fy27AopRsCrs: 44,
      rationalReasons: 'Efficiency gain',
    },
    {
      id: 5,
      plant: 'PP',
      fy26AopRsMt: 1100,
      fy26AopRsCrs: 47,
      fy26ActualRsMt: 1080,
      fy26ActualRsCrs: 46,
      fy27AopRsMt: 1120,
      fy27AopRsCrs: 48,
      rationalReasons: 'New product line',
    },
  ]
  const [contribution, setContribution] = useState(contributionRows)

  // Production (TPH) basis grid columns (tabIndex 11)
  const productionTphColumns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sno',
      title: 'S.No',
      widthT: 60,
      editable: false,
      align: 'right',
      format: '{0:0}',
    },
    { field: 'plant', title: 'Plant', widthT: 120, editable: true },
    { field: 'tph', title: 'TPH', widthT: 100, editable: true, type: 'number' },
    {
      field: 'fy26Aop',
      title: 'FY26 AOP',
      widthT: 100,
      editable: true,
      type: 'number',
    },
    {
      field: 'fy26Actual',
      title: 'FY26 Actual',
      widthT: 100,
      editable: true,
      type: 'number',
    },
    {
      field: 'fy27Aop',
      title: 'FY27 AOP',
      widthT: 100,
      editable: true,
      type: 'number',
    },
    {
      field: 'rationalReasons',
      title: 'Rationale/ Reasons',
      widthT: 200,
      editable: true,
    },
  ]

  const productionTphRows = [
    {
      id: 1,
      sno: 1,
      plant: '',
      tph: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
    {
      id: 2,
      sno: 2,
      plant: '',
      tph: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
    {
      id: 3,
      sno: 3,
      plant: '',
      tph: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
  ]

  const [productionTphState, setProductionTphState] =
    useState(productionTphRows)
  // Shutdown/Slow Down Plan grid columns

  const mcuCapacityUtilizationColumns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sno',
      title: 'S.No',
      widthT: 60,
      editable: false,
      align: 'right',
      format: '{0:0}',
    },
    { field: 'plant', title: 'Plant', widthT: 120, editable: true },
    {
      field: 'fy26Aop',
      title: 'FY26 AOP',
      widthT: 120,
      editable: true,
      type: 'number',
    },
    {
      field: 'fy26Actual',
      title: 'FY26 Actual',
      widthT: 120,
      editable: true,
      type: 'number',
    },
    {
      field: 'fy27Aop',
      title: 'FY27 AOP',
      widthT: 120,
      editable: true,
      type: 'number',
    },
    {
      field: 'rationalReasons',
      title: 'Rationale/ Reasons',
      widthT: 200,
      editable: true,
    },
  ]

  const mcuCapacityUtilizationRows = [
    {
      id: 1,
      sno: 1,
      plant: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
    {
      id: 2,
      sno: 2,
      plant: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
    {
      id: 3,
      sno: 3,
      plant: '',
      fy26Aop: '',
      fy26Actual: '',
      fy27Aop: '',
      rationalReasons: '',
    },
  ]

  const [mcuCapacityUtilizationState, setMcuCapacityUtilizationState] =
    useState(mcuCapacityUtilizationRows)

  // Major Process Incidents FY26 columns and dummy data (tabIndex 9)
  const majorProcessIncidentsFy26Columns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sno',
      title: 'S.No.',
      widthT: 60,
      editable: false,
      align: 'right',
      format: '{0:0}',
    },
    { field: 'plant', title: 'Plant', widthT: 120, editable: true },
    {
      field: 'incidentDescription',
      title: 'Incident Description',
      widthT: 220,
      editable: true,
    },
    {
      field: 'rootCause',
      title: 'Root Cause Analysis',
      widthT: 180,
      editable: true,
    },
    {
      field: 'recommendation',
      title: 'Recommendation',
      widthT: 180,
      editable: true,
    },
    {
      field: 'targetDate',
      title: 'Target Date',
      widthT: 120,
      editable: true,
      type: 'date',
    },
    { field: 'resp', title: 'Resp.', widthT: 100, editable: true },
  ]

  const majorProcessIncidentsFy26Rows = [
    {
      id: 1,
      sno: 1,
      plant: '',
      incidentDescription: '',
      rootCause: '',
      recommendation: '',
      targetDate: '',
      resp: '',
    },
    {
      id: 2,
      sno: 2,
      plant: '',
      incidentDescription: '',
      rootCause: '',
      recommendation: '',
      targetDate: '',
      resp: '',
    },
  ]

  const [majorProcessIncidentsFy26State, setMajorProcessIncidentsFy26State] =
    useState(majorProcessIncidentsFy26Rows)

  //---------------------------
  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setModifiedCells({})

    setLoading(true)
    try {
      var data = await SiteReportDataService.getSiteTeamDetails(
        keycloak,
        SITE_ID,
        AOP_YEAR,
      )

      const formattedData = (data?.data?.Data || []).map((item, idx) => ({
        ...item,
        sno: idx + 1,
        originalRemark: item.remark, // Add serial number if not present
      }))

      setRows(formattedData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  //---------------------------
  const saveChanges = React.useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCells)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['jobRole', 'name', 'age', 'teamSize', 'remark']

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const payload = data.map((item, index) => ({
        id: item.id || null,
        jobRole: item.jobRole,
        name: item.name,
        age: item.age,
        teamSize: item.teamSize,
        remark: item.remark,
      }))

      // 3. Save to API
      const response = await SiteReportDataService.saveSiteTeam(
        keycloak,
        SITE_ID,
        AOP_YEAR,
        payload,
      )

      // 4. Handle API response
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Save failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, keycloak, SITE_ID, AOP_YEAR, fetchData])

  useEffect(() => {
    if (tabIndex === 0) {
      fetchData()
    } else if (tabIndex === 1) {
      fetchDataEnergyPerformance()
      getPerformanceHighlights()
    }
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak, tabIndex])

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? true,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: true,
      downloadExcelBtn: false,
      showNoteWhileDeleting: false,
      showTitleNameBusiness: true,
      titleName: tabIndex === 0 ? 'Site Team' : 'Energy Performance',

      uploadExcelBtn: false,
    },
    isOldYear,
  )
  const adjustedPermissionsslowdown = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: false,
          addButton: true,
          deleteButton: true,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: true,
          allAction: true,
          downloadExcelBtnFromUI: true,
          uploadExcelBtn: false,
        },
        isOldYear,
      ),
    [isOldYear, AOP_YEAR, PLANT_ID, SCREEN_NAME],
  )
  const adjustedPermission1 = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: true,
          allAction: true,
          downloadExcelBtnFromUI: true,
          uploadExcelBtn: false,
          MonthDropdownPEPPHighlight: true,
        },
        isOldYear,
      ),
    [isOldYear, AOP_YEAR, PLANT_ID, SCREEN_NAME],
  )

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {defaultTabs?.length > 1 && (
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
          {defaultTabs.map((label, idx) => (
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
      )}
      {tabIndex === 0 && (
        <KendoDataTables
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          setRows={setRows}
          columns={columns.siteTeam}
          rows={rows}
          fetchData={fetchData}
          saveChanges={saveChanges}
          paginationOptions={[100, 200, 300]}
          updateShutdownData={updateShutdownData}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          deleteId={deleteId}
          open1={open1}
          setDeleteId={setDeleteId}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          permissions={adjustedPermissions}
          disableRedHighlight={true}
          screenType='shutdown'
        />
      )}
      {tabIndex === 1 && (
        <>
          <KendoDataTables
            modifiedCells={modifiedCellsEnergyPerformance}
            setModifiedCells={setModifiedCellsEnergyPerformance}
            columns={columns.energyPerformance}
            rows={energyPerformance}
            setRows={setEnergyPerformance}
            saveChanges={saveChangesEnergyPerformance}
            fetchData={fetchDataEnergyPerformance}
            title='B3.4. Energy Performance'
            permissions={adjustedPermissions}
            snackbarOpen={snackbarOpen}
            setSnackbarOpen={setSnackbarOpen}
            snackbarData={snackbarData}
            setSnackbarData={setSnackbarData}
            setDeleteId={setDeleteId}
            setOpen1={setOpen1}
            handleRemarkCellClick={handleRemarkCellClickEnergyPerformance}
            remarkDialogOpen={remarkDialogOpenEnergyPerformance}
            setRemarkDialogOpen={setRemarkDialogOpenEnergyPerformance}
            currentRemark={currentRemarkEnergyPerformance}
            setCurrentRemark={setCurrentRemarkEnergyPerformance}
            currentRowId={currentRowIdEnergyPerformance}
            setCurrentRowId={setCurrentRowIdEnergyPerformance}
          />
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column', // ?? stack vertically
              alignItems: 'flex-start',
              gap: 0,
              mt: 1,
            }}
          >
            <Typography className='button-title' sx={{ whiteSpace: 'nowrap' }}>
              Performance Highlights
            </Typography>

            <Button
              variant='contained'
              // onClick={onLoad}
              onClick={savePerformanceHighlightsSummary}
              className='btn-save'
              disabled={READ_ONLY || !performanceHighlightsEdited}
              sx={{ alignSelf: 'flex-end' }}
            >
              Save
            </Button>
            <TextArea
              value={performanceSummary}
              rows={6}
              style={{
                width: '100%',
              }}
              onChange={(e) => {
                setPerformanceSummary(e.target.value)
                setPerformanceHighlightsEdited(true)
              }}
              placeholder='Enter summary here...'
              disabled={READ_ONLY}
            />
          </Box>
        </>
      )}
      {tabIndex === 2 && <FixedExpenses />}

      {tabIndex === 3 && <Capex />}
      {tabIndex === 4 && <ShutdownSlowdownPlan />}

      {tabIndex === 5 && <TechnicalAvailability />}

      {tabIndex === 6 && <CrackerReportMannualEntry tabIndex={5} />}

      {tabIndex === 7 && (
        <KendoDataTablesReports
          columns={columns.safetyPerformance}
          rows={Rowssafety}
          setRows={setRowssafety}
          deleteId={deleteId}
          setDeleteId={setDeleteId}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          open1={open1}
          setOpen1={setOpen1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          remarkDialogOpen={remarkDialogOpen1}
          setRemarkDialogOpen={setRemarkDialogOpen1}
          currentRemark={currentRemark1}
          setCurrentRemark={setCurrentRemark1}
          currentRowId={currentRowId1}
          handleRemarkCellClick={handleRemarkCellClick1}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          permissions={adjustedPermissionsslowdown}
        />
      )}
      {tabIndex === 8 && (
        <KendoDataTables
          columns={contributionColumns}
          rows={contribution}
          setRows={setContribution}
          title='B3.5. Contribution (Rs/ MT & Rs Crs.)'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}

      {tabIndex === 9 && (
        <KendoDataTables
          columns={majorProcessIncidentsFy26Columns}
          rows={majorProcessIncidentsFy26State}
          setRows={setMajorProcessIncidentsFy26State}
          title='B3.6. Major Process Incidents FY26: (High & Medium Risks)'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}

      {tabIndex === 10 && (
        <KendoDataTables
          columns={columns.majorIncidents}
          rows={majorIncidents}
          setRows={setMajorIncidents}
          modifiedCells={modifiedCells2} // <-- add this
          setModifiedCells={setModifiedCells2}
          title='B2.1. Major Incidents FY26: (Fatality, PSI Tier 1 & 2, LWC, High Severity, Process Fires)'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          handleRemarkCellClick={handleRemarkCellClickMajorInc}
          remarkDialogOpen={remarkDialogOpen2}
          setRemarkDialogOpen={setRemarkDialogOpen2}
          currentRemark={currentRemark2}
          setCurrentRemark={setCurrentRemark2}
          currentRowId={currentRowId2}
        />
      )}
      {tabIndex === 11 && (
        <KendoDataTables
          columns={columns.majorSafetyInitiative}
          rows={majorSafetyInitiative}
          setRows={setMajorSafetyInitiative}
          title='B2.2. Major Safety Improvement Initiative FY27 (Max 5)'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}

      {tabIndex === 12 && (
        <KendoDataTables
          columns={productionTphColumns}
          rows={productionTphState}
          setRows={setProductionTphState}
          title='Production (TPH) basis'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}

      {tabIndex === 13 && (
        <KendoDataTablesReports
          columns={columns.production}
          rows={production}
          setRows={setProduction}
          title='B3.1. AOP FY27 Basis'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}

      {tabIndex === 15 && (
        <KendoDataTables
          columns={columns.conversionVariableCost}
          rows={conversionVariableCost}
          setRows={setConversionVariableCost}
          title='B3.3. Conversion & Variable Cost'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          groupBy={'plant'}
        />
      )}

      {tabIndex === 16 && (
        <KendoDataTables
          columns={mcuCapacityUtilizationColumns}
          rows={mcuCapacityUtilizationState}
          setRows={setMcuCapacityUtilizationState}
          title='MCU Capacity Utilization (%)'
          permissions={adjustedPermission1}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
        />
      )}
    </div>
  )
}

export default SiteAOPReport
