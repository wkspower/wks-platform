import { useEffect, useState, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  validateRowDataWithRemarks,
  recalcDuration,
} from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'

const SlowdownActivities = () => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const columns = [
    {
      field: 'discription',
      title: 'Slowdown Desc',
      editable: true,
      widthT: 200,
      minWidth: 150,
      type: 'text',
    },
    {
      field: 'maintenanceId',
      title: 'Maintenance ID',
      widthT: 150,
      minWidth: 120,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'maintStartDateTime',
      title: 'SD - From',
      editable: true,
      widthT: 200,
      minWidth: 180,
      type: 'dateTime',
    },
    {
      field: 'maintEndDateTime',
      title: 'SD - To',
      editable: true,
      widthT: 200,
      minWidth: 180,
      type: 'dateTime',
    },
    {
      field: 'durationInHrs',
      title: 'Duration (hrs)',
      editable: true,
      widthT: 150,
      minWidth: 120,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
    },
    {
      field: 'rate',
      title: 'Rate (%)',
      editable: true,
      widthT: 120,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
    },
    {
      field: 'remark',
      title: 'Slowdown Basis',
      editable: true,
      widthT: 250,
      minWidth: 200,
      type: 'textarea',
    },
  ]

  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: true,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: 'Slowdown Activities',
    showImport: false,
    showExport: true,
    ExcelName: `Slowdown Activities - ${AOP_YEAR}`,
    showTitle: true,
  }

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchSlowdownActivitiesData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchSlowdownActivitiesData = async () => {
    setLoading(true)
    try {
      const res = await DataService.getSlowdownActivities(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const data = res?.data || res

      if (!data || data.length === 0) {
        setRows([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const formattedData = data.map((item, index) => ({
        ...item,
        id: item?.id || item?.maintenanceId || index + 1,
        maintenanceId: item?.maintenanceId || item?.id,
        discription: item?.discription || '',
        maintStartDateTime: item?.maintStartDateTime
          ? new Date(item.maintStartDateTime)
          : null,
        maintEndDateTime: item?.maintEndDateTime
          ? new Date(item.maintEndDateTime)
          : null,
        durationInHrs: item?.durationInHrs || null,
        rate: item?.rate || null,
        remark: item?.remark || '',
        originalRemark: item?.remark || '',
      }))

      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching slowdown data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const addTimeOffset = (dateTime) => {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)

    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)

    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Validate required fields
    const requiredFields = [
      'discription',
      'maintStartDateTime',
      'maintEndDateTime',
      'durationInHrs',
      'rate',
    ]
    for (const record of data) {
      for (const field of requiredFields) {
        const value = record[field]
        if (
          value === null ||
          value === undefined ||
          (typeof value === 'string' && value.trim() === '')
        ) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Required field "${field}" is missing for "${record.discription || 'this record'}".`,
            severity: 'error',
          })
          setLoading(false)
          return
        }
      }
    }

    // Validate remarks when data is updated
    const fieldsToCheck = [
      'maintStartDateTime',
      'maintEndDateTime',
      'durationInHrs',
      'rate',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'discription',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    // Validate start date < end date
    for (const record of data) {
      const startDate = new Date(record.maintStartDateTime)
      const endDate = new Date(record.maintEndDateTime)

      if (startDate >= endDate) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Start time must be before end time for "${record.discription || 'this record'}".`,
          severity: 'error',
        })
        setLoading(false)
        return
      }
    }

    try {
      const payload = data.map((item) => ({
        id: item.id && !String(item.id).startsWith('new_') ? item.id : null,
        discription: item.discription,
        maintStartDateTime: addTimeOffset(item.maintStartDateTime),
        maintEndDateTime: addTimeOffset(item.maintEndDateTime),
        durationInHrs: item.durationInHrs,
        rate: item.rate,
        remark: item.remark,
      }))

      console.log('Saving slowdown activities data:', payload)

      const response = await DataService.saveSlowdownActivities(
        keycloak,
        AOP_YEAR,
        payload,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${data.length} changes!`,
        severity: 'success',
      })
      setModifiedCells({})
      await fetchSlowdownActivitiesData()
    } catch (error) {
      console.error('Error saving slowdown data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to save changes. Error: ${error?.message || 'Unknown error'}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await DataService.importSlowdownActivities(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.success || response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchSlowdownActivitiesData()
      } else if (response?.code === 400 && response?.data) {
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Slowdown_Import_Errors_${Date.now()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchSlowdownActivitiesData()
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed. Unable to download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await DataService.exportSlowdownActivities(keycloak, PLANT_ID, AOP_YEAR)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting slowdown data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Custom itemChange handler to automatically calculate duration when dates change
  const customItemChange = useCallback(
    (event, setRowsFunc) => {
      const { dataItem, field, value } = event

      // Only calculate duration when start or end date changes
      if (field !== 'maintStartDateTime' && field !== 'maintEndDateTime') {
        return
      }

      // Get the current row data with the new value
      const updatedRow = { ...dataItem, [field]: value }

      // Calculate duration if both dates are present
      if (updatedRow.maintStartDateTime && updatedRow.maintEndDateTime) {
        const duration = recalcDuration(
          updatedRow.maintStartDateTime,
          updatedRow.maintEndDateTime,
          true, // requiredInHr = true for hours format
        )

        // Update the row with calculated duration
        setRowsFunc((prevRows) =>
          prevRows.map((row) => {
            if (row.id === dataItem.id) {
              return {
                ...row,
                [field]: value,
                durationInHrs: duration,
              }
            }
            return row
          }),
        )

        // Also update modifiedCells to include the calculated duration
        setModifiedCells((prev) => ({
          ...prev,
          [dataItem.id]: {
            ...prev[dataItem.id],
            ...updatedRow,
            durationInHrs: duration,
            inEdit: true,
          },
        }))
      }
    },
    [setModifiedCells],
  )

  const handleAddRow = () => {
    const newRow = {
      id: `new_${Date.now()}`,
      discription: '',
      maintenanceId: '',
      maintStartDateTime: null,
      maintEndDateTime: null,
      durationInHrs: null,
      rate: null,
      remark: '',
      inEdit: true,
    }

    setRows((prevRows) => [...prevRows, newRow])
    setModifiedCells((prev) => ({
      ...prev,
      [newRow.id]: newRow,
    }))
  }

  const deleteRowData = async (dataItem) => {
    setLoading(true)
    try {
      await DataService.deleteSlowdownActivity(keycloak, dataItem.id)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Record deleted successfully!',
        severity: 'success',
      })
      await fetchSlowdownActivitiesData()
    } catch (error) {
      console.error('Error deleting record:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error deleting record!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        handleAddRow={handleAddRow}
        deleteRowData={deleteRowData}
        customItemChange={customItemChange}
        dateCalculationConfig={{
          dateField1: 'maintStartDateTime',
          dateField2: 'maintEndDateTime',
          daysField: 'durationInHrs',
          requiredInHr: true,
        }}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
    </Box>
  )
}

export default SlowdownActivities
