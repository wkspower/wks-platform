import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'

const HRSGHeatRate = () => {
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
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const valueFormat = ValueFormatterPhaseTwo()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    {
      field: 'id',
      title: 'Id',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
      hidden: true,
    },
    {
      field: 'equipmentName',
      title: 'Equipment Type',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    {
      field: 'cppUtility',
      title: 'CPP Utility',
      width: 120,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'hrsgLoad',
      title: 'HRSG Load',
      width: 100,
      type: 'number1',
      format: valueFormat,
      editable: true,
      minWidth: 80,
    },
    {
      field: 'heatRate',
      title: 'Heat Rate',
      width: 120,
      type: 'number1',
      format: valueFormat,
      editable: true,
      minWidth: 100,
    },
    {
      field: 'remarks',
      title: 'Remark',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  useEffect(() => {
    if (PLANT_ID) {
      fetchHeatRateData()
    }
  }, [PLANT_ID])

  const fetchHeatRateData = async () => {
    setLoading(true)
    try {
      // TODO: Replace with actual API call once backend is ready
      const res = await InputApiService.getHRSGHeatRateData(keycloak, PLANT_ID)

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res.map((item, index) => {
        const transformed = {
          id: item?.id || index + 1,
          remarks: item?.remarks || '',
          ...item,
        }
        return transformed
      })
      console.log('res', res)
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching HRSG heat rate data:', error)
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
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showImport: true,
    showExport: true,
    ExcelName: `HRSG Heat Rate - ${AOP_YEAR}`,
    showTitle: true,
    showDropdown: false,
  }

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
    const fieldsToCheck = ['hrsgLoad', 'heatRate']
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'equipmentName',
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
      const payload = modifiedData.map((item) => {
        const { inEdit, ...rest } = item
        return rest
      })
      const tempPayload = JSON.stringify(payload)

      const res = await InputApiService.saveHRSGHeatRateData(
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
      console.error('Error saving heat rate data:', error)
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
      const response = await InputApiService.saveHRSGHeatRateExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.success) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchHeatRateData()
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
      await InputApiService.exportHRSGHeatRateExcel(keycloak)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting HRSG Heat Rate data:', error)
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

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='HRSG Heat Rate'
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
      />
    </Box>
  )
}

export default HRSGHeatRate
