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
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const GRID_NAME = 'Intermediate Values'

export default function IntermediateValuesDataSet() {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({}) // { [gridName]: { rows:[], columns:[] } }
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
    const [day, month, year] = dateStr.split('-')
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

  const fetchDataForGrid = useCallback(
    async (reportType) => {
      try {
        const apiResponse =
          await CrackerReportsApiDataService.configurationIntermediateValues(
            keycloak,
            reportType,
            { plantId: plantID, aopYear: oldYear },
          )

        if (apiResponse?.code !== 200) {
          console.warn('[fetchDataForGrid] non-200 response', apiResponse)
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

        // Make ids globally unique across the whole dataset
        let globalIndex = 0
        const rowsWithId = (apiResponse.data.data || []).map((item) => {
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
          parsedItem.id = globalIndex++
          parsedItem.isEditable = false
          return parsedItem
        })

        return { rows: rowsWithId, columns: enrichedCols }
      } catch (err) {
        console.error('[fetchDataForGrid] error', err)
        return { rows: [], columns: [] }
      }
    },
    [keycloak, enrichColumns, plantID, oldYear],
  )

  // Helper: group rows by a field
  const groupRowsByField = (rows, field) => {
    const map = {}
    rows.forEach((r) => {
      const key = r?.[field] ?? 'Unknown'
      const name = typeof key === 'string' ? key : String(key)
      if (!map[name]) map[name] = []
      map[name].push(r)
    })
    return map
  }

  // single-shot fetch, but now split into multiple grids based on NormTypeName-like column
  const loadGrid = useCallback(async () => {
    setLoading(true)
    try {
      const { rows, columns } = await fetchDataForGrid(GRID_NAME)
      if (!isMountedRef.current) return

      // Find a candidate grouping field. Look for common names like "NormTypeName", "NormType", etc.
      const candidateField =
        (columns || []).find((c) => /normtype/i.test(c.field || '')) ||
        (columns || []).find((c) => /normtype/i.test(c.title || '')) ||
        null

      if (!candidateField) {
        // fallback to single grid if no grouping column found
        setDataMap({ [GRID_NAME]: { rows, columns } })
        setGridNames([GRID_NAME])
        return
      }

      const groupField = candidateField.field

      // group rows
      const grouped = groupRowsByField(rows, groupField)

      // build dataMap: for each group, create a grid name and remove the group column from columns (optional)
      const newDataMap = {}
      const newGridNames = []
      Object.keys(grouped).forEach((groupKey) => {
        const gridTitle = `${groupKey}`
        // Remove grouping column from display/export since it's redundant inside a grouped grid
        const colsForGroup = (columns || []).filter(
          (c) => c.field !== groupField,
        )
        newDataMap[gridTitle] = {
          rows: grouped[groupKey],
          columns: colsForGroup,
        }
        newGridNames.push(gridTitle)
      })

      setDataMap(newDataMap)
      setGridNames(newGridNames)
    } catch (err) {
      console.error('[loadGrid] failed', err)
      // keep previous state if present
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [fetchDataForGrid])

  useEffect(() => {
    loadGrid()
    // re-load when plant/year changes
  }, [loadGrid, plantID, oldYear, yearChanged])

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
  const fileName = `IntermediateValues ${currentDateTime}.xlsx`

  const renderTitle = (t) => t

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden ExcelExport instance for each grid (used to build multi-sheet workbook) */}
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
