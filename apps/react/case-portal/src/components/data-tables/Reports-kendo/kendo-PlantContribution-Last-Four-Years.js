import { Backdrop, Box } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { MockPlantContributionAPILastFourYears } from './mockPlantContributionAPILastFourYears'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import ValueFormatterProductionProductionNormBasis from 'utils/ValueFormatterProduction_ProductionNormBasis'
import { useSelector } from 'react-redux'
import { getRoleName } from 'services/role-service'
const categories = () => {
  return [
    {
      key: 'ProductMixAndProduction',
      title: 'Plant Contribution Summary (T-22)\nProduct mix and Production',
    },
    { key: 'ByProducts', title: 'By products' },
    { key: 'RawMaterial', title: 'Raw material' },
    { key: 'CatChem', title: 'Cat chem' },
    { key: 'Utilities', title: 'Utilities' },
    { key: 'OtherVariableCost', title: 'Other Variable Cost' },
    { key: 'ProductionCostCalculations', title: 'Cost & Contribution Summary' },
  ]
}

export default function PlantContributionLastFourYears() {
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)
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
    const isOldYear = oldYear?.oldYear
    const vertName = verticalChange?.selectedVertical
    const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [otherVariableRows, setOtherVariableRows] = useState([])
  const FORMAT_VALUES_3_DECIMAL =
    lowerVertName == 'elastomer' ? '{0:0.000}' : '{0:0.00}'
  const FORMAT_VALUES_2_DECIMAL =
    lowerVertName == 'elastomer' ? '{0:0.00}' : '{0:0.00}'
  const FORMAT_VALUES_COST = lowerVertName == 'elastomer' ? '{0:0}' : '{0:0.00}'
  const FORMAT_VALUES_PRICE = '{0:0}'

  const loadAll = async () => {
    setLoading(true)
    const out = {}
    let tempOtherVariableRows = []
    await Promise.all(
      categories().map(async ({ key }) => {
        const { columns, columnGrouping } =
          await MockPlantContributionAPILastFourYears.getReport({
            category: key,
            AOP_YEAR,
            lowerVertName,
            FORMAT_VALUES_3_DECIMAL,
            FORMAT_VALUES_2_DECIMAL,
            FORMAT_VALUES_COST,
            FORMAT_VALUES_PRICE,
          })

        const apiResp = await DataService.plantContributionPlanLastFourYears(
          keycloak,
          key,
          PLANT_ID,
          AOP_YEAR,
        )
        let rows = apiResp.data?.plantProductionData || []
        if (apiResp?.code == 200) {
          rows = apiResp?.data?.plantProductionData.map((item, index, arr) => {
            let isBold = false
            // Set bold for last N rows as per category
            // no bold required
            if (
              key === 'ProductMixAndProduction' &&
              index >= arr.length - 4 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'ByProducts' &&
              index >= arr.length - 2 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'RawMaterial' &&
              index >= arr.length - 3 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'ProductionCostCalculations' &&
              index >= arr.length - 6 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'CatChem' &&
              index >= arr.length - 2 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'Utilities' &&
              index >= arr.length - 2 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            if (
              key === 'OtherVariableCost' &&
              index >= arr.length - 2 &&
              lowerVertName === 'meg'
            ) {
              isBold = false
            }
            return {
              ...item,
              id: index,
              actualId: item?.id,
              isEditable:
               key === 'OtherVariableCost' && index >= arr.length - 2
                  ? false
                  : true,
              isdisable:
                key === 'OtherVariableCost' && index >= arr.length - 2
                  ? true
                  : false,
              isBold,
            }
          })
          if (key === 'OtherVariableCost') {
            tempOtherVariableRows = rows
          }
        }
        out[key] = { columns, columnGrouping, rows }
      }),
    )

    setReports(out)
    setOtherVariableRows(tempOtherVariableRows)
    setLoading(false)
  }

  useEffect(() => {
    loadAll()
  }, [keycloak, AOP_YEAR, PLANT_ID])

  const handleCalculate = () => {
    handleCalculateMonthwiseAndTurnaround()
  }

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

      const data = Object.values(modifiedCells)
      const rowsToUpdate = data.map((row) => ({
        id: row?.actualId,
        actualFourYearsAgo: row.actualFourYearsAgo,
        actualThreeYearsAgo: row.actualThreeYearsAgo,
        actualTwoYearsAgo: row.actualTwoYearsAgo,
        actualLastYear: row.actualLastYear,
        budgetCurrent: row.budgetCurrent,
      }))

      const res = await DataService.savePlantContributionlastfourData(
        keycloak,
        rowsToUpdate,
      )

      if (res?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        // Optionally reload data here
        loadAll()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }
  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      ></Backdrop>

      {/* Main Categories Except 'OtherVariableCost' */}
      {categories()
        .filter(
          (item) =>
            item.key !== 'OtherVariableCost' &&
            item.key !== 'ProductionCostCalculations',
        )
        .map(({ key, title }, idx) => {
          const rpt = reports[key] || {}
          return (
            <Box key={key} sx={{ mt: 0 }}>
              <KendoDataTablesReports
                columns={rpt.columns || []}
                rows={rpt.rows || []}
                title={title}
                setRows={() => {}}
                permissions={{
                  textAlignment: 'center',
                  showCalculate: false,
                  showFinalSubmit: idx === 0,
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
          <Box key={key} sx={{ mt: 1 }}>
            <KendoDataTablesReports
              modifiedCells={modifiedCells}
              setRows={setOtherVariableRows}
              columns={rpt.columns || []}
              rows={otherVariableRows || []}
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
      {/* Last: Production Cost Calculations */}
      {(() => {
        const key = 'ProductionCostCalculations'
        const rpt = reports[key] || {}
        return (
          <Box key={key} sx={{ mt: 0 }}>
            <KendoDataTablesReports
              columns={rpt.columns || []}
              rows={rpt.rows || []}
              title={rpt.title || 'Cost & Contribution Summary'}
              setRows={() => {}}
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
