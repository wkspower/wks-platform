import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'
import KendoDataTables from './index'
const MaintenanceTable = () => {
  const keycloak = useSession()
  const { verticalChange, yearChanged, oldYear, plantID } = useSelector(
    (s) => s.dataGridStore,
  )
  const lowerVertName = verticalChange?.selectedVertical?.toLowerCase()

  const dataConfig = useMemo(
    () => ({
      isCracker: lowerVertName === 'cracker',
      serviceFn:
        lowerVertName === 'cracker'
          ? DataService.getCrackerMaintenanceData
          : DataService.getMaintenanceData,
      editable: lowerVertName === 'cracker',
    }),
    [plantID],
  )

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [deleteId, setDeleteId] = useState(null)
  const [open1, setOpen1] = useState(false)

  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = useCallback(async () => {
    try {
      setLoading(true)
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const validationMessage = validateFields(data, ['remarks'])
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({ message: validationMessage, severity: 'error' })
        setLoading(false)
        return
      }

      await saveCrackerMaintenanceData(data)
    } catch (err) {
      console.error('Save Cracker Data Error:', err)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells])
  const saveCrackerMaintenanceData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      let year = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) plantId = JSON.parse(storedPlant)?.id
      year = localStorage.getItem('year') || ''


const decokePlanningDTOList = newRows.map((row) => ({
  fourFD: row.fourFD,
  aopYear: year,
  totalSAD: row.totalSAD,
  monthName: row.monthName ?? null,
  plantId: plantId,
  numberOfDays: row.numberOfDays,
  demoBBU: row.demoBBU,
  coilReplacement: row.coilReplacement,
  demoSAD: row.demoSAD,
  demoSD: row.demoSD,//demoSD
  fourF: row.fourF,
  mnt: row.mnt,
  total: row.total,
  fourFHours: row.fourFHours,
  bbu: row.bbu,
  bbd: row.bbd,
  sad: row.sad,//sad
  demoHSS: row.demoHSS,
  fiveF: row.fiveF, 
  id: row.idFromApi || row.id,
  shutdown: row.shutdown,
  slowdown: row.slowdown,
  totalSAD: row.totalSAD,
  remarks: row.remarks ?? row.remark ?? '',
}))

      const response = await DataService.saveCrackerMaintenance(
        {
          plantId,
          year,
          decokePlanningDTOList,
        },
        keycloak,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
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
        originalRemark: item.remarks,
      }))
      setRows(formatted)
    } catch (err) {
      console.error('Error fetching data:', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [plantID, keycloak])

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
  }, [fetchData, oldYear, yearChanged, plantID])

  // Helper to generate monthly fields
  const getMonthlyColumns = () => {
    const months = [
      { field: 'April', index: 4 },
      { field: 'May', index: 5 },
      { field: 'June', index: 6 },
      { field: 'July', index: 7 },
      { field: 'Aug', index: 8 },
      { field: 'Sep', index: 9 },
      { field: 'Oct', index: 10 },
      { field: 'Nov', index: 11 },
      { field: 'Dec', index: 12 },
      { field: 'Jan', index: 1 },
      { field: 'Feb', index: 2 },
      { field: 'Mar', index: 3 },
    ]

    return months.map(({ field, index }) => ({
      field,
      title: headerMap[index],
      type: 'number',
      format: '{0:n2}',
      editable: false,
      align: 'right',
      headerAlign: 'left',
    }))
  }

  // Shared editable field
  const isEditableField = {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  }

  // Base function to generate column set
  const generateColumns = (nameWidthT) => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      widthT: nameWidthT,
      editable: false,
    },
    ...getMonthlyColumns(),
    isEditableField,
  ]

  // Column sets
  const productionColumnsMEG = generateColumns(390)
  const productionColumnsPE = generateColumns(150)
  const productionColumnsPP = generateColumns(220)

  // Column selection
  let basecols

  switch (lowerVertName) {
    case 'cracker':
      basecols = crackercolumns
      break
    case 'meg':
      basecols = productionColumnsMEG
      break
    case 'pe':
      basecols = productionColumnsPE
      break
    case 'pp':
      basecols = productionColumnsPP
      break
    default:
      basecols = productionColumnsMEG
      break
  }

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
          allAction: true,
          downloadExcelBtnFromUI: true,
          ExcelName: `${lowerVertName}_Maintenance Details`,
          //allAction: false,
          showRefresh: false,
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
        saveChanges={saveChanges}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
      />
    </div>
  )
}
export default MaintenanceTable

// import { useEffect, useState } from 'react'

// import { DataService } from 'services/DataService'
// import { useSession } from 'SessionStoreContext'

// import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import { useSelector } from 'react-redux'

// import Backdrop from '@mui/material/Backdrop'
// import CircularProgress from '@mui/material/CircularProgress'
// import KendoDataTables from './index'
// import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'

// const MaintenanceTable = () => {
//   const keycloak = useSession()
//   const [loading, setLoading] = useState(false)
//   const [open1, setOpen1] = useState(false)
//   const [deleteId, setDeleteId] = useState(null)
//   const [rows, setRows] = useState()

//   const headerMap = generateHeaderNames(localStorage.getItem('year'))

//   const dataGridStore = useSelector((state) => state.dataGridStore)
//   const { sitePlantChange, verticalChange, yearChanged, oldYear } =
//     dataGridStore
//   //const isOldYear = oldYear?.oldYear
//   const isOldYear = oldYear?.oldYear

