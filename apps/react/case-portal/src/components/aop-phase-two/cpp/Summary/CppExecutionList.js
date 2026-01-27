import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { SummaryApiService } from '../../services/cpp/summaryApiService'
import { SvgIcon } from '@progress/kendo-react-common'
import { eyeIcon } from '@progress/kendo-svg-icons'
import { Tooltip } from '@progress/kendo-react-tooltip'

const CppExecutionList = ({ onViewClick }) => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // Column definitions
  const columns = [
    {
      field: 'id',
      title: 'ID',
      widthT: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
      hidden: true,
    },
    {
      field: 'financialYear',
      title: 'Financial Year',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      hidden: false,
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
      field: 'monthsSucceeded',
      title: 'Months Succeeded',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'monthsFailed',
      title: 'Months Failed',
      widthT: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
  ]

  useEffect(() => {
    fetchCppModelLogs()
  }, [])

  const data = [
    {
      id: 'ed724e8c-6f7a-4f0a-9a92-42fc401ac2ae',
      financialYear: 2025,
      executionDateTime: 'Jan 26, 2026, 8:47:21 PM',
      status: 'Success',
      totalIterations: 102,
      totalMonthsProcessed: 12,
      totalExecutionTime: '0.00s',
      monthsSucceeded: 12,
      monthsFailed: 0,
      monthsWithWarnings: 0,
    },
  ]

  const fetchCppModelLogs = async () => {
    setLoading(true)
    try {
      const res = await SummaryApiService.getCppModelLogs(keycloak)
      //   const res = data
      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const formattedData = res?.map((item, index) => ({
        ...item,
        id: item?.id || index + 1,
      }))
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching CPP model logs:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Permissions for view-only grid
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    showExport: false,
    ExcelName: 'CPP Model Logs',
    showImport: false,
    showTitleNameBusiness: false,
    showTitle: true,
    titleName: 'CPP Model Logs',
    customActionButton: true, // Enable custom action button
  }

  // Custom action cell with eye icon
  const CustomActionsCell = ({ dataItem }) => {
    return (
      <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
        <Tooltip anchorElement='target' position='top'>
          <SvgIcon
            icon={eyeIcon}
            themeColor='primary'
            style={{ cursor: 'pointer' }}
            onClick={() => handleViewClick(dataItem)}
            title='View'
          />
        </Tooltip>
      </td>
    )
  }

  const handleViewClick = (dataItem) => {
    console.log('View clicked for:', dataItem)
    if (onViewClick) {
      onViewClick(dataItem)
    } else {
      // Default behavior - you can customize this
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Viewing details for ${dataItem.financialYear}`,
        severity: 'info',
      })
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
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        // customHeight={40}
        paginationConfig={{
          threshold: 50,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 20,
        }}
        customActionCell={CustomActionsCell}
        READ_ONLY={true}
      />
    </Box>
  )
}

export default CppExecutionList
