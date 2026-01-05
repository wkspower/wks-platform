import {
  Box,
  Button,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material'
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
import { OptimizerDataApiService } from 'services/optimizer-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

// Only steam modes and their grids — no other GRID_CONFIGS or fetchers present
// const steamModes = ['5F', '4F', '4F+D']

const RawDataSet = () => {
  const keycloak = useSession()
  const [periodFrom, setPeriodFrom] = useState(null)
  const [periodTo, setPeriodTo] = useState(null)
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
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const SCREEN_NAME = screenTitle?.title

  const isMountedRef = useRef(true)
  const exportRefs = useRef({})
  const [exportTarget, setExportTarget] = useState('ALL') // 'ALL' or specific steam grid

  useEffect(() => {
    return () => {
      isMountedRef.current = false
    }
  }, [])

  useEffect(() => {
    async function fetchPeriod() {
      try {
        const resp =
          await CrackerReportsApiDataService.getConfigurationExecutionDetails(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )
        if (Array.isArray(resp?.data)) {
          const start = resp.data.find((d) => d.Name === 'StartDate')
          const end = resp.data.find((d) => d.Name === 'EndDate')
          if (start?.AttributeValue) setPeriodFrom(start.AttributeValue)
          if (end?.AttributeValue) setPeriodTo(end.AttributeValue)
        }
      } catch (e) {
        // ignore
      }
    }
    fetchPeriod()
  }, [keycloak])

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
        ...(isNumberCol ? { format: '{0:0.000}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  // Fetch only steam-related grids
  const fetchModeGrid = useCallback(
    async (type, mode) => {
      let apiResponse
      if (type === 'Finding Steam') {
        apiResponse = await CrackerReportsApiDataService.getRawasfindingteam(
          keycloak,
          mode,
          PLANT_ID,
          AOP_YEAR,
        )
      } else if (type === 'Raw Steam') {
        apiResponse = await CrackerReportsApiDataService.getRawasteam(
          keycloak,
          periodFrom,
          periodTo,
          mode,
          PLANT_ID,
          AOP_YEAR,
        )
      } else {
        return { rows: [], columns: [] }
      }

      if (apiResponse?.code !== 200) return { rows: [], columns: [] }

      let backendCols = apiResponse.data.columns || []
      if (
        type === 'Finding Steam' &&
        (!backendCols || backendCols.length === 0)
      ) {
        backendCols = [
          { field: 'id', title: 'ID', type: 'string', hidden: true },
          {
            field: 'materialdescription',
            title: 'Material Description',
            type: 'string',
          },
          {
            field: 'modeofOperation',
            title: 'Mode of Operation',
            type: 'string',
          },
          { field: 'totalQuantity', title: 'Total Quantity', type: 'number' },
        ]
      }

      const enrichedCols = enrichColumns(backendCols)

      const dateFields = enrichedCols
        .filter((c) => c.type === 'date')
        .map((c) => c.field)
      const numberFields = enrichedCols
        .filter((c) => c.type === 'number')
        .map((c) => c.field)

      let apiDataArray = []
      if (Array.isArray(apiResponse.data)) apiDataArray = apiResponse.data
      else if (Array.isArray(apiResponse.data.data))
        apiDataArray = apiResponse.data.data

      const rowsWithId = (apiDataArray || []).map((item, index) => {
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
        return { ...parsedItem, id: item.id || index, isEditable: false }
      })

      return { rows: rowsWithId, columns: enrichedCols }
    },
    [keycloak, enrichColumns, periodFrom, periodTo],
  )

  const loadGrids = useCallback(async () => {
    setLoading(true)
    try {
      const responseForModes = await OptimizerDataApiService.fetchModes(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        '1',
      )

      // Dynamic MODE_GRADES (no hard coding)
      const MODE_GRADES =
        responseForModes?.data?.map((mode) => mode?.name).filter(Boolean) || []

      const findingSteamResults = await Promise.all(
        MODE_GRADES?.map(async (mode) => {
          const { rows, columns } = await fetchModeGrid('Finding Steam', mode)
          return { name: `Finding Steam (${mode})`, rows, columns }
        }),
      )

      const rawSteamResults = await Promise.all(
        MODE_GRADES?.map(async (mode) => {
          const { rows, columns } = await fetchModeGrid('Raw Steam', mode)
          return { name: `Raw Steam (${mode})`, rows, columns }
        }),
      )

      if (!isMountedRef.current) return
      const newDataMap = {}
      const names = []
      findingSteamResults
        .concat(rawSteamResults)
        .forEach(({ name, rows, columns }) => {
          newDataMap[name] = { rows, columns }
          names.push(name)
        })
      setDataMap(newDataMap)
      setGridNames(names)
    } catch (err) {
      console.error('[loadGrids] failed', err)
    } finally {
      if (isMountedRef.current) setLoading(false)
    }
  }, [fetchModeGrid])

  useEffect(() => {
    if (periodFrom && periodTo) loadGrids()
  }, [loadGrids, PLANT_ID, oldYear, yearChanged, periodFrom, periodTo])

  // Export helpers (only for steam grids)
  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Steam Norms Data_Set.xlsx`

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
          if (!opts?.sheets?.[0]) return null
          const sheet = { ...opts.sheets[0] }
          sheet.title = name
          return sheet
        } catch {
          return null
        }
      })
      .filter(Boolean)

    if (!sheets.length) return
    baseOptions.sheets = sheets
    baseRef.save(baseOptions)
  }, [gridNames])

  const exportSingleGrid = useCallback((name) => {
    const ref = exportRefs.current[name]
    if (!ref) return
    try {
      const opts = ref.workbookOptions?.()
      ref.save(opts)
    } catch (err) {
      console.error('[exportSingleGrid] failed', err)
    }
  }, [])

  const handleExportClick = useCallback(() => {
    if (exportTarget === 'ALL') exportAllGrids()
    else exportSingleGrid(exportTarget)
  }, [exportAllGrids, exportSingleGrid, exportTarget])

  const renderTitle = (t) => t

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden ExcelExport for steam grids only */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const data = dataMap[name] || { rows: [], columns: [] }
          return (
            <ExcelExport
              key={name}
              ref={(ref) => {
                if (ref) exportRefs.current[name] = ref
              }}
              fileName={fileName}
              data={data.rows}
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

      {/* Only Export button aligned to the right */}
      <Box display='flex' justifyContent='flex-end' mb={2}>
        <Button
          variant='contained'
          onClick={handleExportClick}
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
          const visibleColumns = (d.columns || []).filter((col) => !col.hidden)
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
                      columns={visibleColumns}
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

export default RawDataSet
