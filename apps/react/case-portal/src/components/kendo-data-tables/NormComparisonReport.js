import { Box, CircularProgress, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { useSession } from 'SessionStoreContext'
import { Backdrop } from '../../../node_modules/@mui/material/index'

export default function NormComparisonReport() {
  const keycloak = useSession()
  const [openInTab, setOpenInTab] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [base, setBase] = useState('')

  const dataGridStore = useSelector((s) => s.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const VERTICAL_NAME_LOWERCASE = verticalObject?.name.toLowerCase()

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    let REPORT_CODE = ''
    if (VERTICAL_NAME_LOWERCASE == 'pe' || VERTICAL_NAME_LOWERCASE == 'pp') {
      REPORT_CODE = 'norm-comparison-report-pepp'
    } else {
      REPORT_CODE = 'norm-comparison-report'
    }

    try {
      var data = await BusinessDemandDataApiService.SSRS_NormComparisonReport(
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

    // NO PARAMTERS NEED TO PASS HERE FOR THIS REPORT
    // const params = `&plantId=${PLANT_ID}&siteId=${SITE_ID}&verticalId=${VERTICAL_ID}&finYear=${AOP_YEAR}`

    // return `${base}${params}`
    return `${base}`
  }, [base, PLANT_ID, SITE_ID, VERTICAL_ID, AOP_YEAR])

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
        title='Plant Budget Summary'
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
