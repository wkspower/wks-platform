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
import { styled } from '@mui/material/styles'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import getKendoNormsHistorianBasisPe from '../CommonHeader/KendoNormsHistorianBasisPe'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import moment from 'moment'

const REPORT_TYPES = {
  RAW_MCU: 'RAW MCU',
  MCU_WITHIN_RANGE: 'MCU WITHIN RANGE',
  MCU_RANGE: 'MCU RANGE',
  PRODUCTION_VOLUME_BASIS: 'PRODUCTION VOLUME BASIS',
}

const parseDDMMYYYY = (dateStr) => {
  if (!dateStr) return null
  const [day, month, year] = dateStr.split('-')
  return new Date(`${year}-${month}-${day}`)
}

const formatCurrentDateTime = () => {
  return new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
}

const useDataState = () => {
  const [state, setState] = useState({
    rowsRawMcu: [],
    rowsMcuWithInRange: [],
    rowsMcuRange: [],
    rowsProductionVolDataBasis: [],
    loading: false,
  })

  const updateState = useCallback((updates) => {
    setState((prev) => ({ ...prev, ...updates }))
  }, [])

  return [state, updateState]
}

const DataAccordion = React.memo(
  ({ title, rows, columns, defaultExpanded = true }) => (
    <CustomAccordion defaultExpanded={defaultExpanded} disableGutters>
      <CustomAccordionSummary
        expandIcon={<ExpandMoreIcon />}
        aria-controls={`${title.toLowerCase().replace(/\s+/g, '-')}-content`}
        id={`${title.toLowerCase().replace(/\s+/g, '-')}-header`}
      >
        <Typography component='span' className='grid-title'>
          {title}
        </Typography>
      </CustomAccordionSummary>
      <CustomAccordionDetails>
        <Box sx={{ width: '100%', margin: 0 }}>
          <KendoDataGrid
            rows={rows}
            columns={columns}
            permissions={{ allAction: false }}
          />
        </Box>
      </CustomAccordionDetails>
    </CustomAccordion>
  ),
)

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

