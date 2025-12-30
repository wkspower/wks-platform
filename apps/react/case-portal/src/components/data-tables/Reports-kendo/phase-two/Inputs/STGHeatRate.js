import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'

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
  ]

  const [rows, setRows] = useState([])
 
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
    showExport: false,
    showImport: false,
    showTitle: true,
    showDropdown: false,
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

    try {
      const payload = modifiedData.map((item) => {
        const { inEdit, ...rest } = item
        return rest
      })
      const tempPayload = JSON.stringify(payload)

      console.log('payload', tempPayload)

      const res = await InputApiService.saveSTGHeatRateData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      console.log('res', res)

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
