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

const MonthwiseProduction = () => {
  const keycloak = useSession()

  const thisYear = localStorage.getItem('year')
  // 1. Remark dialog state
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    console.log(row)
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  const columns = [
    {
      field: 'RowNo',
      headerName: 'SL.No',
      flex: 1,
      headerAlign: 'left',
      align: 'left',
    },
    {
      field: 'Month',
      headerName: 'Month',
      flex: 1,
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },

    // Current Year → EOE Production
    {
      field: 'EOEProdBudget', // was eoeBudgetCY
      headerName: 'Budget',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'EOEProdActual', // was eoeActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },

    // Current Year → Operating Hours
    {
      field: 'OpHrsBudget', // was opBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'OpHrsActual', // was opActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },

    // Current Year → Throughput
    {
      field: 'ThroughputBudget', // was thrBudgetCY
      headerName: 'Budget',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'ThroughputActual', // was thrActualCY
      headerName: 'Actual',
      flex: 1,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },

    // Budget Year single values
    {
      field: 'OperatingHours', // was opBudgetBY
      headerName: 'Operating Hours',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'MEGThroughput', // was megTPH
      headerName: 'MEG Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'EOThroughput', // was eoTPH
      headerName: 'EO Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'EOEThroughput', // was eoeTPH
      headerName: 'EOE Throughput, TPH',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'TotalEOE', // was totalEoeMT
      headerName: 'Total EOE, MT',
      flex: 2,
      headerAlign: 'left',
      align: 'right',
      valueFormatter: formatValueToThreeDecimals,
    },

    // (Optional) you can keep Remarks if you plan to add that later
    {
      field: 'Remark',
      headerName: 'Remark',
      minWidth: 150,
      editable: false,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
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
  const [rows, setRows] = useState()

  const defaultCustomHeightGrid1 = {
    mainBox: `${15 + (rows?.length || 0) * 5}vh`,
    otherBox: `${100 + (rows?.length || 0) * 5}%`,
  }
  const defaultCustomHeightGrid2 = {
    mainBox: `${15 + (rows?.length || 0) * 5}vh`,
    otherBox: `${100 + (rows?.length || 0) * 5}%`,
  }

  //api call
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
            isEditable: false,
          }))

          setRows(res)
        } else {
          setRows([])
        }
      } catch (err) {
        console.log(err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [year, plantId])
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )
    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])
  const defaultCustomHeight = { mainBox: '34vh', otherBox: '112%' }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ReportDataGrid
        rows={rows}
        setRows={setRows}
        title='Monthwise Production Summary'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeightGrid1,
          textAlignment: 'center',
          remarksEditable: true,
        }}
        treeData
        getTreeDataPath={(rows) => rows.path}
        defaultGroupingExpansionDepth={1} // expand only first level by default
        disableSelectionOnClick
        columnGroupingModel={columnGroupingModel}
        processRowUpdate={processRowUpdate}
        remarkDialogOpen={remarkDialogOpen}
        unsavedChangesRef={unsavedChangesRef}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
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
          // dynamicGridHeight: true,
          needTotal: true,
        }}
      />
    </Box>
  )
}

export default MonthwiseProduction
