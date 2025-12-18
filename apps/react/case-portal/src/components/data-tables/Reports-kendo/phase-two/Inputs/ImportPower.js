import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import AssetAvailability from './AssetAvailability'
import { Stack } from '../../../../../../node_modules/@mui/material/index'
const dummyRowsData = [
  {
    id: 1,
    plant: "Power from MEL",
    utility: "Power_Dis",
    uom: "MW",
    april: 25.0,
    may: 25.0,
    june: 33.0,
    july: 33.0,
    aug: 33.0,
    sep: 33.0,
    oct: 33.0,
    nov: 33.0,
    dec: 33.0,
    jan: 33.0,
    feb: 33.0,
    march: 33.0,
  },
  {
    id: 2,
    utility: "POWERGEN",
    plant: "NMD - Power Plant 1",
    uom: "MW",
    april: 12.81,
    may: 7.63,
    june: 8.14,
    july: 9.45,
    aug: 10.79,
    sep: 9.89,
    oct: 7.76,
    nov: 9.94,
    dec: 7.94,
    jan: 10.63,
    feb: 11.03,
    march: 0,
  },
  {
    id: 3,
    utility: "POWERGEN",
    plant: "NMD - Power Plant 2",
    uom: "MW",
    april: 11.20,
    may: 11.60,
    june: null,
    july: 6.93,
    aug: null,
    sep: null,
    oct: null,
    nov: null,
    dec: null,
    jan: null,
    feb: null,
    march: null,
  },
  {
    id: 4,
    utility: "POWERGEN",
    plant: "NMD - Power Plant 3",
    uom: "MW",
    april: 12.57,
    may: null,
    june: 6.41,
    july: null,
    aug: 7.90,
    sep: 9.19,
    oct: 8.29,
    nov: 6.77,
    dec: 8.39,
    jan: 6.39,
    feb: 9.04,
    march: 9.44,
  },
  {
    id: 5,
    utility: "POWERGEN",
    plant: "NMD - STG Power Plant",
    uom: "MW",
    april: 14.21,
    may: 14.21,
    june: 12.24,
    july: 12.30,
    aug: 11.92,
    sep: 11.84,
    oct: 11.92,
    nov: 12.24,
    dec: 11.92,
    jan: 11.92,
    feb: 11.67,
    march: 11.92,
  }
];


const ImportPower = () => {
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
  const [rows, setRows] = useState([])
  const valueFormat= ValueFormatterProduction()

  const dummyRowsData=[
    {
      id: 1,
      utility: "Power from MEL",
      assetName: "NMD-Rev Proc",
      uom: "MW",
      [headerMap[4]]: 25.00,
      [headerMap[5]]: 25.00,
      [headerMap[6]]: 33.00,
      [headerMap[7]]: 33.00,
      [headerMap[8]]: 33.00,
      [headerMap[9]]: 33.00,
      [headerMap[10]]: 33.00,
      [headerMap[11]]: 33.00,
      [headerMap[12]]: 33.00,
      [headerMap[1]]: 33.00,
      [headerMap[2]]: 33.00,
      [headerMap[3]]: 33.00,
    },
  ]

  // Column definitions
  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'assetName',
      title: 'Plant',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'utility',
      title: 'Utility',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    // {
    //   field: 'material',
    //   title: 'Material',
    //   width: 150,
    //   minWidth: 150,
    //   type: 'text',
    //   editable: false,
    // },
    { field: 'uom', title: 'UOM', widthT: 60, minWidth: 80, type: 'text', editable: false },
    {
      // field: 'april',
      field:headerMap[4],
      title: headerMap[4], // will be 'Apr-25' if AOP_YEAR is 2025-26
      editable: true, 
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'may',
      field: headerMap[5],
      title: headerMap[5],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'june',
      field: headerMap[6],
      title: headerMap[6],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'july',
      field: headerMap[7],
      title: headerMap[7],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'aug',
      field: headerMap[8],
      title: headerMap[8],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'sep',
      field: headerMap[9],
      title: headerMap[9],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'oct',
      field: headerMap[10],
      title: headerMap[10],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'nov',
      field: headerMap[11],
      title: headerMap[11],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'dec',
      field: headerMap[12],
      title: headerMap[12],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'jan',
      field: headerMap[1],
      title: headerMap[1],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'feb',
      field: headerMap[2],
      title: headerMap[2],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      // field: 'march',
      field: headerMap[3],
      title: headerMap[3],
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
  ]

  useEffect(() => {
    if(AOP_YEAR){
      // fetchImportConsumptionData(keycloak, AOP_YEAR)
      setRows(dummyRowsData);
    }
  }, [AOP_YEAR])

  const fetchImportConsumptionData = async (keycloak, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getImportConsumptionData(
        keycloak,
        AOP_YEAR,
      )
      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      // Process and set the fetched data to rows
      let tempRes=res?.data.map((item, index)=>{
        return { ...item, id: index + 1 }
      })
      setRows(tempRes)
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
      // setRows(dummyRowsData)
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
    titleName: screenTitle?.title,
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
    const payload = modifiedData.map((item) => {
      const { inEdit, ...rest } = item
      return rest
    })
    const tempPayload = JSON.stringify(payload)
     try {
      // Transform modifiedCells into the format expected by the API
      console.log('modifiedData', modifiedData)

      // Call the API to save changes
      const response = await UtilityPlantApiServiceV2.saveImportConsumptionData(
        keycloak,
        PLANT_ID,
        tempPayload
      )
      console.log('response',response)
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
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Purchase Power Input'
        permissions={permissions}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        //groupBy="plant"
      />

  
    </Box>
  )
}

export default ImportPower
