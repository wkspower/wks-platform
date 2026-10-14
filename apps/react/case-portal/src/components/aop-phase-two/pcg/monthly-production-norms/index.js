import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import { customValueFormatterPhaseTwo } from '../../common/ValueFormatterPhaseTwo'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { ProductionNormsApiService } from 'services/production-norms-api-service'

const UNIT_OPTIONS = [
  { id: 'MT/Month', displayName: 'MT/Month' },
  { id: 'TPH', displayName: 'TPH' },
]

const monthFields = [
  { key: 'april', monthNum: 4 },
  { key: 'may', monthNum: 5 },
  { key: 'june', monthNum: 6 },
  { key: 'july', monthNum: 7 },
  { key: 'aug', monthNum: 8 },
  { key: 'sep', monthNum: 9 },
  { key: 'oct', monthNum: 10 },
  { key: 'nov', monthNum: 11 },
  { key: 'dec', monthNum: 12 },
  { key: 'jan', monthNum: 1 },
  { key: 'feb', monthNum: 2 },
  { key: 'march', monthNum: 3 },
]

const getMonthDaysMap = (yearStr) => {
  if (!yearStr) {
    return {
      4: 30,
      5: 31,
      6: 30,
      7: 31,
      8: 31,
      9: 30,
      10: 31,
      11: 30,
      12: 31,
      1: 31,
      2: 28,
      3: 31,
    }
  }
  const startYear = parseInt(yearStr.split('-')[0], 10)
  const nextYear = startYear + 1
  const isLeap = new Date(nextYear, 1, 29).getDate() === 29
  return {
    4: 30,
    5: 31,
    6: 30,
    7: 31,
    8: 31,
    9: 30,
    10: 31,
    11: 30,
    12: 31,
    1: 31,
    2: isLeap ? 29 : 28,
    3: 31,
  }
}

