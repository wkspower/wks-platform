import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import React, { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import NumericInputOnly from 'utils/NumericInputOnly'
import { StartDateTimeEditCell } from 'utils/StartDateTimeEditCell'
import { EndDateTimeEditCell } from 'utils/EndDateTimeEditCell'

import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
import TimeInputCell from 'utils/TimeInputCell'

const ShutDown = ({ permissions }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // const [shutdownData, setShutdownData] = useState([])
  // const [allProducts, setAllProducts] = useState([])
  const [rowModesModel, setRowModesModel] = useState({})

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const keycloak = useSession()
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

    return updatedRow
  }, [])

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

        saveShutdownData(data)
      } catch (error) {
        console.log('Error saving changes:', error)
      }
    }, 400)
  }, [apiRef, rowModesModel])

  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  const saveShutdownData = async (newRow) => {
    setLoading(true)
    try {
      let plantId = ''

      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const shutdownDetails = newRow.map((row) => ({
        productId: row.product,
        discription: row.discription,
        durationInHrs: parseFloat(findDuration('1', row)),
        maintEndDateTime: addTimeOffset(row.maintEndDateTime),
        maintStartDateTime: addTimeOffset(row.maintStartDateTime),
        audityear: localStorage.getItem('year'),
        id: row.idFromApi || null,
        remark: row.remark || 'null',
      }))

      const response = await DataService.saveShutdownData(
        plantId,
        shutdownDetails,
        keycloak,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Shutdown data Saved Successfully!',
        severity: 'success',
      })
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      setLoading(false)
      return response
    } catch (error) {
      setLoading(false)
      console.error('Error saving shutdown data:', error)
    } finally {
      fetchData()
      setLoading(false)
    }
  }

  const updateShutdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInHrs: newRow.durationInHrs,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
      }

      const response = await DataService.updateShutdownData(
        maintenanceId,
        slowDownDetails,
        keycloak,
      )

      setSnackbarOpen(true)
      // setSnackbarMessage("Shutdown data Updated successfully !");
      setSnackbarData({
        message: 'Shutdown data Updated successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Shutdown data Updated successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Shutdown data:', error)
    } finally {
      fetchData()
    }
  }

  const fetchData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getShutDownPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        originalRemark: item.remark,
      }))
      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching Shutdown data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, yearChanged, keycloak, verticalChange, lowerVertName])

  const findDuration1 = (value, row) => {
    if (row && row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)
      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        const durationInMs = end - start
        const durationInMinutes = durationInMs / (1000 * 60)
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = durationInMinutes % 60
        const formattedMinutes = minutes.toString().padStart(2, '0')
        const formattedDuration = `${hours}.${formattedMinutes}`
        return formattedDuration
      }
    }
    return ''
  }

  const findDuration2 = (value, row) => {
    const { maintStartDateTime, maintEndDateTime, durationInHrs } = row

    if (maintStartDateTime && maintEndDateTime) {
      const start = new Date(maintStartDateTime)
      const end = new Date(maintEndDateTime)
      if (!isNaN(start) && !isNaN(end)) {
        const durationInMs = end - start
        const durationInMinutes = durationInMs / (1000 * 60)
        const hours = Math.floor(durationInMinutes / 60)
        const minutes = Math.round(durationInMinutes % 60)
        return `${hours}.${minutes.toString().padStart(2, '0')}`
      }
    }

    return durationInHrs || ''
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

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Shutdown Desc',
      minWidth: 125,
      editable: true,
      flex: 3,
    },
    {
      field: 'maintenanceId',
      headerName: 'maintenanceId',
      editable: false,
      hide: true,
    },

    // {
    //   field: 'product',
    //   headerName: lowerVertName === 'meg' ? 'Product' : 'Grade Name',
    //   editable: true,
    //   minWidth: 125,
    //   valueGetter: (params) => {
    //     // console.log('p1', params);
    //     // console.log('p2', params2);
    //     return params || ''
    //   },
    //   valueFormatter: (params) => {
    //     // console.log('params valueFormatter ', params)
    //     const product = allProducts.find((p) => p.id === params)
    //     return product ? product.displayName : ''
    //   },
    //   renderEditCell: (params) => {
    //     const { value } = params
    //     // console.log('q1', params);
    //     // console.log('q2', params2);
    //     return (
    //       <select
    //         value={value || ''}
    //         onChange={(event) => {
    //           params.api.setEditCellValue({
    //             id: params.id,
    //             field: 'product',
    //             value: event.target.value,
    //           })
    //         }}
    //         style={{
    //           width: '100%',
    //           padding: '5px',
    //           border: 'none', // Removes border
    //           outline: 'none', // Removes focus outline
    //           background: 'transparent', // Keeps background clean
    //         }}
    //       >
    //         {/* Disabled first option */}
    //         <option value='' disabled>
    //           Select
    //         </option>
    //         {allProducts.map((product) => (
    //           <option key={product.id} value={product.id}>
    //             {product.displayName}
    //           </option>
    //         ))}
    //       </select>
    //     )
    //   },
    // },

    {
      field: 'maintStartDateTime',
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      // renderEditCell: (params) => <StartDateTimeEditCell {...params} />,
      renderEditCell: (params) => (
        <StartDateTimeEditCell {...params} apiRef={apiRef} />
      ),

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
      // renderEditCell: (params) => <EndDateTimeEditCell {...params} />,
      // renderEditCell: (params) => <StartDateTimeEditCell {...params} apiRef={apiRef} />,
      renderEditCell: (params) => (
        <EndDateTimeEditCell {...params} apiRef={apiRef} />
      ),
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
      align: 'left',
      headerAlign: 'left',
      // valueGetter: (params) => params?.durationInHrs || 0,
      valueGetter: findDuration,
    },
    {
      field: 'remark',
      headerName: 'Remark',
      minWidth: 250,
      editable: false,
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
        await DataService.deleteShutdownData(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record', error)
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
        title={'Shutdown/Turnaround Activities'}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateShutdownData={updateShutdownData}
        processRowUpdate={processRowUpdate}
        rowModesModel={rowModesModel}
        onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        deleteId={deleteId}
        open1={open1}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
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
          editButton: permissions?.editButton ?? false,
          showUnit: permissions?.showUnit ?? false,
          saveWithRemark: permissions?.saveWithRemark ?? true,
          saveBtn: permissions?.saveBtn ?? true,
          customHeight: permissions?.customHeight,
        }}
      />
    </div>
  )
}

export default ShutDown
