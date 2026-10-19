import React, { useState, useEffect, useMemo, useCallback } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo, {
  customValueFormatterPhaseTwo,
} from '../../common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from '../../common/commonUtilityFunctions'
import { SteadyStateConsumptionApiService } from 'components/aop-phase-two/services/common/steadyStateConsumptionApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import {
  downloadBase64Excel,
  downloadBlobExcel,
} from 'components/aop-phase-two/common/utilities/downloadBase64Excel'

const MONTH_FIELDS = [
  'april',
  'may',
  'june',
  'july',
  'august',
  'september',
  'october',
  'november',
  'december',
  'january',
  'february',
  'march',
]

const CoolingTowerUom=['Kg/Km3', 'kWh/km3', 'm3/km3', 'Nm3/Km3']

const normalizeKey = (s) =>
  String(s ?? '')
    .replace(/\s+/g, '')
    .toLowerCase()

// Norm parameter types that show 3 decimals for DTA_PCG CT plants
const THREE_DECIMAL_NORM_TYPES = [
'Power Norms',
'Norms proposed for Next FY-Total Power',
].map(normalizeKey)
const SIX_DECIMAL_NORM_TYPES=[
'Norms proposed for Next FY-Cooling Water',
'Cooling Water Norms',
].map(normalizeKey)


