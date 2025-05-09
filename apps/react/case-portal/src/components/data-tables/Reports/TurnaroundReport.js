import React, { useState, useCallback } from 'react'
import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import { truncateRemarks } from 'utils/remarksUtils'

const TurnaroundReport = () => {
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // 2️⃣ Click‐handler to launch the dialog
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // 1️⃣ Column definitions
  const columns = [
    { field: 'srNo', headerName: 'Sr No' },
    { field: 'activity', headerName: 'Activities', flex: 2 },
    { field: 'from', headerName: 'From' },
    { field: 'to', headerName: 'To' },
    {
      field: 'duration',
      headerName: 'Duration, hrs',
      align: 'right',
      headerAlign: 'right',
      // flex: ,
    },
    {
      field: 'remark',
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
    { field: 'srNo', headerName: 'Sr No' },
    { field: 'activity', headerName: 'Activities', flex: 2 },
    {
      field: 'duration',
      headerName: 'Duration, hrs',
      // flex: 1,
      align: 'right',
      headerAlign: 'right',
    },
    { field: 'from', headerName: 'From' },
    { field: 'to', headerName: 'To' },
    {
      field: 'periodMonths',
      headerName: 'Period in Months.',
      flex: 1,
      align: 'right',
      headerAlign: 'right',
    },
    {
      field: 'Remark',
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

  // 2️⃣ Grouping model for “Turnaround period”
  const columnGroupingModel = [
    {
      groupId: 'turnaround',
      headerName: 'Turnaround period',
      children: [{ field: 'from' }, { field: 'to' }],
    },
  ]

  // 3️⃣ Hard-coded rows JSON
  const grid1 = [
    {
      id: 1,
      srNo: 1,
      activity: 'Blowdown Prep',
      from: '06/01/2025',
      to: '06/01/2025',
      duration: 2,
      remark: 'OK',
    },
    {
      id: 2,
      srNo: 2,
      activity: 'Shutdown',
      from: '06/02/2025',
      to: '06/02/2025',
      duration: 3,
      remark: 'Pending',
    },
    {
      id: 3,
      srNo: 3,
      activity: 'Line Draining',
      from: '06/03/2025',
      to: '06/03/2025',
      duration: 4,
      remark: 'Check',
    },
    {
      id: 4,
      srNo: 4,
      activity: 'Mechanical Check',
      from: '06/04/2025',
      to: '06/04/2025',
      duration: 5,
      remark: 'OK',
    },
    {
      id: 5,
      srNo: 5,
      activity: 'Electrical Audit',
      from: '06/05/2025',
      to: '06/05/2025',
      duration: 6,
      remark: 'OK',
    },
  ]

  const grid2 = [
    {
      id: 1,
      srNo: 1,
      activity: 'Blowdown Prep',
      duration: 2,
      from: '06/01/2025',
      to: '06/01/2025',
      periodMonths: 12,
      Remark: 'OK',
    },
    {
      id: 2,
      srNo: 2,
      activity: 'Shutdown',
      duration: 3,
      from: '06/02/2025',
      to: '06/02/2025',
      periodMonths: 11,
      Remark: 'Pending',
    },
    {
      id: 3,
      srNo: 3,
      activity: 'Line Draining',
      duration: 4,
      from: '06/03/2025',
      to: '06/03/2025',
      periodMonths: 10,
      Remark: 'Check',
    },
    {
      id: 4,
      srNo: 4,
      activity: 'Mechanical Check',
      duration: 5,
      from: '06/04/2025',
      to: '06/04/2025',
      periodMonths: 9,
      Remark: 'OK',
    },
  ]
  const totalRows = [
    {
      id: 'total-critical',
      srNo: '',
      activity: 'Total turnaround duration (based on Critical activity)',
      from: '', // leave blank or you can span via custom render
      to: '',
      duration: 42, // your computed total
      periodMonths: '',
      Remark: '',
      isTotal: true, // flag so you can style it differently
    },
    {
      id: 'total-table15',
      srNo: '',
      activity: 'Total turnaround duration (based on table 15)',
      from: '',
      to: '',
      duration: 56, // another computed total
      periodMonths: '',
      Remark: '',
      isTotal: true,
    },
  ]

  const [rows, setRows] = useState(() => [...grid1, ...totalRows])
  const [rows2, setRows2] = useState(grid2)
  // 4️⃣ Handle cell edits
  const processRowUpdate = useCallback((newRow) => {
    unsavedChangesRef.current = true
    setRows((prev) => prev.map((r) => (r.id === newRow.id ? newRow : r)))
    return newRow
  }, [])
  const processRowUpdate2 = useCallback((newRow) => {
    unsavedChangesRef.current = true
    setRows2((prev) => prev.map((r) => (r.id === newRow.id ? newRow : r)))
    return newRow
  }, [])
  return (
    <Box>
      <ReportDataGrid
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
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        II. Turnaround details for the previous years since commissioning{' '}
      </Typography>
      <ReportDataGrid
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
        permissions={{
          customHeight: { mainBox: '32vh', otherBox: '100%' },
          textAlignment: 'center',
        }}
      />
    </Box>
  )
}
export default TurnaroundReport
