import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames()

const ProductionvolumeData = () => {
  const keycloak = useSession()
  const [productNormData, setProductNormData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const editAOPMCCalculatedData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteId = ''

      const storedSite = localStorage.getItem('selectedSite')
      if (storedSite) {
        const parsedSite = JSON.parse(storedSite)
        siteId = parsedSite.id
      }

      const aopmccCalculatedData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        august: row.august || null,
        september: row.september || null,
        october: row.october || null,
        november: row.november || null,
        december: row.december || null,
        january: row.january || null,
        february: row.february || null,
        march: row.march || null,

        aopStatus: row.aopStatus || 'draft',
        year: '2024-25',
        plant: plantId,
        plantFKId: plantId,
        site: siteId,
        material: getProductNameById(row.normParametersFKId),

        normParametersFKId: row.normParametersFKId,
        id: row.idFromApi || null,
      }))

      const response = await DataService.editAOPMCCalculatedData(
        plantId,
        aopmccCalculatedData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Business Demand data saved successfully!',
        severity: 'success',
      })
      // fetchData()
      return response
    } catch (error) {
      console.error('Error saving Business Demand data:', error)
    } finally {
      fetchData()
    }
  }
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    console.log(newRow)

    // Extract numeric values from month fields
    const months = [
      'jan',
      'feb',
      'march',
      'april',
      'may',
      'june',
      'july',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
    ]
    const values = months
      .map((month) => Number(newRow[month])) // Convert to number
      .filter((value) => !isNaN(value)) // Filter out NaN values

    // Calculate new average TPH and format it to 2 decimal places
    const newAvgTph =
      values.length > 0
        ? (values.reduce((sum, val) => sum + val, 0) / values.length).toFixed(2) // Format to 2 decimal places
        : '0.00' // Ensure consistent format

    // console.log(newAvgTph)

    // Update the avgTph value
    newRow.avgTph = parseFloat(newAvgTph) // Ensure it's still a number

    setProductNormData((prevData) =>
      prevData.map((row) => (row.id === rowId ? newRow : row)),
    )

    // Store edited row data
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
    try {
      // if (title === 'Business Demand') {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      editAOPMCCalculatedData(data)
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
  }, [apiRef])

  const getProductNameById = (productId) => {
    const lowerCaseProductId = productId?.toLowerCase()

    const foundProduct = allProducts.find(
      (p) => p.id.toLowerCase() === lowerCaseProductId,
    )

    if (foundProduct) {
      return foundProduct.name
    } else {
      console.warn(`Product with ID "${productId}" not found.`)
      return 'EOE'
    }
  }

  const fetchData = async () => {
    try {
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        normParametersFKId: item?.normParametersFKId.toLowerCase(),
        id: index,
      }))
      setProductNormData(formattedData)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(), // Convert id to lowercase
          displayName: product.displayName,
          name: product.name,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    { field: 'idFromApi', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },

    // normParametersFKId
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
    { field: 'avgTph', headerName: 'AVG TPH', minWidth: 150, editable: false },
    { field: 'aopStatus', headerName: 'Remark', minWidth: 75, editable: false },
  ]

  const handleRowEditStop = (params, event) => {
    setRowModesModel({
      ...rowModesModel,
      [params.id]: { mode: GridRowModes.View, ignoreModifications: false },
    })
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productNormData}
        title='Production Volume Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        // deleteId={deleteId}
        // setDeleteId={setDeleteId}
        // setOpen1={setOpen1}
        // open1={open1}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
        onRowEditStop={handleRowEditStop}
        onProcessRowUpdateError={onProcessRowUpdateError}
        experimentalFeatures={{ newEditingApi: true }}
        onCellEditStop={(params, event) => {
          event.defaultMuiPrevented = true
          if (
            params.reason === 'cellFocusOut' ||
            params.reason === 'escapeKeyDown'
          ) {
            const updatedRow = { ...params.row, [params.field]: params.value }
            processRowUpdate(updatedRow, params.row)
          }
        }}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          showRefreshBtn: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ProductionvolumeData
