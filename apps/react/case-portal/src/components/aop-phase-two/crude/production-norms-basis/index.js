import React, { useState, useEffect } from 'react'
import { Box, Stack } from '../../../../../node_modules/@mui/material/index'
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
        return <Configuration startDate={startDate} endDate={endDate} />
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
    </div>
  )
}

export default ProductionNormsBasis
