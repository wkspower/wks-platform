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
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const CALL_DELAY_MS = 200

const BestAchievedNorms = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
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
  } = dataGridStore || {}

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const timeoutIdsRef = useRef([])
  const activeRequestsRef = useRef(0)
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

  function parseDDMMYYYY(dateStr) {
    if (!dateStr) return null
    const [day, month, yearStr] = dateStr.split('-')
    return new Date(`${yearStr}-${month}-${day}`)
  }

  const enrichColumns = useCallback((backendCols = []) => {
    const DEFAULT_MIN_WIDTH = 160

    const cols = backendCols.map((col) => {
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
        hidden: col.field === 'Material_FK_Id',
      }
    })

    if (cols.length > 17) {
      return cols.map((c) => ({
        widthT: c.minWidth ?? DEFAULT_MIN_WIDTH,
        ...c,
      }))
    }

    return cols
  }, [])

  const fetchDataForGrid = useCallback(
    async (reportType) => {
      try {
        const apiResponse = await DataService.getBestAchievedNorms(
          keycloak,
          reportType,
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

  const scheduleAndRunFetch = useCallback(
    (reportType, delayMs) => {
      const id = setTimeout(async () => {
        activeRequestsRef.current += 1
        if (isMountedRef.current) setLoading(true)

        try {
          const { rows, columns } = await fetchDataForGrid(reportType)
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
    [fetchDataForGrid],
  )

  const fetchAllGrids = useCallback(async () => {
    // clear previous timers
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      setLoading(true)

      const typeListResult = await DataService.getBestAchievedNorms(
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

      let types = []
      if (typeListResult?.code === 200) {
        types = (typeListResult?.data?.data ?? []).map((item) => item.TYPE)
      } else {
        setLoading(false)
        return
      }

      const normalized = [...new Set(types)]
      setGridNames(normalized)

      normalized.forEach((type, idx) => {
        const delay = idx * CALL_DELAY_MS
        scheduleAndRunFetch(type, delay)
      })
    } catch (err) {
      console.error('Error fetching TYPE_LIST or config:', err)
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, scheduleAndRunFetch])

  useEffect(() => {
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // ---------- EXCEL EXPORT HELPERS (fixed to clone template sheet safely) ----------
  // Build a single sheet containing styled cells that mimic RedHighlightCell2
  const buildStyledSheet = useCallback(
    (
      name,
      data = { rows: [], columns: [] },
      opts = {},
      templateSheet = null,
    ) => {
      const { allRedCell = [], showThreeColors = false } = opts

      // If a template sheet is provided (from workbookOptions), clone it into plain objects
      // and then replace its rows with a fresh array we control. This avoids "rows.push is not a function".
      let sheet
      if (templateSheet) {
        try {
          sheet = JSON.parse(JSON.stringify(templateSheet))
        } catch (err) {
          // fallback to basic sheet if cloning fails for some reason
          sheet = { title: name, rows: [] }
        }
        sheet.title = name || sheet.title || name
        sheet.rows = [] // ensure rows is a plain array
      } else {
        sheet = { title: name, rows: [] }
      }

      // helper to check matched cell (same logic as UI)
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

      // header row
      sheet.rows.push({
        cells: (data.columns || []).map((col) => ({
          value: col.title || col.field,
          bold: true,
          background: '#E6E6E6',
        })),
      })

      // data rows
      ;(data.rows || []).forEach((row) => {
        const cells = (data.columns || []).map((col) => {
          const raw = row[col.field]
          const cellValue =
            raw instanceof Date
              ? raw
              : raw === undefined || raw === null
                ? ''
                : raw

          const cell = { value: cellValue }

          if (showThreeColors && col.field) {
            const matched = findMatchedCell(row, col.field)
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
        })

        sheet.rows.push({ cells })
      })

      return sheet
    },
    [],
  )

  // Combined export using hidden ExcelExport refs + built sheets (uses template sheet if possible)
  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    if (!keys.length) return

    // pick a base ref to call .workbookOptions and .save
    const firstKey = keys.find((k) => !!exportRefs.current[k])
    if (!firstKey) return
    const baseRef = exportRefs.current[firstKey]

    // try to obtain template workbook options (may throw if ref not ready)
    let baseTemplateOptions = null
    try {
      baseTemplateOptions =
        typeof baseRef.workbookOptions === 'function'
          ? baseRef.workbookOptions()
          : null
    } catch (err) {
      baseTemplateOptions = null
    }

    // template sheet (if available) to clone; use first sheet as the template
    const templateSheet =
      baseTemplateOptions && Array.isArray(baseTemplateOptions.sheets)
        ? baseTemplateOptions.sheets[0]
        : null

    // Build sheets in grid order (gridNames)
    const sheets = gridNames
      .map((name, idx) => {
        const d = dataMap[name] || { rows: [], columns: [] }
        const showThree = idx === 0 // replicate your UI: first grid had showThreeColors
        return buildStyledSheet(
          name,
          d,
          { allRedCell, showThreeColors: showThree },
          templateSheet,
        )
      })
      .filter(Boolean)

    if (!sheets.length) return

    // Try to reuse baseTemplateOptions to keep other workbook meta; else create minimal options
    const baseOptions = baseTemplateOptions ? { ...baseTemplateOptions } : {}
    baseOptions.sheets = sheets

    try {
      baseRef.save(baseOptions)
    } catch (err) {
      console.error('Export failed:', err)
    }
  }, [gridNames, dataMap, allRedCell, buildStyledSheet])

  const fileName = `Best Achieved Norms(Min CC)-DATA-SET.xlsx`

  // ---------- UI render ----------
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

      {/* Hidden ExcelExport instances for each grid (we keep these so we can call .save()) */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const d = dataMap[name] || { rows: [], columns: [] }
          const setRef = (ref) => {
            if (ref) exportRefs.current[name] = ref
          }
          return (
            <ExcelExport
              key={`excel-${name}`}
              data={d.rows}
              ref={setRef}
              fileName={fileName}
            >
              {(d.columns || []).map((col) => (
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
                    {name}
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
      </Box>
    </div>
  )
}

export default BestAchievedNorms
