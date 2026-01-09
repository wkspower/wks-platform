// NormsHistorianBasisAromatics1.jsx
import { Box, Button, Typography, Tooltip as MuiTooltip } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { DataGrid } from '@mui/x-data-grid'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics1 = () => {
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
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const SCREEN_NAME = screenTitle?.title

  const timeoutIdsRef = useRef([])
  const isMountedRef = useRef(true)

  // ---------- Logging helpers ----------
  const startTimeRef = useRef(performance.now())
  const logStage = useCallback((stage, extra = '') => {
    const now = performance.now()
    const elapsedS = ((now - startTimeRef.current) / 1000).toFixed(3)
    console.log(
      `[${new Date().toISOString()}] ${stage} +${elapsedS}s ${extra || ''}`,
    )
  }, [])

  useEffect(() => {
    logStage('component-mounted')
    return () => {
      logStage('component-unmounting')
      isMountedRef.current = false
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [])

  // ---------- Preprocess helper ----------
  const preprocessGrid = useCallback(
    (g) => {
      const name = g?.gridName || '(unknown)'
      logStage(
        `preprocess-start:${name}`,
        `rows=${Array.isArray(g.data) ? g.data.length : 0}`,
      )
      const t0 = performance.now()

      const rawRows = Array.isArray(g.data) ? g.data : []
      const backendCols = Array.isArray(g.columns) ? g.columns : []

      const widthValue = backendCols.length > 20 ? 150 : undefined

      const processedCols = backendCols.map((col) => {
        if (col && col.__processed) return col
        return {
          ...(col || {}),
          widthT: col?.width ?? widthValue,
          isEditable: false,
          __processed: true,
        }
      })

      const result = { rows: rawRows, columns: processedCols }

      const t1 = performance.now()
      logStage(`preprocess-end:${name}`, `ms=${(t1 - t0).toFixed(1)}`)
      return result
    },
    [logStage],
  )

  // ---------- API fetch ----------
  const fetchAllGrids = useCallback(async () => {
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    try {
      logStage('fetchAllGrids-start')
      setLoading(true)

      const configData = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      logStage('config-fetched', `ok=${configData?.code === 200}`)

      if (configData?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        logStage('fetchAllGrids-exit:bad-config')
        return
      }

      const StartDate = configData.data.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData.data.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue
      logStage('config-dates', `Start=${StartDate} End=${EndDate}`)

      if (!StartDate || !EndDate) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        logStage('fetchAllGrids-exit:no-dates')
        return
      }

      const apiResponse = await DataService.getProductionVolDataBasisPe(
        keycloak,
        REPORT_TYPE_FOR_ALL,
        StartDate,
        EndDate,
        'null',
        PLANT_ID,
        AOP_YEAR,
      )
      logStage('apiResponse-received', `code=${apiResponse?.code}`)

      if (apiResponse?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        logStage('fetchAllGrids-exit:bad-api')
        return
      }

      const gridsArray = Array.isArray(apiResponse.data)
        ? apiResponse.data
        : Array.isArray(apiResponse.data?.data)
          ? apiResponse.data.data
          : []

      logStage('grids-array-parsed', `count=${gridsArray?.length || 0}`)

      if (!Array.isArray(gridsArray) || gridsArray.length === 0) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        logStage('fetchAllGrids-exit:no-grids')
        return
      }

      const newMap = {}
      const normalizedNames = []

      console.time('preprocess-all-grids')
      for (const g of gridsArray) {
        const name = g.gridName
        if (!name) continue
        normalizedNames.push(name)
        newMap[name] = preprocessGrid(g)
      }
      console.timeEnd('preprocess-all-grids')
      logStage(
        'preprocess-all-grids-finished',
        `grids=${normalizedNames.length}`,
      )

      if (isMountedRef.current) {
        setGridNames(normalizedNames)
        setDataMap(newMap)
      }
    } catch (err) {
      console.error('Error fetching all grids:', err)
      logStage('fetchAllGrids-error', String(err?.message || err))
    } finally {
      if (isMountedRef.current) {
        setLoading(false)
        logStage('fetchAllGrids-finished')
      }
    }
  }, [keycloak, preprocessGrid, logStage])

  useEffect(() => {
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

  // ---------- Helper: convert backend columns to MUI ----------
  const toMuiColumns = useCallback((cols = []) => {
    if (!Array.isArray(cols)) return []
    return cols
      .filter(
        (c) => String(c.field || c.name || '').toUpperCase() !== 'GRID_TYPE', // hide GRID_TYPE
      )
      .map((c, idx) => {
        const width = c.widthT
          ? typeof c.widthT === 'string'
            ? parseInt(c.widthT, 10) || undefined
            : c.widthT
          : undefined
        return {
          field: c.field || c.name || `col_${idx}`,
          headerName: c.title || c.field || c.name || '',
          width: width,
          flex: width ? undefined : 1,
          sortable: false, // removed sorting
          filterable: false, // removed searching/filtering
          disableColumnMenu: true, // disables the column menu
        }
      })
  }, [])

  // ---------- Prepare list of grids ----------
  const gridList = useMemo(() => {
    return gridNames.map((name) => {
      const d = dataMap[name] || { rows: [], columns: [] }
      return { name, rows: d.rows, columns: d.columns }
    })
  }, [gridNames, dataMap])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' flexDirection='column' gap={2}>
        {gridList.map(({ name, rows, columns }) => {
          const normalizedRows = Array.isArray(rows)
            ? rows.map((r, i) => ({ id: r?.id ?? r?.Id ?? i, ...r }))
            : []
          const muiColumns = toMuiColumns(columns)

          return (
            <div key={name}>
              <Typography component='span' className='grid-title'>
                {name}
              </Typography>
              <div
                className='mui-data-grid'
                style={{ height: 500, width: '100%' }}
              >
                <div style={{ height: 500, width: '100%' }}>
                  <DataGrid
                    rows={normalizedRows}
                    columns={muiColumns}
                    disableSelectionOnClick
                    disableColumnFilter
                    className='custom-data-grid'
                    disableColumnSelector
                    disableDensitySelector
                    autoHeight={false}
                    density='standard'
                    rowHeight={30}
                    sx={{ border: 'none' }}
                    pagination={false}
                    hideFooter
                  />
                </div>
              </div>
            </div>
          )
        })}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics1
