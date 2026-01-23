import { useEffect, useState, useCallback } from 'react'
import { Box, Tabs, Tab, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AssetLoading from './AssetLoading'

const Summary = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  // State management
  const [tabObj, setTabObj] = useState([])
  const [tabIndex, setTabIndex] = useState(0)
  const [tabsData, setTabsData] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  // Initialize tabs
  useEffect(() => {
    const tabs = [
      {
        id: 'asset-loading',
        name: 'Asset Loading',
        displayName: 'Asset Loading',
        displaySequence: 1,
      },
      {
        id: 'error-log',
        name: 'Error Logs',
        displayName: 'Error Logs',
        displaySequence: 2,
      },
    ]
    setTabObj(tabs)
  }, [])

  // Get current tab
  const currentTab = tabObj[tabIndex] || {}

  // Store data for any tab dynamically
  const setRowsForTab = useCallback((tabId, data) => {
    setTabsData((prev) => ({
      ...prev,
      [tabId]: data,
    }))
  }, [])

  // Fetch data for current tab
  const fetchTabData = useCallback(
    async (tabId) => {
      if (!tabId) return
      try {
        setLoading(true)
        setRowsForTab(tabId, [])
      } catch (err) {
        setSnackbarData({
          message: `Failed to load data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setRowsForTab(tabId, [])
      } finally {
        setLoading(false)
      }
    },
    [setRowsForTab],
  )

  // Load data when tab changes
  useEffect(() => {
    if (currentTab.id) {
      fetchTabData(currentTab.id)
    }
  }, [tabIndex, currentTab.id, fetchTabData])

  // Render tab content based on tab ID
  const renderTabContent = () => {
    switch (currentTab.id) {
      case 'asset-loading':
        return <AssetLoading />
      default:
        return null
    }
  }

  return (
    <Box sx={{ p: 2 }}>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '0px 0px 0px 0px',
            minHeight: '28px',
          }}
          textColor='primary'
          indicatorColor='primary'
          value={tabIndex}
          onChange={(e, newIndex) => {
            if (newIndex >= 0 && newIndex < tabObj.length) {
              setTabIndex(newIndex)
            }
          }}
        >
          {tabObj.map((tab) => (
            <Tab
              key={tab.id}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
              label={tab.displayName || tab.name}
            />
          ))}
        </Tabs>
      </Box>

      {/* Tab Content */}
      <Box>{renderTabContent()}</Box>
    </Box>
  )
}

export default Summary
