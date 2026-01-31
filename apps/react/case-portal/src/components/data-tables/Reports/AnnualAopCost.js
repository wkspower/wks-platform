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
import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import getKendoColumns from 'components/data-tables/CommonHeader/kendoHeader'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import { Button } from '@mui/material'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { getRoleName } from 'services/role-service'
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
  // const READ_ONLY = getRoleName(keycloak)

  const [rowsProduction, setRowsProduction] = useState([])
  const [rowsPrice, setRowsPrice] = useState([])
  const [rowsNorm, setRowsNorm] = useState([])
  const [rowsQuantity, setRowsQuantity] = useState([])
  const [rowsNormCost, setRowsNormCost] = useState([])
  const [headers2, setHeaders2] = useState([])
  const [keys2, setKeys2] = useState([])
  const [loading, setLoading] = useState(false)
  const [showGrids, setShowGrids] = useState({})

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const fetchData = async (reportType, setState) => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      const data = await DataService.getAnnualCostAopReport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        reportType,
      )
      if (data?.code === 200) {
        if (reportType === 'price') {
          setHeaders2(data?.data[0]?.headers)
          setKeys2(data?.data[0]?.keys)
          setState(
            data?.data[0]?.results?.map((item, index) => ({
              ...item,
              id: index,
              isEditable: false,
            })),
          )
        } else {
          setState(
            data?.data?.map((item, index) => ({
              ...item,
              id: index,
              isEditable: false,
            })),
          )
        }
      }
    } catch (error) {
      console.error(`Error fetching ${reportType} data:`, error)
    }
  }

  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterProduction()
  const colsProduction = getKendoColumns({
    headerMap,
    type: 'Production',
    valueFormat,
  })
  const colsPrice = getKendoColumns({
    headerMap,
    type: 'Price',
    headers2,
    keys2,
    valueFormat,
  })
  const colsNorm = getKendoColumns({ headerMap, type: 'Norm', valueFormat })
  const colsQuantity = getKendoColumns({
    headerMap,
    type: 'Quantity',
    valueFormat,
  })
  const colsNormCost = getKendoColumns({
    headerMap,
    type: 'NormCost',
    valueFormat,
  })

  useEffect(() => {
    let isCancelled = false
    setRowsProduction([])
    setRowsPrice([])
    setRowsNorm([])
    setRowsQuantity([])
    setRowsNormCost([])
    const fetchAllData = async () => {
      setLoading(true)

      await Promise.all([
        fetchData('production', (data) => {
          if (!isCancelled) setRowsProduction(data)
        }),
        fetchData('price', (data) => {
          if (!isCancelled) setRowsPrice(data)
        }),
        fetchData('norm', (data) => {
          if (!isCancelled) setRowsNorm(data)
        }),
        fetchData('quantity', (data) => {
          if (!isCancelled) setRowsQuantity(data)
        }),
        fetchData('normCost', (data) => {
          if (!isCancelled) setRowsNormCost(data)
        }),
      ])

      if (!isCancelled) {
        setLoading(false)
      }
    }

    fetchAllData()

    return () => {
      isCancelled = true
    }
  }, [PLANT_ID, AOP_YEAR, keycloak])

  useEffect(() => {
    const timers = [
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, production: true })),
        0,
      ),
      setTimeout(() => setShowGrids((prev) => ({ ...prev, price: true })), 100),
      setTimeout(() => setShowGrids((prev) => ({ ...prev, norm: true })), 200),
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, quantity: true })),
        300,
      ),
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, normCost: true })),
        400,
      ),
    ]
    return () => timers.forEach(clearTimeout)
  }, [])

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

    options1.sheets[1] = options2.sheets[0]
    options1.sheets[2] = options3.sheets[0]
    options1.sheets[3] = options4.sheets[0]
    options1.sheets[4] = options5.sheets[0]

    options1.sheets[0].title = 'Production'
    options1.sheets[1].title = 'Price'
    options1.sheets[2].title = 'Norms'
    options1.sheets[3].title = 'Quantity'
    options1.sheets[4].title = 'Norm Cost'

    exportRef1.current.save(options1)
  }

  const fileName = `Annual AOP Cost ${new Date().toISOString().replace(/T/, ' ').replace(/:/g, '-').split('.')[0]}.xlsx`

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div style={{ display: 'none' }}>
        {[rowsProduction, rowsPrice, rowsNorm, rowsQuantity, rowsNormCost].map(
          (data, i) => (
            <ExcelExport
              key={i}
              data={data}
              ref={
                [exportRef1, exportRef2, exportRef3, exportRef4, exportRef5][i]
              }
              fileName={fileName}
            >
              {[
                colsProduction,
                colsPrice,
                colsNorm,
                colsQuantity,
                colsNormCost,
              ][i].map((col) => (
                <ExcelExportColumn
                  key={col.field}
                  field={col.field}
                  title={col.title}
                />
              ))}
            </ExcelExport>
          ),
        )}
      </div>

      {!isOldYear && (
        <Box display='flex' justifyContent='flex-end' mb='2px'>
          <Button
            variant='contained'
            onClick={exportAllGrids}
            className='btn-save'
            // disabled={READ_ONLY}
          >
            Export
          </Button>
        </Box>
      )}

      <Box display='flex' flexDirection='column' gap={1}>
        {[
          {
            label: 'Production',
            visible: showGrids.production,
            rows: rowsProduction,
            cols: colsProduction,
          },
          {
            label: 'Price',
            visible: showGrids.price,
            rows: rowsPrice,
            cols: colsPrice,
          },
          {
            label: 'Norm',
            visible: showGrids.norm,
            rows: rowsNorm,
            cols: colsNorm,
          },
          {
            label:
              lowerVertName === 'meg'
                ? 'Quantity (EOE Production * Individual Particulars Norms Value)'
                : 'Quantity (Production * Individual Particulars Norms Value)',
            visible: showGrids.quantity,
            rows: rowsQuantity,
            cols: colsQuantity,
          },
          {
            label:
              'Annual AOP Cost ((Total Quantity * AvgPrice)/Total Production)',
            visible: showGrids.normCost,
            rows: rowsNormCost,
            cols: colsNormCost,
          },
        ].map(
          (section, i) =>
            section.visible && (
              <CustomAccordion key={i} defaultExpanded disableGutters>
                <CustomAccordionSummary>
                  <Typography component='span' className='grid-title'>
                    {section.label}
                  </Typography>
                </CustomAccordionSummary>
                <CustomAccordionDetails>
                  <Box sx={{ width: '100%', margin: 0 }}>
                    <KendoDataGrid
                      rows={section.rows}
                      columns={section.cols}
                      pageSize={10}
                      scrollable='none'
                      permissions={{
                        allAction: false,
                        isHeight: section?.rows?.length > 15,
                      }}
                    />
                  </Box>
                </CustomAccordionDetails>
              </CustomAccordion>
            ),
        )}
      </Box>
    </div>
  )
}

export default AnnualAopCost
