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
import { useEffect, useState, useRef } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import getKendoNormsHistorianColumns from '../CommonHeader/KendoNormHistoryHeader'
import {
  Button,
  MenuItem,
  TextField,
} from '../../../../node_modules/@mui/material/index'
import moment from '../../../../node_modules/moment/moment'

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
  const [selectedUnit, setSelectedUnit] = useState('TPH')

  const [rowsHistorianValues, setHistorianValues] = useState([])
  const [rowsMcuAndNormGrid, setMcuAndNormGrid] = useState([])
  const [rowsProductionVolumeData, setProductionVolumeData] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)
  const units = ['TPH', 'TPD']

  useEffect(() => {
    const fetchAllData = async (selectedUnit) => {
      if (!selectedUnit) return
      setLoading(true)

      try {
        const results = await Promise.all([
          DataService.getNormsHistorianBasis(
            keycloak,
            'HistorianValues',
            selectedUnit,
          ),
          DataService.getNormsHistorianBasis(
            keycloak,
            'McuAndNormGrid',
            selectedUnit,
          ),

          DataService.getNormsHistorianBasis(
            keycloak,
            'ProductionVolumeData',
            selectedUnit,
          ),
        ])

        const [historianRes, mcuRes, prodRes] = results

        if (historianRes?.code === 200) {
          const rows = historianRes.data.normHistoricBasisData.map(
            (item, index) => ({
              ...item,
              id: index,
              isEditable: false,
              dateTime: item?.dateTime
                ? moment(item.dateTime, 'DD-MM-YYYY').toDate()
                : null,
            }),
          )
          setHistorianValues(rows)
        }

        if (mcuRes?.code === 200) {
          const rows = mcuRes.data.normHistoricBasisData.map((item, index) => ({
            ...item,
            id: index,
            isEditable: false,
            dateTime: item?.dateTime
              ? moment(item.dateTime, 'DD-MM-YYYY').toDate()
              : null,
          }))
          setMcuAndNormGrid(rows)
        }

        if (prodRes?.code === 200) {
          const rows = prodRes.data.normHistoricBasisData.map(
            (item, index) => ({
              ...item,
              id: index,
              isEditable: false,
              dateTime: item?.dateTime
                ? moment(item.dateTime, 'DD-MM-YYYY').toDate()
                : null,
            }),
          )
          setProductionVolumeData(rows)
        }
      } catch (error) {
        console.error('Error fetching data:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchAllData(selectedUnit)
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
    selectedUnit,
  ])

  const year = localStorage.getItem('year')
  const headerMap = generateHeaderNames(year)

  const colsHistorianValues = getKendoNormsHistorianColumns({
    headerMap,
    type: 'HistorianValues',
  })

  const colsMcuAndNormGrid = getKendoNormsHistorianColumns({
    headerMap,
    type: 'McuAndNormGrid',
  })

  const colsProductionVolumeData = getKendoNormsHistorianColumns({
    headerMap,
    type: 'ProductionVolumeData',
  })

  const exportRef1 = useRef(null)
  const exportRef2 = useRef(null)
  const exportRef3 = useRef(null)

  const exportAllGrids = () => {
    const options1 = exportRef1.current.workbookOptions()
    const options2 = exportRef2.current.workbookOptions()
    const options3 = exportRef3.current.workbookOptions()

    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]

    options1.sheets[0].title = 'Production Volume'
    options1.sheets[1].title = 'MCU & Norm'
    options1.sheets[2].title = 'Current Values'

    exportRef1.current.save(options1)
  }

  const currentDateTime = new Date()
    .toISOString()
    .replace(/T/, ' ')
    .replace(/:/g, '-')
    .split('.')[0]
  const fileName = `Norms Historian Basis ${currentDateTime}.xlsx`

  const handleUnitChange = (unit) => {
    setLoading(true)
    setSelectedUnit(unit)
  }

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
        <ExcelExport
          data={rowsProductionVolumeData}
          ref={exportRef1}
          fileName={fileName}
        >
          {colsProductionVolumeData.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsMcuAndNormGrid} ref={exportRef2}>
          {colsMcuAndNormGrid.map((col) => (
            <ExcelExportColumn
              key={col.field}
              field={col.field}
              title={col.title}
            />
          ))}
        </ExcelExport>

        <ExcelExport data={rowsHistorianValues} ref={exportRef3}>
          {colsHistorianValues.map((col) => (
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

        <TextField
          select
          value={selectedUnit || 'TPH'}
          onChange={(e) => {
            setSelectedUnit(e.target.value)
            handleUnitChange(e.target.value)
          }}
          sx={{
            width: '150px',
            backgroundColor: '#FFFFFF',
            marginLeft: '12px',
          }}
          variant='outlined'
          label='Select UOM'
        >
          <MenuItem value='' disabled>
            Select UOM
          </MenuItem>

          {units.map((unit) => (
            <MenuItem key={unit} value={unit}>
              {unit}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <Box display='flex' flexDirection='column' gap={2}>
        {/* Accordion 1 */}
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary>
            <Typography className='grid-title'>
              Production Volume Data
            </Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box sx={{ width: '100%' }}>
              <KendoDataGrid
                rows={rowsProductionVolumeData}
                columns={colsProductionVolumeData}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>

        {/* Accordion 2 */}
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary>
            <Typography className='grid-title'>MCU & Norm</Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box sx={{ width: '100%' }}>
              <KendoDataGrid
                rows={rowsMcuAndNormGrid}
                columns={colsMcuAndNormGrid}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>

        {/* Accordion 3 */}
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary>
            <Typography className='grid-title'>Current Values</Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box sx={{ width: '100%' }}>
              <KendoDataGrid
                rows={rowsHistorianValues}
                columns={colsHistorianValues}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>
      </Box>
    </div>
  )
}

export default NormsHistorianBasis
