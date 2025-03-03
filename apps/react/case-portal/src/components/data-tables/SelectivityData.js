import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { useSelector } from 'react-redux'

const SelectivityData = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const [allCatalyst, setAllCatalyst] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
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
    const start = new Date(newRow.maintStartDateTime)
    const end = new Date(newRow.maintEndDateTime)
    const durationInMins = Math.floor((end - start) / (1000 * 60 * 60)) // Convert ms to Hrs
    // const durationInMins = Math.floor((end - start) / (1000 * 60)) // Convert ms to minutes

    console.log(`Duration in minutes: ${durationInMins}`)

    // Update the duration in newRow
    newRow.durationInMins = durationInMins.toFixed(2)
    // newRow.durationInMins = durationInMins
    setCsData((prevData) =>
      prevData.map((row) => (row.id === rowId ? newRow : row)),
    )

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
      // if (title === 'Business Demand') {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      saveCatalystData(data)
      // }

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
  }, [apiRef])
  const saveCatalystData = async (newRow) => {
    console.log('new Row ', newRow)

    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const turnAroundDetails = {
        april: newRow.apr24,
        may: newRow.may24,
        june: newRow.jun24,
        july: newRow.jul24,
        aug: newRow.aug24,
        sep: newRow.sep24,
        oct: newRow.oct24,
        nov: newRow.nov24,
        dec: newRow.dec24,
        jan: newRow.jan25,
        feb: newRow.feb25,
        march: newRow.mar25,
        TPH: '100',
        attributeName: 'Silver Ox',
        normParameterFKId: '',
        catalystAttributeFKId: 'C6352800-C64A-4944-B490-5A60D1BCE285',
        catalystId: '',
        remarks: '123',
        avgTPH: '123',
        year: 2024,
      }

      const response = await DataService.saveCatalystData(
        plantId,
        turnAroundDetails,
        keycloak,
      )
      //console.log('Catalyst data saved successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Catalyst data saved successfully !");
      setSnackbarData({
        message: 'Catalyst data saved successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Catalyst data saved successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Catalyst data:', error)
    } finally {
      fetchData()
    }
  }
  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.idFromApi ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId

      console.log(maintenanceId, params, id)

      // Ensure UI state updates before the deletion process
      setOpen1(true)
      setDeleteId(id)

      // Perform the delete operation
      return await DataService.deleteBusinessDemandData(maintenanceId, keycloak)
    } catch (error) {
      console.error(`Error deleting Configuration data:`, error)
    } finally {
      fetchData()
    }
  }
  const fetchData = async () => {
    try {
      const data = await DataService.getCatalystSelectivityData(keycloak)
      var formattedData = []
      if (data) {
        formattedData = data?.map((item, index) => ({
          ...item,
          id: index,
        }))
      }
      setCsData(formattedData)
    } catch (error) {
      console.error('Error fetching Turnaround data:', error)
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
        // handleMenuClose();
      }
    }
    const getAllCatalyst = async () => {
      try {
        const data = await DataService.getAllCatalyst(keycloak)

        const productList = data.map((product) => {
          console.log('Original ID:', product.id)
          return {
            id: product.id, // Should not change the case
            displayName: product.displayName,
          }
        })
        console.log('Mapped Product List:', productList)

        setAllCatalyst(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    getAllProducts()
    getAllCatalyst()
    fetchData()
  }, [sitePlantChange, keycloak])
  // Use catalyst options from the JSON file
  // const productOptions = catalystOptionsData.catalystOptions

  const productionColumns = [
    // {
    //   field: 'catalystId',
    //   headerName: 'Catalyst',
    //   editable: true,
    //   minWidth: 225,
    //   valueGetter: (params , params2) => {
    //     console.log('params ',params);
    //     return params || '';
    //   },
    //   valueFormatter: (params) => {
    //     const product = allCatalyst.find((p) => String(p.id).toUpperCase() === String(params));
    //     return product ? product.displayName : '';
    //   },
    //   renderEditCell: (params , params2) => {
    //     const { id, value } = params;
    //     return (
    //       <select
    //         value={value}
    //         onChange={(event) => {
    //           params.api.setEditCellValue({
    //             id: params.id,
    //             field: 'catalystId',
    //             value: event.target.value,
    //           });
    //         }}
    //         style={{
    //           width: '100%',
    //           padding: '5px',
    //           border: 'none',
    //           outline: 'none',
    //           background: 'transparent',
    //         }}
    //       >
    //         {allCatalyst.map((product) => (
    //           <option key={product.id} value={product.id}>
    //             {product.displayName}
    //           </option>
    //         ))}
    //       </select>
    //     );
    //   },
    // },

    {
      field: 'description',
      headerName: 'Description',
      editable: true,
      minWidth: 250,
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
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        title='Configuration'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        handleDeleteClick={handleDeleteClick}
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

export default SelectivityData
