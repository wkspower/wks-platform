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
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const CALL_DELAY_MS = 200
const MONTH_GRID_NAME = 'Month wise Quantity, Tonnes / Month'

const BestAchievedReport = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore
  const timeoutIdsRef = useRef([])
  const activeRequestsRef = useRef(0)
  const isMountedRef = useRef(true)
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

  // helper to detect NormType key in a row (robust against casing)
  const findNormTypeKey = (row = {}) => {
    const keys = Object.keys(row || {})
    const match = keys.find(
      (k) => k.toLowerCase() === 'normtype' || k.toLowerCase() === 'norm_type',
    )
    return match
  }

  // fetchDataForGrid now returns either {rows, columns} OR { groups: {normName: {rows, columns}} }
  const fetchDataForGrid = useCallback(
    async (reportType, mode) => {
      try {
        const lower = (reportType || '').toLowerCase()
        let apiResponse = null

        if (lower.includes('input')) {
          apiResponse = await CrackerReportsApiDataService.spyroInputReport(
            keycloak,
            reportType,
            mode,
          )
        } else {
          apiResponse = await CrackerReportsApiDataService.spyroOutputReport(
            keycloak,
            reportType,
            mode,
          )
        }

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

        const rawRows = apiResponse.data.data || []

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

        // check if grouping by NormType is required
        const sample = rowsWithId[0]
        const normKey = sample ? findNormTypeKey(sample) : null

        if (normKey) {
          // group rows by NormType value
          const groups = {}
          rowsWithId.forEach((r) => {
            const gName = r[normKey] || 'Unknown'
            if (!groups[gName]) groups[gName] = { rows: [], columns: enrichedCols }
            groups[gName].rows.push(r)
          })

          return { groups }
        }

        return { rows: rowsWithId, columns: enrichedCols }
      } catch (err) {
        console.error(`Error fetching ${reportType} (mode: ${mode}):`, err)
        return { rows: [], columns: [] }
      }
    },
    [keycloak, enrichColumns],
  )

  // fetchMonthWiseGrid also supports NormType grouping (calls finalNormsProductionReport)
  const fetchMonthWiseGrid = useCallback(
    async (mode, reportTypeForCall) => {
      try {
        const apiResponseForRawData = await CrackerReportsApiDataService.finalNormsProductionReport(
          keycloak,
          reportTypeForCall,
          mode,
        )

        if (apiResponseForRawData?.code !== 200) {
          return { rows: [], columns: [] }
        }

        const backendCols = apiResponseForRawData.data.columns || []
        const enrichedCols = enrichColumns(backendCols)

        const dateFields = enrichedCols
          .filter((c) => c.type === 'date')
          .map((c) => c.field)
        const numberFields = enrichedCols
          .filter((c) => c.type === 'number')
          .map((c) => c.field)

        const rawRows = apiResponseForRawData.data.data || []

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

        const sample = rowsWithId[0]
        const normKey = sample ? findNormTypeKey(sample) : null

        if (normKey) {
          const groups = {}
          rowsWithId.forEach((r) => {
            const gName = r[normKey] || 'Unknown'
            if (!groups[gName]) groups[gName] = { rows: [], columns: enrichedCols }
            groups[gName].rows.push(r)
          })
          return { groups }
        }

        return { rows: rowsWithId, columns: enrichedCols }
      } catch (err) {
        console.error('Error fetching month-wise raw data:', err)
        return { rows: [], columns: [] }
      }
    },
    [keycloak, enrichColumns],
  )

  // scheduleAndRunFetch handles grouped results by creating separate grid entries per NormType
  const scheduleAndRunFetch = useCallback(
    (reportKey, reportType, mode, delayMs) => {
      const id = setTimeout(async () => {
        activeRequestsRef.current += 1
        if (isMountedRef.current) setLoading(true)

        try {
          let result = { rows: [], columns: [] }

          if (reportKey === MONTH_GRID_NAME) {
            result = await fetchMonthWiseGrid(mode, reportType)
          } else {
            result = await fetchDataForGrid(reportType, mode)
          }

          if (!isMountedRef.current) return

          // if grouped result, create separate grids named "<reportKey> - <NormType>"
          if (result.groups) {
            const childNames = Object.keys(result.groups).map((g) => `${reportKey} - ${g}`)

            // update gridNames: remove parent and append child names
            setGridNames((prev) => {
              const withoutParent = prev.filter((n) => n !== reportKey)
              // avoid duplicate child names
              const newList = [...withoutParent]
              childNames.forEach((cn) => {
                if (!newList.includes(cn)) newList.push(cn)
              })
              return newList
            })

            // set dataMap entries for each child
            setDataMap((prev) => {
              const next = { ...prev }
              Object.entries(result.groups).forEach(([gName, payload]) => {
                const key = `${reportKey} - ${gName}`
                next[key] = payload
              })
              // also delete any existing parent entry
              delete next[reportKey]
              return next
            })
          } else {
            // normal single-grid result
            setDataMap((prev) => ({ ...prev, [reportKey]: result }))
          }
        } catch (err) {
          console.error(`Scheduled fetch failed for ${reportKey}:`, err)
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
    [fetchDataForGrid, fetchMonthWiseGrid],
  )

  // Main: fetch TYPE_LIST then schedule fetching each grid in order
  const fetchAllGrids = useCallback(async () => {
    // clear previous timers
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      setLoading(true)

      // Keep your static result - replace with real TYPE_LIST call if needed
      const typeListResult = {
        code: 200,
        message: 'SP Executed successfully',
        data: {
          data: [
            {
              DisplayOrder: 1,
              TYPE: 'Spyro Output',
            },
            {
              DisplayOrder: 2,
              TYPE: 'Spyro Input',
            },
          ],
          columns: [
            {
              field: 'DisplayOrder',
              editable: false,
              title: 'DisplayOrder',
              type: 'number',
            },
            {
              field: 'TYPE',
              editable: false,
              title: 'TYPE',
              type: 'string',
            },
          ],
        },
      }

      let types = []
      if (typeListResult?.code == 200) {
        types = (typeListResult?.data?.data ?? []).map((item) => item.TYPE)
      } else {
        return
      }

      const normalized = [...new Set(types)] // unique, preserve order as returned

      // ensure Input types appear first
      const inputFirst = []
      const outputLater = []
      normalized.forEach((t) => {
        if ((t || '').toLowerCase().includes('input')) inputFirst.push(t)
        else outputLater.push(t)
      })
      const orderedTypes = [...inputFirst, ...outputLater]

      // modes to call
      const modes = [
        { key: '4F', label: '4F' },
        { key: '5F', label: '5F' },
        { key: '4F+D', label: '4F+D' },
      ]

      // build grid name list: "<TYPE> - <MODE_LABEL>"
      const expandedGridNames = []
      orderedTypes.forEach((type) => {
        modes.forEach((m) => {
          expandedGridNames.push(`${type} - ${m.label}`)
        })
      })

      // append the special month-wise grid at the end (placeholder)
      expandedGridNames.push(MONTH_GRID_NAME)

      setGridNames(expandedGridNames)

      // schedule fetch for each expanded grid with small delays
      expandedGridNames.forEach((gridName, idx) => {
        let typePart = gridName
        let modeKey = modes[0].key

        if (gridName !== MONTH_GRID_NAME) {
          const [tPart, modeLabel] = gridName.split(' - ')
          typePart = tPart
          const modeObj = modes.find((mm) => mm.label === modeLabel)
          modeKey = modeObj ? modeObj.key : modes[0].key
          scheduleAndRunFetch(gridName, typePart, modeKey, idx * CALL_DELAY_MS)
        } else {
          // for month grid pick a sensible report type (last orderedTypes)
          const fallbackReportType = orderedTypes.length ? orderedTypes[orderedTypes.length - 1] : orderedTypes[0]
          scheduleAndRunFetch(gridName, fallbackReportType, modes[0].key, idx * CALL_DELAY_MS)
        }
      })
    } catch (err) {
      console.error('Error fetching TYPE_LIST or config:', err)
      setLoading(false)
    }
  }, [scheduleAndRunFetch])

  useEffect(() => {
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // Export: gather sheets from each ExcelExport instance and combine into one workbook
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
  const fileName = `Best Achieved Norms(Min CC) ${currentDateTime}.xlsx`

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

export default BestAchievedReport
