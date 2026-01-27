import { useEffect, useState, useRef } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import NestedKendoTable from 'components/aop-phase-two/common/NestedKendoTable/index'

const AssetCapacity = () => {
  const keycloak = useSession()
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
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterPhaseTwo()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'assetName',
      title: 'Asset Name',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'plantCode',
      title: 'Plant Code',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Utility Distributed Code',
      widthT: 160,
      minWidth: 160,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Utility Generated Code',
      widthT: 160,
      minWidth: 160,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'fixedMin',
      title: 'Fixed Min',
      widthT: 100,
      minWidth: 100,
      editable: false,
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'fixedMax',
      title: 'Fixed Max',
      widthT: 100,
      minWidth: 100,
      editable: false,
      type: 'number1',
      format: valueFormat,
    },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'april.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'may.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'june.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'july.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'aug.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'sep.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'oct.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'nov.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'dec.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'jan.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'feb.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.min',
          title: 'Min Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
        {
          field: 'march.max',
          title: 'Max Capacity',
          widthT: 120,
          minWidth: 120,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minValue: 'fixedMin',
          maxValue: 'fixedMax',
        },
      ],
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (AOP_YEAR) {
      fetchAssetCapacityData(keycloak, AOP_YEAR)
    }
  }, [AOP_YEAR])

  const fetchAssetCapacityData = async (keycloak, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await InputApiService.getAssetCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.map((item, index) => {
        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
        }
      })
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching asset capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const debounceTimerRef = useRef(null)

  const customItemChange = (e, setRows) => {
    const { dataItem, field, value } = e

    // Clear previous debounce timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }

    // Only validate min vs max for monthly fields
    // Check if field ends with '.min' or '.max'
    const isMonthlyField = field.includes('.')
    const fieldParts = field.split('.')
    const fieldType = fieldParts[fieldParts.length - 1] // 'min' or 'max'
    const monthName = fieldParts[0] // 'april', 'may', etc.

    if (!isMonthlyField || (fieldType !== 'min' && fieldType !== 'max')) {
      return true
    }

    const numValue = parseFloat(value)

    // Skip validation if value is empty or NaN
    if (value === '' || isNaN(numValue)) {
      return true
    }

    // Get the corresponding min or max field
    const correspondingValue =
      dataItem[monthName]?.[fieldType === 'min' ? 'max' : 'min']

    // Validate: min should not be greater than max
    if (
      fieldType === 'min' &&
      correspondingValue !== undefined &&
      numValue > correspondingValue
    ) {
      const monthCapitalized =
        monthName.charAt(0).toUpperCase() + monthName.slice(1)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `${monthCapitalized} min capacity cannot be greater than ${monthCapitalized} max capacity`,
        severity: 'error',
      })
      return false
    }

    // Validate: max should not be less than min
    if (
      fieldType === 'max' &&
      correspondingValue !== undefined &&
      numValue < correspondingValue
    ) {
      const monthCapitalized =
        monthName.charAt(0).toUpperCase() + monthName.slice(1)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `${monthCapitalized} max capacity cannot be less than ${monthCapitalized} min capacity`,
        severity: 'error',
      })
      return false
    }

    return true
  }

  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    showExport: true,
    ExcelName: `Asset Capacity - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells', modifiedCells)
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
      'april.min',
      'april.max',
      'may.min',
      'may.max',
      'june.min',
      'june.max',
      'july.min',
      'july.max',
      'aug.min',
      'aug.max',
      'sep.min',
      'sep.max',
      'oct.min',
      'oct.max',
      'nov.min',
      'nov.max',
      'dec.min',
      'dec.max',
      'jan.min',
      'jan.max',
      'feb.min',
      'feb.max',
      'march.min',
      'march.max',
      'fixedMin',
      'fixedMax',
    ]
    const validationError = validateNestedRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'assetName',
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

    try {
      console.log('payload', payload)

      const response = await InputApiService.saveAssetCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving asset capacity data:', error)
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
      const response = await InputApiService.saveAssetCapacityExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchAssetCapacityData(keycloak, AOP_YEAR)
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `Error File - Asset Capacity.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchAssetCapacityData(keycloak, AOP_YEAR)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
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
      await InputApiService.exportAssetCapacityExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Asset Capacity data:', error)
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

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <NestedKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Asset Capacity Input'
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
        customItemChange={customItemChange}
      />
    </Box>
  )
}

export default AssetCapacity
