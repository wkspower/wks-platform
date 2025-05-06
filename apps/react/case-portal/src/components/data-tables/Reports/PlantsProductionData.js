import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useMemo } from 'react'
import {
  Backdrop,
  CircularProgress,
} from '../../../../node_modules/@mui/material/index'

const PlantsProductionSummary = () => {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')

  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const dataAPI = {
    columns: [
      { header: 'SL.No', field: 'RowNo' },

      {
        header: 'Item',
        children: [{ header: 'Production Volume', field: 'Particulates' }],
      },
      { header: 'Unit', field: 'UOM' },
      {
        header: oldYear || 'old-Year',
        children: [
          { header: 'Budget', field: 'BudgetPrevYear' },
          { header: 'Actual', field: 'ActualPrevYear' },
        ],
      },
      {
        header: thisYear || 'current-Year',
        children: [{ header: 'Budget', field: 'BudgetCurrentYear' }],
      },
      {
        header: 'Variance wrt current year budget',
        children: [
          { header: 'MT', field: 'VarBudgetMT' },
          { header: '%', field: 'VarBudgetPer' },
        ],
      },
      {
        header: 'Variance wrt current year actuals',
        children: [
          { header: 'MT', field: 'VarActualMT' },
          { header: '%', field: 'VarActualPer' },
        ],
      },
      { header: 'Remarks', field: 'Remark' },
    ],
    // rows: [
    //   {
    //     id: 0,
    //     RowNo: 1,
    //     Particulates: 'EOE',
    //     UOM: 'MT',
    //     BudgetPrevYear: 1200,
    //     ActualPrevYear: 1100,
    //     BudgetCurrentYear: 1250,
    //     VarBudgetMT: -70,
    //     VarBudgetPer: '-5.6%',
    //     VarActualMT: -100,
    //     VarActualPer: '-8.3%',
    //     Remark: 'Catalyst replacement due',
    //   },
    //   {
    //     id: 1,
    //     RowNo: 1,
    //     Particulates: 'MEG',
    //     UOM: 'MT',
    //     BudgetPrevYear: 900,
    //     ActualPrevYear: 860,
    //     BudgetCurrentYear: 920,
    //     VarBudgetMT: -120,
    //     VarBudgetPer: '-5.6%',
    //     VarActualMT: -40,
    //     VarActualPer: '-4.7%',
    //     Remark: 'Operating at 95% capacity',
    //   },
    // ],
  }

  const apiCols = dataAPI.columns
  // const rows = dataAPI.rows

  // 1. Flat columns for rendering as before
  const columns = useMemo(() => {
    return apiCols.flatMap((col) =>
      col.children
        ? col.children.map((child) => ({
            field: child.field,
            headerName: child.header,
            // children typically numeric → right align
            align: 'right',
            headerAlign: 'right',
            flex: 1, // share leftover space
            minWidth: 100,
            cellClassName: 'rightAlign',
          }))
        : [
            {
              field: col.field,
              headerName: col.header,
              width: col.field === 'slNo' ? 80 : 150,
              // slNo tiny, Particulates+UOM moderate
              flex: col.field === 'Particulates' ? 2 : undefined,
              minWidth: col.field === 'Particulates' ? 120 : undefined,
              align: col.field === 'UOM' ? 'center' : 'left',
              headerAlign: col.field === 'UOM' ? 'center' : 'left',
            },
          ],
    )
  }, [apiCols])

  // 2. Build a *complete* columnGroupingModel
  const columnGroupingModel = useMemo(() => {
    // 2a) Ungrouped leaves
    const leaves = apiCols
      .filter((col) => !col.children)
      .map((col) => ({ field: col.field }))

    // 2b) Groups, with children as leaf-objects
    const groups = apiCols
      .filter((col) => col.children)
      .map((col) => ({
        groupId: col.header,
        headerName: col.header,
        children: col.children.map((child) => ({ field: child.field })),
      }))

    // You can choose merge order (leaves first or groups first)
    return [...leaves, ...groups]
  }, [apiCols])
  const [row, setRow] = useState()
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        var res = await DataService.getPlantProductionSummary(keycloak)

        console.log(res)
        if (res?.code == 200) {
          res = res?.data.map((Particulates, index) => ({
            ...Particulates,
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
  // console.log(row)

  const defaultCustomHeight = { mainBox: '64vh', otherBox: '90%' }

  return (
    <Box sx={{ height: ' 635px', width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <ReportDataGrid
        rows={row}
        title='Plants Production Summary'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
          saveBtn: true,
          textAlignment: 'center',
        }}
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

export default PlantsProductionSummary
