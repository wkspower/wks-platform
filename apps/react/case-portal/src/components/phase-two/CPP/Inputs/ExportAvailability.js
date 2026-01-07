import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/CPP/utilityPlantApiServiceV2'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'

const dummyDataForExportAvailability = [
  {
    id: 1,
    assetName: 'GT1',
    uom: 'MW',
    april: true,
    may: true,
    june: false,
    july: false,
    aug: false,
    sep: false,
    oct: false,
    nov: false,
    dec: false,
    jan: false,
    feb: false,
    march: false,
  },
  {
    id: 2,
    assetName: 'GT2',
    uom: 'MW',
    april: true,
    may: true,
    june: false,
    july: true,
    aug: true,
    sep: false,
    oct: false,
    nov: true,
    dec: true,
    jan: false,
    feb: false,
    march: false,
  },
  {
    id: 3,
    assetName: 'GT3',
    uom: 'MW',
    april: true,
    may: false,
    june: true,
    july: false,
    aug: true,
    sep: false,
    oct: true,
    nov: false,
    dec: true,
    jan: false,
    feb: true,
    march: false,
  },
  {
    id: 4,
    assetName: 'STG',
    uom: 'MW',
    april: false,
    may: true,
    june: false,
    july: true,
    aug: false,
    sep: true,
    oct: false,
    nov: true,
    dec: false,
    jan: true,
    feb: false,
    march: true,
  },
]
const ExportAvailability = () => {
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
      type: 'boolean',
      editable: true,
    },
    // May
    {
      title: headerMap[5],
      field: 'may',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Jun
    {
      title: headerMap[6],
      field: 'june',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Jul
    {
      title: headerMap[7],
      field: 'july',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Aug
    {
      title: headerMap[8],
      field: 'aug',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Sep
    {
      title: headerMap[9],
      field: 'sep',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Oct
    {
      title: headerMap[10],
      field: 'oct',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    // Nov
    {
      title: headerMap[11],
      field: 'nov',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    //Dec
    {
      title: headerMap[12],
      field: 'dec',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    //Jan
    {
      title: headerMap[1],
      field: 'jan',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    //Feb
    {
      title: headerMap[2],
      field: 'feb',
      width: 80,
      type: 'boolean',
      editable: true,
    },
    //mar
    {
      title: headerMap[3],
      field: 'march',
      width: 80,
      type: 'boolean',
      editable: true,
    },
  ]

  const [rows, setRows] = useState([])

  const nonEditableRows = []

  useEffect(() => {
    if (PLANT_ID) {
      const rowsWithEditableFlag = dummyDataForExportAvailability.map(
        (row) => ({
          ...row,
          isEditable: !nonEditableRows.includes(row.assetName),
        }),
      )
      setRows(rowsWithEditableFlag)
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchPlantRequirementData = async () => {
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
      const rowsWithEditableFlag = res?.data?.map((row) => ({
        ...row,
        isEditable: !nonEditableRows.includes(row.assetName),
      }))
      setRows(rowsWithEditableFlag)
      setSnackbarOpen(true)
      // setSnackbarData({
      //   message: 'Data fetched successfully!',
      //   severity: 'success',
      // })
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
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
        const { inEdit, ...rest } = item
        return rest
      })
      const tempPayload = JSON.stringify(payload)

      console.log('payload', tempPayload)

      // Call the API to save changes
      // const response = await UtilityPlantApiServiceV2.savePlantRequirementData(
      //   keycloak,
      //   PLANT_ID,
      //   tempPayload
      // )

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
        title='Export Availability'
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

export default ExportAvailability
