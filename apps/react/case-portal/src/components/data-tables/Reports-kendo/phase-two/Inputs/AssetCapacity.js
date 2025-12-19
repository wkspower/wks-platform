import React, { useEffect, useState } from 'react'
import KendoDataTables from 'components/kendo-data-tables/index'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { Stack } from '../../../../../../node_modules/@mui/material/index'

const dummyRowsData = [
  {
    id: 1,
    generatingPlant: "40NB - NMD - Power Plant 1",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    april: { minCapacity: 10.0, maxCapacity: 20.0 },
    may: { minCapacity: 11.0, maxCapacity: 21.0 },
    june: { minCapacity: 12.0, maxCapacity: 22.0 },
    july: { minCapacity: 13.0, maxCapacity: 23.0 },
    aug: { minCapacity: 14.0, maxCapacity: 24.0 },
    sep: { minCapacity: 13.5, maxCapacity: 23.5 },
    oct: { minCapacity: 12.5, maxCapacity: 22.5 },
    nov: { minCapacity: 11.5, maxCapacity: 21.5 },
    dec: { minCapacity: 10.5, maxCapacity: 20.5 },
    jan: { minCapacity: 9.5, maxCapacity: 19.5 },
    feb: { minCapacity: 9.0, maxCapacity: 19.0 },
    march: { minCapacity: 10.0, maxCapacity: 20.0 },
  },
  {
    id: 2,
    generatingPlant: "40NC - NMD - Power Plant 2",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    april: { minCapacity: 8.0, maxCapacity: 18.0 },
    may: { minCapacity: 9.0, maxCapacity: 19.0 },
    june: { minCapacity: 10.0, maxCapacity: 20.0 },
    july: { minCapacity: 11.0, maxCapacity: 21.0 },
    aug: { minCapacity: 12.0, maxCapacity: 22.0 },
    sep: { minCapacity: 11.5, maxCapacity: 21.5 },
    oct: { minCapacity: 10.5, maxCapacity: 20.5 },
    nov: { minCapacity: 9.5, maxCapacity: 19.5 },
    dec: { minCapacity: 8.5, maxCapacity: 18.5 },
    jan: { minCapacity: 7.5, maxCapacity: 17.5 },
    feb: { minCapacity: 7.0, maxCapacity: 17.0 },
    march: { minCapacity: 8.0, maxCapacity: 18.0 },
  },
  {
    id: 3,
    generatingPlant: "40ND - NMD - Power Plant 3",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    april: { minCapacity: 6.0, maxCapacity: 16.0 },
    may: { minCapacity: 7.0, maxCapacity: 17.0 },
    june: { minCapacity: 8.0, maxCapacity: 18.0 },
    july: { minCapacity: 9.0, maxCapacity: 19.0 },
    aug: { minCapacity: 10.0, maxCapacity: 20.0 },
    sep: { minCapacity: 9.5, maxCapacity: 19.5 },
    oct: { minCapacity: 8.5, maxCapacity: 18.5 },
    nov: { minCapacity: 7.5, maxCapacity: 17.5 },
    dec: { minCapacity: 6.5, maxCapacity: 16.5 },
    jan: { minCapacity: 5.5, maxCapacity: 15.5 },
    feb: { minCapacity: 5.0, maxCapacity: 15.0 },
    march: { minCapacity: 6.0, maxCapacity: 16.0 },
  },
  {
    id: 4,
    generatingPlant: "40NE - NMD - STG Power Plant",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    uom: "MW",
    april: { minCapacity: 15.0, maxCapacity: 25.0 },
    may: { minCapacity: 16.0, maxCapacity: 26.0 },
    june: { minCapacity: 17.0, maxCapacity: 27.0 },
    july: { minCapacity: 18.0, maxCapacity: 28.0 },
    aug: { minCapacity: 19.0, maxCapacity: 29.0 },
    sep: { minCapacity: 18.5, maxCapacity: 28.5 },
    oct: { minCapacity: 17.5, maxCapacity: 27.5 },
    nov: { minCapacity: 16.5, maxCapacity: 26.5 },
    dec: { minCapacity: 15.5, maxCapacity: 25.5 },
    jan: { minCapacity: 14.5, maxCapacity: 24.5 },
    feb: { minCapacity: 14.0, maxCapacity: 24.0 },
    march: { minCapacity: 15.0, maxCapacity: 25.0 },
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
      locked: true,
    },
    {
      field: 'utilityDistributed',
      title: 'Utility Distributed',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated',
      title: 'Utility Generated',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'distributionPlant',
      title: 'Distribution Plant',
      width: 180,
      minWidth: 180,
      type: 'text',
      editable: false,
      locked: true,
    },
    { field: 'uom', title: 'UOM', width: 80, minWidth: 80, type: 'text', editable: false },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'april.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'may.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'june.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'july.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'aug.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'sep.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'oct.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'nov.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'dec.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'jan.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'feb.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.minCapacity',
          title: 'Min Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
        {
          field: 'march.maxCapacity',
          title: 'Max Capacity',
          width: 80,
          editable: true,
          type: 'number1',
          format: valueFormat,
        },
      ],
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

    const payload = modifiedData

    try {
      console.log('payload', payload)

      const response = await UtilityPlantApiServiceV2.saveAssetCapacityData(
        keycloak,
        PLANT_ID,
        payload
      )

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
      <NestedKendoTable
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