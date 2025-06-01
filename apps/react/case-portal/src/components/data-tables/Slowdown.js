import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { useSelector } from 'react-redux'
import NumericInputOnly from 'utils/NumericInputOnly'
import { StartDateTimeEditCell } from 'utils/StartDateTimeEditCell'
import { EndDateTimeEditCell } from 'utils/EndDateTimeEditCell'
import Tooltip from '@mui/material/Tooltip'

import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { truncateRemarks } from 'utils/remarksUtils'
import { validateFields } from 'utils/validationUtils'
import TimeInputCell from 'utils/TimeInputCell'
import { renderTwoLineEllipsis } from 'components/Utilities/twoLineEllipsisRenderer'
import { GridRowModes } from '../../../node_modules/@mui/x-data-grid/models/gridEditRowModel'

const SlowDown = ({ permissions }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  // const [slowDownData, setSlowDownData] = useState([])
  const [rowModesModel, setRowModesModel] = useState({})
  const [modifiedCells, setModifiedCells] = React.useState({})

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

  const handleCancelClick = (id) => () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })

    // setRowModesModel({
    //   ...rowModesModel,
    //   [id]: { mode: GridRowModes.View, ignoreModifications: true },
    // })

    // const editedRow = rows.find((row) => row.id === id)
    // if (editedRow.isNew) {
    //   setRows(rows.filter((row) => row.id !== id))
    // }
  }

  const handleRemarkCellClick = (row) => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })

    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
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

    const durationChanged = newRow.durationInHrs !== oldRow.durationInHrs
    if (durationChanged) {
      newRow.maintEndDateTime = null
    }
    const updatedRow = { ...newRow }
    const { maintStartDateTime, maintEndDateTime, durationInHrs } = updatedRow
    const isValidDate = (d) => d && !isNaN(new Date(d).getTime())
    if (isValidDate(maintStartDateTime) && isValidDate(maintEndDateTime)) {
      const start = new Date(maintStartDateTime)
      const end = new Date(maintEndDateTime)
      const durationInMinutes = (end - start) / (1000 * 60)
      if (durationInMinutes >= 0) {
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = durationInMinutes % 60
        updatedRow.durationInHrs = `${hours}.${minutes.toString().padStart(2, '0')}`
      } else {
        updatedRow.durationInHrs = ''
      }
    } else if (
      isValidDate(maintStartDateTime) &&
      durationInHrs &&
      !isValidDate(maintEndDateTime)
    ) {
      const [hrs, mins = '00'] = durationInHrs.split('.')
      const totalMinutes = parseInt(hrs) * 60 + parseInt(mins)
      const calculatedEnd = new Date(
        new Date(maintStartDateTime).getTime() + totalMinutes * 60000,
      )
      updatedRow.maintEndDateTime = calculatedEnd.toISOString()
    }
    unsavedChangesRef.current.unsavedRows[rowId || 0] = updatedRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === updatedRow.id ? { ...updatedRow, isNew: false } : row,
      ),
    )

    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return updatedRow
  }, [])

  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }
  const findDuration = (v, row) => {
    if (row.durationInHrs) return row.durationInHrs

    if (row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)

      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        const durationInMs = end - start
        const durationInMinutes = durationInMs / (1000 * 60)
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = durationInMinutes % 60
        return `${hours}.${minutes.toString().padStart(2, '0')}`
      }
    }

    return ''
  }
  const saveSlowDownData = async (newRow) => {
    setLoading(true)
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
        durationInHrs: parseFloat(findDuration('1', row)),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        remark: row.remark,
        rate: row.rate,
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
      }))
      const slowDownDetailsMEG = newRow.map((row) => ({
        productId: row.product,
        discription: row.discription,
        // durationInHrs: parseFloat(findDuration('1', row)),
        durationInHrs: (() => {
          const v = findDuration('1', row)
          if (!v) return null
          const [h = '00', m = '00'] = String(v).split('.')
          return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
        })(),

        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        remark: row.remark,
        rate: row.rate,
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
      }))
      const response = await DataService.saveSlowdownData(
        plantId,
        lowerVertName === 'meg' ? slowDownDetailsMEG : slowDownDetailsMEG,
        keycloak,
      )
      //console.log('Slowdown data Saved Successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Slowdown data Saved Successfully !");
      setSnackbarData({
        message: 'Slowdown data Saved Successfully!',
        severity: 'success',
      })
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      setLoading(false)
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Slowdown data Saved Successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
      setLoading(false)
    } finally {
      fetchData()
      setLoading(false)
    }
  }
  const saveChanges = React.useCallback(async () => {
    const rowsInEditMode = Object.keys(rowModesModel).filter(
      (id) => rowModesModel[id]?.mode === 'edit',
    )

    rowsInEditMode.forEach((id) => {
      apiRef.current.stopRowEditMode({ id })
    })
    setTimeout(() => {
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
          // 'durationInHrs',
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
      } catch (error) {
        // setIsSaving(false);
      }
    }, 400)
  }, [apiRef, rowModesModel])

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
        originalRemark: item.remark,
      }))
      // setSlowDownData(formattedData)
      setRows(formattedData)
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false) // Hide loading
    }
  }

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        var data = []
        if (lowerVertName == 'meg')
          data = await DataService.getAllProducts(keycloak, null)
        else {
          data = await DataService.getAllProductsAll(keycloak, 'Production')
        }
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
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const focusFirstField = async () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    }))
  }

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Slowdown Desc',
      minWidth: 180,
      editable: true,
      flex: 3,
      renderCell: renderTwoLineEllipsis,
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
      minWidth: 150,
      renderEditCell: (params) => {
        const { value, id, api } = params

        const allProductOptions = allProducts.map((product) => ({
          value: product.id,
          label: product.displayName,
        }))

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.product),
        )

        const filteredOptions = allProductOptions.filter(
          (option) =>
            option.value === value || !existingValues.has(option.value),
        )

        return (
          <Autocomplete
            value={
              allProductOptions.find((option) => option.value === value) ||
              (params.row.product &&
                allProductOptions.find(
                  (opt) => opt.value === params.row.product,
                )) ||
              null
            }
            disableClearable
            options={allProductOptions}
            getOptionLabel={(option) => option?.label || ''}
            onChange={(event, newValue) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: newValue?.value || '',
              })
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                variant='outlined'
                size='small'
                fullWidth
                style={{ width: '210px' }}
              />
            )}
          />
        )
      },
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      filterOperators: [
        {
          label: 'contains',
          value: 'contains',
          getApplyFilterFn: (filterItem) => {
            if (!filterItem?.value) {
              return
            }
            return (rowId) => {
              const filterValue = filterItem.value.toLowerCase()
              if (filterValue) {
                const productName = getProductDisplayName(rowId)
                if (productName) {
                  return productName.toLowerCase().includes(filterValue)
                }
              }
              return true
            }
          },
          InputComponent: ({ item, applyValue, focusElementRef }) => (
            <TextField
              autoFocus
              inputRef={focusElementRef}
              size='small'
              label='Contains'
              value={item.value || ''}
              onChange={(event) =>
                applyValue({ ...item, value: event.target.value })
              }
              style={{ marginTop: '8px' }}
            />
          ),
        },
      ],
    },

    {
      field: 'maintStartDateTime',
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      renderEditCell: (params) => <StartDateTimeEditCell {...params} />,
      valueFormatter: (params) => {
        const value = params
        return value && dayjs(value).isValid()
          ? dayjs(value).format('DD/MM/YYYY, h:mm:ss A')
          : ''
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'SD- To',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      renderEditCell: (params) => <EndDateTimeEditCell {...params} />,
      valueFormatter: (params) => {
        const value = params
        return value && dayjs(value).isValid()
          ? dayjs(value).format('DD/MM/YYYY, h:mm:ss A')
          : ''
      },
    },

    {
      field: 'durationInHrs',
      headerName: 'Duration (hrs)',
      editable: true,
      minWidth: 100,
      renderEditCell: TimeInputCell,
      align: 'right',
      headerAlign: 'left',
      valueGetter: findDuration,
      valueFormatter: (params) => {
        const value = params
        if (!value && value !== 0) return ''
        const [hoursStr, minutesStr = '0'] = value.toString().split('.')
        const hours = hoursStr.padStart(2, '0')
        const minutes = minutesStr.padStart(2, '0')
        return `${hours}:${minutes}`
      },
    },

    {
      field: 'rate',
      headerName: 'Rate (TPH)',
      editable: true,
      minWidth: 75,
      renderEditCell: NumericInputOnly,
      align: 'right',
      headerAlign: 'left',
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: false,
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
                width: ' 100%',
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

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

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

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? true,
      addButton: permissions?.addButton ?? true,
      deleteButton: permissions?.deleteButton ?? true,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: false,
    },
    isOldYear,
  )

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <ASDataGrid
        modifiedCells={modifiedCells}
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
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
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
        permissions={adjustedPermissions}
        handleCancelClick={handleCancelClick}
        focusFirstField={focusFirstField}

        // permissions={{
        //   showAction: permissions?.showAction ?? true,
        //   addButton: permissions?.addButton ?? true,
        //   deleteButton: permissions?.deleteButton ?? true,
        //   editButton: permissions?.editButton ?? false,
        //   showUnit: permissions?.showUnit ?? false,
        //   saveWithRemark: permissions?.saveWithRemark ?? true,
        //   saveBtn: permissions?.saveBtn ?? true,
        //   customHeight: permissions?.customHeight,
        // }}
      />
    </div>
  )
}

export default SlowDown
