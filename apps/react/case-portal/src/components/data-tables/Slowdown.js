import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'

const SlowDown = () => {
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [slowDownData, setSlowDownData] = useState([])
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
  const keycloak = useSession()
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
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

  const saveSlowDownData = async (newRow) => {
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      const slowDownDetails = newRow.map((row) => ({
        productId: row.product,
        discription: row.discription,
        // durationInHrs: parseFloat(findDuration('1', row)),
        durationInHrs: parseFloat(row.durationInHrs),
        maintEndDateTime: row.maintEndDateTime,
        maintStartDateTime: row.maintStartDateTime,
        remark: row.remarks,
        rate: row.rate,
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
      }))
      const response = await DataService.saveSlowdownData(
        plantId,
        slowDownDetails,
        keycloak,
      )
      //console.log('Slowdown data Saved Successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Slowdown data Saved Successfully !");
      setSnackbarData({
        message: 'Slowdown data Saved Successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Slowdown data Saved Successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
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
        saveSlowDownData(data)

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000) // Delay of 2 seconds
  }, [apiRef])
  const updateSlowdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInHrs: newRow.durationInHrs,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remarks,
        rate: newRow.rate,
      }

      const response = await DataService.updateSlowdownData(
        maintenanceId,
        slowDownDetails,
        keycloak,
      )
      //console.log('Slowdown data Updated successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Slowdown data Updated successfully !");
      setSnackbarData({
        message: 'Slowdown data Updated successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Slowdown data Updated successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
    } finally {
      fetchData()
    }
  }
  const fetchData = async () => {
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)

      // const data = [
      //   {
      //     discription: 'MEG slowdown',
      //     maintStartDateTime: null,
      //     maintEndDateTime: null,
      //     durationInHrs: null,
      //     rate: 20,
      //     remarks: 'MEG slowdown',
      //     product: '92E0AF06-9535-4B93-8998-E56A71354393',
      //     maintenanceId: 'f79675e1-2973-4844-90ff-6894b4b6bd72',
      //   },
      // ]
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.maintenanceId || item?.id,
        id: index,
      }))
      setSlowDownData(formattedData)
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
    }
  }
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        // console.log('API Response:', data);

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

    // const saveShutdownData = async () => {
    //   try {
    //     // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D';
    //     var plantId = ''

    //     const storedPlant = localStorage.getItem('selectedPlant')
    //     if (storedPlant) {
    //       const parsedPlant = JSON.parse(storedPlant)
    //       plantId = parsedPlant.id
    //     }

    //     const shutdownDetails = {
    //       product: 'Oxygen',
    //       discription: '1 Shutdown maintenance',
    //       durationInHrs: 120,
    //       maintEndDateTime: '2025-02-20T18:00:00Z',
    //       maintStartDateTime: '2025-02-20T16:00:00Z',
    //     }

    //     const response = await DataService.saveShutdownData(
    //       plantId,
    //       shutdownDetails,
    //       keycloak,
    //     )
    //     console.log('Shutdown data Saved Successfully:', response)
    //     return response
    //   } catch (error) {
    //     console.error('Error saving shutdown data:', error)
    //   }
    // }

    fetchData()
    // saveShutdownData()
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

  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.idFromApi ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId

      // console.log(maintenanceId, params, id)

      // Ensure UI state updates before the deletion process
      setOpen1(true)
      setDeleteId(id)

      // Perform the delete operation
      return await DataService.deleteSlowdownData(maintenanceId, keycloak)
    } catch (error) {
      console.error(`Error deleting Slowdown data:`, error)
    } finally {
      fetchData()
    }
  }

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Slowdown Desc',
      minWidth: 250,
      editable: true,
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          Slowdown Desc
        </div>
      ),
      flex: 3,
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
        // console.log('params valueFormatter ', params)
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
              border: 'none',
              outline: 'none',
              background: 'transparent',
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
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
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
      headerName: 'SD- To',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInHrs',
      headerName: 'Duration (hrs)',
      editable: true,
      minWidth: 100,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
      // valueGetter: findDuration,
    },

    {
      field: 'rate',
      headerName: 'Rate',
      editable: true,
      minWidth: 100,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remarks',
      headerName: 'Remarks',
      editable: true,
      minWidth: 200,
    },
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
        setRows={setRows}
        columns={colDefs}
        rows={rows}
        title='Slowdown Plan'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateSlowdownData={updateSlowdownData}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
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

export default SlowDown
