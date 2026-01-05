import { Box, Button } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import Notification from 'components/Utilities/Notification'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { CrackerReportsApiDataService } from 'services/cracker-reports-api-service'
import { AOPWorkFlowService } from 'services/AOPWorkFlowService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { getRoleName } from 'services/role-service.js'
import { OptimizerDataApiService } from 'services/optimizer-api-service'
const CALL_DELAY_MS = 20
const MONTH_GRID_NAME = 'Month wise Quantity, Tonnes / Month'

const OptimizerReport = () => {
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const [calculating, setCalculating] = useState(false) // Add this line
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const timeoutIdsRef = useRef([])
  const activeRequestsRef = useRef(0)
  const isMountedRef = useRef(true)
  const exportRefs = useRef({})
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const isCracker = lowerVertName === 'cracker'

  useEffect(() => {
    return () => {
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [])

  function parseDDMMYYYY(dateStr) {
    if (!dateStr) return null
    const [day, month, year] = String(dateStr).split('-')
    return new Date(`${year}-${month}-${day}`)
  }

  const enrichColumns = useCallback((backendCols = []) => {
    return backendCols.map((col) => {
      const isTextCol = col.type === 'string'
      const isNumberCol = col.type === 'number'
      return {
        ...col,
        title: col.title || col.field,
        filterable: true,
        filter: isTextCol ? 'text' : isNumberCol ? 'numeric' : undefined,
        align: isTextCol ? 'left' : isNumberCol ? 'right' : undefined,
        ...(isNumberCol ? { format: '{0:0.0000}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  // build columns fallback order (used when API doesn't return columns)
  const MODE_COLUMNS_ORDER = [
    'sapMaterialCode',
    'normType',
    'materialDisplayName',
    'uom',
    'april',
    'may',
    'june',
    'july',
    'august',
    'september',
    'october',
    'november',
    'december',
    'january',
    'february',
    'march',
  ]

  const FIELD_TITLE_MAP = {
    sapMaterialCode: 'SAP Mat Code',
    normType: 'Type',
    materialDisplayName: 'Particular',
    uom: 'UOM',
    april: 'April',
    may: 'May',
    june: 'June',
    july: 'July',
    august: 'August',
    september: 'September',
    october: 'October',
    november: 'November',
    december: 'December',
    january: 'January',
    february: 'February',
    march: 'March',
  }

  const columnsFromFirstRow = (firstRow = {}) => {
    const first = firstRow || {}
    return MODE_COLUMNS_ORDER.map((f) => {
      const sample = first[f]
      let type = 'string'
      if (typeof sample === 'number') type = 'number'
      else if (typeof sample === 'string' && sample.split('-').length === 3)
        type = 'date'
      else if (
        sample !== undefined &&
        sample !== null &&
        sample !== '' &&
        !isNaN(sample)
      )
        type = 'number'
      return { field: f, title: FIELD_TITLE_MAP[f] || f, type }
    })
  }

  // fetch for Optimizer Input/Output grids — NO grouping by normType any more
  const fetchDataForGrid = useCallback(
    async (reportType, mode) => {
      try {
        const lower = (reportType || '').toLowerCase()
        let apiResponse = null

        if (lower.includes('input')) {
          apiResponse = await CrackerReportsApiDataService.spyroInputReport(
            keycloak,
            reportType,
            mode,
            PLANT_ID,
            AOP_YEAR,
          )
        } else {
          apiResponse = await CrackerReportsApiDataService.spyroOutputReport(
            keycloak,
            reportType,
            mode,
            PLANT_ID,
            AOP_YEAR,
          )
        }

        if (apiResponse?.code !== 200) {
          return { rows: [], columns: [] }
        }

        const backendCols = apiResponse.data.columns || []
        const enrichedCols = enrichColumns(backendCols)

        const dateFields = enrichedCols
          .filter((c) => c.type === 'date')
          .map((c) => c.field)
        const numberFields = enrichedCols
          .filter((c) => c.type === 'number')
          .map((c) => c.field)

        const rawRows = apiResponse.data.data || []

        const rowsWithId = rawRows.map((item, index) => {
          const parsedItem = { ...item }
          dateFields.forEach((f) => {
            parsedItem[f] = item?.[f] ? parseDDMMYYYY(item[f]) : null
          })
          numberFields.forEach((f) => {
            parsedItem[f] =
              item?.[f] !== undefined && item?.[f] !== null
                ? Number(item[f])
                : null
          })
          return { ...parsedItem, id: index, isEditable: false }
        })

        // NO grouping: always return a single grid payload
        return { rows: rowsWithId, columns: enrichedCols }
      } catch (err) {
        console.error(`Error fetching ${reportType} (mode: ${mode}):`, err)
        return { rows: [], columns: [] }
      }
    },
    [keycloak, enrichColumns, PLANT_ID, AOP_YEAR],
  )

  // scheduleAndRunFetch — supports month grids suffixed with reportType
  const scheduleAndRunFetch = useCallback(
    (reportKey, reportType, mode, delayMs) => {
      const id = setTimeout(async () => {
        activeRequestsRef.current += 1
        if (isMountedRef.current) setLoading(true)

        try {
          let result = { rows: [], columns: [] }

          result = await fetchDataForGrid(reportType, mode)

          if (!isMountedRef.current) return

          // set as single grid (no normType-based grouping)
          setDataMap((prev) => ({ ...prev, [reportKey]: result }))
        } catch (err) {
          console.error(`Scheduled fetch failed for ${reportKey}:`, err)
        } finally {
          activeRequestsRef.current -= 1
          if (activeRequestsRef.current <= 0 && isMountedRef.current) {
            activeRequestsRef.current = 0
            setLoading(false)
          }
        }
      }, delayMs)

      timeoutIdsRef.current.push(id)
    },
    [fetchDataForGrid, AOP_YEAR, PLANT_ID],
  )

  // Main: fetch TYPE_LIST then schedule fetching each grid in order
  const fetchAllGrids = useCallback(async () => {
    // clear previous timers
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []
    setDataMap({})

    try {
      setLoading(true)

      // Keep your static result - replace with real TYPE_LIST call if needed
      const typeListResult = {
        code: 200,
        message: 'SP Executed successfully',
        data: {
          data: [
            {
              DisplayOrder: 1,
              TYPE: 'Output',
            },
            {
              DisplayOrder: 2,
              TYPE: 'Input',
            },
          ],
          columns: [
            {
              field: 'DisplayOrder',
              editable: false,
              title: 'DisplayOrder',
              type: 'number',
            },
            {
              field: 'TYPE',
              editable: false,
              title: 'TYPE',
              type: 'string',
            },
          ],
        },
      }

      let types = []
      if (typeListResult?.code == 200) {
        types = (typeListResult?.data?.data ?? []).map((item) => item.TYPE)
      } else {
        return
      }

      const normalized = [...new Set(types)] // unique, preserve order as returned

      // ensure Input types appear first
      const inputFirst = []
      const outputLater = []
      normalized.forEach((t) => {
        if ((t || '').toLowerCase().includes('input')) inputFirst.push(t)
        else outputLater.push(t)
      })
      const orderedTypes = [...inputFirst, ...outputLater]

      const responseForModes = await OptimizerDataApiService.fetchModes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '1',
      )

      const MODE_GRADES =
        responseForModes?.data?.map((mode) => mode?.name).filter(Boolean) || []

      // STATIC

      // const modes = [
      //   { key: '4F', label: '4F' },
      //   { key: '5F', label: '5F' },
      //   { key: '4F+D', label: '4F+D' },
      // ]

      //DYANAMIC

      const modes = MODE_GRADES.map((grade) => ({
        key: grade,
        label: grade,
      }))

      // build grid name list: "<TYPE> - <MODE_LABEL>"
      const expandedGridNames = []
      orderedTypes.forEach((type) => {
        modes.forEach((m) => {
          expandedGridNames.push(`${type} - ${m.label}`)
        })
      })

      // NOTE: removed month-wise grids here — user requested only the 6 upper grids (2 types x 3 modes)
      // previously we appended MONTH_REPORT_TYPES here; that code is intentionally omitted

      setGridNames(expandedGridNames)

      // schedule fetch for each expanded grid with small delays
      expandedGridNames.forEach((gridName, idx) => {
        let typePart = gridName
        let modeKey = modes[0].key

        if (!gridName.startsWith(MONTH_GRID_NAME)) {
          const [tPart, modeLabel] = gridName.split(' - ')
          typePart = tPart
          const modeObj = modes.find((mm) => mm.label === modeLabel)
          modeKey = modeObj ? modeObj.key : modes[0].key
          scheduleAndRunFetch(gridName, typePart, modeKey, idx * CALL_DELAY_MS)
        } else {
          // month grid includes report type after the first " - "
          const parts = gridName.split(' - ')
          // parts[0] === MONTH_GRID_NAME, parts.slice(1).join(' - ') === reportType (safe if reportType contains hyphens)
          const reportTypePart = parts.slice(1).join(' - ')
          // use default mode for month-wise call
          scheduleAndRunFetch(
            gridName,
            reportTypePart,
            modes[0].key,
            idx * CALL_DELAY_MS,
          )
        }
      })
    } catch (err) {
      console.error('Error fetching TYPE_LIST or config:', err)
      setLoading(false)
    }
  }, [scheduleAndRunFetch, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, PLANT_ID, AOP_YEAR, oldYear, yearChanged])

  // Export: gather sheets from each ExcelExport instance and combine into one workbook

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const exportAllGrids = async () => {
    try {
      setLoading(true)

      if (!PLANT_ID || !AOP_YEAR) {
        throw new Error('Plant ID or year not found ')
      }

      const payload = []

      // Await the API call here to ensure completion
      const data = await AOPWorkFlowService.getExcel(
        keycloak,
        payload,
        PLANT_ID,
        AOP_YEAR,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Report downloaded successfully!',
        severity: 'success',
      })

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Optimizer Report ${currentDateTime}.xlsx`

  const renderTitle = (t) => t

  const calculateMonthWiseData = async () => {
    try {
      setCalculating(true)

      if (!PLANT_ID || !AOP_YEAR) {
        throw new Error('Plant ID or year not found ')
      }

      // Call the calculate API
      const calculateResult =
        await CrackerReportsApiDataService.calculateMonthWiseRawData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (calculateResult?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })

        // Refresh all grids after calculation
        await fetchAllGrids()
      } else {
        throw new Error(calculateResult?.message || 'Calculation failed')
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'Calculation failed',
        severity: 'error',
      })
      console.error('Calculation Error:', error)
    } finally {
      setCalculating(false)
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' justifyContent='flex-end' mb='2px' gap={1}>
        {isCracker && (
          <Button
            variant='contained'
            onClick={calculateMonthWiseData}
            disabled={READ_ONLY || calculating || loading}
            className='btn-save'
            color='primary'
          >
            {calculating ? 'Calculating...' : 'Calculate'}
          </Button>
        )}
        <Button
          variant='contained'
          onClick={exportAllGrids}
          className='btn-save'
          disabled={READ_ONLY}
        >
          Export
        </Button>
      </Box>

      <Box display='flex' flexDirection='column' gap={2}>
        {gridNames.map((name) => {
          const d = dataMap[name] || { rows: [], columns: [] }
          return (
            <div key={name}>
              <CustomAccordion defaultExpanded disableGutters>
                <CustomAccordionSummary
                  aria-controls={`${name}-content`}
                  id={`${name}-header`}
                >
                  <Typography component='span' className='grid-title'>
                    {renderTitle(name)}
                  </Typography>
                </CustomAccordionSummary>
                <CustomAccordionDetails>
                  <Box sx={{ width: '100%', margin: 0 }}>
                    <KendoDataGrid
                      rows={d.rows}
                      columns={d.columns}
                      permissions={{ isHeight: d?.rows?.length > 15 }}
                    />
                  </Box>
                </CustomAccordionDetails>
              </CustomAccordion>
            </div>
          )
        })}
      </Box>

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </div>
  )
}

export default OptimizerReport
