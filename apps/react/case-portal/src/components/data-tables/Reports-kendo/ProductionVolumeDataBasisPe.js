import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Typography,
} from '@mui/material'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'

/* ------------------------
   small helpers (kept your style)
   ------------------------ */
const parseDDMMYYYY = (dateStr) => {
  if (!dateStr) return null
  const [day, month, year] = dateStr.split('-')
  return new Date(`${year}-${month}-${day}`)
}

const formatCurrentDateTime = () =>
  new Date().toISOString().replace(/T/, ' ').replace(/:/g, '-').split('.')[0]

const toTitleCase = (s = '') =>
  s
    .toLowerCase()
    .split(' ')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')

/* ------------------------
   small presentational components
   ------------------------ */
const DataAccordion = React.memo(({ title, rows, columns }) => (
  <CustomAccordion defaultExpanded disableGutters>
    <CustomAccordionSummary expandIcon={<ExpandMoreIcon />}>
      <Typography component='span' className='grid-title'>
        {title}
      </Typography>
    </CustomAccordionSummary>
    <CustomAccordionDetails>
      <Box sx={{ width: '100%', margin: 0 }}>
        <KendoDataGrid
          rows={rows}
          columns={columns}
          permissions={{
            allAction: false,
            isHeight: rows?.length > 15,
          }}
        />
      </Box>
    </CustomAccordionDetails>
  </CustomAccordion>
))
DataAccordion.displayName = 'DataAccordion'

const ExcelExportComponent = React.memo(
  ({ data, columns, exportRef, fileName }) => (
    <ExcelExport data={data} ref={exportRef} fileName={fileName}>
      {columns.map((col) => (
        <ExcelExportColumn
          key={col.field}
          field={col.field}
          title={col.title}
        />
      ))}
    </ExcelExport>
  ),
)
ExcelExportComponent.displayName = 'ExcelExportComponent'

/* ------------------------
   Main component (dynamic)
   ------------------------ */
