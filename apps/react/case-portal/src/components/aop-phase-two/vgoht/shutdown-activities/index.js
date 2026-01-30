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

const ShutdownActivities = () => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    {
      field: 'discription',
      title: 'Shutdown Desc',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: true,
      hidden: false,
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
      format: '{0:0.00}',
    },
    {
      field: 'remark',
      title: 'Shutdown Basis',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 200,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchShutdownActivitiesData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchShutdownActivitiesData = async () => {
    setLoading(true)
    try {
      const res = await DataService.getShutdownActivities(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      console.log('Shutdown Activities data:', res)
      const formattedData = res?.map((item, index) => ({
        ...item,
        remark: item.remark || '',
        id: item?.id || item?.maintenanceId || index + 1,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching shutdown activities data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: true,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: true,
    ExcelName: `Shutdown_Activities_${AOP_YEAR}`,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Shutdown Activities',
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

    const fieldsToCheck = [
      'discription',
      'maintStartDateTime',
      'maintEndDateTime',
      'durationInHrs',
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

    const payload = modifiedData
    try {
      console.log('Saving shutdown activities data:', payload)

      const response = await DataService.saveShutdownActivities(
        keycloak,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      await fetchShutdownActivitiesData()
    } catch (error) {
      console.error('Error saving shutdown activities data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
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
      const response = await DataService.importShutdownActivities(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchShutdownActivitiesData()
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
          link.download = `Shutdown_Activities_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchShutdownActivitiesData()
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Failed to import Excel file.',
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
      await DataService.exportShutdownActivities(keycloak, PLANT_ID, AOP_YEAR)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Shutdown Activities data:', error)
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
      remark: '',
      inEdit: true,
      isNew: true,
    }
    setRows([...rows, newRow])
    setModifiedCells({
      ...modifiedCells,
      [newRow.id]: newRow,
    })
  }

  const deleteRowData = async (dataItem) => {
    setLoading(true)
    try {
      await DataService.deleteShutdownActivity(keycloak, dataItem.id)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Record deleted successfully!',
        severity: 'success',
      })
      await fetchShutdownActivitiesData()
    } catch (error) {
      console.error('Error deleting shutdown activity:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to delete record. Please try again.',
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

export default ShutdownActivities
