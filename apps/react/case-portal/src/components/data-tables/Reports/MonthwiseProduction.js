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
  const columns = [
    {
      field: 'sno',
      headerName: 'Sl. No.',
      width: 90,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'month',
      headerName: 'Month',
      width: 120,
      headerAlign: 'left',
    },

    // Current Year → EOE Production
    {
      field: 'productionBudget', // was eoeBudgetCY
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'productionActual', // was eoeActualCY
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Operating Hours
    {
      field: 'operatingBudget', // was opBudgetCY
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'operatingActual', // was opActualCY
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Throughput
    {
      field: 'throughputBudget', // was thrBudgetCY
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'throughputActual', // was thrActualCY
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Budget Year single values
    {
      field: 'operatingHours', // was opBudgetBY
      headerName: 'Operating Hours',
      width: 140,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'megThroughput', // was megTPH
      headerName: 'MEG Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'eoThroughput', // was eoTPH
      headerName: 'EO Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'eoeThroughput', // was eoeTPH
      headerName: 'EOE Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'totalEOE', // was totalEoeMT
      headerName: 'Total EOE, MT',
      width: 140,
      headerAlign: 'left',
      align: 'left',
    },

    // (Optional) you can keep Remarks if you plan to add that later
    {
      field: 'remarks',
      headerName: 'Remarks',
      flex: 1,
      minWidth: 200,
      headerAlign: 'left',
    },
  ]

  const columnGroupingModel = [
    {
      groupId: 'currentYear',
      headerName: 'Current Year',
      children: [
        {
          groupId: 'cy-eoe',
          headerName: 'EOE Production, MT',
          children: [
            { field: 'productionBudget' }, // was eoeBudgetCY
            { field: 'productionActual' }, // was eoeActualCY
          ],
        },
        {
          groupId: 'cy-op',
          headerName: 'Operating Hours',
          children: [
            { field: 'operatingBudget' }, // was opBudgetCY
            { field: 'operatingActual' }, // was opActualCY
          ],
        },
        {
          groupId: 'cy-thr',
          headerName: 'Throughput, TPH',
          children: [
            { field: 'throughputBudget' }, // was thrBudgetCY
            { field: 'throughputActual' }, // was thrActualCY
          ],
        },
      ],
    },
    {
      groupId: 'budgetYear',
      headerName: 'Budget Year',
      children: [
        { field: 'operatingHours' }, // was opBudgetBY
        { field: 'megThroughput' }, // was megTPH
        { field: 'eoThroughput' }, // was eoTPH
        { field: 'eoeThroughput' }, // was eoeTPH
        { field: 'totalEOE' }, // was totalEoeMT
      ],
    },
  ]

  const defaultCustomHeight = { mainBox: '38vh', otherBox: '90%' }

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
