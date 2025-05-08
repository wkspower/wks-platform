import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { truncateRemarks } from 'utils/remarksUtils'

const AnnualProductionPlan = () => {
  const keycloak = useSession()

  const thisYear = localStorage.getItem('year')

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
  let oldYear4 = ''
  if (oldYear3 && oldYear3.includes('-')) {
    const [start, end] = oldYear3.split('-').map(Number)
    oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const [rowsAssumptions, setRowsassumptions] = useState([])
  const [rowsMaxRate, setRowsMaxRate] = useState([])
  const [rowsOperatingHrs, setRowsOperatingHrs] = useState([])
  const [rowsAverageHourlyRate, setRowsAverageHourlyRate] = useState([])
  const [rowsProductionPerformance, setRowsProductionPerformance] = useState([])

  const columnsAssumptions = [
    { field: 'sno', headerName: 'SL.No', editable: true, minWidth: 20 },
    {
      field: 'part1',
      headerName: 'Assumptions & remarks',
      editable: false,
      flex: 1,
    },
  ]

  const columnsMaxRate = [
    { field: 'sno', headerName: 'SL.No', editable: true, minWidth: 50 },
    {
      field: 'part1',
      headerName: 'Max hourly rate achieved',
      editable: false,
      flex: 1,
    },
    { field: 'part2', headerName: 'Value', editable: false, flex: 1 },
    { field: 'part3', headerName: 'UOM', editable: false, flex: 1 },
  ]

  const columnsOperatingHrs = [
    { field: 'sno', headerName: 'SL.No', editable: true, minWidth: 50 },
    {
      field: 'part1',
      headerName: 'Calculation of Operating hours',
      editable: false,
      flex: 1,
    },
    { field: 'part2', headerName: 'Value', editable: false, flex: 1 },
    { field: 'part3', headerName: 'Hours', editable: false, flex: 1 },
  ]

  const columnsAverageHourlyRate = [
    { field: 'sno', headerName: 'SL.No', editable: true, minWidth: 50 },
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
    },
    {
      field: 'HourlyRate',
      headerName: 'Op. Hrs',
      editable: false,
      flex: 1,
    },
    {
      field: 'PeriodFrom',
      headerName: 'Period from',
      editable: false,
      flex: 1,
    },
    { field: 'PeriodTo', headerName: 'Period to', editable: false, flex: 1 },
  ]

  const columnsProductionPerformance = [
    { field: 'sno', headerName: 'SL.No', editable: false, minWidth: 50 },
    { field: 'Item', headerName: 'Item', editable: false, flex: 1 },

    { field: 'Budget1', headerName: 'Budget', editable: false, flex: 1 },
    { field: 'Actual1', headerName: 'Actual', editable: false, flex: 1 },

    { field: 'Budget2', headerName: 'Budget', editable: false, flex: 1 },
    { field: 'Actual2', headerName: 'Actual', editable: false, flex: 1 },

    { field: 'Budget3', headerName: 'Budget', editable: false, flex: 1 },
    { field: 'Actual3', headerName: 'Actual', editable: false, flex: 1 },

    { field: 'Budget4', headerName: 'Budget', editable: false, flex: 1 },
  ]

  const year4 = localStorage.getItem('year')
  const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
  const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
  const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`

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

  useEffect(() => {
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
    fetchData('assumptions')
    fetchData('maxRate')
    fetchData('OperatingHrs')
    fetchData('AverageHourlyRate')
    fetchData('ProductionPerformance')
  }, [year, plantId])

  return (
    <Box sx={{ height: 'auto', width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Assumptions & remarks{' '}
      </Typography>

      <ReportDataGrid rows={rowsAssumptions} columns={columnsAssumptions} />

      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Max hourly rate achieved{' '}
      </Typography>
      <ReportDataGrid rows={rowsMaxRate} columns={columnsMaxRate} />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Operating hours{' '}
      </Typography>
      <ReportDataGrid rows={rowsOperatingHrs} columns={columnsOperatingHrs} />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Average hourly rate{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsAverageHourlyRate}
        columns={columnsAverageHourlyRate}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production performance comparision with last 3 years{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsProductionPerformance}
        columns={columnsProductionPerformance}
        columnGroupingModel={columnGroupingModel}
        permissions={{
          textAlignment: 'center',
        }}
      />
    </Box>
  )
}

export default AnnualProductionPlan
