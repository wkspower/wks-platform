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
  const saveBusinessDemandData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: row.april,
        may: row.may,
        june: row.june,
        july: row.july,
        aug: row.aug,
        sep: row.sep,
        oct: row.oct,
        nov: row.nov,
        dec: row.dec,
        jan: row.jan,
        feb: row.feb,
        march: row.march,
        remark: row.remark,
        avgTph: row.avgTph,
        year: '2024-25',
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi,
      }))

      const response = await DataService.saveBusinessDemandData(
        plantId,
        businessData, // Now sending an array of rows
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Business Demand data saved successfully!',
        severity: 'success',
      })
      return response
    } catch (error) {
      console.error('Error saving Business Demand data:', error)
    } finally {
      fetchData()
    }
  }
  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
    try {
      // if (title === 'Business Demand') {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      saveBusinessDemandData(data)
      // }

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
  }, [apiRef])
  const updateProductNormData = async (newRow) => {
    try {
      const productNormData = {
        id: newRow.id,
        aopType: newRow.aopType,
        aopCaseId: newRow.aopCaseId,
        aopStatus: newRow.aopStatus,
        aopYear: newRow.aopYear,
        plantFkId: newRow.plantFkId,
        normItem: newRow.normItem,
        april: newRow.april,
        may: newRow.may,
        june: newRow.june,
        july: newRow.july,
        aug: newRow.aug,
        sep: newRow.sep,
        oct: newRow.oct,
        nov: newRow.nov,
        dec: newRow.dec,
        jan: newRow.jan,
        feb: newRow.feb,
        march: newRow.march,
      }

      const response = await DataService.updateProductNormData(
        productNormData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Product Volume data updated successfully !',
        severity: 'success',
      })
      return response
    } catch (error) {
      console.error('Error Updating Product Volume data:', error)
    }
  }
  const fetchData = async () => {
    try {
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      // const data = await DataService.getProductionNormsData(keycloak)
      console.log(data)
      const formattedData = data.map((item) => ({
        ...item,
        id: item.id,
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
        // console.log('API Response:', data)

        // Extract only displayName and id
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
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
    { field: 'id', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },

    { field: 'material', headerName: 'Material', editable: false },
    { field: 'plant', headerName: 'Plant', editable: false },
    { field: 'site', headerName: 'Site', editable: false },

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
