import { useCallback, useEffect, useState } from 'react'

import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataTables from './index'
import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'
import { validateFields } from 'utils/validationUtils'

const MaintenanceTable = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
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
    try {
      console.log("Original newRows:", newRows)

      let plantId = ''
      let year = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) plantId = JSON.parse(storedPlant)?.id
      year = localStorage.getItem('year') || ''

      //  Map newRows to DecokePlanningDTO format
      const decokePlanningDTOList = newRows.map((row) => ({
        id: row?.idFromApi,
        monthName: row.monthName ?? null,
        ibr: row.ibr ,
        mnt: row.mnt ,
        shutdown: row.shutdown ,
        sad: row.sad ,
        bud: row.bud ,
        demoHSS: row.demoHHS , 
        demoBBU: row.demoBBU ,
        demoSAD: row.demoSAD ,
        fourFD: row['fourFD'] ,
        fourF: row['fourF'] ,
        fiveF: row['5F'] ,
        total: row.total ,
        fourFHours: row['fourFHours'] ,
        aopYear: year,
        plantId: plantId,
        remarks: row.remarks ?? row.remark ?? '',
      }))

      console.log("Mapped DTOs:", decokePlanningDTOList)

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
          message: 'Cracker maintenance data saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData() // ?? Refresh table
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving Cracker maintenance data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving Cracker maintenance data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    }
  }




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
  const fetchData = async () => {
    setRows([]);
    setLoading(true);

    try {
      if (lowerVertName === 'cracker') {
        const response = await DataService.getCrackerMaintenanceData(keycloak);
        if (response?.code == 200){
          const formattedData = response?.data.map((item, index) => ({
            ...item,
            idFromApi: item.id,
            id: index,
            originalRemark: item.remarks,
            isEditable: true,
          }));
          setRows(formattedData);
        }
        else{
          console.error('Error fetching data:', error);
          setRows([]);
        }
        }
        else {
        const data = await DataService.getMaintenanceData(keycloak);
          const formattedData = data.map((item, index) => ({
            ...item,
            idFromApi: item.id,
            id: index,
            isEditable: false,
          }));
          setRows(formattedData);
        } 
    } catch (error) {
      console.error('Error fetching data:', error);
      setRows([]);
    } finally {
      setLoading(false);
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
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const productionColumns = [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      width: 220,
      editable: false,
    },
    {
      field: 'April',
      title: headerMap[4],
      align: 'right',
      headerAlign: 'left',
      type: 'number',
      format: '{0:n2}',
      editable: false,
    },

    {
      field: 'May',
      title: headerMap[5],
      type: 'number',
      format: '{0:n2}',
      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'June',
      title: headerMap[6],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'July',
      title: headerMap[7],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Aug',
      title: headerMap[8],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Sep',
      title: headerMap[9],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Oct',
      title: headerMap[10],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },

    {
      field: 'Nov',
      title: headerMap[11],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Dec',
      title: headerMap[12],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Jan',
      title: headerMap[1],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Feb',
      title: headerMap[2],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'Mar',
      title: headerMap[3],
      type: 'number',
      format: '{0:n2}',

      editable: false,
      align: 'right',
      headerAlign: 'left',
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]
  const basecols = lowerVertName == 'cracker' ? crackercolumns : productionColumns

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
      showAction: true,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: lowerVertName == 'cracker',
      showRefresh: false,
      allAction: lowerVertName == 'cracker',
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
        columns={basecols}
        rows={rows}
        setRows={setRows}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        handleCalculate={handleCalculate}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
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
