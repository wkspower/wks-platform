// import React, { useEffect, useState } from 'react'
// import { useSelector } from 'react-redux'
// import { useSession } from 'SessionStoreContext'
// export default function MaintenanceSummary() {
//   const keycloak = useSession()

//   const dataGridStore = useSelector((state) => state.dataGridStore)
//   const {
//     verticalChange,
//     yearChanged,
//     oldYear,
//     plantID,
//     plantObject,
//     siteObject,
//     verticalObject,
//     year,
//   } = dataGridStore

//   const PLANT_ID = plantObject?.id
//   const SITE_ID = siteObject?.id
//   const VERTICAL_ID = verticalObject?.id
//   const AOP_YEAR = year?.selectedYear
//   const isOldYear = oldYear?.oldYear
//   const vertName = verticalChange?.selectedVertical

//   async function handleOpenPdfTempSSRS() {
//     try {
//       let baseurl = ''
//       baseurl =
//         'http://sjmnpb174/ReportServer/Pages/ReportViewer.aspx?%2fAOPReport%2fConsumptionBudgetSummarySiteWise&rs:Command=Render'

//       const url = `${baseurl}`

//       window.open(url, '_blank')
//       return true
//     } catch (e) {
//       console.error('Error opening link:', e)
//       return Promise.reject(e)
//     }
//   }

//   useEffect(() => {
//     handleOpenPdfTempSSRS()
//   }, [])

//   return null
// }

// MaintenanceSummary.jsx
import React, { useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { CircularProgress, Box, Typography, Button } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import { Backdrop } from '../../../node_modules/@mui/material/index'

export default function MaintenanceSummary() {
  const keycloak = useSession()
  const [openInTab, setOpenInTab] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)

  const dataGridStore = useSelector((s) => s.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  // Construct the SSRS URL. Use the ReportServer or ReportManager URL you need.
  // Example uses ReportViewer.aspx path you had.
  const src = useMemo(() => {
    const base = 'http://sjmnpb174/ReportServer/Pages/ReportViewer.aspx'
    // if your report path is /AOPReport/ConsumptionBudgetSummarySiteWise
    const reportPath = '%2fAOPReport%2fConsumptionBudgetSummarySiteWise' // encoded '/AOPReport/ConsumptionBudgetSummarySiteWise'
    const params = new URLSearchParams({
      // Report path already encoded in URL; append render command and other params
      '': reportPath, // this makes URL like ...ReportViewer.aspx?%2fAOPReport%2f...  (some servers prefer this form)
      'rs:Command': 'Render',
      'rc:Toolbar': 'true', // or false
      // pass any report parameters here, use your actual parameter names (example:)
      PlantId: PLANT_ID ?? '',
      SiteId: SITE_ID ?? '',
      VerticalId: VERTICAL_ID ?? '',
      AOPYear: AOP_YEAR ?? '',
    })

    // If the server expects query string without param names for the path, build accordingly:
    // const url = `${base}?%2fAOPReport%2fConsumptionBudgetSummarySiteWise&rs:Command=Render&PlantId=${encodeURIComponent(PLANT_ID)}...`
    return `${base}?${params.toString()}`
  }, [PLANT_ID, SITE_ID, VERTICAL_ID, AOP_YEAR])

  // Optionally open in new tab
  if (openInTab) {
    window.open(src, '_blank')
    setOpenInTab(false)
  }

  return (
    <Box sx={{ height: '100%', width: '100%' }}>
      {loadError && (
        <Box sx={{ color: 'error.main', mb: 1 }}>
          <Typography variant='body2'>
            Report cannot be embedded. It may be blocked by server headers
            (X-Frame-Options / CSP) or needs auth.
          </Typography>
        </Box>
      )}

      <Box
        component='iframe'
        src={src}
        title='Maintenance Summary'
        onLoad={() => {
          setLoading(false)
          setLoadError(false)
        }}
        onError={() => {
          setLoading(false)
          setLoadError(true)
        }}
        sx={{
          width: '100%',
          height: 'calc(100vh - 120px)',
          border: 'none',
        }}
      />

      {loading && (
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
      )}
    </Box>
  )
}
