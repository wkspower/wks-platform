import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { Stack } from '../../../../../../node_modules/@mui/material/index'

const STGShutdownAndOperationalHr = ({ hoursRows = [] }) => {
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year, screenTitle } =
    dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const valueFormat = ValueFormatterProduction()

  // const dummySTGData = [
  //   {
  //     id: 2,
  //     assetName: 'HRSG1_SHP STEAM',
  //     assetType: 'SHP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 0, netOperationHrs: 720 },
  //     july: { shutdownHrs: 0, netOperationHrs: 744 },
  //     aug: { shutdownHrs: 0, netOperationHrs: 744 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  //   {
  //     id: 3,
  //     assetName: 'HRSG2_SHP STEAM',
  //     assetType: 'SHP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 720, netOperationHrs: 0 },
  //     july: { shutdownHrs: 0, netOperationHrs: 744 },
  //     aug: { shutdownHrs: 744, netOperationHrs: 0 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  //   {
  //     id: 4,
  //     assetName: 'HRSG3_SHP STEAM',
  //     assetType: 'SHP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 744, netOperationHrs: 0 },
  //     june: { shutdownHrs: 0, netOperationHrs: 720 },
  //     july: { shutdownHrs: 744, netOperationHrs: 0 },
  //     aug: { shutdownHrs: 0, netOperationHrs: 744 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  //   {
  //     id: 6,
  //     assetName: 'HP Steam PRDS',
  //     assetType: 'HP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 0, netOperationHrs: 720 },
  //     july: { shutdownHrs: 0, netOperationHrs: 744 },
  //     aug: { shutdownHrs: 0, netOperationHrs: 744 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  
  //   {
  //     id: 8,
  //     assetName: 'STG1_MP STEAM',
  //     assetType: 'MP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 100, netOperationHrs: 620 },
  //     july: { shutdownHrs: 100, netOperationHrs: 644 },
  //     aug: { shutdownHrs: 120, netOperationHrs: 624 },
  //     sep: { shutdownHrs: 120, netOperationHrs: 600 },
  //     oct: { shutdownHrs: 120, netOperationHrs: 624 },
  //     nov: { shutdownHrs: 100, netOperationHrs: 620 },
  //     dec: { shutdownHrs: 120, netOperationHrs: 624 },
  //     jan: { shutdownHrs: 120, netOperationHrs: 624 },
  //     feb: { shutdownHrs: 120, netOperationHrs: 552 },
  //     march: { shutdownHrs: 120, netOperationHrs: 624 },
  //   },
  //   {
  //     id: 9,
  //     assetName: 'MP Steam PRDS SHP',
  //     assetType: 'MP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 0, netOperationHrs: 720 },
  //     july: { shutdownHrs: 0, netOperationHrs: 744 },
  //     aug: { shutdownHrs: 0, netOperationHrs: 744 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  //   {
  //     id: 11,
  //     assetName: 'STG1_LP STEAM',
  //     assetType: 'LP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 100, netOperationHrs: 620 },
  //     july: { shutdownHrs: 100, netOperationHrs: 644 },
  //     aug: { shutdownHrs: 120, netOperationHrs: 624 },
  //     sep: { shutdownHrs: 120, netOperationHrs: 600 },
  //     oct: { shutdownHrs: 120, netOperationHrs: 624 },
  //     nov: { shutdownHrs: 100, netOperationHrs: 620 },
  //     dec: { shutdownHrs: 120, netOperationHrs: 624 },
  //     jan: { shutdownHrs: 120, netOperationHrs: 624 },
  //     feb: { shutdownHrs: 120, netOperationHrs: 552 },
  //     march: { shutdownHrs: 120, netOperationHrs: 624 },
  //   },
  //   {
  //     id: 12,
  //     assetName: 'LP Steam PRDS',
  //     assetType: 'LP Steam_Dis',
  //     april: { shutdownHrs: 0, netOperationHrs: 720 },
  //     may: { shutdownHrs: 0, netOperationHrs: 744 },
  //     june: { shutdownHrs: 0, netOperationHrs: 720 },
  //     july: { shutdownHrs: 0, netOperationHrs: 744 },
  //     aug: { shutdownHrs: 0, netOperationHrs: 744 },
  //     sep: { shutdownHrs: 0, netOperationHrs: 720 },
  //     oct: { shutdownHrs: 0, netOperationHrs: 744 },
  //     nov: { shutdownHrs: 0, netOperationHrs: 720 },
  //     dec: { shutdownHrs: 0, netOperationHrs: 744 },
  //     jan: { shutdownHrs: 0, netOperationHrs: 744 },
  //     feb: { shutdownHrs: 0, netOperationHrs: 672 },
  //     march: { shutdownHrs: 0, netOperationHrs: 744 },
  //   },
  // ]

  const dummySTGData =[
    {
        "assetName": "NMD-Power Plant-1",
        "assetId": "6e83b022-8950-434a-a80c-2db532ff6526",
        "assetType": "GT",
        "utilityDistributed": [
            {
                "name": "Power_Dis",
                "sapCode": "310027910"
            }
        ],
        "utilityGenerated": [
            {
                "name": "POWERGEN",
                "sapCode": "310027907"
            }
        ],
        "april": {
            "netOperationHrs": 0.0,
            "shutdownHrs": 720.0
        },
        "may": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "june": {
            "netOperationHrs": 720.0,
            "shutdownHrs": 0.0
        },
        "july": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "aug": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "sep": {
            "netOperationHrs": 720.0,
            "shutdownHrs": 0.0
        },
        "oct": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "nov": {
            "netOperationHrs": 710.0,
            "shutdownHrs": 10.0
        },
        "dec": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "jan": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        },
        "feb": {
            "netOperationHrs": 672.0,
            "shutdownHrs": 0.0
        },
        "march": {
            "netOperationHrs": 744.0,
            "shutdownHrs": 0.0
        }
    },
  ]

  const nestedColumns = [
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },

    {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Distributed SAP Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Generated SAP Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    

    {
      field: 'assetType',
      title: 'Asset Type',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
      hidden:true
    },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'april.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'may.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'june.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'july.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'aug.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'sep.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'oct.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'nov.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'dec.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'jan.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'feb.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'march.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchShutdownAndOperationalData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchShutdownAndOperationalData = async () => {
    setLoading(true)
    try {
      // TODO: Replace with actual API call
      const res = await InputApiService.getOperationHoursData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      
      // Using dummy data for now
      const transformedData = res?.steamResponse?.map(row => ({
        ...row,
        // utilityGenerated: row.utilityGenerated?.[0] || {},
        // utilityDistributed: row.utilityDistributed?.[0] || {},
      }))
      setRows(transformedData)
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
    showTitle: false,
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

    const payload = modifiedData.map(({ id, inEdit, ...rest }) => {
      // Convert utilityGenerated and utilityDistributed back to arrays
      const transformed = { ...rest }
      return transformed
    })
     const tempPayload = {
      steamResponse: payload,
    }

    try {
      console.log('tempPayload', tempPayload)

      const response = await InputApiService.saveOperationHours(
        keycloak,
        AOP_YEAR,
        tempPayload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving operational hours data:', error)
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
      <Stack>
        <NestedKendoTable
          columns={nestedColumns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          groupBy={['assetType']}
          hoursRows={hoursRows}
        />
      </Stack>
    </Box>
  )
}

export default STGShutdownAndOperationalHr
