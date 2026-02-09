import { Box, Tab, Tabs } from '@mui/material'
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
import HistoryDialog from './workflow/HistoryDialog'
import SubmitSection from './workflow/SubmitSection'
import { getUserRole } from '../utils/roleUtils'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'

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
  const [isSubmitEligible, setIsSubmitEligible] = useState(true)
  const [isCheckingEligibility, setIsCheckingEligibility] = useState(false)
  const [isWorkflowTriggered, setIsWorkflowTriggered] = useState(false)

  // Check workflow status on mount
  // useEffect(() => {
  //   checkWorkflowTriggered()
  //   checkSubmitEligibility()
  // }, [PLANT_ID, AOP_YEAR, SITE_ID, VERTICAL_ID])

  const checkSubmitEligibility = async () => {
    try {
      setIsCheckingEligibility(true)
      if (!PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID) {
        setIsSubmitEligible(false)
        return
      }

      const response = await TcsWorkflowApiService.checkSubmitEligibility(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )

      const eligible = response?.isEligible !== false
      setIsSubmitEligible(eligible)

      if (!eligible) {
        setSnackbarData({
          message: response?.message || 'Submit is not eligible at this time',
          severity: 'warning',
        })
        setSnackbarOpen(true)
      }
    } catch (err) {
      console.error('Error checking submit eligibility:', err)
      setIsSubmitEligible(false)
      setSnackbarData({
        message: 'Failed to check submit eligibility',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setIsSubmitEligible(true)
      setIsCheckingEligibility(false)
    }
  }
  const checkWorkflowTriggered = async () => {
    try {
      const response = await TcsWorkflowApiService.checkWorkflowStatus(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )

      const isTriggered = response?.isTriggered === true

      // If workflow is already triggered, disable submit button
      setIsWorkflowTriggered(isTriggered)

      if (isTriggered) {
        setSnackbarData({
          message:
            response?.message ||
            'Workflow has already been triggered for this submission',
          severity: 'info',
        })
        setSnackbarOpen(true)
      }

      return { isTriggered, response }
    } catch (err) {
      console.error('Error checking workflow status:', err)
      setSnackbarData({
        message: 'Failed to check workflow status',
        severity: 'error',
      })
      setSnackbarOpen(true)
    }
  }

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

  // Handle workflow trigger
  const handleTriggerWorkflow = async () => {
    try {
      if (!keycloak || !PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID) {
        setSnackbarData({
          message: 'Missing required parameters to trigger workflow',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return { success: false }
      }

      // Trigger workflow
      await TcsWorkflowApiService.triggerWorkflow(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )

      // Update workflow triggered state
      setIsWorkflowTriggered(true)

      setSnackbarData({
        message: 'Workflow triggered successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      return { success: true }
    } catch (err) {
      console.error('Error triggering workflow:', err)
      setSnackbarData({
        message: 'Failed to trigger workflow',
        severity: 'error',
      })
      setSnackbarOpen(true)
      return { success: false, error: err }
    }
  }

  // Handle remark submission
  const handleRemarkSubmit = async (remark) => {
    console.log('Remark submitted by:', userRole)
    console.log('Remark:', remark)

    try {
      if (keycloak && PLANT_ID && AOP_YEAR && SITE_ID && VERTICAL_ID) {
        if (!isWorkflowTriggered) {
          await handleTriggerWorkflow()
        }

        // Always save remark first
        await TcsWorkflowApiService.saveRemark(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
          VERTICAL_ID,
          remark,
        )
      }
    } catch (err) {
      console.error('Error saving remark:', err)
      setSnackbarData({
        message: 'Failed to save remark',
        severity: 'error',
      })
      setSnackbarOpen(true)
      return
    }

    // Close the remark dialog
    setRemarkDialogOpen(false)
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
        <SubmitSection
          onSubmitClick={() => setRemarkDialogOpen(true)}
          onViewHistory={handleViewHistory}
          isEligible={isSubmitEligible}
          isLoading={isCheckingEligibility}
        />
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
        keycloak={keycloak}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
        setSnackbarOpen={setSnackbarOpen}
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
