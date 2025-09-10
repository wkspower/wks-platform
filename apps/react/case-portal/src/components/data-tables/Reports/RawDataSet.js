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
const steamModes = ['5F', '4F', '4F+D']
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
    name: 'Catcam Monthly',
    fetcher: CrackerReportsApiDataService.getRawatcammonthly,
  },
]

const RawDataSet = () => {
  const keycloak = useSession()
  const [periodFrom, setPeriodFrom] = useState(null)
  const [periodTo, setPeriodTo] = useState(null)
  const [dataMap, setDataMap] = useState({})
  const [gridNames, setGridNames] = useState([])
  const [loading, setLoading] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore
  const isMountedRef = useRef(true)
  const exportRefs = useRef({});
  useEffect(() => {
    return () => {
      isMountedRef.current = false
    }
  }, [])
  useEffect(() => {
    async function fetchPeriod() {
      try {
        const resp = await CrackerReportsApiDataService.getConfigurationExecutionDetails(keycloak)
        if (Array.isArray(resp?.data)) {
          const start = resp.data.find(d => d.Name === 'StartDate')
          const end = resp.data.find(d => d.Name === 'EndDate')
          if (start?.AttributeValue) setPeriodFrom(start.AttributeValue)
          if (end?.AttributeValue) setPeriodTo(end.AttributeValue)
        }
      } catch (e) {
        // fallback: set default values if needed
        // setPeriodFrom('2020-09-01')
        // setPeriodTo('2025-08-31')
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
        ...(isNumberCol ? { format: '{0:#.###}' } : {}),
        editable: false,
        isRightAlligned: isNumberCol ? 'numeric' : undefined,
      }
    })
  }, [])

  // Helper to fetch and format a single mode for a grid type
  const fetchModeGrid = useCallback(async (type, mode) => {
    let apiResponse;
    if (type === 'Finding Steam') {
      apiResponse = await CrackerReportsApiDataService.getRawasfindingteam(
        keycloak,
        mode
      );
    } else if (type === 'Raw Steam') {
      apiResponse = await CrackerReportsApiDataService.getRawasteam(
        keycloak,
        periodFrom,
        periodTo,
        mode
      );
    } else {
      return { rows: [], columns: [] };
    }

    if (apiResponse?.code !== 200) return { rows: [], columns: [] };

    let backendCols = apiResponse.data.columns || [];
    if (
      type === 'Finding Steam' &&
      (!backendCols || backendCols.length === 0)
    ) {
      backendCols = [
        { field: 'id', title: 'ID', type: 'string', hidden: true },
        { field: 'materialdescription', title: 'Material Description', type: 'string' },
        { field: 'modeofOperation', title: 'Mode of Operation', type: 'string' },
        { field: 'totalQuantity', title: 'Total Quantity', type: 'number' },
      ];
    }

    const enrichedCols = enrichColumns(backendCols);

    const dateFields = enrichedCols.filter((c) => c.type === 'date').map((c) => c.field);
    const numberFields = enrichedCols.filter((c) => c.type === 'number').map((c) => c.field);

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
  }, [keycloak, enrichColumns, periodFrom, periodTo]);
    const fetchDataForGrid = useCallback(async (gridConfig) => {
  let apiResponse;
  if (gridConfig.name === 'Raw Data') {
    apiResponse = await gridConfig.fetcher(keycloak, periodFrom, periodTo);
  } else if (gridConfig.name === 'Utility Monthly') {
    apiResponse = await gridConfig.fetcher(keycloak, periodFrom, periodTo);
  } else if (gridConfig.name === 'Catcam') {
    apiResponse = await gridConfig.fetcher(keycloak, periodFrom, periodTo);
  } else if (gridConfig.name === 'Catcam Monthly') {
    apiResponse = await gridConfig.fetcher(keycloak, periodFrom, periodTo);
  } else {
    return { rows: [], columns: [] };
  }

  if (apiResponse?.code !== 200) return { rows: [], columns: [] };

  let backendCols = apiResponse.data.columns || [];
  const enrichedCols = enrichColumns(backendCols);

  const dateFields = enrichedCols.filter((c) => c.type === 'date').map((c) => c.field);
  const numberFields = enrichedCols.filter((c) => c.type === 'number').map((c) => c.field);

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
}, [keycloak, enrichColumns, periodFrom, periodTo]);
  // Fetch all grids in parallel, including per-mode grids for Finding/Raw Steam
  const loadGrids = useCallback(async () => {
    setLoading(true)
    try {
      // Fetch standard grids
      const results = await Promise.all(
        GRID_CONFIGS.map(async (cfg) => {
          const { rows, columns } = await fetchDataForGrid(cfg)
          return { name: cfg.name, rows, columns }
        })
      )

      // Fetch per-mode grids for Finding Steam and Raw Steam
      const findingSteamResults = await Promise.all(
        steamModes.map(async (mode) => {
          const { rows, columns } = await fetchModeGrid('Finding Steam', mode)
          return { name: `Finding Steam (${mode})`, rows, columns }
        })
      )
      const rawSteamResults = await Promise.all(
        steamModes.map(async (mode) => {
          const { rows, columns } = await fetchModeGrid('Raw Steam', mode)
          return { name: `Raw Steam (${mode})`, rows, columns }
        })
      )

      if (!isMountedRef.current) return
      const newDataMap = {}
      const names = []
      results.concat(findingSteamResults).concat(rawSteamResults).forEach(({ name, rows, columns }) => {
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
  }, [fetchDataForGrid, fetchModeGrid])

 
  useEffect(() => {
  if (periodFrom && periodTo) {
    loadGrids()
  }
  // re-load when plant/year changes
}, [loadGrids, plantID, oldYear, yearChanged, periodFrom, periodTo])

const exportAllGrids = useCallback(() => {
  const keys = Object.keys(exportRefs.current || {});
  if (!keys.length) return;

  const firstKey = keys.find((k) => exportRefs.current[k]);
  if (!firstKey) return;

  const baseRef = exportRefs.current[firstKey];
  const baseOptions = baseRef?.workbookOptions?.();
  if (!baseOptions) return;

  // Collect sheets from all grids
  const sheets = gridNames
    .map((name) => {
      const ref = exportRefs.current[name];
      try {
        const opts = ref?.workbookOptions?.();
        if (!opts?.sheets?.[0]) return null;
        const sheet = { ...opts.sheets[0] };
        sheet.title = name; // 👈 set sheet name to grid name
        return sheet;
      } catch {
        return null;
      }
    })
    .filter(Boolean);

  if (!sheets.length) return;

  baseOptions.sheets = sheets;
  baseRef.save(baseOptions);
}, [gridNames]);

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `RawDataSet ${currentDateTime}.xlsx`

  const renderTitle = (t) => t

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
    const data = dataMap[name] || { rows: [], columns: [] };
    return (
      <ExcelExport
        key={name}
        ref={(ref) => {
          if (ref) {
            exportRefs.current[name] = ref; // ✅ store ref by grid name
          }
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
    );
  })}
</div>


    {/* ✅ One global Export button */}
    <Box display="flex" justifyContent="flex-end" mb={2}>
      <Button variant="contained" onClick={exportAllGrids} className="btn-save">
        Export
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