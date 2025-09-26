import React, { useCallback, useEffect, useRef, useState } from 'react'
import { Box, Button } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

// IMPORTANT: This component expects a single API call that returns an array of grids:
// { code:200, data: [ { gridName: 'RAW-MCU', data: { data: [...rows], columns: [...] } }, ... ] }
// If your DataService signature differs, adapt the call in `fetchAllGridsSingleCall` accordingly.

export default function ConsumptionNormsHistorianBasis() {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear, verticalChange } = dataGridStore

  const [dataMap, setDataMap] = useState({}) // { [gridName]: { rows, columns } }
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const exportRefs = useRef({})
  const isMountedRef = useRef(true)

  useEffect(() => {
    return () => {
      isMountedRef.current = false
    }
  }, [])

  // --- Helpers ---
  function sanitizeSheetName(name = '', fallback = 'Sheet') {
    // eslint-disable-next-line no-useless-escape
    const INVALID_SHEET_CHARS_RE = /[\\\/\?\*\[\]\:]/g
    let s = String(name || '')
      .replace(INVALID_SHEET_CHARS_RE, ' ')
      .trim()
    if (s.length === 0) s = fallback
    if (s.length > 31) s = s.slice(0, 31)
    return s
  }

  function normalizeCellValue(v) {
    if (v === undefined || v === null) return ''
    if (v instanceof Date && !Number.isNaN(v.getTime())) return v
    if (typeof v === 'object') {
      try {
        return JSON.stringify(v)
      } catch {
        return String(v)
      }
    }
    return v
  }

  // Try to parse common API date strings like "Apr 1, 2025, 12:00:00 AM" or ISO.
  function parseApiDate(dateStr) {
    if (!dateStr) return null
    // If it's already a Date
    if (dateStr instanceof Date) return dateStr
    // Try native parsing first
    const maybe = new Date(dateStr)
    if (!Number.isNaN(maybe.getTime())) return maybe
    // Fallback: try removing commas and AM/PM then parse
    try {
      const cleaned = String(dateStr)
        .replace(/,\s*/g, ' ')
        .replace(/(AM|PM)$/i, '')
      const p = new Date(cleaned)
      if (!Number.isNaN(p.getTime())) return p
    } catch (e) {
      //a
    }
    return null
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
        ...(isNumberCol ? { format: '{0:#.##}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  // --- Main: single API call that returns all grids ---
  const fetchAllGridsSingleCall = useCallback(async () => {
    setLoading(true)
    try {
      // Fetch StartDate / EndDate ONCE from config (required by backend)
      const configData =
        await DataService.getConfigurationExecutionDetails(keycloak)
      const StartDate = configData?.data?.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData?.data?.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue

      if (!StartDate || !EndDate) {
        console.warn(
          'StartDate / EndDate missing from configuration. Aborting single-call fetch.',
        )
        setGridNames([])
        setDataMap({})
        return
      }

      // SINGLE API CALL: pass TYPE LIST1 and Start/End dates as you requested
      const apiResp = await DataService.getProductionVolDataBasisPe(
        keycloak,
        'TYPE LIST3',
        StartDate,
        EndDate,
      )

      if (!apiResp || apiResp.code !== 200) {
        setGridNames([])
        setDataMap({})
        return
      }

      // apiResp.data is expected to be an array of grids as in your sample
      const grids = Array.isArray(apiResp.data)
        ? apiResp.data
        : apiResp.data?.data || []

      const nextDataMap = {}
      const order = []

      grids.forEach((g, idx) => {
        const gridName = g.gridName || g.TYPE || `Sheet${idx + 1}`
        order.push(gridName)

        const backendCols = (g.data && g.data.columns) || []
        const enrichedCols = enrichColumns(backendCols)

        // Identify date and number fields so we can coerce values
        const dateFields = enrichedCols
          .filter((c) => c.type === 'date')
          .map((c) => c.field)
        const numberFields = enrichedCols
          .filter((c) => c.type === 'number')
          .map((c) => c.field)

        const rows = (g.data && g.data.data ? g.data.data : []).map(
          (item, ix) => {
            const parsed = { ...item }
            dateFields.forEach((f) => {
              parsed[f] = item?.[f] ? parseApiDate(item[f]) : null
            })
            numberFields.forEach((f) => {
              const raw = item?.[f]
              parsed[f] =
                raw === undefined || raw === null || raw === ''
                  ? null
                  : Number(raw)
            })
            return { ...parsed, id: ix, isEditable: false }
          },
        )

        nextDataMap[gridName] = { rows, columns: enrichedCols }
      })

      if (!isMountedRef.current) return
      setGridNames(order)
      setDataMap(nextDataMap)
    } catch (err) {
      console.error('Single-call fetch failed:', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, enrichColumns])

  useEffect(() => {
    // re-fetch when primary inputs change
    fetchAllGridsSingleCall()
  }, [fetchAllGridsSingleCall, plantID, oldYear, yearChanged])

  // --- Export -- keep previous workbook builder (header row + data rows) ---
  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]
    if (!baseRef || typeof baseRef.save !== 'function') return

    const sheets = gridNames
      .map((gridName, idx) => {
        const d = dataMap[gridName] || { rows: [], columns: [] }
        const cols = d.columns || []
        const rows = d.rows || []
        if (!cols.length && !rows.length) return null

        const sheetColumns = cols.map((c) => ({
          autoWidth: true,
          title: c.title || c.field || '',
        }))
        const headerRow = {
          cells: cols.map((c) => ({ value: c.title || c.field || '' })),
        }
        const dataRows = rows.map((r) => ({
          cells: cols.map((c) => ({ value: normalizeCellValue(r?.[c.field]) })),
        }))
        const sheetRows = [headerRow, ...dataRows]

        return {
          title: sanitizeSheetName(gridName, `Sheet${idx + 1}`),
          columns: sheetColumns,
          rows: sheetRows,
        }
      })
      .filter(Boolean)

    if (!sheets.length) return
    try {
      baseRef.save({ sheets })
    } catch (err) {
      console.error('Export save failed:', err)
    }
  }, [gridNames, dataMap])

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Consumption Norms Data Basis ${currentDateTime}.xlsx`

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
