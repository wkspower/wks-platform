import { Box } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { Typography } from '../../../../node_modules/@mui/material/index'
import KendoDataTables from 'components/kendo-data-tables/index'
import { validateFields } from 'utils/validationUtils'
import moment from '../../../../node_modules/moment/moment'

const TurnaroundReport = () => {
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const [modifiedCells2, setModifiedCells2] = React.useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [rows, setRows] = useState()
  const [rows2, setRows2] = useState()

  const [loading, setLoading] = useState(false)
  const keycloak = useSession()

  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClick2 = (row) => {
    setCurrentRemark2(row.remarks || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }
  const formatValueToThreeDecimalsZero = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(0) : ''
  }

  const columns = [
    { field: 'sno', title: 'SL.No', widthT: 100, editable: false },

    {
      field: 'activity',
      title: 'Activities',
      width: 200,
      editable: false,
    },

    {
      title: 'Turnaround Period',
      children: [
        { field: 'fromDateReport', title: 'From', width: 120, editable: false },
        { field: 'toDateReport', title: 'To', width: 120, editable: false },
      ],
    },

    {
      field: 'durationInHrs',
      title: 'Duration, hrs',
      width: 120,
      editable: false,
      align: 'right',
      headerAlign: 'right',
      type: 'number',
    },

    {
      field: 'remarks',
      title: 'Remark',
      width: 200,
      editable: true,
    },
  ]

  const columnsGrid2 = [
    { field: 'sno', title: 'SL.No', widthT: 100, editable: false },

    {
      field: 'activity',
      title: 'Activities',
      width: 250,
      editable: true,
    },

    {
      field: 'fromDateReport',
      title: 'Turnaround Period From',
      width: 120,
      editable: true,
    },
    {
      field: 'toDateReport',
      title: 'Turnaround Period To',
      width: 120,
      editable: true,
    },

    {
      field: 'durationInHrs1',
      title: 'Duration, hrs',
      width: 100,
      type: 'number',
      editable: true,
      align: 'right',
      headerAlign: 'right',
    },

    {
      field: 'periodInMonths',
      title: 'Period in Months',
      width: 120,
      type: 'number',
      editable: true,
      align: 'right',
      headerAlign: 'right',
    },

    {
      field: 'remarks',
      title: 'Remark',
      width: 200,
      editable: true,
    },
  ]

  const mapData = (data, tag) =>
    (data?.data?.plantTurnAroundReportData || []).map((item, i) => ({
      ...item,
      idFromApi: item?.Id,
      id: i,
      idRow: `${tag}-${i}`,
      inEdit: false,
      originalRemark: item?.remarks ?? '',
      isEditable: true,
      durationInHrs1: item?.durationInHrs,
      fromDateReport: item?.fromDate,
      toDateReport: item?.toDate,
    }))

  const fetchCurrentYear = async () => {
    setLoading(true)
    try {
      const res = await DataService.getTurnaroundReportData(
        keycloak,
        'currentYear',
      )
      if (res?.code === 200) {
        setRows(mapData(res, 'CY'))
      } else {
        setRows([])
      }
    } catch (e) {
      console.error('Error loading current year:', e)
    } finally {
      setLoading(false)
    }
  }

  const fetchPreviousYear = async () => {
    setLoading(true)
    try {
      const res = await DataService.getTurnaroundReportData(
        keycloak,
        'previousYear',
      )
      if (res?.code === 200) {
        setRows2(mapData(res, 'PY'))
      } else {
        setRows2([])
      }
    } catch (e) {
      console.error('Error loading previous year:', e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCurrentYear()
  }, [keycloak, year, plantId])

  useEffect(() => {
    fetchPreviousYear()
  }, [keycloak, year, plantId])

  const saveChanges = async () => {
    try {
      const data = Object.values(modifiedCells)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const rowsToUpdate = data.map((row) => ({
        id: row.Id,
        remark: row.remarks,
      }))
      const res = await DataService.saveTurnaroundReport(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      // console.log(res)

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setSnackbarOpen(true)
    }
  }

  const saveChanges2 = async () => {
    try {
      const data = Object.values(modifiedCells2)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const formatIfDate = (value) => {
        if (!value) return ''
        const parsed = moment.utc(
          value,
          ['MMM D, YYYY', 'MMM D, YYYY, h:mm:ss A'],
          true,
        )
        return parsed.isValid()
          ? new Date(parsed.add(1, 'day').format('YYYY-MM-DD'))
          : value
      }

      const rowsToUpdate = data.map((row) => ({
        id: row.Id || null,
        fromDate: formatIfDate(row.fromDateReport),
        toDate: formatIfDate(row.toDateReport),
        activity: row.activity,
        sno: row.rowNumber,
        durationInHrs: row.durationInHrs1,
        remark: row.remarks,
        periodInMonths: row.periodInMonths,
      }))
      const requiredFields = ['remarks']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      const res = await DataService.saveTurnaroundReportWhole(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      // console.log(res)

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        fetchPreviousYear()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setSnackbarOpen(true)
    }
  }
  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const res = await DataService.calculateTurnAroundPlanReportData(
        plantId,
        year,
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Successfully!',
          severity: 'success',
        })
        fetchCurrentYear()
        fetchPreviousYear()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }

      return res
    } catch (error) {
      // setSnackbarOpen(true)
      // setSnackbarData({
      //   message: error.message || 'An error occurred',
      //   severity: 'error',
      // })
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
  }

  const deleteRowData = async (paramsForDelete) => {
    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows2((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteTurnArondReportItem(idFromApi, keycloak)
        setRows2((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchPreviousYear()
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  return (
    <Box>
      <KendoDataTablesReports
        modifiedCells={modifiedCells}
        enableSaveAddBtn={enableSaveAddBtn}
        rows={rows}
        setRows={setRows}
        columns={columns}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        loading={loading}
        handleRemarkCellClick={handleRemarkCellClick}
        title='Turnaround Details (T-19A)'
        setModifiedCells={setModifiedCells}
        permissions={{
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: false,
          saveBtnForRemark: true,
          saveBtn: true,
          showWorkFlowBtns: true,
          showTitle: true,
        }}
        saveChanges={saveChanges}
        handleCalculate={handleCalculate}
      />

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Turnaround details for the previous years data since commissioning{' '}
      </Typography>

      <KendoDataTables
        modifiedCells={modifiedCells2}
        rows={rows2}
        setRows={setRows2}
        columns={columnsGrid2}
        remarkDialogOpen={remarkDialogOpen2}
        setRemarkDialogOpen={setRemarkDialogOpen2}
        currentRemark={currentRemark2}
        setCurrentRemark={setCurrentRemark2}
        currentRowId={currentRowId2}
        setCurrentRowId={setCurrentRowId2}
        saveChanges={saveChanges2}
        handleRemarkCellClick={handleRemarkCellClick2}
        loading={loading}
        fetchData={fetchPreviousYear}
        setModifiedCells={setModifiedCells2}
        deleteRowData={deleteRowData}
        permissions={{
          remarksEditable: true,
          saveBtn: true,
          saveBtnForRemark: true,
          addButton: true,
          allAction: true,
          deleteButton: true,
        }}
      />
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
export default TurnaroundReport
