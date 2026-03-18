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
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

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
  const [isSubmittingRemark, setIsSubmittingRemark] = useState(false)
  const [timelineData, setTimelineData] = useState([])
  // Generate dynamic tooltip for Plant Manager
  const submitTooltip = useMemo(() => {
    if (!isSubmitEligible) {
      return 'Plant submission already done'
    }
    return 'Submit plant data to AOM for approval'
  }, [isSubmitEligible])

  // Check workflow status on mount
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && SITE_ID && VERTICAL_ID && PLANT_NAME) {
      checkSubmitEligibility()
    }
    checkWorkflowTriggered()
  }, [PLANT_ID, AOP_YEAR, SITE_ID, VERTICAL_ID, PLANT_NAME])

  const checkSubmitEligibility = async (showMessage = true) => {
    try {
      setIsCheckingEligibility(true)
      if (!PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID || !PLANT_NAME) {
        setIsSubmitEligible(false)
        return
      }
      // Fetch workflow variables to check submission status
      const variables = await TcsWorkflowApiService.getWorkflowVariables(
        keycloak,
        VERTICAL_ID,
        SITE_ID,
        AOP_YEAR,
      )

      setTimelineData(variables)
      if (variables.length == 0) {
        setIsSubmitEligible(true)
      } else {
        // Find submissionStatus variable
        const submissionStatusVar = variables?.find(
          (v) => v.name === 'submissionStatus',
        )

        if (submissionStatusVar && submissionStatusVar.value) {
          try {
            // Parse the JSON value
            const submissionStatus = JSON.parse(submissionStatusVar.value)

            // Check if current plant has already been submitted
            const isPlantSubmitted = submissionStatus[PLANT_NAME] === true

            if (isPlantSubmitted) {
              // Plant already submitted - disable submit button
              setIsSubmitEligible(false)
              // Only show message if showMessage is true (on page load, not after submission)
              if (showMessage) {
                setSnackbarData({
                  message: `${PLANT_NAME} has already been submitted`,
                  severity: 'info',
                })
                setSnackbarOpen(true)
              }
              return
            } else {
              // Plant not yet submitted - enable submit button
              setIsSubmitEligible(true)
              return
            }
          } catch (parseError) {
            console.error('Error parsing submissionStatus:', parseError)
          }
        }

        // Fallback to original eligibility check
        const eligible = response?.isEligible !== false
        setIsSubmitEligible(eligible)

        if (!eligible) {
          setSnackbarData({
            message: response?.message || 'Submit is not eligible at this time',
            severity: 'warning',
          })
          setSnackbarOpen(true)
        }
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
      setIsCheckingEligibility(false)
    }
  }

  const checkWorkflowTriggered = async () => {
    try {
      const response = await TcsWorkflowApiService.checkWorkflowStatus(
        keycloak,
        VERTICAL_ID,
        SITE_ID,
        AOP_YEAR,
      )

      // If workflow is already triggered, disable submit button
      setIsWorkflowTriggered(response)
      return response
    } catch (err) {
      console.error('Error checking workflow status:', err)
    }
  }
  // PRECHECK DONE

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
      if (!keycloak || !SITE_ID || !VERTICAL_ID) {
        setSnackbarData({
          message: 'Missing required parameters to trigger workflow',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return { success: false }
      }

      // Trigger workflow (start process)
      await TcsWorkflowApiService.triggerWorkflow(
        keycloak,
        VERTICAL_ID,
        SITE_ID,
        AOP_YEAR,
      )

      // Update workflow triggered state
      setIsWorkflowTriggered(true)

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

    // Validation: Check for missing required parameters
    if (
      !keycloak ||
      !PLANT_ID ||
      !PLANT_NAME ||
      !SITE_ID ||
      !VERTICAL_ID ||
      !AOP_YEAR
    ) {
      setSnackbarData({
        message: 'Missing required parameters. Please refresh and try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      return // Don't close dialog, allow user to retry
    }

    // Show loading state
    setIsSubmittingRemark(true)
    let workflowWasTriggered = false

    try {
      // If workflow not triggered, trigger it first and wait for success
      if (!isWorkflowTriggered) {
        const triggerResult = await handleTriggerWorkflow()

        workflowWasTriggered = triggerResult
      }

      // Complete plant submission task with remark
      // if (workflowWasTriggered) {
      await TcsWorkflowApiService.saveRemark(
        keycloak,
        PLANT_ID,
        PLANT_NAME,
        SITE_ID,
        VERTICAL_ID,
        userRole,
        remark,
        AOP_YEAR,
      )

      setSnackbarData({
        message: 'Plant submission completed successfully',
        severity: 'success',
      })
      setSnackbarOpen(true)

      // Refresh submit eligibility after submission (without showing "already submitted" message)
      await checkSubmitEligibility(false)

      // Close the remark dialog on success
      setRemarkDialogOpen(false)
      // }
    } catch (err) {
      console.error('Error saving remark:', err)

      // Handle partial failure: workflow started but submission failed
      if (workflowWasTriggered) {
        setSnackbarData({
          message:
            'Workflow started but plant submission failed. Please try submitting again.',
          severity: 'warning',
        })
      } else {
        setSnackbarData({
          message: 'Failed to complete plant submission. Please try again.',
          severity: 'error',
        })
      }
      setSnackbarOpen(true)
      // Don't close dialog, allow user to retry
    } finally {
      setIsSubmittingRemark(false)
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
      {/* Tabs and Action Buttons in One Row */}
      <Box
        sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}
      >
        {/* Tabs Section - Flex grow to fill available space */}
        <Box sx={{ flex: 1, overflowX: 'auto' }}>
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
          isWorkflowTriggered={isWorkflowTriggered}
          submitTooltip={submitTooltip}
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
          isSubmitEligible,
        })}
      </Box>

      <RemarkDialog
        open={remarkDialogOpen}
        handleClose={() => setRemarkDialogOpen(false)}
        title='TCS Input Submission'
        placeholder='Enter your remarks here...'
        onSubmit={handleRemarkSubmit}
        maxLength={1000}
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
        userRole={userRole}
        timelineData={timelineData}
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
