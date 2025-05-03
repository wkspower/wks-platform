import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'

const MonthwiseProduction = () => {
  const columns = [
    {
      field: 'slNo',
      headerName: 'Sl. No.',
      width: 90,
      headerAlign: 'left',
      align: 'left',
    },
    { field: 'month', headerName: 'Month', width: 120, headerAlign: 'left' },

    // Current Year → EOE Production
    {
      field: 'eoeBudgetCY',
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'eoeActualCY',
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Operating Hours
    {
      field: 'opBudgetCY',
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'opActualCY',
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Current Year → Throughput
    {
      field: 'thrBudgetCY',
      headerName: 'Budget',
      width: 100,
      headerAlign: 'left',
      align: 'left',
      textAlign: 'left',
    },
    {
      field: 'thrActualCY',
      headerName: 'Actual',
      width: 100,
      headerAlign: 'left',
      align: 'left',
    },

    // Budget Year single values
    {
      field: 'opBudgetBY',
      headerName: 'Operating Hours',
      width: 140,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'megTPH',
      headerName: 'MEG Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'eoTPH',
      headerName: 'EO Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
      textAlign: 'left',
    },
    {
      field: 'eoeTPH',
      headerName: 'EOE Throughput, TPH',
      width: 160,
      headerAlign: 'left',
      align: 'left',
      textAlign: 'left',
    },
    {
      field: 'totalEoeMT',
      headerName: 'Total EOE, MT',
      width: 140,
      headerAlign: 'left',
      align: 'left',
      textAlign: 'left',
    },

    {
      field: 'remarks',
      headerName: 'Remarks',
      flex: 1,
      minWidth: 200,
      headerAlign: 'left',
    },
  ]

  // 2) Column grouping model
  const columnGroupingModel = [
    {
      groupId: 'currentYear',
      headerName: 'Current Year ',
      children: [
        {
          groupId: 'cy-eoe',
          headerName: 'EOE Production, MT',
          children: [{ field: 'eoeBudgetCY' }, { field: 'eoeActualCY' }],
        },
        {
          groupId: 'cy-op',
          headerName: 'Operating Hours',
          children: [{ field: 'opBudgetCY' }, { field: 'opActualCY' }],
        },
        {
          groupId: 'cy-thr',
          headerName: 'Throughput, TPH',
          children: [{ field: 'thrBudgetCY' }, { field: 'thrActualCY' }],
        },
      ],
    },
    {
      groupId: 'budgetYear',
      headerName: 'Budget Year ',
      children: [
        { field: 'opBudgetBY' },
        { field: 'megTPH' },
        { field: 'eoTPH' },
        { field: 'eoeTPH' },
        { field: 'totalEoeMT' },
      ],
    },
    // {
    //   groupId: 'rem',
    //   headerName: 'Remarks',
    //   children: [{ field: 'remarks' }],
    // },
  ]

  const rows = [
    {
      id: 1,
      slNo: 1,
      month: 'April',
      eoeBudgetCY: 150,
      eoeActualCY: 140,
      opBudgetCY: 720,
      opActualCY: 700,
      thrBudgetCY: 50,
      thrActualCY: 48,
      opBudgetBY: 730,
      megTPH: 45,
      eoTPH: 40,
      eoeTPH: 55,
      totalEoeMT: 145,
      remarks: '',
    },
    {
      id: 2,
      slNo: 2,
      month: 'May',
      eoeBudgetCY: 160,
      eoeActualCY: 155,
      opBudgetCY: 710,
      opActualCY: 705,
      thrBudgetCY: 52,
      thrActualCY: 50,
      opBudgetBY: 720,
      megTPH: 47,
      eoTPH: 42,
      eoeTPH: 57,
      totalEoeMT: 152,
      remarks: '',
    },
    {
      id: 3,
      slNo: 3,
      month: 'June',
      eoeBudgetCY: 155,
      eoeActualCY: 150,
      opBudgetCY: 730,
      opActualCY: 725,
      thrBudgetCY: 51,
      thrActualCY: 49,
      opBudgetBY: 740,
      megTPH: 46,
      eoTPH: 41,
      eoeTPH: 56,
      totalEoeMT: 150,
      remarks: '',
    },
    {
      id: 4,
      slNo: 4,
      month: 'July',
      eoeBudgetCY: 148,
      eoeActualCY: 145,
      opBudgetCY: 700,
      opActualCY: 695,
      thrBudgetCY: 49,
      thrActualCY: 47,
      opBudgetBY: 710,
      megTPH: 44,
      eoTPH: 39,
      eoeTPH: 54,
      totalEoeMT: 143,
      remarks: 'Catalyst swap',
    },
    {
      id: 5,
      slNo: 5,
      month: 'August',
      eoeBudgetCY: 162,
      eoeActualCY: 160,
      opBudgetCY: 740,
      opActualCY: 735,
      thrBudgetCY: 53,
      thrActualCY: 52,
      opBudgetBY: 750,
      megTPH: 48,
      eoTPH: 43,
      eoeTPH: 58,
      totalEoeMT: 158,
      remarks: '',
    },
    {
      id: 6,
      slNo: 6,
      month: 'September',
      eoeBudgetCY: 158,
      eoeActualCY: 155,
      opBudgetCY: 730,
      opActualCY: 725,
      thrBudgetCY: 52,
      thrActualCY: 50,
      opBudgetBY: 740,
      megTPH: 47,
      eoTPH: 42,
      eoeTPH: 56,
      totalEoeMT: 154,
      remarks: '',
    },
    {
      id: 7,
      slNo: 7,
      month: 'October',
      eoeBudgetCY: 150,
      eoeActualCY: 148,
      opBudgetCY: 720,
      opActualCY: 715,
      thrBudgetCY: 50,
      thrActualCY: 49,
      opBudgetBY: 730,
      megTPH: 45,
      eoTPH: 40,
      eoeTPH: 55,
      totalEoeMT: 146,
      remarks: '',
    },
    {
      id: 8,
      slNo: 8,
      month: 'November',
      eoeBudgetCY: 145,
      eoeActualCY: 140,
      opBudgetCY: 710,
      opActualCY: 705,
      thrBudgetCY: 48,
      thrActualCY: 46,
      opBudgetBY: 720,
      megTPH: 44,
      eoTPH: 39,
      eoeTPH: 54,
      totalEoeMT: 142,
      remarks: '',
    },
    {
      id: 9,
      slNo: 9,
      month: 'December',
      eoeBudgetCY: 155,
      eoeActualCY: 150,
      opBudgetCY: 720,
      opActualCY: 715,
      thrBudgetCY: 51,
      thrActualCY: 49,
      opBudgetBY: 730,
      megTPH: 46,
      eoTPH: 41,
      eoeTPH: 56,
      totalEoeMT: 151,
      remarks: '',
    },
    {
      id: 10,
      slNo: 10,
      month: 'January',
      eoeBudgetCY: 160,
      eoeActualCY: 158,
      opBudgetCY: 730,
      opActualCY: 725,
      thrBudgetCY: 52,
      thrActualCY: 51,
      opBudgetBY: 740,
      megTPH: 47,
      eoTPH: 42,
      eoeTPH: 57,
      totalEoeMT: 156,
      remarks: '',
    },
    {
      id: 11,
      slNo: 11,
      month: 'February',
      eoeBudgetCY: 158,
      eoeActualCY: 155,
      opBudgetCY: 725,
      opActualCY: 720,
      thrBudgetCY: 51,
      thrActualCY: 50,
      opBudgetBY: 735,
      megTPH: 46,
      eoTPH: 41,
      eoeTPH: 56,
      totalEoeMT: 154,
      remarks: '',
    },
    {
      id: 12,
      slNo: 12,
      month: 'March',
      eoeBudgetCY: 152,
      eoeActualCY: 150,
      opBudgetCY: 715,
      opActualCY: 710,
      thrBudgetCY: 50,
      thrActualCY: 49,
      opBudgetBY: 725,
      megTPH: 45,
      eoTPH: 40,
      eoeTPH: 55,
      totalEoeMT: 148,
      remarks: '',
    },
    {
      id: 13,
      slNo: '',
      month: 'Total',
      eoeBudgetCY: 1863,
      eoeActualCY: 1731,
      opBudgetCY: 8705,
      opActualCY: 8575,
      thrBudgetCY: 608,
      thrActualCY: 588,
      opBudgetBY: 8790,
      megTPH: 565,
      eoTPH: 465,
      eoeTPH: 668,
      totalEoeMT: 1762,
      remarks: '',
    },
  ]

  const defaultCustomHeight = { mainBox: '84vh', otherBox: '90%' }

  return (
    <Box sx={{ height: 500, width: '100%' }}>
      <ReportDataGrid
        rows={rows}
        title='Monthwise Production Summary'
        columns={columns}
        permissions={{ customHeight: defaultCustomHeight, saveBtn: true }}
        treeData
        getTreeDataPath={(row) => row.path}
        defaultGroupingExpansionDepth={1} // expand only first level by default
        disableSelectionOnClick
        columnGroupingModel={columnGroupingModel}
        experimentalFeatures
      />
    </Box>
  )
}

export default MonthwiseProduction
