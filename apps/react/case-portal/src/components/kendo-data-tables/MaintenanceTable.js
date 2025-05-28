import { useState, useEffect } from 'react'

import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { renderTwoLineEllipsis } from 'components/Utilities/twoLineEllipsisRenderer'
import KendoDataTables from './index'

const MaintenanceTable = () => {
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
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(2) : ''
  }

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const productionColumns = [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      width: 250,
      renderCell: renderTwoLineEllipsis,
    },
    {
      field: 'April',
      title: headerMap[4],
      align: 'right',
      headerAlign: 'left',
      valueFormatter: formatValueToTwoDecimals,
      width: 150,
      // valueGetter: convertUnits,
    },

    {
      field: 'May',
      title: headerMap[5],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'June',
      title: headerMap[6],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'July',
      title: headerMap[7],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Aug',
      title: headerMap[8],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Sep',
      title: headerMap[9],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Oct',
      title: headerMap[10],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },

    {
      field: 'Nov',
      title: headerMap[11],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Dec',
      title: headerMap[12],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Jan',
      title: headerMap[1],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Feb',
      title: headerMap[2],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Mar',
      title: headerMap[3],
      valueFormatter: formatValueToTwoDecimals,
      width: 150,

      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'isEditable',
      title: 'isEditable',
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
      <KendoDataTables
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
