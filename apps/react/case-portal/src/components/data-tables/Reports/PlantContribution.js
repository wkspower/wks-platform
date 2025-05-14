import { useEffect, useState } from 'react'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
// import { useSession } from 'SessionStoreContext'
import { MockReportService } from './mockPlantContributionAPI'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'

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

  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})

  useEffect(() => {
    const loadAll = async () => {
      setLoading(true)
      const out = {}

      await Promise.all(
        categories.map(async ({ key }) => {
          const { columns, columnGrouping } = await MockReportService.getReport(
            { category: key },
          )

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

    loadAll()
  }, [keycloak, year])

  return (
    <Box sx={{ width: '100%' }}>
      <Backdrop open={loading} sx={{ color: '#fff', zIndex: 9 }}>
        <CircularProgress color='inherit' />
      </Backdrop>

      {categories.map(({ key, title }) => {
        const rpt = reports[key] || {}
        return (
          <Box key={key} sx={{ mt: 2 }}>
            {/* <Typography variant='h6'>{title}</Typography> */}
            <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
              {title}
            </Typography>
            <ReportDataGrid
              columns={rpt.columns || []}
              columnGroupingModel={rpt.columnGrouping || []}
              rows={rpt.rows || []}
              permissions={{ textAlignment: 'center' }}
            />
          </Box>
        )
      })}
    </Box>
  )
}
