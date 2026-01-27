import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import NestedKendoTable from '../../common/NestedKendoTable/index'
import { SummaryApiService } from '../../services/cpp/summaryApiService'

const AssetStatusList = ({ executionId, month, financialYear }) => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // Column definitions for asset status
  const columns = [
    {
      field: 'assetName',
      title: 'Asset Name',
      widthT: 200,
      minWidth: 200,
      type: 'text',
      editable: false,
    },
    {
      field: 'assetType',
      title: 'Asset Type',
      widthT: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
    },
    {
      field: 'status',
      title: 'Status',
      widthT: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
    },
    {
      field: 'priority',
      title: 'Priority',
      widthT: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'isAvailable',
      title: 'Is Available',
      widthT: 120,
      minWidth: 120,
      type: 'boolean',
      editable: false,
    },
    {
      field: 'operatingHours',
      title: 'Operating Hours',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'unit',
      title: 'UOM',
      widthT: 80,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'averageLoad',
      title: 'Average Load',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'generation',
      title: 'Generation',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
  ]

  useEffect(() => {
    if (executionId && month) {
      fetchAssetStatusDetails()
    }
  }, [executionId, month])

  const fetchAssetStatusDetails = async () => {
    setLoading(true)
    try {
      const res = await SummaryApiService.getAssetStatusDetails(
        keycloak,
        executionId,
        month,
      )

      // Extract assets from nested assetStatus.assets
      const assets = res?.assetStatus?.assets || []

      if (assets.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No asset data found', severity: 'info' })
        return
      }

      const formattedData = assets.map((item, index) => ({
        ...item,
        generation: item?.netMWh || item?.steamGenerationMT,
        averageLoad: item?.dispatchedLoadMW || item?.avgSteamGenRateMTPerHr,
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching asset status details:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const getMonthName = (month) => {
    const monthNames = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ]
    return monthNames[month - 1]
  }

  // Permissions for view-only grid
  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    showExport: false,
    ExcelName: 'Asset Status Details',
    showImport: false,
    showTitleNameBusiness: false,
    showTitle: true,
    titleName: `Asset Status ( ${getMonthName(month)}-${financialYear} )`,
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
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customHeight={40}
        READ_ONLY={true}
      />
    </Box>
  )
}

export default AssetStatusList
