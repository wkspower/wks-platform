import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { DataService } from 'services/DataService'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'

const productionColumns = [
  {
    field: 'stock',
    headerName: 'Feed Stock',
    width: 150,
    editable: true,
  },
  {
    field: 'apr24',
    headerName: 'Apr-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'may24',
    headerName: 'May-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'jun24',
    headerName: 'Jun-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'jul24',
    headerName: 'Jul-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'aug24',
    headerName: 'Aug-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'sep24',
    headerName: 'Sep-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'oct24',
    headerName: 'Oct-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'nov24',
    headerName: 'Nov-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'dec24',
    headerName: 'Dec-24',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'jan25',
    headerName: 'Jan-25',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'feb25',
    headerName: 'Feb-25',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'mar25',
    headerName: 'Mar-25',
    width: 100,
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
]

const FeedStockAvailability = () => {
  const [productOptions, setProductOptions] = useState([])
  const [productionData, setProductionData] = useState([])
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
  useEffect(() => {
    getAllProducts()
  }, [])
  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak)
      console.log('API Response:', data)
      const products = data.map((item) => item.displayName || item.name || item)
      setProductOptions(products)
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }
  useEffect(() => {
    if (productOptions.length > 0) {
      const rows = productOptions.map((option, index) => ({
        id: index + 1,
        stock: option,
        apr24: Math.floor(Math.random() * 100),
        may24: Math.floor(Math.random() * 100),
        jun24: Math.floor(Math.random() * 100),
        jul24: Math.floor(Math.random() * 100),
        aug24: Math.floor(Math.random() * 100),
        sep24: Math.floor(Math.random() * 100),
        oct24: Math.floor(Math.random() * 100),
        nov24: Math.floor(Math.random() * 100),
        dec24: Math.floor(Math.random() * 100),
        jan25: Math.floor(Math.random() * 100),
        feb25: Math.floor(Math.random() * 100),
        mar25: Math.floor(Math.random() * 100),
      }))
      setProductionData(rows)
    }
  }, [productOptions])

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productionData}
        title='Feed Stock Availability'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[10, 20, 30]}
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
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default FeedStockAvailability
