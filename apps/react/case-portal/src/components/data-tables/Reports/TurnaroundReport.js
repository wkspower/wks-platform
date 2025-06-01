import React, { useState, useEffect } from 'react'
import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import { truncateRemarks } from 'utils/remarksUtils'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'

const TurnaroundReport = () => {
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const unsavedChangesRefGrid2 = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCells2, setModifiedCells2] = React.useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()

  const [loading, setLoading] = useState(false)
  const keycloak = useSession()

  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClick2 = (row) => {
    setCurrentRemark2(row.remarks || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  const columns = [
    { field: 'sno', headerName: 'SL.No', flex: 1 },
    { field: 'activity', headerName: 'Activities', flex: 2 },
    { field: 'fromDate', headerName: 'From', flex: 2 },
    { field: 'toDate', headerName: 'To', flex: 2 },
    {
      field: 'durationInHrs',
      headerName: 'Duration, hrs',
      align: 'right',
      headerAlign: 'right',
      valueFormatter: formatValueToThreeDecimals,
      flex: 2,
    },
    // {
    //   field: 'periodInMonths',
    //   headerName: 'Period in Months',
    //   flex: 1,
    //   align: 'right',
    //   headerAlign: 'right',
    // },
    {
      field: 'remarks',
      headerName: 'Remark',
      flex: 2,
      renderCell: (params) => (
        <Tooltip title={params.value || ''} arrow>
          <div
            style={{
              cursor: 'pointer',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              width: ' 100%',
            }}
            onClick={() => handleRemarkCellClick(params.row)}
          >
            {truncateRemarks(params.value) || 'Click to add remark'}
          </div>
        </Tooltip>
      ),
    },
  ]

  const columnsGrid2 = [
    { field: 'sno', headerName: 'SL.No', flex: 1 },
    { field: 'activity', headerName: 'Activities', flex: 3 },
    { field: 'fromDate', headerName: 'From', flex: 2 },
    { field: 'toDate', headerName: 'To', flex: 2 },
    {
      field: 'durationInHrs',
      headerName: 'Duration, hrs',
      align: 'right',
      headerAlign: 'right',
      valueFormatter: formatValueToThreeDecimals,
      flex: 1,
    },
    {
      field: 'periodInMonths',
      headerName: 'Period in Months',
      flex: 1,
      align: 'right',
      headerAlign: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'remarks',
      headerName: 'Remark',
      flex: 2,
      renderCell: (params) => (
        <Tooltip title={params.value || ''} arrow>
          <div
            style={{
              cursor: 'pointer',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              width: ' 100%',
            }}
            onClick={() => handleRemarkCellClick2(params.row)}
          >
            {truncateRemarks(params.value) || 'Click to add remark'}
          </div>
        </Tooltip>
      ),
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'turnaround',
      headerName: 'Turnaround period',
      children: [{ field: 'fromDate' }, { field: 'toDate' }],
    },
  ]

  const mapData = (data, tag) =>
    (data?.data?.plantTurnAroundReportData || []).map((item, i) => ({
      ...item,
      id: `${tag}-${i}`,
      isEditable: false,
      remarks: item?.remarks ?? '',
    }))

  const fetchCurrentYear = async () => {
    setLoading(true)
    try {
      const res = await DataService.getTurnaroundReportData(
        keycloak,
        'currentYear',
      )
      if (res?.code === 200) {
        setRows(mapData(res, 'CY'))
      } else {
        setRows([])
      }
    } catch (e) {
      console.error('Error loading current year:', e)
    } finally {
      setLoading(false)
    }
  }

  const fetchPreviousYear = async () => {
    setLoading(true)
    try {
      const res = await DataService.getTurnaroundReportData(
        keycloak,
        'previousYear',
      )
      if (res?.code === 200) {
        setRows2(mapData(res, 'PY'))
      } else {
        setRows2([])
      }
    } catch (e) {
      console.error('Error loading previous year:', e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCurrentYear()
  }, [keycloak, year, plantId])

  useEffect(() => {
    fetchPreviousYear()
  }, [keycloak, year, plantId])

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

  const processRowUpdate2 = React.useCallback((newRow, oldRow) => {
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

    unsavedChangesRefGrid2.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRefGrid2.current.rowsBeforeChange[rowId]) {
      unsavedChangesRefGrid2.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows2((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    if (updatedFields.length > 0) {
      setModifiedCells2((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])

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
        remark: row.remarks,
      }))
      const res = await DataService.saveTurnaroundReport(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      // console.log(res)

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
  const saveRemarkDataForGrid2 = async () => {
    try {
      var data = Object.values(unsavedChangesRefGrid2.current.unsavedRows)
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
        remark: row.remarks,
      }))
      const res = await DataService.saveTurnaroundReport(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      // console.log(res)

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        unsavedChangesRefGrid2.current = {
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
      const res = await DataService.calculateTurnAroundPlanReportData(
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
        fetchCurrentYear()
        fetchPreviousYear()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }

      return res
    } catch (error) {
      // setSnackbarOpen(true)
      // setSnackbarData({
      //   message: error.message || 'An error occurred',
      //   severity: 'error',
      // })
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }
  return (
    <Box>
      <ReportDataGrid
        modifiedCells={modifiedCells}
        rows={rows}
        setRows={setRows}
        columns={columns}
        columnGroupingModel={columnGroupingModel}
        processRowUpdate={processRowUpdate}
        disableSelectionOnClick
        defaultGroupingExpansionDepth={1}
        remarkDialogOpen={remarkDialogOpen}
        unsavedChangesRef={unsavedChangesRef}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        loading={loading}
        title='Turnaround Details (T-19A)'
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: true,
          saveBtnForRemark: true,
          saveBtn: true,
          showWorkFlowBtns: true,
          showTitle: true,
        }}
        saveRemarkData={saveRemarkData}
        handleCalculate={handleCalculate}
      />

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Turnaround details for the previous years since commissioning{' '}
      </Typography>

      <ReportDataGrid
        modifiedCells={modifiedCells2}
        rows={rows2}
        setRows={setRows2}
        columns={columnsGrid2}
        columnGroupingModel={columnGroupingModel}
        processRowUpdate={processRowUpdate2}
        disableSelectionOnClick
        defaultGroupingExpansionDepth={1}
        remarkDialogOpen={remarkDialogOpen2}
        unsavedChangesRef={unsavedChangesRefGrid2}
        setRemarkDialogOpen={setRemarkDialogOpen2}
        currentRemark={currentRemark2}
        setCurrentRemark={setCurrentRemark2}
        currentRowId={currentRowId2}
        setCurrentRowId={setCurrentRowId2}
        saveRemarkData={saveRemarkDataForGrid2}
        loading={loading}
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
          remarksEditable: true,
          saveBtn: true,
          saveBtnForRemark: true,
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
export default TurnaroundReport
