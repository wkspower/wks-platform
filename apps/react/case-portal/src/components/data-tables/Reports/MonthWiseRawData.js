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
import { CrackerReportsApiDataService } from 'services/cracker-reports-api-service'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const MONTH_GRID_NAME = 'Final Norms'
const MODE_GRADES = ['4F', '5F', '4F+D']
const MODE_TYPES = ['Best Achieved', 'Expression', 'Yearly Norms']

export default function MonthWiseRawData() {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore
  const isMountedRef = useRef(true)
  const exportRefs = useRef({})

  useEffect(() => {
    return () => {
      isMountedRef.current = false
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
        ...(isNumberCol ? { format: '{0:#.###}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  const findNormTypeKey = (row = {}) => {
    if (!row) return null
    const keys = Object.keys(row)
    const normalize = (s) =>
      String(s || '')
        .replace(/[_\s]/g, '')
        .toLowerCase()
    const candidates = [
      'normtype',
      'norm_type',
      'norm type',
      'normcategory',
      'norm_category',
      'norm category',
    ]
    const map = {}
    keys.forEach((k) => (map[normalize(k)] = k))
    for (const c of candidates) {
      const found = map[normalize(c)]
      if (found) return found
    }
    const containsNorm = keys.find((k) => k.toLowerCase().includes('norm'))
    return containsNorm || null
  }

  // build columns from the 0th record as requested
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

  // build columns from the 0th record in the fixed order and with mapped titles
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

  const fetchModeWiseGrids = useCallback(async () => {
    const modeMap = {}
    for (const grade of MODE_GRADES) {
      for (const mode of MODE_TYPES) {
        try {
          const resp =
            await NormalOperationNormsApiService.getModeWiseNormsData(
              keycloak,
              grade,
              mode,
            )
          if (resp?.code !== 200) continue

          const raw = resp.data?.mcuNormsValueDTOList || []
          const first = raw[0] || {}
          const cols = columnsFromFirstRow(first)
          const enriched = enrichColumns(cols)

          const rowsWithId = raw.map((r, idx) => {
            const parsed = { ...r }
            cols
              .filter((c) => c.type === 'date')
              .forEach((c) => {
                parsed[c.field] = parsed[c.field]
                  ? parseDDMMYYYY(parsed[c.field])
                  : null
              })
            cols
              .filter((c) => c.type === 'number')
              .forEach((c) => {
                parsed[c.field] =
                  parsed[c.field] !== undefined && parsed[c.field] !== null
                    ? Number(parsed[c.field])
                    : null
              })
            return { ...parsed, id: `${grade}_${mode}_${idx}` }
          })

          const key = `${grade} - ${mode}`
          modeMap[key] = { rows: rowsWithId, columns: enriched }
        } catch (err) {
          console.error(`Mode grid fetch failed for ${grade} - ${mode}:`, err)
        }
      }
    }
    return modeMap
  }, [keycloak, enrichColumns])

  const fetchAndPrepare = useCallback(async () => {
    setLoading(true)
    try {
      const apiResponse =
        await CrackerReportsApiDataService.finalNormsReport(keycloak)

      if (!isMountedRef.current) return

      if (apiResponse?.code !== 200) {
        setDataMap({})
        setGridNames([])
        setLoading(false)
        return
      }

      const backendCols = apiResponse.data?.columns || []
      const enrichedCols = enrichColumns(backendCols)

      const dateFields = enrichedCols
        .filter((c) => c.type === 'date')
        .map((c) => c.field)
      const numberFields = enrichedCols
        .filter((c) => c.type === 'number')
        .map((c) => c.field)

      const rawRows = apiResponse.data?.data || []

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

      const sample = rowsWithId[0] || {}
      const normKey = findNormTypeKey(sample)

      let nextDataMap = {}
      let nextGridNames = []

      if (normKey) {
        const groups = {}
        rowsWithId.forEach((r) => {
          const gNameRaw = r[normKey] || 'Unknown'
          const gName = String(gNameRaw).trim() || 'Unknown'
          if (!groups[gName])
            groups[gName] = { rows: [], columns: enrichedCols }
          groups[gName].rows.push(r)
        })

        const names = Object.keys(groups)
        names.forEach((n, idx) => {
          const key = `${MONTH_GRID_NAME} - ${n}`
          nextDataMap[key] = groups[n]
        })
        nextGridNames = Object.keys(nextDataMap)
      } else {
        const key = MONTH_GRID_NAME
        nextDataMap[key] = { rows: rowsWithId, columns: enrichedCols }
        nextGridNames = [key]
      }

      // fetch the 9 mode-wise grids and append below existing grids
      const modeMap = await fetchModeWiseGrids()

      // merge while preserving order: first existing, then mode grids
      const mergedDataMap = { ...nextDataMap, ...modeMap }
      const mergedNames = [...nextGridNames, ...Object.keys(modeMap)]

      if (!isMountedRef.current) return

      setDataMap(mergedDataMap)
      setGridNames(mergedNames)
    } catch (err) {
      console.error('Error fetching month-wise raw data:', err)
      setDataMap({})
      setGridNames([])
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, enrichColumns, fetchModeWiseGrids])

  useEffect(() => {
    fetchAndPrepare()
  }, [fetchAndPrepare, plantID, oldYear, yearChanged])

  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    if (!keys.length) return

    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]
    const baseOptions = baseRef?.workbookOptions?.()
    if (!baseOptions) return

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
  const fileName = `MonthWiseRawData ${currentDateTime}.xlsx`

  const renderTitle = (t) => t

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden Excel exports for each grid (including the new 9) */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const data = dataMap[name] || { rows: [], columns: [] }
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

      <Box display='flex' justifyContent='flex-end' mb='8px'>
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
          <Typography>No grids available.</Typography>
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
