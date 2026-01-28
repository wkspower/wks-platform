import { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'components/aop-phase-two/services/cpp/utilityPlantApiServiceV2'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import AdvanceKendoTable from '../common/AdvanceKendoTable/index'
import { Stack } from '../../../../node_modules/@mui/material/index'

const data = [
  {
    id: 1,
    receiverUtility: 'Boiler Feed Water',
    receiverUtilityId: '310027927',
    receiverCostCenter: 'NG-Boiler Feed Water',
    receiverCostCenterId: 'RIL_10708017',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Boiler Feed Water',
    senderCostCenterId: 'RIL_10708017',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Boiler Feed Water',
    utilityId: '310027927',
  },
  {
    id: 2,
    receiverUtility: 'COMPRESSED AIR',
    receiverUtilityId: '310027904',
    receiverCostCenter: 'NG-Compressed Air',
    receiverCostCenterId: 'RIL_10708018',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Compressed Air',
    senderCostCenterId: 'RIL_10708018',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'COMPRESSED AIR',
    utilityId: '310027904',
  },
  {
    id: 3,
    receiverUtility: 'Cooling Water 1',
    receiverUtilityId: '310028005',
    receiverCostCenter: 'NG-Cooling Water - 1',
    receiverCostCenterId: 'RIL_10708014',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Cooling Water - 1',
    senderCostCenterId: 'RIL_10708014',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Cooling Water 1',
    utilityId: '310028005',
  },
  {
    id: 4,
    receiverUtility: 'Cooling Water 2',
    receiverUtilityId: '310028004',
    receiverCostCenter: 'NG-Cooling Water - 2',
    receiverCostCenterId: 'RIL_10708016',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Cooling Water - 2',
    senderCostCenterId: 'RIL_10708016',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Cooling Water 2',
    utilityId: '310028004',
  },
  {
    id: 5,
    receiverUtility: 'D M Water',
    receiverUtilityId: '310027966',
    receiverCostCenter: 'NG-DM Water',
    receiverCostCenterId: 'RIL_10708015',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-DM Water',
    senderCostCenterId: 'RIL_10708015',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'D M Water',
    utilityId: '310027966',
  },
  {
    id: 6,
    receiverUtility: 'D M Water',
    receiverUtilityId: '310027966',
    receiverCostCenter: 'NG-Steam Clearing',
    receiverCostCenterId: 'RIL_10713002',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-Steam Clearing',
    senderCostCenterId: 'RIL_10713002',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'D M Water',
    utilityId: '310027966',
  },
  {
    id: 7,
    receiverUtility: 'Effluent Treated',
    receiverUtilityId: '310027994',
    receiverCostCenter: 'NG-ETP',
    receiverCostCenterId: 'RIL_10708019',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-ETP',
    senderCostCenterId: 'RIL_10708019',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Effluent Treated',
    utilityId: '310027994',
  },
  {
    id: 8,
    receiverUtility: 'HP Steam_Dis',
    receiverUtilityId: '310027939',
    receiverCostCenter: 'NG-HP Steam',
    receiverCostCenterId: 'RIL_10708010',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-HP Steam',
    senderCostCenterId: 'RIL_10708010',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'HP Steam_Dis',
    utilityId: '310027939',
  },
  {
    id: 9,
    receiverUtility: 'HRSG1_SHP STEAM',
    receiverUtilityId: '310027926',
    receiverCostCenter: 'NG-HRSG 1-Steam',
    receiverCostCenterId: 'RIL_10708005',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-HRSG 1-Steam',
    senderCostCenterId: 'RIL_10708005',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'HRSG1_SHP STEAM',
    utilityId: '310027926',
  },
  {
    id: 10,
    receiverUtility: 'HRSG2_SHP STEAM',
    receiverUtilityId: '310027929',
    receiverCostCenter: 'NG-HRSG 2-Steam',
    receiverCostCenterId: 'RIL_10708006',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-HRSG 2-Steam',
    senderCostCenterId: 'RIL_10708006',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'HRSG2_SHP STEAM',
    utilityId: '310027929',
  },
  {
    id: 11,
    receiverUtility: 'HRSG3_SHP STEAM',
    receiverUtilityId: '310027930',
    receiverCostCenter: 'NG-HRSG 3-Steam',
    receiverCostCenterId: 'RIL_10708007',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-HRSG 3-Steam',
    senderCostCenterId: 'RIL_10708007',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'HRSG3_SHP STEAM',
    utilityId: '310027930',
  },
  {
    id: 12,
    receiverUtility: 'LP Steam_Dis',
    receiverUtilityId: '310027965',
    receiverCostCenter: 'NG-LP Steam',
    receiverCostCenterId: 'RIL_10708012',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-LP Steam',
    senderCostCenterId: 'RIL_10708012',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'LP Steam_Dis',
    utilityId: '310027965',
  },
  {
    id: 13,
    receiverUtility: 'MP Steam_Dis',
    receiverUtilityId: '310027940',
    receiverCostCenter: 'NG-MP Steam',
    receiverCostCenterId: 'RIL_10708011',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-MP Steam',
    senderCostCenterId: 'RIL_10708011',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'MP Steam_Dis',
    utilityId: '310027940',
  },
  {
    id: 14,
    receiverUtility: 'Nitrogen Gas',
    receiverUtilityId: 'NITROGENG',
    receiverCostCenter: 'NG-Nitrogen/ Oxygen',
    receiverCostCenterId: 'RIL_10708020',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Nitrogen/ Oxygen',
    senderCostCenterId: 'RIL_10708020',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Nitrogen Gas',
    utilityId: 'NITROGENG',
  },
  {
    id: 15,
    receiverUtility: 'Oxygen',
    receiverUtilityId: 'OXYGEN',
    receiverCostCenter: 'NG-Nitrogen/ Oxygen',
    receiverCostCenterId: 'RIL_10708020',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Nitrogen/ Oxygen',
    senderCostCenterId: 'RIL_10708020',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Oxygen',
    utilityId: 'OXYGEN',
  },
  {
    id: 16,
    receiverUtility: 'Power',
    receiverUtilityId: 'POWER',
    receiverCostCenter: 'NG-Power Purchase',
    receiverCostCenterId: 'RIL_10708000',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-Power Purchase',
    senderCostCenterId: 'RIL_10708000',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'Power',
    utilityId: 'POWER',
  },
  {
    id: 17,
    receiverUtility: 'Power_Dis',
    receiverUtilityId: '310027910',
    receiverCostCenter: 'NG-Power Clearing',
    receiverCostCenterId: 'RIL_10713000',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-Power Clearing',
    senderCostCenterId: 'RIL_10713000',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'Power_Dis',
    utilityId: '310027910',
  },
  {
    id: 18,
    receiverUtility: 'POWERGEN',
    receiverUtilityId: '310027907',
    receiverCostCenter: 'NG-GT1-Process',
    receiverCostCenterId: 'RIL_10709000',
    receiverPlant: 'NMD - Power Plant 1',
    receiverPlantId: '40NB',
    senderCostCenter: 'NG-GT1-Process',
    senderCostCenterId: 'RIL_10709000',
    senderPlant: 'NMD - Power Plant 1',
    senderPlantId: '40NB',
    utility: 'POWERGEN',
    utilityId: '310027907',
  },
  {
    id: 19,
    receiverUtility: 'POWERGEN',
    receiverUtilityId: '310027907',
    receiverCostCenter: 'NG-GT2-Process',
    receiverCostCenterId: 'RIL_10710000',
    receiverPlant: 'NMD - Power Plant 2',
    receiverPlantId: '40NC',
    senderCostCenter: 'NG-GT2-Process',
    senderCostCenterId: 'RIL_10710000',
    senderPlant: 'NMD - Power Plant 2',
    senderPlantId: '40NC',
    utility: 'POWERGEN',
    utilityId: '310027907',
  },
  {
    id: 20,
    receiverUtility: 'POWERGEN',
    receiverUtilityId: '310027907',
    receiverCostCenter: 'NG-GT3-Process',
    receiverCostCenterId: 'RIL_10711000',
    receiverPlant: 'NMD - Power Plant 3',
    receiverPlantId: '40ND',
    senderCostCenter: 'NG-GT3-Process',
    senderCostCenterId: 'RIL_10711000',
    senderPlant: 'NMD - Power Plant 3',
    senderPlantId: '40ND',
    utility: 'POWERGEN',
    utilityId: '310027907',
  },
  {
    id: 21,
    receiverUtility: 'POWERGEN',
    receiverUtilityId: '310027907',
    receiverCostCenter: 'NG-STG 1-Process',
    receiverCostCenterId: 'RIL_10712000',
    receiverPlant: 'NMD - STG Power Plant',
    receiverPlantId: '40NE',
    senderCostCenter: 'NG-STG 1-Process',
    senderCostCenterId: 'RIL_10712000',
    senderPlant: 'NMD - STG Power Plant',
    senderPlantId: '40NE',
    utility: 'POWERGEN',
    utilityId: '310027907',
  },
  {
    id: 22,
    receiverUtility: 'SHP Steam_Dis',
    receiverUtilityId: '310027924',
    receiverCostCenter: 'NG-Steam Clearing',
    receiverCostCenterId: 'RIL_10713002',
    receiverPlant: 'NMD - Utility/Power Dist',
    receiverPlantId: '40NG',
    senderCostCenter: 'NG-Steam Clearing',
    senderCostCenterId: 'RIL_10713002',
    senderPlant: 'NMD - Utility/Power Dist',
    senderPlantId: '40NG',
    utility: 'SHP Steam_Dis',
    utilityId: '310027924',
  },
  {
    id: 23,
    receiverUtility: 'STG1_LP STEAM',
    receiverUtilityId: '310028010',
    receiverCostCenter: 'NG-STG 1-Steam',
    receiverCostCenterId: 'RIL_10708008',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-STG 1-Steam',
    senderCostCenterId: 'RIL_10708008',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'STG1_LP STEAM',
    utilityId: '310028010',
  },
  {
    id: 24,
    receiverUtility: 'STG1_MP STEAM',
    receiverUtilityId: '310027952',
    receiverCostCenter: 'NG-STG 1-Steam',
    receiverCostCenterId: 'RIL_10708008',
    receiverPlant: 'NMD - Utility Plant',
    receiverPlantId: '40NF',
    senderCostCenter: 'NG-STG 1-Steam',
    senderCostCenterId: 'RIL_10708008',
    senderPlant: 'NMD - Utility Plant',
    senderPlantId: '40NF',
    utility: 'STG1_MP STEAM',
    utilityId: '310027952',
  },
  {
    id: 25,
    receiverUtility: 'Water',
    receiverUtilityId: 'RAW WATER',
    receiverCostCenter: 'NG-Site Common',
    receiverCostCenterId: 'RIL_10799000',
    receiverPlant: 'NMD-Rev Proc',
    receiverPlantId: '40N0',
    senderCostCenter: 'NG-Site Common',
    senderCostCenterId: 'RIL_10799000',
    senderPlant: 'NMD-Rev Proc',
    senderPlantId: '40N0',
    utility: 'Water',
    utilityId: 'RAW WATER',
  },
]
const SenderReceiverMapping = () => {
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchPlantRequirementData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {
      /*
      const res = await UtilityPlantApiServiceV2.getPlantRequirementData(
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
      */
      const res = data

      console.log('res', res)
      const formattedData = res?.map((item, index) => ({
        ...item,
        remarks: item.remarks || '',
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Column definitions
  const columns = [
    {
      field: 'receiverUtility',
      title: 'Receiver Utility',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
      hidden: false,
    },
    {
      field: 'receiverUtilityId',
      title: 'Receiver Utility ID',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
      hidden: false,
    },
    {
      field: 'receiverCostCenter',
      title: 'Receiver Cost Center',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverCostCenterId',
      title: 'Receiver Cost Center ID',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverPlant',
      title: 'Receiver Plant',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: true,
    },
    {
      field: 'receiverPlantId',
      title: 'Receiver Plant ID',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderCostCenter',
      title: 'Sender Cost Center',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderCostCenterId',
      title: 'Sender Cost Center ID',
      widthT: 180,
      minWidth: 180,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderPlant',
      title: 'Sender Plant',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: true,
    },
    {
      field: 'senderPlantId',
      title: 'Sender Plant ID',
      widthT: 130,
      minWidth: 130,
      type: 'text',
      editable: true,
    },
    {
      field: 'utility',
      title: 'Utility',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: true,
    },
    {
      field: 'utilityId',
      title: 'Utility ID',
      widthT: 120,
      minWidth: 120,
      type: 'text',
      editable: true,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 150,
      minWidth: 150,
      type: 'textarea',
      editable: true,
    },
  ]

  // Permissions
  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    downloadExcelBtnFromUI: true,
    ExcelName: 'Sender Receiver Mapping',
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Sender Receiver Mapping (Utility for Utility)',
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })
    // Add export logic here
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
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          columns={columns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title={permissions.showTitle ? permissions.titleName : ''}
          permissions={permissions}
          handleExport={handleExport}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          customHeight={80}
          paginationConfig={{
            threshold: 100,
            buttonCount: 5,
            pageSizes: [10, 20, 50, 100],
            defaultPageSize: 100,
          }}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
        />
      </Stack>
    </Box>
  )
}

export default SenderReceiverMapping
