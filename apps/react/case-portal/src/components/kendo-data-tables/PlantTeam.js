import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

export default function PlantTeam() {
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
  //---
const [peopleInitiativeRows, setPeopleInitiativeRows] = useState([])
const [modifiedPeopleCells, setModifiedPeopleCells] = useState({})
const [remarkDialogOpenPeople, setRemarkDialogOpenPeople] = useState(false)
const [currentRemarkPeople, setCurrentRemarkPeople] = useState('')
const [currentRowIdPeople, setCurrentRowIdPeople] = useState(null)
//-
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
        title: 'S.No.',
        widthT: 70,
        editable: false,
        type: 'number',
      },
      {
        field: 'function',
        title: 'Function',

        editable: true,
      },
      {
        field: 'jobRole',
        title: 'Job Role',

        editable: true,
      },
      {
        field: 'name',
        title: 'Name',
        editable: true,
      },
      {
        field: 'age',
        title: 'Age',
        editable: true,
        type: 'number',
        widthT: 70,
      },
      {
        field: 'teamSize',
        title: 'Team Size',
        editable: true,
        type: 'number',
        widthT: 120,
      },
    ],
    [plantID, yearChanged],
  )

  const year = thisYear
  const peopleInitiativeColumns = [
  { field: 'serialNumber', title: 'S.No.', widthT: 70, editable: false, type: 'number' },
  { field: 'initiative', title: 'Initiative', editable: true },
  { field: 'outcome', title: 'Outcome', editable: true },
  { field: 'recommendation', title: 'Recommendation', editable: true },
  { field: 'targetDate', title: 'Target Date', editable: true, widthT: 120 },
  { field: 'responsible', title: 'Resp.', editable: true, widthT: 120 }
]

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      // var res = await DataService.getMonthWiseSummary(keycloak)
      var res = {
        code: 200,
        data: [
          {
            serialNumber: 1,
            function: 'Plant Head',
            jobRole: 'Plant Manager',
            name: '',
            age: '',
            teamSize: '',
          },
          {
            serialNumber: 2,
            function: 'Operations',
            jobRole: 'Production Manager',
            name: '',
            age: '',
            teamSize: '',
          },
          {
            serialNumber: 3,
            function: 'Maintenance',
            jobRole: 'Maintenance Manager',
            name: '',
            age: '',
            teamSize: '',
          },
          {
            serialNumber: 4,
            function: 'Safety',
            jobRole: 'Safety Engineer',
            name: '',
            age: '',
            teamSize: '',
          },
          {
            serialNumber: 5,
            function: 'CTS',
            jobRole: 'CTS Manager',
            name: '',
            age: '',
            teamSize: '',
          },
          {
            serialNumber: 6,
            function: 'HR',
            jobRole: 'HR BP',
            name: '',
            age: '',
            teamSize: '',
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
        peopleInitiative: [
        {
          serialNumber: 1,
          initiative: '',
          outcome: '',
          recommendation: '',
          targetDate: '',
          responsible: '',
          id: 0
        },
      ]
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
        const peopleInitiativeMapped = res?.peopleInitiative?.map((item, index) => ({
          ...item,
          id: index
        }))
        setRows(mapped)
        setRowsP(mapped1)
        setPeopleInitiativeRows(peopleInitiativeMapped)
      } else {
        setRows([])
        setRowsP([])
        setPeopleInitiativeRows([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
      setRowsP([])
      setPeopleInitiativeRows([])
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
      titleName: 'Plant Team (Size)',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Plant_Team`,
    },
    isOldYear,
  )
  const getAdjustedPermissionsPeople = (permissions, isOldYear) => {
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

  const peopleInitiativePermissions = getAdjustedPermissionsPeople({
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: 'People Initiative',
    adjustedPermissions: true,
  downloadExcelBtn: true,
  uploadExcelBtn: true,
  ExcelName: `${lowerVertName}_People_Initiative`,
})

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
      <KendoDataTables
  rows={peopleInitiativeRows}
  setRows={setPeopleInitiativeRows}
  title='People Initiative'
  modifiedCells={modifiedPeopleCells}
  setModifiedCells={setModifiedPeopleCells}
  remarkDialogOpen={remarkDialogOpenPeople}
  setRemarkDialogOpen={setRemarkDialogOpenPeople}
  currentRemark={currentRemarkPeople}
  setCurrentRemark={setCurrentRemarkPeople}
  currentRowId={currentRowIdPeople}
  setCurrentRowId={setCurrentRowIdPeople}
  permissions={peopleInitiativePermissions}
  columns={peopleInitiativeColumns}
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
        {...commonGridProps}
        groupBy='Particulars'
      /> */}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
