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
const MODE_TYPES = ['Best Achieved']

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

  // build columns from the 0th record as requested (used for mode-wise fallback)
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

  const fetchModeWiseGrids = useCallback(async () => {
    const modeMap = {}
    for (const grade of MODE_GRADES) {
      for (const mode of MODE_TYPES) {
        try {
          const resp =
          await NormalOperationNormsApiService.getModeWiseNormsDataworkflow(
              keycloak,
              grade,
              mode,
            )
          if (resp?.code !== 200) continue

        // Get data and columns from response
        const raw = resp.data?.data || [] // Changed from mcuNormsValueDTOList to data
        const backendCols = resp.data?.columns || null

        let cols = []
        let enriched = []

        if (backendCols && backendCols.length > 0) {
          // Use backend column definitions
          cols = backendCols
          enriched = enrichColumns(cols)
        } else {
          // Fallback: generate columns from first row if no backend columns
          const first = raw[0] || {}
          cols = Object.keys(first).map((field) => {
            const sample = first[field]
            let type = 'string'
            
            if (typeof sample === 'number') {
              type = 'number'
            } else if (typeof sample === 'boolean') {
              type = 'boolean'
            } else if (typeof sample === 'string' && sample.split('-').length === 3) {
              type = 'date'
            } else if (
              sample !== undefined &&
              sample !== null &&
              sample !== '' &&
              !isNaN(sample)
            ) {
              type = 'number'
            }
            
            return { 
              field, 
              title: field.replace(/([A-Z])/g, ' $1').replace(/_/g, ' ').trim(), 
              type,
              editable: false
            }
          })
          enriched = enrichColumns(cols)
        }

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
      const REPORT_TYPES = [
        'Raw Material',
        'By Products',
        'Cat Chem',
        'Utility Consumption',
      ]

      const nextDataMap = {}
      const nextGridNames = []
      let globalEnrichedCols = null // if one report returns columns, reuse for others

      for (const reportType of REPORT_TYPES) {
        try {
          // NOTE: adjust call if your API expects different signature
          const apiResponse =
            await CrackerReportsApiDataService.finalNormsReport(
              keycloak,
              reportType,
            )

          if (!isMountedRef.current) return

          if (apiResponse?.code !== 200) {
            console.warn(
              `finalNormsReport failed for ${reportType}`,
              apiResponse,
            )
            continue
          }

          const backendCols = apiResponse.data?.columns || null
          // prefer backend-provided columns
          if (!globalEnrichedCols && backendCols && backendCols.length) {
            globalEnrichedCols = enrichColumns(backendCols)
          }

          const rawRows = apiResponse.data?.data || []
          let enrichedCols = globalEnrichedCols

          // fallback: derive columns from first row if no backend columns present yet
          if (!enrichedCols) {
            const sample = rawRows[0] || {}
            const cols = columnsFromFirstRow(sample)
            enrichedCols = enrichColumns(cols)
            globalEnrichedCols = enrichedCols
          }

          const dateFields = (enrichedCols || [])
            .filter((c) => c.type === 'date')
            .map((c) => c.field)
          const numberFields = (enrichedCols || [])
            .filter((c) => c.type === 'number')
            .map((c) => c.field)

          const rowsWithId = (rawRows || []).map((item, index) => {
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
            return {
              ...parsedItem,
              id: `${reportType.replace(/\s+/g, '_')}_${index}`,
              isEditable: false,
            }
          })

          const key = `${MONTH_GRID_NAME} - ${reportType}`
          nextDataMap[key] = { rows: rowsWithId, columns: enrichedCols }
          nextGridNames.push(key)
        } catch (err) {
          console.error(`Error fetching report type ${reportType}:`, err)
        }
      }

      // fetch the 9 mode-wise grids and append below existing grids
      const modeMap = await fetchModeWiseGrids()

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
  const fileName = `NMD_Month_Wise_Raw_Data.xlsx`

  const renderTitle = (t) => t

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden Excel exports for each grid (including the mode grids) */}
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

      {/* <Box display='flex' justifyContent='flex-end' mb='8px'>
        <Button
          variant='contained'
          onClick={exportAllGrids}
          className='btn-save'
        >
          Export
        </Button>
      </Box> */}

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
