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
import getKendoProductionColumns from '../CommonHeader/KendoProdVolBHeader'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../../node_modules/@progress/kendo-react-excel-export/index'
import { Button } from '../../../../node_modules/@mui/material/index'

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

const ProductionVolumeDataBasis = () => {
  const keycloak = useSession()

  const [rowsMC, setRowsMC] = useState([])
  const [rowsMCYearWise, setRowsMCYearWise] = useState([])
  const [rowsCalculatedData, setRowsCalculatedData] = useState([])
  const [rowsRawData, setRowsRowData] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)

  const fetchData = async (reportType, setState) => {
    try {
      setLoading(true)
      var data = []
      data = await DataService.getProductionVolDataBasis(keycloak, reportType)

      if (data?.code === 200) {
        const rowsWithId = data?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
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

  const colsMC = getKendoProductionColumns({
    headerMap,
    type: 'MC',
  })

  const colsMCYearwise = getKendoProductionColumns({
    headerMap,
    type: 'MC Yearwise',
  })

  const colsCalculatedData = getKendoProductionColumns({
    headerMap,
    type: 'Calculated Data',
  })

  const colsRowData = getKendoProductionColumns({
    headerMap,
    type: 'RowData',
  })

  useEffect(() => {
    fetchData('MC', setRowsMC)
    fetchData('MC Yearwise', setRowsMCYearWise)
    fetchData('Calculated Data', setRowsCalculatedData)
    fetchData('RowData', setRowsRowData)
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])
  const exportRef1 = useRef(null)
  const exportRef2 = useRef(null)
  const exportRef3 = useRef(null)
  const exportRef4 = useRef(null)

  const exportAllGrids = () => {
    const options1 = exportRef1.current.workbookOptions()
    const options2 = exportRef2.current.workbookOptions()
    const options3 = exportRef3.current.workbookOptions()
    const options4 = exportRef4.current.workbookOptions()

    // Add additional sheets to first export
    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]
    options1.sheets[3] = options4.sheets[0]

    // Rename sheets
    options1.sheets[0].title = 'MC'
    options1.sheets[1].title = 'MC Yearwise'
    options1.sheets[2].title = 'Calculated Data'
    options1.sheets[3].title = 'Raw Data'

    exportRef1.current.save(options1)
  }

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Production Volume Data Basis ${currentDateTime}.xlsx`

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
        <ExcelExport data={rowsMC} ref={exportRef1} fileName={fileName}>
          {colsMC.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsMCYearWise} ref={exportRef2}>
          {colsMCYearwise.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsCalculatedData} ref={exportRef3}>
          {colsCalculatedData.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsRawData} ref={exportRef4}>
          {colsRowData.map((col) => (
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
                MC
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsMC}
                  columns={colsMC}
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
                MC Yearwise
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsMCYearWise}
                  columns={colsMCYearwise}
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
                Calculated Data
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsCalculatedData}
                  columns={colsCalculatedData}
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
                Raw Data
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsRawData}
                  columns={colsRowData}
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

export default ProductionVolumeDataBasis
