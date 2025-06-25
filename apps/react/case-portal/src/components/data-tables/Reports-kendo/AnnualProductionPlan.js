import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import { useEffect, useState } from 'react'
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

  const columnsAssumptions = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: true,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'part1',
      headerName: 'Assumptions & remarks',
      editable: false,
      flex: 1,
    },
  ]

  const columnsMaxRate = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: true,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'part1',
      headerName: 'Max hourly rate achieved',
      editable: false,
      flex: 1,
    },
    {
      field: 'part2',
      headerName: 'Value',
      editable: false,
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
    },
    { field: 'part3', headerName: 'UOM', editable: false, flex: 1 },
  ]

  const columnsOperatingHrs = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: true,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'part1',
      headerName: 'Calculation of Operating hours',
      editable: false,
      flex: 1,
    },
    {
      field: 'part2',
      headerName: 'Value',
      editable: false,
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
    },
    {
      field: 'part3',
      headerName: 'Hours',
      editable: false,
      flex: 1,
      align: 'right',
    },
  ]

  const columnsAverageHourlyRate = [
    {
      field: 'sno',
      headerName: 'SL.No',
      editable: true,
      widthT: 100,
      align: 'right',
    },
    {
      field: 'Throughput',
      headerName: 'Throughput  limiting causes',
      editable: false,
      flex: 1,
    },
    {
      field: 'OperatingHrs',
      headerName: 'Achievable Hourly rate',
      editable: false,
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
    },
    {
      field: 'HourlyRate',
      headerName: 'Op. Hrs',
      editable: false,
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
    },
    {
      field: 'PeriodFrom',
      headerName: 'Period from',
      editable: false,
      flex: 1,
    },
    { field: 'PeriodTo', headerName: 'Period to', editable: false, flex: 1 },
  ]

  const year4 = localStorage.getItem('year')
  const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
  const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
  const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`

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
          editable: true,
          flex: 1,
          align: 'right',
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
          id: index,
          isEditable: false,
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

      <KendoDataTablesReports
        rows={rowsAssumptions}
        columns={columnsAssumptions}
        handleCalculate={handleCalculate}
        title='Plant Production Plan (T-15) - Assumptions & remarks'
        // title='Plant Contribution (T-21)- MEG\nProduct mix and Production'
        permissions={{
          showWorkFlowBtns: true,
          showCalculate: false,
          showTitle: true,
        }}
      />

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Max hourly rate achieved{' '}
      </Typography>
      <KendoDataTablesReports rows={rowsMaxRate} columns={columnsMaxRate} />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Operating hours{' '}
      </Typography>
      <KendoDataTablesReports
        rows={rowsOperatingHrs}
        columns={columnsOperatingHrs}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Average hourly rate{' '}
      </Typography>
      <KendoDataTablesReports
        rows={rowsAverageHourlyRate}
        columns={columnsAverageHourlyRate}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production performance comparision with last 3 years{' '}
      </Typography>
      <KendoDataTablesReports
        rows={rowsProductionPerformance}
        columns={columnsProductionPerformance}
        columnGroupingModel={columnGroupingModel}
        permissions={{
          textAlignment: 'center',
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

export default AnnualProductionPlan
