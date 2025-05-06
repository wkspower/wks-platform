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
  // const rows = [
  //   // Sl.No 1 group header
  //   {
  //     id: '1-group',
  //     slNo: '1',
  //     item: 'Production Volumes',
  //     path: ['1'],
  //     // group‐headers don’t need numeric fields, but you can stub them:
  //     budgetRef1: null,
  //     actualRef1: null,
  //     budgetRef2: null,
  //     mtRef2: null,
  //     pctRef2: null,
  //     varBudgetMT: null,
  //     varBudgetPct: null,
  //     varActualMT: null,
  //     varActualPct: null,
  //     remarks: '',
  //   },

  //   // 1.1 EOE
  //   {
  //     id: '1-EOE',
  //     slNo: '1',
  //     item: 'EOE',
  //     unit: 'MT',
  //     path: ['1', 'EOE'],
  //     budgetRef1: 1200,
  //     actualRef1: 1100,
  //     budgetRef2: 1250,
  //     mtRef2: 1180,
  //     pctRef2: '94.4%',
  //     varBudgetMT: -70,
  //     varBudgetPct: '-5.6%',
  //     varActualMT: -100,
  //     varActualPct: '-8.3%',
  //     remarks: 'Catalyst replacement due',
  //   },
  //   // 1.2 MEG
  //   {
  //     id: '1-MEG',
  //     slNo: '1',
  //     item: 'MEG',
  //     unit: 'MT',
  //     path: ['1', 'MEG'],
  //     budgetRef1: 900,
  //     actualRef1: 860,
  //     budgetRef2: 920,
  //     mtRef2: 880,
  //     pctRef2: '95.7%',
  //     varBudgetMT: -40,
  //     varBudgetPct: '-4.3%',
  //     varActualMT: -40,
  //     varActualPct: '-4.7%',
  //     remarks: 'Operating at 95% capacity',
  //   },
  //   // 1.3 EO
  //   {
  //     id: '1-EO',
  //     slNo: '1',
  //     item: 'EO',
  //     unit: 'MT',
  //     path: ['1', 'EO'],
  //     budgetRef1: 600,
  //     actualRef1: 580,
  //     budgetRef2: 620,
  //     mtRef2: 590,
  //     pctRef2: '95.2%',
  //     varBudgetMT: -30,
  //     varBudgetPct: '-4.8%',
  //     varActualMT: -20,
  //     varActualPct: '-3.3%',
  //     remarks: 'Minor downtime',
  //   },
  //   // 1.4 Sum
  //   {
  //     id: '1-SUM',
  //     slNo: '1',
  //     item: 'Production (Sum of main products)',
  //     unit: 'MT',
  //     path: ['1', 'Sum'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },

  //   // Sl.No 2: Operating hours
  //   {
  //     id: '2',
  //     slNo: '2',
  //     item: 'Operating hours',
  //     unit: 'Hrs',
  //     path: ['2'],
  //     budgetRef1: 7200,
  //     actualRef1: 7000,
  //     budgetRef2: 7300,
  //     mtRef2: 7100,
  //     pctRef2: '97.3%',
  //     varBudgetMT: -200,
  //     varBudgetPct: '-2.7%',
  //     varActualMT: -300,
  //     varActualPct: '-4.1%',
  //     remarks: '',
  //   },

  //   // Sl.No 3: Production rate
  //   {
  //     id: '3',
  //     slNo: '3',
  //     item: 'Production rate',
  //     unit: 'TPH',
  //     path: ['3'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },

  //   // Sl.No 4: Net Sales realisation
  //   {
  //     id: '4',
  //     slNo: '4',
  //     item: 'Net Sales realisation',
  //     unit: 'Rs/MT',
  //     path: ['4'],
  //     budgetRef1: 50000,
  //     actualRef1: 49500,
  //     budgetRef2: 51000,
  //     mtRef2: 50000,
  //     pctRef2: '98.0%',
  //     varBudgetMT: -1000,
  //     varBudgetPct: '-2.0%',
  //     varActualMT: -1500,
  //     varActualPct: '-3.0%',
  //     remarks: '',
  //   },

  //   // Sl.No 5 group header
  //   {
  //     id: '5-group',
  //     slNo: '5',
  //     item: 'Cost of production',
  //     path: ['5'],
  //     budgetRef1: null,
  //     actualRef1: null,
  //     budgetRef2: null,
  //     mtRef2: null,
  //     pctRef2: null,
  //     varBudgetMT: null,
  //     varBudgetPct: null,
  //     varActualMT: null,
  //     varActualPct: null,
  //     remarks: '',
  //   },

  //   // 5.1 Raw material
  //   {
  //     id: '5-RM',
  //     slNo: '5',
  //     item: 'Raw material',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Raw material'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.2 By product credit
  //   {
  //     id: '5-By',
  //     slNo: '5',
  //     item: 'By product credit',
  //     unit: 'Rs/MT',
  //     path: ['5', 'By product credit'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.3 Net raw-mat
  //   {
  //     id: '5-NetRM',
  //     slNo: '5',
  //     item: 'Rawmaterial net of byproduct',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Rawmaterial net of byproduct'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.4 Cat and chem
  //   {
  //     id: '5-Cat',
  //     slNo: '5',
  //     item: 'Cat and chem',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Cat and chem'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.5 Utilities
  //   {
  //     id: '5-Util',
  //     slNo: '5',
  //     item: 'Utilities',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Utilities'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.6 Others
  //   {
  //     id: '5-Oth',
  //     slNo: '5',
  //     item: 'Others (Stores & Spares, etc)',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Others (Stores & Spares, etc)'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.7 Total conversion cost
  //   {
  //     id: '5-Conv',
  //     slNo: '5',
  //     item: 'Total conversion cost',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Total conversion cost'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  //   // 5.8 Total cost of production
  //   {
  //     id: '5-Total',
  //     slNo: '5',
  //     item: 'Total cost of production',
  //     unit: 'Rs/MT',
  //     path: ['5', 'Total cost of production'],
  //     budgetRef1: 0,
  //     actualRef1: 0,
  //     budgetRef2: 0,
  //     mtRef2: 0,
  //     pctRef2: '0%',
  //     varBudgetMT: 0,
  //     varBudgetPct: '0%',
  //     varActualMT: 0,
  //     varActualPct: '0%',
  //     remarks: '',
  //   },
  // ]

  // const columns = [
  //   {
  //     field: 'slNo',
  //     headerName: 'Sl. No.',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'item',
  //     headerName: 'Item',
  //     flex: 1,
  //     minWidth: 180,
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'unit',
  //     headerName: 'Unit',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'budgetRef1',
  //     headerName: 'Budget',
  //     width: 100,
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'actualRef1',
  //     headerName: 'Actual',
  //     width: 100,
  //     headerAlign: 'left',
  //   },
  //   {
  //     field: 'budgetRef2',
  //     headerName: 'Budget',
  //     width: 100,
  //     headerAlign: 'left',
  //   },

  //   {
  //     field: 'varBudgetMT',
  //     headerName: 'MT',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'varBudgetPct',
  //     headerName: '%',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'varActualMT',
  //     headerName: 'MT',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'varActualPct',
  //     headerName: '%',
  //     width: 80,
  //     headerAlign: 'left',
  //     align: 'left',
  //   },
  //   {
  //     field: 'remarks',
  //     headerName: 'Remarks',
  //     flex: 1,
  //     minWidth: 200,
  //     headerAlign: 'left',
  //   },
  // ]
  // Column grouping to match your two “#REF!” blocks and the two variance blocks
  // const columnGroupingModel = [
  //   {
  //     groupId: 'part1',
  //     headerName: 'Part1',
  //     children: [{ field: 'budgetRef1' }, { field: 'actualRef1' }],
  //   },
  //   {
  //     groupId: 'part2',
  //     headerName: 'Part2',
  //     children: [{ field: 'budgetRef2' }],
  //   },
  //   {
  //     groupId: 'varBud',
  //     headerName: 'Variance wrt current year budget',
  //     children: [{ field: 'varBudgetMT' }, { field: 'varBudgetPct' }],
  //   },
  //   {
  //     groupId: 'varAct',
  //     headerName: 'Variance wrt current year actuals',
  //     children: [{ field: 'varActualMT' }, { field: 'varActualPct' }],
  //   },
  //   // {
  //   //   groupId: 'rem',
  //   //   headerName: 'Remarks',
  //   //   children: [{ field: 'remarks' }],
  //   // },
  // ]
  const dataAPI = {
    columns: [
      { header: 'Sl. No', field: 'sno' },
      // { header: 'Item', field: 'item' },
      {
        header: 'Item',
        children: [{ header: 'Production Volume', field: 'item' }],
      },
      { header: 'Unit', field: 'unit' },
      {
        header: 'Part1',
        children: [
          { header: 'Budget', field: 'part1Budget' },
          { header: 'Actual', field: 'part1Actual' },
        ],
      },
      {
        header: 'Part2',
        children: [{ header: 'Budget', field: 'part2Budget' }],
      },
      {
        header: 'Variance wrt current year budget',
        children: [
          { header: 'MT', field: 'varBudgetMT' },
          { header: '%', field: 'varBudgetPct' },
        ],
      },
      {
        header: 'Variance wrt current year actuals',
        children: [
          { header: 'MT', field: 'varActualMT' },
          { header: '%', field: 'varActualPct' },
        ],
      },
      { header: 'Remarks', field: 'remarks' },
    ],
    rows: [
      {
        id: 0,
        sno: 1,
        item: 'EOE',
        unit: 'MT',
        part1Budget: 1200,
        part1Actual: 1100,
        part2Budget: 1250,
        varBudgetMT: -70,
        varBudgetPct: '-5.6%',
        varActualMT: -100,
        varActualPct: '-8.3%',
        remarks: 'Catalyst replacement due',
      },
      {
        id: 1,
        sno: 1,
        item: 'MEG',
        unit: 'MT',
        part1Budget: 900,
        part1Actual: 860,
        part2Budget: 920,
        varBudgetMT: -120,
        varBudgetPct: '-5.6%',
        varActualMT: -40,
        varActualPct: '-4.7%',
        remarks: 'Operating at 95% capacity',
      },
    ],
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
              // slNo tiny, item+unit moderate
              flex: col.field === 'item' ? 2 : undefined,
              minWidth: col.field === 'item' ? 120 : undefined,
              align: col.field === 'unit' ? 'center' : 'left',
              headerAlign: col.field === 'unit' ? 'center' : 'left',
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
          res = res?.data.map((item, index) => ({
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
