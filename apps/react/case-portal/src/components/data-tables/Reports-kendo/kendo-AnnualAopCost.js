import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import getKendoColumns from 'components/data-tables/CommonHeader/kendoHeader'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../../node_modules/@progress/kendo-react-excel-export/index'
import { Button } from '../../../../node_modules/@mui/material/index'
import { useRef } from 'react'

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

const AnnualAopCost = () => {
  const keycloak = useSession()
  const [rowsProduction, setRowsProduction] = useState([])
  const [rowsPrice, setRowsPrice] = useState([])
  const [rowsNorm, setRowsNorm] = useState([])
  const [rowsQuantity, setRowsQuantity] = useState([])
  const [rowsNormCost, setRowsNormCost] = useState([])
  const [headers2, setHeaders2] = useState([])
  const [keys2, setKeys2] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const fetchData = async (reportType, setState, selectedDropdown) => {
    try {
      selectedDropdown = localStorage.getItem('year')
      var data = []
      data = await DataService.getAnnualCostAopReport(
        keycloak,
        reportType,
        selectedDropdown,
      )
      if (data?.code === 200) {
        const rowsWithId = data?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,
        }))
        if (reportType == 'price') {
          const headers2 = data?.data[0]?.headers
          setHeaders2(headers2)
          const keys2 = data?.data[0]?.keys
          setKeys2(keys2)
          const rowsWithId2 = data?.data[0]?.results?.map((item, index) => ({
            ...item,
            id: index,
            isEditable: false,
          }))
          setState(rowsWithId2)
        } else {
          setState(rowsWithId)
        }
      } else {
        console.error(`Error fetching ${reportType} data`)
        setLoading(false)
      }
    } catch (error) {
      console.error(`Error fetching ${reportType} data:`, error)
      setLoading(false)
    }
  }

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const colsProduction = getKendoColumns({
    headerMap,
    type: 'Production',
  })
  const colsPrice = getKendoColumns({
    headerMap,
    type: 'Price',
    headers2,
    keys2,
  })
  const colsNorm = getKendoColumns({
    headerMap,
    type: 'Norm',
  })
  const colsQuantity = getKendoColumns({
    headerMap,
    type: 'Quantity',
  })
  const colsNormCost = getKendoColumns({
    headerMap,
    type: 'NormCost',
  })

  //useEffect(() => {
  // fetchData('aopYearFilter', setUnit)
  //}, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true)

      const allFetches = [
        fetchData('production', setRowsProduction),
        fetchData('price', setRowsPrice),
        fetchData('norm', setRowsNorm),
        fetchData('quantity', setRowsQuantity),
        fetchData('normCost', setRowsNormCost),
      ]

      await Promise.all(allFetches)
      setLoading(false)
    }

    fetchAllData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const exportRef1 = useRef(null)
  const exportRef2 = useRef(null)
  const exportRef3 = useRef(null)
  const exportRef4 = useRef(null)
  const exportRef5 = useRef(null)

  const exportAllGrids = () => {
    const options1 = exportRef1.current.workbookOptions()
    const options2 = exportRef2.current.workbookOptions()
    const options3 = exportRef3.current.workbookOptions()
    const options4 = exportRef4.current.workbookOptions()
    const options5 = exportRef5.current.workbookOptions()

    // Add additional sheets to first export
    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]
    options1.sheets[3] = options4.sheets[0]
    options1.sheets[4] = options5.sheets[0]

    // Rename sheets
    options1.sheets[0].title = 'Production'
    options1.sheets[1].title = 'Price'
    options1.sheets[2].title = 'Norms'
    options1.sheets[3].title = 'Quantity'
    options1.sheets[4].title = 'Norm Cost'

    exportRef1.current.save(options1)
  }

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Annual AOP Cost ${currentDateTime}.xlsx`

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
        <ExcelExport data={rowsProduction} ref={exportRef1} fileName={fileName}>
          {colsProduction.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsPrice} ref={exportRef2}>
          {colsPrice.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsNorm} ref={exportRef3}>
          {colsNorm.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>
        <ExcelExport data={rowsQuantity} ref={exportRef4}>
          {colsQuantity.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsNormCost} ref={exportRef5}>
          {colsNormCost.map((col) => (
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

      <Box display='flex' flexDirection='column' gap={1}>
        <div>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Production
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid
                  rows={rowsProduction}
                  columns={colsProduction}
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
                Price
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid rows={rowsPrice} columns={colsPrice} />
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
                Norm
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid rows={rowsNorm} columns={colsNorm} />
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
                Quantity (EOE Production * Individual Particluars Norms Value)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid rows={rowsQuantity} columns={colsQuantity} />
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
                Annual AOP Cost ((Total Quantity * AvgPrice)/Total Production)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataGrid rows={rowsNormCost} columns={colsNormCost} />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
      </Box>
    </div>
  )
}

export default AnnualAopCost
