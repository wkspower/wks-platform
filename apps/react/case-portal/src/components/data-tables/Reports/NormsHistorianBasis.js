import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'
import moment from 'moment'
import { useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import KendoDataGrid from 'components/Kendo-Report-DataGrid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import getKendoNormsHistorianColumns from '../CommonHeader/KendoNormHistoryHeader'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
// import NormsHistorianBasisAromatics from './NormsHistorianBasisAromatics'
import NormsHistorianBasisPe from './NormsHistorianBasisPe'
import { getRoleName } from 'services/role-service'
const NormsHistorianBasis = () => {
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

  const [rowsHistorianValues, setHistorianValues] = useState([])
  const [rowsMcuAndNormGrid, setMcuAndNormGrid] = useState([])
  const [rowsProductionVolumeData, setProductionVolumeData] = useState([])

  const [rowsBestAchieved, setRowsBestAchieved] = useState([])
  const [rowsExpressionBased, setRowsExpressionBased] = useState([])
  const [rowsCurrentYear, setRowsCurrentYear] = useState([])

  const [colsBestAchieved, setColsBestAchieved] = useState([])
  const [colsExpressionBased, setColsExpressionBased] = useState([])
  const [colsCurrentYear, setColsCurrentYear] = useState([])

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
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear

  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [loading, setLoading] = useState(false)
  const [showGrids, setShowGrids] = useState({})

  const [selectedUnit, setSelectedUnit] = useState('')
  const [units, setUnits] = useState([])

  const headerMap = generateHeaderNames(AOP_YEAR)

  const exportRef1 = useRef(null)
  const exportRef2 = useRef(null)
  const exportRef3 = useRef(null)
  const valueFormat = ValueFormatterProduction()
  const generateColumns = (type) =>
    getKendoNormsHistorianColumns({ headerMap, type, valueFormat })

  const fetchAllData = async (selectedUnit) => {
    if (!PLANT_ID || !AOP_YEAR) return
    if (lowerVertName != 'meg') return

    if (!selectedUnit) return
    setLoading(true)
    let isCancelled = false

    try {
      let results = []

      if (lowerVertName === 'cracker') {
        results = await Promise.allSettled([
          DataService.getProductionVolDataBasisMode(
            keycloak,
            'bestachieved',
            undefined,
            undefined,
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),

          DataService.getProductionVolDataBasisMode(
            keycloak,
            'expessionbased',
            undefined,
            undefined,
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),
          DataService.getProductionVolDataBasisMode(
            keycloak,
            'currentyear',
            undefined,
            undefined,
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),
        ])
      } else {
        results = await Promise.allSettled([
          DataService.getNormsHistorianBasis(
            keycloak,
            'HistorianValues',
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),
          DataService.getNormsHistorianBasis(
            keycloak,
            'McuAndNormGrid',
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),
          DataService.getNormsHistorianBasis(
            keycloak,
            'ProductionVolumeData',
            selectedUnit,
            PLANT_ID,
            AOP_YEAR,
          ),
        ])
      }

      const processData = (result) =>
        result?.status === 'fulfilled' && result?.value?.code === 200
          ? result.value.data.normHistoricBasisData.map((item, index) => ({
              ...item,
              id: index,
              isEditable: false,
              dateTime: item?.dateTime
                ? moment(item.dateTime, 'DD-MM-YYYY').toDate()
                : null,
            }))
          : []

      if (lowerVertName === 'cracker') {
        const [best, expr, current] = results
        const bestRows = processData(best)
        const exprRows = processData(expr)
        const currRows = processData(current)

        setRowsBestAchieved(bestRows)
        setRowsExpressionBased(exprRows)
        setRowsCurrentYear(currRows)

        setColsBestAchieved(generateColumns('bestachieved'))
        setColsExpressionBased(generateColumns('expessionbased'))
        setColsCurrentYear(generateColumns('currentyear'))
      } else {
        const [historian, mcu, prod] = results
        const historianRows = processData(historian)
        const mcuRows = processData(mcu)
        const prodRows = processData(prod)

        setHistorianValues(historianRows)
        setMcuAndNormGrid(mcuRows)
        setProductionVolumeData(prodRows)
      }
    } catch (error) {
      console.error('Unexpected error:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAllData(selectedUnit)
  }, [AOP_YEAR, keycloak, PLANT_ID, selectedUnit])

  useEffect(() => {
    if (lowerVertName == 'cracker') {
      setUnits(['Monthly', '4F', '5F', '4F+D'])
      setSelectedUnit('Monthly')
    } else {
      setUnits(['TPH', 'TPD'])
      setSelectedUnit('TPH')
    }
  }, [AOP_YEAR, keycloak, PLANT_ID, lowerVertName])

  useEffect(() => {
    const timers = [
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, production: true })),
        0,
      ),
      setTimeout(() => setShowGrids((prev) => ({ ...prev, norm: true })), 100),
      setTimeout(
        () => setShowGrids((prev) => ({ ...prev, current: true })),
        200,
      ),
    ]
    return () => timers.forEach(clearTimeout)
  }, [])

  const colsHistorianValues = generateColumns('HistorianValues')
  const colsMcuAndNormGrid = generateColumns('McuAndNormGrid')
  const colsProductionVolumeData = generateColumns('ProductionVolumeData')

  const exportAllGrids = () => {
    const refs = [exportRef1, exportRef2, exportRef3]
    const options = refs.map((ref) => ref.current.workbookOptions())

    if (lowerVertName === 'cracker') {
      // For Cracker, skip 0th sheet and only take from ref2, ref3
      const crackerSheets = [
        options[1].sheets[0], // Expression
        // options[2].sheets[0], // Individual Best Achieved
      ]

      crackerSheets[0].title = '(Norms) Expression'
      // crackerSheets[1].title = '(Norms) (Individual Best Achieved)'

      exportRef1.current.save({ ...options[0], sheets: crackerSheets })
    } else {
      // Normal flow
      options[0].sheets[1] = options[1].sheets[0]
      options[0].sheets[2] = options[2].sheets[0]

      options[0].sheets[0].title = 'Production Volume'
      options[0].sheets[1].title = 'MCU & Norm'
      options[0].sheets[2].title = 'Current Values'

      exportRef1.current.save(options[0])
    }
  }

  const fileName = `${VERTICAL_NAME}-Norms Historian Basis.xlsx`

  const handleUnitChange = (unit) => {
    setLoading(true)
    setSelectedUnit(unit)
  }

  const gridData =
    lowerVertName === 'cracker'
      ? [
          // {
          //   label: 'Norms (Best Achieved-Min CC)',
          //   rows: rowsBestAchieved,
          //   cols: colsBestAchieved,
          // },
          {
            label: 'Norms (Expression)',
            rows: rowsExpressionBased,
            cols: colsExpressionBased,
          },
          // {
          //   label: 'Norms (Individual Best Achieved)',
          //   rows: rowsCurrentYear,
          //   cols: colsCurrentYear,
          // },
        ]
      : [
          {
            label: 'Production Target',
            visible: showGrids.production,
            rows: rowsProductionVolumeData,
            cols: colsProductionVolumeData,
          },
          {
            label: 'MCU & Norm',
            visible: showGrids.norm,
            rows: rowsMcuAndNormGrid,
            cols: colsMcuAndNormGrid,
          },
          {
            label: 'Current Values',
            visible: showGrids.current,
            rows: rowsHistorianValues,
            cols: colsHistorianValues,
          },
        ]

  if (lowerVertName != 'meg') {
    return <NormsHistorianBasisPe />
  } else
    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

        <div style={{ display: 'none' }}>
          {(lowerVertName === 'cracker'
            ? [rowsBestAchieved, rowsExpressionBased, rowsCurrentYear]
            : [
                rowsProductionVolumeData,
                rowsMcuAndNormGrid,
                rowsHistorianValues,
              ]
          ).map((data, i) => (
            <ExcelExport
              key={i}
              data={data}
              ref={[exportRef1, exportRef2, exportRef3][i]}
              fileName={fileName}
            >
              {(lowerVertName === 'cracker'
                ? [colsBestAchieved, colsExpressionBased, colsCurrentYear]
                : [
                    colsProductionVolumeData,
                    colsMcuAndNormGrid,
                    colsHistorianValues,
                  ])[i].map((col) => (
                <ExcelExportColumn
                  key={col.field}
                  field={col.field}
                  title={col.title}
                />
              ))}
            </ExcelExport>
          ))}
        </div>

        <Box display='flex' justifyContent='flex-end' gap={2}>
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

          {lowerVertName !== 'cracker' && (
            <TextField
              select
              value={selectedUnit}
              onChange={(e) => handleUnitChange(e.target.value)}
              className='dropdown-select'
              variant='outlined'
              label={
                lowerVertName === 'cracker' ? 'Select Mode.' : 'Select UOM.'
              }
            >
              <MenuItem value='' disabled>
                {lowerVertName === 'cracker' ? 'Select Mode.' : 'Select UOM.'}
              </MenuItem>

              {units.map((unit) => (
                <MenuItem key={unit} value={unit}>
                  {unit}
                </MenuItem>
              ))}
            </TextField>
          )}
        </Box>

        <Box display='flex' flexDirection='column' gap={2}>
          {gridData.map(
            (section, index) =>
              (section.visible ?? true) && (
                <CustomAccordion key={index} defaultExpanded disableGutters>
                  <CustomAccordionSummary>
                    <Typography className='grid-title'>
                      {section.label}
                    </Typography>
                  </CustomAccordionSummary>
                  <CustomAccordionDetails>
                    <Box sx={{ width: '100%' }}>
                      <KendoDataGrid
                        rows={section.rows}
                        loading={loading}
                        columns={section.cols}
                        permissions={{ isHeight: section?.rows?.length > 15 }}
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

export default NormsHistorianBasis
