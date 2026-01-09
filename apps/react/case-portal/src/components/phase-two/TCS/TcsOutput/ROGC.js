import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'services/phase-two-services/TCS/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/phase-two/common/commonUtilityFunctions'

const ROGC = ({
  PLANT_ID,
  SITE_ID,
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


  // Fetch ROGC Data
  const fetchRogcData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      // TODO: Replace with actual API call once backend is ready
      // const response = getMockRogcResponse()
      const response = await TcsOutputApiService.getTcsRogcData(
        keycloak,
        SITE_ID,
        PLANT_ID,
        AOP_YEAR,
      )
      console.log('TCS ROGC Response:', response)

      if (response?.furnaceData?.length >0 && response?.furnaceData && Array.isArray(response.furnaceData)) {
        // Calculate days dynamically based on financial year
        const getDaysInMonth = (year, month) => {
          return new Date(year, month, 0).getDate()
        }

        // Extract the start year from AOP_YEAR (e.g., "2025-26" -> 2025)
        const startYear = parseInt(AOP_YEAR?.split('-')[0])
        const endYear = startYear + 1

        // Add days row at the beginning
        const daysRow = {
          id: 'days_row',
          furnace: 'Days',
          apr: getDaysInMonth(startYear, 4),
          may: getDaysInMonth(startYear, 5),
          jun: getDaysInMonth(startYear, 6),
          jul: getDaysInMonth(startYear, 7),
          aug: getDaysInMonth(startYear, 8),
          sep: getDaysInMonth(startYear, 9),
          oct: getDaysInMonth(startYear, 10),
          nov: getDaysInMonth(startYear, 11),
          dec: getDaysInMonth(startYear, 12),
          jan: getDaysInMonth(endYear, 1),
          feb: getDaysInMonth(endYear, 2),
          mar: getDaysInMonth(endYear, 3),
          remarks: '-',
          isEditable: false,
          inEdit: false,
        }
        transformedData = [daysRow]

        // Add furnace data
        const furnaceRows = response.furnaceData.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
        }))
        transformedData.push(...furnaceRows)

        // Add average row
        const averageRow = {
          id: 'average_row',
          furnace: 'Average of Duty_Furnace_Cracking',
          ...response.gCalPerHrData,
          remarks: '-',
          isEditable: false,
          inEdit: false,
        }
        transformedData.push(averageRow)
      }

      setRows(transformedData)
      setOriginalRows(transformedData)
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
    if (PLANT_ID && AOP_YEAR && SITE_ID) {
      fetchRogcData()
    }
  }, [PLANT_ID, AOP_YEAR, SITE_ID, fetchRogcData])

  // Generate header names with month-year format
  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])

  // Column configuration for ROGC - hardcoded like FixedConsumption.js
  const columns = useMemo(() => {
    return [
      { field: 'id', title: 'ID', hidden: true },
      {
        field: 'furnace',
        title: 'Furnace',
        width: 150,
        minWidth: 150,
        type: 'text',
        editable: false,
      },
      {
        field: 'apr',
        title: headerMap[4],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'may',
        title: headerMap[5],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'jun',
        title: headerMap[6],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'jul',
        title: headerMap[7],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'aug',
        title: headerMap[8],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'sep',
        title: headerMap[9],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'oct',
        title: headerMap[10],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'nov',
        title: headerMap[11],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'dec',
        title: headerMap[12],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'jan',
        title: headerMap[1],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'feb',
        title: headerMap[2],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'mar',
        title: headerMap[3],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'remarks',
        title: 'Remark',
        editable: true,
        width: 150,
        minWidth: 150,
        type: 'textarea',
      },
    ]
  }, [headerMap])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    // Prevent remark dialog from opening if row is not editable
    if (!row?.isEditable && row?.isEditable !== undefined) {
      return
    }
    setCurrentRemark(row.remarks || '')
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

export default ROGC
