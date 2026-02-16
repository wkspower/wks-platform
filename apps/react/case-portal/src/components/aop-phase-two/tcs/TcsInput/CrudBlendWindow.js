import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
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
    { key: 'VGOVRDrop', title: 'VGO-VR Drop' },
    { key: 'CrudeSpecificConstraints', title: 'Crude Specific Constraints' },
  ]

  // Carry forward data from previous year
  const handleCarryForward = useCallback(async () => {
    try {
      console.log('No data found, attempting carry-forward...')

      const carryForwardResponse =
        await TcsApiService.carryForwardCrudBlendWindow(
          keycloak,
          AOP_YEAR,
          SITE_ID,
          PLANT_ID,
        )

      console.log('Carry-forward response:', carryForwardResponse)

      setSnackbarData({
        message: 'Data carried forward from previous year successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      return true
    } catch (carryForwardErr) {
      console.error('Error during carry-forward:', carryForwardErr)
      return false
    }
  }, [keycloak, AOP_YEAR, SITE_ID, PLANT_ID, setSnackbarData, setSnackbarOpen])

  // Fetch all tables data once
  const fetchAllTablesData = useCallback(
    async (skipCarryForward = false) => {
      if (!PLANT_ID || !AOP_YEAR || !SITE_ID) {
        console.warn('Missing required params:', {
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
        })
        return
      }
      try {
        setLoading(true)
        console.log('Fetching Crude Blend Window data with:', {
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
        })

        const response = await TcsApiService.getCrudBlendWindowData(
          keycloak,
          PLANT_ID,
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

        // Check if all three tables have empty results
        const allTablesEmpty =
          Array.isArray(response) &&
          response.length > 0 &&
          response.every(
            (item) =>
              item.data &&
              item.data.results &&
              Array.isArray(item.data.results) &&
              item.data.results.length === 0,
          )

        console.log('All tables empty check:', allTablesEmpty)

        // If all tables have empty results and carry forward not skipped, attempt carry forward
        if (allTablesEmpty && !skipCarryForward) {
          const carryForwardSuccess = await handleCarryForward()
          if (carryForwardSuccess) {
            // Refetch data after successful carry forward
            await fetchAllTablesData(true)
            return
          }
        }

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
    },
    [
      keycloak,
      PLANT_ID,
      AOP_YEAR,
      SITE_ID,
      setSnackbarData,
      setSnackbarOpen,
      handleCarryForward,
    ],
  )

  // Fetch data on mount and when dependencies change
  useEffect(() => {
    console.log('useEffect triggered with:', { PLANT_ID, AOP_YEAR, SITE_ID })
    if (PLANT_ID && AOP_YEAR && SITE_ID) {
      fetchAllTablesData()
    }
  }, [PLANT_ID, AOP_YEAR, SITE_ID, fetchAllTablesData])

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
          PLANT_ID={PLANT_ID}
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
