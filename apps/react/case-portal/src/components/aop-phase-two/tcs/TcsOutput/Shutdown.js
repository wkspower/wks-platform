import { Box, Backdrop, CircularProgress } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import ApproveDialog from '../TcsInput/workflow/ApproveDialog'

const Shutdown = ({
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
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // State to store API response metadata (headers and keys)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

  const [openApproveDialogeBox, setOpenApproveDialogeBox] = useState(false)
  // Approve Dialog handlers
  const closeApproveDialogeBox = () => setOpenApproveDialogeBox(false)
  const handleApprove = (plantId, remark) => {
    console.log('Approved plantId:', plantId, 'Remark:', remark)
    // TODO: Call API to approve the plant
    setSnackbarData({
      message: `Plant ${plantId} approved successfully`,
      severity: 'success',
    })
    setSnackbarOpen(true)
    closeApproveDialogeBox()
  }
  const handleReject = (plantId, remark) => {
    console.log('Rejected plantId:', plantId, 'Remark:', remark)
    // TODO: Call API to reject the plant
    setSnackbarData({
      message: `Plant ${plantId} rejected`,
      severity: 'error',
    })
    setSnackbarOpen(true)
    closeApproveDialogeBox()
  }

  // Fetch Shutdown Data
  const fetchShutdownData = useCallback(async () => {
    if (!SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsOutputApiService.getTcsShutdownData(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      console.log('TCS Shutdown Response:', response)

      if (response?.results && Array.isArray(response.results)) {
        // Create a map of particulates to plantId
        const particulatesMap = new Map()
        let plantIdCounter = 1

        transformedData = response.results.map((item, index) => {
          let plantId = item.plantId

          // If no plantId exists, use particulates field to determine plantId
          if (!plantId && item.particulates) {
            if (!particulatesMap.has(item.particulates)) {
              particulatesMap.set(item.particulates, plantIdCounter++)
            }
            plantId = particulatesMap.get(item.particulates)
          } else if (!plantId) {
            // Fallback if neither plantId nor particulates exists
            plantId = new Date().getTime() + index
          }

          return {
            id: item.id || `row_${index}`,
            ...item,
            plantId: plantId,
            plantName: item.plantName || item.particulates,
            inEdit: false,
            isEditable: false,
          }
        })
      }

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata({ headers: response.headers, keys: response.keys })
      }

      setRows(transformedData)
      setOriginalRows(transformedData)
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
      fetchShutdownData()
    }
  }, [SITE_ID, VERTICAL_ID, AOP_YEAR, fetchShutdownData])

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

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
    showExport: true,
    showTitle: true,
    filterable: false,
    approveBtn: true,
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
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
        readonly={true}
        onApproveClick={() => setOpenApproveDialogeBox(true)}
      />

      {/* Approve Dialog */}
      <ApproveDialog
        open={openApproveDialogeBox}
        onClose={closeApproveDialogeBox}
        onApprove={handleApprove}
        onReject={handleReject}
        entries={rows.map((row) => ({
          id: row.id,
          plantId: row.plantId,
          plantName: row.plantName,
        }))}
      />
    </Box>
  )
}

export default Shutdown
