import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/vgoht/productionNormsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { configurationAndReportManualEntryResponse } from '../dummyData'
import RevButtonSection from './components/RevButtonSection'

const PIMSThroughput = ({ startDate, endDate }) => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
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
  const [revisionUpdated, setRevisionUpdated] = useState(false)

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
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchConfigurationData()
    }
  }, [PLANT_ID, AOP_YEAR, revisionUpdated])

  const dummyData = [
    {
      id: '2B7E617F-DAA8-4327-8C6C-F3B639D69DE7',
      normParameterFKId: 'E7A2BA96-B369-40AD-AF80-72239996EDBB',
      jan: 2920.035,
      feb: 16461.149,
      mar: 6405.702,
      apr: 4812.173,
      may: 10052.645,
      jun: 10781.083,
      jul: 9144.971,
      aug: 9787.126,
      sep: 8891.538,
      oct: 12134.646,
      nov: 15012.286,
      dec: 2095.782,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'Cold feed from RTF to VGOHT',
      productDisplayOrder: '1',
    },
    {
      id: '16E987CF-8AFE-4272-9C8B-FC775B8BEBD2',
      normParameterFKId: '9C83E851-A359-45A2-B35D-DAFA67135BA0',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'Flushing Oil to VGOHT1',
      productDisplayOrder: '2',
    },
    {
      id: 'C0923D85-CD6E-4418-880B-FA4DB9CB34EE',
      normParameterFKId: 'E18DBB72-6745-42D9-8AD3-508C8C1730C4',
      jan: 124308.004,
      feb: 111088.591,
      mar: 101633.016,
      apr: 114457.432,
      may: 107590.219,
      jun: 115834.295,
      jul: 110321.779,
      aug: 105408.989,
      sep: 38813.577,
      oct: 21301.069,
      nov: 101892.113,
      dec: 97473.388,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: '361 HCGO from Coker',
      productDisplayOrder: '3',
    },
    {
      id: '75F43F2E-11F1-4393-8018-F8AC6B06953E',
      normParameterFKId: '4DA16870-9F12-4B4E-B460-63440815AC35',
      jan: 354648.534,
      feb: 353568.539,
      mar: 360590.265,
      apr: 364573.028,
      may: 352760.289,
      jun: 356467.534,
      jul: 349617.985,
      aug: 310361.24,
      sep: 139773.935,
      oct: 40705.809,
      nov: 353080.684,
      dec: 365779.406,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'Hot Feed from CDU',
      productDisplayOrder: '4',
    },
    {
      id: 'C89F357D-8CDD-46F4-90FE-E86D05098AE1',
      normParameterFKId: '16F6C40C-977D-4923-9A78-96E26E3DD7B6',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'AGO feed from DUF tanks',
      productDisplayOrder: '5',
    },
    {
      id: '3E57E267-5DD8-4AA4-B205-FDF67F693840',
      normParameterFKId: 'AA084164-5D68-4DB3-A547-FE8D8A321D1B',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'LCO from FCCU',
      productDisplayOrder: '6',
    },
    {
      id: '7815BB2A-A67B-40F3-B543-FA1C3EF8CB7E',
      normParameterFKId: '888FDE42-5884-4EC0-92D1-9DFD63FA79D7',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'Sour vaccum gas oil',
      productDisplayOrder: '7',
    },
    {
      id: 'CCA0DCA9-51FB-4763-B89E-F77EE2311A0E',
      normParameterFKId: '40B54589-528D-4202-A483-DBFCB84BFDA6',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: 'Test',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: "VGO T'PUT",
      productDisplayOrder: '8',
    },
    {
      id: '9724F54D-B9F8-40E3-88D5-E5D16AD71A37',
      normParameterFKId: 'DDFB6281-7373-47B8-B929-6738AC6F57E5',
      jan: 0,
      feb: 0,
      mar: 0,
      apr: 0,
      may: 0,
      jun: 0,
      jul: 0,
      aug: 0,
      sep: 0,
      oct: 0,
      nov: 0,
      dec: 0,
      remarks: '',
      auditYear: '2026-27',
      UOM: 'MT',
      ConfigTypeDisplayName: 'Configuration',
      TypeDisplayName: 'Feed',
      productName: 'Cold Feed %',
      productDisplayOrder: '9',
    },
  ]
  const fetchConfigurationData = async () => {
    setLoading(true)
    try {
      await new Promise((resolve) => setTimeout(resolve, 1000))

      const res = dummyData

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      console.log('Configuration data:', res)
      const formattedData = res?.map((item, index) => ({
        ...item,
        remarks: item.remarks || '',
        value: item.apr,
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching configuration data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
      setRevisionUpdated(false)
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
    ExcelName: `PIMS_THROUGHPUT_${AOP_YEAR}`,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'PIMS Throughput',
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

      const response = await ProductionNormsApiService.savePIMSThroughputData(
        keycloak,
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
      const response = await ProductionNormsApiService.importConfigurationExcel(
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
      await ProductionNormsApiService.exportConfigurationExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
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

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <RevButtonSection
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
        revisionUpdated={revisionUpdated}
        setRevisionUpdated={setRevisionUpdated}
      />
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
        groupBy={['TypeDisplayName']}
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

export default PIMSThroughput
