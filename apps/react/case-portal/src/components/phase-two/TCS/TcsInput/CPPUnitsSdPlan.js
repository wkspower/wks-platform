import {
  Box,
  Backdrop,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/TCS/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'

const CPPUnitsSdPlan = ({
  PLANT_ID,
  AOP_YEAR,
  currentTab,
  SITE_ID,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterPhaseTwo()
  // State management
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [apiMetadata, setApiMetadata] = useState({
    headers: [
      'Machine',
      'IBR Due Date',
      'GT Maintenance',
      'No. of Days',
      'Shutdown Date',
      'Startup Date',
      'Major Jobs',
    ],
    keys: [
      'machine',
      'ibrDueDate',
      'gtMaintenance',
      'noOfDays',
      'shutDownDate',
      'startUpDate',
      'majorJobs',
    ],
  })

  // Fetch data
  const fetchData = useCallback(async () => {
    if (!AOP_YEAR || !SITE_ID) {
      console.warn('Missing required params:', { AOP_YEAR, SITE_ID })
      return
    }
    try {
      setLoading(true)
      console.log('Fetching CPP Units SD Plan data with:', {
        AOP_YEAR,
        SITE_ID,
      })

      const response = await TcsApiService.getCPPUnitsSdPlanData(
        keycloak,
        AOP_YEAR,
        SITE_ID,
      )

      const transformedData = (response || []).map((item, index) => ({
        id: item.id || `row_${index}`,
        ...item,
        majorJobs: item.majorJobs || '',
        inEdit: false,
      }))

      console.log('CPP Units SD Plan API Response:', transformedData)
      setRows(transformedData)
      setOriginalRows(transformedData)
    } catch (err) {
      console.error('Error fetching CPP Units SD Plan data:', err)
      setSnackbarData({
        message: 'Failed to load CPP Units SD Plan data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, AOP_YEAR, SITE_ID, setSnackbarData, setSnackbarOpen])

  // Fetch data on mount and when dependencies change
  useEffect(() => {
    console.log('useEffect triggered with:', { PLANT_ID, AOP_YEAR, SITE_ID })
    if (PLANT_ID && AOP_YEAR && SITE_ID) {
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, SITE_ID, fetchData])

  // Column configuration for CPP Units SD Plan
  const columnConfig = {
    machine: { editable: false, type: 'text', minWidth: 100, widthT: 120 },
    ibrDueDate: {
      editable: true,
      type: 'dateTime',
      minWidth: 100,
      widthT: 120,
    },
    gtMaintenance: {
      type: 'multi-select',
      options: [
        { value: 'MI', label: 'MI' },
        { value: 'HGPI', label: 'HGPI' },
        { value: 'IBR', label: 'IBR' },
        { value: 'RLA', label: 'RLA' },
      ],
      editable: true,
      minWidth: 100,
      widthT: 150,
    },
    noOfDays: {
      editable: true,
      type: 'wholeNumber',
      minWidth: 80,
      widthT: 100,
    },
    shutDownDate: {
      editable: true,
      type: 'dateTime',
      minWidth: 100,
      widthT: 120,
    },
    startUpDate: {
      editable: true,
      type: 'dateTime',
      minWidth: 100,
      widthT: 120,
    },
    majorJobs: { editable: true, type: 'textarea', minWidth: 200, widthT: 300 },
  }

  // Build columns dynamically from API metadata
  const columns = useMemo(() => {
    const { headers, keys } = apiMetadata

    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers from backend
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })

    // Build columns using columnConfig for type/formatting
    return Object.entries(columnConfig).map(([key, config]) => ({
      field: key,
      title: columnMap[key] || key,
      ...config,
    }))
  }, [apiMetadata])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.majorJobs || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Helper function to add IST timezone offset (+5:30) to dates before sending to backend
  const addTimeOffset = (dateTime) => {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  // Save handler with API call
  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    var rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = [
      'ibrDueDate',
      'gtMaintenance',
      'noOfDays',
      'shutDownDate',
      'startUpDate',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'machine',
      'majorJobs',
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

    try {
      // Format date fields to add IST timezone offset before sending to backend
      const formattedData = modifiedData.map((item) => {
        const { inEdit, ...rest } = item
        if (rest.ibrDueDate) {
          rest.ibrDueDate = addTimeOffset(rest.ibrDueDate)
        }
        if (rest.shutDownDate) {
          rest.shutDownDate = addTimeOffset(rest.shutDownDate)
        }
        if (rest.startUpDate) {
          rest.startUpDate = addTimeOffset(rest.startUpDate)
        }
        return rest
      })

      console.log('payload', formattedData)

      const response = await TcsApiService.saveCPPUnitsSdPlanData(
        keycloak,
        formattedData,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      fetchData()
    } catch (error) {
      console.error('Error saving CPP Units SD Plan data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // Delete row data
  const deleteRowData = useCallback(
    async (paramsForDelete) => {
      setLoading(true)

      try {
        const { id } = paramsForDelete
        const deleteId = id

        // Check if this is a newly added row (not saved to backend yet)
        const isNewRow = paramsForDelete.isNew

        if (isNewRow) {
          // Just remove from local state
          setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Row removed successfully!',
            severity: 'success',
          })
        } else {
          // Call API to delete from backend
          // await TcsApiService.deleteSlowdownData(
          //   keycloak,
          //   PLANT_ID,
          //   AOP_YEAR,
          //   id,
          // )
          setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Record deleted successfully!',
            severity: 'success',
          })
          fetchData()
        }
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
    },
    [keycloak, PLANT_ID, AOP_YEAR, fetchData, setSnackbarData, setSnackbarOpen],
  )

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    addButton: true,
    deleteButton: true,
    showAction: true,
    allAction: true,
    showTitleNameBusiness: true,
    showTitle: true,
    // showImport: true,
    // downloadExcelBtnFromUI: true,
    // ExcelName: `CPP Units SD Plan - ${AOP_YEAR}`,
  }

  return (
    <Box sx={{ mt: 2 }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack spacing={2}>
        <AdvanceKendoTable
          columns={columns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
          deleteRowData={deleteRowData}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          paginationConfig={{
            threshold: 100,
            defaultPageSize: 50,
            pageSizes: [10, 20, 50, 100],
          }}
          dateCalculationConfig={{
            dateField1: 'shutDownDate',
            dateField2: 'startUpDate',
            daysField: 'noOfDays',
            requiredInHr: false,
            roundDaysAndDates: true,
          }}
        />
      </Stack>
    </Box>
  )
}

export default CPPUnitsSdPlan
