import { Box, Button } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const CALL_DELAY_MS = 200

const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()

  // Dynamic data map keyed by exact grid name from API
  // dataMap = { [gridName]: { rows: [], columns: [] } }
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([]) // ordered list from API
  const [loading, setLoading] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)

  const { plantID, yearChanged, oldYear, verticalChange } = dataGridStore
  const [tabIndex, setTabIndex] = useState(0)
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const timeoutIdsRef = useRef([])
  const activeRequestsRef = useRef(0)
  const isMountedRef = useRef(true)
  // dynamic refs for excel exports: exportRefs.current[gridName] = ExcelExportInstance
  const exportRefs = useRef({})

  useEffect(() => {
    return () => {
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [])

  function parseDDMMYYYY(dateStr) {
    if (!dateStr) return null
    const [day, month, year] = dateStr.split('-')
    return new Date(`${year}-${month}-${day}`)
  }

  const enrichColumns = useCallback((backendCols = []) => {
    return backendCols.map((col) => {
      const isTextCol = col.type === 'string'
      const isNumberCol = col.type === 'number'
      // const isDateCol = col.type === 'date' // unused but available
      return {
        ...col,
        title: col.title || col.field,
        filterable: true,
        filter: isTextCol ? 'text' : isNumberCol ? 'numeric' : undefined,
        align: isTextCol ? 'left' : isNumberCol ? 'right' : undefined,
        ...(isNumberCol ? { format: '{0:#.##}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  // Fetch columns + rows for one grid type. Returns { rows, columns }
  const fetchDataForGrid = useCallback(
    async (reportType, StartDate, EndDate) => {
      try {
        const apiResponse = await DataService.getProductionVolDataBasisPe(
          keycloak,
          reportType,
          StartDate,
          EndDate,
        )

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

        const rowsWithId = (apiResponse.data.data || []).map((item, index) => {
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

        return { rows: rowsWithId, columns: enrichedCols }
      } catch (err) {
        console.error(`Error fetching ${reportType}:`, err)
        return { rows: [], columns: [] }
      }
    },
    [keycloak, enrichColumns],
  )

  // Schedule and run a single fetch (keeps loading state correct)
  const scheduleAndRunFetch = useCallback(
    (reportType, delayMs) => {
      const id = setTimeout(async () => {
        activeRequestsRef.current += 1
        if (isMountedRef.current) setLoading(true)

        try {
          // get config before fetching grid (so we have StartDate/EndDate)
          const configData =
            await DataService.getConfigurationExecutionDetails(keycloak)
          if (configData?.code !== 200) return

          const StartDate = configData.data.find(
            (d) => d.Name === 'StartDate',
          )?.AttributeValue
          const EndDate = configData.data.find(
            (d) => d.Name === 'EndDate',
          )?.AttributeValue
          if (!StartDate || !EndDate) return

          const { rows, columns } = await fetchDataForGrid(
            reportType,
            StartDate,
            EndDate,
          )

          if (!isMountedRef.current) return
          setDataMap((prev) => ({ ...prev, [reportType]: { rows, columns } }))
        } catch (err) {
          console.error(`Scheduled fetch failed for ${reportType}:`, err)
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
    [fetchDataForGrid, keycloak],
  )

  // Main: fetch TYPE_LIST then schedule fetching each grid in order
  const fetchAllGrids = useCallback(async () => {
    // clear previous timers
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      setLoading(true)
      const configData =
        await DataService.getConfigurationExecutionDetails(keycloak)
      if (configData?.code !== 200) {
        setLoading(false)
        return
      }
      const StartDate = configData.data.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData.data.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue
      if (!StartDate || !EndDate) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      // request TYPE_LIST
      const typeListResult = await DataService.getProductionVolDataBasisPe(
        keycloak,
        'TYPE LIST1',
        StartDate,
        EndDate,
      )

      let types = []
      if (typeListResult?.code == 200) {
        types = (typeListResult?.data?.data ?? []).map((item) => item.TYPE)
      } else {
        return
      }

      const normalized = [...new Set(types)] // unique, preserve order as returned
      setGridNames(normalized)

      // schedule fetch for each grid with delay to throttle
      normalized.forEach((type, idx) => {
        const delay = idx * CALL_DELAY_MS
        scheduleAndRunFetch(type, delay)
      })
    } catch (err) {
      console.error('Error fetching TYPE_LIST or config:', err)
      setLoading(false)
    }
  }, [keycloak, scheduleAndRunFetch])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
    // cleanup timers on dependency change
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // eslint-disable-next-line no-useless-escape
  const INVALID_SHEET_CHARS_RE = /[\\\/\?\*\[\]\:]/g

  function sanitizeSheetName(name = '', fallback = 'Sheet') {
    let s = String(name || '')
      .replace(INVALID_SHEET_CHARS_RE, ' ')
      .trim()
    if (s.length === 0) s = fallback
    if (s.length > 31) s = s.slice(0, 31) // Excel limit
    return s
  }

  function normalizeCellValue(v) {
    if (v === undefined || v === null) return ''
    // If it's a Date object, keep as Date (Kendo/Excel accepts Date) — but fallback to ISO if not supported
    if (v instanceof Date) return v
    // If it's an object or array, convert to string (avoid injecting nested objects)
    if (typeof v === 'object') {
      try {
        return JSON.stringify(v)
      } catch {
        return String(v)
      }
    }
    return v
  }

  // Replace your existing exportAllGrids with this improved implementation
  const exportAllGrids = useCallback(() => {
    // find any existing ExcelExport ref to use as base for saving
    const keys = Object.keys(exportRefs.current || {})
    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]
    if (!baseRef || typeof baseRef.save !== 'function') return

    // build sheets from gridNames & dataMap (preserve gridNames order)
    const sheets = gridNames
      .map((gridName, idx) => {
        const d = dataMap[gridName] || { rows: [], columns: [] }
        const cols = d.columns || []
        const rows = d.rows || []

        // if no columns and no rows, skip this sheet
        if (!cols.length && !rows.length) return null

        // Build columns for the workbook. Keep autoWidth for nice sizing.
        const sheetColumns = cols.map((c) => ({
          autoWidth: true,
          // Kendo workbook column title isn't used here to render the header row,
          // but we keep title for clarity and potential use.
          title: c.title || c.field || '',
        }))

        // Build an explicit header row so Excel has column headers
        const headerRow = {
          cells: cols.map((c) => ({ value: c.title || c.field || '' })),
        }

        // Build the data rows
        const dataRows = rows.map((r) => {
          return {
            cells: cols.map((c) => {
              const raw = r?.[c.field]
              const value = normalizeCellValue(raw)
              // Kendo workbook accepts JS Date objects as cell.value for date cells
              return { value }
            }),
          }
        })

        // Combine header + data rows (header first)
        const sheetRows = [headerRow, ...dataRows]

        return {
          title: sanitizeSheetName(gridName, `Sheet${idx + 1}`),
          columns: sheetColumns,
          rows: sheetRows,
        }
      })
      .filter(Boolean)

    if (!sheets.length) return

    const workbookOptions = {
      sheets,
    }

    try {
      baseRef.save(workbookOptions)
    } catch (err) {
      console.error('Export save failed:', err)
    }
  }, [gridNames, dataMap])

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Production Target Data Basis ${currentDateTime}.xlsx`

  // helper to render Title exactly as API sent (or tweak)
  const renderTitle = (t) => t

  const PETabs = ['Steady State Norm Basis', 'Overall Consumption Norm Basis']
  const defaultTabs = ['Steady State Norm Basis']

  let activeTabs = defaultTabs
  if (lowerVertName === 'pe') {
    activeTabs = PETabs
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden ExcelExport instances for each grid */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const data = dataMap[name] || { rows: [], columns: [] }
          // function ref to capture the export instance
          const setRef = (ref) => {
            if (ref) exportRefs.current[name] = ref
          }
          return (
            <ExcelExport
              key={`excel-${name}`}
              data={data.rows}
              ref={setRef}
              fileName={fileName}
            >
              {(data.columns || []).map((col) => (
                <ExcelExportColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.field}
                />
              ))}
            </ExcelExport>
          )
        })}
      </div>

      {tabIndex === 0 && (
        <Box display='flex' justifyContent='flex-end' mb='2px'>
          <Button
            variant='contained'
            onClick={exportAllGrids}
            className='btn-save'
          >
            Export
          </Button>
        </Box>
      )}

      <Box display='flex' flexDirection='column' gap={2}>
        {/* {gridNames.length === 0 && !loading && (
          <Typography>No grids available for the selected period.</Typography>
        )} */}

        {tabIndex === 0 && (
          <>
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
          </>
        )}
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasisPe
