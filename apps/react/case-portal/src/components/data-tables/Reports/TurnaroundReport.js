import React, { useState, useCallback, useEffect } from 'react'
import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import { truncateRemarks } from 'utils/remarksUtils'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const TurnaroundReport = () => {
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
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
            }}
            onClick={() => handleRemarkCellClick(params.row)}
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

  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()

  // const processRowUpdate = useCallback((newRow, oldRow) => {
  //   const rowId = newRow.id

  //   const updatedFields = []
  //   for (const key in newRow) {
  //     if (
  //       Object.prototype.hasOwnProperty.call(newRow, key) &&
  //       newRow[key] !== oldRow[key]
  //     ) {
  //       updatedFields.push(key)
  //     }
  //   }

  //   unsavedChangesRef.current = true
  //   setRows((prev) => prev.map((r) => (r.id === newRow.id ? newRow : r)))

  //   if (updatedFields.length > 0) {
  //     setModifiedCells((prevModifiedCells) => ({
  //       ...prevModifiedCells,
  //       [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
  //     }))
  //   }
  //   return newRow
  // }, [])
  // const processRowUpdate2 = useCallback((newRow) => {
  //   unsavedChangesRef.current = true
  //   setRows2((prev) => prev.map((r) => (r.id === newRow.id ? newRow : r)))
  //   return newRow
  // }, [])
  const [loading, setLoading] = useState(false)
  const keycloak = useSession()

  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  // useEffect(() => {
  //   const fetchData = async () => {
  //     try {
  //       setLoading(true)
  //       // let type =
  //       var res = await DataService.getTurnaroundReportData(
  //         keycloak,
  //         'currentYear',
  //       )
  //       var res2 = await DataService.getTurnaroundReportData(
  //         keycloak,
  //         'previousYear',
  //       )

  //       // console.log(res)
  //       if (res?.code == 200) {
  //         res = res?.data?.plantTurnAroundReportData.map((item, index) => ({
  //           ...item,
  //           id: index,
  //           isEditable: false,
  //         }))

  //         setRows(res)
  //       }
  //       if (res2?.code == 200) {
  //         res2 = res2?.data?.plantTurnAroundReportData.map((item, index) => ({
  //           ...item,
  //           id: index,
  //           isEditable: false,
  //         }))

  //         setRows2(res2)
  //       }
  //     } catch (err) {
  //       console.log(err)
  //     } finally {
  //       setLoading(false)
  //     }
  //   }
  //   fetchData()
  // }, [year, plantId])
  useEffect(() => {
    const mapData = (data, tag) =>
      (data?.data?.plantTurnAroundReportData || []).map((item, i) => ({
        ...item,
        id: `${tag}-${i}`,
        isEditable: false,
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

    fetchCurrentYear()
  }, [keycloak, year, plantId])

  useEffect(() => {
    const mapData = (data, tag) =>
      (data?.data?.plantTurnAroundReportData || []).map((item, i) => ({
        ...item,
        id: `${tag}-${i}`,
        isEditable: false,
      }))

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

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows2((prevRows) =>
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
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
          remarksEditable: true,
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        II. Turnaround details for the previous years since commissioning{' '}
      </Typography>
      <ReportDataGrid
        modifiedCells={modifiedCells}
        rows={rows2}
        setRows={setRows2}
        columns={columnsGrid2}
        columnGroupingModel={columnGroupingModel}
        processRowUpdate={processRowUpdate2}
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
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
          remarksEditable: true,
        }}
      />
    </Box>
  )
}
export default TurnaroundReport
