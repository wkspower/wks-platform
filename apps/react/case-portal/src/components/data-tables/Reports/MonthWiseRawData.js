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

const MONTH_GRID_NAME = 'Month wise Quantity, Tonnes / Month'

const MonthWiseRawData = () => {
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

  const fetchAndPrepare = useCallback(async () => {
    setLoading(true)
    try {
      // Call the API (no mode/dates). Replace with correct method if different.
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

      if (normKey) {
        const groups = {}
        rowsWithId.forEach((r) => {
          const gNameRaw = r[normKey] || 'Unknown'
          const gName = String(gNameRaw).trim() || 'Unknown'
          if (!groups[gName])
            groups[gName] = { rows: [], columns: enrichedCols }
          groups[gName].rows.push(r)
        })

        // preserve API order if possible; otherwise alphabetical
        const names = Object.keys(groups)
        // build dataMap so first entry uses full parent title + NormType, rest are NormType only
        const nextDataMap = {}
        names.forEach((n, idx) => {
          const key = idx === 0 ? `${MONTH_GRID_NAME} - ${n}` : n
          nextDataMap[key] = groups[n]
        })

        setDataMap(nextDataMap)
        setGridNames(Object.keys(nextDataMap))
      } else {
        const key = MONTH_GRID_NAME
        setDataMap({ [key]: { rows: rowsWithId, columns: enrichedCols } })
        setGridNames([key])
      }
    } catch (err) {
      console.error('Error fetching month-wise raw data:', err)
      setDataMap({})
      setGridNames([])
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, enrichColumns])

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

export default MonthWiseRawData
