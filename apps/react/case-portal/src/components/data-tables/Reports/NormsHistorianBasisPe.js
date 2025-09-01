import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useRef, useState, useCallback } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { Button } from '@mui/material'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'

const CALL_DELAY_MS = 200

const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()

  // Dynamic data map keyed by exact grid name from API
  // dataMap = { [gridName]: { rows: [], columns: [] } }
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([]) // ordered list from API
  const [loading, setLoading] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore

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
        'TYPE LIST2',
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
    fetchAllGrids()
    // cleanup timers on dependency change
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // Export: gather sheets from each ExcelExport instance and combine into one workbook
  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    if (!keys.length) return

    // find first available ref
    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]
    const baseOptions = baseRef?.workbookOptions?.()
    if (!baseOptions) return

    // collect first sheet from each ref (preserves order of gridNames when possible)
    const sheets = gridNames
      .map((name) => {
        const ref = exportRefs.current[name]
        try {
          const opts = ref?.workbookOptions?.()
          return opts?.sheets?.[0] ? { ...opts.sheets[0] } : null
        } catch {
          return null
        }
      })
      .filter(Boolean)

    if (!sheets.length) return

    // set readable titles (use the original grid name)
    sheets.forEach((s, idx) => {
      s.title = gridNames[idx] || s.title || `Sheet${idx + 1}`
    })

    baseOptions.sheets = sheets
    baseRef.save(baseOptions)
  }, [gridNames])

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Norms Historian Data Basis ${currentDateTime}.xlsx`

  // helper to render Title exactly as API sent (or tweak)
  const renderTitle = (t) => t

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

      <Box display='flex' justifyContent='flex-end' mb='2px'>
        <Button
          variant='contained'
          onClick={exportAllGrids}
          className='btn-save'
        >
          Export
        </Button>
      </Box>

      <Box display='flex' flexDirection='column' gap={2}>
        {gridNames.length === 0 && !loading && (
          <Typography>No grids available for the selected period.</Typography>
        )}

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
    </div>
  )
}

export default ProductionVolumeDataBasisPe
