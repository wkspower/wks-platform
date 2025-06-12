import { Box } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import {
  Backdrop,
  CircularProgress,
} from '../../../../node_modules/@mui/material/index'

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
  const [modifiedCells, setModifiedCells] = React.useState({})

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const numberEditor = (cellProps) => {
    const { dataItem, field, onChange } = cellProps

    const handleChange = (event) => {
      const val = event.target.value
      onChange({
        dataItem,
        field,
        value: val === '' ? null : Number(val),
      })
    }

    return (
      <td>
        <input
          type='number'
          step='any'
          value={dataItem[field] ?? ''}
          onChange={handleChange}
          style={{ width: '100%' }}
        />
      </td>
    )
  }

  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const apiCols = [
    { field: 'RowNo', title: 'SL.No', width: 80, editable: false },

    {
      title: 'Item',
      children: [
        {
          field: 'Particulates',
          title: 'Production Volume',
          width: 150,
          editable: false,
        },
      ],
    },

    { field: 'UOM', title: 'Unit', width: 100, editable: false },

    {
      title: oldYear || 'Old Year',
      children: [
        {
          field: 'BudgetPrevYear',
          title: 'Budget',
          width: 120,
          editable: false,
          format: '{0:#.###}',
        },
        {
          field: 'ActualPrevYear',
          title: 'Actual',
          width: 120,
          editable: true,
        },
      ],
    },

    {
      title: thisYear || '2024-25',
      children: [
        {
          field: 'BudgetCurrentYear',
          title: 'Budget',
          width: 120,
          editable: false,
          format: '{0:#.###}',
        },
      ],
    },

    {
      title: 'Variance wrt current year budget',
      children: [
        {
          field: 'VarBudgetMT',
          title: 'MT',
          width: 120,
          editable: false,
          format: '{0:#.###}',
        },
        {
          field: 'VarBudgetPer',
          title: '%',
          width: 100,
          editable: false,
          format: '{0:#.###}',
        },
      ],
    },

    {
      title: 'Variance wrt current year actuals',
      children: [
        {
          field: 'VarActualMT',
          title: 'MT',
          width: 120,
          editable: false,
          format: '{0:#.###}',
        },
        {
          field: 'VarActualPer',
          title: '%',
          width: 100,
          editable: false,
          format: '{0:#.###}',
        },
      ],
    },

    { field: 'Remark', title: 'Remarks', width: 200, editable: true },
  ]

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getPlantProductionSummary(keycloak)
      if (res?.code == 200) {
        res = res?.data.map((Particulates, index) => ({
          ...Particulates,
          id: index,
          isEditable: index == 4 || index == 5 ? true : false,
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

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const defaultCustomHeight = { mainBox: 'fit-content', otherBox: '90%' }

  const saveChanges = async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var data = Object.values(modifiedCells)

      const rowsToUpdate = data.map((row) => ({
        id: row.Id,
        remark: row.Remark,
        ActualPrevYear: row.ActualPrevYear,
      }))
      const res = await DataService.savePlantProductionData(
        keycloak,
        rowsToUpdate,
        plantId,
      )

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

      <KendoDataTablesReports
        rows={rows}
        setRows={setRows}
        title='Plant Production Summary (T-14)'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        columns={apiCols}
        permissions={{
          customHeight: defaultCustomHeight,
          saveBtn: true,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: false,
          showTitle: true,
        }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        saveChanges={saveChanges}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
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
