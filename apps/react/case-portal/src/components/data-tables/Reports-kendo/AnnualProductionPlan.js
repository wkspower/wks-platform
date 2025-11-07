import { Box } from '@mui/material'
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
import { useSelector } from 'react-redux'
//import { format } from '../../../../node_modules/@progress/kendo-intl/dist/npm/format'
import { format } from '@progress/kendo-intl'
const AnnualProductionPlan = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const thisYear = AOP_YEAR
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
  const isOldYear = oldYear?.oldYear === 1
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
  const dateRegex = /^(\d{1,2}-[a-zA-Z]{3}-\d{2,4})$/

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
      widthT: 80,
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
      widthT: 80,
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
      widthT: 150,
      type: 'number',
      format: '{0:#.##}',
    },
    { field: 'uom', headerName: 'UOM', editable: true, widthT: 120 },
  ]

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
      widthT: 150,
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'uom',
      headerName: 'Hours',
      editable: true,
      flex: 1,
      align: 'right',
      widthT: 120,
    },
  ]

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
      widthT: 150,
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'durationHours',
      headerName: 'Op. Hrs',
      editable: true,
      flex: 1,
      align: 'right',
      widthT: 150,
      type: 'number',
    },
    {
      field: 'periodFrom',
      headerName: 'Period from',
      editable: true,
      flex: 1,
    },
    { field: 'periodTo', headerName: 'Period to', editable: true, flex: 1 },
  ]

  const year4 = AOP_YEAR
  const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
  const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
  const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`

  const columnsProductionPerformance = [
    {
      field: 'sno',
      title: 'SL.No',
      editable: false,
      widthT: 80,
      format: '{0:#.#}',
      align: 'right',
    },
    {
      field: 'Item',
      title: 'Item',
      editable: false,
      flex: 1,
      widthT: 250,
    },

    {
      title: year1,
      children: [
        {
          field: 'Budget1',
          title: 'Budget',
          editable: false,
          flex: 1,
          align: 'right',
          format: '{0:#.##}',
          type: 'number',
        },
        {
          field: 'Actual1',
          title: 'Actual',
          editable: false,
          flex: 1,
          align: 'right',
          format: '{0:#.##}',
          type: 'number',
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
          format: '{0:#.##}',
          type: 'number',
        },
        {
          field: 'Actual2',
          title: 'Actual',
          editable: false,
          flex: 1,
          align: 'right',
          format: '{0:#.##}',
          type: 'number',
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
          format: '{0:#.##}',
          type: 'number',
        },
        {
          field: 'Actual3',
          title: 'Actual',
          editable: false,
          flex: 1,
          align: 'right',
          format: '{0:#.##}',
          type: 'number',
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
          format: '{0:#.##}',
          type: 'number',
        },
      ],
    },
  ]

  const [loading, setLoading] = useState(false)

  const fetchData = async (type) => {
    try {
      setLoading(true)
      var res = await DataService.getAnnualProductionPlanReportData(
        keycloak,
        type,
        PLANT_ID,
        AOP_YEAR,
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
        if (type === 'maxRate') {
          const dateRegex = /^(\d{1,2}-[a-zA-Z]{3}-\d{2,4})$/

          res = res.map((item) => {
            const value = item.maxHourlyRateValue
            const num = parseFloat(value)

            return {
              ...item,
              maxHourlyRateValue:
                typeof value === 'string' &&
                !dateRegex.test(value.trim()) &&
                !isNaN(num)
                  ? num // ? keep raw number
                  : typeof value === 'number'
                    ? value // ? keep as number
                    : value,
            }
          })
        }

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
  }, [AOP_YEAR, keycloak, PLANT_ID])

  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)

      const res = await DataService.calculateAnnualProductionPlanData(
        PLANT_ID,
        AOP_YEAR,
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
          PLANT_ID,
          AOP_YEAR,
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
          PLANT_ID,
          AOP_YEAR,
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
          PLANT_ID,
          AOP_YEAR,
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
          PLANT_ID,
          AOP_YEAR,
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
          saveBtn: !isOldYear,
          allAction: true,
          showReportTitle: true,
        }}
      />

      <KendoDataTables
        rows={rowsMaxRate}
        columns={columnsMaxRate}
        permissions={{
          saveBtn: !isOldYear,
          allAction: true,
          showReportTitle: true,
        }}
        modifiedCells={modifiedCells2}
        setModifiedCells={setModifiedCells2}
        currentRowId={currentRowId2}
        setCurrentRowId={setCurrentRowId2}
        setRows={setRowsMaxRate}
        saveChanges={saveChanges2}
        titleName='Max hourly rate achieved'
      />

      <KendoDataTables
        rows={rowsOperatingHrs}
        columns={columnsOperatingHrs}
        permissions={{
          saveBtn: !isOldYear,
          allAction: true,
          showReportTitle: true,
        }}
        modifiedCells={modifiedCells3}
        setModifiedCells={setModifiedCells3}
        currentRowId={currentRowId3}
        setCurrentRowId={setCurrentRowId3}
        setRows={setRowsOperatingHrs}
        saveChanges={saveChanges3}
        titleName='Calculation of Operating hours'
      />

      <KendoDataTables
        rows={rowsAverageHourlyRate}
        columns={columnsAverageHourlyRate}
        permissions={{
          saveBtn: !isOldYear,
          allAction: true,
          showReportTitle: true,
        }}
        modifiedCells={modifiedCells4}
        setModifiedCells={setModifiedCells4}
        currentRowId={currentRowId4}
        setCurrentRowId={setCurrentRowId4}
        setRows={setRowsAverageHourlyRate}
        saveChanges={saveChanges4}
        titleName='Calculation of Average hourly rate'
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production performance comparision with last 3 years{' '}
      </Typography>
      <KendoDataTablesReports
        rows={rowsProductionPerformance}
        columns={columnsProductionPerformance}
        setRows={setRowsProductionPerformance}
        permissions={{
          textAlignment: 'center',
          showReportTitle: true,
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
