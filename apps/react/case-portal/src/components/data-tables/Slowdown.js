import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { truncateRemarks } from 'utils/remarksUtils'
import { validateFields } from 'utils/validationUtils'

const SlowDown = ({ permissions }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  // const [slowDownData, setSlowDownData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState()
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const keycloak = useSession()
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
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

  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }
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
        durationInHrs:
          lowerVertName === 'meg'
            ? parseFloat(row.durationInHrs)
            : parseFloat(findDuration('1', row)),
        // durationInHrs: parseFloat(row.durationInHrs),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        remark: row.remark,
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
    try {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = [
        'maintStartDateTime',
        'maintEndDateTime',
        'discription',
        'remark',
        'rate',
        'durationInHrs',
        'product',
      ]
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      saveSlowDownData(data)

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
    } catch (error) {
      // setIsSaving(false);
    }
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
    setLoading(true)
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.maintenanceId || item?.id,
        id: index,
      }))
      // setSlowDownData(formattedData)
      setRows(formattedData)
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false) // Hide loading
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(
          keycloak,
          lowerVertName === 'meg' ? 'Production' : 'Grade',
        )
        var productList = []
        if (lowerVertName === 'meg') {
          productList = data
            .filter((product) =>
              ['EO', 'EOE', 'MEG'].includes(product.displayName),
            )
            .map((product) => ({
              id: product.id,
              displayName: product.displayName,
            }))
        } else {
          productList = data.map((product) => ({
            id: product.id,
            displayName: product.displayName,
          }))
        }

        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }

    fetchData()
    // saveShutdownData()
    getAllProducts()
  }, [sitePlantChange, keycloak, lowerVertName])

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Slowdown Desc',
      minWidth: 250,
      editable: true,
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
      headerName: 'Particulars',
      editable: true,
      minWidth: 125,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value, id, api } = params

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.product),
        )

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
            <option value='' disabled>
              Select
            </option>
            {allProducts
              .filter(
                (product) =>
                  product.id === value || !existingValues.has(product.id),
              ) // Ensure selected value is included
              .map((product) => (
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
      valueFormatter: (params) => {
        return params ? dayjs(params).format('DD/MM/YYYY, h:mm:ss A') : ''
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
      valueFormatter: (params) => {
        return params ? dayjs(params).format('DD/MM/YYYY, h:mm:ss A') : ''
      },
    },
    lowerVertName === 'meg'
      ? {
          field: 'durationInHrs',
          headerName: 'Duration (hrs)',
          editable: true,
          minWidth: 75,
          renderEditCell: NumericInputOnly,
          align: 'left',
          headerAlign: 'left',
        }
      : {
          field: 'durationInHrs',
          headerName: 'Duration (hrs)',
          editable: false,
          minWidth: 75,
          renderEditCell: NumericInputOnly,
          align: 'left',
          headerAlign: 'left',
          valueGetter: findDuration,
        },

    {
      field: 'rate',
      headerName: 'Rate',
      editable: true,
      minWidth: 75,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: true,
      minWidth: 180,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
  ]

  // const handleRowEditStop = (params, event) => {
  //   setRowModesModel({
  //     ...rowModesModel,
  //     [params.id]: { mode: GridRowModes.View, ignoreModifications: false },
  //   })
  // }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const deleteRowData = async (paramsForDelete) => {
    try {
      const { idFromApi, id } = paramsForDelete.row
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteSlowdownData(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <ASDataGrid
        setRows={setRows}
        columns={colDefs}
        rows={rows}
        title={'Slowdown Activities'}
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
        // onRowEditStop={handleRowEditStop}
        onProcessRowUpdateError={onProcessRowUpdateError}
        experimentalFeatures={{ newEditingApi: true }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        deleteRowData={deleteRowData}
        permissions={{
          showAction: permissions?.showAction ?? true,
          addButton: permissions?.addButton ?? true,
          deleteButton: permissions?.deleteButton ?? true,
          editButton: permissions?.editButton ?? true,
          showUnit: permissions?.showUnit ?? false,
          saveWithRemark: permissions?.saveWithRemark ?? true,
          saveBtn: permissions?.saveBtn ?? true,
        }}
      />
    </div>
  )
}

export default SlowDown
