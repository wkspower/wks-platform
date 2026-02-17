import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { configurationAndReportManualEntryResponse } from '../dummyData'

const Configuration = () => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterPhaseTwo()
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [dependencyRules, setDependencyRules] = useState({})

  // Build dependency rules from row data
  // Expects rows to have dependencyConfig property on controller fields
  const buildDependencyRules = (rowsData) => {
    const rules = {}
    rowsData.forEach((row) => {
      if (row.dependencyConfig && row.productName) {
        rules[row.productName] = {
          dependentProductName: row.dependencyConfig.dependentProductName,
          values: row.dependencyConfig.valueMapping || {},
        }
      }
    })
    return rules
  }

  const columns = [
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'UOM',
      title: 'UOM',
      widthT: 80,
      minWidth: 60,
      type: 'text',
      editable: false,
    },
    {
      field: 'value',
      title: 'Value',
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'conditional',
      format: valueFormat,
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
    if (PLANT_ID && AOP_YEAR) {
      fetchConfigurationData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchConfigurationData = async () => {
    setLoading(true)
    try {
      // Simulate API call with 1 second delay
      await new Promise((resolve) => setTimeout(resolve, 1000))

      // const res = await ProductionNormsApiService.getConfigurationData(
      //   keycloak,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )

      const res = configurationAndReportManualEntryResponse.data.filter(
        (item) => item.normType !== 'PIMS Throughput',
      )

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const formattedData = res?.map((item, index) => ({
        ...item,
        remarks: item.remarks || '',
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)

      // Build dependency rules from the data
      const rules = buildDependencyRules(formattedData)
      setDependencyRules(rules)
    } catch (error) {
      console.error('Error fetching configuration data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: true,
    ExcelName: `Production_Norms_Configuration_${AOP_YEAR}`,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Configuration',
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

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const fieldsToCheck = [
      'apr',
      'may',
      'jun',
      'jul',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'mar',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'particulars',
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
      console.log('Saving configuration data:', payload)

      // const response = await ProductionNormsApiService.saveConfigurationData(
      //   keycloak,
      //   AOP_YEAR,
      //   payload,
      // )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving configuration data:', error)
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
      // const response = await ProductionNormsApiService.importConfigurationExcel(
      //   file,
      //   keycloak,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchConfigurationData()
      } else if (response?.code === 400 && response?.data) {
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
          link.download = `Configuration_Errors_${new Date().getTime()}.xlsx`
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
          await fetchConfigurationData()
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
      // await ProductionNormsApiService.exportConfigurationExcel(
      //   keycloak,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Configuration data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const handleCustomItemChange = (e, setRowsCallback) => {
    const { dataItem, field, value } = e

    if (field !== 'value') return

    const currentProductName = dataItem.productName
    const dependencyRule = dependencyRules[currentProductName]

    if (!dependencyRule) return

    const dependentValue = dependencyRule.values[value]
    if (dependentValue === undefined) return

    setRowsCallback((prevRows) => {
      return prevRows.map((row) => {
        if (row.productName === dependencyRule.dependentProductName) {
          return {
            ...row,
            value: dependentValue,
            inEdit: true,
          }
        }
        return row
      })
    })

    setModifiedCells((prev) => {
      const dependentRow = rows.find(
        (r) => r.productName === dependencyRule.dependentProductName,
      )
      if (!dependentRow) return prev

      return {
        ...prev,
        [dependentRow.id]: {
          ...dependentRow,
          value: dependentValue,
          inEdit: true,
        },
      }
    })

    // Update customModifiedCells for orange highlighting
    setCustomModifiedCells((prev) => {
      const dependentRow = rows.find(
        (r) => r.productName === dependencyRule.dependentProductName,
      )
      if (!dependentRow) return prev

      return {
        ...prev,
        [dependentRow.id]: {
          ...(prev[dependentRow.id] || {}),
          value: dependentValue,
        },
      }
    })
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
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
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customItemChange={handleCustomItemChange}
        externalCustomModifiedCells={customModifiedCells}
        externalSetCustomModifiedCells={setCustomModifiedCells}
        groupBy={['normType']}
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

export default Configuration
