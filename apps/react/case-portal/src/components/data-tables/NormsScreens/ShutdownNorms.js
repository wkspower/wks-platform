import React, { useState } from 'react'
import DataGridTable from '../ASDataGrid'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

const shutdownNormsColumns = [
  {
    field: 'srNo',
    headerName: 'Sr. No',
    minWidth: 50,
    maxWidth: 70,
    editable: false,
  },
  { field: 'particular', headerName: 'Particular', width: 150, editable: true },
  {
    field: 'unit',
    headerName: 'Unit',
    minWidth: 50,
    editable: true,
  },
  {
    field: 'norms',
    headerName: 'Norms',
    minWidth: 50,
    editable: true,
  },
  {
    field: 'april',
    headerName: headerMap['apr'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'may',
    headerName: headerMap['may'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'june',
    headerName: headerMap['jun'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'july',
    headerName: headerMap['jul'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'aug',
    headerName: headerMap['aug'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'sep',
    headerName: headerMap['sep'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'oct',
    headerName: headerMap['oct'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'nov',
    headerName: headerMap['nov'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'dec',
    headerName: headerMap['dec'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'jan',
    headerName: headerMap['jan'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'feb',
    headerName: headerMap['feb'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'march',
    headerName: headerMap['mar'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
]

const shutdownNormsData = [
  { id: 1, srNo: 1, particular: 'Equipment A', unit: 'Hours', norms: 10 },
  { id: 2, srNo: 2, particular: 'Equipment B', unit: 'Days', norms: 2 },
  { id: 3, srNo: 3, particular: 'Material C', unit: 'Kg', norms: 50 },
  { id: 4, srNo: 4, particular: 'Tool D', unit: 'Pcs', norms: 5 },
  { id: 5, srNo: 5, particular: 'Machine E', unit: 'Hours', norms: 20 },
  { id: 6, srNo: 6, particular: 'Component F', unit: 'Litres', norms: 15 },
  { id: 7, srNo: 7, particular: 'System G', unit: 'Units', norms: 3 },
  { id: 8, srNo: 8, particular: 'Gear H', unit: 'Sets', norms: 8 },
  { id: 9, srNo: 9, particular: 'Pump I', unit: 'Hours', norms: 12 },
  { id: 10, srNo: 10, particular: 'Sensor J', unit: 'Pcs', norms: 6 },
]

const ShutdownNorms = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState(shutdownNormsData)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const keycloak = useSession()
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])
  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
    setTimeout(async () => {
      try {
        // var data = Object.values(unsavedChangesRef.current.unsavedRows)
        // saveShutdownData(data)

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
    }, 1000) // Delay of 2 seconds
  }, [apiRef])
  return (
    <div>
      <DataGridTable
        columns={shutdownNormsColumns}
        rows={rows}
        setRows={setRows}
        title='Shutdown Norms'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        // deleteId={deleteId}
        open1={open1}
        // setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        // handleDeleteClick={handleDeleteClick}
        // fetchData={fetchData}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ShutdownNorms
