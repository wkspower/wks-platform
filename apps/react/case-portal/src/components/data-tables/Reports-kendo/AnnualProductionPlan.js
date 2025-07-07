import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import Notification from 'components/Utilities/Notification'
import KendoDataTables from 'components/kendo-data-tables/index'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import moment from '../../../../node_modules/moment/moment'

const AnnualProductionPlan = () => {
  const keycloak = useSession()

  const thisYear = localStorage.getItem('year')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  let oldYear1 = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear1 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  let oldYear2 = ''
  if (oldYear1 && oldYear1.includes('-')) {
    const [start, end] = oldYear1.split('-').map(Number)
    oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  let oldYear3 = ''
  if (oldYear2 && oldYear2.includes('-')) {
    const [start, end] = oldYear2.split('-').map(Number)
    oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  // let oldYear4 = ''
  // if (oldYear3 && oldYear3.includes('-')) {
  //   const [start, end] = oldYear3.split('-').map(Number)
  //   oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  // }

  const [rowsAssumptions, setRowsassumptions] = useState([])
  const [rowsMaxRate, setRowsMaxRate] = useState([])
  const [rowsOperatingHrs, setRowsOperatingHrs] = useState([])
  const [rowsAverageHourlyRate, setRowsAverageHourlyRate] = useState([])
  const [rowsProductionPerformance, setRowsProductionPerformance] = useState([])
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCells2, setModifiedCells2] = React.useState({})
  const [modifiedCells3, setModifiedCells3] = React.useState({})
  const [modifiedCells4, setModifiedCells4] = React.useState({})
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [currentRowId3, setCurrentRowId3] = useState(null)
  const [currentRowId4, setCurrentRowId4] = useState(null)
  const [rows, setRows] = useState()

  const formatValueToThreeDecimals = (params) => {
    const dateRegex =
      /^(\d{1,2}[-/ ]\d{1,2}[-/ ]\d{2,4}|\d{1,2} [a-zA-Z]+ \d{4}|\d{1,2}-[a-zA-Z]{3}-\d{2,4})$/

    if (params === 0) return 0

    if (typeof params === 'string' && dateRegex.test(params.trim())) {
      return params
    }

    const num = parseFloat(params)
    return isNaN(num) ? '' : num.toFixed(1)
  }
  const formatValueToThreeDecimalsTwo = (params) => {
    const dateRegex =
      /^(\d{1,2}[-/ ]\d{1,2}[-/ ]\d{2,4}|\d{1,2} [a-zA-Z]+ \d{4}|\d{1,2}-[a-zA-Z]{3}-\d{2,4})$/

    if (params === 0) return 0

    if (typeof params === 'string' && dateRegex.test(params.trim())) {
      return params
    }

    const num = parseFloat(params)
    return isNaN(num) ? '' : num.toFixed(2)
  }

  const formatValueToThreeDecimalsTwoProductionPerformance = (params, row) => {
    const rowsWithPercentage =
      row?.Item?.includes('Operating') || row?.Item?.includes('EOE')

    if (rowsWithPercentage) {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(0) : ''
    } else {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(2) : ''
    }
  }

  const formatValueToThreeDecimalsZero = (params) => {
    const dateRegex =
      /^(\d{1,2}[-/ ]\d{1,2}[-/ ]\d{2,4}|\d{1,2} [a-zA-Z]+ \d{4}|\d{1,2}-[a-zA-Z]{3}-\d{2,4})$/

    if (params === 0) return 0

    if (typeof params === 'string' && dateRegex.test(params.trim())) {
      return params
    }

    const num = parseFloat(params)
    return isNaN(num) ? '' : num.toFixed(0)
  }
  // {
  //               "activity": "1 MT EOE \u003d 1 MT EO",
  //               "sno": 1,
  //               "id": "E0383316-53DE-4A28-B1D5-AC57294ECE8E"
  //           },
  const columnsAssumptions = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: false,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'activity',
      headerName: 'Assumptions & remarks',
      editable: true,
      flex: 1,
    },
    {
      field: 'id',
      hidden: true,
    },
  ]
  //  {
  //               "maxHourlyRateValue": "480.8190",
  //               "uom": "TPD",
  //               "activity": "Recorded max daily production",
  //               "sno": 1,
  //               "id": "2813A86A-5AA0-408B-A8FB-F2E49BC844C3"
  //           },
  const columnsMaxRate = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: false,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'activity',
      headerName: 'Max hourly rate achieved',
      editable: true,
      flex: 1,
    },
    {
      field: 'id',
      hidden: true,
    },
    {
      field: 'maxHourlyRateValue',
      headerName: 'Value',
      editable: true,
      flex: 1,
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimals(params.value)}</span>
        </Tooltip>
      ),
      type:'number'
    },
    { field: 'uom', headerName: 'UOM', editable: true, flex: 1 },
  ]
  // {
  //               "rateValue": 8760.00000000,
  //               "uom": "Hrs",
  //               "activity": "Total available hours",
  //               "sno": 1,
  //               "id": "56C9D602-C34B-42ED-983D-218C56CD7568"
  //           },
  const columnsOperatingHrs = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: false,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'activity',
      headerName: 'Calculation of Operating hours',
      editable: true,
      flex: 1,
    },
    {
      field: 'id',
      hidden: true,
    },
    {
      field: 'rateValue',
      headerName: 'Value',
      editable: true,
      flex: 1,
      align: 'right',
      valueFormatter: formatValueToThreeDecimalsZero,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimalsZero(params.value)}</span>
        </Tooltip>
      ),
      type:'number'
    },
    {
      field: 'uom',
      headerName: 'Hours',
      editable: true,
      flex: 1,
      align: 'right',
    },
  ]

  //  {
  //                 "durationHours": 720.00000000,
  //                 "rateValue": 18.96000000,
  //                 "periodTo": "30-Jun-25",
  //                 "activity": "Plant running normal",
  //                 "sno": 3,
  //                 "periodFrom": "01-Jun-25",
  //                 "id": "021CD9C0-2074-4A17-BDAE-88A98A5A0CA9"
  //             },
  const columnsAverageHourlyRate = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: false,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'activity',
      headerName: 'Throughput  limiting causes',
      editable: true,
      flex: 1,
    },
    {
      field: 'id',
      hidden: true,
    },
    {
      field: 'rateValue',
      headerName: 'Achievable Hourly rate',
      editable: true,
      flex: 1,
      align: 'right',
      valueFormatter: formatValueToThreeDecimalsTwo,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimalsTwo(params.value)}</span>
        </Tooltip>
      ),
      type:'number'
    },
    {
      field: 'durationHours',
      headerName: 'Op. Hrs',
      editable: true,
      flex: 1,
      align: 'right',
      valueFormatter: formatValueToThreeDecimalsZero,
      renderCell: (params) => (
        <Tooltip
          title={params.value != null ? params.value.toString() : ''}
          arrow
        >
          <span>{formatValueToThreeDecimalsZero(params.value)}</span>
        </Tooltip>
      ),
      type:'number'
    },
    {
      field: 'periodFrom',
      headerName: 'Period from',
      editable: true,
      flex: 1,
    },
    { field: 'periodTo', headerName: 'Period to', editable: true, flex: 1 },
  ]

  const year4 = localStorage.getItem('year')
  const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
  const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
  const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`
  // {
  //               "Item": "Operating hours, Hrs",
  //               "sno": 2,
  //               "Actual2": 8760.00000000,
  //               "Actual3": 8760.00000000,
  //               "Budget4": 8496.00000000,
  //               "Actual1": 8760.00000000,
  //               "Budget3": 8760.00000000,
  //               "Budget2": 8496.00000000,
  //               "Budget1": 8736.00000000
  //           },
  const columnsProductionPerformance = [
    {
      field: 'sno',
      title: 'SL.No',
      editable: false,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'Item',
      title: 'Item',
      editable: false,
      flex: 1,
      // Pass the column's editable state to the cell renderer
    },
    // Grouped Columns
    {
      title: year1,
      children: [
        {
          field: 'Budget1',
          title: 'Budget',
          editable: false, // This column is NOT editable
          flex: 1,
          align: 'right',
          // Pass the column's editable state to the cell renderer
        },
        {
          field: 'Actual1',
          title: 'Actual',
          editable: false, // This column is NOT editable
          flex: 1,
          align: 'right',
          // Pass the column's editable state to the cell renderer
        },
      ],
    },
    {
      title: year2,
      children: [
        {
          field: 'Budget2',
          title: 'Budget',
          editable: false,
          flex: 1,
          align: 'right',
        },
        {
          field: 'Actual2',
          title: 'Actual',
          editable: false,
          flex: 1,
          align: 'right',
        },
      ],
    },
    {
      title: year3,
      children: [
        {
          field: 'Budget3',
          title: 'Budget',
          editable: false,
          flex: 1,
          align: 'right',
        },
        {
          field: 'Actual3',
          title: 'Actual',
          editable: false,
          flex: 1,
          align: 'right',
        },
      ],
    },
    {
      title: year4,
      children: [
        {
          field: 'Budget4',
          title: 'Budget',
          editable: false,
          flex: 1,
          align: 'right',
          format: '{0:#.#####}',
          type:'number'
        },
      ],
    },
  ]

  const columnGroupingModel = [
    {
      groupId: year1,
      children: [{ field: 'Budget1' }, { field: 'Actual1' }],
    },
    {
      groupId: year2,
      children: [{ field: 'Budget2' }, { field: 'Actual2' }],
    },
    {
      groupId: year3,
      children: [{ field: 'Budget3' }, { field: 'Actual3' }],
    },
    {
      groupId: year4,
      children: [{ field: 'Budget4' }],
    },
  ]

  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const fetchData = async (type) => {
    try {
      setLoading(true)
      var res = await DataService.getAnnualProductionPlanReportData(
        keycloak,
        type,
      )
      if (res?.code == 200) {
        res = res?.data?.plantProductionData.map((item, index) => ({
          ...item,
          idFromApi: item?.id,
          id: index,
          isEditable: true,
          inEdit: false,
          periodFrom: item?.periodFrom
            ? moment(item.periodFrom, 'DD-MMM-YY').toDate()
            : null,
          periodTo: item?.periodTo
            ? moment(item.periodTo, 'DD-MMM-YY').toDate()
            : null,
        }))

        switch (type) {
          case 'assumptions':
            setRowsassumptions(res)
            break

          case 'maxRate':
            setRowsMaxRate(res)
            break

          case 'OperatingHrs':
            setRowsOperatingHrs(res)
            break

          case 'AverageHourlyRate':
            setRowsAverageHourlyRate(res)
            break

          case 'ProductionPerformance':
            setRowsProductionPerformance(res)
            break

          default:
            console.warn('Unknown report type:', type)
            break
        }
      } else {
        setRows([])
      }
    } catch (err) {
      console.log(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData('assumptions')
    fetchData('maxRate')
    fetchData('OperatingHrs')
    fetchData('AverageHourlyRate')
    fetchData('ProductionPerformance')
  }, [year, keycloak, plantId])

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
      const res = await DataService.calculateAnnualProductionPlanData(
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
        fetchData('assumptions')
        fetchData('maxRate')
        fetchData('OperatingHrs')
        fetchData('AverageHourlyRate')
        fetchData('ProductionPerformance')
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

      const dataList = data.map((row) => ({
        id: row.idFromApi,
        uom: row.uom,
        sno: row.sno,
        activity: row.activity,
        rateValue: row.rateValue,
      }))
      const res = await DataService.saveAnnualProduction(
        {
          plantId,
          year,
          reportType: 'assumptions',
          dataList,
        },
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData('assumptions')
        // fetchData('maxRate')
        // fetchData('OperatingHrs')
        // fetchData('AverageHourlyRate')
        // fetchData('ProductionPerformance')
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

      const dataList = data.map((row) => ({
        id: row.idFromApi,
        uom: row.uom,
        sno: row.sno,
        activity: row.activity,
        maxHourlyRateValue: row.maxHourlyRateValue,
      }))
      const res = await DataService.saveAnnualProduction(
        {
          plantId,
          year,
          reportType: 'maxRate',
          dataList,
        },
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells2({})
        // fetchData('assumptions')
        fetchData('maxRate')
        // fetchData('OperatingHrs')
        // fetchData('AverageHourlyRate')
        // fetchData('ProductionPerformance')
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
  const saveChanges3 = async () => {
    try {
      const data = Object.values(modifiedCells3)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const dataList = data.map((row) => ({
        id: row.idFromApi,
        uom: row.uom,
        sno: row.sno,
        activity: row.activity,
        rateValue: row.rateValue,
      }))
      const res = await DataService.saveAnnualProduction(
        {
          plantId,
          year,
          reportType: 'OperatingHrs',
          dataList,
        },
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells3({})
        // fetchData('assumptions')
        // fetchData('maxRate')
        fetchData('OperatingHrs')
        // fetchData('AverageHourlyRate')
        // fetchData('ProductionPerformance')
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
  const saveChanges4 = async () => {
    try {
      const data = Object.values(modifiedCells4)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const dataList = data.map((row) => ({
        id: row.idFromApi,
        sno: row.sno,
        activity: row.activity,
        rateValue: row.rateValue,
        durationHours: row.durationHours,
        periodFrom: row?.periodFrom
          ? moment(row.periodFrom)
              .add(1, 'day')
              .utc()
              .startOf('day')
              .toISOString()
          : null,
        periodTo: row?.periodTo
          ? moment(row.periodTo)
              .add(1, 'day')
              .utc()
              .startOf('day')
              .toISOString()
          : null,
      }))
      const res = await DataService.saveAnnualProduction(
        {
          plantId,
          year,
          reportType: 'AverageHourlyrate',
          dataList,
        },
        keycloak,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells4({})
        // fetchData('assumptions')
        // fetchData('maxRate')
        // fetchData('OperatingHrs')
        fetchData('AverageHourlyRate')
        //fetchData('ProductionPerformance')
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

  return (
    <Box sx={{ height: 'auto', width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Assumptions & remarks{' '}
      </Typography> */}

      <KendoDataTables
        rows={rowsAssumptions}
        setRows={setRowsassumptions}
        columns={columnsAssumptions}
        handleCalculate={handleCalculate}
        titleName='Plant Production Plan (T-15) - Assumptions & remarks'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        saveChanges={saveChanges}
        loading={loading}
        fetchData={() => fetchData('assumptions')}
        permissions={{
          showWorkFlowBtns: true,
          showCalculate: false,
          showTitle: true,
          saveBtn: true,
          allAction: true,
          showT15: true,
        }}
      />

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Max hourly rate achieved{' '}
      </Typography>
      <KendoDataTables
        rows={rowsMaxRate}
        columns={columnsMaxRate}
        permissions={{ saveBtn: true, allAction: true }}
        modifiedCells={modifiedCells2}
        setModifiedCells={setModifiedCells2}
        currentRowId={currentRowId2}
        setCurrentRowId={setCurrentRowId2}
        setRows={setRowsMaxRate}
        saveChanges={saveChanges2}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Operating hours{' '}
      </Typography>
      <KendoDataTables
        rows={rowsOperatingHrs}
        columns={columnsOperatingHrs}
        permissions={{ saveBtn: true, allAction: true }}
        modifiedCells={modifiedCells3}
        setModifiedCells={setModifiedCells3}
        currentRowId={currentRowId3}
        setCurrentRowId={setCurrentRowId3}
        setRows={setRowsOperatingHrs}
        saveChanges={saveChanges3}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Average hourly rate{' '}
      </Typography>
      <KendoDataTables
        rows={rowsAverageHourlyRate}
        columns={columnsAverageHourlyRate}
        permissions={{ saveBtn: true, allAction: true }}
        modifiedCells={modifiedCells4}
        setModifiedCells={setModifiedCells4}
        currentRowId={currentRowId4}
        setCurrentRowId={setCurrentRowId4}
        setRows={setRowsAverageHourlyRate}
        saveChanges={saveChanges4}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production performance comparision with last 3 years{' '}
      </Typography>
      <KendoDataTablesReports
        rows={rowsProductionPerformance}
        columns={columnsProductionPerformance}
        columnGroupingModel={columnGroupingModel}
        setRows={setRowsProductionPerformance}
        permissions={{
          textAlignment: 'center',
        }}
        // modifiedCells={modifiedCells5}
        // setModifiedCells={setModifiedCells5}
        // currentRowId={currentRowId5}
        // setCurrentRowId={setCurrentRowId5}
        // setRows={setRows5}
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

export default AnnualProductionPlan
