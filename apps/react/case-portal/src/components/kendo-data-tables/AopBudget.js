import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Select, MenuItem, Backdrop, Box, CircularProgress, Typography, Button } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { workbookOptions, toDataURL } from '@progress/kendo-react-excel-export'
import { ExcelExport, ExcelExportColumn } from '@progress/kendo-react-excel-export'
import { validateFields } from 'utils/validationUtils'
export default function AopBudget() {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')

  const [row, setRows] = useState([])
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
  const consumptionExportRef = useRef(null)
  const procurementExportRef = useRef(null)
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  // second grid states
  const [rowsP, setRowsP] = useState([])
  const [remarkDialogOpenP, setRemarkDialogOpenP] = useState(false)
  const [currentRemarkP, setCurrentRemarkP] = useState('')
  const [currentRowIdP, setCurrentRowIdP] = useState(null)
  const [modifiedCellsP, setModifiedCellsP] = useState({})
  const [enableSaveAddBtnP, setEnableSaveAddBtnP] = useState(false)
  const [rowsConsumption, setRowsConsumption] = useState([]);
  const [rowsProcurement, setRowsProcurement] = useState([]);
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
/*width: 120,
      type: 'number',
      format: '{0:#.###}',
      editable: false, */
const monthFields = [
  { field: 'apr', index: 4, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'may', index: 5, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'jun', index: 6, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'jul', index: 7, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'aug', index: 8, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'sep', index: 9, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'oct', index: 10, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'nov', index: 11, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'dec', index: 12, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'jan', index: 1, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'feb', index: 2, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
  { field: 'mar', index: 3, editable: true, type: 'number',format: '{0:#.###}',width: 120 },
]

// Custom cell renderer for month fields: green if >0, red if <0, black otherwise
const getMonthCell = (field) => (props) => {
  const value = props.dataItem[field];
  let color = 'black';
  if (value > 0) color = 'green';
  else if (value < 0) color = 'red';
  return <span style={{ color }}>{value}</span>;
};

const columns = [
  { field: 'plantName', title: 'Plant', width: 120,},
  { field: 'costName', title: 'Cost', width: 120,},
  { field: 'budgetType', title: 'Budget Type', width: 120,hidden: true},
  ...monthFields.map(({ field, index, editable, type, format, width }) => ({
    field,
    title: headerMap[index],
    editable,    // Make sure this is passed through
    type,
    format,
    width,
    cell: getMonthCell(field), // <-- custom cell renderer for color
  })),
  { field: 'remark', title: 'Remark', editable: true, width: 120 }, 
]
  const fetchData = useCallback(async () => {
  setLoading(true)
  try {
    const plantObject = JSON.parse(localStorage.getItem('selectedPlant'))
    const plantName = plantObject?.name

    // Fetch for Consumption Budget
    const resConsumption = await DataService.maintenacegetdata(keycloak, 'ConsumptionBudget')
    const mapped = (resConsumption?.data || []).map((item, index) => ({
  ...item,
  plantName: plantName || item.plantName || '',
  IsEditable: true,
  originalRemark: item.remark?.trim() || '', // add this
}));
setRows(mapped)

    // Fetch for Procurement Budget
    const resProcurement = await DataService.maintenacegetdata(keycloak, 'ProcurementBudget')
    const mappedP = (resProcurement?.data || []).map((item, index) => ({
  ...item,
  plantName: plantName || item.plantName || '',
  IsEditable: true
  originalRemark: item.remark?.trim() || '', // add this
}));
setRowsP(mappedP)
console.log('Consumption rows:', mapped);
console.log('Procurement rows:', mappedP);
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
}, [fetchData, yearChanged, plantID,keycloak])
  const year = thisYear

useEffect(() => { console.log('row', row); }, [row]);
useEffect(() => { console.log('rowsP', rowsP); }, [rowsP]);

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

  const handleCalculate = () => {}
  const handleCalculateP = () => {}

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  const handleRemarkCellClickP = useCallback((row) => {
    setCurrentRemarkP(row.remark || '')
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
      saveBtn: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Procurment Budget',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
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
      saveBtn: false,
      showTitleNameBusiness: true,

      titleName: 'Consumption Budget',

      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      ExcelName: `${lowerVertName}_Monthly Consumption Budget`,
    },
    isOldYear,
  )

 const handleCustomExport = () => {
  // Get workbook options from both refs
  const consumptionOptions = consumptionExportRef.current?.workbookOptions()
  const procurementOptions = procurementExportRef.current?.workbookOptions()

  if (!consumptionOptions?.sheets?.[0] && !procurementOptions?.sheets?.[0]) {
    alert('No data to export!')
    return
  }

  // Build a single sheet with both grids separated by a blank row and a title row
  const allRows = []

  if (consumptionOptions?.sheets?.[0]) {
    allRows.push({
      cells: [{ value: 'Consumption Budget', bold: true, fontSize: 14 }, ...columns.slice(1).map(() => ({ value: '' }))],
    })
    allRows.push(...consumptionOptions.sheets[0].rows)
    allRows.push({ cells: columns.map(() => ({ value: '' })) }) // blank row
  }

  if (procurementOptions?.sheets?.[0]) {
    allRows.push({
      cells: [{ value: 'Procurement Budget', bold: true, fontSize: 14 }, ...columns.slice(1).map(() => ({ value: '' }))],
    })
    allRows.push(...procurementOptions.sheets[0].rows)
  }

  const options = {
    sheets: [
      {
        title: 'AOP Budget',
        rows: allRows,
      },
    ],
  }

  toDataURL(options).then((dataURL) => {
    const link = document.createElement('a')
    link.href = dataURL
    link.download = `AopBudget.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  })
}
function omitFields(obj, fields) {
  const result = { ...obj }
  fields.forEach(field => {
    delete result[field]
  })
  return result
}
const handleSaveAll = async () => {
  setLoading(true)
  try {
    // Get modified rows for both grids
    const consumptionData = Object.values(modifiedCells)
    const procurementData = Object.values(modifiedCellsP)

    if (!consumptionData.length && !procurementData.length) {
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setSnackbarOpen(true)
      setLoading(false)
      return
    }
     const requiredFields = ['remark']
    const validationMessageC = validateFields(consumptionData, requiredFields)
    const validationMessageP = validateFields(procurementData, requiredFields)
    if (validationMessageC || validationMessageP) {
      setSnackbarData({ 
        message: validationMessageC || validationMessageP, 
        severity: 'error' 
      })
      setSnackbarOpen(true)
      setLoading(false)
      return
    }
    // Fields to omit from payload
    const fieldsToOmit = [
      'isEditable',
      'IsEditable'
    ]

    // Combine and clean all modified rows
    const allRows = [
      ...consumptionData.map(row => omitFields(row, fieldsToOmit)),
      ...procurementData.map(row => omitFields(row, fieldsToOmit))
    ]

    // Send as array payload
    await DataService.savemaintenacegetdata(allRows, keycloak)

    setSnackbarData({ message: 'Saved successfully!', severity: 'success' })
    setSnackbarOpen(true)
    setModifiedCells({})      // <-- clear modified cells for Consumption
    setModifiedCellsP({})
    fetchData()
  } catch (err) {
    setSnackbarData({ message: 'Save failed!', severity: 'error' })
    setSnackbarOpen(true)
  } finally {
    setLoading(false)
  }
}
  return (
    <Box>
      <div style={{ display: 'none' }}>
  <ExcelExport
    ref={consumptionExportRef}
    data={row}
    fileName="AopBudget.xlsx"
  >
    {columns.map(col => (
      <ExcelExportColumn key={col.field} field={col.field} title={col.title} />
    ))}
  </ExcelExport>
  <ExcelExport
    ref={procurementExportRef}
    data={rowsP}
    fileName="AopBudget.xlsx"
  >
    {columns.map(col => (
      <ExcelExportColumn key={col.field} field={col.field} title={col.title} />
    ))}
  </ExcelExport>
</div>
     <Box display="flex" justifyContent="flex-end" gap={2} mb={2}>
  <Button
    variant="contained"
    onClick={handleCustomExport}
    className="btn-save"
  >
    Export
  </Button>
  <Button
    variant="contained"
    onClick={handleSaveAll}
    className="btn-save"
    disabled={loading}
  >
    Save
  </Button>
</Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        title='Consumption Budget'
        titleMain='Monthly Budget'
        modifiedCells={modifiedCells}
        columns={columns}
        rows={row}
        setRows={setRows}
        fetchData={fetchData}
        setModifiedCells={setModifiedCells}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        enableSaveAddBtn={enableSaveAddBtn}
        // saveChanges={saveChanges}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={adjustedPermissionsC}
        groupBy='budgetType'
       
      />

      <KendoDataTables
        rows={rowsP}
        setRows={setRowsP}
        title='Procurement Budget'
        modifiedCells={modifiedCellsP}
        columns={columns}
        fetchData={fetchData}
        setModifiedCells={setModifiedCellsP}
        remarkDialogOpen={remarkDialogOpenP}
        setRemarkDialogOpen={setRemarkDialogOpenP}
        currentRemark={currentRemarkP}
        setCurrentRemark={setCurrentRemarkP}
        currentRowId={currentRowIdP}
        setCurrentRowId={setCurrentRowIdP}
        enableSaveAddBtn={enableSaveAddBtnP}
        // saveChanges={saveChangesP}
        handleCalculate={handleCalculateP}
        handleRemarkCellClick={handleRemarkCellClickP}
        permissions={adjustedPermissionsP}
        groupBy='budgetType'
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