//   const vertName = verticalChange?.selectedVertical
//   const lowerVertName = vertName?.toLowerCase() || 'meg'

//   const [snackbarData, setSnackbarData] = useState({
//     message: '',
//     severity: 'info',
//   })
//   const [snackbarOpen, setSnackbarOpen] = useState(false)

//   const fetchData = async () => {
//     setRows([])
//     setLoading(true)
//     try {
//       if (lowerVertName === 'cracker') {
//         const response = await DataService.getCrackerMaintenanceData(keycloak)
//         // console.log('Cracker Data :', response)
//         if (response?.code == 200) {
//           const formattedData = response?.data.map((item, index) => ({
//             ...item,
//             idFromApi: item.id,
//             id: index,
//             isEditable: true,
//           }))
//           setRows(formattedData)
//         } else {
//           console.error('Error fetching data:')
//           setRows([])
//         }
//       } else {
//         const data = await DataService.getMaintenanceData(keycloak)
//         const formattedData = data?.map((item, index) => ({
//           ...item,
//           idFromApi: item.id,
//           id: index,
//           isEditable: false,
//         }))
//         setRows(formattedData)
//       }
//     } catch (error) {
//       console.error('Error fetching data:', error)
//       setRows([])
//     } finally {
//       setLoading(false)
//     }
//   }

//   const handleCalculate = async () => {
//     try {
//       const storedPlant = localStorage.getItem('selectedPlant')
//       const year = localStorage.getItem('year')
//       if (storedPlant) {
//         const parsedPlant = JSON.parse(storedPlant)
//         plantId = parsedPlant.id
//       }
//       var plantId = plantId
//       const data = await DataService.handleCalculateMaintenance(
//         plantId,
//         year,
//         keycloak,
//       )

//       if (data == 0) {
//         setSnackbarOpen(true)
//         setSnackbarData({
//           message: 'Data refreshed successfully!',
//           severity: 'success',
//         })
//         fetchData()
//       } else {
//         setSnackbarOpen(true)
//         setSnackbarData({
//           message: 'Data Refresh Falied!',
//           severity: 'error',
//         })
//       }

//       return data
//     } catch (error) {
//       setSnackbarOpen(true)
//       setSnackbarData({
//         message: error.message || 'An error occurred',
//         severity: 'error',
//       })
//       console.error('Error!', error)
//     }
//   }

//   useEffect(() => {
//     fetchData()
//   }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

//   const productionColumnsMEG = [
//     {
//       field: 'Name',
//       title: 'Description',
//       align: 'left',
//       headerAlign: 'left',
//       width: 220,
//       editable: false,
//     },
//     {
//       field: 'April',
//       title: headerMap[4],
//       align: 'right',
//       headerAlign: 'left',
//       type: 'number',
//       format: '{0:n2}',
//       editable: false,
//     },

//     {
//       field: 'May',
//       title: headerMap[5],
//       type: 'number',
//       format: '{0:n2}',
//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'June',
//       title: headerMap[6],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'July',
//       title: headerMap[7],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Aug',
//       title: headerMap[8],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Sep',
//       title: headerMap[9],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Oct',
//       title: headerMap[10],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },

//     {
//       field: 'Nov',
//       title: headerMap[11],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Dec',
//       title: headerMap[12],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Jan',
//       title: headerMap[1],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Feb',
//       title: headerMap[2],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },
//     {
//       field: 'Mar',
//       title: headerMap[3],
//       type: 'number',
//       format: '{0:n2}',

//       editable: false,
//       align: 'right',
//       headerAlign: 'left',
//     },

//     {
//       field: 'isEditable',
//       title: 'isEditable',
//       hidden: true,
//     },
//   ]
//   const basecols =
//     lowerVertName == 'cracker' ? crackercolumns : productionColumnsMEG

//   const getAdjustedPermissions = (permissions, isOldYear) => {
//     if (isOldYear != 1) return permissions
//     return {
//       ...permissions,
//       showAction: false,
//       addButton: false,
//       deleteButton: false,
//       editButton: false,
//       showUnit: false,
//       saveWithRemark: false,
//       saveBtn: lowerVertName == 'cracker',
//       isOldYear: isOldYear,
//       allAction: lowerVertName == 'cracker',
//     }
//   }

//   const adjustedPermissions = getAdjustedPermissions(
//     {
//       showAction: false,
//       addButton: false,
//       deleteButton: false,
//       editButton: false,
//       showUnit: false,
//       saveWithRemark: false,
//       saveBtn: lowerVertName == 'cracker',
//       showRefresh: false,
//       allAction: lowerVertName == 'cracker',
//     },
//     isOldYear,
//   )

//   return (
//     <div>
//       <Backdrop
//         sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
//         open={!!loading}
//       >
//         <CircularProgress color='inherit' />
//       </Backdrop>
//       <KendoDataTables
//         columns={basecols}
//         rows={rows}
//         setRows={setRows}
//         snackbarData={snackbarData}
//         snackbarOpen={snackbarOpen}
//         handleCalculate={handleCalculate}
//         setDeleteId={setDeleteId}
//         fetchData={fetchData}
//         setOpen1={setOpen1}
//         setSnackbarOpen={setSnackbarOpen}
//         setSnackbarData={setSnackbarData}
//         deleteId={deleteId}
//         open1={open1}
//         permissions={adjustedPermissions}
//       />
//     </div>
//   )
// }

// export default MaintenanceTable
