import React from 'react'
import { Box } from '@mui/material'
import KendoDataTablesCrackerRunLength from './index-cracker-runlength'
import KendoDataTablesCrackerRunLengthNMD from './index-cracker-runlength-nmd'

const FurnaceRunLengthGridNMD = ({
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
  handleExcelUpload,
  downloadExcelForConfiguration,
  handleCalculate,
}) => {
  return (
    <Box sx={{ mt: 1 }}>
      <KendoDataTablesCrackerRunLengthNMD
        columns={columns}
        rows={rows}
        setRows={setRows}
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
        titleName='Furnace Actual and Proposed Runlength'
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        handleCalculate={handleCalculate}
      />
    </Box>
  )
}

export default FurnaceRunLengthGridNMD
