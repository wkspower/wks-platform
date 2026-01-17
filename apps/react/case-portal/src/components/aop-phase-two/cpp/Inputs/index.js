import { useEffect, useState, useCallback } from 'react'
import {
  Box,
  Tabs,
  Tab,
  Backdrop,
  CircularProgress,
  Stack,
} from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import ImportPower from './ImportPower'
import AssetAvailability from './AssetAvailability'
import AssetCapacity from './AssetCapacity'
import ShutdownAndOperational from './ShutdownAndOperational'
import { generateMockData, getColumnsForTab } from './InputUtility'
import ExportAvailability from './ExportAvailability'
import HeatRate from './HeatRate'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'

const Inputs = () => {
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
  const [modifiedCells, setModifiedCells] = useState({})
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterPhaseTwo()

  // Initialize tabs
  useEffect(() => {
    const tabs = [
      {
        id: 'purchase-power',
        name: 'purchasePowerInput',
        displayName: 'Purchase Power Input',
        displaySequence: 1,
      },
      {
        id: 'shutdown-operational',
        name: 'shutdownOperationalHrs',
        displayName: 'Shutdown and Operational Hrs.',
        displaySequence: 2,
      },
      {
        id: 'asset-priority',
        name: 'assetPriority',
        displayName: 'Asset Priority',
        displaySequence: 3,
      },
      {
        id: 'asset-capacity',
        name: 'assetCapacity',
        displayName: 'Asset Capacity',
        displaySequence: 4,
      },
      {
        id: 'heat-rate',
        name: 'heatRate',
        displayName: 'Heat Rate',
        displaySequence: 5,
      },
      // { id: 'export-availability',name:'exportAvailability', displayName: 'Export Availability', displaySequence: 6 },
    ]
    setTabObj(tabs)
  }, [])

  // Get current tab
  const currentTab = tabObj[tabIndex] || {}
  const currentTabDisplay = currentTab.displayName || 'Purchase Power Input'

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
        let transformedData = []

        // Mock data for demonstration - replace with actual API call
        const mockData = generateMockData(tabId)
        transformedData = mockData.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
        }))

        setRowsForTab(tabId, transformedData)
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

  // Get rows for current tab
  const getRows = useCallback(
    (tabId) => {
      return tabsData[tabId] || []
    },
    [tabsData],
  )

  // Setup rows and columns for current tab (outside conditional)
  const rows = getRows(currentTab.id)
  const setRowsForCurrent = useCallback(
    (newRows) => setRowsForTab(currentTab.id, newRows),
    [currentTab.id, setRowsForTab],
  )
  const columns = getColumnsForTab(currentTab.id, headerMap, valueFormat)

  // Save changes
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      setLoading(true)
      // Replace with actual API call
      // const response = await UtilityPlantApiServiceV2.saveImportPowerData(...)

      // Mock response
      const response = { code: 200 }

      if (response.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Changes saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchTabData(currentTab.id)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving changes:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, currentTab.id, fetchTabData])

  // Render tab content based on tab ID
  const renderTabContent = () => {
    switch (currentTab.id) {
      case 'purchase-power':
        return <ImportPower />
      case 'asset-priority':
        return (
          <Stack sx={{ mt: 2 }}>
            <AssetAvailability />
          </Stack>
        )
      case 'asset-capacity':
        return <AssetCapacity />
      case 'shutdown-operational':
        return <ShutdownAndOperational />
      case 'export-availability':
        return <ExportAvailability />
      case 'heat-rate':
        return <HeatRate />
      default:
        return (
          <Box key={currentTab.id}>
            <AdvanceKendoTable
              rows={rows}
              setRows={setRowsForCurrent}
              fetchData={() => fetchTabData(currentTab.id)}
              configType='import_power'
              columns={columns}
              saveChanges={saveChanges}
              snackbarData={snackbarData}
              snackbarOpen={snackbarOpen}
              setSnackbarOpen={setSnackbarOpen}
              setSnackbarData={setSnackbarData}
              modifiedCells={modifiedCells}
              setModifiedCells={setModifiedCells}
              permissions={{}}
            />
          </Box>
        )
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

export default Inputs
