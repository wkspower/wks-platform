import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
const dummyRowsData = [
  {
    id: 1,
    plant: 'NMD - Power Plant 1',
 
    plantId: '40NB',
   
    //costCenter: 'NG-GT1-Process',    
    costCenter: 'Demo',
   
    costCenterId: 'RIL_10709000',
    cppUtility: 'LP Steam_Dis',
    cppUtilityId: '310027965',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 502,
    may: 486,
    june: 502,
    july: 486,
    aug: 502,
    sep: 486,
    oct: 502,
    nov: 454,
    dec: 502,
    jan: 454,
    feb: 502,
    march: 454,
    grandTotal: 5427,
  },
  {
    id: 2,
    plant: 'NMD - Power Plant 2',
    plantId: '40NC',
    costCenter: 'NG-GT2-Process',
    costCenterId: 'RIL_10710000',
    cppUtility: 'Water',
    cppUtilityId: 'RAW WATER',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'M3',
    april: 18,
    may: 19,
    june: 18,
    july: 19,
    aug: 18,
    sep: 19,
    oct: 18,
    nov: 17,
    dec: 19,
    jan: 17,
    feb: 19,
    march: 17,
    grandTotal: 201,
  },
  {
    id: 3,
    plant: 'NMD - Power Plant 3',
    plantId: '40ND',
    costCenter: 'NG-GT3-Process',
    costCenterId: 'RIL_10711000',
    cppUtility: 'LP Steam_Dis',
    cppUtilityId: '310027965',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 486,
    may: 502,
    june: 486,
    july: 502,
    aug: 486,
    sep: 502,
    oct: 486,
    nov: 454,
    dec: 486,
    jan: 454,
    feb: 486,
    march: 454,
    grandTotal: 1490,
  },
  {
    id: 4,
    plant: 'NMD - STG Power Plant',
    plantId: '40NE',
    costCenter: 'NG-STG 1-Process',
    costCenterId: 'RIL_10712000',
    cppUtility: 'Water',
    cppUtilityId: 'RAW WATER',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'M3',
    april: 19,
    may: 19,
    june: 19,
    july: 19,
    aug: 19,
    sep: 19,
    oct: 19,
    nov: 19,
    dec: 19,
    jan: 19,
    feb: 19,
    march: 19,
    grandTotal: 55,
  },
  {
    id: 5,
    plant: 'NMD - Utility Plant',
    plantId: '40NF',
    costCenter: 'NG-Compressed Air',
    costCenterId: 'RIL_10708018',
    cppUtility: 'RAW WATER',
    cppUtilityId: 'RAW WATER',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'M3',
    april: 250,
    may: 250,
    june: 250,
    july: 250,
    aug: 250,
    sep: 250,
    oct: 250,
    nov: 250,
    dec: 250,
    jan: 250,
    feb: 250,
    march: 250,
    grandTotal: 3000,
  },
  {
    id: 6,
    plant: 'NMD - Utility Plant',
    plantId: '40NF',
    costCenter: 'NG-ETP',
    costCenterId: 'RIL_10708019',
    cppUtility: 'Power_Dis',
    cppUtilityId: '310027910',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40N0',
    uom: 'KWH',
    april: 4794000,
    may: null,
    june: null,
    july: null,
    aug: null,
    sep: null,
    oct: null,
    nov: null,
    dec: null,
    jan: null,
    feb: null,
    march: null,
    grandTotal: 5144620,
  },
  {
    id: 7,
    plant: 'NMD - Utility Plant',
    plantId: '40NF',
    costCenter: 'NG-HRSG 1-Steam',
    costCenterId: 'RIL_10708005',
    cppUtility: 'LP Steam_Dis',
    cppUtilityId: '310027965',
    cppPlant: 'NMD - Utility/Power Dist',
    cppPlantId: '40NG',
    uom: 'MT',
    april: 614,
    may: 594,
    june: 614,
    july: 594,
    aug: 614,
    sep: 594,
    oct: 614,
    nov: 554,
    dec: 614,
    jan: 554,
    feb: 614,
    march: 554,
    grandTotal: 1800,
  },
  {
    id: 8,
    plant: 'NMD - Utility/Power Dist',
    plantId: '40NG',
    costCenter: 'NG-Power Sale',
    costCenterId: 'RIL_10713001',
    cppUtility: 'Power_Dis',
    cppUtilityId: '310027910',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'KWH',
    april: 89084,
    may: 91931,
    june: 93844,
    july: 93193,
    aug: 91808,
    sep: 88284,
    oct: 94243,
    nov: 85125,
    dec: 91268,
    jan: 109093,
    feb: 0,
    march: 0,
    grandTotal: 109093,
  },
  {
    id: 9,
    plant: 'NMD - Rev Proc',
    plantId: '40N0',
    costCenter: 'NG-Corporate Social Responsibilities',
    costCenterId: 'RIL_10799029',
    cppUtility: 'Water',
    cppUtilityId: 'RAW WATER',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'M3',
    april: 29837,
    may: 30331,
    june: 30831,
    july: 30831,
    aug: 30831,
    sep: 30831,
    oct: 30831,
    nov: 30831,
    dec: 30831,
    jan: 30831,
    feb: 30831,
    march: 30831,
    grandTotal: 393303,
  },
  {
    id: 10,
    plant: 'NMD - Rev Proc',
    plantId: '40N0',
    costCenter: 'NG-Environment',
    costCenterId: 'RIL_10799005',
    cppUtility: 'COMPRESSED AIR',
    cppUtilityId: '310027904',
    cppPlant: 'NMD - Utility Plant',
    cppPlantId: '40NF',
    uom: 'NM3',
    april: 26860,
    may: 27858,
    june: 26860,
    july: 27858,
    aug: 26860,
    sep: 27858,
    oct: 26860,
    nov: 25812,
    dec: 26860,
    jan: 25812,
    feb: 26860,
    march: 25812,
    grandTotal: 328303,
  },
  {
    id: 11,
    plant: 'NMD - Rev Proc',
    plantId: '40N0',
    costCenter: 'NG-Fire',
    costCenterId: 'RIL_10799003',
    cppUtility: 'Power_Dis',
    cppUtilityId: '310027910',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'KWH',
    april: 30600,
    may: 32100,
    june: 32400,
    july: 32400,
    aug: 31620,
    sep: 31620,
    oct: 31620,
    nov: 31620,
    dec: 31620,
    jan: 31620,
    feb: 31620,
    march: 31620,
    grandTotal: 381620,
  },
  {
    id: 12,
    plant: 'NMD - Rev Proc',
    plantId: '40N0',
    costCenter: 'NG-Site Common',
    costCenterId: 'RIL_10799000',
    cppUtility: 'Power_Dis',
    cppUtilityId: '310027910',
    cppPlant: 'NMD-Rev Proc',
    cppPlantId: '40N0',
    uom: 'KWH',
    april: 86400,
    may: 89400,
    june: 89280,
    july: 89280,
    aug: 89280,
    sep: 89280,
    oct: 89280,
    nov: 89280,
    dec: 89280,
    jan: 89280,
    feb: 89280,
    march: 89280,
    grandTotal: 109280,
  },
]

