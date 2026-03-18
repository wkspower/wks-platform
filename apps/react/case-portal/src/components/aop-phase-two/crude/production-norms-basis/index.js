import React, { useState, useEffect } from 'react'
import {
  Box,
  Stack,
  Button,
  CircularProgress,
  Snackbar,
  Alert,
} from '../../../../../node_modules/@mui/material/index'
import TabSection from 'components/aop-phase-two/common/utilities/Tabs'
import ConfigurationAccordian from '../../common/components/ConfigurationAccordian'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import Configuration from './Configuration'
import Constants from './Constants'
import ReportManualEntry from './ReportManualEntry'
import TabAccessApiService from 'components/aop-phase-two/services/common/tabAccessApiService'
import PIMSThroughput from './PIMSThroughput'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/crude/productionNormsApiService'
import Notification from 'components/aop-phase-two/common/utilities/Notification'

const ProductionNormsBasis = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { oldYear, plantObject, siteObject, verticalObject, year } =
    dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const isOldYear = false

  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [tabs, setTabs] = useState([])
  const [availableTabs, setAvailableTabs] = useState([])
  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const [normCalculationLoading, setNormCalculationLoading] = useState(false)
  const [refreshData, setRefreshData] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
    autoHide: true,
  })

  const getConfigurationTabsMatrix = async () => {
    if (!PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID) return
    setLoading(true)
    try {
      const response = await TabAccessApiService.getConfigurationTabsMatrix(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )
      if (response?.code === 200) {
        const parsedData = JSON.parse(response?.data)
        setTabs(parsedData)
      } else {
        setTabs([])
      }
    } catch (error) {
      console.error('Error fetching configuration tabs matrix:', error)
      setTabs([])
    } finally {
      setLoading(false)
    }
  }

  const getConfigurationAvailableTabs = async () => {
    setLoading(true)
    try {
      const response =
        await TabAccessApiService.getConfigurationAvailableTabs(keycloak)
      if (response?.code === 200) {
        setAvailableTabs(response?.data?.configurationTypeList)
      } else {
        setAvailableTabs([])
      }
    } catch (error) {
      console.error('Error fetching configuration available tabs:', error)
      setAvailableTabs([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setTabIndex(0)
    getConfigurationTabsMatrix()
    getConfigurationAvailableTabs()
  }, [PLANT_ID, AOP_YEAR])

  // Callback to receive dates from ConfigurationAccordian
  const handleDatesChange = (start, end) => {
    setStartDate(start)
    setEndDate(end)
  }

  // Helper function to format date for API
  const formatDateForAPI = (date) => {
    if (!date) return null
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // Handler for Load Norm Calculation button
  const handleLoadNormCalculation = async () => {
    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Please select both start date and end date',
        severity: 'warning',
        autoHide: true,
      })
      return
    }

    if (!PLANT_ID || !AOP_YEAR || !SITE_ID) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Missing required parameters',
        severity: 'error',
        autoHide: true,
      })
      return
    }

    setNormCalculationLoading(true)
    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)

      const response =
        await ProductionNormsApiService.loadButtonNormCalculation(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
          periodFrom,
          periodTo,
        )

      if (response?.code === 422) {
        // Then show validation error after a delay
        setTimeout(() => {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response.message || 'Validation error occurred.',
            severity: 'error',
            autoHide: false,
          })
          setRefreshData(true)
        }, 500)
      } else {
        // Code 200 - show only success notification
        setSnackbarOpen(true)
        setSnackbarData({
          message:
            response?.message || 'Norm calculation completed successfully!',
          severity: 'success',
          autoHide: true,
        })
        setRefreshData(true)
      }
    } catch (error) {
      console.error('Error in norm calculation:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to calculate norms. Please try again.',
        severity: 'error',
        autoHide: true,
      })
    } finally {
      setNormCalculationLoading(false)
    }
  }

  const [start, end] = AOP_YEAR ? AOP_YEAR.split('-').map(Number) : [0, 0]
  const prevYearFormatted = `${start - 1}-${(start - 1 + 1).toString().slice(-2)}`

  // Helper function to get tab display name by matching the UUID from tabs array
  const getTabName = (tabId) => {
    if (!tabId || !availableTabs.length) return null
    const tab = availableTabs.find(
      (t) => t.id.toLowerCase() === tabId.toLowerCase(),
    )
    return tab ? tab.displayName : null
  }

  // Dynamic tab list from API (filtered to exclude 'Report Manual Entry')
  const tablist = tabs
    .map((tabId) => {
      if (!tabId || !availableTabs.length) return ''
      const tabInfo = availableTabs.find(
        (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
      )

      if (tabInfo) {
        const originalName = tabInfo.displayName
        // Filter out Report Manual Entry
        if (originalName === 'Report Manual Entry') {
          return null
        }
        return originalName
      }
      return tabId
    })
    .filter((tab) => tab !== null)

  const renderTab = () => {
    if (!tabs.length || !availableTabs.length) {
      return null
    }

    const currentTabId = tabs[tabIndex]
    if (!currentTabId) return null

    const currentTabName = getTabName(currentTabId)

    switch (currentTabName) {
      case 'Configuration':
        return (
          <Configuration
            startDate={startDate}
            endDate={endDate}
            refreshData={refreshData}
          />
        )
      case 'Constants':
        return <Constants startDate={startDate} endDate={endDate} />
      case 'PIMS Throughput':
        return <PIMSThroughput startDate={startDate} endDate={endDate} />
      case 'Report Manual Entry':
        return <ReportManualEntry startDate={startDate} endDate={endDate} />
      default:
        return null
    }
  }

  return (
    <div>
      <Stack sx={{ mt: 1, mb: 1 }}>
        <ConfigurationAccordian
          PLANT_ID={PLANT_ID}
          AOP_YEAR={AOP_YEAR}
          READ_ONLY={READ_ONLY}
          isOldYear={isOldYear}
          isSummaryRequired={true}
          onDatesChange={handleDatesChange}
          onLoadNormCalculation={handleLoadNormCalculation}
          normCalculationLoading={normCalculationLoading}
        />
      </Stack>

      {tabs.length > 0 && availableTabs.length > 0 && (
        <Stack
          direction='row'
          justifyContent='space-between'
          alignItems='center'
        >
          <TabSection
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={tablist}
          />
        </Stack>
      )}

      {/* Tab Content */}
      <Box sx={{ mt: 2 }}>{renderTab()}</Box>
      {/* Notification */}
      <Notification
        open={snackbarOpen}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarData.message}
        severity={snackbarData.severity}
        autoHide={snackbarData.autoHide}
      />
    </div>
  )
}

export default ProductionNormsBasis
