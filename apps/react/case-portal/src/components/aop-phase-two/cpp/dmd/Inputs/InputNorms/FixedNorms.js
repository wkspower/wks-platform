import { useEffect, useState, useMemo, useCallback } from 'react'
import { Box } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo, {
  customValueFormatterPhaseTwo,
} from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import NestedKendoTable from 'components/aop-phase-two/common/NestedKendoTable/index'
import { InputNormsApiService } from 'components/aop-phase-two/services/cpp/jmd/inputNormsApiService'
import useConfigurationDates from 'components/aop-phase-two/common/hooks/useConfigurationDates'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { useDebounce } from 'hooks/useDebounce'

const FixedNorms = () => {
  const keycloak = useSession()
  const {
    startDate,
    endDate,
    dateLoading,
    error: configError,
  } = useConfigurationDates()

  // State management
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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
    jmdSelectedPlants,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear

  const PLANT_ID_LIST = plantObject?.id
  // useMemo(
  //   () => jmdSelectedPlants?.map((plant) => plant.id) || [],
  //   [jmdSelectedPlants],
  // )
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = customValueFormatterPhaseTwo(6)
  const customFormatFive = customValueFormatterPhaseTwo(5)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const nestedColumns = [
    //Generating Plant
    {
      field: 'generatingPlantName',
      title: 'Generating Plant',
      widthT: 180,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 180,
    },
    //Utility
    {
      field: 'utilityName',
      title: 'Utility',
      widthT: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 120,
    },
    // Utility ID
    {
      field: 'utilityId',
      title: 'Utility ID',
      widthT: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 120,
    },
    //UOM
    {
      field: 'uom',
      title: 'Generation UOM',
      widthT: 180,
      type: 'text',
      editable: false,
      minWidth: 180,
    },
    // Account
    {
      field: 'accountName',
      title: 'Account',
      widthT: 150,
      type: 'text',
      editable: false,
      minWidth: 150,
    },
    // Material
    {
      field: 'materialName',
      title: 'Material',
      widthT: 130,
      type: 'text',
      editable: false,
      minWidth: 130,
    },
    // SAP Code
    {
      field: 'materialId',
      title: 'SAP Code',
      widthT: 130,
      type: 'text',
      editable: false,
      minWidth: 130,
    },
    // Issuing Plant
    {
      field: 'issuingPlantName',
      title: 'Issuing Plant',
      widthT: 150,
      type: 'text',
      editable: false,
      minWidth: 150,
    },
    {
      field: 'issuingUom',
      title: 'Issuing UOM',
      widthT: 150,
      type: 'text',
      editable: false,
      minWidth: 150,
    },
    {
      field: 'normTypeName',
      title: 'Norm Type',
      widthT: 150,
      type: 'text',
      editable: false,
      minWidth: 150,
    },
    {
      field: 'actualNorm',
      title: 'Proposed Norm',
      widthT: 200,
      type: 'proposedNorm',
      editable: true,
      isNumberEditable: false,
      format: customFormatFive,
      minWidth: 200,
      alwaysEditable: true,
    },
    // Apr
    {
      field: 'aprNorms',
      title: headerMap[4],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // May
    {
      field: 'mayNorms',
      title: headerMap[5],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Jun
    {
      field: 'junNorms',
      title: headerMap[6],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Jul
    {
      field: 'julNorms',
      title: headerMap[7],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Aug
    {
      field: 'augNorms',
      title: headerMap[8],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Sep
    {
      field: 'sepNorms',
      title: headerMap[9],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Oct
    {
      field: 'octNorms',
      title: headerMap[10],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Nov
    {
      field: 'novNorms',
      title: headerMap[11],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Dec
    {
      field: 'decNorms',
      title: headerMap[12],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Jan
    {
      field: 'janNorms',
      title: headerMap[1],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Feb
    {
      field: 'febNorms',
      title: headerMap[2],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Mar
    {
      field: 'marNorms',
      title: headerMap[3],
      widthT: 100,
      minWidth: 100,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
      alwaysEditable: true,
    },
  ]

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [fetchLoading, setFetchLoading] = useState(false)
  const [calculationLoading, setCaculationLoading] = useState(false)
  const [exportLoading, setExportLoading] = useState(false)
  const [calculateBtnEnabled, setCalculateBtnEnabled] = useState(false)
  const [aopCalaculation, setAopCalaculation] = useState([])

  const formatDate = (date) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // Show error notification if configuration is not set up
  useEffect(() => {
    if (configError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: configError,
        severity: 'warning',
      })
    }
  }, [configError])

  const fetchNormsData = useCallback(
    async (startDate, endDate) => {
      setFetchLoading(true)
      try {
        const res = await InputNormsApiService.getNormsData(
          keycloak,
          PLANT_ID_LIST,
          AOP_YEAR,
          startDate,
          endDate,
        )

        const normsList = res?.data?.list || []
        const aopCalcList = res?.data?.aopCalculationList || []

        // Enable calculate button if any plant has aopCalculation dirty-flag records
        const hasAopCalculation = aopCalcList.some(
          (item) => item?.aopCalculation?.length > 0,
        )
        setCalculateBtnEnabled(hasAopCalculation)
        setAopCalaculation(aopCalcList)

        if (normsList.length === 0) {
          setRows([])
          setCalculateBtnEnabled(false)
          setAopCalaculation([])
          setSnackbarOpen(true)
          setSnackbarData({ message: 'No data found', severity: 'info' })
          return
        }
        let tempRes = normsList
          ?.filter((item) => item.issuingUom !== 'MMBTU')
          ?.map((item, index) => {
            const actualNormValue =
              item.actualNorm !== null && item.actualNorm !== undefined
                ? item.actualNorm
                : 0
            const applyToAll = item.applyActualNormToAll || false

            const isModelCalculated = item.normTypeName === 'Model Calculated'

            // If applyActualNormToAll is true, populate all 12 months with actualNorm value
            if (
              applyToAll &&
              actualNormValue !== null &&
              actualNormValue !== undefined
            ) {
              return {
                ...item,
                id: item.id || index + 1,
                remarks: item.remarks || '',
                actualNorm: actualNormValue,
                applyActualNormToAll: applyToAll,
                isEditable: false, // Row not editable when checkbox is checked
                isModelCalculated, // Flag to completely lock row for 'Model Calculated'
                aprNorms: actualNormValue,
                mayNorms: actualNormValue,
                junNorms: actualNormValue,
                julNorms: actualNormValue,
                augNorms: actualNormValue,
                sepNorms: actualNormValue,
                octNorms: actualNormValue,
                novNorms: actualNormValue,
                decNorms: actualNormValue,
                janNorms: actualNormValue,
                febNorms: actualNormValue,
                marNorms: actualNormValue,
              }
            }

            // Otherwise, keep the original month values
            return {
              ...item,
              id: item.id || index + 1,
              remarks: item.remarks || '',
              actualNorm: actualNormValue,
              applyActualNormToAll: applyToAll,
              isEditable: !isModelCalculated, // Row not editable when norm type is 'Model Calculated'
              isModelCalculated, // Flag to completely lock row for 'Model Calculated'
            }
          })

        setRows(tempRes)
        setOriginalRows(tempRes)
      } catch (error) {
        console.error('Error fetching fixed consumption data:', error)
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Error fetching data', severity: 'error' })
      } finally {
        setFetchLoading(false)
      }
    },
    [keycloak, PLANT_ID_LIST, AOP_YEAR],
  )

  useDebounce(
    () => {
      if (PLANT_ID_LIST?.length && AOP_YEAR && startDate && endDate) {
        const formattedStartDate = formatDate(startDate)
        const formattedEndDate = formatDate(endDate)
        fetchNormsData(formattedStartDate, formattedEndDate)
        setModifiedCells({})
      }
    },
    1000,
    [PLANT_ID_LIST, AOP_YEAR, startDate, endDate, fetchNormsData],
  )

  // Permissions (adjust as needed)
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showImport: true,
    showTitle: true,
    showExport: true,
    showCalculate: true,
    enableCalculate: calculateBtnEnabled,
    ExcelName: `Norms - ${AOP_YEAR}`,
    note: aopCalaculation?.some((item) => item?.aopCalculation?.length > 0)
      ? ' Please calculate again there are some inputs updated.'
      : '',
  }

  // Calculate Norms data via API
  const handleCalculate = async () => {
    setCaculationLoading(true)
    try {
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)
      const res = await InputNormsApiService.calculateNormsData(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        formattedStartDate,
        formattedEndDate,
      )

      if (res) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })
        // Refresh the data after calculation
        const formattedStartDate = formatDate(startDate)
        const formattedEndDate = formatDate(endDate)
        await fetchNormsData(formattedStartDate, formattedEndDate)
      }
    } catch (error) {
      console.error('Error calculating norms data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error during calculation. Please try again.',
        severity: 'error',
      })
    } finally {
      setCaculationLoading(false)
    }
  }

  // Save handler with API call
  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    var rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = [
      'aprNorms',
      'mayNorms',
      'junNorms',
      'julNorms',
      'augNorms',
      'sepNorms',
      'octNorms',
      'novNorms',
      'decNorms',
      'janNorms',
      'febNorms',
      'marNorms',
    ]
    const validationError = validateNestedRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'generatingPlantName',
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

    const payload = modifiedData
    const tempPayload = payload?.map((item) => {
      const { id, inEdit, ...rest } = item
      return {
        ...rest,
      }
    })

    try {
      // Transform modifiedCells into the format expected by the API

      console.log('payload', tempPayload)

      const response = await InputNormsApiService.saveNormsData(
        keycloak,
        tempPayload,
        AOP_YEAR,
      )

      // Update the local state with the saved data
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })

      // Refresh data after save to reflect server-calculated values (e.g. actualNorm)
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)
      await fetchNormsData(formattedStartDate, formattedEndDate)
    } catch (error) {
      console.error('Error saving plant requirement data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await InputNormsApiService.importNormsData(
        keycloak,
        file,
        PLANT_ID_LIST,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        // Refresh data after import
        const formattedStartDate = formatDate(startDate)
        const formattedEndDate = formatDate(endDate)
        await fetchNormsData(formattedStartDate, formattedEndDate)
      } else if (response?.code === 400 && response?.data) {
        // Handle error response with Excel file download
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Norms_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          // Refresh data after import
          const formattedStartDate = formatDate(startDate)
          const formattedEndDate = formatDate(endDate)
          await fetchNormsData(formattedStartDate, formattedEndDate)
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Failed to import Excel file.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setExportLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)

      await InputNormsApiService.exportNormsData(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        formattedStartDate,
        formattedEndDate,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Norms data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setExportLoading(false)
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    if (row.isModelCalculated) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Custom item change handler for actualNorm checkbox logic
  const handleCustomItemChange = (e, setRows) => {
    const { dataItem, field, value } = e
    const itemId = dataItem.id

    // Block all edits to Proposed Norm for 'Model Calculated' norm type
    if (
      dataItem.isModelCalculated &&
      (field === 'applyActualNormToAll' || field === 'actualNorm')
    ) {
      return false
    }

    // When checkbox is toggled
    if (field === 'applyActualNormToAll') {
      const actualNormValue = dataItem.actualNorm

      if (value && actualNormValue !== null && actualNormValue !== undefined) {
        // Checkbox is checked - copy actualNorm to all 12 months and disable row
        const updatedRow = {
          ...dataItem,
          applyActualNormToAll: value,
          isEditable: false, // Disable row when checkbox is checked
          aprNorms: actualNormValue,
          mayNorms: actualNormValue,
          junNorms: actualNormValue,
          julNorms: actualNormValue,
          augNorms: actualNormValue,
          sepNorms: actualNormValue,
          octNorms: actualNormValue,
          novNorms: actualNormValue,
          decNorms: actualNormValue,
          janNorms: actualNormValue,
          febNorms: actualNormValue,
          marNorms: actualNormValue,
        }

        setRows((prev) => prev.map((r) => (r.id === itemId ? updatedRow : r)))

        setModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || dataItem),
            ...updatedRow,
            inEdit: true,
          },
        }))
        return false
      } else {
        // Checkbox is unchecked - restore entire original row
        const originalRow = originalRows.find((r) => r.id === itemId)
        const isModelCalculated = originalRow?.isModelCalculated || false

        const updatedRow = {
          ...originalRow,
          applyActualNormToAll: value,
          isEditable: !isModelCalculated, // Don't enable row if norm type is 'Model Calculated'
        }

        setRows((prev) => prev.map((r) => (r.id === itemId ? updatedRow : r)))

        setModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || dataItem),
            ...updatedRow,
            inEdit: true,
          },
        }))
        return false
      }
    }

    // When actualNorm value changes and checkbox is checked
    if (field === 'actualNorm') {
      const isChecked = dataItem.applyActualNormToAll

      if (isChecked) {
        // Update actualNorm and all 12 months
        const updatedRow = {
          ...dataItem,
          actualNorm: value,
          aprNorms: value,
          mayNorms: value,
          junNorms: value,
          julNorms: value,
          augNorms: value,
          sepNorms: value,
          octNorms: value,
          novNorms: value,
          decNorms: value,
          janNorms: value,
          febNorms: value,
          marNorms: value,
        }

        setRows((prev) => prev.map((r) => (r.id === itemId ? updatedRow : r)))

        setModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || dataItem),
            ...updatedRow,
            inEdit: true,
          },
        }))
        return false
      }
    }
  }

  return (
    <Box>
      <LoaderBackdrop
        open={fetchLoading || calculationLoading || exportLoading || !!loading}
        showMessage={calculationLoading}
        message='Your data is being processed. This may take a few moments—thank you for your patience.'
      />

      <NestedKendoTable
        columns={nestedColumns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Norms'
        permissions={permissions}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customHeight={70}
        groupBy={['generatingPlantName']}
        customItemChange={handleCustomItemChange}
      />
    </Box>
  )
}

export default FixedNorms
