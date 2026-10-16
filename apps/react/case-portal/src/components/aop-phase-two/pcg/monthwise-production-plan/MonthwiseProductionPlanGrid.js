import { useState, useEffect, useCallback, useMemo } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import { customValueFormatterPhaseTwo } from '../../common/ValueFormatterPhaseTwo'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import { MonthwiseProductionPlanApiService } from 'components/aop-phase-two/services/common/monthwiseProductionPlanApiService'

// Lowercase month fields — same as ProductionNorms.js (PE vertical)
const monthFields = [
  'april',
  'may',
  'june',
  'july',
  'aug',
  'sep',
  'oct',
  'nov',
  'dec',
  'jan',
  'feb',
  'march',
]

const monthIndexMap = {
  april: 4,
  may: 5,
  june: 6,
  july: 7,
  aug: 8,
  sep: 9,
  oct: 10,
  nov: 11,
  dec: 12,
  jan: 1,
  feb: 2,
  march: 3,
}

// Unit options for the dropdown (same units as ProductionNorms PE vertical)
const UNIT_OPTIONS = [
  { id: 'MT', displayName: 'MT' },
  { id: 'KT', displayName: 'KT' },
]

const MonthwiseProductionPlanGrid = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Monthwise_Production_Plan',
  )

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [rawRows, setRawRows] = useState([]) // always stores MT values from API
  const [selectedUnit, setSelectedUnit] = useState('MT')
  const [calculationObject, setCalculationObject] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const valueFormat = customValueFormatterPhaseTwo(2)
  const headerMap = generateHeaderNames(AOP_YEAR)

  const columns = useMemo(
    () => [
      {
        field: 'displayName',
        title: 'Particulars',
        widthT: 250,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
      },
      ...monthFields.map((field) => ({
        field,
        title: headerMap[monthIndexMap[field]],
        widthT: 110,
        minWidth: 110,
        type: 'number1',
        editable: false,
        format: valueFormat,
      })),
      {
        field: 'averageTPH',
        title: `Total (${selectedUnit})`,
        widthT: 110,
        minWidth: 110,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
    ],
    [selectedUnit, headerMap, valueFormat],
  )

  // Build display rows applying unit conversion from raw MT data
  const buildDisplayRows = useCallback((rawData, unit) => {
    const isKiloTon = unit === 'KT'

    const formattedData = rawData.map((item, index) => {
      const convertedItem = { ...item }
      monthFields.forEach((month) => {
        const val = item[month]
        convertedItem[month] = isKiloTon
          ? val != null && val !== ''
            ? val / 1000
            : val
          : val
      })
      const averageTPH = monthFields.reduce(
        (sum, month) => sum + (parseFloat(convertedItem[month]) || 0),
        0,
      )
      return { ...convertedItem, id: index, averageTPH }
    })

    // Totals row (same as PE vertical in ProductionNorms.js)
    const totalsRow = {
      id: formattedData.length,
      displayName: 'Total',
      isEditable: false,
      ...monthFields.reduce((acc, field) => {
        acc[field] = formattedData.reduce(
          (sum, row) => sum + (parseFloat(row[field]) || 0),
          0,
        )
        return acc
      }, {}),
    }
    totalsRow.averageTPH = monthFields.reduce(
      (sum, field) => sum + (parseFloat(totalsRow[field]) || 0),
      0,
    )

    return [...formattedData, totalsRow]
  }, [])

  // Re-apply unit conversion whenever unit or raw data changes
  useEffect(() => {
    if (rawRows.length > 0) {
      setRows(buildDisplayRows(rawRows, selectedUnit))
    }
  }, [selectedUnit, rawRows, buildDisplayRows])

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setRows([])
    setRawRows([])
    setLoading(true)
    try {
      let response =
        await MonthwiseProductionPlanApiService.getMonthwiseProductionPlan(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code !== 200) {
        setRows([])
        setRawRows([])
        return
      }

      setCalculationObject(response?.data?.aopCalculation || [])
      const dataSet = response?.data?.aopDTOList || []

      // Map raw MT values — same as ProductionNorms.js for PE vertical
      const rawData = dataSet
        .map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          originalRemark: product.remark,
          remark: product.remark,
          isEditable: false,
          Particulars: product.normParameterDisplayName,
        }))
        .map(({ materialFKId, ...rest }) => rest)
        .map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
        }))

      setRawRows(rawData) // triggers useEffect → applies selectedUnit conversion
    } catch (error) {
      console.error('Error fetching monthwise production plan:', error)
      setRows([])
      setRawRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel export started!', severity: 'info' })
    try {
      await MonthwiseProductionPlanApiService.exportMonthwiseProductionPlan(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting monthwise production plan:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
  }

  const handleCalculate = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Calculating...', severity: 'info' })
    try {
      const data =
        await MonthwiseProductionPlanApiService.calculateMonthwiseProductionPlan(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      if (data?.code === 200) {
        await fetchData()
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error calculating monthwise production plan:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Calculation failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    showExport: false,
    downloadExcelBtnFromUI: true,
    ExcelName: EXCEL_NAME,
    showImport: false,
    showCalculate: true,
    calculateDisabled: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Month wise Production Plan',
    showDropdown: true, // unit conversion dropdown
  }

  const dropdownConfig = {
    options: UNIT_OPTIONS,
    label: 'Unit',
    placeholder: 'Select Unit',
    valueKey: 'id',
    labelKey: 'displayName',
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleExport={handleExport}
        handleCalculate={handleCalculate}
        handleUnitChange={setSelectedUnit}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedUnit}
        setSelectedDropdownValue={setSelectedUnit}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
    </Box>
  )
}

export default MonthwiseProductionPlanGrid
