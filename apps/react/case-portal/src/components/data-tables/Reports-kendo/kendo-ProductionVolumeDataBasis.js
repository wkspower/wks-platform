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
import {
  Button,
  MenuItem,
  TextField,
} from '../../../../node_modules/@mui/material/index'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../../node_modules/@progress/kendo-react-excel-export/index'
import moment from '../../../../node_modules/moment/moment'
import getKendoProductionColumns from '../CommonHeader/KendoProdVolBHeader'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
// import ProductionVolumeDataBasisPe from './kendo-ProductionVolumeDataBasisPe'
import ProductionVolumeDataBasisPe from './ProductionVolumeDataBasisPe'

import { getRoleName } from 'services/role-service'
const ProductionVolumeDataBasis = () => {
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

  const units = ['TPH', 'TPD']
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [rowsMC, setRowsMC] = useState([])
  const [rowsMCYearWise, setRowsMCYearWise] = useState([])
  const [rowsCalculatedData, setRowsCalculatedData] = useState([])
  const [rowsRawData, setRowsRowData] = useState([])
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
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const isOldYear = oldYear?.oldYear === 1
  const [loading, setLoading] = useState(false)
  const [showGrids, setShowGrids] = useState({})
  const headerMap = generateHeaderNames(AOP_YEAR)
  const fetchData = async (reportType, setState, selectedUnit) => {
    if (lowerVertName != 'meg') return
    if (!PLANT_ID || !AOP_YEAR) return
    if (!selectedUnit) return
    try {
      setLoading(true)
      var data = []
      data = await DataService.getProductionVolDataBasis(
        keycloak,
        reportType,
        selectedUnit,
        PLANT_ID,
        AOP_YEAR,
      )

      if (data?.code === 200) {
        const rowsWithId = data?.data?.map((item, index) => ({
          ...item,
          id: index,
          isEditable: false,

          startDate: item?.startDate
            ? moment(item.startDate, 'DD-MM-YYYY').toDate()
            : null,
          dateTime: item?.dateTime
            ? moment(item.dateTime, 'DD-MM-YYYY').toDate()
            : null,
          endDate: item?.endDate
            ? moment(item.endDate, 'DD-MM-YYYY').toDate()
            : null,
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

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const valueFormat = ValueFormatterProduction()
  const colsMC = getKendoProductionColumns({
    headerMap,
    type: 'MC',
    valueFormat,
  })

  const colsMCYearwise = getKendoProductionColumns({
    headerMap,
    type: 'MC Yearwise',
    valueFormat,
  })

  const colsCalculatedData = getKendoProductionColumns({
    headerMap,
    type: 'Calculated Data',
    valueFormat,
  })

  const colsRowData = getKendoProductionColumns({
    headerMap,
    type: 'RowData',
    valueFormat,
  })

  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true)
      await Promise.all([
        fetchData('MC', setRowsMC, selectedUnit),
        fetchData('MC Yearwise', setRowsMCYearWise, selectedUnit),
        fetchData('Calculated Data', setRowsCalculatedData, selectedUnit),
        fetchData('RowData', setRowsRowData, selectedUnit),
      ])
      setLoading(false)
    }
    if (lowerVertName == 'meg') {
      fetchAllData()
    }
  }, [oldYear, yearChanged, keycloak, PLANT_ID, selectedUnit])

  useEffect(() => {
    const timers = [
      setTimeout(() => setShowGrids((prev) => ({ ...prev, mc: true })), 100),
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, AOP_YEAR: true })),
        300,
      ),
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, calculated: true })),
        500,
      ),
      setTimeout(() => setShowGrids((prev) => ({ ...prev, raw: true })), 700),
    ]
    return () => timers.forEach(clearTimeout)
  }, [])

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

  const fileName = `Production Volume Data ${new Date().toISOString().replace(/T/, ' ').replace(/:/g, '-').split('.')[0]}.xlsx`

  if (lowerVertName != 'meg') {
    return <ProductionVolumeDataBasisPe />
  } else
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
          {[rowsMC, rowsMCYearWise, rowsCalculatedData, rowsRawData].map(
            (data, i) => (
              <ExcelExport
                key={i}
                data={data}
                ref={[exportRef1, exportRef2, exportRef3, exportRef4][i]}
                fileName={fileName}
              >
                {[colsMC, colsMCYearwise, colsCalculatedData, colsRowData][
                  i
                ].map((col) => (
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

        <Box display='flex' justifyContent='flex-end' gap={1}>
          {!isOldYear && (
            <Button
              variant='contained'
              onClick={exportAllGrids}
              className='btn-save'
              disabled={READ_ONLY}
            >
              Export
            </Button>
          )}
          <TextField
            select
            value={selectedUnit || 'TPH'}
            onChange={(e) => {
              setSelectedUnit(e.target.value)
              handleUnitChange(e.target.value)
            }}
            className='dropdown-select'
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
          {[
            {
              label: 'MC',
              visible: showGrids.mc,
              rows: rowsMC,
              cols: colsMC,
            },
            {
              label: 'MC Yearwise',
              visible: showGrids.AOP_YEAR,
              rows: rowsMCYearWise,
              cols: colsMCYearwise,
            },
            {
              label: 'Calculated Data',
              visible: showGrids.calculated,
              rows: rowsCalculatedData,
              cols: colsCalculatedData,
            },
            {
              label: 'Raw Data',
              visible: showGrids.raw,
              rows: rowsRawData,
              cols: colsRowData,
            },
          ].map(
            (section, index) =>
              section.visible && (
                <div key={index}>
                  <CustomAccordion defaultExpanded disableGutters>
                    <CustomAccordionSummary
                      aria-controls='meg-grid-content'
                      id='meg-grid-header'
                    >
                      <Typography component='span' className='grid-title'>
                        {section.label}
                      </Typography>
                    </CustomAccordionSummary>
                    <CustomAccordionDetails>
                      <Box sx={{ width: '100%', margin: 0 }}>
                        <KendoDataGrid
                          rows={section.rows}
                          columns={section.cols}
                          permissions={{
                            allAction: false,
                            isHeight: section?.rows?.length > 15,
                          }}
                        />
                      </Box>
                    </CustomAccordionDetails>
                  </CustomAccordion>
                </div>
              ),
          )}
        </Box>
      </div>
    )
}

export default ProductionVolumeDataBasis
