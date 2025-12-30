import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/tcsApiService'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'

const ROGC = ({
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

  // State to store API response metadata (headers and keys)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

  // Mock API response for ROGC (will be removed once backend is working)
  const getMockRogcResponse = () => {
    return {
      headers: ['Particulars', 'apr', 'may', 'june', 'july', 'aug', 'sep', 'oct', 'nov', 'dec', 'jan', 'feb', 'march', 'remark'],
      keys: ['Particulars', 'apr', 'may', 'june', 'july', 'aug', 'sep', 'oct', 'nov', 'dec', 'jan', 'feb', 'march', 'remark'],
      results: [
        {
          id: 1,
          Particulars: 'F1',
          apr: 0.0,
          may: 2.3,
          june: 0.0,
          july: 0.0,
          aug: 2.3,
          sep: 0.0,
          oct: 0.0,
          nov: 0.0,
          dec: 2.3,
          jan: 18,
          feb: 0.0,
          march: 0.0,
          remark: '',
        },
        {
          id: 2,
          Particulars: 'F2',
          apr: 0.0,
          may: 0.0,
          june: 0.0,
          july: 2.3,
          aug: 0.0,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 0.0,
          jan: 2.3,
          feb: 0.0,
          march: 0.0,
          remark: '',
        },
        {
          id: 3,
          Particulars: 'F3',
          apr: 2.3,
          may: 0.0,
          june: 0.0,
          july: 2.3,
          aug: 0.0,
          sep: 0.0,
          oct: 2.3,
          nov: 0.0,
          dec: 0.0,
          jan: 0.0,
          feb: 0.0,
          march: 0.0,
          remark: '',
        },
      ],
    }
  }

  // Fetch ROGC Data
  const fetchRogcData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      // TODO: Replace with actual API call once backend is ready
      const response = getMockRogcResponse()
      // const response = await TcsApiService.getTcsRogcData(
      //   keycloak,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )
      console.log('TCS ROGC Response:', response)

      if (response?.results && Array.isArray(response.results)) {
        transformedData = response.results.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
        }))

        // Add average row
        const averageRow = {
          id: 'average_row',
          Particulars: 'Average',
          apr: 0.75,
          may: 0.77,
          june: 0.0,
          july: 1.53,
          aug: 0.77,
          sep: 0.0,
          oct: 1.53,
          nov: 0.0,
          dec: 0.77,
          jan: 6.0,
          feb: 0.0,
          march: 0.0,
          remark: '',
          isEditable: false,
          inEdit: false,
        }
        transformedData.push(averageRow)
      }

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata({ headers: response.headers, keys: response.keys })
      }

      setRows(transformedData)
    } catch (err) {
      console.error('Error fetching ROGC data:', err)
      setSnackbarData({
        message: `Failed to load ROGC data. Please try again.`,
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
      fetchRogcData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchRogcData])

  // Generate header names with month-year format
  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])

  // Column configuration for ROGC - uses backend titles except for months which use headerMap
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

    // Month fields that should use headerMap instead of backend title
    const monthFields = {
      apr: headerMap[4],
      may: headerMap[5],
      june: headerMap[6],
      july: headerMap[7],
      aug: headerMap[8],
      sep: headerMap[9],
      oct: headerMap[10],
      nov: headerMap[11],
      dec: headerMap[12],
      jan: headerMap[1],
      feb: headerMap[2],
      march: headerMap[3],
    }

    // Build columns configuration
    const columnConfig = {
      Particulars: { editable: false, type: 'text', minWidth: 150, width: 150 },
      apr: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      may: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      june: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      july: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      aug: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      sep: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      oct: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      nov: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      dec: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      jan: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      feb: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      march: { editable: true, type: 'number1', minWidth: 80, width: 80 },
      remark: { editable: true, type: 'textarea', minWidth: 150, width: 150 },
    }

    return Object.entries(columnConfig).map(([key, config]) => ({
      field: key,
      title: monthFields[key] || columnMap[key] || key,
      ...config,
    }))
  }, [apiMetadata, headerMap])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    // Prevent remark dialog from opening if row is not editable
    if (!row?.isEditable && row?.isEditable !== undefined) {
      return
    }
    setCurrentRemark(row.Particulars || '')
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
      console.log('ROGC data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const response = await TcsApiService.saveRogcData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        data,
      )
      console.log('Save ROGC response:', response)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'ROGC data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchRogcData()
    } catch (error) {
      console.error('Error saving ROGC data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving ROGC data!',
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
    fetchRogcData,
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
        fetchData={fetchRogcData}
        configType='tcs_rogc'
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

export default ROGC
