import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ASDataGrid from './ASDataGrid'
const headerMap = generateHeaderNames()

import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
const ProductionNorms = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
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

    console.log(newAvgTph)

    // Update the avgTph value
    newRow.avgTph = parseFloat(newAvgTph) // Ensure it's still a number

    setCsData((prevData) =>
      prevData.map((row) => (row.id === rowId ? newRow : row)),
    )

    // Store edited row data
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    // onRowUpdate.updatedRow(unsavedChangesRef.current.unsavedRows)
    console.log(unsavedChangesRef.current.unsavedRows)

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
      updateProductNormData(data)
      // }

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

  const updateProductNormData = async (newRow) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const productNormData = newRow.map((row) => ({
        aopType: row.aopType || 'production',
        aopCaseId: row.aopCaseId || null,
        aopStatus: row.aopStatus || null,
        aopYear: '2024-25',
        plantFkId: plantId,

        normParametersFKId: row.normParametersFKId,

        normItem: getProductNameById(row.normParametersFKId),

        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        march: row.march || null,
        avgTPH: row.avgTPH || null,
        aopRemarks: row.aopRemarks || 'remarks',

        id: row.idFromApi || null,
      }))

      const response = await DataService.updateProductNormData(
        productNormData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Product Volume data saved successfully !',
        severity: 'success',
      })
      return response
    } catch (error) {
      console.error('Error Saving Product Volume data:', error)
    }
  }
  const fetchData = async () => {
    try {
      const data = await DataService.getAOPData(keycloak)

      // const data = await DataService.getProductionNormsData(keycloak)
      // console.log(data)
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        normParametersFKId: item?.normParametersFKId.toLowerCase(),
        id: index,
      }))
      setCsData(formattedData)
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
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

    fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    { field: 'idFromApi', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },

    { field: 'aopYear', headerName: 'Year', minWidth: 80 },

    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },

    // normParametersFKId
    {
      field: 'normParametersFKId',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params) => {
        return params || ''
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'normParametersFKId',
                value: event.target.value,
              })
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',
              outline: 'none',
              background: 'transparent',
            }}
          >
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        )
      },
    },

    { field: 'april', headerName: headerMap['apr'], editable: true },
    { field: 'may', headerName: headerMap['may'], editable: true },
    { field: 'june', headerName: headerMap['jun'], editable: true },
    { field: 'july', headerName: headerMap['jul'], editable: true },

    { field: 'aug', headerName: headerMap['aug'], editable: true },
    { field: 'sep', headerName: headerMap['sep'], editable: true },
    { field: 'oct', headerName: headerMap['oct'], editable: true },

    { field: 'nov', headerName: headerMap['nov'], editable: true },
    { field: 'dec', headerName: headerMap['dec'], editable: true },
    { field: 'jan', headerName: headerMap['jan'], editable: true },
    { field: 'feb', headerName: headerMap['feb'], editable: true },
    { field: 'march', headerName: headerMap['mar'], editable: true },
    {
      field: 'averageTPH',
      headerName: 'AVG TPH',
      minWidth: 150,
      editable: false,
    },
    {
      field: 'aopRemarks',
      headerName: 'Remark',
      minWidth: 75,
      editable: false,
    },
    { field: 'aopStatus', headerName: 'aopStatus', editable: false },
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
        rows={csData}
        title='Production AOP'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateProductNormData={updateProductNormData}
        processRowUpdate={processRowUpdate}
        onRowEditStop={handleRowEditStop}
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
        onProcessRowUpdateError={onProcessRowUpdateError}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          showCalculate: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ProductionNorms
