import { Box, Backdrop, CircularProgress } from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/TCS/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'

const Shutdown = ({
  PLANT_ID,
  AOP_YEAR,
  currentTab,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterProduction()

  // State management
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Detect numeric fields from data
  const getNumericKeysInAllRows = (rowsData = []) => {
    if (!Array.isArray(rowsData) || rowsData.length === 0) return []

    const allKeys = Array.from(
      rowsData.reduce((set, row) => {
        if (row && typeof row === 'object') {
          Object.keys(row).forEach((k) => set.add(k))
        }
        return set
      }, new Set()),
    )

    return allKeys.filter((key) =>
      rowsData.every((row) => {
        const v = row?.[key]
        if (v === undefined || v === null || String(v).trim() === '')
          return true
        const n = Number(String(v).trim())
        return Number.isFinite(n)
      }),
    )
  }

  // State to store API response metadata (headers and keys)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

  // Fetch Shutdown Data
  const fetchShutdownData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsApiService.getTcsShutdownData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      console.log('TCS Shutdown Response:', response)

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
    } catch (err) {
      console.error('Error fetching Shutdown data:', err)
      setSnackbarData({
        message: `Failed to load Shutdown data. Please try again.`,
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
      fetchShutdownData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchShutdownData])

  // Column configuration for Shutdown - dynamically generated from API response
  const columnConfig = {
    particulates: { editable: false, type: 'text', minWidth: 100, width: 100 },
    durationInDays: {
      editable: true,
      type: 'wholeNumber',
      minWidth: 100,
      width: 100,
    },
    startDate: { editable: true, type: 'dateTime', minWidth: 100, widthT: 200 },
    endDate: { editable: true, type: 'dateTime', minWidth: 100, widthT: 200 },
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

  // Helper function to add IST timezone offset (+5:30) to dates before sending to backend
  const addTimeOffset = (dateTime) => {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  // Save changes
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      console.log('Unit Capacity data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // Format date fields to add IST timezone offset before sending to backend
      const formattedData = data.map((item) => {
        const formatted = { ...item }
        if (formatted.startDate) {
          formatted.startDate = addTimeOffset(formatted.startDate)
        }
        if (formatted.endDate) {
          formatted.endDate = addTimeOffset(formatted.endDate)
        }
        return formatted
      })

      const response = await TcsApiService.saveShutdownData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        formattedData,
      )
      console.log('Save Shutdown response:', response)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Shutdown data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchShutdownData()
    } catch (error) {
      console.error('Error saving Shutdown data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Shutdown data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchShutdownData,
  ])

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
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
        fetchData={fetchShutdownData}
        configType='tcs_shutdown'
        handleRemarkCellClick={handleRemarkCellClick}
        columns={columns}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
      />
    </Box>
  )
}

export default Shutdown
