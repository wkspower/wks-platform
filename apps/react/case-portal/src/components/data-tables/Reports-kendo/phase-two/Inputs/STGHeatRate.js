import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { validateRowDataWithRemarks } from 'components/Utilities/commonUtilityFunctions'

const STGHeatRate = () => {
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
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    {
      field: 'loadMW',
      title: 'Load (MW)',
      width: 100,
      type: 'number1',
      editable: true,
      minWidth: 80,
    },
    {
      field: 'svhInletTPH',
      title: 'SVH Inlet (TPH)',
      width: 120,
      type: 'number1',
      editable: true,
      minWidth: 100,
    },
    {
      field: 'smBleedFlowTPH',
      title: 'SM Bleed Flow (TPH)',
      width: 140,
      type: 'number1',
      editable: true,
      minWidth: 120,
    },
    {
      field: 'slExtFlowTPH',
      title: 'SL Ext Flow (TPH)',
      width: 130,
      type: 'number1',
      editable: true,
      minWidth: 110,
    },
    {
      field: 'condensingLoadM3Hr',
      title: 'Condensing load (m3/hr)',
      width: 150,
      type: 'number1',
      editable: true,
      minWidth: 130,
    },
    {
      field: 'heatRateKcalKWH',
      title: 'Heat Rate Calc (Kcal/KWH)',
      width: 160,
      type: 'number1',
      editable: true,
      minWidth: 140,
    },
    {
      field: 'remarks',
      title: 'Remark',
      width: 230,
      type: 'textarea',
      editable: true,
      minWidth: 150,
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
      const res = await InputApiService.getSTGHeatRateData(keycloak, PLANT_ID)

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      console.log('res', res)
      setRows(res)
      setOriginalRows(res)
    } catch (error) {
      console.error('Error fetching STG heat rate data:', error)
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
    downloadExcelBtnFromUI: true,
    ExcelName: `STG Heat Rate - ${AOP_YEAR}`,
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
    const fieldsToCheck = [
      'loadMW',
      'svhInletTPH',
      'smBleedFlowTPH',
      'slExtFlowTPH',
      'condensingLoadM3Hr',
      'heatRateKcalKWH',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
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

      const res = await InputApiService.saveSTGHeatRateData(
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
        title='STG Heat Rate'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
      />
    </Box>
  )
}

export default STGHeatRate
