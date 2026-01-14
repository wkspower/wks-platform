import { Box, Backdrop, CircularProgress } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import {
  validateRowDataWithRemarks,
  recalcDuration,
  recalcEndDate,
  validateDateRanges,
  validateDateOverlaps,
} from 'components/aop-phase-two/common/commonUtilityFunctions'

const Slowdown = ({
  PLANT_ID,
  PLANT_NAME,
  AOP_YEAR,
  currentTab,
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
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // State to store API response metadata (headers and keys)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })
  const [originalRows, setOriginalRows] = useState([])

  // Fetch Shutdown Data
  const fetchSlowdownData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsApiService.getTcsSlowdownData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      console.log('TCS Slowdown Response:', response)

      if (response?.results && Array.isArray(response.results)) {
        transformedData = response.results.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
        }))
      }

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata({ headers: response.headers, keys: response.keys })
      }

      setRows(transformedData)
      setOriginalRows(transformedData)
    } catch (err) {
      console.error('Error fetching Slowdown data:', err)
      setSnackbarData({
        message: `Failed to load Slowdown data. Please try again.`,
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    currentTab.id,
    setSnackbarData,
    setSnackbarOpen,
  ])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchSlowdownData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchSlowdownData])

  // Column configuration for Slowdown - dynamically generated from API response
  const columnConfig = {
    particulates: { editable: false, type: 'text', minWidth: 100, widthT: 100 },
    durationInDays: {
      editable: true,
      type: 'wholeNumber',
      minWidth: 100,
      widthT: 100,
    },
    throughputDuringSlowdown: {
      editable: true,
      type: 'wholeNumber',
      minWidth: 100,
      widthT: 100,
    },
    throughputUOM: {
      editable: true,
      type: 'select',
      minWidth: 80,
      widthT: 100,
      options: [
        { value: 'KBPSD', label: 'KBPSD' },
        { value: 'KTPD', label: 'KTPD' },
      ],
    },
    startDate: { editable: true, type: 'dateTime', minWidth: 150, widthT: 150 },
    endDate: { editable: true, type: 'dateTime', minWidth: 150, widthT: 150 },
    purpose: { editable: true, type: 'text', minWidth: 200, widthT: 200 },
  }

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
    setCurrentRemark(row.purpose || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Track when modifiedCells is cleared and reset inEdit flags
  useEffect(() => {
    if (Object.keys(modifiedCells).length === 0) {
      setRows((prev) =>
        prev.map((row) => ({
          ...row,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells])

  // Custom itemChange handler to validate date overlaps in real-time
  const handleCustomItemChange = useCallback(
    (event, setRowsFunc) => {
      const { dataItem, field, value } = event

      // Only validate when startDate or endDate is changed
      if (field !== 'startDate' && field !== 'endDate') {
        return
      }

      // Get the current row data with the new value
      const updatedRow = { ...dataItem, [field]: value }

      // Check if both dates are present
      if (!updatedRow.startDate || !updatedRow.endDate) {
        return // Skip validation if either date is missing
      }

      const start1 = new Date(updatedRow.startDate)
      const end1 = new Date(updatedRow.endDate)

      // Validate that start date is before end date
      if (start1 > end1) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Start date must be before end date',
          severity: 'error',
        })
        return
      }

      // Check for overlaps with other rows
      setRowsFunc((prevRows) => {
        for (let i = 0; i < prevRows.length; i++) {
          const row = prevRows[i]

          // Skip the current row being edited
          if (row.id === updatedRow.id) continue

          // Skip rows without dates or different particulates
          if (!row.startDate || !row.endDate || !row.particulates) continue
          if (row.particulates !== updatedRow.particulates) continue

          const start2 = new Date(row.startDate)
          const end2 = new Date(row.endDate)

          // Check if date ranges overlap
          if (start1 <= end2 && start2 <= end1) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: `Overlapping slowdown dates for ${updatedRow.particulates}: ${start1.toLocaleDateString()} - ${end1.toLocaleDateString()} overlaps with ${start2.toLocaleDateString()} - ${end2.toLocaleDateString()}`,
              severity: 'error',
            })
            break
          }
        }

        return prevRows
      })
    },
    [setSnackbarOpen, setSnackbarData],
  )

  // Helper function to add IST timezone offset (+5:30) to dates before sending to backend
  const addTimeOffset = (dateTime) => {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  // Save changes - only handles API call and formatting
  const saveChanges = useCallback(
    async (validatedData) => {
      try {
        // Format date fields to add IST timezone offset before sending to backend
        // Set id to null for new items
        const formattedData = validatedData.map((item) => {
          const formatted = { ...item }

          // If this is a new item, set id to null
          if (item.isNew) {
            formatted.id = null
          }

          // Add timezone offset to date fields
          if (formatted.startDate) {
            formatted.startDate = addTimeOffset(formatted.startDate)
          }
          if (formatted.endDate) {
            formatted.endDate = addTimeOffset(formatted.endDate)
          }

          return formatted
        })

        const response = await TcsApiService.saveSlowdownData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          formattedData,
        )
        console.log('Save Slowdown response:', response)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Slowdown data saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchSlowdownData()
      } catch (error) {
        console.error('Error saving Slowdown data:', error)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving Slowdown data!',
          severity: 'error',
        })
      }
    },
    [
      keycloak,
      PLANT_ID,
      AOP_YEAR,
      setSnackbarData,
      setSnackbarOpen,
      fetchSlowdownData,
    ],
  )

  // Validation handler - performs all validations before saving
  const validateData = useCallback(async () => {
    try {
      // Check if there are any modified cells
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // Validation 1: Check for overlapping date ranges
      const allRows = [...rows]

      // Update allRows with modified data
      data.forEach((modifiedRow) => {
        const index = allRows.findIndex((row) => row.id === modifiedRow.id)
        if (index !== -1) {
          allRows[index] = modifiedRow
        } else {
          allRows.push(modifiedRow)
        }
      })

      // Validation: Check that start date is before end date for each row
      const dateRangeError = validateDateRanges(
        allRows,
        'startDate',
        'endDate',
        'particulates',
      )
      if (dateRangeError) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: dateRangeError,
          severity: 'error',
        })
        return
      }

      // Check for overlaps within the same particulate
      const overlapError = validateDateOverlaps(
        allRows,
        'startDate',
        'endDate',
        'particulates',
        'particulates',
        'Overlapping slowdown dates found',
      )
      if (overlapError) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: overlapError,
          severity: 'error',
        })
        return
      }

      // Validation 2: Check if remarks are filled when data is updated
      const fieldsToCheck = [
        'durationInDays',
        'throughputDuringSlowdown',
        'throughputUOM',
        'startDate',
        'endDate',
      ]

      const validationError = validateRowDataWithRemarks(
        data,
        originalRows,
        fieldsToCheck,
        'particulates',
        'purpose',
      )

      if (validationError) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationError,
          severity: 'error',
        })
        return
      }

      // All validations passed, proceed to save
      await saveChanges(data)
    } catch (error) {
      console.error('Error during validation:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error during validation!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    rows,
    originalRows,
    saveChanges,
    setSnackbarOpen,
    setSnackbarData,
  ])

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
          await TcsApiService.deleteSlowdownData(keycloak, id)
          setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Record deleted successfully!',
            severity: 'success',
          })
          fetchSlowdownData()
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
    [
      keycloak,
      PLANT_ID,
      AOP_YEAR,
      fetchSlowdownData,
      setSnackbarData,
      setSnackbarOpen,
    ],
  )

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: true,
    deleteButton: true,
    showAction: true,
    remarksEditable: true,
    showCalculate: false,
    showExport: false,
    showImport: false,
    saveBtnForRemark: true,
    saveBtn: true,
    showWorkFlowBtns: false,
    showTitle: true,
    filterable: false,
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
        rows={rows}
        setRows={setRows}
        fetchData={fetchSlowdownData}
        configType='tcs_slowdown'
        handleRemarkCellClick={handleRemarkCellClick}
        columns={columns}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={validateData}
        deleteRowData={deleteRowData}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
        customItemChange={handleCustomItemChange}
        initialFieldValues={{ particulates: PLANT_NAME }}
        dateCalculationConfig={{
          dateField1: 'startDate',
          dateField2: 'endDate',
          daysField: 'durationInDays',
          requiredInHr: false,
          roundDaysAndDates: true,
        }}
      />
    </Box>
  )
}

export default Slowdown
