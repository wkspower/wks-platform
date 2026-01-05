import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { dummyDataForAssetAvailability } from '../nestedDummyData'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { TcsApiService } from 'services/phase-two-services/tcsApiService'

const AssetAvailability = () => {
  const keycloak = useSession()
  // State management

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const columns = [
    //Generating Plant
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },

    // Apr
    {
      title: headerMap[4],
      field: 'april',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // May
    {
      title: headerMap[5],
      field: 'may',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Jun
    {
      title: headerMap[6],
      field: 'june',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Jul
    {
      title: headerMap[7],
      field: 'july',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Aug
    {
      title: headerMap[8],
      field: 'aug',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Sep
    {
      title: headerMap[9],
      field: 'sep',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Oct
    {
      title: headerMap[10],
      field: 'oct',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    // Nov
    {
      title: headerMap[11],
      field: 'nov',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    //Dec
    {
      title: headerMap[12],
      field: 'dec',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    //Jan
    {
      title: headerMap[1],
      field: 'jan',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    //Feb
    {
      title: headerMap[2],
      field: 'feb',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    //mar
    {
      title: headerMap[3],
      field: 'march',
      width: 80,
      type: 'wholeNumber',
      editable: true,
      wholeNumberOnly: true,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  const [rows, setRows] = useState([])

  const nonEditableRows = ['HRSG1', 'HRSG2', 'HRSG3', 'PRDS']

  useEffect(() => {
    if (PLANT_ID) {
      fetchAssetPriorityData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchAssetPriorityData = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getAssetPriority(
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
      const rowsWithEditableFlag = res?.map((row, index) => ({
        ...row,
        id: row.id ||index + 1,
        remarks: row.remarks || '',
      }))
      setRows(rowsWithEditableFlag)
    } catch (error) {
      console.error('Error fetching asset priority data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Permissions (adjust as needed)
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

  // Save handler with API call
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
        const { id, inEdit, ...rest } = item
        return rest
      })

      console.log('payload', payload)

      // Call the API to save changes
      const response = await InputApiService.saveAssetPriority(
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
      console.error('Error saving plant requirement data:', error)
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
        title='Asset Priority'
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

export default AssetAvailability
