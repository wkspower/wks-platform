import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
const dummyRows = [
  {
    id: 1,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
   // cppUtilities: 'COMPRESSED AIR',
   ccpUtility: 'DEMO',
    cppUtilityId: '310027904', 
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'NM3',
    april: 531520,
    may: 561720,
    june: 543600,
    july: 561720,
    aug: 561720,
    sep: 543600,
    oct: 561720,
    nov: 545336,
    dec: 561720,
    jan: 561720,
    feb: 507360,
    march: 561720,
    grandTotal: 6602057,
  },
  {
    id: 2,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'Cooling Water 2',
    cppUtilityId: '310028004',
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'KM3',
    april: 3779,
    may: 3587,
    june: 3779,
    july: 3779,
    aug: 3779,
    sep: 3588,
    oct: 3779,
    nov: 3391,
    dec: 3779,
    jan: 3779,
    feb: 3414,
    march: 3779,
    grandTotal: 42751,
  },
  {
    id: 3,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'DM Water',
    cppUtilityId: '310027866',
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'M3',
    april: 1267,
    may: 1333,
    june: 1286,
    july: 1333,
    aug: 1333,
    sep: 1286,
    oct: 1333,
    nov: 6291,
    dec: 1333,
    jan: 1333,
    feb: 1210,
    march: 1333,
    grandTotal: 20733,
  },
  {
    id: 4,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'HP Steam_Dis',
    cppUtilityId: '310027939',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 350,
    may: 2600,
    june: 4500,
    july: 350,
    aug: 2600,
    sep: 4500,
    oct: 1040,
    nov: 2800,
    dec: 350,
    jan: 2600,
    feb: 2800,
    march: 350,
    grandTotal: 11290,
  },
  {
    id: 5,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'LP Steam_Dis',
    cppUtilityId: '310027965',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 3781,
    may: 3181,
    june: 3653,
    july: 3823,
    aug: 3181,
    sep: 3653,
    oct: 2487,
    nov: 3781,
    dec: 3781,
    jan: 3653,
    feb: 3781,
    march: 3653,
    grandTotal: 43371,
  },
  {
    id: 6,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'MP Steam_Dis',
    cppUtilityId: '310027940',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 12072,
    may: 11382,
    june: 11448,
    july: 13712,
    aug: 14372,
    sep: 11076,
    oct: 11086,
    nov: 16590,
    dec: 17003,
    jan: 16551,
    feb: 17240,
    march: 16408,
    grandTotal: 164408,
  },
  {
    id: 7,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'Nitrogen Gas',
    cppUtilityId: 'NITROGENG',
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'NM3',
    april: 295135,
    may: 302086,
    june: 312135,
    july: 312135,
    aug: 312135,
    sep: 302066,
    oct: 312135,
    nov: 235000,
    dec: 312135,
    jan: 312135,
    feb: 218929,
    march: 312135,
    grandTotal: 3600131,
  },
  {
    id: 8,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'Oxygen',
    cppUtilityId: 'OXYGEN',
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'MT',
    april: 5787,
    may: 6023,
    june: 2620,
    july: 5856,
    aug: 5464,
    sep: 6023,
    oct: 6152,
    nov: 2641,
    dec: 5543,
    jan: 5503,
    feb: 4500,
    march: 5543,
    grandTotal: 8581,
  },
  {
    id: 9,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'Power_Dis',
    cppUtilityId: '310027910',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'KVH',
    april: 2061312,
    may: 2178432,
    june: 2108160,
    july: 2178432,
    aug: 2178432,
    sep: 2108160,
    oct: 2178432,
    nov: 1344362,
    dec: 2178432,
    jan: 2178432,
    feb: 1967616,
    march: 2178432,
    grandTotal: 24883631,
  },
  {
    id: 10,
    // processPlant: 'NMD - EG',
    // processPlantId: '40N3',
    cppUtility: 'Water',
    cppUtilityId: 'RAW WATER',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'M3',
    april: 518,
    may: 518,
    june: 518,
    july: 518,
    aug: 518,
    sep: 518,
    oct: 518,
    nov: 518,
    dec: 518,
    jan: 518,
    feb: 518,
    march: 518,
    grandTotal: 518,
  },
]

const PlantRequirement = () => {
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
  const [rows, setRows] = useState([])
  // Column definitions
  const columns = [
    {
      field: 'plantName',
      title: 'Process Plant',
      width: 150,
      minWidth:150,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'plantCode',
      title: 'Plant Code',
      width: 120,
      minWidth:120,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'cppUtilities',
      title: 'CPP Utilities',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppUtiltiyIds',
      title: 'CPP Utility ID',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlant',
      title: 'CPP Plant',
      widthT: 120,
      minWidth:80,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlantId',
      title: 'CPP Plant ID',
      widthT: 100,
      minWidth:80,
      type: 'text',
      editable: false,
      hidden: true,
    },
    { field: 'uom', title: 'UOM', widthT: 60,minWidth:60, type: 'text', editable: false },
    {
      field: 'april',
      title: headerMap[4], // will be 'Apr-25' if AOP_YEAR is 2025-26
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'sep',
      title: headerMap[9],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: false,
      widthT: 100,
      minWidth:80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat
    },
    {
      field: 'grandTotal',
      title: 'Grand Total',
      widthT: 120,
      type: 'number',
      format: valueFormat
    },
  ]

  useEffect(() => {
    if(PLANT_ID && AOP_YEAR){
      fetchPlantRequirementData();
    }
  }, [PLANT_ID,AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getPlantRequirementData(
        keycloak,
        PLANT_ID,
        AOP_YEAR
      )
      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      console.log('res', res)
      // Process and set the fetched data to rows
      setRows(res)
      // setSnackbarOpen(true)
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
    saveBtn: false,
    allAction: true,
    showTitleNameBusiness: true,
    showTitle:true,
    titleName: screenTitle?.title,
  }

  // Save handler with API call
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
    const payload = modifiedData;
    try {
      // Transform modifiedCells into the format expected by the API
      console.log('payload', payload)

      // Call the API to save changes
      // const response = await UtilityPlantApiServiceV2.savePlantRequirementData(
      //   keycloak,
      //   PLANT_ID,
      //   payload
      // )

      // Update the local state with the saved data
      // setRows(updatedRows)
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
      {/* <KendoDataTables */}
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['plantName']}
      />
    </Box>
  )
}

export default PlantRequirement
