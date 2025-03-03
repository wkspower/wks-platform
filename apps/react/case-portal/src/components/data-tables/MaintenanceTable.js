import React, { useState } from 'react'
import ASDataGrid from './ASDataGrid'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'

const productionColumns = [
  {
    field: 'description',
    headerName: 'Description ( in Hrs )',
    editable: true,
    minWidth: 250,
    maxWidth: 300,
  },
  { field: 'apr24', headerName: 'Apr-24', editable: true },
  { field: 'may24', headerName: 'May-24', editable: true },
  { field: 'jun24', headerName: 'Jun-24', editable: true },
  { field: 'jul24', headerName: 'Jul-24', editable: true },
  { field: 'aug24', headerName: 'Aug-24', editable: true },
  { field: 'sep24', headerName: 'Sep-24', editable: true },
  { field: 'oct24', headerName: 'Oct-24', editable: true },
  { field: 'nov24', headerName: 'Nov-24', editable: true },
  { field: 'dec24', headerName: 'Dec-24', editable: true },
  { field: 'jan25', headerName: 'Jan-25', editable: true },
  { field: 'feb25', headerName: 'Feb-25', editable: true },
  { field: 'mar25', headerName: 'Mar-25', editable: true },

  // { field: 'remark', headerName: 'Remark', minWidth : 150, editable: true },
]

const productionData = [
  {
    id: 1,
    description: 'Total available hrs for the month',
    apr24: 720,
    may24: 720,
    jun24: 720,
    jul24: 720,
    aug24: 720,
    sep24: 720,
    oct24: 720,
    nov24: 720,
    dec24: 720,
    jan25: 720,
    feb25: 720,
    mar25: 720,
  },
  {
    id: 2,
    description: 'Shutdown',
    apr24: 50,
    may24: 40,
    jun24: 60,
    jul24: 55,
    aug24: 45,
    sep24: 50,
    oct24: 52,
    nov24: 48,
    dec24: 49,
    jan25: 50,
    feb25: 53,
    mar25: 51,
  },
  {
    id: 3,
    description: 'Non-shutdown operating hrs for the month',
    apr24: 670,
    may24: 680,
    jun24: 660,
    jul24: 665,
    aug24: 675,
    sep24: 670,
    oct24: 668,
    nov24: 672,
    dec24: 671,
    jan25: 670,
    feb25: 667,
    mar25: 669,
  },
  {
    id: 4,
    description: 'Average slowdown load, % of PVT',
    apr24: 85,
    may24: 80,
    jun24: 90,
    jul24: 88,
    aug24: 84,
    sep24: 86,
    oct24: 85,
    nov24: 87,
    dec24: 89,
    jan25: 88,
    feb25: 84,
    mar25: 85,
  },
  {
    id: 5,
    description: 'Slowdown load reduction as shutdown hrs equivalent',
    apr24: 30,
    may24: 35,
    jun24: 28,
    jul24: 32,
    aug24: 31,
    sep24: 33,
    oct24: 29,
    nov24: 30,
    dec24: 32,
    jan25: 31,
    feb25: 34,
    mar25: 33,
  },
  {
    id: 6,
    description: 'Effective operating hrs @ PVT',
    apr24: 640,
    may24: 645,
    jun24: 632,
    jul24: 633,
    aug24: 644,
    sep24: 637,
    oct24: 639,
    nov24: 642,
    dec24: 639,
    jan25: 641,
    feb25: 633,
    mar25: 636,
  },
  {
    id: 7,
    description: 'Machine oiling task',
    apr24: 100,
    may24: 105,
    jun24: 98,
    jul24: 110,
    aug24: 115,
    sep24: 120,
    oct24: 130,
    nov24: 135,
    dec24: 140,
    jan25: 145,
    feb25: 150,
    mar25: 155,
  },
  {
    id: 8,
    description: 'Gearbox maintenance check',
    apr24: 95,
    may24: 100,
    jun24: 105,
    jul24: 110,
    aug24: 115,
    sep24: 120,
    oct24: 125,
    nov24: 130,
    dec24: 135,
    jan25: 140,
    feb25: 145,
    mar25: 150,
  },
  {
    id: 9,
    description: 'Safety equipment check',
    apr24: 90,
    may24: 95,
    jun24: 100,
    jul24: 105,
    aug24: 110,
    sep24: 115,
    oct24: 120,
    nov24: 125,
    dec24: 130,
    jan25: 135,
    feb25: 140,
    mar25: 145,
  },
  {
    id: 10,
    description: 'Electrical panel audit',
    apr24: 135,
    may24: 140,
    jun24: 145,
    jul24: 150,
    aug24: 155,
    sep24: 160,
    oct24: 165,
    nov24: 170,
    dec24: 175,
    jan25: 180,
    feb25: 185,
    mar25: 190,
  },
]

const MaintenanceTable = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
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
    console.log(newRow)
    const start = new Date(newRow.maintStartDateTime)
    const end = new Date(newRow.maintEndDateTime)
    const durationInMins = Math.floor((end - start) / (1000 * 60 * 60)) // Convert ms to Hrs
    // const durationInMins = Math.floor((end - start) / (1000 * 60)) // Convert ms to minutes

    console.log(`Duration in minutes: ${durationInMins}`)

    // Update the duration in newRow
    newRow.durationInMins = durationInMins.toFixed(2)
    // newRow.durationInMins = durationInMins
    // setShutdownData((prevData) =>
    //   prevData.map((row) => (row.id === rowId ? newRow : row)),
    // )

    // Store edited row data
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    // setHasUnsavedRows(true)
    return newRow
  }, [])
  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
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
  }, [apiRef])
  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productionData}
        title='Maintenance Details'
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
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: false,
        }}
      />
    </div>
  )
}
export default MaintenanceTable
