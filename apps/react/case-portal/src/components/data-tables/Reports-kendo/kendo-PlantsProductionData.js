import { Box } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSelector } from 'react-redux'
import {
  Backdrop,
  CircularProgress,
} from '../../../../node_modules/@mui/material/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'

const PlantsProductionSummary = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
    const {
      verticalChange,
      yearChanged,
      oldYear,
      plantID,
      plantObject,
      siteObject,
      verticalObject,
      year,
      screenTitle,
    } = dataGridStore
    const PLANT_ID = plantObject?.id
    const SITE_ID = siteObject?.id
    const VERTICAL_ID = verticalObject?.id
    const VERTICAL_NAME = verticalObject?.name
    const AOP_YEAR = year?.selectedYear
    const vertName = verticalChange?.selectedVertical
    const lowerVertName = vertName?.toLowerCase() || 'meg'
  const thisYear = AOP_YEAR
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const VALUE_FORMATTOR_PRODUCTION = ValueFormatterProduction()
  const VALUE_FORMATTOR_CONSUMPTION = ValueFormatterConsumption()
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const isOldYear = (() => {
    if (!AOP_YEAR || !AOP_YEAR.includes('-')) return false
    const currentYear = new Date().getFullYear()
    const startYear = parseInt(AOP_YEAR.split('-')[0])
    return startYear < currentYear
  })()

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

  let itoldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    itoldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const apiCols = [
    {
      field: 'RowNo',
      title: 'SL.No',
      widthT: 80,
      format: '{0:#.#}',
      editable: false,
    },

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

    { field: 'UOM', title: 'Unit', widthT: 80, editable: false },

    {
      title: itoldYear || 'Old Year',
      children: [
        {
          field: 'BudgetPrevYear',
          title: 'Budget',
          width: 120,
          editable: false,
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
        },
        {
          field: 'ActualPrevYear',
          title: 'Actual',
          width: 120,
          format: VALUE_FORMATTOR_PRODUCTION,
          editable: false,
          type: 'number',
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
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
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
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
        },
        {
          field: 'VarBudgetPer',
          title: '%',
          width: 100,
          editable: false,
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
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
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
        },
        {
          field: 'VarActualPer',
          title: '%',
          width: 100,
          editable: false,
          format: VALUE_FORMATTOR_PRODUCTION,
          type: 'number',
        },
      ],
    },

    { field: 'Remark', title: 'Remarks', width: 200, editable: true },
  ]

  const fetchData = async () => {
    if(!PLANT_ID || !AOP_YEAR) return 
    try {
      setLoading(true)
      var res = await DataService.getPlantProductionSummary(keycloak, PLANT_ID, AOP_YEAR)
      if (res?.code == 200) {
        res = res?.data.map((Particulates, index) => ({
          ...Particulates,
          id: index,
          // isEditable: index == 4 || index == 5 ? true : false,
          isEditable: true,
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
  }, [AOP_YEAR, PLANT_ID])

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
        PLANT_ID,
        AOP_YEAR,
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

      const res = await DataService.handleCalculatePlantProductionData(
        PLANT_ID,
        AOP_YEAR,
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
          saveBtn: !isOldYear,
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
