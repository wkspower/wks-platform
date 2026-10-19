import { useEffect, useState } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/naphthasplitter/productionNormsApiService'
import { useSession } from 'SessionStoreContext'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { customValueFormatterPhaseTwo } from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

const NormsRationalizationFactor = ({ startDate, endDate }) => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, siteObject } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const valueFormat = customValueFormatterPhaseTwo(2)
  const columns = [
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
      field: 'uom',
      title: 'UOM',
      widthT: 120,
      minWidth: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'value',
      title: 'Value',
      editable: true,
      widthT: 150,
      minWidth: 120,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remark',
      widthT: 350,
      type: 'textarea',
      editable: true,
      minWidth: 300,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchConstantsData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchConstantsData = async () => {
    setLoading(true)
    try {
      const res = await ProductionNormsApiService.getRationalizationFactorData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.data?.length === 0) {
        setRows([])
        return
      }

      console.log('Norms Rationalization Factor data:', res)
     const formattedData = res?.data?.map((item, index) => ({
        ...item,
        id: item?.normParameterFKId || index + 1,
        productName: item.displayName || '',
        uom: item.uom || '',
        value: item?.value !== undefined && item?.value !== null ? item.value : '',
        remarks: item.remarks || '',
        type: item.type || item.normParameterTypeDisplayName || 'Energy Factor',
        normParameterTypeDisplayName: item.normParameterTypeDisplayName || item.type || 'Energy Factor',
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching constants data:', error)
      setRows([])
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
    showExport: false,
    ExcelName: `Production_Norms_Constants_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Norms Rationalization Factor',
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
    const recordsToValidate = data.length > 0 ? data : modifiedData

    const fieldsToCheck = ['value']
    const validationError = validateRowDataWithRemarks(
      recordsToValidate,
      originalRows,
      fieldsToCheck,
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

    // Clean payload matching NormsRationalizationFactorDTO exactly
    const payload = recordsToValidate.map((item) => ({
      normParameterFKId: item.normParameterFKId,
      displayName: item.displayName || item.productName || '',
      value: item.value !== null && item.value !== undefined ? String(item.value) : '0',
      remarks: item.remarks || '',
      uom: item.uom || '',
      normParameterTypeDisplayName:
        item.normParameterTypeDisplayName || item.type || 'Norms Rationalization Factor',
      type: item.type || item.normParameterTypeDisplayName || 'Norms Rationalization Factor',
    }))

    try {
      console.log('Saving Norms Rationalization Factor data:', payload)

      const response = await ProductionNormsApiService.saveRationalizationFactorData(
        keycloak,
        AOP_YEAR,
        PLANT_ID,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: response?.message || `Successfully saved ${payload.length} changes!`,
        severity: 'success',
      })
      await fetchConstantsData()
    } catch (error) {
      console.error('Error saving Norms Rationalization Factor data:', error)
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

      const response = await ProductionNormsApiService.importConstantsExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        periodFrom,
        periodTo,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchConstantsData()
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
      await ProductionNormsApiService.exportConstantsExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Norms Rationalization Factor data:', error)
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
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['type']}
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

export default NormsRationalizationFactor
