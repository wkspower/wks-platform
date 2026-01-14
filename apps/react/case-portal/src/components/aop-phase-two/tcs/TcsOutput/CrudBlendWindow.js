import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import CrudBlendWindowGrid from './CrudBlendWindowComponents/CrudBlendWindowGrid'

const CrudBlendWindow = ({
  PLANT_ID,
  AOP_YEAR,
  currentTab,
  SITE_ID,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const [allTablesData, setAllTablesData] = useState({})

  const gridConfigs = [
    { key: 'CrudeBlendWindow', title: 'Crude Blend Window' },
    { key: 'VGOVRDROP', title: 'VGO-VR Drop' },
    { key: 'CrudeSpecificConstraints', title: 'Crude Specific Constraints' },
  ]

  // Fetch all tables data once
  const fetchAllTablesData = useCallback(async () => {
    if (!AOP_YEAR || !SITE_ID) {
      console.warn('Missing required params:', { AOP_YEAR, SITE_ID })
      return
    }
    try {
      setLoading(true)
      console.log('Fetching Crude Blend Window data with:', {
        AOP_YEAR,
        SITE_ID,
      })

      const response = await TcsOutputApiService.getCrudBlendWindowData(
        keycloak,
        AOP_YEAR,
        SITE_ID,
      )

      console.log('Crude Blend Window API Response:', response)

      // Transform response into a map keyed by table name
      const tablesDataMap = {}
      if (Array.isArray(response)) {
        response.forEach((item) => {
          if (item.table && item.data) {
            tablesDataMap[item.table] = item.data
          }
        })
      }

      console.log('Transformed tables data:', tablesDataMap)
      console.log('tablesDataMap', tablesDataMap)
      setAllTablesData(tablesDataMap)
    } catch (err) {
      console.error('Error fetching Crude Blend Window data:', err)
      setSnackbarData({
        message: 'Failed to load Crude Blend Window data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      setAllTablesData({})
    } finally {
      setLoading(false)
    }
  }, [keycloak, AOP_YEAR, SITE_ID, setSnackbarData, setSnackbarOpen])

  // Fetch data on mount and when dependencies change
  useEffect(() => {
    console.log('useEffect triggered with:', { AOP_YEAR, SITE_ID })
    if (AOP_YEAR && SITE_ID) {
      fetchAllTablesData()
    }
  }, [AOP_YEAR, SITE_ID, fetchAllTablesData])

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {gridConfigs.map((config) => (
        <CrudBlendWindowGrid
          key={config.key}
          tableKey={config.key}
          title={config.title}
          AOP_YEAR={AOP_YEAR}
          SITE_ID={SITE_ID}
          tableData={allTablesData[config.key]}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          onRefresh={fetchAllTablesData}
        />
      ))}
    </Box>
  )
}

export default CrudBlendWindow
