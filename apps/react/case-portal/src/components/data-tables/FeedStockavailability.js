// import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
// import { DataService } from 'services/DataService'
import React, { useState } from 'react'
// import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import NumericInputOnly from 'utils/NumericInputOnly'
import getEnhancedColDefs from './CommonHeader/feedstockHeaders'

const FeedStockAvailability = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  // const [productOptions, setProductOptions] = useState([])
  // const [productionData, setProductionData] = useState([])
  // const dataGridStore = useSelector((state) => state.dataGridStore)
  // const { sitePlantChange } = dataGridStore
  const [open1, setOpen1] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({})
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  // const keycloak = useSession()

  const productionColumns = getEnhancedColDefs({
    // allProducts,
    headerMap,
  })

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
    setEnableSaveAddBtn(true)
  }

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
  const saveChanges = React.useCallback(async () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
    // console.log(
    //   'Edited Data: ',
    //   Object.values(unsavedChangesRef.current.unsavedRows),
    // )
    setTimeout(async () => {
      try {
        // var data = Object.values(unsavedChangesRef.current.unsavedRows)
        // saveShutdownData(data)
        //  // Validation: Check if there are any rows to save
        //  if (data.length === 0) {
        //   setSnackbarOpen(true)
        //   setSnackbarData({
        //     message: 'No Records to Save!',
        //     severity: 'info',
        //   })
        //   return
        // }

        // // Validate that both normParameterId and remark are not empty
        // const invalidRows = data.filter(
        //   (row) => !row.normParametersFKId.trim() || !row.remark.trim(),
        // )

        // if (invalidRows.length > 0) {
        //   setSnackbarOpen(true)
        //   setSnackbarData({
        //     message: 'Please fill required fields: Product and Remark.',
        //     severity: 'error',
        //   })
        //   return
        // }
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000) // Delay of 2 seconds
  }, [apiRef, rowModesModel])
  // useEffect(() => {
  //   // getAllProducts()
  // }, [])
  // const getAllProducts = async () => {
  //   try {
  //     const data = await DataService.getAllProducts(keycloak, 'Consumption')
  //     // console.log('API Response:', data)
  //     const products = data.map((item) => item.displayName || item.name || item)
  //     // setProductOptions(products)
  //   } catch (error) {
  //     console.error('Error fetching product:', error)
  //   }
  // }
  // useEffect(() => {
  //   // if (productOptions.length > 0) {
  //   //   const rows = productOptions.map((option, index) => ({
  //   //     id: index + 1,
  //   //     stock: option,
  //   //     april: Math.floor(Math.random() * 100),
  //   //     may: Math.floor(Math.random() * 100),
  //   //     june: Math.floor(Math.random() * 100),
  //   //     july: Math.floor(Math.random() * 100),
  //   //     august: Math.floor(Math.random() * 100),
  //   //     september: Math.floor(Math.random() * 100),
  //   //     october: Math.floor(Math.random() * 100),
  //   //     november: Math.floor(Math.random() * 100),
  //   //     december: Math.floor(Math.random() * 100),
  //   //     january: Math.floor(Math.random() * 100),
  //   //     february: Math.floor(Math.random() * 100),
  //   //     march: Math.floor(Math.random() * 100),
  //   //   }))
  //     // setProductionData(rows)
  //   }
  // }, [productOptions])

  return (
    <div>
      <ASDataGrid
        modifiedCells={modifiedCells}
        enableSaveAddBtn={enableSaveAddBtn}
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        title='Feed Stock Availability'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[10, 20, 30]}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
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
          showUnit: true,
          units: ['TPH', 'TPD'],
          saveWithRemark: false,
          saveBtn: false,
        }}
      />
    </div>
  )
}

export default FeedStockAvailability
