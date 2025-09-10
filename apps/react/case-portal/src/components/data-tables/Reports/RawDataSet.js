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
const steamModes = ['4F', '5F', '4F+D']
const GRID_CONFIGS = [
  {
    name: 'Raw Data',
    fetcher: CrackerReportsApiDataService.getRawDataSetvalues,
  },
  {
    name: 'Utility Monthly',
    fetcher: CrackerReportsApiDataService.getRawutilitymonthly,
  },
  {
    name: 'Catcam',
    fetcher: CrackerReportsApiDataService.getRawCatcame,
  },
  {
    name: 'ATCAM Monthly',
    fetcher: CrackerReportsApiDataService.getRawatcammonthly,
  },
  {
    name: 'Finding Steam',
    fetcher: (keycloak, from, to) =>
      CrackerReportsApiDataService.getRawasfindingteam(keycloak, defaultSteamMode),
  },
  {
    name: 'Raw Steam',
    fetcher: (keycloak, from, to) =>
      CrackerReportsApiDataService.getRawasteam(keycloak, from, to, defaultSteamMode),
  },
]

const defaultPeriodFrom = '2020-09-01'
const defaultPeriodTo = '2025-08-31'
const defaultSteamMode = '4F'

const RawDataSet = () => {
  const keycloak = useSession()
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore
  const isMountedRef = useRef(true)
  const exportRefs = useRef(null);
  const exportGrid = useCallback((name) => {
    const ref = exportRefs.current[name]
    if (ref) {
      ref.save()
    }
  }, [])

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

  const fetchDataForGrid = useCallback(
  async (gridConfig) => {
    try {
      let apiResponse;
      if (gridConfig.name === 'Finding Steam') {
        apiResponse = await gridConfig.fetcher(keycloak);
      } else {
        apiResponse = await gridConfig.fetcher(
          keycloak,
          defaultPeriodFrom,
          defaultPeriodTo
        );
      }

      if (apiResponse?.code !== 200) {
        console.warn(`[fetchDataForGrid] non-200 response for ${gridConfig.name}`, apiResponse)
        return { rows: [], columns: [] }
      }

      let backendCols = apiResponse.data.columns || []

      // Manually set columns for Finding Steam if not present
      if (
        gridConfig.name === 'Finding Steam' &&
        (!backendCols || backendCols.length === 0)
      ) {
        backendCols = [
          { field: 'id', title: 'ID', type: 'string', hidden: true },
          { field: 'materialdescription', title: 'Material Description', type: 'string' },
          { field: 'modeofOperation', title: 'Mode of Operation', type: 'string' },
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

      let apiDataArray = [];
if (Array.isArray(apiResponse.data)) {
  apiDataArray = apiResponse.data;
} else if (Array.isArray(apiResponse.data.data)) {
  apiDataArray = apiResponse.data.data;
}

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
    } catch (err) {
      console.error(`[fetchDataForGrid] error for ${gridConfig.name}`, err)
      return { rows: [], columns: [] }
    }
  },
  [keycloak, enrichColumns]
)
  // Fetch all grids in parallel
  const loadGrids = useCallback(async () => {
    setLoading(true)
    try {
      const results = await Promise.all(
        GRID_CONFIGS.map(async (cfg) => {
          const { rows, columns } = await fetchDataForGrid(cfg)
          return { name: cfg.name, rows, columns }
        })
      )
      if (!isMountedRef.current) return
      const newDataMap = {}
      const names = []
      results.forEach(({ name, rows, columns }) => {
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
  }, [fetchDataForGrid])

  useEffect(() => {
    loadGrids()
    // re-load when plant/year changes
  }, [loadGrids, plantID, oldYear, yearChanged])

//   const exportAllGrids = useCallback(() => {
//     const keys = Object.keys(exportRefs.current || {})
//     if (!keys.length) return

//     const firstKey = keys.find((k) => exportRefs.current[k])
//     if (!firstKey) return
//     const baseRef = exportRefs.current[firstKey]
//     const baseOptions = baseRef?.workbookOptions?.()
//     if (!baseOptions) return

//     const sheets = gridNames
//       .map((name) => {
//         const ref = exportRefs.current[name]
//         try {
//           const opts = ref?.workbookOptions?.()
//           return opts?.sheets?.[0] ? { ...opts.sheets[0] } : null
//         } catch {
//           return null
//         }
//       })
//       .filter(Boolean)

//     if (!sheets.length) return

//     sheets.forEach((s, idx) => {
//       s.title = gridNames[idx] || s.title || `Sheet${idx + 1}`
//     })

//     baseOptions.sheets = sheets
//     baseRef.save(baseOptions)
//   }, [gridNames])

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `RawDataSet ${currentDateTime}.xlsx`

  const renderTitle = (t) => t
  const allRows = gridNames.flatMap(name =>
  (dataMap[name]?.rows || []).map(row => ({
    ...row,
    __GridName: name
  }))
);

const allColumns = [
  { field: "__GridName", title: "Grid Name" },
  ...Array.from(
    new Map(
      gridNames.flatMap(name =>
        (dataMap[name]?.columns || []).map(col => [col.field, col])
      )
    ).values()
  )
];
const exportAllGrids = useCallback(() => {
  if (exportRefs.current) {
    exportRefs.current.save();
  }
}, []);

  return (
  <div>
    <Backdrop
      sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
      open={!!loading}
    >
      <CircularProgress color="inherit" />
    </Backdrop>

    {/* Hidden ExcelExport instance for each grid */}
    <div style={{ display: 'none' }}>
      {gridNames.map((name) => {
        const data = dataMap[name] || { rows: [], columns: [] }
        const setRef = (ref) => {
          if (ref) exportRefs.current[name] = ref
        }
        return (
          <ExcelExport
    ref={exportRefs}
    fileName={fileName}
    data={allRows}
  >
    {allColumns.map(col => (
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

    {/* ✅ One global Export button */}
    <Box display="flex" justifyContent="flex-end" mb={2}>
      <Button variant="contained" onClick={exportAllGrids} className="btn-save">
        Export All
      </Button>
    </Box>

    <Box display="flex" flexDirection="column" gap={2}>
      {gridNames.length === 0 && !loading && (
        <Typography>No grids available.</Typography>
      )}

      {gridNames.map((name) => {
        const d = dataMap[name] || { rows: [], columns: [] }
        const visibleColumns = (d.columns || []).filter(col => !col.hidden)
        return (
          <div key={name}>
            <CustomAccordion defaultExpanded disableGutters>
              <CustomAccordionSummary
                aria-controls={`${name}-content`}
                id={`${name}-header`}
              >
                <Typography component="span" className="grid-title">
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