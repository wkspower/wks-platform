import { useEffect, useState, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'

const ShutdownHistoryConfig = () => {
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
  const [rows, setRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const monthOptions = [
    { value: 'January', label: 'January' },
    { value: 'February', label: 'February' },
    { value: 'March', label: 'March' },
    { value: 'April', label: 'April' },
    { value: 'May', label: 'May' },
    { value: 'June', label: 'June' },
    { value: 'July', label: 'July' },
    { value: 'August', label: 'August' },
    { value: 'September', label: 'September' },
    { value: 'October', label: 'October' },
    { value: 'November', label: 'November' },
    { value: 'December', label: 'December' },
  ]

  const getYearOptions = () => {
    const currentYear = new Date().getFullYear()
    return Array.from({ length: 6 }, (_, i) => {
      const year = (currentYear - i).toString()
      return { value: year, label: year }
    })
  }

  const columns = [
    {
      field: 'monthly',
      title: 'Month',
      widthT: 150,
      minWidth: 120,
      type: 'select',
      options: monthOptions,
      editable: true,
      hidden: false,
    },
    {
      field: 'year',
      title: 'Year',
      widthT: 120,
      minWidth: 100,
      type: 'select',
      options: getYearOptions(),
      editable: true,
      hidden: false,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 300,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchShutdownHistoryConfig()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchShutdownHistoryConfig = async () => {
    setLoading(true)
    try {
      const res = await DataService.getShutdownHistoryConfig(
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

      console.log('Shutdown History Config data:', res)
      const formattedData = res?.map((item, index) => ({
        ...item,
        remarks: item.remarks || item.remark || '',
        id: item?.id || item?.idFromApi || index + 1,
      }))
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching shutdown history config data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: true,
    deleteButton: true,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: false,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Shutdown History Config',
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

    const requiredFields = ['remarks']
    const validationMessage = validateFields(modifiedData, requiredFields)
    if (validationMessage) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationMessage,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const dataList = modifiedData.map((row) => {
      const obj = {
        month: row.monthly,
        year: row.year,
        aopYear: AOP_YEAR,
        remark: row.remarks,
        PlantFKId: PLANT_ID,
      }

      if (row.idFromApi || row.id) {
        obj.id = row.idFromApi || row.id
      }

      return obj
    })

    try {
      console.log('Saving shutdown history config data:', dataList)

      const response = await DataService.saveShutdownHistoryConfig(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        dataList,
      )

      if (response?.code === 200) {
        setModifiedCells({})
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        await fetchShutdownHistoryConfig()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving shutdown history config data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const handleAddRow = () => {
    const newRow = {
      id: `new_${Date.now()}`,
      monthly: '',
      year: '',
      remarks: '',
      inEdit: true,
      isNew: true,
    }
    setRows([...rows, newRow])
    setModifiedCells({
      ...modifiedCells,
      [newRow.id]: newRow,
    })
  }

  const deleteRowData = async (dataItem) => {
    setLoading(true)
    try {
      await DataService.deleteShutdownHistoryConfig(keycloak, dataItem.id)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Record deleted successfully!',
        severity: 'success',
      })
      await fetchShutdownHistoryConfig()
    } catch (error) {
      console.error('Error deleting shutdown history config:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to delete record. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
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
        handleAddRow={handleAddRow}
        deleteRowData={deleteRowData}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
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

export default ShutdownHistoryConfig
