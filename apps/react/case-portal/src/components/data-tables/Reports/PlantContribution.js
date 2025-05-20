import { useEffect, useState } from 'react'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid'
// import { useSession } from 'SessionStoreContext'
import { MockReportService } from './mockPlantContributionAPI'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import Notification from 'components/Utilities/Notification'

const categories = [
  { key: 'ProductMixAndProduction', title: 'Product mix and production' },
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
            isEditable: false,
          }))
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

  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop open={loading} sx={{ color: '#fff', zIndex: 9 }}>
        <CircularProgress color='inherit' />
      </Backdrop>

      {categories.map(({ key, title }, idx) => {
        const rpt = reports[key] || {}
        const canCalculate = idx === 0
        return (
          <Box key={key} sx={{ mt: 0 }}>
            {/* <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
              {title}
            </Typography> */}
            <ReportDataGrid
              columns={rpt.columns || []}
              columnGroupingModel={rpt.columnGrouping || []}
              rows={rpt.rows || []}
              handleCalculate={handleCalculate}
              title={title}
              permissions={{
                textAlignment: 'center',
                showCalculate: canCalculate,
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
