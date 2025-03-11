import dayjs from 'dayjs'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import ASDataGrid from './ASDataGrid'

const TurnaroundPlanTable = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [TaData, setTaData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
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
  const keycloak = useSession()

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    // setHasUnsavedRows(true)
    return newRow
  }, [])

  const saveTurnAroundData = async (newRow) => {
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const turnAroundDetails = newRow.map((row) => ({
        productId: row.product,
        discription: row.discription,
        // durationInMins: parseFloat(findDuration('1', row)),
        durationInHrs: parseFloat(row.durationInHrs),
        maintEndDateTime: row.maintEndDateTime,
        maintStartDateTime: row.maintStartDateTime,
        remark: row.remark,
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
      }))

      const response = await DataService.saveTurnAroundData(
        plantId,
        turnAroundDetails,
        keycloak,
      )
      console.log(response)
      // if (response.ok && response.length < 0) {
      //   const errorData = await response.json() // Get the actual error message
      //   throw new Error(errorData.errorMessage || 'Failed to save data')
      // }
      // Check if response is empty or not defined
      if (!response || (Array.isArray(response) && response.length === 0)) {
        throw new Error('Failed to save data: No data returned')
      }

      // Optionally, if response has an 'ok' flag:
      if (response.ok === false) {
        const errorData = await response.json()
        throw new Error(errorData.errorMessage || 'Failed to save data')
      }

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Turnaround Plan data Saved Successfully!',
        severity: 'success',
      })
      return response
    } catch (error) {
      console.error('Error saving Turnaround Plan data:', error)

      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message, // Show exact error message from API
        severity: 'error',
      })
    } finally {
      fetchData()
    }
  }

  const saveChanges = React.useCallback(async () => {
    console.log(
      'Edited Data: ',
      Object.values(unsavedChangesRef.current.unsavedRows),
    )
    setTimeout(async () => {
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
    }, 1000) // Delay of 1 seconds
  }, [apiRef])

  const fetchData = async () => {
    try {
      const data = await DataService.getTAPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
      }))

      setTaData(formattedData)
      setRows(formattedData)
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
    fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak])

  const findDuration = (value, row) => {
    if (row && row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)

      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        // Check if dates are valid
        const durationInMs = end - start

        // Calculate duration in hours and minutes
        const durationInHours = Math.floor(durationInMs / (1000 * 60 * 60))
        const remainingMs = durationInMs % (1000 * 60 * 60)
        const durationInMinutes = Math.floor(remainingMs / (1000 * 60))

        // Format the duration as "HH:MM"
        const formattedDuration = `${String(durationInHours).padStart(2, '0')}:${String(durationInMinutes).padStart(2, '0')}`
        return formattedDuration
      } else {
        return '' // Or handle invalid dates as needed
      }
    } else {
      return '' // Or handle missing dates as needed
    }
  }

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
            {/* Disabled first option */}
            <option value='' disabled>
              Select
            </option>
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
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
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
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInMins',
      headerName: 'Duration (hrs)',
      editable: false,
      type: 'number',
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      valueGetter: findDuration,
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
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'TurnAround data Updated successfully!',
        severity: 'success',
      })
      return response
    } catch (error) {
      console.error('Error Updating TurnAround data:', error)
    } finally {
      fetchData()
    }
  }

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
        setRows={setRows}
        columns={colDefs}
        rows={rows}
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
        // handleDeleteClick={handleDeleteClick}
        onRowEditStop={handleRowEditStop}
        onProcessRowUpdateError={onProcessRowUpdateError}
        experimentalFeatures={{ newEditingApi: true }}
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
