// import React, { useEffect, useState } from 'react'
// import { useSelector } from 'react-redux'
// import { useSession } from 'SessionStoreContext'
// export default function PlantBudgetSummary() {
//   const keycloak = useSession()

//   const [row, setRows] = useState([])
//   const [loading, setLoading] = useState(false)
//   const [openSaveDialog, setOpenSaveDialog] = useState(false)
//   const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
//   const [currentRemark, setCurrentRemark] = useState('')
//   const [currentRowId, setCurrentRowId] = useState(null)
//   const [modifiedCells, setModifiedCells] = React.useState({})
//   const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)

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
//   const lowerVertName = vertName?.toLowerCase() || 'meg'

//   async function handleOpenPdfTempSSRS() {
//     try {
//       let baseurl = ''
//       baseurl =
//         'http://sjmnpb174/ReportServer/Pages/ReportViewer.aspx?%2fAOP&rs:Command=Render'
//       const params = new URLSearchParams({
//         verticalId: VERTICAL_ID,
//         siteId: SITE_ID,
//         plantId: PLANT_ID,
//         finYear: AOP_YEAR,
//       })
//       const url = `${baseurl}?${params.toString()}`

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

// PlantBudgetSummary.jsx

import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { CircularProgress, Box, Typography, Button } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import { Backdrop, Toolbar } from '../../../node_modules/@mui/material/index'
import { renderChildren } from '../../../node_modules/@progress/kendo-react-layout/index'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'

export default function PlantBudgetSummary() {
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

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    try {
      var data = await BusinessDemandDataApiService.ssrsBudgetSummary(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      setBase(data?.data[0]?.reportURL)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, keycloak])

  const src = useMemo(() => {
    if (!base) return
    const params = `&plantId=${PLANT_ID}&siteId=${SITE_ID}&verticalId=${VERTICAL_ID}&finYear=${AOP_YEAR}`

    return `${base}${params}`
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
