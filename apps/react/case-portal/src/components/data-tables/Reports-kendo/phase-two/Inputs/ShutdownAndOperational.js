import React, { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
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
    apr_shutdownHrs: 720.00,
    apr_netOperatingHrs: 0.00,
    may_shutdownHrs: 744.00,
    may_netOperatingHrs: null,
    jun_shutdownHrs: 720.00,
    jun_netOperatingHrs: null,
    jul_shutdownHrs: 744.00,
    jul_netOperatingHrs: null,
    aug_shutdownHrs: 744.00,
    aug_netOperatingHrs: null,
    sep_shutdownHrs: 720.00,
    sep_netOperatingHrs: null,
    oct_shutdownHrs: 744.00,
    oct_netOperatingHrs: null,
    nov_shutdownHrs: 720.00,
    nov_netOperatingHrs: null,
    dec_shutdownHrs: 744.00,
    dec_netOperatingHrs: null,
    jan_shutdownHrs: 744.00,
    jan_netOperatingHrs: null,
    feb_shutdownHrs: 672.00,
    feb_netOperatingHrs: null,
    mar_shutdownHrs: 744.00,
    mar_netOperatingHrs: null,
  },
  {
    id: 2,
    generatingPlant: "40NC - NMD - Power Plant 2",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    apr_shutdownHrs: 720.00,
    apr_netOperatingHrs: null,
    may_shutdownHrs: 744.00,
    may_netOperatingHrs: null,
    jun_shutdownHrs: 720.00,
    jun_netOperatingHrs: 0.00,
    jul_shutdownHrs: 744.00,
    jul_netOperatingHrs: null,
    aug_shutdownHrs: 744.00,
    aug_netOperatingHrs: 0.00,
    sep_shutdownHrs: 720.00,
    sep_netOperatingHrs: null,
    oct_shutdownHrs: 744.00,
    oct_netOperatingHrs: null,
    nov_shutdownHrs: 720.00,
    nov_netOperatingHrs: 0.00,
    dec_shutdownHrs: 744.00,
    dec_netOperatingHrs: null,
    jan_shutdownHrs: 744.00,
    jan_netOperatingHrs: 0.00,
    feb_shutdownHrs: 672.00,
    feb_netOperatingHrs: null,
    mar_shutdownHrs: 744.00,
    mar_netOperatingHrs: 0.00,
  },
  {
    id: 3,
    generatingPlant: "40ND - NMD - Power Plant 3",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    apr_shutdownHrs: 720.00,
    apr_netOperatingHrs: null,
    may_shutdownHrs: 744.00,
    may_netOperatingHrs: 0.00,
    jun_shutdownHrs: 720.00,
    jun_netOperatingHrs: null,
    jul_shutdownHrs: 744.00,
    jul_netOperatingHrs: null,
    aug_shutdownHrs: 744.00,
    aug_netOperatingHrs: null,
    sep_shutdownHrs: 720.00,
    sep_netOperatingHrs: null,
    oct_shutdownHrs: 744.00,
    oct_netOperatingHrs: null,
    nov_shutdownHrs: 720.00,
    nov_netOperatingHrs: null,
    dec_shutdownHrs: 744.00,
    dec_netOperatingHrs: null,
    jan_shutdownHrs: 744.00,
    jan_netOperatingHrs: null,
    feb_shutdownHrs: 672.00,
    feb_netOperatingHrs: null,
    mar_shutdownHrs: 744.00,
    mar_netOperatingHrs: null,
  },
  {
    id: 4,
    generatingPlant: "40N0 - NMD-Rev Proc",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "POWER - Power",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    apr_shutdownHrs: 720.00,
    apr_netOperatingHrs: null,
    may_shutdownHrs: 744.00,
    may_netOperatingHrs: null,
    jun_shutdownHrs: 720.00,
    jun_netOperatingHrs: null,
    jul_shutdownHrs: 744.00,
    jul_netOperatingHrs: null,
    aug_shutdownHrs: 744.00,
    aug_netOperatingHrs: null,
    sep_shutdownHrs: 720.00,
    sep_netOperatingHrs: null,
    oct_shutdownHrs: 744.00,
    oct_netOperatingHrs: null,
    nov_shutdownHrs: 720.00,
    nov_netOperatingHrs: null,
    dec_shutdownHrs: 744.00,
    dec_netOperatingHrs: null,
    jan_shutdownHrs: 744.00,
    jan_netOperatingHrs: null,
    feb_shutdownHrs: 672.00,
    feb_netOperatingHrs: null,
    mar_shutdownHrs: 744.00,
    mar_netOperatingHrs: null,
  },
  {
    id: 5,
    generatingPlant: "40NE - NMD - STG Power Plant",
    utilityDistributed: "310027910 - Power_Dis",
    utilityGenerated: "310027907 - POWERGEN",
    distributionPlant: "40NG - NMD - Utility/Power Dist",
    apr_shutdownHrs: 720.00,
    apr_netOperatingHrs: null,
    may_shutdownHrs: 744.00,
    may_netOperatingHrs: null,
    jun_shutdownHrs: 720.00,
    jun_netOperatingHrs: null,
    jul_shutdownHrs: 744.00,
    jul_netOperatingHrs: 100.00,
    aug_shutdownHrs: 744.00,
    aug_netOperatingHrs: 620.00,
    sep_shutdownHrs: 720.00,
    sep_netOperatingHrs: 100.00,
    oct_shutdownHrs: 744.00,
    oct_netOperatingHrs: 120.00,
    nov_shutdownHrs: 720.00,
    nov_netOperatingHrs: 120.00,
    dec_shutdownHrs: 744.00,
    dec_netOperatingHrs: 120.00,
    jan_shutdownHrs: 744.00,
    jan_netOperatingHrs: 100.00,
    feb_shutdownHrs: 672.00,
    feb_netOperatingHrs: 120.00,
    mar_shutdownHrs: 744.00,
    mar_netOperatingHrs: 120.00,
  }
];

const ShutdownAndOperational = () => {
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
    {
      title: headerMap[4],
      children: [
        { field: 'apr_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'apr_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[5],
      children: [
        { field: 'may_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'may_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[6],
      children: [
        { field: 'jun_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'jun_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title:    headerMap[7],
      children: [
        { field: 'jul_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'jul_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[8],
      children: [
        { field: 'aug_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'aug_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[9],
      children: [
        { field: 'sep_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'sep_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[10],
      children: [
        { field: 'oct_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'oct_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[11],
      children: [
        { field: 'nov_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'nov_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[12],
      children: [
        { field: 'dec_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'dec_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[1],
      children: [
        { field: 'jan_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'jan_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[2],
      children: [
        { field: 'feb_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'feb_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
    {
      title: headerMap[3],
      children: [
        { field: 'mar_shutdownHrs', title: 'Shutdown Hrs', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'mar_netOperatingHrs', title: 'Net Operating Hrs', width: 80, editable: false, type: 'number1', format: valueFormat },
      ],
    },
  ]

  useEffect(() => {
    if (AOP_YEAR) {
    //   fetchShutdownAndOperationalData(keycloak, AOP_YEAR)
    setRows(dummyRowsData);
    }
  }, [AOP_YEAR])

  const fetchShutdownAndOperationalData = async (keycloak, AOP_YEAR) => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getShutdownAndOperationalData(
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
      console.error('Error fetching shutdown and operational data:', error)
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

      const response = await UtilityPlantApiServiceV2.saveShutdownAndOperationalData(
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
      console.error('Error saving shutdown and operational data:', error)
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
        title='Shutdown and Operational Hours Input'
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

export default ShutdownAndOperational