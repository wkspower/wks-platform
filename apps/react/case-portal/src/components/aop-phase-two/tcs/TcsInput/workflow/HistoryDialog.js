import React, { useState, useEffect, useMemo } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Divider,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box,
  CircularProgress,
} from '@mui/material'
import { ROLES } from '../../utils/roleUtils'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { parseApprovalStatusResponse } from './utilityFunctions'
import WorkflowTimeline from './WorkflowTimeline'
import { Tooltip } from '../../../../../../node_modules/@mui/material/index'

const RemarkCell = ({ text, maxLength = 100 }) => {
  const [expanded, setExpanded] = useState(false)
  const isLong = text && text.length > maxLength

  if (!text || text === '-') {
    return (
      <Typography variant='body2' sx={{ color: 'text.secondary' }}>
        -
      </Typography>
    )
  }

  return (
    <Box>
      <Typography
        variant='body2'
        sx={{
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
          pr: 1,
        }}
      >
        {expanded || !isLong ? text : `${text.substring(0, maxLength)}...`}
      </Typography>
      {isLong && (
        <Button
          size='small'
          onClick={() => setExpanded(!expanded)}
          sx={{
            textTransform: 'none',
            minWidth: 'auto',
            p: 0,
            mt: 0.5,
            fontSize: '0.75rem',
            color: '#1976d2',
            '&:hover': {
              backgroundColor: 'transparent',
              textDecoration: 'underline',
            },
          }}
        >
          {expanded ? 'View Less' : 'View More'}
        </Button>
      )}
    </Box>
  )
}

// Helper function to map role codes to display names
const getRoleDisplayName = (roleCode) => {
  if (!roleCode) return '-'

  switch (roleCode) {
    case 'plant_manager':
      return 'CTS Engineer'
    case 'eps_engineer':
      return 'AOM'
    case 'cts_head':
      return 'CTS Head'
    case 'eps_head':
      return 'EPS Head'
    case 'cluster_head':
      return 'R&M Cluster Head'
    default:
      return roleCode
  }
}

const HistoryDialog = ({
  open,
  onClose,
  title = 'History',
  userRole = '',
  plantId = null,
  type = 'ROLE_WISE',
  timelineData = [],
}) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantId || plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  // Tab state management
  const [tabValue, setTabValue] = useState(0)
  const [auditTrailData, setAuditTrailData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Transform timeline data into workflow steps
  const workflowSteps = useMemo(() => {
    if (!timelineData || timelineData.length === 0) {
      // Workflow not started - show Step 1 active, rest pending
      return [
        {
          id: 1,
          label: 'Step 1',
          // role: 'Plant Manager',
          role: 'CTS Engineer',
          status: 'active',
        },
        {
          id: 2,
          label: 'Step 2',
          role: 'AOM',
          status: 'pending',
        },
        {
          id: 3,
          label: 'Step 3',
          role: 'EPS Head / CTS Head',
          status: 'pending',
        },
        {
          id: 4,
          label: 'Step 4',
          role: 'R&M Cluster Head',
          status: 'pending',
        },
      ]
    }
    return parseApprovalStatusResponse(timelineData, PLANT_NAME)
  }, [timelineData, PLANT_NAME])

  console.log('timelineData', timelineData)
  console.log('workflowSteps', workflowSteps)
  // Fetch audit trail data when dialog opens
  useEffect(() => {
    const fetchAuditTrail = async () => {
      if (!open || !keycloak || !SITE_ID || !VERTICAL_ID || !userRole) {
        return
      }

      // For plant_manager, need PLANT_ID
      if (userRole === 'plant_manager' && !PLANT_ID) {
        return
      }

      // For eps_engineer, need AOP_YEAR
      if (userRole === 'eps_engineer' && !AOP_YEAR) {
        return
      }

      setLoading(true)
      setError(null)

      try {
        let response

        if (type == 'ROLE_WISE') {
          if (userRole === 'plant_manager') {
            response =
              await TcsWorkflowApiService.getPlantManagerSubmissionHistory(
                keycloak,
                PLANT_ID,
                SITE_ID,
                VERTICAL_ID,
                AOP_YEAR,
              )
          }
          if (userRole === 'eps_engineer') {
            response =
              await TcsWorkflowApiService.getEpsEngineerSubmissionHistory(
                keycloak,
                SITE_ID,
                VERTICAL_ID,
                AOP_YEAR,
              )
          }
          if (userRole === 'cts_head' || userRole === 'eps_head') {
            response = await TcsWorkflowApiService.getCtsHeadSubmissionHistory(
              keycloak,
              SITE_ID,
              VERTICAL_ID,
              AOP_YEAR,
            )
          }
          if (userRole === 'cluster_head') {
            response =
              await TcsWorkflowApiService.getClusterHeadSubmissionHistory(
                keycloak,
                SITE_ID,
                VERTICAL_ID,
                AOP_YEAR,
              )
          }
        } else {
          response = await TcsWorkflowApiService.getPlantwiseHistory(
            keycloak,
            PLANT_ID,
            SITE_ID,
            VERTICAL_ID,
            AOP_YEAR,
          )
        }

        // Sort the data by submissionDateTime (primary) and verifiedDateTime (secondary)
        const sortedData = (response || []).sort((a, b) => {
          // Parse dates - format: "Feb 2, 2026, 11:19:21 AM"
          const parseDate = (dateStr) => {
            if (!dateStr) return new Date(0) // Return epoch for null/undefined dates
            return new Date(dateStr)
          }

          const dateA = parseDate(a.submissionDateTime)
          const dateB = parseDate(b.submissionDateTime)

          // Primary sort: submissionDateTime (descending - most recent first)
          if (dateA.getTime() !== dateB.getTime()) {
            return dateB.getTime() - dateA.getTime()
          }

          // Secondary sort: verifiedDateTime (descending - most recent first)
          const verifiedDateA = parseDate(a.verifiedDateTime)
          const verifiedDateB = parseDate(b.verifiedDateTime)
          return verifiedDateB.getTime() - verifiedDateA.getTime()
        })

        setAuditTrailData(sortedData)
      } catch (err) {
        console.error('Error fetching audit trail:', err)
        setAuditTrailData([])
      } finally {
        setLoading(false)
      }
    }

    fetchAuditTrail()
  }, [
    open,
    keycloak,
    PLANT_ID,
    SITE_ID,
    VERTICAL_ID,
    AOP_YEAR,
    userRole,
    plantId,
  ])

  const columns = useMemo(() => {
    const baseColumns = [
      {
        field: 'submissionDateTime',
        header: 'Submission Date',
        width: '12%',
        minWidth: '160px',
      },
      {
        field: 'submittedBy',
        header: 'Submitted By',
        width: '10%',
        minWidth: '100px',
      },
      {
        field: 'submissionRemark',
        header: 'Submission Remark',
        width: '18%',
        minWidth: '150px',
      },
      {
        field: 'verifiedDateTime',
        header: 'Verified Date',
        width: '12%',
        minWidth: '160px',
      },
      {
        field: 'verifiedBy',
        header: 'Verified By',
        width: '10%',
        minWidth: '100px',
      },
      {
        field: 'verifiedRemark',
        header: 'Verified Remark',
        width: '18%',
        minWidth: '150px',
      },
      {
        field: 'status',
        header: 'Status',
        width: '10%',
        minWidth: '100px',
        isChip: true,
      },
    ]

    // Only show Plant Name column based on type and role
    if (
      userRole == ROLES.PLANT_MANAGER ||
      (userRole == ROLES.EPS_ENGINEER && type == 'PLANT_WISE')
    ) {
      return [
        {
          field: 'plantName',
          header: 'Plant Name',
          width: '10%',
          minWidth: '100px',
        },
        ...baseColumns,
      ]
    }

    return baseColumns
  }, [userRole, type])

  const displayData = auditTrailData.length > 0 ? auditTrailData : []

  // Display columns based on role
  const displayColumns = React.useMemo(() => {
    // Hide verified columns for CLUSTER_HEAD when type is ROLE_WISE
    if (userRole === ROLES.CLUSTER_HEAD && type === 'ROLE_WISE') {
      return columns.filter(
        (col) =>
          col.field !== 'verifiedDateTime' &&
          col.field !== 'verifiedBy' &&
          col.field !== 'verifiedRemark',
      )
    }
    return columns
  }, [columns, userRole, type])

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'approved':
        return 'success'
      case 'rejected':
        return 'error'
      case 'pending':
        return 'warning'
      case 'submitted':
        return 'info'
      default:
        return 'default'
    }
  }

  // Get role-based status tooltip
  const getStatusTooltip = (status) => {
    const statusLower = status?.toLowerCase()

    if (statusLower === 'pending') {
      switch (userRole) {
        case ROLES.PLANT_MANAGER:
          // return 'Pending for approval of EPS Engineer'
          return 'Pending for approval of AOM'
        case ROLES.EPS_ENGINEER:
          return 'Pending for approval of CTS Head / EPS Head'
        case ROLES.CTS_HEAD:
        case ROLES.EPS_HEAD:
          return 'Pending for approval of Cluster Head'
        case ROLES.CLUSTER_HEAD:
          return 'Finalised data for PIMS Output'
        default:
          return 'Pending approval'
      }
    }

    if (statusLower === 'approved') {
      switch (userRole) {
        case ROLES.PLANT_MANAGER:
          // return 'Approved by EPS Engineer'
          return 'Approved by AOM'
        case ROLES.EPS_ENGINEER:
          return 'Approved by CTS Head / EPS Head'
        case ROLES.CTS_HEAD:
        case ROLES.EPS_HEAD:
          return 'Approved by Cluster Head'
        case ROLES.CLUSTER_HEAD:
          return 'Final approval completed'
        default:
          return 'Approved'
      }
    }

    if (statusLower === 'rejected') {
      switch (userRole) {
        case ROLES.PLANT_MANAGER:
          // return 'Rejected by EPS Engineer'
          return 'Rejected by AOM'
        case ROLES.EPS_ENGINEER:
          return 'Rejected by CTS Head / EPS Head'
        case ROLES.CTS_HEAD:
        case ROLES.EPS_HEAD:
          return 'Rejected by Cluster Head'
        case ROLES.CLUSTER_HEAD:
          return 'Rejected at final approval'
        default:
          return 'Rejected'
      }
    }

    return status || 'Unknown status'
  }

  console.log('userRole', userRole)
  console.log('displayData', displayData)

  const getTitle = () => {
    switch (userRole) {
      case ROLES.PLANT_MANAGER:
        return 'CTS Engineer History'
      case ROLES.CTS_HEAD:
      case ROLES.EPS_HEAD:
        return 'EPS/CTS Head History'
      case ROLES.EPS_ENGINEER:
        return 'AOM History'
      default:
        return title
    }
  }
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth='md'
      fullWidth
      PaperProps={{
        sx: {
          minHeight: '400px',
          maxHeight: '80vh',
        },
      }}
    >
      <DialogTitle>
        <Typography variant='h6' component='div' fontWeight='600'>
          {getTitle()}
        </Typography>
        <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
          Complete audit trail of all changes
        </Typography>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ p: 0 }}>
        {/* Timeline Tab */}
        <Box sx={{ width: '100%', p: 3 }}>
          <WorkflowTimeline steps={workflowSteps} />
        </Box>

        {/* Audit Trail Tab */}
        <>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : displayData.length === 0 ? (
            <Typography
              variant='body2'
              color='text.secondary'
              sx={{ textAlign: 'center', py: 4 }}
            >
              No history available
            </Typography>
          ) : (
            <TableContainer
              component={Paper}
              elevation={0}
              sx={{
                ml: 'auto',
                mr: 'auto',
                maxWidth: '95%',
                maxHeight: '350px',
                overflowX: 'auto',
                overflowY: 'auto',
                mt: 2,
                '&::-webkit-scrollbar': {
                  height: '6px',
                  width: '6px',
                },
                '&::-webkit-scrollbar-track': {
                  bgcolor: '#f5f5f5',
                },
                '&::-webkit-scrollbar-thumb': {
                  bgcolor: '#bdbdbd',
                  borderRadius: 1,
                  '&:hover': {
                    bgcolor: '#9e9e9e',
                  },
                },
              }}
            >
              <Table stickyHeader>
                <TableHead>
                  <TableRow>
                    {displayColumns.map((col, idx) => {
                      const isStatusColumn = col.field === 'status'
                      const isLastColumn = idx === displayColumns.length - 1
                      return (
                        <TableCell
                          key={col.field}
                          sx={{
                            fontWeight: 600,
                            bgcolor: '#f5f5f5',
                            borderBottom: '2px solid #e0e0e0',
                            width: col.width || 'auto',
                            minWidth: col.minWidth || 'auto',
                            whiteSpace: 'nowrap',
                            position: 'sticky',
                            top: 0,
                            zIndex: isStatusColumn ? 3 : 2,
                            ...(isStatusColumn && {
                              right: 0,
                              boxShadow: '-2px 0 4px rgba(0, 0, 0, 0.1)',
                            }),
                          }}
                        >
                          {col.header}
                        </TableCell>
                      )
                    })}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {displayData.map((item, index) => (
                    <TableRow
                      key={item.id || index}
                      sx={{
                        '&:hover': {
                          bgcolor: 'action.hover',
                        },
                        bgcolor: index === 0 ? '#f0f7ff' : 'inherit',
                      }}
                    >
                      {displayColumns.map((col) => {
                        const isStatusColumn = col.field === 'status'
                        return (
                          <TableCell
                            key={col.field}
                            sx={{
                              width: col.width || 'auto',
                              minWidth: col.minWidth || 'auto',
                              ...(isStatusColumn && {
                                position: 'sticky',
                                right: 0,
                                zIndex: 1,
                                bgcolor: index === 0 ? '#f0f7ff' : '#ffffff',
                                boxShadow: '-2px 0 4px rgba(0, 0, 0, 0.1)',
                                '&:hover': {
                                  bgcolor: index === 0 ? '#f0f7ff' : '#ffffff',
                                },
                              }),
                            }}
                          >
                            {col.isChip ? (
                              <Tooltip
                                title={getStatusTooltip(item[col.field])}
                                arrow
                                placement='top'
                              >
                                <Chip
                                  label={
                                    userRole === ROLES.CLUSTER_HEAD &&
                                    item[col.field]?.toLowerCase() === 'pending'
                                      ? 'SUBMITTED'
                                      : item[col.field]
                                  }
                                  color={
                                    userRole === ROLES.CLUSTER_HEAD &&
                                    item[col.field]?.toLowerCase() === 'pending'
                                      ? 'success'
                                      : getStatusColor(item[col.field])
                                  }
                                  size='small'
                                  sx={{ fontWeight: 500, cursor: 'pointer' }}
                                />
                              </Tooltip>
                            ) : col.field === 'submittedRemark' ||
                              col.field === 'verifiedRemark' ||
                              col.field === 'submissionRemark' ? (
                              <RemarkCell text={item[col.field]} />
                            ) : col.field === 'submittedBy' ||
                              col.field === 'verifiedBy' ? (
                              <Typography
                                variant='body2'
                                sx={{
                                  whiteSpace: 'pre-wrap',
                                  wordBreak: 'break-word',
                                  overflowY: 'auto',
                                  pr: 1,
                                }}
                              >
                                {getRoleDisplayName(item[col.field])}
                              </Typography>
                            ) : (
                              <Typography
                                variant='body2'
                                sx={{
                                  whiteSpace: 'pre-wrap',
                                  wordBreak: 'break-word',
                                  overflowY: 'auto',
                                  pr: 1,
                                }}
                              >
                                {item[col.field] || '-'}
                              </Typography>
                            )}
                          </TableCell>
                        )
                      })}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      </DialogContent>

      <Divider />

      <DialogActions sx={{ p: 2, gap: 1 }}>
        <Typography
          variant='body2'
          color='text.secondary'
          sx={{ flex: 1, ml: 1 }}
        >
          Total Records: {auditTrailData.length}
        </Typography>
        <Button onClick={onClose} variant='outlined' color='error'>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default HistoryDialog
