import { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'components/aop-phase-two/services/cpp/utilityPlantApiServiceV2'
import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from '../common/AdvanceKendoTable/index'
const SenderReceiverMapping = () => {
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchPlantRequirementData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getSRMapping(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const apiRows = Array.isArray(res) ? res : res?.data

      if (!apiRows || apiRows.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const formattedData = apiRows.map((item, index) => ({
        ...item,
        remarks: item?.remarks || '',
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Column definitions
  const columns = [
    {
      field: 'receiverUtility',
      title: 'Receiver Utility',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
      hidden: false,
    },
    {
      field: 'receiverUtilityId',
      title: 'Receiver Utility ID',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
      hidden: false,
    },
    {
      field: 'receiverCostCenter',
      title: 'Receiver Cost Center',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverCostCenterId',
      title: 'Receiver Cost Center ID',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverPlant',
      title: 'Receiver Plant',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverPlantId',
      title: 'Receiver Plant ID',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderCostCenter',
      title: 'Sender Cost Center',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderCostCenterId',
      title: 'Sender Cost Center ID',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderPlant',
      title: 'Sender Plant',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderPlantId',
      title: 'Sender Plant ID',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: true,
    },
    {
      field: 'utility',
      title: 'Utility',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
    },
    {
      field: 'utilityId',
      title: 'Utility ID',
      widthT: 120,
      minWidth: 120,
      type: 'text',
      editable: true,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 150,
      minWidth: 150,
      type: 'textarea',
      editable: true,
    },
  ]

  // Permissions
  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    downloadExcelBtnFromUI: false,
    ExcelName: 'Sender Receiver Mapping',
    showImport: true,
    showExport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Sender Receiver Mapping (Utility for Utility)',
  }

  const saveChanges = async () => {
    setLoading(true)

    const modifiedData = Object.values(modifiedCells)
    const dataToSave = modifiedData.filter((row) => row.inEdit || row.isNew)

    if (!dataToSave || dataToSave.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    const payload = dataToSave.map((item) => {
      const { inEdit, isNew, saveStatus, errDescription, ...rest } = item
      return {
        ...rest,
        aopYear: AOP_YEAR,
        plantFkId: PLANT_ID,
      }
    })

    try {
      const response = await UtilityPlantApiServiceV2.saveSRMapping(
        keycloak,
        payload,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Successfully saved changes!',
        severity: 'success',
      })
      setModifiedCells({})
      await fetchPlantRequirementData()
      return response
    } catch (error) {
      console.error('Error saving SR mapping:', error)
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
      const response = await UtilityPlantApiServiceV2.importSRMappingExcel(
        file,
        keycloak,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchPlantRequirementData()
        return
      }

      if (response?.code === 400 && response?.data) {
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
          link.download = `SRMapping_Errors_${new Date().getTime()}.xlsx`
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
          await fetchPlantRequirementData()
          return
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
          return
        }
      }

      setSnackbarOpen(true)
      setSnackbarData({
        message: response?.message || 'Failed to import Excel file.',
        severity: 'error',
      })
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
      await UtilityPlantApiServiceV2.exportSRMappingExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting SR mapping:', error)
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
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          columns={columns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title={permissions.showTitle ? permissions.titleName : ''}
          permissions={permissions}
          handleExport={handleExport}
          handleExcelUpload={handleExcelUpload}
          saveChanges={saveChanges}
          fetchData={fetchPlantRequirementData}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          customHeight={80}
          paginationConfig={{
            threshold: 100,
            buttonCount: 5,
            pageSizes: [10, 20, 50, 100],
            defaultPageSize: 100,
          }}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
        />
      </Stack>
    </Box>
  )
}

export default SenderReceiverMapping