const FixedConsumption = () => {
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
  const [rows, setRows] = useState(dummyRowsData)
  const valueFormat= ValueFormatterProduction()
  // Column definitions
  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'plant',
      title: 'Plant',
      width: 150,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'plantId',
      title: 'Plant ID',
      width: 120,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'costCenter',
      title: 'CostCenter',
      widthT: 120,
      type: 'text',
      editable: false,
      hidden:true,
    },
    {
      field: 'costCenterId',
      title: 'CostCenter ID',
      widthT: 95,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'cppUtility',
      title: 'CPP Utilities',
      widthT: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppUtilityId',
      title: 'CPP Utility IDs',
      widthT: 105,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlant',
      title: 'CPP Plant',
      widthT: 110,
      type: 'text',
      editable: false,
    },
    {
      field: 'cppPlantId',
      title: 'CPP Plant ID',
      width: 120,
      type: 'text',
      editable: false,
    },
    { field: 'uom', title: 'UOM', widthT: 60, type: 'text', editable: false },
    {
      field: 'april',
      title: headerMap[4], // will be 'Apr-25' if AOP_YEAR is 2025-26
      editable: true, 
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3],
      editable: true,
      widthT: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'grandTotal',
      title: 'Grand Total',
      widthT: 120,
      type: 'number',
      format: valueFormat,
    },
  ]

  useEffect(() => {
    if(PLANT_ID&&AOP_YEAR){
      fetchFixedConsumptionData(keycloak, PLANT_ID,AOP_YEAR)
      setModifiedCells({})
    }
  }, [PLANT_ID,AOP_YEAR])

  const fetchFixedConsumptionData = async (keycloak, PLANT_ID,AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getFixedConsumptionData(
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

      const formattedData = res.map((item, index) => ({
        ...item,
        id: index,
      }))
      // Process and set the fetched data to rows
      console.log('*** fixed consumption data', formattedData)
    
      setRows(formattedData)
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
    showTitle:true,
  }

  // Dummy save handler
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

    console.log('modifiedData', modifiedData)
    // const payload = JSON.stringify(modifiedData)
    const payload = modifiedData

    
     try {
      // Transform modifiedCells into the format expected by the API
    

      // Call the API to save changes
      const response = await UtilityPlantApiServiceV2.saveFixedConsumptionData(
        keycloak,
        PLANT_ID,
        payload,
        AOP_YEAR
      )
      console.log('response',response)
      // Update the local state with the saved data
      // setRows(updatedRows)
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} rows changes!`,
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
        // title='Fixed Consumption'
        title={screenTitle?.title}
        permissions={permissions}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy="plant"
        // groupBy={['plant', 'plantId']}
      />
    </Box>
  )
}

export default FixedConsumption
