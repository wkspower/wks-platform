import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

// import DataGridTable from 'components/data-tables/ASDataGrid'
// import { DataGrid } from '@mui/x-data-grid'
import {
  // Backdrop,
  Box,
  // Typography,
  // CircularProgress,
} from '../../../node_modules/@mui/material/index'
import { remarkColumn } from 'components/Utilities/remarkColumn'
// import ReportDataGrid from './ReportDataGrid'
import Notification from 'components/Utilities/Notification'
// import KendoDataTables from 'components/kendo-data-tables/index'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'

const ProductionAopView = ({
  handleCalculate,
  fetchSecondGridData,
  handleExport,
}) => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [columns, setColumns] = useState([])
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
    const SITE_ID = siteObject?.id
    const VERTICAL_ID = verticalObject?.id
    const VERTICAL_NAME = verticalObject?.name
    const AOP_YEAR = year?.selectedYear
    const vertName = verticalChange?.selectedVertical
    const lowerVertName = vertName?.toLowerCase() || 'meg'
  // remark dialog state
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const VALUE_FORMATOR = ValueFormatterProduction()

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const isOldYear = oldYear?.oldYear === 1
  const formatValueToNoDecimals = (val) =>
    val && !isNaN(val) ? Math.round(val) : val

  function getNumericKeysInAllRows1(data = []) {
    if (!Array.isArray(data) || data.length === 0) return []

    const keys = Object.keys(data[0])

    return keys.filter((key) =>
      data.every((row) => {
        const value = row[key]
        return value === '' || !isNaN(Number(value))
      }),
    )
  }

  function getNumericKeysInAllRows(rows = []) {
    if (!Array.isArray(rows) || rows.length === 0) return []

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
        if (row === null || typeof row !== 'object') return true
        const v = row[key]
        if (v === undefined || v === null || String(v).trim() === '')
          return true
        const n = Number(String(v).trim())
        return Number.isFinite(n)
      }),
    )
  }

  const handleRemarkCellClick = (row) => {
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
  const fetchData = async () => {
    if(!PLANT_ID || !AOP_YEAR) return
    try {
      var data = await DataService.getWorkflowDataProduction(keycloak, PLANT_ID, AOP_YEAR)
      var formattedRows = data.results.map((row, id) => {
        const newRow = { id }
        Object.entries(row).forEach(([key, val]) => {
          if (!isNaN(val) && val !== '') {
            newRow[key] = val
          } else {
            newRow[key] = val
          }
        })
        return newRow
      })

      formattedRows = formattedRows?.map((item) => ({
        ...item,
      }))

      setRows(formattedRows)

      const results = data?.results
      const numericKeys = getNumericKeysInAllRows(results)

      const generateColumns = ({ headers, keys }) => {
        const cols = headers.map((header, idx) => {
          const key = keys[idx]
          const isRemark = key === 'remark'
          return {
            field: key,
            headerName: header,
            editable: isRemark,
            flex: 1,
            ...(idx === 0 && {
              renderHeader: (params) => <div>{params.colDef.headerName}</div>,
            }),
            ...(numericKeys.includes(key) && {
              align: 'right',
              type: 'number',
              format: VALUE_FORMATOR,
            }),
          }
        })

        const remarkIdx = cols.findIndex((col) => col.field === 'remark')
        if (remarkIdx !== -1) {
          cols[remarkIdx] = remarkColumn(handleRemarkCellClick)
        }

        return cols
      }

      setColumns(generateColumns(data))
    } catch (error) {
      console.error('Error fetching data:', error)
      setRows([])
      setLoading(false)
    }
  }
  const handlecalcualteWithRefreshAll = () => {
    handleCalculate()
    fetchSecondGridData()
    fetchData()
  }

  useEffect(() => {
    fetchData()
  }, [plantID, yearChanged])

  // const lastColumnField = columns[columns.length - 1]?.field
  const saveChanges = async () => {
    try {
      // console.log(rows, 'workflowDto')
      await DataService.saveAnnualWorkFlowData(keycloak, rows, PLANT_ID)
      // console.log(response, 'response')
      setSnackbarData({
        message: 'Data Saved Successfully!',
        severity: 'success',
      })
      // setActionDisabled(true)
      // getCaseId()
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarData({ message: err.message, severity: 'error' })
      // setActionDisabled(false)
    } finally {
      setSnackbarOpen(true)
      // setOpenRejectDialog(false)
      // setText('')
    }
  }
  return (
    <Box
    // sx={{
    //   height: 'auto',
    //   width: '100%',
    //   padding: '0px 0px',
    //   margin: '0px 0px 0px',
    //   backgroundColor: '#F2F3F8',
    //   borderRadius: 0,
    //   borderBottom: 'none',
    // }}
    >
      {/* <Typography component='span' className='grid-title'>
        Production Data
      </Typography> */}

      {/* <Box> */}
      <KendoDataTablesReports
        rows={rows || []}
        loading={loading}
        setRows={setRows}
        title='Production Data'
        columns={columns}
        saveChanges={saveChanges}
        treeData
        getTreeDataPath={(rows) => rows.path}
        remarkDialogOpen={remarkDialogOpen}
        setModifiedCells={setModifiedCells}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        modifiedCells={modifiedCells}
        enableSaveAddBtn={enableSaveAddBtn}
        setEnableSaveAddBtn={setEnableSaveAddBtn}
        handleCalculate={handlecalcualteWithRefreshAll}
        handleRemarkCellClick={handleRemarkCellClick}
        handleExport={handleExport}
        permissions={{
          textAlignment: 'center',
          remarksEditable: !isOldYear,
          saveBtn: !isOldYear,
          allAction: !isOldYear,
          showCalculate: !isOldYear,
          showTitle: true,
        }}
      />
      {/* </Box> */}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default ProductionAopView
