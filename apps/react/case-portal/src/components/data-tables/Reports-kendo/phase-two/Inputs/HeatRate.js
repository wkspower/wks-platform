import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { Stack } from '../../../../../../node_modules/@mui/material/index'
import STGHeatRate from './STGHeatRate'
import HRSGHeatRate from './HRSGHeatRate'

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
      field: 'cppUtility',
      title: 'CPP Utility',
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
      field: 'freeSteamFactor',
      title: 'Free Steam Factor',
      width: 130,
      type: 'number1',
      editable: true,
      minWidth: 100,
    },
    {
      field: 'remarks',
      title: 'Remark',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [rows, setRows] = useState([])
  const [selectedPlant, setSelectedPlant] = useState('')
  const [dropdownOptions, setDropdownOptions] = useState([])
  useEffect(() => {
    if (selectedPlant) {
      fetchHeatRateData(selectedPlant)
    }
  }, [PLANT_ID, AOP_YEAR, selectedPlant])

  useEffect(() => {
    getPlantList()
  }, [PLANT_ID, AOP_YEAR])

  const getPlantList = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getPlantList(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      // Convert to required format
      const convertedData = res?.map((item) => ({
        id: item[0],
        name: item[1],
      }))

      if (convertedData?.length === 0) {
        setDropdownOptions([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      setSelectedPlant(convertedData[0]?.id)
      setDropdownOptions(convertedData)
    } catch (error) {
      console.error('Error fetching plant list:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const fetchHeatRateData = async (assetId) => {
    setLoading(true)
    try {
      const res = await InputApiService.getHeatRateData(keycloak, assetId)

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.map((item, index) => {
        return { ...item, id: item.id || index + 1,remarks: item.remarks || '' }
      })
      setRows(tempRes)
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
    saveBtn: false,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showExport: false,
    showImport: false,
    showTitle: true,
    showDropdown: true,
  }

  const dropdownConfig = {
    options: dropdownOptions,
    label: 'Select Plant',
    placeholder: 'Select Plant',
    valueKey: 'id',
    labelKey: 'name',
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

      const res = await InputApiService.saveHeatRateData(
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
        title='GT Heat Rate'
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
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedPlant}
        setSelectedDropdownValue={setSelectedPlant}
        paginationConfig={{
          threshold: 20,           // Show pagination if > 50 rows
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 20,
        }}
      />

      <Stack sx={{mt:2}}><STGHeatRate /></Stack>
      <Stack sx={{mt:2}}><HRSGHeatRate /></Stack>
    </Box>
  )
}

export default HeatRate
