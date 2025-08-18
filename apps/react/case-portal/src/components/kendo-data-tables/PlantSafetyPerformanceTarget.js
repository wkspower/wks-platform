import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

export default function PlantSafetyPerformanceTarget() {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  // second grid states
  const [rowsP, setRowsP] = useState([])
  const [remarkDialogOpenP, setRemarkDialogOpenP] = useState(false)
  const [currentRemarkP, setCurrentRemarkP] = useState('')
  const [currentRowIdP, setCurrentRowIdP] = useState(null)
  const [modifiedCellsP, setModifiedCellsP] = useState({})
  const [enableSaveAddBtnP, setEnableSaveAddBtnP] = useState(false)

  const [rows3, setRows3] = useState([])

  const [remarkDialogOpen3, setRemarkDialogOpen3] = useState(false)
  const [currentRemark3, setCurrentRemark3] = useState('')
  const [currentRowId3, setCurrentRowId3] = useState(null)
  const [modifiedCells3, setModifiedCells3] = useState({})
  const [enableSaveAddBtn3, setEnableSaveAddBtn3] = useState(false)

  const [rows4, setRows4] = useState([])

  const [enableSaveAddBtn4, setEnableSaveAddBtn4] = useState(false)
  const [remarkDialogOpen4, setRemarkDialogOpen4] = useState(false)
  const [currentRemark4, setCurrentRemark4] = useState('')
  const [currentRowId4, setCurrentRowId4] = useState(null)
  const [modifiedCells4, setModifiedCells4] = useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = useRef({ unsavedRows: {}, rowsBeforeChange: {} })

  const oldYearLabel = useMemo(() => {
    if (!thisYear || !thisYear.includes('-')) return ''
    const [start, end] = thisYear.split('-').map(Number)
    return `${start - 1}-${(end - 1).toString().slice(-2)}`
  }, [thisYear])

  const verticalName = useMemo(() => {
    const stored = localStorage.getItem('selectedVertical')
    try {
      return stored ? JSON.parse(stored).name?.toLowerCase() : ''
    } catch (e) {
      return ''
    }
  }, [])

  const columns = useMemo(
    () => [
      {
        field: 'serialNumber',
        title: 'S.No',
        widthT: 70,
        editable: false,
      },
      {
        field: 'kpi',
        title: 'KPI',
        editable: true,
        widthT: 300,
      },
      {
        field: 'uom',
        title: 'UOM',
        widthT: 80,
        editable: true,
      },
      {
        field: 'bestAchived',
        title: 'Best Achived',

        editable: true,
      },
      {
        field: 'fyAop',
        title: 'FY25 AOP',

        editable: true,
      },
      {
        field: 'fyActual',
        title: 'FY25 Actual',

        editable: true,
      },
      {
        field: 'fyActual',
        title: 'FY26 Plan',

        editable: true,
      },

      { field: 'remarks', title: 'Remark', widthT: 220, editable: false },
    ],
    [plantID, yearChanged],
  )
  const columns4 = useMemo(
    () => [
      {
        field: 'serialNumber',
        title: 'S.No',
        widthT: 70,
        editable: false,
      },
      {
        field: 'initiative',
        title: 'Initiative',
        editable: true,
        widthT: 250,
      },
      {
        field: 'outcome',
        title: 'Outcome',
        editable: true,
      },
      {
        field: 'recommendation',
        title: 'Recommendation',
        editable: true,
      },
      {
        field: 'targetDate',
        title: 'Target Date',
        editable: true,
      },
      {
        field: 'responsible',
        title: 'Resp.',
        editable: true,
        widthT: 120,
      },
    ],
    [plantID, yearChanged],
  )

  const columns3 = useMemo(
    () => [
      {
        field: 'serialNumber',
        title: 'S.No',
        widthT: 70,
        editable: false,
      },
      {
        field: 'incidentDescription',
        title: 'Incident Description',
        editable: true,
        widthT: 250,
      },
      {
        field: 'rootCauses',
        title: 'Root Causes',
        editable: true,
      },
      {
        field: 'recommendation',
        title: 'Recommendation',
        editable: true,
      },
      {
        field: 'targetDate',
        title: 'Target Date',
        editable: true,
      },
      {
        field: 'responsible',
        title: 'Resp.',
        editable: true,
        widthT: 120,
      },
    ],
    [plantID, yearChanged],
  )

  const year = thisYear

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      // var res = await DataService.getMonthWiseSummary(keycloak)
      var res = {
        code: 200,
        data: [
          {
            serialNumber: 1,
            kpi: 'Fatality',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 2,
            kpi: 'LWC',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 3,
            kpi: 'Total Recordable Cases Frequency Rate (TRCFR)',
            uom: 'Rate',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 4,
            kpi: 'PSE Tier-1',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 5,
            kpi: 'PSE Tier-2',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 6,
            kpi: 'Process Fire',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 7,
            kpi: 'Non-Process Fire',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 8,
            kpi: 'Electrical Fire',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 9,
            kpi: 'Overdue Investigation',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
          {
            serialNumber: 10,
            kpi: 'Overdue IM Recommendation',
            uom: 'Nos',
            bestAchived: '',
            fyAop: '',
            fyActual: '',
            fy26Plan: '',
            remarks: '',
          },
        ],
        data1: [
          {
            Particulars: 'Routine',
            Cost: 'Material Cost',
            April: 1900,
            May: 1534,
            June: 1956,
            July: 887,
            August: 713,
            September: 647,
            October: 1875,
            November: 1942,
            December: 1510,
            January: 1287,
            February: 1398,
            March: 1944,
            remarks: 'Regular service checks',
          },
        ],

        data2: [
          {
            serialNumber: 1,
            incidentDescription: '',
            rootCauses: '',
            recommendation: '',
            targetDate: '',
            responsible: '',
          },
          {
            serialNumber: '',
            incidentDescription: '',
            rootCauses: '',
            recommendation: '',
            targetDate: '',
            responsible: '',
          },
        ],

        data3: [
          {
            serialNumber: 1,
            initiative: '',
            outcome: '',
            recommendation: '',
            targetDate: '',
            responsible: '',
          },
          {
            serialNumber: '',
            initiative: '',
            outcome: '',
            recommendation: '',
            targetDate: '',
            responsible: '',
          },
        ],
      }

      if (res?.code === 200) {
        const mapped = res?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
        }))
        const mapped1 = res?.data1?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
        }))
        const mapped3 = res?.data2?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
        }))
        const mapped4 = res?.data3?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
        }))
        setRows(mapped)
        setRowsP(mapped1)
        setRows3(mapped3)
        setRows4(mapped4)
      } else {
        setRows([])
        setRowsP([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
      setRowsP([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, yearChanged, plantID])

  useEffect(() => {
    fetchData()
  }, [fetchData, yearChanged, plantID])

  const saveChanges = useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCells)
      if (!data.length) {
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setSnackbarOpen(true)
        return
      }
      // save logic...
    } finally {
      setSnackbarOpen(true)
      setLoading(false)
    }
  }, [modifiedCells])

  const saveChangesP = useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCellsP)
      if (!data.length) {
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setSnackbarOpen(true)
        return
      }
      // save logic...
    } finally {
      setSnackbarOpen(true)
      setLoading(false)
    }
  }, [modifiedCellsP])

  const handleCalculate = () => {}
  const handleCalculateP = () => {}

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  const handleRemarkCellClickP = useCallback((row) => {
    setCurrentRemarkP(row.remarks || '')
    setCurrentRowIdP(row.id)
    setRemarkDialogOpenP(true)
  }, [])

  const getAdjustedPermissionsP = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
    }
  }
  const getAdjustedPermissions3 = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
    }
  }
  const getAdjustedPermissions4 = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
    }
  }

  const adjustedPermissionsP = getAdjustedPermissionsP(
    {
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Procurment Budget',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Monthly Procurment Budget`,
    },
    isOldYear,
  )
  const adjustedPermissions3 = getAdjustedPermissions3(
    {
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      titleName:
        'Major Incidents FY25 (Fatality, PSE Tier 1 & 2, LWC, High Severity, Process Fires)',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      // ExcelName: `${lowerVertName}_Monthly Procurment Budget`,
    },
    isOldYear,
  )
  const adjustedPermissions4 = getAdjustedPermissions4(
    {
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Safety Improvement Initiative',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      // ExcelName: `${lowerVertName}_Monthly Procurment Budget`,
    },
    isOldYear,
  )
  const getAdjustedPermissionsC = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
    }
  }

  const adjustedPermissionsC = getAdjustedPermissionsC(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Plant Safety Performance & Targets',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Plant Safety Performance & Targets`,
    },
    isOldYear,
  )

  const commonGridProps = {
    columns,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        rows={rows}
        setRows={setRows}
        title='Consumption Budget'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        enableSaveAddBtn={enableSaveAddBtn}
        saveChanges={saveChanges}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={adjustedPermissionsC}
        // groupBy='Particulars'
        {...commonGridProps}
      />

      {/* <KendoDataTables
        rows={rowsP}
        setRows={setRowsP}
        title='Procurement Budget'
        modifiedCells={modifiedCellsP}
        setModifiedCells={setModifiedCellsP}
        remarkDialogOpen={remarkDialogOpenP}
        setRemarkDialogOpen={setRemarkDialogOpenP}
        currentRemark={currentRemarkP}
        setCurrentRemark={setCurrentRemarkP}
        currentRowId={currentRowIdP}
        setCurrentRowId={setCurrentRowIdP}
        enableSaveAddBtn={enableSaveAddBtnP}
        saveChanges={saveChangesP}
        handleCalculate={handleCalculateP}
        handleRemarkCellClick={handleRemarkCellClickP}
        permissions={adjustedPermissionsP}
        columns={columns3}
      /> */}
      <KendoDataTables
        rows={rows3}
        setRows={setRows3}
        title='Procurement Budget'
        modifiedCells={modifiedCells3}
        setModifiedCells={setModifiedCells3}
        remarkDialogOpen={remarkDialogOpen3}
        setRemarkDialogOpen={setRemarkDialogOpen3}
        currentRemark={currentRemark3}
        setCurrentRemark={setCurrentRemark3}
        currentRowId={currentRowId3}
        setCurrentRowId={setCurrentRowId3}
        enableSaveAddBtn={enableSaveAddBtn3}
        // saveChanges={saveChanges3}
        // handleCalculate={handleCalculate3}
        // handleRemarkCellClick={handleRemarkCellClick3}
        permissions={adjustedPermissions3}
        columns={columns3}
      />

      <KendoDataTables
        rows={rows4}
        setRows={setRows4}
        title='Procurement Budget'
        modifiedCells={modifiedCells4}
        setModifiedCells={setModifiedCells4}
        remarkDialogOpen={remarkDialogOpen4}
        setRemarkDialogOpen={setRemarkDialogOpen4}
        currentRemark={currentRemark4}
        setCurrentRemark={setCurrentRemark4}
        currentRowId={currentRowId4}
        setCurrentRowId={setCurrentRowId4}
        enableSaveAddBtn={enableSaveAddBtn4}
        // saveChanges={saveChanges4}
        // handleCalculate={handleCalculate4}
        // handleRemarkCellClick={handleRemarkCellClick4}
        permissions={adjustedPermissions4}
        columns={columns4}
      />

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
