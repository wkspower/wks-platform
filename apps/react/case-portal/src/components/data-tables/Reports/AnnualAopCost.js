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
import {
  MenuItem,
  TextField,
} from '../../../../node_modules/@mui/material/index'
import getEnhancedAnnualAopCostReport from '../CommonHeader/AopCostReportHeader'

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

  const [unit, setUnit] = useState([])
  const [selectedUnit, setSelectedUnit] = useState('')
  const [loading, setLoading] = useState(false)

  const handleUnitChange = (event) => {
    setSelectedUnit(event)
  }

  useEffect(() => {
    if (unit?.length > 0) {
      setSelectedUnit(unit[0].name)
    }
  }, [unit])

  const fetchData = async (reportType, setState, selectedDropdown) => {
    try {
      setLoading(true)
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
        setLoading(false)

        if (reportType == 'aopYearFilter') {
          setUnit(data?.data)
          setSelectedUnit(data?.data[0]?.name)
        } else if (reportType == 'price') {
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
        // setLoading(false)
      } else {
        console.error(`Error fetching ${reportType} data`)
        setLoading(false)
      }
    } catch (error) {
      console.error(`Error fetching ${reportType} data:`, error)
      setLoading(false)
    }
  }

  // const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const year = extractYear(selectedUnit) || '2025-26'
  const headerMap = generateHeaderNames(year)

  function extractYear(dropdownValue) {
    if (!dropdownValue) return ''
    const parts = dropdownValue.trim().split(' ')
    return parts.length > 1 ? parts[1] : ''
  }

  const colsProduction = getEnhancedAnnualAopCostReport({
    headerMap,
    type: 'Production',
  })

  // const colsPrice = getEnhancedAnnualAopCostReport({
  //   headerMap,
  //   type: 'Price',
  // })

  const colsPrice = getEnhancedAnnualAopCostReport({
    headerMap,
    type: 'Price',
    headers2,
    keys2,
  })

  const colsNorm = getEnhancedAnnualAopCostReport({
    headerMap,
    type: 'Norm',
  })

  const colsQuantity = getEnhancedAnnualAopCostReport({
    headerMap,
    type: 'Quantity',
  })

  const colsNormCost = getEnhancedAnnualAopCostReport({
    headerMap,
    type: 'NormCost',
  })

  useEffect(() => {
    fetchData('aopYearFilter', setUnit)
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  useEffect(() => {
    if (selectedUnit) {
      fetchData('production', setRowsProduction, selectedUnit)
      fetchData('price', setRowsPrice, selectedUnit)
      fetchData('norm', setRowsNorm, selectedUnit)
      fetchData('quantity', setRowsQuantity, selectedUnit)
      fetchData('normCost', setRowsNormCost, selectedUnit)
    }
  }, [
    selectedUnit,
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
  ])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' flexDirection='column' gap={2}>
        <Box display='flex' justifyContent='flex-end'>
          <TextField
            select
            value={selectedUnit}
            onChange={(e) => {
              const value = e.target.value
              setSelectedUnit(value)
              handleUnitChange(value)
            }}
            sx={{ width: '200px', backgroundColor: '#FFFFFF' }}
            variant='outlined'
            label='Select AOP Year'
          >
            <MenuItem value='' disabled>
              Select AOP Year
            </MenuItem>

            {unit?.map((unit) => (
              <MenuItem key={unit?.name} value={unit?.name}>
                {unit?.displayName}
              </MenuItem>
            ))}
          </TextField>
        </Box>

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
                <AopCostReportView
                  rows={rowsProduction}
                  columns={colsProduction}
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
                Price
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsPrice}
                  columns={colsPrice}
                  height='340px'
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
                Norm
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsNorm}
                  columns={colsNorm}
                  height='340px'
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
                Quantity (EOE Production * Individual Particluars Norms Value)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsQuantity}
                  columns={colsQuantity}
                  height='340px'
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
                Annual AOP Cost ((Total Quantity * AvgPrice)/Total Production)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <AopCostReportView
                  rows={rowsNormCost}
                  columns={colsNormCost}
                  height='340px'
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </div>
      </Box>
    </div>
  )
}

export default AnnualAopCost
