import { Backdrop, Box } from '@mui/material'
import React, { useEffect, useState } from 'react'
// import { useSession } from 'SessionStoreContext'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { DataService } from 'services/DataService'
import { MockReportService } from './mockPlantContributionAPI'
import { useSelector } from 'react-redux'
import { getRoleName } from 'services/role-service'
import { AOPWorkFlowService } from 'services/AOPWorkFlowService'

export default function PlantContribution() {
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)

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
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const categories = () => [
    {
      key: 'ProductMixAndProduction',
      title:
        lowerVertName === 'meg'
          ? 'Plant Contribution (T-21) - MEG\nProduct mix and Production'
          : 'Plant Contribution (T-21)\nProduct mix and Production',
    },
    { key: 'ByProducts', title: 'By products' },
    { key: 'RawMaterial', title: 'Raw material' },
    { key: 'CatChem', title: 'Cat chem' },
    { key: 'Utilities', title: 'Utilities' },
    { key: 'OtherVariableCost', title: 'Other Variable Cost' },
    { key: 'ProductionCostCalculations', title: 'Production Cost Calculation' },
  ]
  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [rows, setRows] = useState()

  const IS_CRACKER = lowerVertName === 'cracker'

  const FORMAT_VALUES_3_DECIMAL = IS_CRACKER
    ? '{0:0.0000}'
    : lowerVertName === 'elastomer'
      ? '{0:0.000}'
      : '{0:0.00}'

  const FORMAT_VALUES_2_DECIMAL = IS_CRACKER ? '{0:0.0000}' : '{0:0.00}'

  const FORMAT_VALUES_COST = IS_CRACKER
    ? '{0:0.0000}'
    : lowerVertName === 'elastomer'
      ? '{0:0}'
      : '{0:0.00}'

  const FORMAT_VALUES_PRICE = '{0:0}'

  const FORMAT_VALUES_NORMS = IS_CRACKER
    ? '{0:0.0000}'
    : lowerVertName === 'meg' || lowerVertName === 'elastomer'
      ? '{0:0.00000}'
      : '{0:0.00}'

  const loadAll = async () => {
    setLoading(true)
    const out = {}

    await Promise.all(
      categories().map(async ({ key }) => {
        const { columns, columnGrouping } = await MockReportService.getReport({
          category: key,
          AOP_YEAR,
          lowerVertName,
          FORMAT_VALUES_3_DECIMAL,
          FORMAT_VALUES_2_DECIMAL,
          FORMAT_VALUES_COST,
          FORMAT_VALUES_PRICE,
          FORMAT_VALUES_NORMS,
        })

        const apiResp = await DataService.getPlantContributionYearWisePlan(
          keycloak,
          key,
          PLANT_ID,
          AOP_YEAR,
        )
        let rows = apiResp.data?.plantProductionData || []
        if (apiResp?.code == 200) {
          rows = apiResp?.data?.plantProductionData.map((item, index) => ({
            ...item,
            id: index,
            actualId: item?.id,
            isEditable:
              !READ_ONLY &&
              key === 'OtherVariableCost' &&
              index <= rows.length - 4,
          }))
          if (key == 'OtherVariableCost') setRows(rows)
        } else {
          rows = []
        }
        out[key] = { columns, columnGrouping, rows }
      }),
    )

    setReports(out)
    setLoading(false)
  }

  useEffect(() => {
    loadAll()
  }, [keycloak, AOP_YEAR, PLANT_ID])

  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }
  const handleCalculateMonthwiseAndTurnaround = async () => {
    try {
      setLoading(true)

      const res = await AOPWorkFlowService.calculatePlantContributionReportData(
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
        loadAll()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refreshed Faild!',
          severity: 'error',
        })
      }

      return res
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

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.Remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

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
      // console.log('row',row);
      const rowsToUpdate = data.map((row) => ({
        id: row?.actualId,
        prevYearActual: row.PrevYearActual,
        PrevYearBudget: row.PrevYearBudget,
        CurrentYearBudget: row.CurrentYearBudget,

        remark: row.remarks || '',
      }))
      const res = await DataService.savePlantContributionData(
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
        // unsavedChangesRef.current = {
        //   unsavedRows: {},
        //   rowsBeforeChange: {},
        // }
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

  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      ></Backdrop>

      {/* Main Categories Except 'OtherVariableCost' and 'ProductionCostCalculations' */}
      {categories()
        .filter(({ key }) => key !== 'OtherVariableCost' && key !== 'ProductionCostCalculations')
        .map(({ key, title }, idx) => {
          const rpt = reports[key] || {}
          return (
            <Box key={key} sx={{ mt: 1 }}>
              <KendoDataTablesReports
                columns={rpt.columns || []}
                rows={rpt.rows || []}
                handleCalculate={handleCalculate}
                title={title}
                permissions={{
                  textAlignment: 'center',
                  showCalculate: false,
                  showFinalSubmit: false,
                  showTitle: true,
                }}
              />
            </Box>
          )
        })}

      {/* Separate Grid for 'OtherVariableCost' */}
      {(() => {
        const key = 'OtherVariableCost'
        const rpt = reports[key] || {}
        return (
          <Box key={key} sx={{ mt: 0 }}>
            <KendoDataTablesReports
              modifiedCells={modifiedCells}
              setRows={setRows}
              columns={rpt.columns || []}
              rows={rows || []}
              handleCalculate={handleCalculate}
              title={'Other Variable Cost'}
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              setCurrentRowId={setCurrentRowId}
              loading={loading}
              handleRemarkCellClick={handleRemarkCellClick}
              setModifiedCells={setModifiedCells}
              permissions={{
                customHeight: { mainBox: '32vh', otherBox: '100%' },
                textAlignment: 'center',
                remarksEditable: true,
                showCalculate: false,
                saveBtnForRemark: true,
                saveBtn: true,
                showWorkFlowBtns: true,
                showTitle: true,
              }}
              saveChanges={saveChanges}
            />
          </Box>
        )
      })()}

      {/* 'ProductionCostCalculations' as Last Grid */}
      {(() => {
        const key = 'ProductionCostCalculations'
        const rpt = reports[key] || {}
        return (
          <Box key={key} sx={{ mt: 1 }}>
            <KendoDataTablesReports
              columns={rpt.columns || []}
              rows={rpt.rows || []}
              handleCalculate={handleCalculate}
              title={'Production Cost Calculation'}
              permissions={{
                textAlignment: 'center',
                showCalculate: false,
                showFinalSubmit: false,
                showTitle: true,
              }}
            />
          </Box>
        )
      })()}

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
