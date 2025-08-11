import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { Button } from '../../../../node_modules/@mui/material/index'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../../node_modules/@progress/kendo-react-excel-export/index'

const CALL_DELAY_MS = 200

const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()

  const [rawMcuData, setRawMcuData] = useState({ rows: [], columns: [] })
  const [mcuWithInRangeData, setMcuWithInRangeData] = useState({
    rows: [],
    columns: [],
  })
  const [mcuRangeData, setMcuRangeData] = useState({ rows: [], columns: [] })

  const [avgAnnualNormsData, setAvgAnnualNormsData] = useState({
    rows: [],
    columns: [],
  })
  const [consecutiveDaysData, setConsecutiveDaysData] = useState({
    rows: [],
    columns: [],
  })
  const [miisNormsRawData, setMiisNormsRawData] = useState({
    rows: [],
    columns: [],
  })
  const [bestAchievedNormsData, setBestAchievedNormsData] = useState({
    rows: [],
    columns: [],
  })

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantID, yearChanged, oldYear } = dataGridStore

  const [loading, setLoading] = useState(false)

  const timeoutIdsRef = useRef([])
  const activeRequestsRef = useRef(0)
  const isMountedRef = useRef(true)

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

  const fetchDataNoLoader = async (reportType, setState) => {
    try {
      const configData =
        await DataService.getConfigurationExecutionDetails(keycloak)
      if (configData.code !== 200) return

      const StartDate = configData.data.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData.data.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue
      if (!StartDate || !EndDate) {
        setState([])
        return
      }

      const apiResponse = await DataService.getProductionVolDataBasisPe(
        keycloak,
        reportType,
        StartDate,
        EndDate,
      )

      if (apiResponse?.code === 200) {
        const backendCols = apiResponse.data.columns || []

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

        const dateFields = enrichedCols
          .filter((col) => col.type === 'date')
          .map((col) => col.field)

        const numberFields = enrichedCols
          .filter((col) => col.type === 'number')
          .map((col) => col.field)

        const rowsWithId = (apiResponse.data.data || []).map((item, index) => {
          let parsedItem = { ...item }

          dateFields.forEach((field) => {
            parsedItem[field] = item?.[field]
              ? parseDDMMYYYY(item[field])
              : null
          })

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

        if (isMountedRef.current) {
          setState({ rows: rowsWithId, columns: enrichedCols })
        }
      }
    } catch (err) {
      console.error(`Error fetching ${reportType}:`, err)
    }
  }

  const scheduleAndRunFetch = (reportType, setState, delayMs) => {
    const id = setTimeout(async () => {
      activeRequestsRef.current += 1
      if (isMountedRef.current) setLoading(true)

      try {
        await fetchDataNoLoader(reportType, setState)
      } finally {
        activeRequestsRef.current -= 1
        if (activeRequestsRef.current <= 0 && isMountedRef.current) {
          activeRequestsRef.current = 0
          setLoading(false)
        }
      }
    }, delayMs)

    timeoutIdsRef.current.push(id)
  }

  useEffect(() => {
    timeoutIdsRef.current.forEach((t) => clearTimeout(t))
    timeoutIdsRef.current = []

    const yearNow = localStorage.getItem('year')
    const headerMap = generateHeaderNames(yearNow)

    const reports = [
      { name: 'RAW MCU', setter: setRawMcuData },
      { name: 'MCU WITHIN RANGE', setter: setMcuWithInRangeData },
      { name: 'MCU RANGE', setter: setMcuRangeData },
      { name: 'AVG ANNUAL NORMS', setter: setAvgAnnualNormsData },
      { name: 'CONSECUTIVE DAYS', setter: setConsecutiveDaysData },
      { name: 'MIIS NORMS RAW DATA', setter: setMiisNormsRawData },
      { name: 'BEST ACHIEVED NORMS', setter: setBestAchievedNormsData },
    ]

    reports.forEach((r, idx) => {
      const delay = idx * CALL_DELAY_MS
      scheduleAndRunFetch(r.name, r.setter, delay)
    })

    return () => {
      timeoutIdsRef.current.forEach((t) => clearTimeout(t))
      timeoutIdsRef.current = []
    }
  }, [plantID, oldYear, yearChanged, keycloak])

  const exportRef1 = useRef(null)
  const exportRef2 = useRef(null)
  const exportRef3 = useRef(null)
  const exportRef4 = useRef(null)
  const exportRef5 = useRef(null)
  const exportRef6 = useRef(null)
  const exportRef7 = useRef(null)

  const exportAllGrids = () => {
    const options1 = exportRef1.current.workbookOptions()
    const options2 = exportRef2.current.workbookOptions()
    const options3 = exportRef3.current.workbookOptions()
    const options4 = exportRef4.current.workbookOptions()
    const options5 = exportRef5.current.workbookOptions()
    const options6 = exportRef6.current.workbookOptions()
    const options7 = exportRef7.current.workbookOptions()

    // Add additional sheets to first export
    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]
    options1.sheets[3] = options4.sheets[0]
    options1.sheets[4] = options5.sheets[0]
    options1.sheets[5] = options6.sheets[0]
    options1.sheets[6] = options7.sheets[0]

    // Rename sheets
    options1.sheets[0].title = 'RAW MCU'
    options1.sheets[1].title = 'MCU WITHIN RANGE'
    options1.sheets[2].title = 'MCU RANGE'
    options1.sheets[3].title = 'AVG ANNUAL NORMS'
    options1.sheets[4].title = 'CONSECUTIVE DAYS'
    options1.sheets[5].title = 'MIIS NORMS RAW DATA'
    options1.sheets[6].title = 'BEST ACHIEVED NORMS'

    exportRef1.current.save(options1)
  }

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Norms Historian Data Basis ${currentDateTime}.xlsx`

  // define this just above the return() in your component
  const reports = [
    { key: 'RAW_MCU', title: 'Raw MCU', data: rawMcuData, ref: exportRef1 },
    {
      key: 'MCU_WITHIN_RANGE',
      title: 'MCU within Range',
      data: mcuWithInRangeData,
      ref: exportRef2,
    },
    {
      key: 'MCU_RANGE',
      title: 'MCU Range',
      data: mcuRangeData,
      ref: exportRef3,
    },
    {
      key: 'CONSECUTIVE_DAYS',
      title: 'CONSECUTIVE DAYS',
      data: consecutiveDaysData,
      ref: exportRef5,
    },
    {
      key: 'MIIS_NORMS_RAW',
      title: 'MIIS NORMS RAW DATA',
      data: miisNormsRawData,
      ref: exportRef6,
    },
    {
      key: 'AVG_ANNUAL',
      title: 'AVG NORMS',
      data: avgAnnualNormsData,
      ref: exportRef4,
    },
    {
      key: 'BEST_ACHIEVED',
      title: 'BEST ACHIEVED NORMS',
      data: bestAchievedNormsData,
      ref: exportRef7,
    },
  ]

  /* ---------- Then in JSX replace the repeated parts with: ---------- */

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Export hidden ExcelExport instances (generated) */}
      <div style={{ display: 'none' }}>
        {reports.map((r) => (
          <ExcelExport
            key={r.key}
            data={r.data.rows}
            ref={r.ref}
            fileName={fileName}
          >
            {(r.data.columns || []).map((col) => (
              <ExcelExportColumn
                key={col.field}
                field={col.field}
                title={col.title}
              />
            ))}
          </ExcelExport>
        ))}
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
        {reports.map((r) => (
          <div key={r.key}>
            <CustomAccordion defaultExpanded disableGutters>
              <CustomAccordionSummary
                aria-controls='meg-grid-content'
                id='meg-grid-header'
              >
                <Typography component='span' className='grid-title'>
                  {r.title}
                </Typography>
              </CustomAccordionSummary>
              <CustomAccordionDetails>
                <Box sx={{ width: '100%', margin: 0 }}>
                  <KendoDataGrid
                    rows={r.data.rows}
                    columns={r.data.columns}
                    permissions={{ allAction: false }}
                  />
                </Box>
              </CustomAccordionDetails>
            </CustomAccordion>
          </div>
        ))}
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasisPe
