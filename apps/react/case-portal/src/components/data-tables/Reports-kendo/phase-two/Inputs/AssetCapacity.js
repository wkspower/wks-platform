import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { Stack } from '../../../../../../node_modules/@mui/material/index'

const dummyRowsData = [
  {
    id: 1,
    generatingPlant: "40NB - NMD - Power Plant 1",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    distributionNormMapping: 1.00,
    minCapacity: null,
    maxCapacity: 22.00,
    april: 15.0,
    may: 16.0,
    june: 17.0,
    july: 18.0,
    aug: 19.0,
    sep: 18.5,
    oct: 17.5,
    nov: 16.5,
    dec: 15.5,
    jan: 14.5,
    feb: 14.0,
    march: 15.0,
  },
  {
    id: 2,
    generatingPlant: "40NC - NMD - Power Plant 2",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    distributionNormMapping: 1.00,
    minCapacity: null,
    maxCapacity: 22.00,
    april: 12.0,
    may: 13.0,
    june: 14.0,
    july: 15.0,
    aug: 16.0,
    sep: 15.5,
    oct: 14.5,
    nov: 13.5,
    dec: 12.5,
    jan: 11.5,
    feb: 11.0,
    march: 12.0,
  },
  {
    id: 3,
    generatingPlant: "40ND - NMD - Power Plant 3",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    distributionNormMapping: 1.00,
    minCapacity: null,
    maxCapacity: 22.00,
    april: 10.0,
    may: 11.0,
    june: 12.0,
    july: 13.0,
    aug: 14.0,
    sep: 13.5,
    oct: 12.5,
    nov: 11.5,
    dec: 10.5,
    jan: 9.5,
    feb: 9.0,
    march: 10.0,
  },
  {
    id: 4,
    generatingPlant: "40NE - NMD - STG Power Plant",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    distributionNormMapping: 1.00,
    minCapacity: 5.00,
    maxCapacity: 25.00,
    april: 20.0,
    may: 21.0,
    june: 22.0,
    july: 23.0,
    aug: 24.0,
    sep: 23.5,
    oct: 22.5,
    nov: 21.5,
    dec: 20.5,
    jan: 19.5,
    feb: 19.0,
    march: 20.0,
  }
];

const AssetCapacity = () => {
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
  const valueFormat = ValueFormatterProduction()

  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'generatingPlant',
      title: 'Generating Plant',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
    },
    {
      field: 'utilityDistributed',
      title: 'Utility Distributed',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
    },
    {
      field: 'utilityGenerated',
      title: 'Utility Generated',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
    },
    {
      field: 'distributionPlant',
      title: 'Distribution Plant',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
    },
    { field: 'uom', title: 'UOM', widthT: 60, minWidth: 80, type: 'text', editable: false },
    {
      field: 'distributionNormMapping',
      title: 'Distribution Norm Mapping',
      width: 150,
      minWidth: 120,
      type: 'number',
      editable: false,
    },
    {
      field: 'minCapacity',
      title: 'Min Capacity',
      width: 120,
      minWidth: 100,
      type: 'number1',
      editable: true,
    },
    {
      field: 'maxCapacity',
      title: 'Max Capacity',
      width: 120,
      minWidth: 100,
      type: 'number1',
      editable: true,
    },
    {
      field: 'april',
      title: headerMap[4],
      editable: true,
      widthT: 100,
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
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
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'march',
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
    if (AOP_YEAR) {
    //   fetchAssetCapacityData(keycloak, AOP_YEAR)
    setRows(dummyRowsData)
    }
  }, [AOP_YEAR])

  const fetchAssetCapacityData = async (keycloak, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getAssetCapacityData(
        keycloak,
        AOP_YEAR,
      )
      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.data.map((item, index) => {
        return { ...item, id: index + 1 }
      })
      setRows(tempRes)
    } catch (error) {
      console.error('Error fetching asset capacity data:', error)
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
    showTitle: true,
    titleName: screenTitle?.title,
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
    const payload = modifiedData.map((item) => {
      const { inEdit, ...rest } = item
      return rest
    })
    const tempPayload = JSON.stringify(payload)
    try {
      console.log('modifiedData', modifiedData)

      const response = await UtilityPlantApiServiceV2.saveAssetCapacityData(
        keycloak,
        PLANT_ID,
        tempPayload
      )
      console.log('response', response)
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving asset capacity data:', error)
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
        title='Asset Capacity Input'
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

export default AssetCapacity