import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
// import AopCostReportView from 'components/data-tables-views/ReportDataGrid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'

import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../../node_modules/@progress/kendo-react-excel-export/index'
import { Button } from '../../../../node_modules/@mui/material/index'
import getKendoNormsHistorianBasisPe from '../CommonHeader/KendoNormsHistorianBasisPe'

const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))

const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))

const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

const ProductionVolumeDataBasisPe = () => {
  const keycloak = useSession()

  const [rowsRawMcu, setRowsRawMcu] = useState([])
  const [rowsMcuWithInRange, setRowsMcuWithInRange] = useState([])
  const [rowsMcuRange, setRowsMcuRange] = useState([])
  const [rowsAvgAnnualNorms, setRowsAvgAnnualNorms] = useState([])
  const [rowsConsecutiveDays, setRowsConsecutiveDays] = useState([])
  const [rowsMiisNormsRawData, setRowsMiisNormsRawData] = useState([])
  const [rowsBestAchivedNorms, setRowsBestAchivedNorms] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)

  function parseDDMMYYYY(dateStr) {
    if (!dateStr) return null
    const [day, month, year] = dateStr.split('-')
    return new Date(`${year}-${month}-${day}`) // YYYY-MM-DD (ISO format)
  }

  const fetchData = async (reportType, setState) => {
    try {
      setLoading(true)
      var data = []
      data = await DataService.getProductionVolDataBasisPe(keycloak, reportType)

      if (data?.code === 200) {
        const rowsWithId = data?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
          startDate: item?.startDate ? parseDDMMYYYY(item.startDate) : null,
          endDate: item?.endDate ? parseDDMMYYYY(item.endDate) : null,
          dateTime: item?.dateTime ? parseDDMMYYYY(item.dateTime) : null,
        }))
        setLoading(false)
        setState(rowsWithId)
      } else {
        console.error(`Error fetching ${reportType} data`)
        setLoading(false)
      }
    } catch (error) {
      console.error(`Error fetching ${reportType} data:`, error)
      setLoading(false)
    }
  }

  const year = localStorage.getItem('year')
  const headerMap = generateHeaderNames(year)

  const colsRawMcu = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'RAW MCU',
  })

  const colsMcuWithInRange = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'MCU WITHIN RANGE',
  })

  const colsMcuRange = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'MCU RANGE',
  })

  const colsAvgAnnualNorms = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'AVG ANNUAL NORMS',
  })
  const colsConsecutiveDays = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'CONSECUTIVE DAYS',
  })
  const colsMiisNormsRawData = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'MIIS NORMS RAW DATA',
  })
  const colsBestAchivedNorms = getKendoNormsHistorianBasisPe({
    headerMap,
    type: 'BEST ACHIEVED NORMS',
  })

  useEffect(() => {
    fetchData('RAW MCU', setRowsRawMcu)
    fetchData('MCU WITHIN RANGE', setRowsMcuWithInRange)
    fetchData('MCU RANGE', setRowsMcuRange)
    fetchData('AVG ANNUAL NORMS', setRowsAvgAnnualNorms)
    fetchData('CONSECUTIVE DAYS', setRowsConsecutiveDays)
    fetchData('MIIS NORMS RAW DATA', setRowsMiisNormsRawData)
    fetchData('BEST ACHIEVED NORMS', setRowsBestAchivedNorms)
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

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

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Export hidden ExcelExport instances */}
      <div style={{ display: 'none' }}>
        <ExcelExport data={rowsRawMcu} ref={exportRef1} fileName={fileName}>
          {colsRawMcu.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsMcuWithInRange} ref={exportRef2}>
          {colsMcuWithInRange.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsMcuRange} ref={exportRef3}>
          {colsMcuRange.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsAvgAnnualNorms} ref={exportRef4}>
          {colsAvgAnnualNorms.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsConsecutiveDays} ref={exportRef5}>
          {colsConsecutiveDays.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsMiisNormsRawData} ref={exportRef6}>
          {colsMiisNormsRawData.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsBestAchivedNorms} ref={exportRef7}>
          {colsBestAchivedNorms.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
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
        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Raw MCU
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsRawMcu}
                  columns={colsRawMcu}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>

        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                MCU within Range
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsMcuWithInRange}
                  columns={colsMcuWithInRange}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>

        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                MCU Range
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsMcuRange}
                  columns={colsMcuRange}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>

        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                AVG ANNUAL NORMS
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsAvgAnnualNorms}
                  columns={colsAvgAnnualNorms}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>

        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                CONSECUTIVE DAYS
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsConsecutiveDays}
                  columns={colsConsecutiveDays}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                MIIS NORMS RAW DATA
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsMiisNormsRawData}
                  columns={colsMiisNormsRawData}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                BEST ACHIEVED NORMS
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsBestAchivedNorms}
                  columns={colsBestAchivedNorms}
                  permissions={{ allAction: false }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasisPe
