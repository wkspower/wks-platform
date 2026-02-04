import { Box, Tab, Tabs } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'
import { useSession } from 'SessionStoreContext'
import UnitCapacity from './UnitCapacity'
import NetUnitCapacity from './NetUnitCapacity'
import Shutdown from './Shutdown'
import Slowdown from './Slowdown'
import CPPUnitsSdPlan from './CPPUnitsSdPlan'
import CrudBlendWindow from './CrudBlendWindow'
import ROGC from './ROGC'
import PCGOutlook from './PCGOutlook'
import RemarkDialog from '../TcsInput/workflow/RemarkDialog'
import HistoryDialog from '../TcsInput/workflow/HistoryDialog'
import ApproveDialog from '../TcsInput/workflow/ApproveDialog'
import SubmitSection from '../TcsInput/workflow/SubmitSection'
import { getUserRole, ROLES } from '../utils/roleUtils'

// Handler to render tab component based on displayName
const renderTabComponent = (tabDisplayName, props) => {
  switch (tabDisplayName) {
    case 'Unit Capacity':
      return <UnitCapacity {...props} />
    case 'Net Unit Capacity':
      return <NetUnitCapacity {...props} />
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

const TcsOutput = () => {
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

  // Remark state
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false)
  const [approveDialogOpen, setApproveDialogOpen] = useState(false)
  const [isSubmitEligible, setIsSubmitEligible] = useState(false)
  const [isCheckingEligibility, setIsCheckingEligibility] = useState(false)
  const [isSubmittingRemark, setIsSubmittingRemark] = useState(false)
  const [timelineData, setTimelineData] = useState([])

  const handleViewHistory = () => {
    setHistoryDialogOpen(true)
  }

  const handleReviewClick = () => {
    setApproveDialogOpen(true)
  }

  const userRole = useMemo(() => {
    let allUsers = keycloak?.realmAccess?.roles
    console.log('allUsers', allUsers)
    return getUserRole(allUsers)
  }, [keycloak?.realmAccess?.roles])
  console.log('userRole', userRole)
  // Get current tab object (has id, displayName, displaySequence)
  const currentTab = tabObj[tabIndex] || {}

  // Generate dynamic tooltip based on role and eligibility
  const submitTooltip = useMemo(() => {
    if (!isSubmitEligible) {
      if (userRole === ROLES.EPS_ENGINEER) {
        return 'All plants must be approved before submission'
      } else if (userRole === ROLES.CTS_HEAD || userRole === ROLES.EPS_HEAD) {
        return 'Submission is pending from the EPS Engineer, or you have already submitted.'
      } else if (userRole === ROLES.CLUSTER_HEAD) {
        return 'Submission is pending from the CTS/EPS Head, or you have already submitted.'
      }

      return 'Submission not available'
    }

    if (userRole === ROLES.EPS_ENGINEER) {
      return 'Submit all approved plants to CTS Head/EPS Head'
    } else if (userRole === ROLES.CTS_HEAD || userRole === ROLES.EPS_HEAD) {
      return 'Submit'
    } else if (userRole === ROLES.CLUSTER_HEAD) {
      return 'Submit'
    }
    return 'Submit'
  }, [isSubmitEligible, userRole])

  // Fetch all tabs and visible tab IDs from backend
  useEffect(() => {
    fetchTabsData()
    checkSubmitEligibility()
  }, [AOP_YEAR, PLANT_ID, SITE_ID, VERTICAL_ID])

  // Check if user can submit based on workflow variables
  const checkSubmitEligibility = async () => {
    try {
      if (!keycloak || !SITE_ID || !AOP_YEAR) {
        return
      }

      setIsCheckingEligibility(true)

      // Fetch workflow variables to check approval status
      const variables = await TcsWorkflowApiService.getWorkflowVariables(
        keycloak,
        VERTICAL_ID,
        SITE_ID,
        AOP_YEAR,
      )

      console.log('Workflow Variables:', variables)
      setTimelineData(variables)
      if (variables.length === 0) {
        setIsSubmitEligible(false)
        return
      }

      // Find approvalStatus variable
      const approvalStatusVar = variables?.find(
        (v) => v.name === 'approvalStatus',
      )

      if (approvalStatusVar && approvalStatusVar.value) {
        try {
          // Parse the JSON value
          const approvalStatus = JSON.parse(approvalStatusVar.value)
          console.log('Approval Status:', approvalStatus)

          // For EPS Engineer: Check if all plants have been approved and EBS not yet submitted
          if (userRole === ROLES.EPS_ENGINEER) {
            // Check if EBS approval is already done from approvalStatus
            const ebsApproved = approvalStatus.ebs_approved === true

            // If EBS already approved, EPS Engineer cannot submit again
            if (ebsApproved) {
              setIsSubmitEligible(false)
            } else {
              // Check if approved count equals total count
              const plantCountVar = variables?.find(
                (v) => v.name === 'plantCount',
              )

              if (plantCountVar && plantCountVar.value) {
                try {
                  const plantCount = JSON.parse(plantCountVar.value)
                  const approvedCount = plantCount.approved_plants || 0
                  const totalCount = plantCount.total_plants || 0

                  const allPlantsApproved =
                    approvedCount === totalCount && totalCount > 0

                  setIsSubmitEligible(allPlantsApproved)
                } catch (parseError) {
                  console.error('Error parsing plantCount:', parseError)
                  setIsSubmitEligible(false)
                }
              } else {
                setIsSubmitEligible(false)
              }
            }
          }
          // For CTS Head/EPS Head: Check if EBS approved is true AND CTS approved is false
          else if (userRole === ROLES.CTS_HEAD || userRole === ROLES.EPS_HEAD) {
            const ebsApproved = approvalStatus.ebs_approved === true
            const ctsApproved = approvalStatus.cts_approved === true

            // Enable submit button only if EBS is approved but CTS is not yet approved
            const canSubmit = ebsApproved && !ctsApproved
            setIsSubmitEligible(canSubmit)
          }
          // For Cluster Head: Check if CTS approved is true AND Cluster Head approved is false
          else if (userRole === ROLES.CLUSTER_HEAD) {
            const ctsApproved = approvalStatus.cts_approved === true
            const clusterHeadApproved =
              approvalStatus.cluster_head_approved === true

            // Enable submit button only if CTS is approved but Cluster Head is not yet approved
            const canSubmit = ctsApproved && !clusterHeadApproved
            setIsSubmitEligible(canSubmit)
          } else {
            setIsSubmitEligible(false)
          }
        } catch (parseError) {
          console.error('Error parsing approvalStatus:', parseError)
          setIsSubmitEligible(false)
        }
      } else {
        setIsSubmitEligible(false)
      }
    } catch (err) {
      console.error('Error checking submit eligibility:', err)
      setIsSubmitEligible(false)
    } finally {
      setIsCheckingEligibility(false)
    }
  }

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

  // Handle submission based on user role
  const handleRemarkSubmit = async (remark) => {
    try {
      // Validate required parameters
      if (
        !keycloak ||
        !plantObject?.name ||
        !SITE_ID ||
        !userRole ||
        !AOP_YEAR
      ) {
        setSnackbarData({
          message: 'Missing required parameters. Please refresh and try again.',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return
      }

      setIsSubmittingRemark(true)

      // Call appropriate API based on user role
      if (userRole === ROLES.EPS_ENGINEER) {
        // EPS Engineer submission - uses submittedBy and submissionRemark
        await TcsWorkflowApiService.epsEngineerSubmission(
          keycloak,
          plantObject.name,
          SITE_ID,
          VERTICAL_ID,
          AOP_YEAR,
          remark,
          userRole, // submittedBy
        )
      }

      setSnackbarData({
        message: 'Submission completed successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      // Refresh eligibility after submission
      await checkSubmitEligibility()

      // Close the remark dialog on success
      setRemarkDialogOpen(false)
    } catch (err) {
      console.error('Error submitting:', err)

      setSnackbarData({
        message: 'Failed to complete submission. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setIsSubmittingRemark(false)
    }
  }

  // Handle approval for CTS_HEAD and EPS_HEAD
  const handleApprove = async (remark) => {
    try {
      // Validate required parameters
      if (!keycloak || !SITE_ID || !VERTICAL_ID || !userRole || !AOP_YEAR) {
        setSnackbarData({
          message: 'Missing required parameters. Please refresh and try again.',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return
      }

      setIsSubmittingRemark(true)

      // Call appropriate approve APIs based on role
      if (userRole === ROLES.CLUSTER_HEAD) {
        // First API: Approve/Reject with true
        await TcsWorkflowApiService.clusterHeadApproveReject(
          keycloak,
          SITE_ID,
          true, // approvalStatus = true for approve
          AOP_YEAR,
          remark,
          userRole, // verifiedBy
          VERTICAL_ID,
        )

        // Second API: Submission
        await TcsWorkflowApiService.clusterHeadSubmission(
          keycloak,
          SITE_ID,
          AOP_YEAR,
          remark,
          userRole, // verifiedBy
          VERTICAL_ID,
        )
      } else {
        // CTS_HEAD or EPS_HEAD
        // First API: Approve/Reject with true
        await TcsWorkflowApiService.ctsHeadApproveReject(
          keycloak,
          SITE_ID,
          true, // approvalStatus = true for approve
          AOP_YEAR,
          remark,
          userRole, // submittedBy
          VERTICAL_ID,
        )

        // Second API: Submission
        await TcsWorkflowApiService.ctsHeadSubmission(
          keycloak,
          SITE_ID,
          AOP_YEAR,
          remark,
          userRole, // submittedBy
          VERTICAL_ID,
        )
      }

      setSnackbarData({
        message: 'Approved successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      // Refresh eligibility after approval
      await checkSubmitEligibility()

      // Close the remark dialog on success
      setRemarkDialogOpen(false)
    } catch (err) {
      console.error('Error approving:', err)

      setSnackbarData({
        message: 'Failed to approve. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      throw err
    } finally {
      setIsSubmittingRemark(false)
    }
  }

  // Handle rejection for CTS_HEAD and EPS_HEAD
  const handleReject = async (remark) => {
    try {
      // Validate required parameters
      if (!keycloak || !SITE_ID || !VERTICAL_ID || !userRole || !AOP_YEAR) {
        setSnackbarData({
          message: 'Missing required parameters. Please refresh and try again.',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return
      }

      setIsSubmittingRemark(true)

      // Call appropriate reject API based on role
      if (userRole === ROLES.CLUSTER_HEAD) {
        await TcsWorkflowApiService.clusterHeadApproveReject(
          keycloak,
          SITE_ID,
          false, // approvalStatus = false for reject
          AOP_YEAR,
          remark,
          userRole, // verifiedBy
          VERTICAL_ID,
        )
      } else {
        // CTS_HEAD or EPS_HEAD
        await TcsWorkflowApiService.ctsHeadApproveReject(
          keycloak,
          SITE_ID,
          false, // approvalStatus = false for reject
          AOP_YEAR,
          remark,
          userRole, // verifiedBy
          VERTICAL_ID,
        )
      }

      setSnackbarData({
        message: 'Rejected successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      // Refresh eligibility after rejection
      await checkSubmitEligibility()

      // Close the remark dialog on success
      setRemarkDialogOpen(false)
    } catch (err) {
      console.error('Error rejecting:', err)

      setSnackbarData({
        message: 'Failed to reject. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      throw err
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
          onReviewClick={handleReviewClick}
          isEligible={isSubmitEligible}
          isLoading={isCheckingEligibility || isSubmittingRemark}
          submitTooltip={submitTooltip}
          showReviewBtn={userRole === ROLES.EPS_ENGINEER}
          reviewTooltip='Review and approve/reject plants'
        />
      </Box>

      {/* Tab Content */}
      <Box>
        {renderTabComponent(currentTab.displayName, {
          currentTab,
          PLANT_ID,
          AOP_YEAR,
          SITE_ID,
          VERTICAL_ID,
          snackbarData,
          setSnackbarData,
          snackbarOpen,
          setSnackbarOpen,
          userRole,
        })}
      </Box>

      <RemarkDialog
        open={remarkDialogOpen}
        handleClose={() => setRemarkDialogOpen(false)}
        placeholder='Enter your remarks here...'
        onSubmit={handleRemarkSubmit}
        onApprove={handleApprove}
        onReject={handleReject}
        maxLength={1000}
        role={userRole}
        keycloak={keycloak}
      />

      {/* History Dialog */}
      <HistoryDialog
        open={historyDialogOpen}
        onClose={() => setHistoryDialogOpen(false)}
        title='Audit Trail'
        userRole={userRole}
        timelineData={timelineData}
      />

      {/* Approve/Reject Dialog */}
      <ApproveDialog
        open={approveDialogOpen}
        onClose={async () => {
          setApproveDialogOpen(false)
          await checkSubmitEligibility()
        }}
        tab={currentTab.displayName}
        year={AOP_YEAR}
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

export default TcsOutput
