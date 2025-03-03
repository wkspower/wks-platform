import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'

const TurnaroundPlanTable = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [TaData, setTaData] = useState([])
  const [allProducts, setAllProducts] = useState([])
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

    setTaData((prevData) =>
      prevData.map((row) => (row.id === rowId ? newRow : row)),
    )

    // Extract numeric values from month fields

    // Store edited row data
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    // setHasUnsavedRows(true)
    return newRow
  }, [])

  const saveTurnAroundData = async (newRow) => {
    try {
      // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'
      var plantId = ''

      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const turnAroundDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remark,
        // rate: newRow.rate,
      }
      const response = await DataService.saveTurnAroundData(
        plantId,
        turnAroundDetails,
        keycloak,
      )
      //console.log('Turnaround Plan data saved successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Turnaround Plan data saved successfully !");
      setSnackbarData({
        message: 'Turnaround Plan data saved successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Turnaround Plan data saved successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Turnaround Plan data:', error)
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
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      saveTurnAroundData(data)

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
  }, [apiRef])
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
      return await DataService.deleteTurnAroundData(maintenanceId, keycloak)
    } catch (error) {
      console.error(`Error deleting Business data:`, error)
    } finally {
      fetchData()
    }
  }
  const fetchData = async () => {
    try {
      const data = await DataService.getTAPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        // ...item,
        // id: index,

        ...item,
        idFromApi: item.id,
        id: index,
      }))

      setTaData(formattedData)
    } catch (error) {
      console.error('Error fetching Turnaround data:', error)
    }
  }
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        console.log('API Response:', data)

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

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Activities',
      minWidth: 300,
      editable: true,
    },

    {
      field: 'maintenanceId',
      headerName: 'maintenanceId',
      editable: false,
      hide: true,
    },

    {
      field: 'product',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params) => {
        // console.log('p1', params);
        // console.log('p2', params2);
        return params || ''
      },
      valueFormatter: (params) => {
        console.log('params valueFormatter ', params)
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value } = params
        // console.log('q1', params);
        // console.log('q2', params2);
        return (
          <select
            value={value || allProducts[0]?.id}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: event.target.value,
              })
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none', // Removes border
              outline: 'none', // Removes focus outline
              background: 'transparent', // Keeps background clean
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

    {
      field: 'maintStartDateTime',
      headerName: 'TA- From',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'TA- To',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInMins',
      headerName: 'Duration (hrs)',
      editable: false,
      // type: 'number',
      minWidth: 100,
      // renderCell: (params) => {
      //   // const durationInHours = params.value ? (params.value / 60).toFixed(2) : "0.00";
      //   return `${params.value}`
      // },
    },
    {
      field: 'period',
      headerName: 'Periods (in months)',
      editable: true,
      minWidth: 120,
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: true,
      minWidth: 200,
    },
  ]

  const updateTurnAroundData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const turnAroundDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remark,
      }

      const response = await DataService.updateTurnAroundData(
        maintenanceId,
        turnAroundDetails,
        keycloak,
      )
      //console.log('TurnAround data Updated successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("TurnAround data Updated successfully !");
      setSnackbarData({
        message: 'TurnAround data Updated successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "TurnAround data Updated successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error Updating TurnAround data:', error)
    } finally {
      fetchData()
    }
  }
  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={TaData}
        title='Turnaround Plan'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateTurnAroundData={updateTurnAroundData}
        saveChanges={saveChanges}
        snackbarOpen={snackbarOpen}
        snackbarData={snackbarData}
        processRowUpdate={processRowUpdate}
        apiRef={apiRef}
        deleteId={deleteId}
        open1={open1}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleDeleteClick={handleDeleteClick}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default TurnaroundPlanTable
