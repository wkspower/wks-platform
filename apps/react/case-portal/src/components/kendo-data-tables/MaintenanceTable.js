import { useEffect, useState, useCallback, useMemo } from 'react'

import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataTables from './index'
import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'

const MaintenanceTable = () => {
  const keycloak = useSession()
  const { sitePlantChange, verticalChange, yearChanged, oldYear } = useSelector(
    (s) => s.dataGridStore,
  )
  const lowerVertName = verticalChange?.selectedVertical?.toLowerCase() || 'meg'
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const dataConfig = useMemo(
    () => ({
      isCracker: lowerVertName === 'cracker',
      serviceFn:
        lowerVertName === 'cracker'
          ? DataService.getCrackerMaintenanceData
          : DataService.getMaintenanceData,
      editable: lowerVertName === 'cracker',
    }),
    [lowerVertName],
  )
  const headerMap = useMemo(
    () => generateHeaderNames(localStorage.getItem('year')),
    [],
  )
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)


  //const isOldYear = oldYear?.oldYear


  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [deleteId, setDeleteId] = useState(null)
  const [open1, setOpen1] = useState(false)

  // const fetchData = async () => {
  //   setRows([])
  //   setLoading(true)
  //   try {
  //     const data = await DataService.getMaintenanceData(keycloak)
  //     var formattedData = []
  //     if (data) {
  //       formattedData = data?.map((item, index) => ({
  //         ...item,
  //         idFromApi: item.id,
  //         id: index,
  //         isEditable: false,
  //       }))
  //     }
  //     setRows(formattedData)
  //     setLoading(false)
  //   } catch (error) {
  //     console.error('Error fetching  data:', error)
  //     setRows([])
  //     setLoading(false)
  //   }
  // }
  const fetchData = useCallback(async () => {
    setRows([])
    setLoading(true)

    try {
      const resp = await dataConfig.serviceFn(keycloak)
      const raw = dataConfig.isCracker ? resp.data : resp
      const formatted = (raw || []).map((item, idx) => ({
            ...item,
            idFromApi: item.id,
        id: idx,
        isEditable: dataConfig.editable,
      }))
      setRows(formatted)
    } catch (err) {
      console.error('Error fetching data:', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [plantId, keycloak])


  const handleCalculate = useCallback(async () => {
    const plantId = JSON.parse(localStorage.getItem('selectedPlant') || '{}').id
      const year = localStorage.getItem('year')
    try {
      const result = await DataService.handleCalculateMaintenance(
        plantId,
        year,
        keycloak,
      )

        setSnackbarData({
        message:
          result === 0
            ? 'Data refreshed successfully!'
            : 'Data Refresh Failed!',
        severity: result === 0 ? 'success' : 'error',
        })
        setSnackbarOpen(true)
      if (result === 0) fetchData()

    } catch (err) {
      console.error(err)
      setSnackbarData({ message: err.message || 'Error!', severity: 'error' })
      setSnackbarOpen(true)
    }
  }, [keycloak, fetchData])

  useEffect(() => {
    fetchData()
  }, [fetchData, sitePlantChange, oldYear, yearChanged])

  const productionColumns = useMemo(
    () => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      width: 220,
    },
      ...[4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3].map((i) => ({
        field: [
          'April',

          'May',
          'June',

          'July',

          'Aug',

          'Sep',

          'Oct',


          'Nov',

          'Dec',

          'Jan',
          'Feb',

          'Mar',
        ][i - 1],
        title: headerMap[i],
      type: 'number',
      format: '{0:n2}',

      align: 'right',
      headerAlign: 'left',
      })),
      { field: 'isEditable', title: 'isEditable', hidden: true },
    ],
    [headerMap],

  )

  const basecols = useMemo(
    () => (dataConfig.isCracker ? crackercolumns : productionColumns),
    [dataConfig.isCracker, productionColumns],
  )

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
      saveBtn: dataConfig.isCracker,
      isOldYear: isOldYear,
      allAction: dataConfig.isCracker,
    }
  }

  const adjustedPermissions = useMemo(
    () =>
      getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
          saveBtn: dataConfig.isCracker,
      showRefresh: false,
          allAction: dataConfig.isCracker,
    },
        oldYear?.oldYear,
      ),
    [dataConfig.isCracker, oldYear],
  )

  return (
    <div>
      <Backdrop
        open={loading}
        sx={{ color: '#fff', zIndex: (t) => t.zIndex.drawer + 1 }}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <KendoDataTables
        columns={basecols}
        rows={rows}
        setRows={setRows}
        fetchData={fetchData}
        handleCalculate={handleCalculate}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        open1={open1}
        setOpen1={setOpen1}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
        permissions={adjustedPermissions}
      />
    </div>
  )
}

export default MaintenanceTable
// import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import Backdrop from '@mui/material/Backdrop'
// import CircularProgress from '@mui/material/CircularProgress'
// import KendoDataTables from './index'
