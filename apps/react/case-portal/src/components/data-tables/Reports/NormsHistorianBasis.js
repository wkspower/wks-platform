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
import getEnhancedNormsHistorianBasis from '../CommonHeader/NormsHistorianValuesHeaders'

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

const NormsHistorianBasis = () => {
  const keycloak = useSession()

  const [rowsHistorianValues, setHistorianValues] = useState([])
  const [rowsMcuAndNormGrid, setMcuAndNormGrid] = useState([])
  const [rowsProductionVolumeData, setProductionVolumeData] = useState([])

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
      data = await DataService.getNormsHistorianBasis(keycloak, reportType)

      if (data?.code === 200) {
        const rowsWithId = data?.data?.normHistoricBasisData?.map(
          (item, index) => ({
            ...item,
            id: index,
            isEditable: false,
          }),
        )
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

  const colsHistorianValues = getEnhancedNormsHistorianBasis({
    headerMap,
    type: 'HistorianValues',
  })

  const colsMcuAndNormGrid = getEnhancedNormsHistorianBasis({
    headerMap,
    type: 'McuAndNormGrid',
  })

  const colsProductionVolumeData = getEnhancedNormsHistorianBasis({
    headerMap,
    type: 'ProductionVolumeData',
  })

  useEffect(() => {
    fetchData('HistorianValues', setHistorianValues)
    fetchData('McuAndNormGrid', setMcuAndNormGrid)
    fetchData('ProductionVolumeData', setProductionVolumeData)
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
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Production Volume Data
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsProductionVolumeData}
                  columns={colsProductionVolumeData}
                  height='93px'
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
                Mcu & Norm
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsMcuAndNormGrid}
                  columns={colsMcuAndNormGrid}
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
                Historian Values
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsHistorianValues}
                  columns={colsHistorianValues}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
      </Box>
    </div>
  )
}

export default NormsHistorianBasis
