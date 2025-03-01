import { useState, useMemo, useEffect, useCallback } from 'react'
import { DataGrid } from '@mui/x-data-grid'
import {
  Button,
  TextField,
  IconButton,
  Typography,
  Box,
  InputAdornment,
} from '@mui/material'
import {
  GridRowModes,
  GridToolbarContainer,
  GridActionsCellItem,
  GridRowEditStopReasons,
} from '@mui/x-data-grid'
// import MoreVertIcon from '@mui/icons-material/MoreVert'
import SearchIcon from '@mui/icons-material/Search'
// import FilterAltIcon from '@mui/icons-material/FilterAlt'
// import EditIcon from '@mui/icons-material/Edit'
import { useSession } from 'SessionStoreContext'
import { Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material'
import { DataService } from 'services/DataService'

import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import { Grid, MenuItem } from '../../../node_modules/@mui/material/index'

import Notification from 'components/Utilities/Notification'

import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Close'

import {
  FileDownload,
  FileUpload,
} from '../../../node_modules/@mui/icons-material/index'

const jioColors = {
  primaryBlue: '#0F3CC9',
  accentRed: '#E31C3D',
  background: '#FFFFFF',
  headerBg: '#0F3CC9',
  rowEven: '#FFFFFF',
  rowOdd: '#E8F1FF',
  textPrimary: '#2D2D2D',
  border: '#D0D0D0',
  darkTransparentBlue: 'rgba(127, 147, 206, 0.8)', // New color added
}

const DataGridTable = ({
  columns: initialColumns = [],
  rows: initialRows = [],
  title = 'Turnaround Plan Details',
  onAddRow,
  onDeleteRow,
  onRowUpdate,
  permissions,
}) => {
  const [selectedRows, setSelectedRows] = useState({})
  // const [snackbarMessage, setSnackbarMessage] = useState('')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  // const [rowModesModel, setRowModesModel] = useState({})
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [openYearData, setOpenYearData] = useState(false)
  const [yearData, setYearData] = useState('')
  const [resizedColumns, setResizedColumns] = useState({})
  const [open, setOpen] = useState(false)
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [remark, setRemark] = useState('')
  const [product, setProduct] = useState('')
  const [openRemark, setOpenRemark] = useState(false)
  const keycloak = useSession()
  const [days, setDays] = useState([])
  const [rows, setRows] = useState(initialRows)
  const [searchText, setSearchText] = useState('')
  const [isFilterActive, setIsFilterActive] = useState(false)
  const [selectedRowId, setSelectedRowId] = useState(null) // Store selected row ID
  const unitOptions = ['In percentage (%)', 'Absolute number']
  const [selectedUnit, setSelectedUnit] = useState()
  // const [localEditState, setLocalEditState] = useState(null); // Local edit state for validation errors
  const handleOpenRemark = () => setOpenRemark(true)
  const handleCloseRemark = () => setOpenRemark(false)
  const handleClose1 = () => setOpen1(false)
  const handleSearchChange = (event) => {
    setSearchText(event.target.value)
  }
  const [rowModesModel, setRowModesModel] = useState({})
  const [changedRowIds, setChangedRowIds] = useState([])

  const handleRowEditStop = (params, event) => {
    //console.log('handleRowEditStop', params)

    if (params.reason === GridRowEditStopReasons.rowFocusOut) {
      event.defaultMuiPrevented = true
    }
  }

  const handleRowEditCommit = (id, event) => {
    const editedRow = rows.find((row) => row.id === id)
    console.log('Row Data After Editing:', editedRow)
    // handleEditClick(id, event)
  }

  const handleEditClick = (id, row) => () => {
    //console.log('id',id)
    //console.log('row',row)
//    setChangedRowIds(id)
    setIsUpdating(true)
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
  }

  // const handleSaveClick = (id) => () => {
  //   setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.View } });
  // };

  const handleSaveClick = (id, rowData) => {
    // if (!rowData.remarks) {
    //   setSnackbarOpen(true)
    //   setSnackbarData({
    //     message: 'Please Fill remark Fields!',
    //     severity: 'error',
    //   })
    //   return
    // }
    //console.log('Newly Added Row Data:', rowData)
    console.log('selectedRows:', changedRowIds)
    console.log(changedRowIds)
    handleOpenRemark()
    setRowModesModel((prev) => ({
      ...prev,
      [id]: { mode: GridRowModes.View },
    }))
  }

  // useEffect(() => {
  //   //console.log("Updated Rows!!!!!!!!!!!!!!!!!!!!!!!:", rows);
  // }, [rows]);

  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId
      console.log(maintenanceId, params, id)
      // Define a mapping of titles to corresponding delete functions
      const deleteFunctions = {
        'Slowdown Plan': DataService.deleteSlowdownData,
        'Shutdown Plan': DataService.deleteShutdownData,
        'TA Plan': DataService.deleteTurnAroundData,
        'Product Demand': DataService.deleteBusinessDemandData,
        'Catalyst Selectivity Data': DataService.deleteBusinessDemandData,
        // 'Consumption Norms': DataService.deleteBusinessDemandData,
      }

      // Check if title exists in deleteFunctions and execute the corresponding function
      if (deleteFunctions[title]) {
        return await deleteFunctions[title](maintenanceId, keycloak)
      }

      setOpen1(true)
      setDeleteId(id)
    } catch (error) {
      console.error(`Error deleting ${title} data:`, error)
    }
  }

  const handleCancelClick = (id) => () => {
    setRowModesModel({
      ...rowModesModel,
      [id]: { mode: GridRowModes.View, ignoreModifications: true },
    })

    const editedRow = rows.find((row) => row.id === id)
    if (editedRow.isNew) {
      setRows(rows.filter((row) => row.id !== id))
    }
  }

  const handleRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const saveShutdownData = async (newRow) => {
    try {
      // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'

      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      // plantId = plantId;

      const shutdownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
      }

      const response = await DataService.saveShutdownData(
        plantId,
        shutdownDetails,
        keycloak,
      )
      //console.log('Shutdown data saved successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Shutdown data saved successfully !");
      setSnackbarData({
        message: 'Shutdown data saved successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Shutdown data saved successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving shutdown data:', error)
    }
  }

  const updateSlowdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
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
    }
  }

  const updateShutdownData = async (newRow) => {
    try {
      var maintenanceId = newRow?.maintenanceId

      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
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
    }
  }

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
    }
  }

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

  const saveSlowDownData = async (newRow) => {
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      const slowDownDetails = {
        productId: newRow.product,
        discription: newRow.discription,
        durationInMins: newRow.durationInMins,
        maintEndDateTime: newRow.maintEndDateTime,
        maintStartDateTime: newRow.maintStartDateTime,
        remark: newRow.remarks,
        rate: newRow.rate,
      }
      const response = await DataService.saveSlowdownData(
        plantId,
        slowDownDetails,
        keycloak,
      )
      //console.log('Slowdown data saved successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Slowdown data saved successfully !");
      setSnackbarData({
        message: 'Slowdown data saved successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Slowdown data saved successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Slowdown data:', error)
    }
  }

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
    }
  }
  const saveBusinessDemandData = async (newRow) => {
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businnessData = [
        {
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
          march: newRow.mar,
          remark: newRow.remark,
          avgTph: newRow.avgTph,
          year: '2024-25',
          plantId: plantId,
          normParameterId: newRow.normParameterId,
        },
      ]

      const response = await DataService.saveBusinessDemandData(
        plantId,
        businnessData,
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
    }
  }
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
    }
  }

  const processRowUpdate = useCallback(
    (newRow) => {
      if (title == 'Shutdown Plan') {
        if (
          !newRow.discription?.trim() ||
          !newRow.product?.trim() ||
          !newRow.maintStartDateTime ||
          !newRow.maintEndDateTime
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'discription',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)

        if (newRow?.maintenanceId) {
          updateShutdownData(newRow)
        } else {
          saveShutdownData(newRow)
        }

        onRowUpdate?.(updatedRow)
        //console.log('Updated Row inside processRowUpdate:', updatedRow)
        setSelectedRows(updatedRow)

        return updatedRow // Ensure function returns the updated row
      }
      if (title == 'Slowdown Plan') {
        if (
          !newRow.discription?.trim() ||
          !newRow.product?.trim() ||
          !newRow.maintStartDateTime ||
          !newRow.maintEndDateTime ||
          !newRow.remarks ||
          !newRow.rate
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'discription',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)

        if (newRow?.maintenanceId) {
          updateSlowdownData(newRow)
        } else {
          saveSlowDownData(newRow)
        }
        onRowUpdate?.(updatedRow)
        setSelectedRows(updatedRow)
        return updatedRow // Ensure function returns the updated row
      }
      if (title == 'Turnaroud Plan') {
        //console.log('TA',newRow)
        if (
          !newRow.discription?.trim() ||
          !newRow.product?.trim() ||
          !newRow.maintStartDateTime ||
          !newRow.maintEndDateTime ||
          !newRow.durationInMins ||
          !newRow.remark
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'discription',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)

        if (newRow?.maintenanceId) {
          updateTurnAroundData(newRow)
        } else saveTurnAroundData(newRow)

        onRowUpdate?.(updatedRow)
        setSelectedRows(updatedRow)

        return updatedRow // Ensure function returns the updated row
      }
      if (
        title == 'Production Volume Data' ||
        title == 'Production Norms Data'
      ) {
        if (
          !newRow.april ||
          !newRow.aug ||
          !newRow.dec ||
          !newRow.feb ||
          !newRow.jan ||
          !newRow.july ||
          !newRow.june ||
          !newRow.march ||
          !newRow.may ||
          !newRow.nov ||
          !newRow.oct ||
          !newRow.sep
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'april',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)

        if (newRow?.maintenanceId) {
          updateProductNormData(newRow)
        } else {
          updateProductNormData(newRow)
          // saveTurnAroundData(newRow)
        }
        onRowUpdate?.(updatedRow)
        //console.log('Updated Row inside processRowUpdate:', updatedRow)
        setSelectedRows(updatedRow)

        return updatedRow // Ensure function returns the updated row
      }
      if (title == 'Configuration') {
        if (!newRow.dec24) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'dec24',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)

        // if(newRow?.id){
        //   // updateCatalystData(newRow)
        // }else
        {
          saveCatalystData(newRow)
          // saveTurnAroundData(newRow)
        }
        onRowUpdate?.(updatedRow)
        //console.log('Updated Row inside processRowUpdate:', updatedRow)
        setSelectedRows(updatedRow)

        return updatedRow // Ensure function returns the updated row
      }

      if (title == 'Business Demand') {
        if (
          !newRow.april ||
          !newRow.may ||
          !newRow.june ||
          !newRow.july ||
          !newRow.aug ||
          !newRow.sep ||
          !newRow.oct ||
          !newRow.nov ||
          !newRow.dec ||
          !newRow.jan ||
          !newRow.feb ||
          !newRow.march ||
          !newRow.remark ||
          !newRow.avgTph
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please Fill all Fields!',
            severity: 'error',
          })
          setRowModesModel(() => ({
            [newRow.id]: {
              mode: GridRowModes.Edit,
              fieldToFocus: 'april',
            },
          }))
          return
        }

        const updatedRow = { ...newRow, isNew: false }
        const updatedRows = rows.map((row) =>
          row?.id === newRow?.id ? updatedRow : row,
        )

        setRows(updatedRows)
        {
          saveBusinessDemandData(newRow)
        }
        onRowUpdate?.(updatedRow)
        setSelectedRows(updatedRow)
        return updatedRow
      }
    },
    [rows, onRowUpdate],
  )

  useEffect(() => {
    setRows(initialRows)
  }, [initialRows])

  const onColumnResized = (params) => {
    if (params.column) {
      const field = params.column.getColDef().field
      setResizedColumns((prev) => ({
        ...prev,
        [field]: true,
      }))
    }
  }

  const handleOpenYearData = (params) => {
    if (params.row.product || product === '') {
      setSnackbarOpen(true)
      // setSnackbarMessage('Select a Product First!')
      setSnackbarData({
        message: 'Select a Product First !',
        severity: 'error',
      })
      return
    }
    setOpenYearData(false)
    // setOpenYearData(true)
  }

  const handleCloseYearData = () => {
    setOpenYearData(false)
    setYearData('')
  }

  const addYearData = () => {
    //console.log("Year's Data:", yearData)
    handleCloseYearData()
  }

  // const handleFilterClick = () => {
  //   setIsFilterActive(!isFilterActive)
  // }
  const handleImportExport = () => {
    alert('File Import/Export feature coming soon!')
  }

  // const filteredRows = rows.filter((row) => {
  //   const matchesSearch = Object.values(row).some((value) =>
  //     String(value).toLowerCase().includes(searchText.toLowerCase()),
  //   )
  //   const matchesDuration = !isFilterActive || row.durationHrs > 100
  //   return matchesSearch && matchesDuration
  // })

  // const handleMenuClick = (event, row) => {
  //   setAnchorEl(event.currentTarget)
  //   setSelectedRow(row)
  // }

  // const handleMenuClose = () => {
  //   setAnchorEl(null)
  //   setSelectedRow(null)
  // }

  const handleDeleteRow = (id) => {
    setDeleteId(id)
    setOpen1(true)
  }

  const deleteTheRecord = () => {
    const updatedRows = rows.filter((row) => row?.id !== deleteId)
    setRows(rows.filter((row) => row.id !== deleteId))
    setRows(updatedRows)
    onDeleteRow?.(deleteId)
    setDeleteId(null)
    setOpen1(false)
    //now that snackbar will open
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Slowdown data deleted successfully!',
      severity: 'success',
    })
  }

  const handleAddRow1 = () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true, // Mark row as new
      ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])), // Empty values
    }
    setRows((prevRows) => [...prevRows, newRow])
    onAddRow?.(newRow)
    setProduct('')
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    }))
  }

  const handleAddRow = () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])),
    }

    setRows((prevRows) => [newRow, ...prevRows])
    onAddRow?.(newRow)
    setProduct('')
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    }))
  }

  const dummyApiCall = async (id) => {
    try {
      const data = await DataService.getProductById(keycloak, id)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getYearlyData = async (year) => {
    try {
      const data = await DataService.getYearlyData(keycloak, year)
      //console.log('API getYearlyData:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getPlantAndSite = async () => {
    try {
      const data = await DataService.getAllSites(keycloak)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getShutDownPlantData = async () => {
    try {
      const data = await DataService.getShutDownPlantData(keycloak)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getSlowDownPlantData = async () => {
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getTAPlantData = async () => {
    try {
      const data = await DataService.getTAPlantData(keycloak)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak)
      //console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const handleSaveRow = (id) => {
    const updatedRows = rows.map((row) =>
      row.id === id ? { ...row, isNew: false } : row,
    )
    setRows(updatedRows)
  }

  const handleCancelRow = (id) => {
    const updatedRows = rows.filter((row) => row.id !== id)
    setRows(updatedRows)
  }

  const defaultColumns = useMemo(() => {
    return initialColumns.map((col) => ({
      ...col,
      flex: !resizedColumns[col.field] ? 1 : undefined,
    }))
  }, [initialColumns, resizedColumns])

  const columns = useMemo(() => [
    ...defaultColumns,
    ...(permissions?.showAction
      ? [
          {
            field: 'actions',
            type: 'actions',
            headerName: 'Actions',
            width: 180,
            cellClassName: 'actions',

            getActions: (params) => {
              const { id, row } = params // Extract row data
              // //console.log("Row Data inside getActions:", params.row);
              // setSelectedRows(row)

              const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit

              if (isInEditMode) {
                return [
                  <GridActionsCellItem
                    key={`save-${id}`}
                    icon={<SaveIcon />}
                    label='Save'
                    sx={{ color: 'primary.main', display: 'none' }}
                    onClick={() => handleSaveClick(id, params.row)} // Pass row data
                  />,
                  <GridActionsCellItem
                    key={`cancel-${id}`}
                    icon={<CancelIcon />}
                    label='Cancel'
                    className='textPrimary'
                    onClick={handleCancelClick(id)}
                    color='inherit'
                  />,
                ]
              }

              return [
                permissions?.editButton && (
                  <GridActionsCellItem
                    key={`edit-${id}`}
                    icon={<EditIcon sx={{ color: jioColors.primaryBlue }} />}
                    label='Edit'
                    className='textPrimary'
                    onClick={handleEditClick(id, row)}
                    color='inherit'
                    sx={{ display: 'none' }}
                  />
                ),
                permissions?.deleteButton && (
                  <GridActionsCellItem
                    key={`delete-${id}`}
                    icon={<DeleteIcon sx={{ color: jioColors.accentRed }} />}
                    label='Delete'
                    onClick={() => handleDeleteClick(id, params)}
                    color='inherit'
                  />
                ),
              ].filter(Boolean) // Remove `null` values if permission is false
            },
            minWidth: 70,
            maxWidth: 100,
            headerClassName: 'last-column-header',
          },
        ]
      : []), // If no permissions, hide the Actions column
  ])

  const addRemark = () => {
    //console.log('Remark:', remark)
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === selectedRowId ? { ...row, remark } : row,
      ),
    )

    setOpenRemark(false)
    setRemark('')
  }

  const monthFields = new Set([
    'apr24',
    'may24',
    'jun24',
    'jul24',
    'aug24',
    'sep24',
    'oct24',
    'nov24',
    'dec24',
    'jan25',
    'feb25',
    'mar25',
  ])

  const nonEditableFields = [
    'product',
    'averageTPH',
    'desc',
    'shutdown',
    'to',
    'from',
    'id',
    'actions',
    'isNew',
    'taTo',
    'taFrom',
    'activities',
    'durationHrs',
    'period',
  ]

  // const handleCellClick = (params) => {}
  useEffect(() => {
    console.log('-->:', changedRowIds)
    console.log('Length of changedRowIds:', changedRowIds.length)
  }, [changedRowIds])

  const handleCellClick = (params) => {
    console.log(params)
    setChangedRowIds(params?.row)
    if (title == 'Product MCU Val') {
      if (nonEditableFields.includes(params.field)) return // Block non-editable fields

      if (params?.field === 'remark') {
        setRemark(params?.value || '')
        setSelectedRowId(params.id)
        handleOpenRemark()
      } else if (monthFields.has(params.field)) {
        // Allow editing only if value exists
        if (params.value !== '' && params.value !== null) {
          setOpen(false)
          // setOpen(true)
        }
      }
    }

    if (
      params.row.product === '' &&
      nonEditableFields.includes(params.field) &&
      monthFields.has(params.field)
    ) {
      // setSnackbarOpen(true)
      // setSnackbarMessage('Select a Product First!')
      // return
    } else {
      setProduct(params.row.product)
    }

    if (params?.field === 'remark') {
      setRemark(params?.value || '') // Auto-fetch the params value
      setSelectedRowId(params.id)
      handleOpenRemark()
    } else {
      if (monthFields.has(params.field)) {
        if (params.value == '') {
          handleOpenYearData(params) // Open popup only for month fields with no value
          return
        }
      }

      // If not a month field, just return
      if (nonEditableFields.includes(params.field)) return

      // Handle editable fields
      if (
        params.isEditable &&
        !nonEditableFields.includes(params.field) &&
        params.value !== null &&
        params.value !== undefined
      ) {
        const field = params.field
        const monthAbbr = field.substring(0, 3).toLowerCase()
        const yearShort = field.substring(3)
        const year = 2000 + parseInt(yearShort, 10)

        const monthMap = {
          jan: 0,
          feb: 1,
          mar: 2,
          apr: 3,
          may: 4,
          jun: 5,
          jul: 6,
          aug: 7,
          sep: 8,
          oct: 9,
          nov: 10,
          dec: 11,
        }
        const month = monthMap[monthAbbr]

        if (month === undefined) {
          console.error('Invalid month abbreviation:', monthAbbr)
          return
        }

        //console.log('params-params', params)

        // Calculate days in the selected month
        const totalDays = new Date(year, month + 1, 0).getDate()
        const perDayValue = (params.value / totalDays).toFixed(2) // Keep 2 decimal places

        const daysArray = Array.from({ length: totalDays }, (_, index) => {
          const date = new Date(year, month, index + 1)
          const day = String(date.getDate()).padStart(2, '0')
          const monthName = date.toLocaleString('en-GB', { month: 'short' })
          const yearShort = date.getFullYear().toString().slice(-2)

          const formattedDate = `${day}-${monthName}-${yearShort}`

          return {
            date: formattedDate,
            value: parseFloat(perDayValue), // Convert back to number with 2 decimals
          }
        })

        setDays(daysArray)
        setOpen(false)
        // setOpen(true)
      }
    }
  }

  // useEffect(() => {
  //   const getDaysInMonth = () => {
  //     const date = new Date()
  //     const year = date.getFullYear()
  //     const month = date.getMonth()
  //     const totalDays = new Date(year, month + 1, 0).getDate() // Get total days in month

  //     const daysArray = []
  //     for (let day = 1; day <= totalDays; day++) {
  //       daysArray.push({
  //         date: day, // Just the day number (1, 2, 3, ...30)
  //         value: Math.floor(Math.random() * 100), // Random value
  //       })
  //     }
  //     return daysArray
  //   }

  //   setDays(getDaysInMonth())
  // }, [])

  const handleSubmit = () => {
    const isEmpty = days.some((day) => day.value === '' || day.value === null)
    if (isEmpty) {
      setSnackbarOpen(true)
      // setSnackbarMessage('Add data for all fields!')
      setSnackbarData({
        message: 'Add data for all fields!',
        severity: 'error',
      })

      return
    }
    //console.log('Submitted Data:', days)
    setOpen(false) // Close the modal
    if (permissions?.saveWithRemark) {
      // Update the row with the new data
      setOpenRemark(true)
      setRows((prevRows) =>
        prevRows.map((row) =>
          row.id === selectedRowId ? { ...row, ...days } : row,
        ),
      )
    }
  }

  const handleCloseSnackbar = () => {
    setSnackbarOpen(false)
  }

  const handleCancel = () => {
    setOpen(false) // Just close the modal
  }

  // useEffect(() => {
  //   const getDaysInMonth = () => {
  //     const date = new Date()
  //     const year = date.getFullYear()
  //     const month = date.getMonth()
  //     const totalDays = new Date(year, month + 1, 0).getDate() // Get total number of days in the month

  //     return Array.from({ length: totalDays }, (_, index) => ({
  //       date: index + 1, // Day number (1, 2, 3, ...30)
  //       value: Math.floor(Math.random() * 100), // Random value
  //     }))
  //   }

  //   setDays(getDaysInMonth())
  // }, [])

  // Handle input changes
  const handleValueChange = (index, newValue) => {
    setDays((prevDays) =>
      prevDays.map((day, i) =>
        i === index ? { ...day, value: newValue } : day,
      ),
    )
  }
  const [columnFilters, setColumnFilters] = useState({})

  // Update the filter state for each column
  const handleFilterChange = (field, value) => {
    setColumnFilters((prevFilters) => ({
      ...prevFilters,
      [field]: value,
    }))
  }

  // Combine all filters: global search, duration filter, and column filters
  const filteredRows = useMemo(() => {
    return rows.filter((row) => {
      // Global search across all fields
      const matchesSearch = Object.values(row).some((value) =>
        String(value).toLowerCase().includes(searchText.toLowerCase()),
      )

      // Duration filter condition
      const matchesDuration = !isFilterActive || row.durationHrs > 100

      // Column-specific filters: For each column filter, check if row's value includes the filter
      const matchesColumnFilters = Object.entries(columnFilters).every(
        ([field, filterValue]) => {
          if (!filterValue) return true // No filter applied for this column
          return String(row[field])
            .toLowerCase()
            .includes(filterValue.toLowerCase())
        },
      )

      return matchesSearch && matchesDuration && matchesColumnFilters
    })
  }, [rows, searchText, isFilterActive, columnFilters])

  return (
    <Box
      sx={{
        height: '81vh',
        width: '100%',
        padding: 1,
        backgroundColor: '#F2F3F8',
        // backgroundColor: '#fff',
        borderRadius: 0,
        borderBottom: 'none',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: 1,
        }}
      >
        <Typography
          sx={{
            color: '#040510',
            fontSize: '1.5rem',
            fontWeight: 300,
            letterSpacing: '0.5px',
          }}
        >
          {title}
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'flex',
          justifyContent: 'flex-end',
          alignItems: 'center',
          marginTop: 2,
          marginBottom: 1,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {permissions?.showCalculate && (
            <Button
              variant='contained'
              sx={{
                // marginTop: 2,
                backgroundColor: jioColors.primaryBlue,
                color: jioColors.background,
                borderRadius: 1,
                padding: '8px 24px',
                textTransform: 'none',
                fontSize: '0.875rem',
                fontWeight: 500,
                '&:hover': {
                  backgroundColor: '#143B6F',
                  boxShadow: 'none',
                },
              }}
            >
              CALCULATE
            </Button>
          )}
          {permissions?.showRefreshBtn && (
            <Button
              variant='contained'
              sx={{
                // marginTop: 2,
                backgroundColor: jioColors.primaryBlue,
                color: jioColors.background,
                borderRadius: 1,
                padding: '8px 24px',
                textTransform: 'none',
                fontSize: '0.875rem',
                fontWeight: 500,
                '&:hover': {
                  backgroundColor: '#143B6F',
                  boxShadow: 'none',
                },
              }}
            >
              Refresh
            </Button>
          )}

          {permissions?.showUnit && (
            <TextField
              select
              value={selectedUnit}
              onChange={(e) => setSelectedUnit(e.target.value)}
              sx={{ width: '150px', backgroundColor: jioColors.background }}
              variant='outlined'
              label='Select UOM'
            >
              <MenuItem value='' disabled>
                Select UOM
              </MenuItem>
              {unitOptions.map((unit) => (
                <MenuItem key={unit} value={unit}>
                  {unit}
                </MenuItem>
              ))}
            </TextField>
          )}

          {/* commented for demo 4 March
          
          <TextField
            variant='outlined'
            placeholder='Search...'
            value={searchText}
            onChange={handleSearchChange}
            sx={{
              width: '300px',
              borderRadius: 1,
              backgroundColor: jioColors.background,
              color: '#8A9BC2',
            }}
            InputProps={{
              endAdornment: (
                <InputAdornment position='start'>
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          /> */}
          <IconButton
            aria-label='import'
            onClick={handleImportExport}
            sx={{
              border: `1px solid ${jioColors.border}`,
              borderRadius: 1,
              padding: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
              color: 'inherit',
              width: '150px',
              '&:hover': {
                backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF', // Removes hover effect
              },
            }}
          >
            <FileDownload
              sx={{ color: '#2A3ACD' }}
              // sx={{ color: isFilterActive ? jioColors.background : 'inherit' }}
            />
            <span
              style={{
                fontSize: '0.875rem',
                color: '#2A3ACD',
              }}
            >
              Import
            </span>
          </IconButton>
          <IconButton
            aria-label='export'
            onClick={handleImportExport}
            sx={{
              border: `1px solid ${jioColors.border}`,
              borderRadius: 1,
              padding: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
              color: 'inherit',
              width: '150px',
              '&:hover': {
                backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF', // Removes hover effect
              },
            }}
          >
            <FileUpload
              sx={{ color: '#2A3ACD' }}
              // sx={{ color: isFilterActive ? jioColors.background : 'inherit' }}
            />
            <span
              style={{
                fontSize: '0.875rem',
                color: '#2A3ACD',
              }}
            >
              Export
            </span>
          </IconButton>
        </Box>
      </Box>

      <Box sx={{ height: 'calc(100% - 150px)', width: '100%' }}>
        {/* <Grid container spacing={2}>
          {columns.map((col) => (
            <Grid item xs key={col.field}>
              <TextField
                placeholder={`Filter ${col.headerName}`}
                variant='outlined'
                size='small'
                onChange={(e) => handleFilterChange(col.field, e.target.value)}
              />
            </Grid>
          ))}
        </Grid> */}
        <DataGrid
          rows={filteredRows}
          columns={columns.map((col) => ({
            ...col,
            editable: col.field === 'product' ? true : col.editable,
          }))}
          columnVisibilityModel={{
            maintenanceId: false,
            id: false,
            plantFkId: false,
            aopCaseId: false,
            aopType: false,
            aopYear: false,
            NormParameterMonthlyTransactionId: false,
            // NormParametersId: false,
            idFromApi: false,
          }}
          rowHeight={35}
          processRowUpdate={processRowUpdate}
          onColumnResized={onColumnResized}
          onCellClick={handleCellClick}
          onRowEditCommit={handleRowEditCommit}
          editMode='row'
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          onRowEditStop={handleRowEditStop}
          slotProps={{
            toolbar: { setRows, setRowModesModel },
          }}
          getRowClassName={(params) =>
            params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
          }
          sx={{
            borderRadius: '0px',
            border: `1px solid ${jioColors.border}`,
            backgroundColor: jioColors.background,
            fontSize: '0.8rem',
            ' & .MuiDataGrid-columnHeaderTitleContainer:last-child:after .MuiDataGrid-columnHeaderTitleContainer:after':
              {
                bordeRight: 'none !important',
              },

            '& .MuiDataGrid-cell:last-child:after': {
              borderRight: 'none',
            },
            '& .MuiDataGrid-columnHeader:last-child:after': {
              borderRight: 'none',
            },
            '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer:after':
              {
                borderRight: 'none',
              },
            // Added direct rule for the title container without the pseudo-element:
            '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer':
              {
                borderRight: 'none',
              },
            '& .MuiDataGrid-cell.last-column, & .MuiDataGrid-columnHeaderTitleContainer.last-column & .MuiDataGrid-columnHeader.last-column':
              {
                borderRight: 'none',
              },

            // borderRight: `1px solid ${jioColors.border}`,
            '& .MuiDataGrid-root .MuiDataGrid-cell': {
              fontSize: '0.8rem',
              color: '#A9A9A9',
            },
            '& .MuiDataGrid-root': {
              borderRadius: '0px',
            },
            '& .MuiDataGrid-footerContainer': {
              display: 'none',
            },
            // Remove the direct right border from cells and headers
            '& .MuiDataGrid-cell, & .MuiDataGrid-columnHeaders & .MuiDataGrid-columnHeaderTitleContainer':
              {
                borderRight: 'none',
                position: 'relative',
              },
            // Apply a pseudo-element for a short right border on cells
            '& .MuiDataGrid-cell:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust this percentage as needed for the "short" border
              borderRight: `1px solid ${jioColors.border}`,
            },

            // Apply a similar pseudo-element for header cells
            '& .MuiDataGrid-columnHeaders:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust as needed
              borderRight: `1px solid ${jioColors.border}`,
            },
            '& .MuiDataGrid-columnHeaderTitleContainer:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust as needed
              borderRight: `1px solid ${jioColors.border}`,
            },

            '& .MuiDataGrid-columnHeaders': {
              // borderRight: `1px solid ${jioColors.border}`,
              // backgroundColor: jioColors.headerBg,
              // color: '#FFFFFF',
              backgroundColor: '#FAFAFC',
              color: '#3E4E75',
              fontSize: '0.8rem',
              fontWeight: 600,
              borderBottom: `2px solid ${'#DAE0EF'}`,
              borderTopLeftRadius: '0px',
              borderTopRightRadius: '0px',
            },
            '& .MuiDataGrid-cell': {
              // borderRight: `1px solid ${jioColors.border}`,
              borderBottom: `1px solid ${'#DAE0EF'}`,
              color: '#3E4E75',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              fontSize: '0.8rem',
              cursor: 'pointer',
            },
            '& .MuiDataGrid-row': {
              borderBottom: `1px solid ${jioColors.border}`,
            },
            '& .even-row': {
              backgroundColor: jioColors.rowEven,
            },
            '& .odd-row': {
              backgroundColor: jioColors.rowOdd,
            },
            '& .MuiDataGrid-toolbarContainer': {
              display: 'flex',
              justifyContent: 'flex-end',
              gap: 1,
              paddingRight: 2,
              alignSelf: 'flex-end',
            },
            // '& .MuiDataGrid-columnHeaders .last-column-header': {
            //   paddingRight: '16px',
            // },
            // '& .MuiDataGrid-cell.last-column-cell': {
            //   paddingRight: '16px',
            // },
          }}
        />
      </Box>
      <Box
        sx={{
          marginTop: 2,
          display: 'flex',
          gap: 2,
        }}
      >
        {permissions.addButton && (
          <Button
            variant='contained'
            sx={{
              // marginTop: 2,
              backgroundColor: jioColors.primaryBlue,
              color: jioColors.background,
              borderRadius: 1,
              padding: '8px 24px',
              textTransform: 'none',
              fontSize: '0.875rem',
              fontWeight: 500,
              minWidth: 120, // Same width for consistency
              '&:hover': {
                backgroundColor: '#143B6F',
                boxShadow: 'none',
              },
            }}
            onClick={handleAddRow}
          >
            Add Item
          </Button>
        )}

        <Button
          variant='contained'
          sx={{
            // marginTop: 2,
            backgroundColor: jioColors.primaryBlue,
            color: jioColors.background,
            borderRadius: 1,
            padding: '8px 24px',
            textTransform: 'none',
            fontSize: '0.875rem',
            fontWeight: 500,
            minWidth: 120, // Same width for consistency
            '&:hover': {
              backgroundColor: '#143B6F',
              boxShadow: 'none',
            },
          }}
          // onClick={handleAddRow}
        >
          Save
        </Button>
      </Box>

      {/* <Notification
        open={snackbarOpen}
        message={snackbarMessage}
        severity='error'
        onClose={handleCloseSnackbar}
      /> */}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={handleCloseSnackbar}
      />

      <Dialog
        open={open1}
        onClose={handleClose1}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to delete this row?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose1}>Cancel</Button>
          <Button onClick={deleteTheRecord} autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openRemark} onClose={handleCloseRemark}>
        <DialogTitle>Add Remark</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='remark'
            label='Remark'
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '400px' }}
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
            multiline
            rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseRemark}>Cancel</Button>
          <Button onClick={addRemark}>Add</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openYearData} onClose={handleCloseYearData}>
        <DialogTitle>Add Months Data</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='yearData'
            label="Months's Data"
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '400px' }}
            value={yearData}
            onChange={(e) => setYearData(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseYearData}>Cancel</Button>
          <Button onClick={addYearData}>Add</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={open} onClose={handleCancel} maxWidth='lg' fullWidth>
        <DialogTitle>
          <Typography variant='h6'>
            Day wise monthly Data for Financial Year 2024-2025
          </Typography>
        </DialogTitle>

        <DialogContent>
          <Box sx={{ maxHeight: '80vh', overflowX: 'auto', padding: '10px' }}>
            <Table>
              <TableHead>
                {/* First row: Days 1 to 11 */}
                <TableRow>
                  {days.slice(0, 11).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{
                        textAlign: 'left',
                        fontWeight: 'bold',
                        padding: '6px',
                      }}
                    >
                      {day.date}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>

              <TableBody>
                {/* Values for Days 1 to 11 */}
                <TableRow>
                  {days.slice(0, 11).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{ textAlign: 'left', padding: '6px' }}
                    >
                      <TextField
                        type='number'
                        value={day.value}
                        onChange={(e) =>
                          handleValueChange(index, e.target.value)
                        }
                        size='small'
                        sx={{ width: '85px', marginTop: '2px' }}
                        error={!day.value}
                      />
                    </TableCell>
                  ))}
                </TableRow>

                {/* Second row: Days 12 to 22 */}
                <TableRow>
                  {days.slice(11, 22).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{
                        textAlign: 'left',
                        fontWeight: 'bold',
                        padding: '6px',
                      }}
                    >
                      {day.date}
                    </TableCell>
                  ))}
                </TableRow>

                <TableRow>
                  {days.slice(11, 22).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{ textAlign: 'left', padding: '6px' }}
                    >
                      <TextField
                        type='number'
                        value={day.value}
                        onChange={(e) =>
                          handleValueChange(index + 11, e.target.value)
                        }
                        size='small'
                        sx={{ width: '85px', marginTop: '2px' }}
                        error={!day.value}
                      />
                    </TableCell>
                  ))}
                </TableRow>

                {/* Third row: Days 23 to 30 */}
                <TableRow>
                  {days.slice(22, 31).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{
                        textAlign: 'left',
                        fontWeight: 'bold',
                        padding: '6px',
                      }}
                    >
                      {day.date}
                    </TableCell>
                  ))}
                </TableRow>

                <TableRow>
                  {days.slice(22, 31).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{ textAlign: 'left', padding: '6px' }}
                    >
                      <TextField
                        type='number'
                        value={day.value}
                        onChange={(e) =>
                          handleValueChange(index + 22, e.target.value)
                        }
                        size='small'
                        sx={{ width: '85px', marginTop: '2px' }}
                        error={!day.value}
                      />
                    </TableCell>
                  ))}
                </TableRow>
              </TableBody>
            </Table>
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCancel} variant='outlined' sx={{ mr: 2 }}>
            Discard
          </Button>
          <Button
            onClick={handleSubmit}
            variant='contained'
            sx={{ backgroundColor: jioColors?.headerBg, color: 'white' }}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default DataGridTable
