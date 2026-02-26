import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import NestedKendoTable from 'components/aop-phase-two/common/NestedKendoTable/index'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import DateRangeSelectorWithHistory from 'components/aop-phase-two/common/utilities/DateRangeSelectorWithHistory'

const FixedNorms = () => {
  const keycloak = useSession()
  // State management

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [dateLoading, setDateLoading] = useState(false)
  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
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
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterPhaseTwo()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const nestedColumns = [
    //Generating Plant
    {
      field: 'generatingPlantName',
      title: 'Generating Plant',
      widthT: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 150,
    },
    //Utility
    {
      field: 'utilityName',
      title: 'Utility',
      widthT: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    // Utility ID
    {
      field: 'utilityId',
      title: 'Utility ID',
      widthT: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //UOM
    {
      field: 'uom',
      title: 'Generation UOM',
      widthT: 130,
      type: 'text',
      editable: false,
      minWidth: 130,
    },
    // Account
    {
      field: 'accountName',
      title: 'Account',
      widthT: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Material
    {
      field: 'materialName',
      title: 'Material',
      widthT: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // SAP Code
    {
      field: 'materialId',
      title: 'SAP Code',
      widthT: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Issuing Plant
    {
      field: 'issuingPlantName',
      title: 'Issuing Plant',
      widthT: 120,
      type: 'text',
      editable: false,
      minWidth: 120,
    },
    {
      field: 'issuingUom',
      title: 'Issuing UOM',
      widthT: 120,
      type: 'text',
      editable: false,
      minWidth: 120,
    },
    {
      field: 'actualNorm',
      title: 'Actual Norm',
      widthT: 120,
      type: 'numberWithCheckbox',
      editable: true,
      isNumberEditable: false,
      format: valueFormat,
      minWidth: 120,
      alwaysEditable: true,
    },
    // Apr
    {
      field: 'aprNorms',
      title: headerMap[4],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // May
    {
      field: 'mayNorms',
      title: headerMap[5],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Jun
    {
      field: 'junNorms',
      title: headerMap[6],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Jul
    {
      field: 'julNorms',
      title: headerMap[7],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Aug
    {
      field: 'augNorms',
      title: headerMap[8],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Sep
    {
      field: 'sepNorms',
      title: headerMap[9],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Oct
    {
      field: 'octNorms',
      title: headerMap[10],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Nov
    {
      field: 'novNorms',
      title: headerMap[11],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    // Dec
    {
      field: 'decNorms',
      title: headerMap[12],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Jan
    {
      field: 'janNorms',
      title: headerMap[1],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Feb
    {
      field: 'febNorms',
      title: headerMap[2],
      widthT: 80,
      editable: true,
      type: 'number1',
      format: valueFormat,
    },
    //Mar
    {
      field: 'marNorms',
      title: headerMap[3],
      widthT: 80,
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
  const [calculationLoading, setCaculationLoading] = useState(false)

  const formatDate = (date) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && startDate && endDate) {
      setDateLoading(true)
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)
      fetchNormsData(formattedStartDate, formattedEndDate)
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR, startDate, endDate])

  const fetchNormsData = async (startDate, endDate) => {
    setLoading(true)
    try {
      const res = await InputApiService.getNormBasedUtilityBudget(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        startDate,
        endDate,
      )

      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.data?.map((item, index) => {
        const actualNormValue =
          item.actualNorm !== null && item.actualNorm !== undefined
            ? item.actualNorm
            : 0
        const applyToAll = item.applyActualNormToAll || false

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
          isEditable: true, // Row editable when checkbox unchecked
        }
      })

      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
      setDateLoading(false)
    }
  }

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
    ExcelName: `Norms - ${AOP_YEAR}`,
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

      const response = await InputApiService.saveNormsData(
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
      const response = await InputApiService.saveCPPNormsExcel(
        file,
        keycloak,
        PLANT_ID,
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
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)

      await InputApiService.exportCPPNormsExcel(
        keycloak,
        PLANT_ID,
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
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Custom item change handler for actualNorm checkbox logic
  const handleCustomItemChange = (e, setRows) => {
    const { dataItem, field, value } = e
    const itemId = dataItem.id

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

        const updatedRow = {
          ...originalRow,
          applyActualNormToAll: value,
          isEditable: true, // Enable row when checkbox is unchecked
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
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading || !!dateLoading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack sx={{ mt: 2, mb: 2 }}>
        <DateRangeSelectorWithHistory
          onDateChange={({ startDate, endDate }) => {
            console.log('Dates changed:', startDate, endDate)
            setStartDate(startDate)
            setEndDate(endDate)
          }}
          disabled={false}
          timeRequired={false}
          showLastRefreshed={true}
          dateLoading={dateLoading}
          setDateLoading={setDateLoading}
        />
      </Stack>
      <NestedKendoTable
        columns={nestedColumns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Norms'
        permissions={permissions}
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
        groupBy={['generatingPlantName', 'accountName']}
        customItemChange={handleCustomItemChange}
      />
    </Box>
  )
}

export default FixedNorms
