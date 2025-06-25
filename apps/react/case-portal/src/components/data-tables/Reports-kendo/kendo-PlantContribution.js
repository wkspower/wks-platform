import { Backdrop, Box } from '@mui/material'
import React, { useEffect, useState } from 'react'
// import { useSession } from 'SessionStoreContext'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { DataService } from 'services/DataService'
import { MockReportService } from './mockPlantContributionAPI'

const categories = [
  {
    key: 'ProductMixAndProduction',
    title: 'Plant Contribution (T-21)- MEG\nProduct mix and Production',
  },
  { key: 'ByProducts', title: 'By products' },
  { key: 'RawMaterial', title: 'Raw material' },
  { key: 'CatChem', title: 'Cat chem' },
  { key: 'Utilities', title: 'Utilities' },
  { key: 'OtherVariableCost', title: 'Other Variable Cost' },
  { key: 'ProductionCostCalculations', title: 'Production Cost Calculation' },
]

export default function PlantContribution() {
  const keycloak = useSession()
  const year = localStorage.getItem('year')
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id

  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [rows, setRows] = useState()
  // const currFY = localStorage.getItem('year') || ''

  const loadAll = async () => {
    setLoading(true)
    const out = {}

    await Promise.all(
      categories.map(async ({ key }) => {
        const { columns, columnGrouping } = await MockReportService.getReport({
          category: key,
          year,
        })

        const apiResp = await DataService.getPlantContributionYearWisePlan(
          keycloak,
          key,
        )
        let rows = apiResp.data?.plantProductionData || [] // adapt if resp shape differs
        // console.log(apiResp)
        if (apiResp?.code == 200) {
          rows = apiResp?.data?.plantProductionData.map((item, index) => ({
            ...item,
            id: index,
            actualId: item?.id,
            isEditable:
              key == 'OtherVariableCost' && [1, 2, 3, 0].includes(index)
                ? true
                : false,
            // isEditable: [1, 2, 3, 0].includes(index),
            // isEditable: false,
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
  }, [keycloak, year, plantId])

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
      const res = await DataService.calculatePlantContributionReportData(
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
        plantId,
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

      {/* Main Categories Except 'OtherVariableCost' */}
      {categories
        .filter(({ key }) => key !== 'OtherVariableCost')
        .map(({ key, title }, idx) => {
          const rpt = reports[key] || {}
          return (
            <Box key={key} sx={{ mt: 0 }}>
              <KendoDataTablesReports
                columns={rpt.columns || []}
                rows={rpt.rows || []}
                handleCalculate={handleCalculate}
                title={title}
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
          <Box key={key} sx={{ mt: 4 }}>
            <KendoDataTablesReports
              modifiedCells={modifiedCells}
              setRows={setRows}
              columns={rpt.columns || []}
              rows={rows || []}
              handleCalculate={handleCalculate}
              title={'Other Variable Cost'}
              // unsavedChangesRef={unsavedChangesRef}
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

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
