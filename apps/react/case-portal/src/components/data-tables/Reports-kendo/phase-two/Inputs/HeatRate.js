import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { dummyDataForHeatRate } from '../nestedDummyData'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import { MenuItem, TextField } from '../../../../../../node_modules/@mui/material/index'

const HeatRate = () => {
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
      field: 'equipType',
      title: 'Equipment Type',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    {
      field: 'cpputility',
      title: 'CPU Utility',
      width: 120,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'gtLoad',
      title: 'GT Load',
      width: 100,
      type: 'number1',
      editable: true,
      minWidth: 80,
    },
    {
      field: 'heatRate',
      title: 'Heat Rate',
      width: 120,
      type: 'number1',
      editable: true,
      minWidth: 100,
    },
    {
      field: 'freeStmFactor',
      title: 'Free Steam Factor',
      width: 130,
      type: 'number1',
      editable: true,
      minWidth: 100,
    },
  ]

  const [rows, setRows] = useState([])
  const [selectedPlant, setSelectedPlant] = useState('')

  useEffect(() => {
    if (PLANT_ID && selectedPlant) {
      if(selectedPlant=='NMD-Power Plant-1'){
        setRows(dummyDataForHeatRate)
      }
      else{
       setRows([])
      }
    }
  }, [PLANT_ID, AOP_YEAR,selectedPlant])

  const fetchHeatRateData = async () => {
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
      console.log('res', res)
      setRows(res?.data)
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error fetching heat rate data:', error)
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

      <TextField
        select
        value={selectedPlant || ''}
        onChange={(e) => {
          const selectedPlantId = e.target.value
          // const selectedGradeObj = grades.find(
          //   (g) => g.gradeId === selectedGradeId,
          // )
          setSelectedPlant(selectedPlantId)
        }}
        className='dropdown-select'
        variant='outlined'
        label='Select'
        InputLabelProps={{
          shrink: true,
          sx: {
            fontWeight: 'bold',
          },
        }}
      >
        <MenuItem value='' disabled>
          Select
        </MenuItem>

        {['NMD-Power Plant-1', 'NMD-Power Plant-2', 'NMD-Power Plant-3']?.map(
          (plant) => (
            <MenuItem key={plant} value={plant}>
              {plant}
            </MenuItem>
          ),
        )}
      </TextField>
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Heat Rate'
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

export default HeatRate
