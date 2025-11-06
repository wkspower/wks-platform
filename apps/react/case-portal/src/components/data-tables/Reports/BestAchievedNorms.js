import { Box, Button, Typography } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useCallback, useEffect, useRef, useState, useMemo } from 'react'
import { useSelector } from 'react-redux'
import { DataSetaApiService } from 'services/data-set-api-service'
import { DataService } from 'services/DataService'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const REPORT_TYPE_FOR_ALL = 'OverallConsumption' // <-- change to your backend's value if needed

// ---------------------------------------------------------------------------
// Helpers (kept lightweight / optimized)
// ---------------------------------------------------------------------------

// eslint-disable-next-line
const INVALID_SHEET_CHARS_RE = /[\\\/\?\*\[\]\:]/g
function sanitizeSheetName(name = '', fallback = 'Sheet') {
  let s = String(name || '')
    .replace(INVALID_SHEET_CHARS_RE, ' ')
    .trim()
  if (s.length === 0) s = fallback
  if (s.length > 31) s = s.slice(0, 31)
  return s
}

function normalizeCellValue(v) {
  if (v === undefined || v === null) return ''
  if (v instanceof Date) return v
  if (typeof v === 'object') {
    try {
      return JSON.stringify(v)
    } catch {
      return String(v)
    }
  }
  return v
}

// Optimized single-pass column inference
function inferColumnsFromRows(rows = []) {
  const stats = {} // { field: { sawNumber, sawDate, sawNonEmpty } }
  for (let i = 0; i < rows.length; i++) {
    const r = rows[i]
    if (!r || typeof r !== 'object') continue
    for (const [k, v] of Object.entries(r)) {
      if (!stats[k])
        stats[k] = { sawNumber: false, sawDate: false, sawNonEmpty: false }
      if (v === undefined || v === null || v === '') continue
      stats[k].sawNonEmpty = true
      if (typeof v === 'number') {
        stats[k].sawNumber = true
        continue
      }
      const numericCandidate = String(v).replace(/[,]/g, '')
      if (!isNaN(Number(numericCandidate))) {
        stats[k].sawNumber = true
        continue
      }
      const d = new Date(v)
      if (!isNaN(d.getTime())) {
        stats[k].sawDate = true
      }
    }
  }

  return Object.keys(stats).map((f) => {
    const st = stats[f]
    let type = 'string'
    if (st.sawNumber) type = 'number'
    else if (st.sawDate) type = 'date'
    return { field: f, title: f, type }
  })
}