const SteadyStateConsumption = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, siteObject } = dataGridStore
  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Steady_State_Consumption',
  )

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const siteName = (siteObject?.name || '').trim().toUpperCase()
  const plantName = (plantObject?.name || '').trim()
  const isDtapcgCtPlant =
    siteName.toLowerCase() === 'dta_pcg'  &&
    (plantName.toLowerCase() === 'ct1 (g731)' ||
      plantName.toLowerCase() === 'ct3 (g733)' ||
     plantName.toLowerCase() === 'ct2 (g733)')

  const isSezpcgCtPlant=siteName.toLowerCase() === 'sez_pcg' && (plantName.toLowerCase() === 'ct1 (h731)' ||
      plantName.toLowerCase() === 'ct3 (h733)' || plantName.toLowerCase() === 'ct2 (h732)' ||plantName.toLowerCase() === 'ct4 (h734)')
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

  const isDtaCtPlant=siteName === 'DTA' &&
      (
  plantName.toLowerCase() === 'ct1 (731)' ||
  plantName.toLowerCase() === 'ct2 (732)' ||
  plantName.toLowerCase() === 'ct3 (733)' ||
  plantName.toLowerCase() === 'ct4 (734)' ||
  plantName.toLowerCase() === 'ct5 (735)' ||
  plantName.toLowerCase() === 'ct6 (736)' ||
  plantName.toLowerCase() === 'ct7 (737)' ||
  plantName.toLowerCase() === 'ct8 (738)' ||
  plantName.toLowerCase() === 'ct9 (739)' ||
  plantName.toLowerCase() === 'ct10 (740)' 
)

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
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

      if (isDtapcgCtPlant || isSezpcgCtPlant || isDtaCtPlant) {
        // Decide by normParameterTypeName (or display name), not by UOM
        const cleanUom = String(uom ?? '')
          .trim()
          .toLowerCase()
        const isThreeDecimal = CoolingTowerUom.some(
          (u) => u.trim().toLowerCase() === cleanUom,
        )
        if (isThreeDecimal) {
          return (Math.trunc(num * 1000) / 1000).toFixed(3)
        }
        return Math.trunc(num).toString()
      }
      if (isFCCTame) {
        return (Math.trunc(num * 100000) / 100000).toFixed(5)
      }
      if (isAsuPlant) {
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
        const isSixDecimal = typeKeys.some((k) =>
          SIX_DECIMAL_NORM_TYPES.includes(k),
        )
        if (isSixDecimal) {
          return (Math.trunc(num * 1000000) / 1000000).toFixed(6)
        }
        return (Math.trunc(num * 100000) / 100000).toFixed(5)
      }

      return Math.trunc(num).toString()
    },
    [isDtaCtPlant, isDtapcgCtPlant, isSezpcgCtPlant, isAsuPlant, isFCCTame],
  )

  const columns = useMemo(() => {
    const valueFormat = undefined
    const headerMap = generateHeaderNames(AOP_YEAR)

    return [
      {
        field: 'id',
        title: 'Id',
        widthT: 250,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
        hidden: true,
      },
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
        editable: true,
        format: valueFormat,
      },
      {
        field: 'may',
        title: headerMap[5],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'june',
        title: headerMap[6],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'july',
        title: headerMap[7],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'august',
        title: headerMap[8],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'september',
        title: headerMap[9],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'october',
        title: headerMap[10],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'november',
        title: headerMap[11],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'december',
        title: headerMap[12],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'january',
        title: headerMap[1],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'february',
        title: headerMap[2],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'march',
        title: headerMap[3],
        widthT: 120,
        minWidth: 120,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'remarks',
        title: 'Remark',
        widthT: 150,
        minWidth: 120,
        type: 'textarea',
        editable: true,
      },
    ]
  }, [isDtaCtPlant, isDtapcgCtPlant,isSezpcgCtPlant, isAsuPlant, isFCCTame, AOP_YEAR])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, isDtaCtPlant, isDtapcgCtPlant,isSezpcgCtPlant, isAsuPlant, isFCCTame])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response =
        await SteadyStateConsumptionApiService.getSteadyStateConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const data = response?.data?.mcuNormsValueDTOList || []
      const formattedData = data?.map((item, index) => {
        const row = {
          ...item,
          remarks: item.remarks || '',
          id: item?.id || index + 1,
          isEditable: true,
        }
        const uom = item?.UOM || item?.uom || ''
        if (isDtaCtPlant) {
          MONTH_FIELDS.forEach((m) => {
            if (row[m] !== undefined && row[m] !== null && row[m] !== '') {
              row[m] = formatValueByUom(row[m], uom, row)
            }
          })
        } else {
          // Apart from isDtaCtPlant: default formatting
          MONTH_FIELDS.forEach((m) => {
            if (row[m] !== undefined && row[m] !== null && row[m] !== '') {
              row[m] = formatValueByUom(row[m], uom, row)
            }
          })
        }
        return row
      })
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching steady state consumption data:', error)
      setRows([])
      setOriginalRows([])
    } finally {
      setLoading(false)
    }
  }

  const saveChanges = async () => {
    setLoading(true)

    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = modifiedData
      .filter((row) => row.inEdit)
      .map((row) => {
        const uom = row?.UOM || row?.uom || ''
        const updatedRow = { ...row }
        if (isDtaCtPlant) {
          MONTH_FIELDS.forEach((m) => {
            if (
              updatedRow[m] !== undefined &&
              updatedRow[m] !== null &&
              updatedRow[m] !== ''
            ) {
              updatedRow[m] = formatValueByUom(updatedRow[m], uom, updatedRow)
            }
          })
        } else {
          // Apart from isDtaCtPlant: default formatting
          MONTH_FIELDS.forEach((m) => {
            if (
              updatedRow[m] !== undefined &&
              updatedRow[m] !== null &&
              updatedRow[m] !== ''
            ) {
              updatedRow[m] = formatValueByUom(updatedRow[m], uom, updatedRow)
            }
          })
        }
        return updatedRow
      })
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      MONTH_FIELDS,
      'productName',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    try {
      await SteadyStateConsumptionApiService.saveSteadyStateConsumption(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        data,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      setOriginalRows([])
      setRows([])
      await fetchData()
    } catch (error) {
      console.error('Error saving steady state consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
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
      const response =
        await SteadyStateConsumptionApiService.calculateSteadyStateConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code === 422) {
        setTimeout(() => {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response.message || 'Validation error occurred.',
            severity: 'error',
            autoHide: false,
          })
        }, 500)
      } else if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error calculating steady state consumption:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Calculation failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel export started!',
      severity: 'info',
    })

    try {
      const blob =
        await SteadyStateConsumptionApiService.exportSteadyStateConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      downloadBlobExcel(blob, EXCEL_NAME)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting steady state consumption data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleImport = async (file) => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Importing data...',
      severity: 'info',
    })

    try {
      const response =
        await SteadyStateConsumptionApiService.importSteadyStateConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          file,
        )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        // Partial save — download error file
        downloadBase64Excel(
          response.data,
          'Error File Steady State Consumption.xlsx',
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Import failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing steady state consumption data:', error)
      setSnackbarData({
        message: 'Import failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: true,
    showImport: true,
    showCalculate: true,
    ExcelName: `Steady_State_Consumption_${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Steady State Consumption (Norm/Quantity)',
    showDropdown: false,
    remarksEditable: true,
    showCalulcationPromt: true,
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
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExport={handleExport}
        handleExcelUpload={handleImport}
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

export default SteadyStateConsumption