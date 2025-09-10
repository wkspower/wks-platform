import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

export default function AopBudget() {
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
        field: 'plant',
        title: 'Plant',
        widthT: 60,
        editable: false,
      },
      {
        field: 'Cost',
        title: 'Cost',
        widthT: 100,
        editable: false,
      },
      {
        field: 'April',
        title: headerMap[4],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'May',
        title: headerMap[5],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'June',
        title: headerMap[6],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'July',
        title: headerMap[7],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'August',
        title: headerMap[8],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'September',
        title: headerMap[9],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'October',
        title: headerMap[10],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'November',
        title: headerMap[11],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'December',
        title: headerMap[12],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'January',
        title: headerMap[1],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'February',
        title: headerMap[2],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      {
        field: 'March',
        title: headerMap[3],

        editable: true,
        type: 'number',
        format: '{0:#.###}',
      },
      { field: 'remarks', title: 'Remark', widthT: 100, editable: false },
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
          {
            Particulars: 'Routine',
            Cost: 'Service Cost',
            April: 1432,
            May: 1123,
            June: 1800,
            July: 1674,
            August: 1766,
            September: 1640,
            October: 1203,
            November: 933,
            December: 1880,
            January: 1922,
            February: 691,
            March: 1187,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'Routine',
            Cost: 'Total Cost',
            April: 5086,
            May: 4089,
            June: 5688,
            July: 3649,
            August: 3917,
            September: 3813,
            October: 4678,
            November: 4774,
            December: 5034,
            January: 3808,
            February: 4071,
            March: 4612,
            remarks: 'Total',
            isEditable: false,
          },

          {
            Particulars: 'One time',
            Cost: 'Material Cost',
            April: 1337,
            May: 1382,
            June: 1975,
            July: 932,
            August: 1866,
            September: 1291,
            October: 828,
            November: 1057,
            December: 1046,
            January: 1698,
            February: 1104,
            March: 1538,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'One time',
            Cost: 'Service Cost',
            April: 769,
            May: 1761,
            June: 1217,
            July: 1877,
            August: 1274,
            September: 1811,
            October: 1643,
            November: 989,
            December: 1242,
            January: 1963,
            February: 1695,
            March: 944,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'One time',
            Cost: 'Total Cost',
            April: 4061,
            May: 4041,
            June: 4349,
            July: 4629,
            August: 3787,
            September: 4731,
            October: 4081,
            November: 2735,
            December: 4014,
            January: 5625,
            February: 4429,
            March: 3902,
            remarks: 'Total',
            isEditable: false,
          },

          {
            Particulars: 'Shutdown',
            Cost: 'Material Cost',
            April: 1654,
            May: 1088,
            June: 1490,
            July: 1721,
            August: 1299,
            September: 1960,
            October: 1106,
            November: 1930,
            December: 1778,
            January: 1022,
            February: 1419,
            March: 1661,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'Shutdown',
            Cost: 'Service Cost',
            April: 1327,
            May: 1503,
            June: 943,
            July: 1868,
            August: 1724,
            September: 700,
            October: 1720,
            November: 1066,
            December: 1041,
            January: 1873,
            February: 1100,
            March: 1309,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'Shutdown',
            Cost: 'Total Cost',
            April: 4103,
            May: 4441,
            June: 4319,
            July: 5304,
            August: 3844,
            September: 4656,
            October: 4318,
            November: 4663,
            December: 4531,
            January: 3872,
            February: 3851,
            March: 3806,
            remarks: 'Total',
            isEditable: false,
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
          {
            Particulars: 'Routine',
            Cost: 'Total Cost',
            April: 3654,
            May: 2966,
            June: 3888,
            July: 1975,
            August: 2151,
            September: 2173,
            October: 3475,
            November: 3841,
            December: 3154,
            January: 1886,
            February: 3380,
            March: 3425,
            remarks: 'Total',
            isEditable: false,
          },

          {
            Particulars: 'One time',
            Cost: 'Material Cost',
            April: 1337,
            May: 1382,
            June: 1975,
            July: 932,
            August: 1866,
            September: 1291,
            October: 828,
            November: 1057,
            December: 1046,
            January: 1698,
            February: 1104,
            March: 1538,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'One time',
            Cost: 'Total Cost',
            April: 3292,
            May: 2280,
            June: 3132,
            July: 2753,
            August: 2513,
            September: 2920,
            October: 2438,
            November: 1746,
            December: 2772,
            January: 3662,
            February: 2734,
            March: 2958,
            remarks: 'Total',
            isEditable: false,
          },

          {
            Particulars: 'Shutdown',
            Cost: 'Material Cost',
            April: 1654,
            May: 1088,
            June: 1490,
            July: 1721,
            August: 1299,
            September: 1960,
            October: 1106,
            November: 1930,
            December: 1778,
            January: 1022,
            February: 1419,
            March: 1661,
            remarks: 'Regular service checks',
          },
          {
            Particulars: 'Shutdown',
            Cost: 'Total Cost',
            April: 2776,
            May: 2938,
            June: 3376,
            July: 3436,
            August: 2120,
            September: 3956,
            October: 2598,
            November: 3597,
            December: 3490,
            January: 1999,
            February: 2751,
            March: 2497,
            remarks: 'Total',
            isEditable: false,
          },
        ],
      }

      const plantObject = JSON.parse(localStorage.getItem('selectedPlant'))
      const plantName = plantObject?.name

      if (res?.code === 200) {
        const mapped = res?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
          plant: plantName,
        }))
        const mapped1 = res?.data1?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
          plant: plantName,
        }))
        setRows(mapped)
        setRowsP(mapped1)
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

      titleName: 'Consumption Budget',

      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Monthly Consumption Budget`,
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
        titleMain='Monthly Budget'
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
        groupBy='Particulars'
        {...commonGridProps}
      />

      <KendoDataTables
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
        {...commonGridProps}
        groupBy='Particulars'
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