// ---------------------------------------------------------------------------
// Lightweight GridPanel component so we can memoize rows/cols per-grid
// ---------------------------------------------------------------------------
function GridPanel({
  name,
  d,
  idx,
  expanded,
  setExpanded,
  allRedCellList,
  showColors,
}) {
  // Support both the old single-expanded-value API (for safety) and the new Set-based API:
  const isExpanded =
    expanded && typeof expanded.has === 'function'
      ? expanded.has(name)
      : expanded === name

  // memoize row/column arrays to keep stable refs for KendoDataGrid and avoid re-renders
  const memoized = useMemo(
    () => ({
      rows: d?.rows || [],
      columns: d?.columns || [],
    }),
    [d?.rows, d?.columns],
  )

  const handleToggle = () => {
    // If expanded is a Set, toggle membership
    if (
      expanded &&
      typeof expanded.has === 'function' &&
      typeof setExpanded === 'function'
    ) {
      setExpanded((prev) => {
        const next = new Set(prev || [])
        if (next.has(name)) next.delete(name)
        else next.add(name)
        return next
      })
      return
    }

    // Fallback to original single-value behavior for compatibility
    if (typeof setExpanded === 'function') {
      setExpanded(isExpanded ? null : name)
    }
  }

  return (
    <div key={name}>
      <CustomAccordion
        expanded={isExpanded}
        onChange={handleToggle}
        disableGutters
      >
        <CustomAccordionSummary
          aria-controls={`${name}-content`}
          id={`${name}-header`}
        >
          <Typography component='span' className='grid-title'>
            {name}
          </Typography>
        </CustomAccordionSummary>
        <CustomAccordionDetails>
          <Box sx={{ width: '100%', margin: 0 }}>
            {isExpanded ? (
              <KendoDataGrid
                rows={memoized.rows}
                columns={memoized.columns}
                permissions={{ isHeight: memoized.rows.length > 15 }}
                {...(idx === 1 && showColors
                  ? { allRedCell: allRedCellList, showThreeColors: true }
                  : {})}
              />
            ) : (
              <Box sx={{ py: 2 }}>Click to load grid</Box>
            )}
          </Box>
        </CustomAccordionDetails>
      </CustomAccordion>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Main component
// ---------------------------------------------------------------------------
export default function BestAchievedNorms() {
  const keycloak = useSession()

  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const [tabIndex, setTabIndex] = useState(0)

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
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const timeoutIdsRef = useRef([])
  const isMountedRef = useRef(true)

  // store the red-cells list and a lookup map for O(1) matching
  const [allRedCellList, setAllRedCellList] = useState([])
  const redLookupRef = useRef(new Map())

  // accordion expanded state — start with a Set so we can expand multiple independently
  const [expanded, setExpanded] = useState(() => new Set())

  // export control (dynamic ExcelExport mount)
  const [isExporting, setIsExporting] = useState(false)
  const workbookRef = useRef(null)
  const excelExportRef = useRef(null)

  useEffect(() => {
    return () => {
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [])

  // ---------------------------------------------------------------------------
  // Column enrichment (kept same except optimized hidden logic)
  // ---------------------------------------------------------------------------
  const enrichColumns = useCallback((backendCols = []) => {
    const filteredCols = backendCols.filter((col) => col.field !== 'GRID_TYPE')
    const applyFixedWidth = filteredCols.length > 15
    const fixedWidth = applyFixedWidth ? 150 : undefined

    return filteredCols.map((col) => {
      const isTextCol = col.type === 'string'
      const isNumberCol = col.type === 'number'
      return {
        ...col,
        title: col.title || col.field,
        filterable: true,
        filter: isTextCol ? 'text' : isNumberCol ? 'numeric' : undefined,
        align: isTextCol ? 'left' : isNumberCol ? 'right' : undefined,
        ...(isNumberCol ? { format: '{0:0.00}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
        // hide Material FK field (both common casings)
        hidden:
          (col.field &&
            (col.field === 'Material_FK_Id' || col.field === 'materialFkId')) ||
          col.hidden,
        // set fixed width when total cols > 15
        ...(fixedWidth ? { widthT: fixedWidth } : {}),
      }
    })
  }, [])

  // ---------------------------------------------------------------------------
  // Normalize row values according to detected column types
  // ---------------------------------------------------------------------------
  function normalizeRowValues(row = {}, columns = []) {
    const parsed = { ...row }
    columns.forEach((c) => {
      const raw = row[c.field]
      if (raw === undefined || raw === null || raw === '') {
        parsed[c.field] = raw === 0 ? 0 : null
        return
      }
      if (c.type === 'number') {
        parsed[c.field] =
          typeof raw === 'number'
            ? raw
            : Number(String(raw).replace(/[,]/g, ''))
        if (Number.isNaN(parsed[c.field])) parsed[c.field] = null
        return
      }
      if (c.type === 'date') {
        const d = new Date(raw)
        parsed[c.field] = !isNaN(d.getTime()) ? d : null
        return
      }
      // strings and objects left as-is (objects will be stringified during export)
    })
    return parsed
  }

  // ---------------------------------------------------------------------------
  // Fetch all grids in one call and build dataMap + gridNames (batched setState)
  // ---------------------------------------------------------------------------
  const fetchAllGrids = useCallback(async () => {
    // clear previous timers if any
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      setLoading(true)

      const apiResponse = await DataService.getBestAchievedNorms(
        keycloak,
        'TYPE LIST',
      )

      const code1 = NormalOperationNormsApiService.BestAchivedColorCodes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '4F',
      )
      const code2 = NormalOperationNormsApiService.BestAchivedColorCodes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '5F',
      )
      const code3 = NormalOperationNormsApiService.BestAchivedColorCodes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '4F+D',
      )

      const [res1, res2, res3] = await Promise.all([code1, code2, code3])

      const mergedData = [
        ...(res1?.data?.data || []),
        ...(res2?.data?.data || []),
        ...(res3?.data?.data || []),
      ].map((obj) => ({
        ...obj,
        // normalized key used by RedHighlightCell2 matching logic
        normParameterFKId: (
          obj.NormParameter_FK_Id ||
          obj.normParameterFKId ||
          ''
        ).toUpperCase(),
      }))

      // build O(1) lookup map keyed by `${normId}|${month}`
      const redLookup = new Map()
      mergedData.forEach((cell) => {
        const normId = (
          cell.normParameterFKId ||
          cell.NormParameter_FK_Id ||
          cell.normParameterFKId ||
          ''
        )
          .toString()
          .toLowerCase()
        const month = (cell.month || '').toString().toLowerCase()
        const key = `${normId}|${month}`
        redLookup.set(key, cell.mode)
      })

      // build dataMap and gridNames in local vars (batch setState)
      if (apiResponse?.code !== 200) {
        if (isMountedRef.current) {
          setAllRedCellList(mergedData)
          redLookupRef.current = redLookup
          setGridNames([])
          setDataMap({})
        }
        return
      }

      const gridsArray = Array.isArray(apiResponse.data)
        ? apiResponse.data
        : Array.isArray(apiResponse.data?.data)
          ? apiResponse.data.data
          : []

      if (!Array.isArray(gridsArray) || gridsArray.length === 0) {
        if (isMountedRef.current) {
          setAllRedCellList(mergedData)
          redLookupRef.current = redLookup
          setGridNames([])
          setDataMap({})
        }
        return
      }

      const normalizedNames = gridsArray.map((g) => g.gridName)

      const newMap = {}
      for (let i = 0; i < gridsArray.length; i++) {
        const g = gridsArray[i]
        const rawRows = Array.isArray(g.data) ? g.data : []
        const inferredCols =
          Array.isArray(g.columns) && g.columns.length
            ? g.columns
            : inferColumnsFromRows(rawRows)
        const enrichedCols = enrichColumns(inferredCols)

        const rowsWithId = rawRows.map((r, idx) => {
          const parsed = normalizeRowValues(r, inferredCols)
          return { ...parsed, id: idx, isEditable: false }
        })

        newMap[g.gridName] = { rows: rowsWithId, columns: enrichedCols }
      }

      if (isMountedRef.current) {
        setAllRedCellList(mergedData)
        redLookupRef.current = redLookup
        setGridNames(normalizedNames)
        setDataMap(newMap)
        // set all grids expanded on first load
        setExpanded(new Set(normalizedNames))
      }
    } catch (err) {
      console.error('Error fetching all grids (new shape):', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, enrichColumns, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // ---------------------------------------------------------------------------
  // Export helpers: build workbookOptions, then mount ExcelExport briefly to call save()
  // ---------------------------------------------------------------------------

  const exportAllGrids = useCallback(async () => {
    try {
      setLoading(true)

      //EXPORT FROM KENDO GRID
      //START

      // const sheets = gridNames
      //   .map((gridName, idx) => {
      //     const d = dataMap[gridName] || { rows: [], columns: [] }
      //     let cols = (d.columns || []).filter(
      //       (c) =>
      //         !(
      //           c &&
      //           (c.field === 'Material_FK_Id' || c.field === 'materialFkId')
      //         ) && !c.hidden,
      //     )
      //     const rows = d.rows || []
      //     if (!cols.length && !rows.length) return null
      //     const sheetColumns = cols.map((c) => ({
      //       autoWidth: true,
      //       title: c.title || c.field || '',
      //     }))
      //     const headerRow = {
      //       cells: cols.map((c) => ({ value: c.title || c.field || '' })),
      //     }
      //     const findMatchedCell = (row, monthField) => {
      //       const normId =
      //         row.materialFKId ||
      //         row.NormParameter_FK_Id ||
      //         row.Material_FK_Id ||
      //         row.NormParameterFKId ||
      //         row.normParameterFKId
      //       if (!normId) return null
      //       const key = `${String(normId).toLowerCase()}|${(monthField || '').toString().toLowerCase()}`
      //       const mode = redLookupRef.current?.get(key)
      //       return mode ? { mode } : null
      //     }
      //     const dataRows = rows.map((r) => ({
      //       cells: cols.map((c) => {
      //         const rawVal = normalizeCellValue(r?.[c.field])
      //         const cell = { value: rawVal }
      //         if (idx === 1 && c.field) {
      //           const monthCandidate = r.month || c.title || c.field || ''
      //           const matched = findMatchedCell(r, monthCandidate)
      //           if (matched) {
      //             if (matched.mode === 'Propane(1Z)') {
      //               cell.background = '#FFD6D6' // light red
      //               cell.color = '#9A0000' // dark red text
      //               cell.bold = true
      //             } else if (matched.mode === 'Propane(2Z)') {
      //               cell.background = '#DFFFD8' // light green
      //               cell.color = '#006400' // dark green text
      //               cell.bold = true
      //             }
      //           }
      //         }
      //         return cell
      //       }),
      //     }))
      //     const sheetRows = [headerRow, ...dataRows]
      //     return {
      //       title: sanitizeSheetName(gridName, `Sheet${idx + 1}`),
      //       columns: sheetColumns,
      //       rows: sheetRows,
      //     }
      //   })
      //   .filter(Boolean)
      // if (!sheets.length) return
      // const workbookOptions = { sheets }
      // workbookRef.current = workbookOptions
      // setIsExporting(true)

      //END

      const response = await DataSetaApiService.bestAchievedMinCCExport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        VERTICAL_NAME,
      )

      if (response?.data?.code === 200) {
        setLoading(false)
      }
    } catch (error) {
      console.error('Export failed:', error)
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (!isExporting) return
    const t = setTimeout(() => {
      try {
        if (excelExportRef.current && workbookRef.current) {
          excelExportRef.current.save(workbookRef.current)
        } else {
          console.error('ExcelExport ref or workbookOptions missing')
        }
      } catch (err) {
        console.error('Export save failed:', err)
      } finally {
        workbookRef.current = null
        setIsExporting(false)
      }
    }, 0)
    return () => clearTimeout(t)
  }, [isExporting])

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Best Achhieved Basis (MIN CC).xlsx`
  const renderTitle = (t) => t
  const defaultTabs = ['TAB1']
  let activeTabs = defaultTabs

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box
        display='flex'
        justifyContent='space-between'
        alignItems='center'
        mb='0px'
      >
        <Typography component='div' className='grid-title' sx={{ mb: 0 }}>
          <span style={{ color: 'red', fontWeight: 'bold' }}>Red</span> -
          Propane (1Z)&nbsp;&nbsp;
          <span style={{ color: 'green', fontWeight: 'bold' }}>Green</span> -
          Propane (2Z)
        </Typography>

        {isExporting && (
          <div style={{ display: 'none' }}>
            <ExcelExport
              data={[]}
              ref={(r) => (excelExportRef.current = r)}
              fileName={fileName}
            />
          </div>
        )}

        <Button
          variant='contained'
          onClick={exportAllGrids}
          className='btn-save'
        >
          Export
        </Button>
      </Box>

      <Box display='flex' flexDirection='column' gap={2}>
        {tabIndex === 0 && (
          <>
            {gridNames.map((name, idx) => {
              if (idx === 0) return null

              const d = dataMap[name] || { rows: [], columns: [] }
              return (
                <GridPanel
                  key={name}
                  name={name}
                  d={d}
                  idx={idx}
                  expanded={expanded}
                  setExpanded={setExpanded}
                  allRedCellList={allRedCellList}
                  showColors={true}
                />
              )
            })}
          </>
        )}
      </Box>
    </div>
  )
}
