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
  const [slowDownData, setSlowDownData] = useState([])
  const [allProducts, setAllProducts] = useState([])
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
  const saveBusinessDemandData = async (newRows) => {
    try {
      console.log('saveBusiness API Called')
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

    console.log(values)
    // Calculate new average TPH
    const newAvgTph =
      values.length > 0
        ? values.reduce((sum, val) => sum + val, 0) / values.length
        : 0
    console.log(newAvgTph)
    // Update the avgTph value
    newRow.avgTph = newAvgTph
    setProductVolumeData((prevData) =>
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
  const fetchData = async () => {
    try {
      const data = await DataService.getAOPData(keycloak)
      const formattedData = data.map((item) => ({
        ...item,
        id: item.id,
      }))
      setProductNormData(formattedData)
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
    }
  }
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak])

  const productionColumns = [
    { field: 'id', headerName: 'ID' },
    {
      field: 'aopCaseId',
      headerName: 'Case ID',
      minWidth: 120,
      editable: false,
    },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },
    { field: 'aopYear', headerName: 'Year', minWidth: 80 },
    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },
    { field: 'normItem', headerName: 'Product', minWidth: 80, editable: false },

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
    { field: 'avgTph', headerName: 'AVG TPH', minWidth: 150, editable: false },
    { field: 'aopStatus', headerName: 'Remark', minWidth: 75, editable: false },
  ]

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
