import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { truncateRemarks } from 'utils/remarksUtils'
import { validateFields } from 'utils/validationUtils'

import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'
import NumericInputOnly from 'utils/NumericInputOnly'
import { useSelector } from 'react-redux'

const MonthwiseProduction = () => {
  const keycloak = useSession()
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
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

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  let oldYear = ''
  if (AOP_YEAR && AOP_YEAR.includes('-')) {
    const [start, end] = AOP_YEAR.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const columns = [
    {
      field: 'RowNo',
      headerName: 'SL.No',
      editable: false,
      widthT: 50,
    },
    {
      field: 'Month',
      headerName: 'Month',
      editable: false,
    },

    {
      field: 'EOEProdBudget', // was eoeBudgetCY
      headerName: 'Budget',
      editable: false,
    },
    {
      field: 'EOEProdActual', // was eoeActualCY
      headerName: 'Actual',
      editable: false,
    },

    // Current Year → Operating Hours
    {
      field: 'OpHrsBudget', // was opBudgetCY
      headerName: 'Budget',
      editable: false,
    },
    {
      field: 'OpHrsActual', // was opActualCY
      headerName: 'Actual',
      editable: true,
    },

    // Current Year → Throughput
    {
      field: 'ThroughputBudget', // was thrBudgetCY
      headerName: 'Budget',
      editable: false,
    },
    {
      field: 'ThroughputActual', // was thrActualCY
      headerName: 'Actual',
      editable: false,
    },

    // Budget Year single values
    {
      field: 'OperatingHours', // was opBudgetBY
      headerName: 'Operating Hours',
      editable: false,
    },
    {
      field: 'MEGThroughput', // was megTPH
      headerName: 'Throughput, TPH',
      editable: false,
    },
    {
      field: 'EOThroughput', // was eoTPH
      headerName: 'EO Throughput, TPH',
      editable: false,
    },
    {
      field: 'EOEThroughput', // was eoeTPH
      headerName: 'EOE Throughput, TPH',
      editable: false,
    },
    {
      field: 'TotalEOE', // was totalEoeMT
      headerName: 'Total EOE, MT',
      editable: false,
    },

    // (Optional) you can keep Remarks if you plan to add that later
    {
      field: 'Remark',
      headerName: 'Remark',
      minWidth: 150,
      editable: false,
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'currentYear',
      headerName: oldYear,
      children: [
        {
          groupId: 'cy-eoe',
          headerName: 'Production, MT',
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

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getMonthWiseSummary(keycloak)
      if (res?.code == 200) {
        res = res?.data?.data.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
          originalRemark: item.Remark,
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
  }, [AOP_YEAR, PLANT_ID])
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
        ThroughputActual: row?.ThroughputActual,
      }))

      const res = await DataService.saveMonthwiseProduction(
        keycloak,
        rowsToUpdate,
        PLANT_ID,
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

      const res = await DataService.handleCalculateMonthwiseProduction(
        PLANT_ID,
        AOP_YEAR,
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
        title='Monthwise Production (T-16)'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeightGrid1,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: false,
          saveBtnForRemark: true,
          saveBtn: !isOldYear,
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
        enableSaveAddBtn={enableSaveAddBtn}
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
          saveBtn: !isOldYear,
          showCalculate: false,
          customHeight: defaultCustomHeight,
          // dynamicGridHeight: true,
          needTotal: true,
          roundOffDecimals: true,
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
