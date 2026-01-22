import { Box, Tab, Tabs, IconButton, Tooltip } from '@mui/material'
import HistoryIcon from '@mui/icons-material/History'
import Notification from 'components/Utilities/Notification'
import { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import UnitCapacity from './UnitCapacity'
import Shutdown from './Shutdown'
import Slowdown from './Slowdown'
import CPPUnitsSdPlan from './CPPUnitsSdPlan'
import CrudBlendWindow from './CrudBlendWindow'
import ROGC from './ROGC'
import PCGOutlook from './PCGOutlook'
import NetUnitCapacity from './NetUnitCapacity'
import RemarkDialog from './workflow/RemarkDialog'
import { Button } from '../../../../../node_modules/@mui/material/index'
import HistoryDialog from './workflow/HistoryDialog'
import { getUserRole } from '../utils/roleUtils'

// Handler to render tab component based on displayName
const renderTabComponent = (tabDisplayName, props) => {
  switch (tabDisplayName) {
    case 'Unit Capacity':
      return <UnitCapacity {...props} />
    case 'Shutdown':
      return <Shutdown {...props} />
    case 'Slowdown':
      return <Slowdown {...props} />
    case 'Net Unit Capacity':
      return <NetUnitCapacity {...props} />
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

const TcsInput = () => {
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
  const PLANT_NAME = plantObject?.name
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

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false)

  const handleViewHistory = () => {
    setHistoryDialogOpen(true)
  }

  const handleCloseHistory = () => {
    setHistoryDialogOpen(false)
  }

  // Get current tab object (has id, displayName, displaySequence)
  const currentTab = tabObj[tabIndex] || {}

  // Console user roles
  console.log('User Roles:', keycloak?.realmAccess?.roles)

  const userRole = useMemo(() => {
    let allUsers = keycloak?.realmAccess?.roles
    console.log('allUsers', allUsers)
    return getUserRole(allUsers)
  }, [keycloak?.realmAccess?.roles])

  // Fetch all tabs and visible tab IDs from backend
  useEffect(() => {
    fetchTabsData()
  }, [PLANT_ID, SITE_ID, VERTICAL_ID])

  const fetchTabsData = async () => {
    try {
      if (!PLANT_ID || !SITE_ID || !VERTICAL_ID) return

      // First API: Get list of all tabs
      const allTabsResponse = await TcsApiService.getTcsAllTabs(keycloak)
      const allTabsList = allTabsResponse?.data?.configurationTypeList || []
      setTabObj(allTabsList)

      // Second API: Get array of tab IDs to show
      const visibleTabsResponse = await TcsApiService.getTcsVisibleTabs(
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
    } catch (err) {
      console.error('Error fetching tabs:', err)
      setSnackbarData({
        message: 'Failed to load tabs configuration',
        severity: 'error',
      })
      setSnackbarOpen(true)
    }
  }

  // Handle remark submission
  const handleRemarkSubmit = (remark) => {
    console.log('Remark submitted submitted by:', userRole)
    console.log('Remark submitted:', remark)
    // TODO: Add API call to save remark
    setSnackbarData({
      message: 'Remark submitted successfully!',
      severity: 'success',
    })
    setSnackbarOpen(true)
  }

  const data = [
    {
      id: 1,
      submittedDate: '2022-01-15 14:30:00',
      submittedBy: 'Plant Manager',
      submittedRemark: 'Resubmitted after corrections',
      verifiedDate: '2022-01-16 09:15:00',
      verifiedBy: 'EPS Engineer',
      verifiedRemark: 'Data looks good, approved for processing',
      status: 'Approved',
    },
    {
      id: 2,
      submittedDate: '2022-01-10 10:45:00',
      submittedBy: 'Plant Manager',
      submittedRemark: 'Initial submission with all data validated',
      verifiedDate: '2022-01-12 11:20:00',
      verifiedBy: 'EPS Engineer',
      verifiedRemark: 'Minor discrepancies found, needs revision',
      status: 'Rejected',
    },
  ]

  return (
    <Box
      sx={{
        p: 2,
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
        borderRadius: '4px',
        backgroundColor: '#fff',
      }}
    >
      {/* Tabs and Action Buttons in One Row */}
      <Box
        sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}
      >
        {/* Tabs Section - Flex grow to fill available space */}
        <Box sx={{ flex: 1, overflowX: 'auto' }}>
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

        {/* Submit button and History icon - Fixed on right */}
        <Box
          sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}
        >
          <Button
            className='btn-save'
            style={{ background: '#28a745', color: '#ffffff' }}
            onClick={() => setRemarkDialogOpen(true)}
          >
            Submit
          </Button>
          <Tooltip title='View History'>
            <Button
              variant='outlined'
              onClick={handleViewHistory}
              sx={{
                textTransform: 'none',
                borderColor: '#1976d2',
                color: '#1976d2',
                padding: '6px 16px',
                maxHeight: '1.8rem',
                '&:hover': {
                  borderColor: '#1565c0',
                  backgroundColor: '#e3f2fd',
                },
              }}
            >
              <HistoryIcon />
            </Button>
          </Tooltip>
        </Box>
      </Box>

      {/* Tab Content */}
      <Box>
        {renderTabComponent(currentTab.displayName, {
          currentTab,
          PLANT_ID,
          PLANT_NAME,
          AOP_YEAR,
          SITE_ID,
          snackbarData,
          setSnackbarData,
          snackbarOpen,
          setSnackbarOpen,
        })}
      </Box>

      <RemarkDialog
        open={remarkDialogOpen}
        handleClose={() => setRemarkDialogOpen(false)}
        title='TCS Input Submission'
        placeholder='Enter your remarks here...'
        onSubmit={handleRemarkSubmit}
        maxLength={1000}
        historyData={data}
        role={userRole}
      />

      {/* History Dialog */}
      <HistoryDialog
        open={historyDialogOpen}
        onClose={handleCloseHistory}
        title='Audit Trail'
        data={data}
        role={userRole}
      />

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default TcsInput
