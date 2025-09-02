import { Backdrop, Box } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import React, { useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { MockReportService } from './mockPlantContributionAPI'

const categories = () => {
  return [
    {
      key: 'ProductMixAndProduction',
      title: 'Plant Contribution (T-21)\nProduct mix and Production',
    },
    { key: 'ByProducts', title: 'By products' },
    { key: 'RawMaterial', title: 'Raw material' },
    { key: 'CatChem', title: 'Cat chem' },
    { key: 'Utilities', title: 'Utilities' },
    { key: 'OtherVariableCost', title: 'Other Variable Cost' },
    { key: 'ProductionCostCalculations', title: 'Production Cost Calculation' },
  ]
}

export default function PlantContributionLastFourYears() {
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
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})

  const verticalName = JSON.parse(
    localStorage.getItem('selectedVertical'),
  )?.name?.toLowerCase()
  const loadAll = async () => {
    setLoading(true)
    const out = {}

    await Promise.all(
      categories().map(async ({ key }) => {
        const { columns, columnGrouping } = await MockReportService.getReport({
          category: key,
          year,
          verticalName,
        })

        const apiResp = await DataService.getPlantContributionYearWisePlan(
          keycloak,
          key,
        )
        let rows = apiResp.data?.plantProductionData || []
        if (apiResp?.code == 200) {
          rows = apiResp?.data?.plantProductionData.map((item, index) => ({
            ...item,
            id: index,
            actualId: item?.id,
            isEditable: false,
          }))
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
      console.error('Error!', error)
    } finally {
      setLoading(false)
    }
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

      {categories()
        .filter((item) => item.key) // keep only items that have a key
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

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
