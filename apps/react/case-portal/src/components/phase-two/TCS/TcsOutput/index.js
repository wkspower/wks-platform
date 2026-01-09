import { Box, Tab, Tabs } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { TcsOutputApiService } from 'services/phase-two-services/TCS/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import UnitCapacity from './UnitCapacity'
import Shutdown from './Shutdown'
import Slowdown from './Slowdown'
import CPPUnitsSdPlan from './CPPUnitsSdPlan'
import CrudBlendWindow from './CrudBlendWindow'
import ROGC from './ROGC'
import PCGOutlook from './PCGOutlook'

// Handler to render tab component based on displayName
const renderTabComponent = (tabDisplayName, props) => {
  switch (tabDisplayName) {
    case 'Unit Capacity':
      return <UnitCapacity {...props} />
    case 'Shutdown':
      return <Shutdown {...props} />
    case 'Slowdown':
      return <Slowdown {...props} />
    case 'CPP Units SD Plan':
      return <CPPUnitsSdPlan {...props} />
    case 'PCG Outlook':
      return <PCGOutlook {...props} />
    case 'ROGC':
      return <ROGC {...props} />
    case 'Crude Blend Window':
      return <CrudBlendWindow {...props} />
    default:
      return null
  }
}

const TcsOutput= () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  // State management - Snackbar notifications
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // Tab management
  const [tabObj, setTabObj] = useState([])
  const [tabIndex, setTabIndex] = useState(0)

  // Get current tab object (has id, displayName, displaySequence)
  const currentTab = tabObj[tabIndex] || {}

  // Fetch all tabs and visible tab IDs from backend
  useEffect(() => {
    fetchTabsData()
  }, [PLANT_ID, SITE_ID, VERTICAL_ID])

  const fetchTabsData = async () => {
    try {
      if (!PLANT_ID || !SITE_ID || !VERTICAL_ID) return

      // First API: Get list of all tabs
      const allTabsResponse = await TcsOutputApiService.getTcsAllTabs(keycloak)
      const allTabsList = allTabsResponse?.data?.configurationTypeList || []
      setTabObj(allTabsList)

      // Second API: Get array of tab IDs to show
      const visibleTabsResponse = await TcsOutputApiService.getTcsVisibleTabs(
        keycloak,
        VERTICAL_ID,
        SITE_ID,
        PLANT_ID,
      )
      console.log('visibleTabsResponse', visibleTabsResponse)

      let visibleTabIds = []
      if (visibleTabsResponse?.data) {
        visibleTabIds =
          typeof visibleTabsResponse.data === 'string'
            ? JSON.parse(visibleTabsResponse.data)
            : visibleTabsResponse.data
      }

      // Filter tabs to show only visible ones
      if (
        allTabsList &&
        Array.isArray(visibleTabIds) &&
        visibleTabIds.length > 0
      ) {
        const visibleTabIdsLower = visibleTabIds.map((id) => id.toLowerCase())
        const filteredTabs = allTabsList
          .filter((tab) => visibleTabIdsLower.includes(tab.id.toLowerCase()))
          .sort((a, b) => a.displaySequence - b.displaySequence)
        setTabObj(filteredTabs)
      } else if (
        allTabsList &&
        (!visibleTabIds || visibleTabIds.length === 0)
      ) {
        // If no visible tabs are returned, show all tabs
        console.warn('No visible tabs configured')
        setTabObj([])
      }
      console.log('tabObj', tabObj)
    } catch (err) {
      console.error('Error fetching tabs:', err)
      setSnackbarData({
        message: 'Failed to load tabs configuration',
        severity: 'error',
      })
      setSnackbarOpen(true)
    }
  }

  return (
    <Box
      sx={{
        p: 2,
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
        borderRadius: '4px',
        backgroundColor: '#fff',
      }}
    >
      {/* Tabs */}
      <Box sx={{ overflowX: 'auto', width: '100%' }}>
        <Tabs
          // sx={{
          //   '& .MuiTabs-indicator': {
          //     background: `linear-gradient(90deg, #1e3a8a 0%, #1e40af 100%)`,
          //   },
          //   '& .MuiTab-root.Mui-selected': {
          //     background: `linear-gradient(90deg, #1e3a8a 0%, #1e40af 100%)`,
          //     backgroundClip: 'text',
          //     WebkitBackgroundClip: 'text',
          //     WebkitTextFillColor: 'transparent',
          //   },
          // }}
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
          {tabObj &&
            tabObj?.map((tab) => (
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
      <Box>
        {renderTabComponent(currentTab.displayName, {
          currentTab,
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
          snackbarData,
          setSnackbarData,
          snackbarOpen,
          setSnackbarOpen,
        })}
      </Box>

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default TcsOutput
