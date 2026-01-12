import { Box, Backdrop, CircularProgress } from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'services/phase-two-services/TCS/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'

const Slowdown = ({
  SITE_ID,
  VERTICAL_ID,
  PLANT_ID,
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

  // Fetch Shutdown Data
  const fetchSlowdownData = useCallback(async () => {
    if (!SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsOutputApiService.getTcsSlowdownData(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      console.log('TCS Slowdown Response:', response)

      if (response?.results && Array.isArray(response.results)) {
        transformedData = response.results.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
          isEditable: false,
        }))
      }

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata({ headers: response.headers, keys: response.keys })
      }

      setRows(transformedData)
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
    SITE_ID,
    VERTICAL_ID,
    AOP_YEAR,
    currentTab.id,
    setSnackbarData,
    setSnackbarOpen,
  ])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (SITE_ID && VERTICAL_ID && AOP_YEAR) {
      fetchSlowdownData()
    }
  }, [SITE_ID, VERTICAL_ID, AOP_YEAR, fetchSlowdownData])

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
      type: 'dropdown',
      minWidth: 80,
      widthT: 100,
      options: [
        { value: 'KBPSD', label: 'KBPSD' },
        { value: 'KTPD', label: 'KTPD' },
        { value: 'TPD', label: 'TPD' },
        { value: 'TPH', label: 'TPH' },
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

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    showExport: true,
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
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
        readonly={true}
      />
    </Box>
  )
}

export default Slowdown