const MonthlyProductionNormsPCG = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const { plantObject, year, oldYear, yearChanged } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Month_Wise_Production_Plan',
  )

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [rawRows, setRawRows] = useState([])
  const [selectedUnit, setSelectedUnit] = useState('MT/Month')
  const [calculationObject, setCalculationObject] = useState([])

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const valueFormat = customValueFormatterPhaseTwo(2)
  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])
  const monthDays = useMemo(() => getMonthDaysMap(AOP_YEAR), [AOP_YEAR])

  // ========================|| Data Transformation (Read-Only) ||======================== //
  const transformRows = useCallback(
    (dataList, unit) => {
      const isTPH = unit === 'TPH'
      const formattedData = dataList.map((item, index) => {
        const transformed = {
          ...item,
          id: item.id !== undefined && item.id !== null ? item.id : index,
          idFromApi: item.idFromApi !== undefined ? item.idFromApi : item.id,
          uom: unit || 'MT/Month',
          UOM: unit || 'MT/Month',
          displayName: item.displayName || '',
          Particulars: item.Particulars || item.displayName || '',
          normParametersFKId: (
            item.normParametersFKId ||
            item.materialFKId ||
            ''
          )
            ?.toString()
            ?.toLowerCase(),
          aopRemarks: item.aopRemarks || item.remarks || '',
          remarks: item.aopRemarks || item.remarks || '',
          isEditable: false,
        }

        monthFields.forEach(({ key, monthNum }) => {
          const rawVal = item[key]
          if (rawVal !== undefined && rawVal !== null && rawVal !== '') {
            const num = parseFloat(rawVal)
            if (isTPH) {
              const days = monthDays[monthNum] || 30
              transformed[key] = num ? num / 24 / days : 0
            } else {
              transformed[key] = num
            }
          } else {
            transformed[key] = isTPH ? 0 : '0.00'
          }
        })

        const total = monthFields.reduce(
          (sum, { key }) => sum + (parseFloat(transformed[key]) || 0),
          0,
        )
        transformed.averageTPH = total

        return transformed
      })

      // Add Total Row
      if (formattedData.length > 0) {
        const totalsRow = {
          id: 'total',
          displayName: 'Total',
          Particulars: 'Total',
          uom: unit || 'MT/Month',
          UOM: unit || 'MT/Month',
          isEditable: false,
        }

        monthFields.forEach(({ key }) => {
          totalsRow[key] = formattedData.reduce(
            (sum, row) => sum + (parseFloat(row[key]) || 0),
            0,
          )
        })

        totalsRow.averageTPH = monthFields.reduce(
          (sum, { key }) => sum + (parseFloat(totalsRow[key]) || 0),
          0,
        )

        return [...formattedData, totalsRow]
      }

      return formattedData
    },
    [monthDays],
  )

  // ========================|| Data Fetching ||======================== //
  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      const response = await ProductionNormsApiService.getAOPData(
        keycloak,
        'Production',
        PLANT_ID,
        AOP_YEAR,
      )

      setCalculationObject(response?.data?.aopCalculation || [])
      if (response?.code !== 200 && response?.status !== 200) {
        setRows([])
        setRawRows([])
        setSnackbarData({
          message: 'Error fetching data. Please try again.',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return
      }

      const dataSet = response?.data?.aopDTOList || []
      const raw = dataSet.map((product, index) => ({
        ...product,
        id: index,
        idFromApi: product.id,
        normParametersFKId: product.materialFKId,
        materialFKId: product.materialFKId,
        originalRemark: product.aopRemarks,
        isEditable: false,
        april: product?.april != null ? Number(product.april) : 0,
        may: product?.may != null ? Number(product.may) : 0,
        june: product?.june != null ? Number(product.june) : 0,
        july: product?.july != null ? Number(product.july) : 0,
        aug: product?.aug != null ? Number(product.aug) : 0,
        sep: product?.sep != null ? Number(product.sep) : 0,
        oct: product?.oct != null ? Number(product.oct) : 0,
        nov: product?.nov != null ? Number(product.nov) : 0,
        dec: product?.dec != null ? Number(product.dec) : 0,
        jan: product?.jan != null ? Number(product.jan) : 0,
        feb: product?.feb != null ? Number(product.feb) : 0,
        march: product?.march != null ? Number(product.march) : 0,
        displayName: product.displayName || product.normParameterDisplayName || '',
        Particulars: product.displayName || product.Particulars || product.normParameterDisplayName || '',
        aopRemarks: product.aopRemarks || '',
        remarks: product.aopRemarks || '',
        uom: selectedUnit || 'MT/Month',
        UOM: selectedUnit || 'MT/Month',
      }))

      setRawRows(raw)
      setRows(transformRows(raw, selectedUnit))
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
      setSnackbarData({
        message: 'Error fetching Production AOP data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak, selectedUnit, transformRows])

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  useEffect(() => {
    if (rawRows.length > 0) {
      setRows(transformRows(rawRows, selectedUnit))
    }
  }, [selectedUnit, rawRows, transformRows])

  // ========================|| Calculate Handler ||======================== //
  const handleCalculate = useCallback(async () => {
    setLoading(true)
    try {
      const data = await ProductionNormsApiService.handleCalculate(
        PLANT_ID,
        AOP_YEAR,
        keycloak,
      )
      if (data?.code === 200 || data?.status === 200) {
        await fetchData()
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      console.error('Error calculating Production Plan data:', error)
      setSnackbarData({
        message: 'Error calculating data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak, fetchData])

  // ========================|| Columns Definition (All Read-Only) ||======================== //
  const columns = useMemo(
    () => [
      {
        field: 'displayName',
        title: 'Products',
        widthT: 250,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'uom',
        title: 'UOM',
        widthT: 100,
        minWidth: 90,
        type: 'text',
        editable: false,
      },
      ...monthFields.map(({ key, monthNum }) => ({
        field: key,
        title: headerMap[monthNum] || key.toUpperCase(),
        widthT: 110,
        minWidth: 100,
        type: 'number1',
        editable: false,
        format: valueFormat,
      })),
      {
        field: 'averageTPH',
        title: `Total (${selectedUnit})`,
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'aopRemarks',
        title: 'Remarks',
        widthT: 180,
        minWidth: 150,
        type: 'textarea',
        editable: false,
      },
    ],
    [headerMap, valueFormat, selectedUnit],
  )

  // ========================|| Permissions (Read-Only) ||======================== //
  const permissions = useMemo(
    () => ({
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      saveBtn: false,
      saveWithRemark: false,
      showCalculate: true,
      calculateDisabled:
        !calculationObject || Object.keys(calculationObject).length === 0,
      allAction: true,
      showNote: true,
      showTitleNameBusiness: true,
      showTitle: true,
      titleName: 'Month wise Production plan',
      showDropdown: true,
      showExport: false,
      downloadExcelBtnFromUI: true,
      ExcelName: EXCEL_NAME,
    }),
    [calculationObject, EXCEL_NAME],
  )

  const dropdownConfig = useMemo(
    () => ({
      options: UNIT_OPTIONS,
      label: 'Unit',
      placeholder: 'Select Unit',
      valueKey: 'id',
      labelKey: 'displayName',
    }),
    [],
  )

  return (
    <Box sx={{ pb: 4 }}>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        title={permissions.titleName}
        permissions={permissions}
        handleCalculate={handleCalculate}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedUnit}
        setSelectedDropdownValue={setSelectedUnit}
        handleUnitChange={setSelectedUnit}
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

export default MonthlyProductionNormsPCG
