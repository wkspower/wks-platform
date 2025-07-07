import React from 'react'
import { Box } from '@mui/material'
import KendoDataTablesCracker from './index-cracker.js'

const SDTAActivitiesGrid = ({
  columns,
  rows,
  setRows,
  fetchData,
  handleRemarkCellClick,
  remarkDialogOpen,
  currentRemark,
  setCurrentRemark,
  currentRowId,
  snackbarData,
  snackbarOpen,
  setSnackbarOpen,
  setSnackbarData,
  modifiedCells,
  allMonths,
  setModifiedCells,
  permissions,
  saveChanges,
  setRemarkDialogOpen,
}) => {
  return (
    <Box sx={{ mt: 1 }}>
      <KendoDataTablesCracker
        columns={columns}
        rows={rows}
        setRows={setRows}
        editable={true}
        editField="inEdit"
        fetchData={fetchData}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        allMonths={allMonths}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
        saveChanges={saveChanges}
        setRemarkDialogOpen={setRemarkDialogOpen}
        titleName='SD / TA Activities'
      />
    </Box>
  )
}

export default SDTAActivitiesGrid