const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = useMemo(
    () => vertName?.toLowerCase() || 'meg',
    [vertName],
  )
  const isOldYear = useMemo(() => oldYear?.oldYear === 1, [oldYear])
  const year = useMemo(
    () => localStorage.getItem('year'),
    [plantID, yearChanged],
  )
  const headerMap = useMemo(() => generateHeaderNames(year), [year])

  // dynamic state:
  // gridNames: array of strings returned by TYPE_LIST call
  // columnsMap: { [gridName]: [cols...] }
  // rowsMap: { [gridName]: [rows...] }
  const [gridNames, setGridNames] = useState([])
  const [columnsMap, setColumnsMap] = useState({})
  const [rowsMap, setRowsMap] = useState({})
  const [loading, setLoading] = useState(false)

  // dynamic refs for exports
  // exportRefs.current = { [gridNameNormalized]: workbookRef }
  const exportRefs = useRef({})

  // fetch columns + rows for a single reportType (gridName)
  const fetchDataForGrid = useCallback(
    async (reportType, StartDate, EndDate) => {
      try {
        const result = await DataService.getProductionVolDataBasisPe(
          keycloak,
          reportType,
          StartDate,
          EndDate,
        )

        if (result?.code !== 200) {
          console.error(`Error fetching ${reportType} data`)
          return { rows: [], columns: [] }
        }

        const backendCols = result.data.columns || []

        // Enrich backend columns with UI props
        const enrichedCols = backendCols.map((col) => {
          const isTextCol = col.type === 'string'
          const isNumberCol = col.type === 'number'
          // const isDateCol = col.type === 'date'

          return {
            ...col,
            title: col.title || col.field,
            filterable: true,
            filter: isTextCol ? 'text' : isNumberCol ? 'numeric' : undefined,
            align: isTextCol ? 'left' : isNumberCol ? 'right' : undefined,
            ...(isNumberCol ? { format: '{0:#.##}' } : {}),
            editable: false,
            isRightAlligned: isNumberCol ? 'numeric' : undefined,
          }
        })

        // detect fields to parse
        const dateFields = enrichedCols
          .filter((c) => c.type === 'date')
          .map((c) => c.field)
        const numberFields = enrichedCols
          .filter((c) => c.type === 'number')
          .map((c) => c.field)

        // parse rows
        const rowsWithId = (result.data.data || []).map((item, index) => {
          let parsedItem = { ...item }
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
    [keycloak],
  )

  // fetch all: get TYPE_LIST then fetch each grid
  const fetchAllData = useCallback(async () => {
    setLoading(true)
    try {
      const configData =
        await DataService.getConfigurationExecutionDetails(keycloak)
      if (configData.code !== 200) {
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
        setColumnsMap({})
        setRowsMap({})
        setLoading(false)
        return
      }

      // first call TYPE_LIST to get the available grids
      const typeListResult = await DataService.getProductionVolDataBasisPe(
        keycloak,
        'TYPE LIST1',
        StartDate,
        EndDate,
      )

      // const types = Array.isArray(typeListResult?.data)
      //   ? typeListResult.data
      //   : [
      //       'RAW MCU',
      //       'MCU WITHIN RANGE',
      //       'MCU RANGE',
      //       'PRODUCTION VOLUME BASIS',
      //     ]
      // // normalize to unique names

      let types = []
      if (typeListResult?.code == 200) {
        types = (typeListResult?.data?.data ?? []).map((item) => item.TYPE)
      } else {
        return
      }

      const normalizedTypes = [...new Set(types)]

      setGridNames(normalizedTypes)

      // fetch all grid data in parallel
      const promises = normalizedTypes.map((type) =>
        fetchDataForGrid(type, StartDate, EndDate).then((res) => ({
          type,
          ...res,
        })),
      )

      const resolved = await Promise.all(promises)

      const nextColumns = {}
      const nextRows = {}
      resolved.forEach((r) => {
        nextColumns[r.type] = r.columns || []
        nextRows[r.type] = r.rows || []
      })

      setColumnsMap(nextColumns)
      setRowsMap(nextRows)
      setLoading(false)
    } catch (err) {
      console.error('Error fetching configuration or grids:', err)
      setLoading(false)
    }
  }, [fetchDataForGrid, keycloak])

  useEffect(() => {
    fetchAllData()
    // include plantID etc. so it refetches on relevant changes
  }, [fetchAllData, plantID, oldYear, yearChanged, lowerVertName])

  // Export: merge sheets from each individual exporter
  const exportAllGrids = useCallback(() => {
    const keys = Object.keys(exportRefs.current || {})
    if (!keys.length) return

    // pick first available ref as base
    const firstKey = keys.find((k) => exportRefs.current[k])
    if (!firstKey) return

    const baseRef = exportRefs.current[firstKey]
    const baseOptions = baseRef.workbookOptions
      ? baseRef.workbookOptions()
      : null
    if (!baseOptions) return

    // collect sheet objects (first sheet from each export)
    const sheets = keys
      .map((k) => {
        try {
          const ref = exportRefs.current[k]
          const opts = ref?.workbookOptions?.() // safe-call
          return opts?.sheets?.[0] ? { ...opts.sheets[0] } : null
        } catch {
          return null
        }
      })
      .filter(Boolean)

    if (!sheets.length) return

    // assign readable titles (use original grid name if available)
    sheets.forEach((s, idx) => {
      s.title = toTitleCase(keys[idx])
    })

    // replace base sheets with our combined sheets
    baseOptions.sheets = sheets

    // save using the first ref
    baseRef.save(baseOptions)
  }, [])

  const fileName = useMemo(
    () => `Production Volume Data Basis ${formatCurrentDateTime()}.xlsx`,
    [],
  )

  /* ------------------------
     Render
     ------------------------ */
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden Excel Export Components (one per grid) */}
      <div style={{ display: 'none' }}>
        {gridNames.map((name) => {
          const key = name
          const cols = columnsMap[key] || []
          const rows = rowsMap[key] || []
          // use function ref to store the export instance
          const setter = (ref) => {
            if (ref) exportRefs.current[key] = ref
          }
          return (
            <ExcelExportComponent
              key={`excel-${key}`}
              data={rows}
              columns={cols}
              exportRef={setter}
              fileName={fileName}
            />
          )
        })}
      </div>

      {/* Export button */}
      {!isOldYear && (
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

      {/* dynamic grids */}
      <Box display='flex' flexDirection='column' gap={2}>
        {gridNames.length === 0 && !loading && (
          <Typography>
            No data grids available for the selected period.
          </Typography>
        )}

        {gridNames.map((name) => {
          const visibleTitle = toTitleCase(name)
          const cols = columnsMap[name] || []
          const rows = rowsMap[name] || []
          return (
            <DataAccordion
              key={name}
              title={visibleTitle}
              rows={rows}
              columns={cols}
            />
          )
        })}
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasisPe
