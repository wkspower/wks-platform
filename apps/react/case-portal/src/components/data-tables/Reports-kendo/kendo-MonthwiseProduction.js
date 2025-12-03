import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { truncateRemarks } from 'utils/remarksUtils'
import { useSelector } from 'react-redux'
import {
  Backdrop,
  CircularProgress,
  Tooltip,
  Typography,
} from '../../../../node_modules/@mui/material/index'

import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import ProductionNorms from 'components/kendo-data-tables/ProductionNorms'
import NumericInputOnly from 'utils/NumericInputOnly'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'
const MonthwiseProduction = () => {
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
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
  const lowerVertName = vertName?.toLowerCase()
  const thisYear = AOP_YEAR
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
    if (READ_ONLY) return
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  let oldYear = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const VALUE_FORMATTOR_PRODUCTION = ValueFormatterProduction()
  const VALUE_FORMATTOR_CONSUMPTION = ValueFormatterConsumption()

  const colsMeg = [
    {
      field: 'RowNo',
      title: 'SL.No',
      widthT: 80,
      format: '{0:#.#}',
      editable: false,
    },

    {
      field: 'Month',
      title: 'Month',
      width: 100,
      editable: false,
    },

    {
      title: oldYear,
      children: [
        {
          title: 'Production, MT',
          children: [
            {
              field: 'EOEProdBudget',
              title: 'Budget',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'EOEProdActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
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
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'OpHrsActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
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
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'ThroughputActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
          ],
        },
      ],
    },

    {
      title: thisYear,
      children: [
        {
          field: 'OperatingHours',
          title: 'Operating Hours',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'MEGThroughput',
          title: 'MEG Throughput, TPH',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'EOThroughput',
          title: 'EO Throughput, TPH',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'EOEThroughput',
          title: 'EOE Throughput, TPH',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'TotalEOE',
          title: 'Total EOE, MT',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
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

  const colsNonMeg = [
    {
      field: 'RowNo',
      title: 'SL.No',
      widthT: 80,
      editable: false,
      format: '{0:#.#}',
    },

    {
      field: 'Month',
      title: 'Month',
      width: 100,
      editable: false,
    },

    {
      title: oldYear,
      children: [
        {
          title: 'Production, MT',
          children: [
            {
              field: 'ProdBudget',
              title: 'Budget',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'ProdActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
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
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'OpHrsActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
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
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
            {
              field: 'ThroughputActual',
              title: 'Actual',
              width: 120,
              editable: false,
              type: 'number',
              format: VALUE_FORMATTOR_PRODUCTION,
            },
          ],
        },
      ],
    },

    {
      title: thisYear,
      children: [
        {
          field: 'OperatingHours',
          title: 'Operating Hours',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'Throughput',
          title: 'Throughput, TPH',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
        },
        {
          field: 'Total',
          title: 'Total, MT',
          width: 150,
          editable: false,
          type: 'number',
          format: VALUE_FORMATTOR_PRODUCTION,
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

  const fetchData = async () => {
    try {
      setLoading(true)
      var res = await DataService.getMonthWiseSummary(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code == 200) {
        res = res?.data?.data.map((item, index) => ({
          ...item,
          id: index,
          isEditable: true,
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
  }, [AOP_YEAR, PLANT_ID])

  const defaultCustomHeight = { mainBox: '34vh', otherBox: '112%' }

  const saveChanges = async () => {
    try {
      // console.log('modifiedCells', modifiedCells);

      const data = Object.values(modifiedCells)

      if (data.length === 0) {
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
        opHrsActual: row?.OpHrsActual,
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
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)

      const res = await DataService.handleCalculateMonthwiseProduction(
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
        columns={lowerVertName == 'meg' ? colsMeg : colsNonMeg}
        permissions={{
          customHeight: defaultCustomHeightGrid1,
          textAlignment: 'center',
          remarksEditable: true,
          showCalculate: false,
          saveBtnForRemark: true,
          saveBtn: !isOldYear,
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
          hideByProducts: true,
          hideNoteText: true,
          hideExportBtn: true,
          title: 'Main Products - Production for the budget year',
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
