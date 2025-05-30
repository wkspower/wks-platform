import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { truncateRemarks } from 'utils/remarksUtils'
import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'

const MonthwiseProduction = () => {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    // console.log(row)
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  const columns = [
    {
      field: 'RowNo',
      headerName: 'SL.No',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'Month',
      headerName: 'Month',
      flex: 1,
      headerAlign: 'left',
    },

    {
      field: 'EOEProdBudget', // was eoeBudgetCY
      headerName: 'Budget',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'EOEProdActual', // was eoeActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },

    // Current Year → Operating Hours
    {
      field: 'OpHrsBudget', // was opBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'OpHrsActual', // was opActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },

    // Current Year → Throughput
    {
      field: 'ThroughputBudget', // was thrBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'ThroughputActual', // was thrActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },

    // Budget Year single values
    {
      field: 'OperatingHours', // was opBudgetBY
      headerName: 'Operating Hours',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'MEGThroughput', // was megTPH
      headerName: 'MEG Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'EOThroughput', // was eoTPH
      headerName: 'EO Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'EOEThroughput', // was eoeTPH
      headerName: 'EOE Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'TotalEOE', // was totalEoeMT
      headerName: 'Total EOE, MT',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
    },

    // (Optional) you can keep Remarks if you plan to add that later
    {
      field: 'Remark',
      headerName: 'Remark',
      minWidth: 150,
      editable: false,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                width: ' 100%',
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'currentYear',
      headerName: oldYear,
      children: [
        {
          groupId: 'cy-eoe',
          headerName: 'EOE Production, MT',
          children: [
            { field: 'EOEProdBudget' }, // was eoeBudgetCY
            { field: 'EOEProdActual' }, // was eoeActualCY
          ],
        },
        {
          groupId: 'cy-op',
          headerName: 'Operating Hours',
          children: [
            { field: 'OpHrsBudget' }, // was opBudgetCY
            { field: 'OpHrsActual' }, // was opActualCY
          ],
        },
        {
          groupId: 'cy-thr',
          headerName: 'Throughput, TPH',
          children: [
            { field: 'ThroughputBudget' }, // was thrBudgetCY
            { field: 'ThroughputActual' }, // was thrActualCY
          ],
        },
      ],
    },
    {
      groupId: 'budgetYear',
      headerName: thisYear,
      children: [
        { field: 'OperatingHours' }, // was opBudgetBY
        { field: 'MEGThroughput' }, // was megTPH
        { field: 'EOThroughput' }, // was eoTPH
        { field: 'EOEThroughput' }, // was eoeTPH
        { field: 'TotalEOE' }, // was totalEoeMT
      ],
    },
  ]
  const [rows, setRows] = useState()

  const defaultCustomHeightGrid1 = {
    mainBox: `${15 + (rows?.length || 0) * 5}vh`,
    otherBox: `${100 + (rows?.length || 0) * 5}%`,
  }

  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getMonthWiseSummary(keycloak)
      if (res?.code == 200) {
        res = res?.data?.data.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
        }))

        setRows(res)
      } else {
        setRows([])
      }
    } catch (err) {
      console.log(err)
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    fetchData()
  }, [year, plantId])
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
  const defaultCustomHeight = { mainBox: '34vh', otherBox: '112%' }

  const saveRemarkData = async () => {
    try {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const rowsToUpdate = data.map((row) => ({
        id: row.Id,
        remark: row.Remark,
      }))
      const res = await DataService.saveMonthwiseProduction(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setSnackbarOpen(true)
    }
  }
  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const res = await DataService.handleCalculateMonthwiseProduction(
        plantId,
        year,
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }
      // fetchData()
      return res
    } catch (error) {
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }
  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ReportDataGrid
        rows={rows}
        setRows={setRows}
        title='Monthwise Production Plan'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeightGrid1,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: true,
          saveBtnForRemark: true,
          saveBtn: true,
          showWorkFlowBtns: true,
          showTitle: true,
        }}
        treeData
        getTreeDataPath={(rows) => rows.path}
        defaultGroupingExpansionDepth={1} // expand only first level by default
        disableSelectionOnClick
        columnGroupingModel={columnGroupingModel}
        processRowUpdate={processRowUpdate}
        remarkDialogOpen={remarkDialogOpen}
        unsavedChangesRef={unsavedChangesRef}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        modifiedCells={modifiedCells}
        saveRemarkData={saveRemarkData}
        handleCalculate={handleCalculate}

        // setSnackbarData={setSnackbarData}
        // setSnackbarOpen={setSnackbarOpen}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Main Products - Production for the budget year{' '}
      </Typography>
      <ProductionNorms
        permissions={{
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: false,
          showCalculate: false,
          customHeight: defaultCustomHeight,
          // dynamicGridHeight: true,
          needTotal: true,
        }}
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

export default MonthwiseProduction
