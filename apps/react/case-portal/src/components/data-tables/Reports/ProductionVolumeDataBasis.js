import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import AopCostReportView from 'components/data-tables-views/ReportDataGrid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import getEnhancedProductionVolDataBasis from '../CommonHeader/MCHeaders'

// const CustomAccordion = styled((props) => (
//   <MuiAccordion disableGutters elevation={0} square {...props} />
// ))(() => ({
//   position: 'unset',
//   border: 'none',
//   boxShadow: 'none',
//   margin: '0px',
//   '&:before': {
//     display: 'none',
//   },
// }))

// const CustomAccordionSummary = styled((props) => (
//   <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
// ))(() => ({
//   backgroundColor: '#fff',
//   padding: '0px 12px',
//   minHeight: '40px',
//   '& .MuiAccordionSummary-content': {
//     margin: '8px 0',
//   },
// }))

// const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
//   padding: '0px 0px 12px',
//   backgroundColor: '#F2F3F8',
// }))

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

  const colsMC = getEnhancedProductionVolDataBasis({
    headerMap,
    type: 'MC',
  })

  const colsMCYearwise = getEnhancedProductionVolDataBasis({
    headerMap,
    type: 'MC Yearwise',
  })

  const colsCalculatedData = getEnhancedProductionVolDataBasis({
    headerMap,
    type: 'Calculated Data',
  })

  const colsRowData = getEnhancedProductionVolDataBasis({
    headerMap,
    type: 'RowData',
  })

  useEffect(() => {
    fetchData('MC', setRowsMC)
    fetchData('MC Yearwise', setRowsMCYearWise)
    fetchData('Calculated Data', setRowsCalculatedData)
    fetchData('RowData', setRowsRowData)
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' flexDirection='column' gap={2}>
        <div>
          {/* <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            > */}
          <Typography component='span' className='grid-title'>
            MC
          </Typography>
          {/* </CustomAccordionSummary> */}
          {/* <CustomAccordionDetails> */}
          <Box sx={{ width: '100%', margin: 0 }}>
            <AopCostReportView rows={rowsMC} columns={colsMC} height='93px' />
          </Box>
          {/* </CustomAccordionDetails> */}
          {/* </CustomAccordion> */}
        </div>

        <div>
          {/* <CustomAccordion defaultExpanded disableGutters> */}
          {/* <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            > */}
          <Typography component='span' className='grid-title'>
            MC Yearwise
          </Typography>
          {/* </CustomAccordionSummary> */}
          {/* <CustomAccordionDetails> */}
          <Box sx={{ width: '100%', margin: 0 }}>
            <AopCostReportView
              rows={rowsMCYearWise}
              columns={colsMCYearwise}
              height='340px'
            />
          </Box>
          {/* </CustomAccordionDetails> */}
          {/* </CustomAccordion> */}
        </div>

        <div>
          {/* <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            > */}
          <Typography component='span' className='grid-title'>
            Calculated Data
          </Typography>
          {/* </CustomAccordionSummary> */}
          {/* <CustomAccordionDetails> */}
          <Box sx={{ width: '100%', margin: 0 }}>
            <AopCostReportView
              rows={rowsCalculatedData}
              columns={colsCalculatedData}
              height='340px'
            />
          </Box>
          {/* </CustomAccordionDetails>
          </CustomAccordion> */}
        </div>

        <div>
          {/* <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            > */}
          <Typography component='span' className='grid-title'>
            Raw Data
          </Typography>
          {/* </CustomAccordionSummary> */}
          {/* <CustomAccordionDetails> */}
          <Box sx={{ width: '100%', margin: 0 }}>
            <AopCostReportView
              rows={rowsRawData}
              columns={colsRowData}
              height='340px'
            />
          </Box>
          {/* </CustomAccordionDetails>
          </CustomAccordion> */}
        </div>
      </Box>
    </div>
  )
}

export default ProductionVolumeDataBasis
