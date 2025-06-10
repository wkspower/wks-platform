import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { truncateRemarks } from 'utils/remarksUtils'

import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'

import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import ProductionNorms from 'components/kendo-data-tables/ProductionNorms'
import NumericInputOnly from 'utils/NumericInputOnly'

const MonthwiseProduction = () => {
  const keycloak = useSession()
  const thisYear = localStorage.getItem('year')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
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
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(2) : ''
  }
  const formatValueToThreeDecimalsZero = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(0) : ''
  }

  const columns = [
    { field: 'RowNo', title: 'SL.No', width: 80, editable: false },

    {
      field: 'Month',
      title: 'Month',
      width: 100,
      editable: false,
    },

    {
      title: oldYear || 'Old Year',
      children: [
        {
          title: 'EOE Production, MT',
          children: [
            {
              field: 'EOEProdBudget',
              title: 'Budget',
              width: 120,
              editable: false,
            },
            {
              field: 'EOEProdActual',
              title: 'Actual',
              width: 120,
              editable: false,
            },
          ],
        },
        {
          title: 'Operating Hours',
          children: [
            {
              field: 'OpHrsBudget',
              title: 'Budget',
              width: 120,
              editable: false,
            },
            {
              field: 'OpHrsActual',
              title: 'Actual',
              width: 120,
              editable: false,
            },
          ],
        },
        {
          title: 'Throughput, TPH',
          children: [
            {
              field: 'ThroughputBudget',
              title: 'Budget',
              width: 120,
              editable: false,
            },
            {
              field: 'ThroughputActual',
              title: 'Actual',
              width: 120,
              editable: true,
            },
          ],
        },
      ],
    },

    {
      title: thisYear || '2024-25',
      children: [
        {
          field: 'OperatingHours',
          title: 'Operating Hours',
          width: 150,
          editable: false,
        },
        {
          field: 'MEGThroughput',
          title: 'MEG Throughput, TPH',
          width: 150,
          editable: false,
        },
        {
          field: 'EOThroughput',
          title: 'EO Throughput, TPH',
          width: 150,
          editable: false,
        },
        {
          field: 'EOEThroughput',
          title: 'EOE Throughput, TPH',
          width: 150,
          editable: false,
        },
        {
          field: 'TotalEOE',
          title: 'Total EOE, MT',
          width: 150,
          editable: false,
        },
      ],
    },

    {
      field: 'Remark',
      title: 'Remarks',
      width: 200,
      editable: true,
    },
  ]

  const [rows, setRows] = useState()

  const defaultCustomHeightGrid1 = {
    mainBox: `${15 + (rows?.length || 0) * 5}vh`,
    otherBox: `${100 + (rows?.length || 0) * 5}%`,
  }

  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getMonthWiseSummary(keycloak)
      if (res?.code == 200) {
        res = res?.data?.data.map((item, index) => ({
          ...item,
          id: index,

          originalRemark: item.Remark,
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

  const saveChanges = async () => {
    try {
      console.log('modifiedCells', modifiedCells)

      var data = modifiedCells
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
        ThroughputActual: row?.ThroughputActual,
      }))

      // const hasEmptyThroughput = rowsToUpdate?.some(
      //   (row) =>
      //     row.ThroughputActual === null ||
      //     row.ThroughputActual === undefined ||
      //     row.ThroughputActual === '',
      // )

      // if (hasEmptyThroughput) {
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: 'Please fill in Actual Throughput before saving.',
      //     severity: 'error',
      //   })
      //   setLoading(false)
      //   return
      // }

      // const requiredFields = ['remarks', 'ThroughputActual']

      // const validationMessage = validateFields(data, requiredFields)
      // if (validationMessage) {
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: validationMessage,
      //     severity: 'error',
      //   })
      //   setLoading(false)
      //   return
      // }

      const res = await DataService.saveMonthwiseProduction(
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
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const res = await DataService.handleCalculateMonthwiseProduction(
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
      // fetchData()
      return res
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
        title='Monthwise Production (T-16)'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        columns={columns}
        permissions={{
          customHeight: defaultCustomHeightGrid1,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: false,
          saveBtnForRemark: true,
          saveBtn: true,
          showWorkFlowBtns: true,
          showTitle: true,
        }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        enableSaveAddBtn={enableSaveAddBtn}
        saveChanges={saveChanges}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
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
          roundOffDecimals: true,
        }}
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

export default MonthwiseProduction
