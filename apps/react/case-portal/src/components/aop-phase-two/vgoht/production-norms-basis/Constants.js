import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/vgoht/productionNormsApiService'
import { useSession } from 'SessionStoreContext'
import { validateRowDataWithoutRemarks, validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { customValueFormatterPhaseTwo } from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import RowBasedKendoTable from 'components/aop-phase-two/common/RowBasedKendoTable/index'

const Constants = ({ startDate, endDate, refreshData }) => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalObject, plantObject, year, siteObject } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const valueFormat = customValueFormatterPhaseTwo(4)

  const site = siteObject?.name?.toLowerCase()?.trim()
  const plant = plantObject?.name?.toLowerCase()?.trim()

  const isThreeDatesUi = useMemo(() => {
    if (site === 'sez' && ['vgoht-4', 'vgoht-3'].includes(plant)) return true
    if (site === 'dta' && ['dht1', 'dht2', 'vgoht-1', 'vgoht-2'].includes(plant)) return true
    return false
  }, [site, plant])
  
  const isNotRequiredRemarkValidation = useMemo(() => site === 'dta' && plant === 'hnuu', [site, plant])

  const EXCEL_NAME = generateExcelName(dataGridStore, 'Production_Norms_Basis_Constants')

  const columns = useMemo(() => {
    const cols = [
      {
        field: 'productName',
        title: 'Particulars',
        widthT: 300,
        minWidth: 250,
        type: 'text',
        editable: false,
        hidden: false,
      },
      {
        field: 'UOM',
        title: 'UOM',
        widthT: 120,
        minWidth: 100,
        type: 'text',
        editable: false,
      },
    ]

    const commonProps = { editable: true, widthT: 150, minWidth: 120, align: 'left', headerAlign: 'left', type: 'row-based', format: valueFormat, allowNegative: true }
    
    if (isThreeDatesUi) {
      cols.push({
        title: 'Operation',
        children: [
          { field: 'apr', title: 'Normal', ...commonProps },
          { field: 'may', title: 'Slow Down', ...commonProps },
          { field: 'jun', title: 'SOR', ...commonProps }
        ]
      })
    } else {
      cols.push({ field: 'value', title: 'Value', ...commonProps })
    }

    cols.push({
      field: 'remarks',
      title: 'Remark',
      widthT: 350,
      type: 'textarea',
      editable: true,
      minWidth: 300,
    })

    return cols
  }, [valueFormat, isThreeDatesUi])


  const fetchConstantsData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      setRows([])
      setOriginalRows([])
      setModifiedCells({})

      const method = isThreeDatesUi ? 'getConstantsDataEORSOR' : 'getConstantsData'
      const res = await ProductionNormsApiService[method](keycloak, PLANT_ID, AOP_YEAR)


      if (res?.data?.length === 0) {
        setRows([])
        setOriginalRows([])
        return
      }

      
      const formattedData = res?.data?.map((item, index) => ({
        ...item,
        remarks: item.remarks || '',
        normType: item?.type || null,
        id: item?.id || index + 1,
        type: (item?.UOM?.toLowerCase() || item?.uom?.toLowerCase()) === "boolean" ? "checkbox" : "number",
        format: valueFormat,
        allowNegative: true
      }))
      
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching constants data:', error)
      setRows([])
      setOriginalRows([])
    } finally {
        setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, isThreeDatesUi, keycloak])

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchConstantsData()
    }, 50)
    return () => clearTimeout(timer)
  }, [fetchConstantsData, refreshData])

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: true,
    ExcelName: EXCEL_NAME,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Constants',
  }

  const formatDateForAPI = (date) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const saveChanges = async () => {
    setLoading(true)

    // Validate required parameters
    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message:
          'Period dates are required. Please ensure dates are loaded from AOP Period Basis.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

    if (!SITE_ID) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Site ID is required.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

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
    
    const fieldsToCheck = isThreeDatesUi ? ['apr', 'may', 'jun']  : ['value']
    const skipRemark = isNotRequiredRemarkValidation && data.some(r => (r.type || '').toLowerCase() === 'filter criteria')
    const validator = skipRemark ? validateRowDataWithoutRemarks : validateRowDataWithRemarks
    const validationError = validator(data, originalRows, fieldsToCheck, 'productName')

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const payload = data.map((row) => {
      return {
        ...row,
        apr: row?.apr === true ? 1 : row?.apr === false ? 0 : row?.apr || 0,
        may: row?.may === true ? 1 : row?.may === false ? 0 : row?.may || 0,
        jun: row?.jun === true ? 1 : row?.jun === false ? 0 : row?.jun || 0,
        value: row?.value || 0,
        type: row.normType
      }
    })
    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)


      const method = isThreeDatesUi ? 'saveConstantsDataEORSOR' : 'saveConstantsData'
      await ProductionNormsApiService[method](keycloak, AOP_YEAR, PLANT_ID, SITE_ID, periodFrom, periodTo, payload)

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      fetchConstantsData()
    } catch (error) {
      console.error('Error saving constants data:', error)
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

    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message:
          'Period dates are required. Please ensure dates are loaded from AOP Period Basis.',
        severity: 'error',
      })
      return
    }

    setLoading(true)
    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)

      const method = isThreeDatesUi ? 'importConstantsExcelEORSOR' : 'importConstantsExcel'
      const response = await ProductionNormsApiService[method](file, keycloak, PLANT_ID, AOP_YEAR, periodFrom, periodTo)

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchConstantsData()
      } else if (response?.code === 400 && response?.data) {
        try {
          const binaryString = window.atob(response.data)
          const bytes = Uint8Array.from(binaryString, (c) => c.charCodeAt(0))
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Constants_Errors_${new Date().getTime()}.xlsx`
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
          await fetchConstantsData()
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
      const method = isThreeDatesUi ? 'exportConstantsExcelEORSOR' : 'exportConstantsExcel'
      await ProductionNormsApiService[method](keycloak, PLANT_ID, AOP_YEAR, EXCEL_NAME)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Constants data:', error)
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

  const handleDynamicColumnMerger = (dataItem, field) => {
    const megreColumns = ['Factors for Utility (HPS,MPS,LPS, Power) Norms', 'Factors for Utility Norms', 'Factors for Utility']
    if (megreColumns.includes(dataItem?.normType)) {
      if (field === 'apr') return { colSpan: 3 }
      if (field === 'may' || field === 'jun') return { hidden: true }
    }
    return {}
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <RowBasedKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        dynamicColumnMerger={handleDynamicColumnMerger}
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

export default Constants
