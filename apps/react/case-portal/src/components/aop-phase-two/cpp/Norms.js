import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { UtilityPlantApiServiceV2 } from 'components/aop-phase-two/services/cpp/utilityPlantApiServiceV2'
import NestedKendoTable from '../common/NestedKendoTable/index'
import { Stack, Typography } from '../../../../node_modules/@mui/material/index'

const Norms = () => {
  const keycloak = useSession()
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
    // Apr
    {
      title: headerMap[4],
      children: [
        {
          field: 'apr.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'apr.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'apr.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'apr.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'apr.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'apr.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // May
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'may.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'may.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'may.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'may.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Jun
    {
      title: headerMap[6],
      children: [
        {
          field: 'jun.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jun.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jun.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jun.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jun.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jun.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Jul
    {
      title: headerMap[7],
      children: [
        {
          field: 'jul.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jul.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jul.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jul.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jul.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jul.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Aug
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'aug.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'aug.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.quantity',
          title: 'Quantity',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'aug.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'aug.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Sep
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'sep.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'sep.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'sep.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'sep.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Oct
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'oct.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'oct.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'oct.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'oct.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    // Nov
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'nov.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'nov.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'nov.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'nov.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Dec
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'dec.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'dec.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'dec.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'dec.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Jan
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jan.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'jan.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'jan.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jan.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'jan.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Feb
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'feb.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'feb.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'feb.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'feb.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'feb.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    //Mar
    {
      title: headerMap[3],
      minWidth: 320,
      children: [
        {
          field: 'mar.qty',
          title: 'Qty',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'mar.generationUom',
          title: 'Generation UOM',
          widthT: 80,
          editable: false,
          type: 'number1',
          format: valueFormat,
          hidden: true,
        },
        {
          field: 'mar.norms',
          title: 'Norms',
          widthT: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
          minWidth: 80,
        },
        {
          field: 'mar.quantity',
          title: 'Quantity',
          widthT: 100,
          minWidth: 100,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'mar.amount',
          title: 'Amount',
          widthT: 80,
          minWidth: 80,
          type: 'number',
          format: valueFormat,
        },
        {
          field: 'mar.price',
          title: 'Price',
          widthT: 80,
          minWidth: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
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

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [calculationLoading, setCaculationLoading] = useState(false)

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchNormsData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchNormsData = async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getNormBasedUtilityBudget(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.data?.map((item, index) => {
        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
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
    showCalculate: true,
    showExport: true,
    ExcelName: `Norms - ${AOP_YEAR}`,
  }

  // Calculate Norms data via API
  const handleCalculate = async () => {
    setCaculationLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.calculateNormsData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation completed successfully!',
          severity: 'success',
        })
        // Refresh the data after calculation
        await fetchNormsData()
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
      'apr.norms',
      'apr.price',
      'may.norms',
      'may.price',
      'jun.norms',
      'jun.price',
      'jul.norms',
      'jul.price',
      'aug.norms',
      'aug.price',
      'sep.norms',
      'sep.price',
      'oct.norms',
      'oct.price',
      'nov.norms',
      'nov.price',
      'dec.norms',
      'dec.price',
      'jan.norms',
      'jan.price',
      'feb.norms',
      'feb.price',
      'mar.norms',
      'mar.price',
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
      const { normHeaderId, id, inEdit, ...rest } = item
      return {
        ...rest,
        normsHeaderFkId: normHeaderId,
      }
    })

    try {
      // Transform modifiedCells into the format expected by the API

      console.log('payload', tempPayload)

      // Call the API to save changes
      // NOTE: Update this API call to expect nested format when ready
      const response = await UtilityPlantApiServiceV2.saveNormsData(
        keycloak,
        tempPayload, // Now sending nested format: { apr: { norms, quantity, ... } }
        AOP_YEAR,
      )

      // Update the local state with the saved data
      // setRows(updatedRows)
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
      const response = await UtilityPlantApiServiceV2.saveNormsExcel(
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
        await fetchNormsData()
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
          await fetchNormsData()
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
      await UtilityPlantApiServiceV2.exportNormsExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
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

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading || calculationLoading}
      >
        <Stack
          display='flex'
          flexDirection='column'
          alignItems='center'
          justifyContent='center'
        >
          <CircularProgress color='inherit' />
          {calculationLoading && (
            <Typography variant='h5' sx={{ mt: 2 }}>
              Your data is being processed. This may take a few moments—thank
              you for your patience.
            </Typography>
          )}
        </Stack>
      </Backdrop>
      <NestedKendoTable
        columns={nestedColumns}
        rows={rows}
        setRows={setRows}
        handleCalculate={handleCalculate}
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
        customHeight={80}
        groupBy={['generatingPlantName', 'accountName']}
      />
    </Box>
  )
}

export default Norms
