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
import Notification from 'components/Utilities/Notification'

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
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const handleRemarkCellClick = (row) => {
    // console.log(row)
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
  }

  const apiCols = dataAPI.columns

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

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getPlantProductionSummary(keycloak)

      // console.log(res)
      if (res?.code == 200) {
        res = res?.data.map((Particulates, index) => ({
          ...Particulates,
          id: index,
          isEditable: false,

          VarBudgetPer:
            Particulates.VarBudgetPer != null
              ? Number(Number(Particulates.VarBudgetPer).toFixed(1))
              : '',

          VarActualPer:
            Particulates.VarActualPer != null
              ? Number(Number(Particulates.VarActualPer).toFixed(1))
              : '',

          // Round to nearest whole number
          VarBudgetMT:
            Particulates.VarBudgetMT != null
              ? Math.round(Number(Particulates.VarBudgetMT))
              : '',

          VarActualMT:
            Particulates.VarActualMT != null
              ? Math.round(Number(Particulates.VarActualMT))
              : '',

          BudgetPrevYear:
            Particulates.BudgetPrevYear != null
              ? Math.round(Number(Particulates.BudgetPrevYear))
              : '',

          BudgetCurrentYear:
            Particulates.BudgetCurrentYear != null
              ? Math.round(Number(Particulates.BudgetCurrentYear))
              : '',

          ActualPrevYear:
            Particulates.ActualPrevYear != null
              ? Math.round(Number(Particulates.ActualPrevYear))
              : '',
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
  useEffect(() => {
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
                    width: ' 100%',
                  }}
                  onDoubleClick={() => handleRemarkCellClick(params.row)}
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

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const defaultCustomHeight = { mainBox: 'fit-content', otherBox: '90%' }
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

    setRows((prevrow) =>
      prevrow.map((row) =>
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

  const saveWorkflowData = async () => {
    try {
      var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      const rowsToUpdate = data.map((row) => ({
        id: row.Id,
        remark: row.Remark,
      }))
      const res = await DataService.savePlantProductionData(
        keycloak,
        rowsToUpdate,
        plantId,
      )

      // console.log(res)

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
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

  const handleCalculate = () => {
    handleCalculatePlantProductionData()
  }
  const handleCalculatePlantProductionData = async () => {
    try {
      setLoading(true)

      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const res = await DataService.handleCalculatePlantProductionData(
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
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }

      // fetchCurrentYear()

      return res?.data
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
        title='Plant Production Summary (T-14)'
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeight,
          saveBtn: true,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: true,
          showTitle: true,

          // showCalculate: false,
          // showWorkFlowBtns: true,
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
        saveWorkflowData={saveWorkflowData}
        handleCalculate={handleCalculate}
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

export default PlantsProductionSummary
