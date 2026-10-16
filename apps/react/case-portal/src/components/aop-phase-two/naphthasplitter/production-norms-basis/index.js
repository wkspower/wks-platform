import React, { useState, useEffect, useCallback } from 'react'
import { Box, Stack } from '@mui/material'
import TabSection from 'components/aop-phase-two/common/utilities/Tabs'
import ConfigurationAccordian from '../../common/components/ConfigurationAccordian'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import Constants from './Constants'
import Configuration from './Configuration'
import TabAccessApiService from 'components/aop-phase-two/services/common/tabAccessApiService'
import PIMSThroughput from './PIMSThroughput'
import PIMSMontlyThroughput from 'components/aop-phase-two/crude/production-norms-basis/PIMSMontlyThroughput'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/naphthasplitter/productionNormsApiService'
import Notification from 'components/aop-phase-two/common/utilities/Notification'
import ManualEntry from './ManualEntry'

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

  const getConfigurationTabsMatrix = useCallback(async () => {
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
  }, [keycloak, PLANT_ID, AOP_YEAR, SITE_ID, VERTICAL_ID])

  const getConfigurationAvailableTabs = useCallback(async () => {
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
  }, [keycloak])

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setTabIndex(0)
    getConfigurationTabsMatrix()
    getConfigurationAvailableTabs()
  }, [
    PLANT_ID,
    AOP_YEAR,
    getConfigurationTabsMatrix,
    getConfigurationAvailableTabs,
  ])

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
    setRefreshData(false)
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
        setTimeout(() => {
          setRefreshData(true)
        }, 500)
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
  const filteredTabs = tabs
    .map((tabId) => {
      const tabInfo = availableTabs.find(
        (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
      )

      if (!tabInfo) return null

      const name = tabInfo.displayName

      return {
        id: tabId,
        name,
      }
    })
    .filter(Boolean)

  const renderTab = () => {
    if (!filteredTabs.length || !availableTabs.length) {
      return null
    }

    const currentTabId = filteredTabs[tabIndex]?.id
    if (!currentTabId) return null

    const tabData = getTabName(currentTabId)
    const currentTabName = typeof tabData === 'object' ? tabData.name : tabData

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
        return <Constants startDate={startDate} endDate={endDate} refreshData={refreshData} />
      case 'PIMS Throughput':
        return <PIMSThroughput startDate={startDate} endDate={endDate} refreshData={refreshData} />
      case 'PIMS Monthly Throughput':
      case 'pims-monthly-throughput':
      case 'pims-monthly-throughtput':
        return <PIMSMontlyThroughput startDate={startDate} endDate={endDate} refreshData={refreshData} />
      case 'Manual Entry':
        return <ManualEntry startDate={startDate} endDate={endDate} refreshData={refreshData} />
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
          isOldYear={isOldYear}
          isSummaryRequired={false}
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
            tabs={filteredTabs.map((tab) => tab.name)}
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
