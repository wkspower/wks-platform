import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import ASDataGrid from './ASDataGrid'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

const headerMap = generateHeaderNames()

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

const MaintenanceTable = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
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

  useEffect(() => {
    fetchData()
  }, [sitePlantChange, keycloak, lowerVertName])

  const productionColumns = [
    {
      field: 'Name',
      headerName: 'Description',
      align: 'left',
      headerAlign: 'left',
      minWidth: 250,
    },
    {
      field: 'April',
      headerName: headerMap[4],
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },

    {
      field: 'May',
      headerName: headerMap[5],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'June',
      headerName: headerMap[6],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'July',
      headerName: headerMap[7],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Aug',
      headerName: headerMap[8],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Sep',
      headerName: headerMap[9],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Oct',
      headerName: headerMap[10],

      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'Nov',
      headerName: headerMap[11],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Dec',
      headerName: headerMap[12],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Jan',
      headerName: headerMap[1],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Feb',
      headerName: headerMap[2],

      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'Mar',
      headerName: headerMap[3],
      align: 'left',
      headerAlign: 'left',
    },
  ]

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
        title='Maintenance Details'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        handleCalculate={handleCalculate}
        apiRef={apiRef}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        permissions={{
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: false,
          showRefresh: false,
        }}
      />
    </div>
  )
}

export default MaintenanceTable
