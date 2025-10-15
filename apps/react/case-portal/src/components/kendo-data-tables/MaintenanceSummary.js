import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Select,
  MenuItem,
  Backdrop,
  Box,
  CircularProgress,
  Typography,
  Button,
} from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { validateFields } from 'utils/validationUtils'
export default function MaintenanceSummary() {
  const keycloak = useSession()
  const [row, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [openSaveDialog, setOpenSaveDialog] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const headerMap = generateHeaderNames(AOP_YEAR)
  const thisYear = AOP_YEAR

  // second grid states
  const [rowsP, setRowsP] = useState([])
  const [remarkDialogOpenP, setRemarkDialogOpenP] = useState(false)
  const [currentRemarkP, setCurrentRemarkP] = useState('')
  const [currentRowIdP, setCurrentRowIdP] = useState(null)
  const [modifiedCellsP, setModifiedCellsP] = React.useState({})
  const [enableSaveAddBtnP, setEnableSaveAddBtnP] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const oldYearLabel = useMemo(() => {
    if (!thisYear || !thisYear.includes('-')) return ''
    const [start, end] = thisYear.split('-').map(Number)
    return `${start - 1}-${(end - 1).toString().slice(-2)}`
  }, [thisYear])

  const monthFields = [
    {
      field: 'apr',
      index: 4,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'may',
      index: 5,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'jun',
      index: 6,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'jul',
      index: 7,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'aug',
      index: 8,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'sep',
      index: 9,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'oct',
      index: 10,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'nov',
      index: 11,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'dec',
      index: 12,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'jan',
      index: 1,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'feb',
      index: 2,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
    {
      field: 'mar',
      index: 3,
      editable: true,
      type: 'number',
      format: '{0:#.###}',
      width: 120,
    },
  ]

  const columns = [
    { field: 'plantName', title: 'Plant', width: 120 },
    { field: 'costName', title: 'Cost', width: 120 },
    { field: 'budgetType', title: 'Budget Type', width: 120, hidden: true },
    ...monthFields.map(({ field, index, editable, type, format, width }) => ({
      field,
      title: headerMap[index],
      editable, // Make sure this is passed through
      type,
      format,
      width,
    })),
    { field: 'remark', title: 'Remark', editable: true, width: 120 },
  ]
  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      // Fetch for Consumption Budget
      const resConsumption = await DataService.maintenacegetdata(
        keycloak,
        'ConsumptionBudget',
      )
      const mapped = (resConsumption?.data || []).map((item, index) => ({
        ...item,
        plantName: item.plantName || item.plantName || '',
        IsEditable: item.isEditable,
        originalRemark: item.remark?.trim() || '', // add this
      }))
      setRows(mapped)

      // Fetch for Procurement Budget
      const resProcurement = await DataService.maintenacegetdata(
        keycloak,
        'ProcurementBudget',
      )
      const mappedP = (resProcurement?.data || []).map((item, index) => ({
        ...item,
        plantName: item.plantName || item.plantName || '',
        IsEditable: item.isEditable,
        originalRemark: item.remark?.trim() || '', // add this
      }))
      setRowsP(mappedP)
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
      setRowsP([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, yearChanged, plantID])

  useEffect(() => {
    fetchData()
  }, [fetchData, yearChanged, plantID, keycloak])
  const handleCalculate = () => {}
  const handleCalculateP = () => {}

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  const handleRemarkCellClickP = useCallback((row) => {
    setCurrentRemarkP(row.remark || '')
    setCurrentRowIdP(row.id)
    setRemarkDialogOpenP(true)
  }, [])

  const getAdjustedPermissionsP = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsP = getAdjustedPermissionsP(
    {
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Procurement Budget',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      ExcelName: `${lowerVertName}_Monthly Procurement Budget`,
    },
    isOldYear,
  )
  const getAdjustedPermissionsC = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsC = getAdjustedPermissionsC(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Consumption Budget',
      adjustedPermissions: true,
      // downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Monthly Consumption Budget`,
    },
    isOldYear,
  )

  function omitFields(obj, fields) {
    const result = { ...obj }
    fields.forEach((field) => {
      delete result[field]
    })
    return result
  }
  const handleSaveAll = async () => {
    setLoading(true)
    try {
      // Get modified rows for both grids
      const consumptionData = Object.values(modifiedCells)
      const procurementData = Object.values(modifiedCellsP)

      if (!consumptionData.length && !procurementData.length) {
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setSnackbarOpen(true)
        setLoading(false)
        return
      }
      const requiredFields = ['remark']
      const validationMessageC = validateFields(consumptionData, requiredFields)
      const validationMessageP = validateFields(procurementData, requiredFields)
      if (validationMessageC || validationMessageP) {
        setSnackbarData({
          message: validationMessageC || validationMessageP,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setLoading(false)
        return
      }
      // Fields to omit from payload
      const fieldsToOmit = ['isEditable', 'IsEditable']

      // Combine and clean all modified rows
      const allRows = [
        ...consumptionData.map((row) => omitFields(row, fieldsToOmit)),
        ...procurementData.map((row) => omitFields(row, fieldsToOmit)),
      ]

      // Send as array payload
      await DataService.savemaintenacegetdata(allRows, keycloak)

      setSnackbarData({ message: 'Saved successfully!', severity: 'success' })
      setSnackbarOpen(true)
      setModifiedCells({}) // <-- clear modified cells for Consumption
      setModifiedCellsP({})
      fetchData()
    } catch (err) {
      setSnackbarData({ message: 'Save failed!', severity: 'error' })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }
  const downloadExcelForConfiguration = async () => {
    setLoading(true)
    try {
      await DataService.maintenaceExportdata(keycloak)

      setSnackbarData({ message: 'Export started!', severity: 'success' })
      setSnackbarOpen(true)
    } catch (err) {
      setSnackbarData({ message: 'Export failed!', severity: 'error' })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }
  const budgetMaintenanceExcelFile = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await DataService.maintenaceImportExceldata(rawFile, keycloak)

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - budgetMaintenance.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error uploading Budget Maintenance Excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    budgetMaintenanceExcelFile(rawFile)
  }

  async function handleOpenPdfTempSSRS(title) {
    try {
      let baseurl = ''
      baseurl =
        'http://sjmnpb174/ReportServer/Pages/ReportViewer.aspx?%2fAOPReport%2fConsumptionBudgetSummarySiteWise&rs:Command=Render'

      const url = `${baseurl}`

      window.open(url, '_blank')
      return true
    } catch (e) {
      console.error('Error opening link:', e)
      return Promise.reject(e)
    }
  }

  useEffect(() => {
    handleOpenPdfTempSSRS('Some Title')
  }, [])

  return null // no UI
}
