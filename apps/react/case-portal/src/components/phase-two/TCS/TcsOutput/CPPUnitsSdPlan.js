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
import { TcsOutputApiService } from 'services/phase-two-services/TCS/tcsOutputApiService'
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

      const response = await TcsOutputApiService.getCPPUnitsSdPlanData(
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
    gtMaintenance: { editable: true, minWidth: 100, widthT: 150 },
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


  const permissions = {
    showAction: true,
    allAction: true,
    showExport: true,
    showTitle: true,
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
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          paginationConfig={{ threshold: 100, defaultPageSize: 50, pageSizes: [10, 20, 50, 100] }}
          dateCalculationConfig={{
            dateField1: 'shutDownDate',
            dateField2: 'startUpDate',
            daysField: 'noOfDays',
            requiredInHr: false,
          }}
          readonly={true}
        />
      </Stack>
    </Box>
  )
}

export default CPPUnitsSdPlan
