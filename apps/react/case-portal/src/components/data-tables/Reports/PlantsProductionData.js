import { Box } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { useMemo } from 'react'
import {
  Backdrop,
  CircularProgress,
  Tooltip,
} from '../../../../node_modules/@mui/material/index'
import { truncateRemarks } from 'utils/remarksUtils'

const PlantsProductionSummary = () => {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const [rows, setRows] = useState()

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

  const dataAPI = {
    columns: [
      { header: 'SL.No', field: 'RowNo', flex: 0.5 },

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
          { header: '%', field: 'VarBudgetPer', type: 'number' },
        ],
      },
      {
        header: 'Variance wrt current year actuals',
        children: [
          { header: 'MT', field: 'VarActualMT' },
          { header: '%', field: 'VarActualPer', type: 'number' },
        ],
      },
      { header: 'Remarks', field: 'Remark', editable: true },
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
  // const columns = useMemo(() => {
  //   return apiCols.flatMap(
  //     (col) =>
  //       col.children
  //         ? col.children.map((child) => ({
  //             field: child.field,
  //             headerName: child.header,
  //             // children typically numeric → right align
  //             // align: 'right',
  //             // headerAlign: 'right',
  //             flex: 1, // share leftover space
  //             minWidth: 100,
  //             cellClassName: 'rightAlign',
  //           }))
  //         : [
  //             {
  //               field: col.field,
  //               headerName: col.header,
  //               width:
  //                 col.field === 'RowNo'
  //                   ? 80
  //                   : col.field === 'UOM'
  //                     ? 100 // shrink UOM to 100px
  //                     : 250,
  //               flex: col.field === 'Particulates' ? 2 : undefined,
  //               minWidth: col.field === 'Particulates' ? 120 : undefined,
  //               // align: col.field === 'UOM' ? 'center' : 'left',
  //               // headerAlign: col.field === 'UOM' ? 'center' : 'left',
  //               renderCell:
  //                 col.field === 'Remark'
  //                   ? (params) => (
  //                       <span
  //                         // style={{ color: 'blue', cursor: 'pointer' }}
  //                         onClick={() => handleRemarkCellClick(params.rows)}
  //                       >
  //                         {params.value}
  //                       </span>
  //                     )
  //                   : undefined,
  //             },
  //           ],
  //   )
  // }, [apiCols])

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

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        var res = await DataService.getPlantProductionSummary(keycloak)

        // console.log(res)
        if (res?.code == 200) {
          res = res?.data.map((Particulates, index) => ({
            ...Particulates,
            id: index,
          }))
          // res = res?.map((item) => ({
          //   ...item,
          //   isEditable: false,
          // }))
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
  const columns = useMemo(() => {
    if (!rows || rows.length === 0) return []

    // helper to detect numeric column across all rows
    const detectNumber = (field) =>
      rows.every((r) => typeof r[field] === 'number')

    return apiCols.flatMap((col) => {
      // handle grouped children
      if (col.children) {
        return col.children.map((child) => {
          const isNum = detectNumber(child.field)
          return {
            field: child.field,
            headerName: child.header,
            type: isNum ? 'number' : 'string',
            align: isNum ? 'right' : 'left',
            headerAlign: isNum ? 'right' : 'left',
            flex: 1,
            minWidth: 100,
          }
        })
      }

      // leaf column
      const isNum = detectNumber(col.field)
      const base = {
        field: col.field,
        headerName: col.header,
        type: isNum ? 'number' : 'string',
        align: isNum ? 'right' : 'left',
        headerAlign: isNum ? 'right' : 'left',

        // your width/flex logic
        width: col.field === 'RowNo' ? 80 : col.field === 'UOM' ? 100 : 250,
        flex: col.field === 'Particulates' ? 2 : undefined,
        minWidth: col.field === 'Particulates' ? 120 : undefined,
      }

      // for Remark column, inject custom renderer
      if (col.field === 'Remark') {
        return {
          ...base,
          renderCell: (params) => {
            const txt = params.value || ''
            const display = truncateRemarks(txt, 15)
            return (
              <Tooltip title={txt} arrow>
                <div
                  style={{
                    cursor: 'pointer',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    maxWidth: 140,
                  }}
                  onClick={() => handleRemarkCellClick(params.row)}
                >
                  {display || 'Click to add remark'}
                </div>
              </Tooltip>
            )
          },
        }
      }

      return base
    })
  }, [apiCols, rows, handleRemarkCellClick])
  // const columns = useMemo(() => {
  //   if (!rows || rows.length === 0) return []

  //   return apiCols.flatMap((col) => {
  //     // helper to detect number columns across all rows
  //     const detectNumber = (field) =>
  //       rows.every((r) => typeof r[field] === 'number')

  //     if (col.children) {
  //       return col.children.map((child) => {
  //         const isNum = detectNumber(child.field)
  //         return {
  //           field: child.field,
  //           headerName: child.header,
  //           type: isNum ? 'number' : 'string',
  //           align: isNum ? 'right' : 'left',
  //           // headerAlign: isNum ? 'right' : 'left',
  //           flex: 1,
  //           minWidth: 100,
  //         }
  //       })
  //     }

  //     const isNum = detectNumber(col.field)
  //     return {
  //       field: col.field,
  //       headerName: col.header,
  //       type: isNum ? 'number' : 'string',
  //       align: isNum ? 'right' : 'left',
  //       // headerAlign: isNum ? 'right' : 'left',

  //       width: col.field === 'RowNo' ? 80 : col.field === 'UOM' ? 100 : 250,
  //       flex: col.field === 'Particulates' ? 2 : undefined,
  //       minWidth: col.field === 'Particulates' ? 120 : undefined,

  //       renderCell:
  //         col.field === 'Remark'
  //           ? (params) => (
  //               <span onClick={() => handleRemarkCellClick(params.row)}>
  //                 {params.value}
  //               </span>
  //             )
  //           : undefined,
  //     }
  //   })
  // }, [apiCols])

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const defaultCustomHeight = { mainBox: '64vh', otherBox: '90%' }
  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow
    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevrow) =>
      prevrow.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])

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
        title='Plants Production Summary'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
          saveBtn: true,
          textAlignment: 'center',
        }}
        treeData
        getTreeDataPath={(rows) => rows.path}
        defaultGroupingExpansionDepth={1} // expand only first level by default
        disableSelectionOnClick
        columnGroupingModel={columnGroupingModel}
        experimentalFeatures
        processRowUpdate={processRowUpdate}
        remarkDialogOpen={remarkDialogOpen}
        unsavedChangesRef={unsavedChangesRef}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
      />
    </Box>
  )
}

export default PlantsProductionSummary
