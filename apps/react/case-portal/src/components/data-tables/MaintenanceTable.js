import { useState, useEffect } from 'react'

import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
// import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import ASDataGrid from './ASDataGrid'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { renderTwoLineEllipsis } from 'components/Utilities/twoLineEllipsisRenderer'
import { Tooltip } from '../../../node_modules/@mui/material/index'

const MaintenanceTable = () => {
  // const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  // const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState()

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  //const isOldYear = oldYear?.oldYear
  const isOldYear = oldYear?.oldYear

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getMaintenanceData(keycloak)
      var formattedData = []
      if (data) {
        formattedData = data?.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          isEditable: false,
        }))
      }
      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching  data:', error)
      setRows([])
      setLoading(false)
    }
  }

  const handleCalculate = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const data = await DataService.handleCalculateMaintenance(
        plantId,
        year,
        keycloak,
      )

      if (data == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    }
  }

  const formatValueToTwoDecimals = (params) => {
    if (!params && params !== 0) return ''

    const hours = Math.floor(params)
    const minutes = Math.abs(Math.floor((params - hours) * 60))

    return `${hours}:${minutes.toString().padStart(2, '0')}`
  }

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const productionColumns = [
    {
      field: 'Name',
      headerName: 'Description',
      align: 'left',
      headerAlign: 'left',
      minWidth: 250,
      renderCell: renderTwoLineEllipsis,
    },
    {
      field: 'April',
      headerName: headerMap[4],
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToTwoDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),

      // valueGetter: convertUnits,
    },

    {
      field: 'May',
      headerName: headerMap[5],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'June',
      headerName: headerMap[6],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'July',
      headerName: headerMap[7],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Aug',
      headerName: headerMap[8],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Sep',
      headerName: headerMap[9],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Oct',
      headerName: headerMap[10],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },

    {
      field: 'Nov',
      headerName: headerMap[11],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Dec',
      headerName: headerMap[12],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Jan',
      headerName: headerMap[1],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Feb',
      headerName: headerMap[2],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'Mar',
      headerName: headerMap[3],
      valueFormatter: formatValueToTwoDecimals,

      align: 'right',
      headerAlign: 'left',
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToTwoDecimals(params.value)}</span>
        </Tooltip>
      ),
    },
    {
      field: 'isEditable',
      headerName: 'isEditable',
    },
  ]

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      showRefresh: false,
      allAction: false,
    },
    isOldYear,
  )

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ASDataGrid
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        handleCalculate={handleCalculate}
        // apiRef={apiRef}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default MaintenanceTable
