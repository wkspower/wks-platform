import React, { useCallback, useEffect, useRef, useState, useMemo } from 'react'
import { Box, Tab, Tabs, Typography, Button } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ConsumptionNormsHistorianBasis from './ConsumptionNormsHistorianBasis'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({}) // values will be processed rows + columns (light processing only)
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
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const timeoutIdsRef = useRef([])
  const isMountedRef = useRef(true)
  const exportRefs = useRef({}) // refs for hidden ExcelExport instances

  useEffect(
    () => () => {
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    },
    [],
  )

  // Helper: light preprocess — keep stable refs and a width property; do NOT change data values
  const preprocessGrid = useCallback((g) => {
    const rawRows = Array.isArray(g.data) ? g.data : []
    const backendCols = Array.isArray(g.columns) ? g.columns : []

    const widthValue = backendCols.length > 20 ? '150px' : undefined

    const processedCols = backendCols.map((col) => {
      if (col && col.__processed) return col
      const newCol = {
        ...(col || {}),
        widthT: col?.width ?? widthValue,
        isEditable: false,
        __processed: true,
      }
      return newCol
    })

    return { rows: rawRows, columns: processedCols }
  }, [])

  const fetchAllGrids = useCallback(async () => {
    if(!PLANT_ID || !AOP_YEAR) return
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      setLoading(true)

      const configData = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (configData?.code !== 200) {
        setGridNames([])
        setDataMap({})
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

      const apiResponse = await DataService.getProductionVolDataBasisPe(
        keycloak,
        REPORT_TYPE_FOR_ALL,
        StartDate,
        EndDate,
        null,
        PLANT_ID,
        AOP_YEAR,
      )

      if (apiResponse?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

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

      const newMap = {}
      const normalizedNames = []

      for (const g of gridsArray) {
        const name = g.gridName
        if (!name) continue
        normalizedNames.push(name)
        newMap[name] = preprocessGrid(g) // preprocessed: stable refs, but values unchanged
      }

      if (isMountedRef.current) {
        setGridNames(normalizedNames)
        setDataMap(newMap)
      }
    } catch (err) {
      console.error('Error fetching all grids (new shape):', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, preprocessGrid])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, PLANT_ID, oldYear, yearChanged])

  const renderTitle = useCallback((t) => t, [])

  const PETabs = ['Steady State Norm Basis', 'Overall Consumption Norm Basis']
  const defaultTabs = ['Steady State Norm Basis']
  const activeTabs = lowerVertName === 'pe' ? PETabs : defaultTabs

  // Memoize the grid list so we don't recompute on each render unless dataMap/gridNames change
  const gridList = useMemo(() => {
    return gridNames.map((name) => {
      const d = dataMap[name] || { rows: [], columns: [] }
      return { name, rows: d.rows, columns: d.columns }
    })
  }, [gridNames, dataMap])

  // -----------------------
  // Excel export helpers
  // -----------------------
  // sanitize sheet name (avoid invalid chars / length)
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

  const fileName = `Norms Historian Basis.xlsx`

  const exportAllGrids = useCallback(() => {
    // build sheets array for the workbook
    const sheets = gridNames
      .map((gridName, idx) => {
        const d = dataMap[gridName] || { rows: [], columns: [] }
        const cols = Array.isArray(d.columns) ? d.columns : []
        const rows = Array.isArray(d.rows) ? d.rows : []

        // if no columns provided, infer fields from first row (light, only for headers)
        let colFields = []
        if (cols.length > 0) {
          colFields = cols.map((c) => ({
            field: c.field || c.name || '',
            title: c.title || c.field || c.name || '',
          }))
        } else if (rows.length > 0 && typeof rows[0] === 'object') {
          colFields = Object.keys(rows[0]).map((k) => ({ field: k, title: k }))
        }

        if (!colFields.length && !rows.length) return null

        const sheetColumns = colFields.map((c) => ({
          autoWidth: true,
          title: c.title || c.field || '',
        }))
        const headerRow = {
          cells: colFields.map((c) => ({ value: c.title || c.field || '' })),
        }

        const dataRows = rows.map((r) => ({
          cells: colFields.map((c) => ({
            value: normalizeCellValue(r?.[c.field]),
          })),
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

    // Use first available ExcelExport ref to save a combined workbook
    const keys = Object.keys(exportRefs.current || {})
    const firstKey = keys.find((k) => exportRefs.current[k])
    const baseRef = exportRefs.current[firstKey]
    if (!baseRef || typeof baseRef.save !== 'function') {
      console.error('Excel export reference not available')
      return
    }

    try {
      baseRef.save({ sheets })
    } catch (err) {
      console.error('Export save failed:', err)
    }
  }, [gridNames, dataMap])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden ExcelExport instances for each grid (so we can call save with refs) */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const d = dataMap[name] || { rows: [], columns: [] }
          const setRef = (ref) => {
            if (ref) exportRefs.current[name] = ref
          }

          // prepare columns for ExcelExportColumn if backend provides them,
          // otherwise we won't output explicit columns (workbook built manually in exportAllGrids)
          const cols = Array.isArray(d.columns) ? d.columns : []

          return (
            <ExcelExport
              key={`excel-${name}`}
              data={d.rows || []}
              ref={setRef}
              fileName={fileName}
            >
              {cols.map((col) => (
                <ExcelExportColumn
                  key={col.field || col.title || Math.random()}
                  field={col.field}
                  title={col.title || col.field}
                />
              ))}
            </ExcelExport>
          )
        })}
      </div>

      {/* Export button — kept visible and functional as requested */}
      {activeTabs.length > 0 && tabIndex === 0 && (
        <Box display='flex' justifyContent='flex-end' mb='2px'>
          <Button
            variant='contained'
            onClick={exportAllGrids}
            className='btn-save'
          >
            Export
          </Button>
        </Box>
      )}

      {activeTabs.length > 1 && (
        <Tabs
          value={tabIndex}
          onChange={(e, newIndex) => setTabIndex(newIndex)}
          variant='scrollable'
          scrollButtons='auto'
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '0 0 10px 0',
            minHeight: '28px',
          }}
          textColor='primary'
          indicatorColor='primary'
        >
          {activeTabs.map((label, idx) => (
            <Tab
              key={idx}
              label={label}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />
          ))}
        </Tabs>
      )}

      <Box display='flex' flexDirection='column' gap={2}>
        {tabIndex === 0 &&
          gridList.map(({ name, rows, columns }) => (
            <div key={name}>
              <Typography component='span' className='grid-title'>
                {renderTitle(name)}
              </Typography>

              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rows}
                  columns={columns}
                  permissions={{ isHeight: rows?.length > 15 }}
                />
              </Box>
            </div>
          ))}

        {tabIndex === 1 && <ConsumptionNormsHistorianBasis />}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics
