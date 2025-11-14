import { Box, Tab, Tabs, Typography } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataGrid2 from 'components/Kendo-Report-DataGrid/index-2'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ConsumptionNormsHistorianBasis from './ConsumptionNormsHistorianBasis'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'

const REPORT_TYPE_FOR_ALL = 'NormsHistorian'

const NormsHistorianBasisAromatics2 = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({}) // processed rows + columns
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
  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const timeoutIdsRef = useRef([])
  const isMountedRef = useRef(true)
  const exportRefs = useRef({})

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
    // g should be an object with { data: [...], columns: [...] }
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

  // Fetch all grids — iterate API array in order, and for each grid take gridName, columns then data
  const fetchAllGrids = useCallback(async () => {
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
        'null',
        PLANT_ID,
        AOP_YEAR,
      )
      if (apiResponse?.code !== 200) {
        setGridNames([])
        setDataMap({})
        setLoading(false)
        return
      }

      // Normalize to the array that actually contains grid objects
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
      const orderedNames = []

      // IMPORTANT: iterate in API order, and for each grid use:
      //   const name = g.gridName      // -> grid name
      //   const cols = g.columns       // -> grid columns
      //   const rows = g.data          // -> grid rows/data
      for (const g of gridsArray) {
        // defensive checks
        if (!g) continue
        const name = g.gridName || g.gridname || g.name || null
        if (!name) continue

        // push in the same order received from API
        orderedNames.push(name)

        // build a local object for preprocess (explicitly pass columns then data)
        const gridObjectForPreprocess = {
          columns: Array.isArray(g.columns) ? g.columns : [],
          data: Array.isArray(g.data) ? g.data : [],
        }

        // preprocess keeps stable refs and adds widthT + __processed
        newMap[name] = preprocessGrid(gridObjectForPreprocess)
      }

      if (isMountedRef.current) {
        setGridNames(orderedNames) // API order preserved
        setDataMap(newMap)
      }
    } catch (err) {
      console.error('Error fetching all grids (ordered processing):', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [keycloak, preprocessGrid, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    setTabIndex(0)
    fetchAllGrids()
    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [fetchAllGrids, plantID, oldYear, yearChanged])

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

  // renderGrids: build JSX using an imperative for-loop (no .map)
  const renderGrids = useCallback(() => {
    const out = []
    // use gridNames order (already set from API)
    for (let i = 0; i < gridNames.length; i++) {
      const name = gridNames[i]
      if (!name) continue
      const d = dataMap[name] || { rows: [], columns: [] }
      out.push(
        <div key={name}>
          <Typography component='span' className='grid-title'>
            {renderTitle(name)}
          </Typography>

          <Box sx={{ width: '100%', margin: 0 }}>
            <KendoDataGrid
              rows={d.rows}
              columns={d.columns}
              permissions={{ isHeight: d.rows?.length > 15 }}
            />
          </Box>
        </div>,
      )
    }
    return out
  }, [gridNames, dataMap, renderTitle])

  // Excel export helpers (kept unchanged)
  // eslint-disable-next-line
  const INVALID_SHEET_CHARS_RE = /[\\\/\?\*\[\]\:]/g
  const fileName = `Norms Historian Basis.xlsx`

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

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
        {tabIndex === 0 && renderGrids()}
      </Box>
    </div>
  )
}

export default NormsHistorianBasisAromatics2
