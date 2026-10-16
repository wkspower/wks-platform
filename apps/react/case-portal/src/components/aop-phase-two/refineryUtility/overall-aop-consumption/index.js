import React, { useState, useEffect, useMemo, useCallback } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import { customValueFormatterPhaseTwo } from '../../common/ValueFormatterPhaseTwo'
import { OverallAopConsumptionApiService } from 'components/aop-phase-two/services/common/overallAopConsumptionApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'

const THREE_DECIMAL_UOMS = ['kw', 'kw/m3', 'kg/km3']

// Lowercase + strip all whitespace so "Power Consumption" === "PowerConsumption"
const normalizeKey = (s) =>
  String(s ?? '')
    .replace(/\s+/g, '')
    .toLowerCase()

// Norm parameter types that show 3 decimals for DTA_PCG CT plants
const THREE_DECIMAL_NORM_TYPES = [
  'Specific Other Utilities consumption',
  'Specific Makeup Consumption',
  'Power Consumption',
  'Chemical Consumption',
  'Other Utilities consumption',
].map(normalizeKey)

const OverallAopConsumption = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, siteObject } = dataGridStore
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Overall_AOP_Consumption')
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const siteName = (siteObject?.name || '').trim().toUpperCase()
  const plantName = (plantObject?.name || '').trim()
  const isDtaCtPlant =
    siteName === 'DTA' &&
    (plantName === 'CT4 (734)' || plantName === 'CT6 (736)')
  const isDtapcgCtPlant =
    siteName.toLowerCase() === 'dta_pcg'&&
    (plantName.toLowerCase() === 'ct1 (g731)' ||
      plantName.toLowerCase() === 'ct3 (g733)')

  const isSezpcgCtPlant=siteName.toLowerCase() === 'sez_pcg' && (plantName.toLowerCase() === 'ct1 (h731)' ||
      plantName.toLowerCase() === 'ct3 (h733)' || plantName.toLowerCase() === 'ct2 (h732)')
  const isFCCTame = siteName === 'DTA' && plantName === 'FCC-2_SHP-TAME'
  const isAsuPlant =
    (siteName === 'DTA' &&
      (plantName.toLowerCase() === 'air & asu' ||
        plantName.toLowerCase() === 'pcg asu')) ||
    (siteName === 'SEZ' &&
      (plantName.toLowerCase() === 'air & asu' ||
        plantName.toLowerCase() === 'pcg asu')) ||
    (siteName === 'C2' &&
      (plantName.toLowerCase() === 'air' ||
        plantName.toLowerCase() === 'c2_asu'))

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const formatValueByUom = useCallback(
    (val, uom, row = {}) => {
      if (val === null || val === undefined || val === '') return ''
      const num = parseFloat(val)
      if (isNaN(num)) return val

      if (isDtaCtPlant) {
        const cleanUom = String(uom ?? '')
          .trim()
          .toLowerCase()
        const isThreeDecimal = THREE_DECIMAL_UOMS.some(
          (u) => u.trim().toLowerCase() === cleanUom,
        )
        if (isThreeDecimal) {
          return (Math.trunc(num * 1000) / 1000).toFixed(3)
        }
        return Math.trunc(num).toString()
      }

      if (isDtapcgCtPlant || isSezpcgCtPlant) {
        // Decide by normParameterTypeName (or display name), not by UOM
        const typeKeys = [
          normalizeKey(row?.normParameterTypeName),
          normalizeKey(row?.normParameterTypeDisplayName),
        ]
        const isThreeDecimal = typeKeys.some((k) =>
          THREE_DECIMAL_NORM_TYPES.includes(k),
        )
        if (isThreeDecimal) {
          return (Math.trunc(num * 1000) / 1000).toFixed(3)
        }
        return Math.trunc(num).toString()
      }

      if (isAsuPlant || isFCCTame) {
        return (Math.trunc(num * 100000) / 100000).toFixed(5)
      }

      return Math.trunc(num).toString()
    },
    [isDtaCtPlant, isAsuPlant, isFCCTame, isDtapcgCtPlant, isSezpcgCtPlant],
  )

  const columns = useMemo(() => {
    const valueFormat = undefined
    const headerMap = generateHeaderNames(AOP_YEAR)

    return [
      {
        field: 'sapCode',
        title: 'SAP MAT Code',
        widthT: 250,
        minWidth: 150,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'productName',
        title: 'Particulars',
        widthT: 250,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'normParameterTypeDisplayName',
        title: 'Type',
        widthT: 250,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
        hidden: true,
      },
      {
        field: 'UOM',
        title: 'UOM',
        widthT: 120,
        minWidth: 120,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'april',
        title: headerMap[4],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'may',
        title: headerMap[5],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'june',
        title: headerMap[6],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'july',
        title: headerMap[7],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'aug',
        title: headerMap[8],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'sep',
        title: headerMap[9],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'oct',
        title: headerMap[10],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'nov',
        title: headerMap[11],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'dec',
        title: headerMap[12],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'jan',
        title: headerMap[1],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'feb',
        title: headerMap[2],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
      {
        field: 'march',
        title: headerMap[3],
        // widthT: 100,
        minWidth: 120,
        type: 'number1',
        editable: false,
        format: valueFormat,
      },
    ]
  }, [AOP_YEAR, isDtaCtPlant, isAsuPlant, isFCCTame, isDtapcgCtPlant, isSezpcgCtPlant])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, isDtaCtPlant, isAsuPlant, isFCCTame, isDtapcgCtPlant, isSezpcgCtPlant])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response =
        await OverallAopConsumptionApiService.getOverallAopConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const data =
        response?.data?.aopConsumptionNormDTOList?.map((item) => {
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

          const monthValues = monthFields.map((field) => {
            const val = item[field]
            return val !== null && val !== undefined && !isNaN(val)
              ? Number(val)
              : 0
          })

          const sum = monthValues.reduce((acc, val) => acc + val, 0)
          const avgNorms = sum / 12

          const uom = item?.UOM || item?.uom || ''
          const formattedItem = {
            ...item,
            avgNorms,
            isEditable: false,
          }

          monthFields.forEach((field) => {
            if (
              formattedItem[field] !== undefined &&
              formattedItem[field] !== null &&
              formattedItem[field] !== ''
            ) {
              formattedItem[field] = formatValueByUom(
                formattedItem[field],
                uom,
                formattedItem,
              )
            }
          })

          return formattedItem
        }) || []
      setRows(data)
    } catch (error) {
      console.error('Error fetching overall AOP consumption data:', error)
      setRows([])
    } finally {
      setLoading(false)
    }
  }

  const handleCalculate = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Calculating...',
      severity: 'info',
    })

    try {
      const calculatedData =
        await OverallAopConsumptionApiService.calculateOverallAopConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      setSnackbarData({
        message: 'Calculation completed successfully!',
        severity: 'success',
      })
      await fetchData()
    } catch (error) {
      console.error('Error calculating overall AOP consumption:', error)
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
    // showExport: true,
    downloadExcelBtnFromUI: true,
    showCalculate: true,
    ExcelName: EXCEL_NAME,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Overall AOP Consumption (Norm/Quantity)',
    showDropdown: false,
    showCalulcationPromt: true,
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleCalculate={handleCalculate}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['normParameterTypeDisplayName']}
        customHeight={70}
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

export default OverallAopConsumption