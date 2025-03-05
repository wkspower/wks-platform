import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { DataService } from 'services/DataService'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

const productionColumns = [
  {
    field: 'stock',
    headerName: 'Feed Stock',
    width: 150,
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
    field: 'august',
    headerName: headerMap['aug'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'september',
    headerName: headerMap['sep'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'october',
    headerName: headerMap['oct'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'november',
    headerName: headerMap['nov'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'december',
    headerName: headerMap['dec'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'january',
    headerName: headerMap['jan'],
    editable: true,
    type: 'number',
    align: 'left',
    headerAlign: 'left',
  },
  {
    field: 'february',
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
    // console.log(newRow)
    const start = new Date(newRow.maintStartDateTime)
    const end = new Date(newRow.maintEndDateTime)
    const durationInMins = Math.floor((end - start) / (1000 * 60 * 60)) // Convert ms to Hrs
    // const durationInMins = Math.floor((end - start) / (1000 * 60)) // Convert ms to minutes

    // console.log(`Duration in minutes: ${durationInMins}`)

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
      // console.log('API Response:', data)
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
        april: Math.floor(Math.random() * 100),
        may: Math.floor(Math.random() * 100),
        june: Math.floor(Math.random() * 100),
        july: Math.floor(Math.random() * 100),
        august: Math.floor(Math.random() * 100),
        september: Math.floor(Math.random() * 100),
        october: Math.floor(Math.random() * 100),
        november: Math.floor(Math.random() * 100),
        december: Math.floor(Math.random() * 100),
        january: Math.floor(Math.random() * 100),
        february: Math.floor(Math.random() * 100),
        march: Math.floor(Math.random() * 100),
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
