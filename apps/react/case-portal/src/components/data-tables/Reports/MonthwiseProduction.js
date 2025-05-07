import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import {
  Backdrop,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import ProductionNorms from '../ProductionNorms'
import { useEffect, useMemo, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const MonthwiseProduction = () => {
  const keycloak = useSession()

  const thisYear = localStorage.getItem('year')

  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  const columns = [
    {
      field: 'RowNo',
      headerName: 'Sl. No.',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'Month',
      headerName: 'Month',
      flex: 1,
      headerAlign: 'left',
    },

    // Current Year → EOE Production
    {
      field: 'EOEProdBudget', // was eoeBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'productionActual', // was eoeActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Operating Hours
    {
      field: 'OpHrsBudget', // was opBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'OpHrsActual', // was opActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Throughput
    {
      field: 'ThroughputBudget', // was thrBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'ThroughputActual', // was thrActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },

    // Budget Year single values
    {
      field: 'OperatingHours', // was opBudgetBY
      headerName: 'Operating Hours',
      flex: 2,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'MEGThroughput', // was megTPH
      headerName: 'MEG Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'EOThroughput', // was eoTPH
      headerName: 'EO Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'EOEThroughput', // was eoeTPH
      headerName: 'EOE Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'TotalEOE', // was totalEoeMT
      headerName: 'Total EOE, MT',
      flex: 2,
      headerAlign: 'left',
      align: 'left',
    },

    // (Optional) you can keep Remarks if you plan to add that later
    {
      field: 'remarks',
      headerName: 'Remarks',
      flex: 2,
      // minWidth: 200,
      headerAlign: 'left',
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'currentYear',
      headerName: oldYear,
      children: [
        {
          groupId: 'cy-eoe',
          headerName: 'EOE Production, MT',
          children: [
            { field: 'EOEProdBudget' }, // was eoeBudgetCY
            { field: 'productionActual' }, // was eoeActualCY
          ],
        },
        {
          groupId: 'cy-op',
          headerName: 'Operating Hours',
          children: [
            { field: 'OpHrsBudget' }, // was opBudgetCY
            { field: 'OpHrsActual' }, // was opActualCY
          ],
        },
        {
          groupId: 'cy-thr',
          headerName: 'Throughput, TPH',
          children: [
            { field: 'ThroughputBudget' }, // was thrBudgetCY
            { field: 'ThroughputActual' }, // was thrActualCY
          ],
        },
      ],
    },
    {
      groupId: 'budgetYear',
      headerName: thisYear,
      children: [
        { field: 'OperatingHours' }, // was opBudgetBY
        { field: 'MEGThroughput' }, // was megTPH
        { field: 'EOThroughput' }, // was eoTPH
        { field: 'EOEThroughput' }, // was eoeTPH
        { field: 'TotalEOE' }, // was totalEoeMT
      ],
    },
  ]

  const defaultCustomHeight = { mainBox: '35vh', otherBox: '110%' }

  //api call
  const [row, setRow] = useState()
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        var res = await DataService.getMonthWiseSummary(keycloak)

        console.log(res)
        if (res?.code == 200) {
          res = res?.data?.data.map((item, index) => ({
            ...item,
            id: index,
          }))

          setRow(res)
        } else {
          setRow([])
        }
      } catch (err) {
        console.log(err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [year, plantId])

  return (
    <Box sx={{ height: 500, width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ReportDataGrid
        rows={row}
        title='Monthwise Production Summary'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
          textAlignment: 'center',
        }}
        treeData
        getTreeDataPath={(row) => row.path}
        defaultGroupingExpansionDepth={1} // expand only first level by default
        disableSelectionOnClick
        columnGroupingModel={columnGroupingModel}
        experimentalFeatures
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Main Products - Production for the budget year{' '}
      </Typography>
      <ProductionNorms
        permissions={{
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: false,
          showCalculate: false,
          customHeight: defaultCustomHeight,
          needTotal: true,
        }}
      />
    </Box>
  )
}

export default MonthwiseProduction
