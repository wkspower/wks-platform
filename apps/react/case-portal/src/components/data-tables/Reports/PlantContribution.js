import { useEffect, useState } from 'react'
import { Box, Typography, Backdrop, CircularProgress } from '@mui/material'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
// import { useSession } from 'SessionStoreContext'
import { MockReportService } from './mockPlantContributionAPI'

const categories = [
  { key: 'ProductMix', title: 'A. Product mix and production' },
  { key: 'ByProducts', title: 'B. By products' },
  { key: 'RawMaterial', title: 'C. Raw material' },
  { key: 'CatChem', title: 'D. Cat chem' },
  { key: 'Utilities', title: 'E. Utilities' },
  { key: 'OtherVars', title: 'Other Variable Cost' },
  { key: 'ProdCalc', title: 'Production Cost Calculation' },
]

export default function PlantContribution() {
  // const keycloak = useSession()
  const year = localStorage.getItem('year')

  const [loading, setLoading] = useState(false)
  const [reports, setReports] = useState({})

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      const out = {}
      for (let { key } of categories) {
        const data = await MockReportService.getReport({ category: key })
        // data = { columns, columnGrouping, rows }
        out[key] = data
      }
      setReports(out)
      setLoading(false)
    }
    load()
  }, [year])

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

// import { Box } from '@mui/material'
// // import DataGridTable from '../ASDataGrid'
// import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
// import { useEffect, useMemo, useState } from 'react'
// import { DataService } from 'services/DataService'
// import { useSession } from 'SessionStoreContext'
// import {
//   Backdrop,
//   CircularProgress,
//   Typography,
// } from '../../../../node_modules/@mui/material/index'
// import { MockReportService } from './mockPlantContributionAPI'

// const PlantContribution = () => {
//   const keycloak = useSession()

//   const thisYear = localStorage.getItem('year')

//   let oldYear1 = ''
//   if (thisYear && thisYear.includes('-')) {
//     const [start, end] = thisYear.split('-').map(Number)
//     oldYear1 = `${start - 1}-${(end - 1).toString().slice(-2)}`
//   }
//   let oldYear2 = ''
//   if (oldYear1 && oldYear1.includes('-')) {
//     const [start, end] = oldYear1.split('-').map(Number)
//     oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
//   }
//   let oldYear3 = ''
//   if (oldYear2 && oldYear2.includes('-')) {
//     const [start, end] = oldYear2.split('-').map(Number)
//     oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
//   }

//   const year4 = localStorage.getItem('year')
//   const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
//   const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
//   const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`

//   const [loading, setLoading] = useState(false)
//   const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
//   const year = localStorage.getItem('year')

//    useEffect(() => {
//     const fetchData = async (category) => {
//       try {
//         setLoading(true)
//         var res = await MockReportService.getReport(
//           category

//         )
//         if (res?.code == 200) {
//           res = res?.data?.plantProductionData.map((item, index) => ({
//             ...item,
//             id: index,
//             isEditable: false,
//           }))

//           switch (type) {
//             case 'assumptions':
//               setRowsassumptions(res)
//               break

//             case 'maxRate':
//               setRowsMaxRate(res)
//               break

//             case 'OperatingHrs':
//               setRowsOperatingHrs(res)
//               break

//             case 'AverageHourlyRate':
//               setRowsAverageHourlyRate(res)
//               break

//             case 'ProductionPerformance':
//               setRowsProductionPerformance(res)
//               break

//             default:
//               console.warn('Unknown report type:', type)
//               break
//           }
//         } else {
//           setRows([])
//         }
//       } catch (err) {
//         console.log(err)
//       } finally {
//         setLoading(false)
//       }
//     }
//     fetchData('assumptions')
//     fetchData('maxRate')
//     fetchData('OperatingHrs')
//     fetchData('AverageHourlyRate')
//     fetchData('ProductionPerformance')
//   }, [year, plantId])

//   return (
//     <Box sx={{ height: 'auto', width: '100%' }}>
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         A. Product mix and production{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsProductMix}
//         columns={columnsProductMix}
//         columnGroupingModel={columnGroupingModel}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         B. By products{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsByProducts}
//         columns={columnsByProducts}
//         columnGroupingModel={columnGroupingModelByProducts}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         C. Raw material{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsRawMaterial}
//         columns={columnsRawMaterial}
//         columnGroupingModel={columnGroupingRawMaterial}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         D. Cat chem{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsCatChem}
//         columns={columnsCatChem}
//         columnGroupingModel={columnGroupingCatChem}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         E. Utilities{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsUtilities}
//         columns={columnsUtilities}
//         columnGroupingModel={columnGroupingUtilities}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />{' '}
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         Other Variable Cost{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsOtherVars}
//         columns={columnsOtherVars}
//         columnGroupingModel={columnGroupingOtherVars}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//       <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
//         Production Cost Calculation{' '}
//       </Typography>
//       <ReportDataGrid
//         rows={rowsProdCalc}
//         columns={columnsProdCalc}
//         columnGroupingModel={columnGroupingProdCalc}
//         permissions={{
//           textAlignment: 'center',
//         }}
//       />
//     </Box>
//   )
// }

// export default PlantContribution
