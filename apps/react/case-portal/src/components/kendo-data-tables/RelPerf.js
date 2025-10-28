import React, { useEffect, useState } from 'react'
import KendoDataTables from './index'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { DataService } from 'services/DataService'
import { FunctionalApiService } from 'services/functional-api-service'
import { validateFields } from 'utils/validationUtils'
import moment from '../../../node_modules/moment/moment'
import {
  Backdrop,
  CircularProgress,
} from '../../../node_modules/@mui/material/index'

export default function RelPerf() {
  // Reliability Performance Grid (already present)
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)

  // Incidents
  // Improvement
  // Financial

  const [snackbarOpenIncidents, setSnackbarOpenIncidents] = useState(false)
  const [OpenIncidents, setOpenIncidents] = useState(false)

  const [snackbarDataIncidents, setSnackbarDataIncidents] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpenImprovement, setSnackbarOpenImprovement] = useState(false)
  const [OpenImprovement, setOpenImprovement] = useState(false)

  const [snackbarDataImprovement, setSnackbarDataImprovement] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpenFinancial, setSnackbarOpenFinancial] = useState(false)
  const [OpenFinancial, setOpenFinancial] = useState(false)

  const [snackbarDataFinancial, setSnackbarDataFinancial] = useState({
    message: '',
    severity: 'info',
  })

  const [
    snackbarOpenReliabilityPerformance,
    setSnackbarOpenReliabilityPerformance,
  ] = useState(false)
  const [OpenReliabilityPerformance, setOpenReliabilityPerformance] =
    useState(false)

  const [
    snackbarDataReliabilityPerformance,
    setSnackbarDataReliabilityPerformance,
  ] = useState({
    message: '',
    severity: 'info',
  })

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
  const [startYear, endYear] = AOP_YEAR.split('-')

  const [remarkDialogOpenMajorIncidents, setRemarkDialogOpenMajorIncidents] =
    useState(false)
  const [currentRemarkMajorIncidents, setCurrentRemarkMajorIncidents] =
    useState('')
  const [currentRowIdMajorIncidents, setCurrentRowIdMajorIncidents] =
    useState(null)

  const [reliabilityRows, setReliabilityRows] = useState([])

  const [modifiedReliabilityCells, setModifiedReliabilityCells] = useState({})
  const [modifiedFinancialCells, setModifiedFinancialCells] = useState({})
  const [modifiedMajorIncidentsCells, setModifiedMajorIncidentsCells] =
    useState({})
  const [
    modifiedReliabilityInitiativeCells,
    setModifiedReliabilityInitiativeCells,
  ] = useState({})

  const [remarkDialogOpenReliability, setRemarkDialogOpenReliability] =
    useState(false)
  const [currentRemarkReliability, setCurrentRemarkReliability] = useState('')
  const [currentRowIdReliability, setCurrentRowIdReliability] = useState(null)

  const [financialRows, setFinancialRows] = useState([])
  const [remarkDialogOpenFinancial, setRemarkDialogOpenFinancial] =
    useState(false)
  const [currentRemarkFinancial, setCurrentRemarkFinancial] = useState('')
  const [currentRowIdFinancial, setCurrentRowIdFinancial] = useState(null)

  // Major Reliability Incidents Grid
  const majorIncidentsColumns = [
    {
      field: 'incidentDescription',
      title: 'Incident Description',
      editable: true,
    },
    {
      field: 'rootCauseAnalysis',
      title: 'Root Cause Analysis',
      editable: true,
    },
    { field: 'recommendation', title: 'Recommendation', editable: true },
    { field: 'targetDate', title: 'Target Date', editable: true, type: 'date' },
    { field: 'responsible', title: 'Resp.', editable: true },
    { field: 'remarks', title: 'Remarks', editable: true },
  ]

  const [majorIncidentsRows, setMajorIncidentsRows] = useState([])

  const [reliabilityInitiativeRows, setReliabilityInitiativeRows] = useState([])

  const [
    remarkDialogOpenReliabilityInitiative,
    setRemarkDialogOpenReliabilityInitiative,
  ] = useState(false)
  const [
    currentRemarkReliabilityInitiative,
    setCurrentRemarkReliabilityInitiative,
  ] = useState('')
  const [
    currentRowIdReliabilityInitiative,
    setCurrentRowIdReliabilityInitiative,
  ] = useState(null)

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      const toDateObject = (value) =>
        value ? moment(value, 'MMM D, YYYY').toDate() : null
      var data = await FunctionalApiService.getReliabilityPerformance(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        'Reliability Performance',
      )

      const processedData1 = data.data.map((item, index) => ({
        ...item,
        originalRemark: item?.remarks || '',
      }))

      setReliabilityRows(processedData1)

      var data2 = await FunctionalApiService.getReliabilityPerformance(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        'Reliability Incident',
      )

      const processedData2 = data2.data.map((item, index) => ({
        ...item,
        originalRemark: item?.remarks || '',
      }))

      setFinancialRows(processedData2)

      var data3 = await FunctionalApiService.getReliabilityRecords(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        'Major Reliability Incident',
      )

      const processedData3 = data3.data.map((item, index) => ({
        ...item,
        originalRemark: item?.remarks || '',
        targetDate: toDateObject(item?.targetDate) || '',
      }))

      setMajorIncidentsRows(processedData3)

      var data4 = await FunctionalApiService.getReliabilityRecords(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        'Reliability Improvement Initiative',
      )

      var data5 = await FunctionalApiService.testMacro(
        keycloak,
        '20.15',
        PLANT_ID,
        AOP_YEAR,
      )

      console.log('macro', data5)

      const processedDatar = data4.data.map((item, index) => ({
        ...item,
        originalRemark: item?.remarks || '',
        targetDate: toDateObject(item?.targetDate) || '',
      }))

      setReliabilityInitiativeRows(processedDatar)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [plantID, oldYear, yearChanged, keycloak])

  const reliabilityPerformanceColumns = [
    {
      field: 'rowNo',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'parameter', title: 'Parameter', editable: false },
    { field: 'uom', title: 'UOM', editable: false, widthT: 70 },
    {
      field: 'bestAchieved',
      title: 'Best Achieved',
      editable: true,
      type: 'number',
    },
    {
      field: 'aop',
      title: `FY${startYear.slice(-2)} AOP`,
      editable: true,
      type: 'number',
    },
    {
      field: 'actual',
      title: `FY${startYear.slice(-2)} Actual`,
      editable: true,
      type: 'number',
    },
    {
      field: 'plann',
      title: `FY${endYear} Plan`,
      editable: true,
      type: 'number',
    },
    { field: 'limit', title: 'Limit', editable: true },
    {
      field: 'rationale',
      title: 'Rationale / Reasons for Changes',
      editable: true,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      editable: true,
    },
  ]

  const initialReliabilityRows = [
    {
      serialNumber: 1,
      parameter: 'Technical availability, YTD',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>99%',
      rationale: '',
      id: 0,
    },
    {
      serialNumber: 2,
      parameter: 'Maintenance Effectiveness',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>80%',
      rationale: '',
      id: 1,
    },
    {
      serialNumber: 3,
      parameter: 'Shutdown Schedule Compliance',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '100 ± 10%',
      rationale: '',
      id: 2,
    },
    {
      serialNumber: 4,
      parameter: 'Open NSD PM order backlog in weeks',
      uom: 'Weeks',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '4-6 weeks',
      rationale: '',
      id: 3,
    },
    {
      serialNumber: 5,
      parameter: 'Planned Jobs Schedule compliance',
      uom: '%',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '>96%',
      rationale: '',
      id: 4,
    },
    {
      serialNumber: 6,
      parameter: 'Inspection overdue (VitalEquipment)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 5,
    },
    {
      serialNumber: 7,
      parameter: 'Overdue reliability recommendations (NSD-APM)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 6,
    },
    {
      serialNumber: 8,
      parameter: 'Overdue IM Recommendations in E&M Discipline',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 7,
    },
    {
      serialNumber: '9.1',
      parameter: 'Total no. of Asset Failures - YTD (IM count)',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '20% reduction YOY',
      rationale: '',
      id: 8,
    },
    {
      serialNumber: '9.2',
      parameter: 'Repetitive failures - YTD',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '0',
      rationale: '',
      id: 9,
    },
    {
      serialNumber: 10,
      parameter: 'JMS approval time till A2',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '7 days',
      rationale: '',
      id: 10,
    },
    {
      serialNumber: 11,
      parameter: 'Pending GRN',
      uom: 'Nos',
      bestAchieved: '',
      fy25Aop: '',
      fy25Actual: '',
      fy26Plan: '7 days',
      rationale: '',
      id: 11,
    },
  ]

  // Financial Aspect Grid
  const financialAspectColumns = [
    {
      field: 'rowNo',
      title: 'S.No.',
      widthT: 70,
      editable: false,
      type: 'number',
    },
    { field: 'parameter', title: 'Parameter', editable: true },
    { field: 'uom', title: 'UOM', editable: false, widthT: 70 },
    {
      field: 'bestAchieved',
      title: 'Best Achieved',
      editable: true,
      type: 'number',
    },
    {
      field: 'aop',
      title: `FY${startYear.slice(-2)} AOP`,
      editable: true,
      type: 'number',
    },
    {
      field: 'actual',
      title: `FY${startYear.slice(-2)} Actual`,
      editable: true,
      type: 'number',
    },
    {
      field: 'plann',
      title: `FY${endYear} Plan`,
      editable: true,
      type: 'number',
    },
    // { field: 'limit', title: 'Limit', editable: true },

    {
      field: 'rationale',
      title: 'Rationale / Reasons for Changes',
      editable: true,
    },
    { field: 'remarks', title: 'Remarks', editable: true },
  ]

  // Reliability Improvement Initiative Grid
  const reliabilityInitiativeColumns = [
    { field: 'initiative', title: 'Initiative', editable: true },
    { field: 'outcome', title: 'Outcome', editable: true },
    { field: 'recommendation', title: 'Recommendation', editable: true },
    { field: 'targetDate', title: 'Target Date', editable: true, type: 'date' },
    { field: 'responsible', title: 'Resp.', editable: true },
    { field: 'remarks', title: 'Remarks', editable: true },
  ]

  // Permissions (reuse for all grids or customize per grid)
  const gridPermissions = {
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    adjustedPermissions: true,
  }

  const handleRemarkCellClickIncidents = (dataItem) => {
    setCurrentRemarkMajorIncidents(dataItem.remarks || '')
    setCurrentRowIdMajorIncidents(dataItem.id)
    setRemarkDialogOpenMajorIncidents(true)
  }
  const handleRemarkCellClickImprovement = (dataItem) => {
    setCurrentRemarkReliabilityInitiative(dataItem.remarks || '')
    setCurrentRowIdReliabilityInitiative(dataItem.id)
    setRemarkDialogOpenReliabilityInitiative(true)
  }
  const handleRemarkCellClickFinancial = (dataItem) => {
    setCurrentRemarkFinancial(dataItem.remarks || '')
    setCurrentRowIdFinancial(dataItem.id)
    setRemarkDialogOpenFinancial(true)
  }
  const handleRemarkCellClickReliabilityPerformance = (dataItem) => {
    setCurrentRemarkReliability(dataItem.remarks || '')
    setCurrentRowIdReliability(dataItem.id)
    setRemarkDialogOpenReliability(true)
  }

  const saveIncidents = async (newRows) => {
    try {
      const payloadData = newRows.map((row) => ({
        initiative: row?.initiative,
        outcome: row?.outcome,
        bestAchieved: row?.bestAchieved,
        id: row?.id,
        recommendation: row?.recommendation,
        responsible: row?.responsible,
        targetDate: row?.targetDate
          ? moment(row.targetDate).format('YYYY-MM-DD')
          : null,
        remarks: row?.remarks,
        rootCauseAnalysis: row?.rootCauseAnalysis,
        incidentDescription: row?.incidentDescription,
      }))

      const response = await FunctionalApiService.saveReliabilityRecords(
        payloadData,
        keycloak,
      )

      setSnackbarOpenIncidents(true)
      setSnackbarDataIncidents({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedMajorIncidentsCells({})

      fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
    } finally {
      // fetchData()
    }
  }
  const saveImprovement = async (newRows) => {
    try {
      const payloadData = newRows.map((row) => ({
        initiative: row?.initiative,
        outcome: row?.outcome,
        bestAchieved: row?.bestAchieved,
        id: row?.id,
        recommendation: row?.recommendation,
        responsible: row?.responsible,
        targetDate: row?.targetDate
          ? moment(row.targetDate).format('YYYY-MM-DD')
          : null,
        remarks: row?.remarks,
      }))

      const response = await FunctionalApiService.saveReliabilityRecords(
        payloadData,
        keycloak,
      )

      setSnackbarOpenImprovement(true)
      setSnackbarDataImprovement({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedReliabilityInitiativeCells({})

      fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
    } finally {
      // fetchData()
    }
  }
  const saveFinancial = async (newRows) => {
    try {
      const payloadData = newRows.map((row) => ({
        actual: row?.actual,
        aop: row?.aop,
        bestAchieved: row?.bestAchieved,
        id: row?.id,
        limit: row?.limit,
        plann: row?.plann,
        rationale: row?.rationale,
        remarks: row?.remarks,
      }))

      const response = await FunctionalApiService.saveReliabilityPerformance(
        payloadData,
        keycloak,
      )

      setSnackbarOpenFinancial(true)
      setSnackbarDataFinancial({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedFinancialCells({})

      fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
    } finally {
      // fetchData()
    }
  }
  const saveReliabilityPerformance = async (newRows) => {
    try {
      const payloadData = newRows.map((row) => ({
        actual: row?.actual,
        aop: row?.aop,
        bestAchieved: row?.bestAchieved,
        id: row?.id,
        limit: row?.limit,
        plann: row?.plann,
        rationale: row?.rationale,
        remarks: row?.remarks,
      }))

      const response = await FunctionalApiService.saveReliabilityPerformance(
        payloadData,
        keycloak,
      )

      setSnackbarOpenReliabilityPerformance(true)
      setSnackbarDataReliabilityPerformance({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedReliabilityCells({})

      fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
    } finally {
      // fetchData()
    }
  }

  const saveChangesReliabilityPerformance = React.useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedReliabilityCells).length === 0) {
        setSnackbarOpenReliabilityPerformance(true)
        setSnackbarDataReliabilityPerformance({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedReliabilityCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length == 0) {
        setSnackbarOpenReliabilityPerformance(true)
        setSnackbarDataReliabilityPerformance({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const requiredFields = ['remarks']
      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpenReliabilityPerformance(true)
        setSnackbarDataReliabilityPerformance({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      // ?? Additional validation for UOM = '%'
      const invalidPercentRows = data.filter((row) => {
        if (row?.uom === '%') {
          const fieldsToCheck = ['actual', 'aop', 'bestAchieved', 'plann']
          return fieldsToCheck.some((key) => {
            const value = parseFloat(row[key])
            return isNaN(value) || value < 1 || value > 100
          })
        }
        return false
      })

      if (invalidPercentRows.length > 0) {
        setSnackbarOpenReliabilityPerformance(true)
        setSnackbarDataReliabilityPerformance({
          message: 'For rows with UOM as %, values must be between 1 and 100.',
          severity: 'error',
        })
        setLoading(false)
        return
      }

      // ? Proceed to save if validation passes
      saveReliabilityPerformance(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedReliabilityCells])

  const saveChangesIncidents = React.useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedMajorIncidentsCells).length === 0) {
        setSnackbarOpenIncidents(true)
        setSnackbarDataIncidents({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedMajorIncidentsCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length == 0) {
        setSnackbarOpenIncidents(true)
        setSnackbarDataIncidents({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const requiredFields = ['remarks']

      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpenIncidents(true)
        setSnackbarDataIncidents({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      saveIncidents(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedMajorIncidentsCells])

  const saveChangesFinancial = React.useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedFinancialCells).length === 0) {
        setSnackbarOpenFinancial(true)
        setSnackbarDataFinancial({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedFinancialCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length == 0) {
        setSnackbarOpenFinancial(true)
        setSnackbarDataFinancial({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const requiredFields = ['remarks']
      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpenFinancial(true)
        setSnackbarDataFinancial({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      // ?? Additional validation for UOM = '%'
      const invalidPercentRows = data.filter((row) => {
        if (row?.uom === '%') {
          const fieldsToCheck = ['actual', 'aop', 'bestAchieved', 'plann']
          return fieldsToCheck.some((key) => {
            const value = parseFloat(row[key])
            return isNaN(value) || value < 1 || value > 100
          })
        }
        return false
      })

      if (invalidPercentRows.length > 0) {
        setSnackbarOpenFinancial(true)
        setSnackbarDataFinancial({
          message: 'For rows with UOM as %, values must be between 1 and 100.',
          severity: 'error',
        })
        setLoading(false)
        return
      }

      // ? Proceed to save if validation passes
      saveFinancial(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedFinancialCells])

  const saveChangesImprovement = React.useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedReliabilityInitiativeCells).length === 0) {
        setSnackbarOpenImprovement(true)
        setSnackbarDataImprovement({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedReliabilityInitiativeCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length == 0) {
        setSnackbarOpenImprovement(true)
        setSnackbarDataImprovement({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const requiredFields = ['remarks']

      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpenImprovement(true)
        setSnackbarDataImprovement({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      saveImprovement(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedReliabilityInitiativeCells])

  return (
    <>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Reliability Performance Grid */}
      <KendoDataTables
        rows={reliabilityRows}
        setRows={setReliabilityRows}
        title='Reliability Performance'
        modifiedCells={modifiedReliabilityCells}
        setModifiedCells={setModifiedReliabilityCells}
        remarkDialogOpen={remarkDialogOpenReliability}
        setRemarkDialogOpen={setRemarkDialogOpenReliability}
        currentRemark={currentRemarkReliability}
        setCurrentRemark={setCurrentRemarkReliability}
        currentRowId={currentRowIdReliability}
        setCurrentRowId={setCurrentRowIdReliability}
        snackbarData={snackbarDataReliabilityPerformance}
        snackbarOpen={snackbarOpenReliabilityPerformance}
        setSnackbarOpen={setSnackbarOpenReliabilityPerformance}
        setSnackbarData={setSnackbarDataReliabilityPerformance}
        setOpenReliabilityPerformance={setOpenReliabilityPerformance}
        handleRemarkCellClick={handleRemarkCellClickReliabilityPerformance}
        OpenReliabilityPerformance={OpenReliabilityPerformance}
        permissions={{
          ...gridPermissions,
          titleName: 'Reliability Performance',
          ExcelName: 'Reliability_Performance',
          downloadExcelBtn: true,
          uploadExcelBtn: true,
        }}
        columns={reliabilityPerformanceColumns}
        saveChanges={saveChangesReliabilityPerformance}
      />

      {/* Financial Aspect Grid */}
      <KendoDataTables
        rows={financialRows}
        setRows={setFinancialRows}
        title='Financial Aspect'
        modifiedCells={modifiedFinancialCells}
        setModifiedCells={setModifiedFinancialCells}
        remarkDialogOpen={remarkDialogOpenFinancial}
        setRemarkDialogOpen={setRemarkDialogOpenFinancial}
        currentRemark={currentRemarkFinancial}
        setCurrentRemark={setCurrentRemarkFinancial}
        currentRowId={currentRowIdFinancial}
        setCurrentRowId={setCurrentRowIdFinancial}
        permissions={{
          ...gridPermissions,
          titleName: 'Financial Aspect',
          ExcelName: 'Financial_Aspect',
        }}
        columns={financialAspectColumns}
        saveChanges={saveChangesFinancial}
        snackbarData={snackbarDataFinancial}
        snackbarOpen={snackbarOpenFinancial}
        setSnackbarOpen={setSnackbarOpenFinancial}
        setSnackbarData={setSnackbarDataFinancial}
        setOpenFinancial={setOpenFinancial}
        handleRemarkCellClick={handleRemarkCellClickFinancial}
        OpenFinancial={OpenFinancial}
      />

      {/* Major Reliability Incidents Grid */}
      <KendoDataTables
        rows={majorIncidentsRows}
        setRows={setMajorIncidentsRows}
        title={`Major Reliability Incidents FY${startYear.slice(-2)} (High & Medium Risks)`}
        modifiedCells={modifiedMajorIncidentsCells}
        setModifiedCells={setModifiedMajorIncidentsCells}
        remarkDialogOpen={remarkDialogOpenMajorIncidents}
        setRemarkDialogOpen={setRemarkDialogOpenMajorIncidents}
        currentRemark={currentRemarkMajorIncidents}
        setCurrentRemark={setCurrentRemarkMajorIncidents}
        currentRowId={currentRowIdMajorIncidents}
        setCurrentRowId={setCurrentRowIdMajorIncidents}
        permissions={{
          ...gridPermissions,
          titleName: `Major Reliability Incidents FY${startYear.slice(-2)} (High & Medium Risks)`,
          ExcelName: 'Major_Reliability_Incidents',
        }}
        columns={majorIncidentsColumns}
        saveChanges={saveChangesIncidents}
        snackbarData={snackbarDataIncidents}
        snackbarOpen={snackbarOpenIncidents}
        setSnackbarOpen={setSnackbarOpenIncidents}
        setSnackbarData={setSnackbarDataIncidents}
        setOpenIncidents={setOpenIncidents}
        handleRemarkCellClick={handleRemarkCellClickIncidents}
        OpenIncidents={OpenIncidents}
      />

      {/* Reliability Improvement Initiative Grid */}
      <KendoDataTables
        rows={reliabilityInitiativeRows}
        setRows={setReliabilityInitiativeRows}
        title='Reliability Improvement Initiative'
        modifiedCells={modifiedReliabilityInitiativeCells}
        setModifiedCells={setModifiedReliabilityInitiativeCells}
        remarkDialogOpen={remarkDialogOpenReliabilityInitiative}
        setRemarkDialogOpen={setRemarkDialogOpenReliabilityInitiative}
        currentRemark={currentRemarkReliabilityInitiative}
        setCurrentRemark={setCurrentRemarkReliabilityInitiative}
        currentRowId={currentRowIdReliabilityInitiative}
        setCurrentRowId={setCurrentRowIdReliabilityInitiative}
        permissions={{
          ...gridPermissions,
          titleName: 'Reliability Improvement Initiative',
          ExcelName: 'Reliability_Improvement_Initiative',
        }}
        columns={reliabilityInitiativeColumns}
        saveChanges={saveChangesImprovement}
        snackbarData={snackbarDataImprovement}
        snackbarOpen={snackbarOpenImprovement}
        setSnackbarOpen={setSnackbarOpenImprovement}
        setSnackbarData={setSnackbarDataImprovement}
        setOpenImprovement={setOpenImprovement}
        handleRemarkCellClick={handleRemarkCellClickImprovement}
        OpenImprovement={OpenImprovement}
      />
    </>
  )
}
