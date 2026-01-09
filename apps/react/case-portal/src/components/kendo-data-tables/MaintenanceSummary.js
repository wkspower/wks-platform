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
import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { CircularProgress, Box, Typography, Button } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import { Backdrop } from '../../../node_modules/@mui/material/index'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'

export default function MaintenanceSummary() {
  const keycloak = useSession()
  const [openInTab, setOpenInTab] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [base, setBase] = useState('')

  const dataGridStore = useSelector((s) => s.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME_LOWERCASE = plantObject?.name?.toLowerCase()
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME_LOWERCASE = verticalObject?.name.toLowerCase()
  const AOP_YEAR = year?.selectedYear

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    let REPORT_CODE = ''
    if (VERTICAL_NAME_LOWERCASE == 'pe' || VERTICAL_NAME_LOWERCASE == 'pp') {
      REPORT_CODE = 'maintenance-summary'
    } else {
      REPORT_CODE = 'maintenance-summary'
    }
    try {
      var data = await BusinessDemandDataApiService.ssrsMaintenanceSummary(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        REPORT_CODE,
      )

      setBase(data?.data[0]?.reportURL)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    setLoading(true)
    fetchData()
  }, [PLANT_ID, AOP_YEAR, keycloak])

  const src = useMemo(() => {
    if (!base) return

    const params = `&PlantId=${PLANT_ID}&SiteId=${SITE_ID}&VerticalId=${VERTICAL_ID}&AOPYear=${AOP_YEAR}`

    return `${base}${params}`
  }, [base, PLANT_ID, SITE_ID, VERTICAL_ID, AOP_YEAR])

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