const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()
  const [data, updateData] = useDataState()

  const exportRefs = {
    rawMcu: useRef(null),
    mcuWithinRange: useRef(null),
    mcuRange: useRef(null),
    productionVolume: useRef(null),
  }

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

  // const columns = useMemo(
  //   () => ({

  //     rawMcu :
  //     mcuWithinRange :
  //     mcuRange  :
  //     productionVolume :
  //   }),
  //   [headerMap],
  // )

  const [columnsMap, setColumnsMap] = useState({
    [REPORT_TYPES.RAW_MCU]: [],
    [REPORT_TYPES.MCU_WITHIN_RANGE]: [],
    [REPORT_TYPES.MCU_RANGE]: [],
    [REPORT_TYPES.PRODUCTION_VOLUME_BASIS]: [],
  })

  const fetchData = useCallback(
    async (reportType, StartDate, EndDate) => {
      try {
        const result = await DataService.getProductionVolDataBasisPe(
          keycloak,
          reportType,
          StartDate,
          EndDate,
        )

        if (result?.code === 200) {
          const backendCols = result.data.columns || []

          // Enrich backend columns with UI props
          const enrichedCols = backendCols.map((col) => {
            const isTextCol = col.type === 'string'
            const isNumberCol = col.type === 'number'
            const isDateCol = col.type === 'date'

            return {
              ...col,
              filterable: true,
              filter: isTextCol ? 'text' : isNumberCol ? 'numeric' : undefined,
              align: isTextCol ? 'left' : isNumberCol ? 'right' : undefined,
              ...(isNumberCol ? { format: '{0:#.###}' } : {}),
              editable: false,
              isRightAlligned: isNumberCol ? 'numeric' : undefined,
            }
          })

          setColumnsMap((prev) => ({
            ...prev,
            [reportType]: enrichedCols,
          }))

          // Detect fields by type
          const dateFields = enrichedCols
            .filter((col) => col.type === 'date')
            .map((col) => col.field)

          const numberFields = enrichedCols
            .filter((col) => col.type === 'number')
            .map((col) => col.field)

          // Parse rows
          const rowsWithId = (result.data.data || []).map((item, index) => {
            let parsedItem = { ...item }

            // Date parsing
            dateFields.forEach((field) => {
              parsedItem[field] = item?.[field]
                ? parseDDMMYYYY(item[field])
                : null
            })

            // Number parsing
            numberFields.forEach((field) => {
              parsedItem[field] =
                item?.[field] !== undefined && item?.[field] !== null
                  ? Number(item[field])
                  : null
            })

            return {
              ...parsedItem,
              id: index,
              isEditable: false,
            }
          })

          return rowsWithId
        } else {
          console.error(`Error fetching ${reportType} data`)
          return []
        }
      } catch (error) {
        console.error(`Error fetching ${reportType} data:`, error)
        return []
      }
    },
    [keycloak],
  )

  // Fetch configuration and all data
  const fetchAllData = useCallback(async () => {
    try {
      updateData({ loading: true })

      const configData =
        await DataService.getConfigurationExecutionDetails(keycloak)

      if (configData.code === 200) {
        const StartDate = configData.data.find(
          (d) => d.Name === 'StartDate',
        )?.AttributeValue
        const EndDate = configData.data.find(
          (d) => d.Name === 'EndDate',
        )?.AttributeValue

        if (!StartDate || !EndDate) {
          updateData({
            loading: false,
            rowsRawMcu: [],
            rowsMcuWithInRange: [],
            rowsMcuRange: [],
            rowsProductionVolDataBasis: [],
          })
          return
        }

        // Fetch all datasets in parallel
        const [
          rawMcuData,
          mcuWithinRangeData,
          mcuRangeData,
          productionVolumeData,
        ] = await Promise.all([
          fetchData(REPORT_TYPES.RAW_MCU, StartDate, EndDate),
          fetchData(REPORT_TYPES.MCU_WITHIN_RANGE, StartDate, EndDate),
          fetchData(REPORT_TYPES.MCU_RANGE, StartDate, EndDate),
          fetchData(REPORT_TYPES.PRODUCTION_VOLUME_BASIS, StartDate, EndDate),
        ])

        updateData({
          rowsRawMcu: rawMcuData,
          rowsMcuWithInRange: mcuWithinRangeData,
          rowsMcuRange: mcuRangeData,
          rowsProductionVolDataBasis: productionVolumeData,
          loading: false,
        })
      } else {
        console.error('Error fetching configuration data')
        updateData({ loading: false })
      }
    } catch (error) {
      console.error('Error fetching configuration data:', error)
      updateData({ loading: false })
    }
  }, [keycloak, fetchData, updateData])

  // Export functionality
  const exportAllGrids = useCallback(() => {
    const options1 = exportRefs.rawMcu.current.workbookOptions()
    const options2 = exportRefs.mcuWithinRange.current.workbookOptions()
    const options3 = exportRefs.mcuRange.current.workbookOptions()

    // Add additional sheets
    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]

    // Rename sheets
    options1.sheets[0].title = 'RAW MCU'
    options1.sheets[1].title = 'MCU WITHIN RANGE'
    options1.sheets[2].title = 'MCU RANGE'

    exportRefs.rawMcu.current.save(options1)
  }, [])

  // Effects
  useEffect(() => {
    fetchAllData()
  }, [fetchAllData, plantID, oldYear, yearChanged, lowerVertName])

  // Memoized filename
  const fileName = useMemo(
    () => `Norms Historian Data Basis ${formatCurrentDateTime()}.xlsx`,
    [],
  )

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={data.loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Hidden Excel Export Components */}
      <div style={{ display: 'none' }}>
        <ExcelExportComponent
          data={data.rowsRawMcu}
          columns={columnsMap[REPORT_TYPES.RAW_MCU]}
          exportRef={exportRefs.rawMcu}
          fileName={fileName}
        />
        <ExcelExportComponent
          data={data.rowsMcuWithInRange}
          columns={columnsMap[REPORT_TYPES.MCU_WITHIN_RANGE]}
          exportRef={exportRefs.mcuWithinRange}
        />
        <ExcelExportComponent
          data={data.rowsMcuRange}
          columns={columnsMap[REPORT_TYPES.MCU_RANGE]}
          exportRef={exportRefs.mcuRange}
        />
        <ExcelExportComponent
          data={data.rowsProductionVolDataBasis}
          columns={columnsMap[REPORT_TYPES.PRODUCTION_VOLUME_BASIS]}
          exportRef={exportRefs.productionVolume}
        />
      </div>

      {/* Export Button */}
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

      {/* Data Grids */}
      <Box display='flex' flexDirection='column' gap={2}>
        <DataAccordion
          title='Raw MCU'
          rows={data.rowsRawMcu}
          columns={columnsMap[REPORT_TYPES.RAW_MCU]}
        />

        <DataAccordion
          title='MCU within Range'
          rows={data.rowsMcuWithInRange}
          columns={columnsMap[REPORT_TYPES.MCU_WITHIN_RANGE]}
        />

        <DataAccordion
          title='MCU Range'
          rows={data.rowsMcuRange}
          columns={columnsMap[REPORT_TYPES.MCU_RANGE]}
        />

        <DataAccordion
          title='Production Volume Basis'
          rows={data.rowsProductionVolDataBasis}
          columns={columnsMap[REPORT_TYPES.PRODUCTION_VOLUME_BASIS]}
        />
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasisPe
