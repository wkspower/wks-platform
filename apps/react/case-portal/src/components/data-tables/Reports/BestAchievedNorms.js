import { Box, Button, Typography } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const REPORT_TYPE_FOR_ALL = 'OverallConsumption' // <-- change to your backend's value if needed

const BestAchievedNorms = () => {
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
  const AOP_YEAR = year?.selectedYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const timeoutIdsRef = useRef([])
  const isMountedRef = useRef(true)
  const exportRefs = useRef({})

  const [allRedCell, setAllRedCell] = useState([])

  useEffect(() => {
    return () => {
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [])

  const enrichColumns = useCallback((backendCols = []) => {
    return backendCols
      .filter((col) => col.field !== 'GRID_TYPE')
      .map((col) => {
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
          // hide Material FK field (both common casings)
          hidden:
            (col.field &&
              (col.field === 'Material_FK_Id' ||
                col.field === 'materialFkId')) ||
            col.hidden,
        }
      })
  }, [])

  // ---------------------------------------------------------------------------
  // Infer columns from row objects (returns [{ field, title, type }])
  // ---------------------------------------------------------------------------
  function inferColumnsFromRows(rows = []) {
    const fieldSet = new Set()
    rows.forEach((r) => {
      if (!r || typeof r !== 'object') return
      Object.keys(r).forEach((k) => fieldSet.add(k))
    })

    const fields = Array.from(fieldSet)

    const cols = fields.map((f) => {
      let detectedType = 'string'
      for (const r of rows) {
        if (!r) continue
        const v = r?.[f]
        if (v === undefined || v === null || v === '') continue
        if (typeof v === 'number') {
          detectedType = 'number'
          break
        }
        // detect date-like strings
        const d = new Date(v)
        if (!isNaN(d.getTime())) {
          detectedType = 'date'
          break
        }
        // numeric string (allow commas)
        const numericCandidate = String(v).replace(/[,]/g, '')
        if (!isNaN(Number(numericCandidate))) {
          detectedType = 'number'
          break
        }
      }
      return { field: f, title: f, type: detectedType }
    })

    return cols
  }

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
  // Fetch all grids in one call and build dataMap + gridNames
  // The backend is expected to return: apiResponse.data = [ { gridName, data: [...] }, ... ]
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

      let code1 = NormalOperationNormsApiService.BestAchivedColorCodes(
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

      // code1 = {
      //   code: 200,
      //   message: 'Data fetched successfully',
      //   data: {
      //     data: [
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'January',
      //         mode: 'Propane(1Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'March',
      //         mode: 'Propane(1Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'April',
      //         mode: 'Propane(1Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'June',
      //         mode: 'Propane(1Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'October',
      //         mode: 'Propane(2Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'May',
      //         mode: 'Propane(2Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'June',
      //         mode: 'Propane(2Z)',
      //       },
      //       {
      //         NormParameter_FK_Id: '1583B754-AAE9-46C9-8D1B-761A8933F1B5',
      //         month: 'July',
      //         mode: 'Propane(2Z)',
      //       },
      //     ],
      //   },
      // }

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

      setAllRedCell(mergedData)

      if (apiResponse?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      // Support two possible shapes for convenience:
      // 1) apiResponse.data is the array of grids
      // 2) apiResponse.data.data is the array (older wrappers)
      const gridsArray = Array.isArray(apiResponse.data)
        ? apiResponse.data
        : Array.isArray(apiResponse.data?.data)
          ? apiResponse.data.data
          : []

      if (!Array.isArray(gridsArray) || gridsArray.length === 0) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      const normalizedNames = gridsArray.map((g) => g.gridName)
      setGridNames(normalizedNames)

      const newMap = {}
      gridsArray.forEach((g) => {
        const rawRows = Array.isArray(g.data) ? g.data : []
        const inferredCols =
          Array.isArray(g.columns) && g.columns.length
            ? g.columns
            : inferColumnsFromRows(rawRows)
        const enrichedCols = enrichColumns(inferredCols)

        const rowsWithId = rawRows.map((r, i) => {
          const parsed = normalizeRowValues(r, inferredCols)
          return { ...parsed, id: i, isEditable: false }
        })

        newMap[g.gridName] = { rows: rowsWithId, columns: enrichedCols }
      })

      if (isMountedRef.current) setDataMap(newMap)
    } catch (err) {
      console.error('Error fetching all grids (new shape):', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, enrichColumns])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // ---------------------------------------------------------------------------
  // Excel export helpers (keeps your existing implementation compatible)
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

  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]
    if (!baseRef || typeof baseRef.save !== 'function') return

    const sheets = gridNames
      .map((gridName, idx) => {
        const d = dataMap[gridName] || { rows: [], columns: [] }
        // filter out hidden columns (including Material_FK_Id / materialFkId)
        let cols = (d.columns || []).filter(
          (c) =>
            !(
              c &&
              (c.field === 'Material_FK_Id' || c.field === 'materialFkId')
            ) && !c.hidden,
        )
        const rows = d.rows || []
        if (!cols.length && !rows.length) return null

        const sheetColumns = cols.map((c) => ({
          autoWidth: true,
          title: c.title || c.field || '',
        }))

        const headerRow = {
          cells: cols.map((c) => ({ value: c.title || c.field || '' })),
        }

        // helper to find match for coloring (same logic as UI)
        const findMatchedCell = (row, monthField) => {
          const normId =
            row.materialFKId ||
            row.NormParameter_FK_Id ||
            row.Material_FK_Id ||
            row.NormParameterFKId ||
            row.normParameterFKId
          if (!normId) return null
          return allRedCell?.find((cell) => {
            const monthMatch =
              (cell.month || '').toString().toLowerCase() ===
              (monthField || '').toString().toLowerCase()
            const cellNormId = (
              cell.normParameterFKId ||
              cell.NormParameter_FK_Id ||
              cell.NormParameterFKId ||
              ''
            )
              .toString()
              .toLowerCase()
            const normIdStr = (normId || '').toString().toLowerCase()
            return monthMatch && cellNormId === normIdStr
          })
        }

        const dataRows = rows.map((r) => ({
          cells: cols.map((c) => {
            const rawVal = normalizeCellValue(r?.[c.field])
            const cell = { value: rawVal }

            // Apply coloring for first sheet (replicate UI's showThreeColors === true for idx === 0)
            if (idx === 0 && c.field) {
              const monthCandidate = r.month || c.title || c.field || ''
              const matched = findMatchedCell(r, monthCandidate)
              if (matched) {
                if (matched.mode === 'Propane(1Z)') {
                  cell.background = '#FFD6D6' // light red
                  cell.color = '#9A0000' // dark red text
                  cell.bold = true
                } else if (matched.mode === 'Propane(2Z)') {
                  cell.background = '#DFFFD8' // light green
                  cell.color = '#006400' // dark green text
                  cell.bold = true
                }
              }
            }

            return cell
          }),
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

    const workbookOptions = { sheets }

    try {
      baseRef.save(workbookOptions)
    } catch (err) {
      console.error('Export save failed:', err)
    }
  }, [gridNames, dataMap, allRedCell])

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

      <Typography component='div' className='grid-title' sx={{ mb: 1 }}>
        <span style={{ color: 'red', fontWeight: 'bold' }}>Red</span> - Propane
        (1Z)&nbsp;&nbsp;
        <span style={{ color: 'green', fontWeight: 'bold' }}>Green</span> -
        Propane (2Z)
      </Typography>
      {/* Hidden ExcelExport instances for each grid */}
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
              {(data.columns || [])
                .filter(
                  (col) =>
                    !col.hidden &&
                    col.field !== 'Material_FK_Id' &&
                    col.field !== 'materialFkId',
                )
                .map((col) => (
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
        {tabIndex === 0 && (
          <>
            {gridNames.map((name, idx) => {
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
                          {...(idx === 0
                            ? { allRedCell: allRedCell, showThreeColors: true }
                            : {})}
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

export default BestAchievedNorms
