import React, { useState, useEffect, useMemo } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  IconButton,
  Tooltip,
  CircularProgress,
  Checkbox,
  Alert,
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import HistoryIcon from '@mui/icons-material/History'
import HistoryDialog from './HistoryDialog'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'

const ApproveDialog = ({ open, onClose, year, userRole, timelineData }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { siteObject, verticalObject } = dataGridStore

  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year

  const [remarks, setRemarks] = useState({})
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false)
  const [selectedPlantHistory, setSelectedPlantHistory] = useState(null)
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Bulk action states
  const [selectedPlants, setSelectedPlants] = useState([])
  const [bulkRemark, setBulkRemark] = useState('')
  const [bulkActionLoading, setBulkActionLoading] = useState(false)

  // Individual action loading states
  const [individualLoading, setIndividualLoading] = useState({})

  // Fetch audit trail entries when dialog opens
  useEffect(() => {
    const fetchEntries = async () => {
      if (!open || !keycloak || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) {
        return
      }

      setLoading(true)
      setError(null)

      try {
        const response =
          await TcsWorkflowApiService.getPlantDataForApproveReject(
            keycloak,
            SITE_ID,
            VERTICAL_ID,
            AOP_YEAR,
          )
        setEntries(response || [])
      } catch (err) {
        console.error('Error fetching audit trail entries:', err)
        setError('Failed to load plant entries')
        setEntries([])
      } finally {
        setLoading(false)
      }
    }

    fetchEntries()
  }, [open, keycloak, SITE_ID, VERTICAL_ID, AOP_YEAR])

  // Get plants from entries
  const uniquePlants = useMemo(() => {
    return entries.map((entry) => ({
      plantId: entry.plantId,
      plantName: entry.plantName || `Plant ${entry.plantId}`,
      siteId: entry.siteId,
      verticalId: entry.verticalId,
      submissionRemark: entry.submissionRemark || '',
      submittedBy: entry.submittedBy || '',
      submissionDateTime: entry.submissionDateTime || '',
      type: entry.type,
      status: entry.status || 'PENDING',
      verifiedBy: entry.verifiedBy || '',
      verifiedRemark: entry.verifiedRemark || '',
      verifiedDateTime: entry.verifiedDateTime || '',
    }))
  }, [entries])

  useEffect(() => {
    if (open) {
      const initialRemarks = {}
      uniquePlants.forEach((plant) => {
        initialRemarks[plant.plantId] = '' // Start with empty remark for EPS Engineer to fill
      })
      setRemarks(initialRemarks)
      setSelectedPlants([])
      setBulkRemark('')
    }
  }, [open, uniquePlants])

  // Reset individual remarks when bulk mode activates (2+ plants selected)
  useEffect(() => {
    if (selectedPlants.length > 1) {
      const clearedRemarks = {}
      uniquePlants.forEach((plant) => {
        clearedRemarks[plant.plantId] = ''
      })
      setRemarks(clearedRemarks)
    }
  }, [selectedPlants.length > 1])

  const handleRemarkChange = (plantId, value) => {
    setRemarks((prev) => ({
      ...prev,
      [plantId]: value,
    }))
  }

  const handleApproveClick = async (plantId) => {
    setIndividualLoading((prev) => ({ ...prev, [plantId]: 'approve' }))
    setError(null)

    try {
      const remark = remarks[plantId] || ''
      const plant = uniquePlants.find((p) => p.plantId === plantId)
      const plantName = plant?.plantName || ''

      await TcsWorkflowApiService.epsEngineerSingleApproveReject(
        keycloak,
        plantId,
        SITE_ID,
        VERTICAL_ID,
        true, // approvalStatus = true for approve
        remark,
        AOP_YEAR,
        userRole,
        plantName,
      )

      // Refresh entries after approval
      const response = await TcsWorkflowApiService.getPlantDataForApproveReject(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      setEntries(response || [])
    } catch (err) {
      console.error('Error approving plant:', err)
      setError('Failed to approve plant. Please try again.')
    } finally {
      setIndividualLoading((prev) => {
        const newState = { ...prev }
        delete newState[plantId]
        return newState
      })
    }
  }

  const handleRejectClick = async (plantId) => {
    setIndividualLoading((prev) => ({ ...prev, [plantId]: 'reject' }))
    setError(null)

    try {
      const remark = remarks[plantId] || ''
      const plant = uniquePlants.find((p) => p.plantId === plantId)
      const plantName = plant?.plantName || ''

      await TcsWorkflowApiService.epsEngineerSingleApproveReject(
        keycloak,
        plantId,
        SITE_ID,
        VERTICAL_ID,
        false, // approvalStatus = false for reject
        remark,
        AOP_YEAR,
        userRole,
        plantName,
      )

      // Refresh entries after rejection
      const response = await TcsWorkflowApiService.getPlantDataForApproveReject(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      setEntries(response || [])
    } catch (err) {
      console.error('Error rejecting plant:', err)
      setError('Failed to reject plant. Please try again.')
    } finally {
      setIndividualLoading((prev) => {
        const newState = { ...prev }
        delete newState[plantId]
        return newState
      })
    }
  }

  // Bulk action handlers
  const handleSelectPlant = (plantId) => {
    setSelectedPlants((prev) => {
      if (prev.includes(plantId)) {
        return prev.filter((id) => id !== plantId)
      }
      return [...prev, plantId]
    })
  }

  const handleSelectAll = (event) => {
    if (event.target.checked) {
      // Only select plants with PENDING status
      const pendingPlants = uniquePlants.filter((p) => p.status === 'PENDING')
      setSelectedPlants(pendingPlants.map((p) => p.plantId))
    } else {
      setSelectedPlants([])
    }
  }

  const handleBulkApprove = async () => {
    if (selectedPlants.length === 0 || !bulkRemark.trim()) {
      return
    }

    setBulkActionLoading(true)
    setError(null)

    try {
      // Build array of PlantSubmissionAuditTrailDTO objects
      const plantSubmissionList = selectedPlants.map((plantId) => {
        const plant = uniquePlants.find((p) => p.plantId === plantId)
        return {
          plantId,
          plantName: plant?.plantName || '',
          siteId: SITE_ID,
          verticalId: VERTICAL_ID,
          verifiedRemark: bulkRemark,
          verifiedBy: userRole,
        }
      })

      // Call bulk API with all plants at once
      await TcsWorkflowApiService.epsEngineerMultipleApproveReject(
        keycloak,
        SITE_ID,
        true, // approvalStatus = true for approve
        AOP_YEAR,
        plantSubmissionList,
      )

      // Refresh entries after bulk approval
      const response = await TcsWorkflowApiService.getPlantDataForApproveReject(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      setEntries(response || [])

      // Reset bulk action states
      setSelectedPlants([])
      setBulkRemark('')
      setError(null)
    } catch (err) {
      console.error('Error in bulk approve:', err)
      setError('Failed to approve plants. Please try again.')
    } finally {
      setBulkActionLoading(false)
    }
  }

  const handleBulkReject = async () => {
    if (selectedPlants.length === 0 || !bulkRemark.trim()) {
      return
    }

    setBulkActionLoading(true)
    setError(null)

    try {
      // Build array of PlantSubmissionAuditTrailDTO objects
      const plantSubmissionList = selectedPlants.map((plantId) => {
        const plant = uniquePlants.find((p) => p.plantId === plantId)
        return {
          plantId,
          plantName: plant?.plantName || '',
          siteId: SITE_ID,
          verticalId: VERTICAL_ID,
          verifiedRemark: bulkRemark,
          verifiedBy: userRole,
        }
      })

      // Call bulk API with all plants at once
      await TcsWorkflowApiService.epsEngineerMultipleApproveReject(
        keycloak,
        SITE_ID,
        false, // approvalStatus = false for reject
        AOP_YEAR,
        plantSubmissionList,
      )

      // Refresh entries after bulk rejection
      const response = await TcsWorkflowApiService.getPlantDataForApproveReject(
        keycloak,
        SITE_ID,
        VERTICAL_ID,
        AOP_YEAR,
      )
      setEntries(response || [])

      // Reset bulk action states
      setSelectedPlants([])
      setBulkRemark('')
      setError(null)
    } catch (err) {
      console.error('Error in bulk reject:', err)
      setError('Failed to reject plants. Please try again.')
    } finally {
      setBulkActionLoading(false)
    }
  }

  const handleCancel = () => {
    setRemarks({})
    setSelectedPlants([])
    setBulkRemark('')
    onClose()
  }

  const handleViewHistory = (plant) => {
    setSelectedPlantHistory(plant)
    setHistoryDialogOpen(true)
  }

  const handleCloseHistory = () => {
    setHistoryDialogOpen(false)
    setSelectedPlantHistory(null)
  }

  // Check if any individual action is in progress
  const isAnyIndividualActionLoading = Object.keys(individualLoading).length > 0

  console.log('entries', entries)

  return (
    <>
      <Dialog
        open={open}
        onClose={handleCancel}
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
          <Typography variant='h5' component='div' fontWeight='600'>
            Review
          </Typography>
          <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
            {selectedPlants.length > 1
              ? `${selectedPlants.length} plant(s) selected for bulk action`
              : 'Review and take action on each plant'}
          </Typography>
        </DialogTitle>

        <Divider />

        <DialogContent sx={{ p: 0 }}>
          {/* Bulk Action Section - Only show when more than 1 plant selected */}
          {selectedPlants.length > 1 && (
            <Box
              sx={{
                p: 2,
                bgcolor: '#f5f5f5',
                borderBottom: '1px solid #e0e0e0',
              }}
            >
              <Typography variant='subtitle2' fontWeight={600} sx={{ mb: 1 }}>
                Remark
              </Typography>
              <TextField
                fullWidth
                size='small'
                multiline
                rows={2}
                value={bulkRemark}
                onChange={(e) => setBulkRemark(e.target.value)}
                placeholder='Enter remark for all selected plants...'
                variant='outlined'
                sx={{
                  bgcolor: 'white',
                  '& .MuiOutlinedInput-root': {
                    fontSize: '0.875rem',
                  },
                }}
              />
              <Box sx={{ display: 'flex', gap: 1, mt: 1.5 }}>
                <Button
                  variant='contained'
                  size='small'
                  startIcon={
                    bulkActionLoading ? (
                      <CircularProgress size={16} color='inherit' />
                    ) : (
                      <CheckCircleIcon />
                    )
                  }
                  onClick={handleBulkApprove}
                  disabled={!bulkRemark.trim() || bulkActionLoading}
                  sx={{
                    bgcolor: '#2e7d32',
                    '&:hover': { bgcolor: '#1b5e20' },
                    textTransform: 'none',
                  }}
                >
                  {bulkActionLoading
                    ? 'Approving...'
                    : `Approve ${selectedPlants.length} Plant(s)`}
                </Button>
                <Button
                  variant='contained'
                  size='small'
                  startIcon={
                    bulkActionLoading ? (
                      <CircularProgress size={16} color='inherit' />
                    ) : (
                      <CancelIcon />
                    )
                  }
                  onClick={handleBulkReject}
                  disabled={!bulkRemark.trim() || bulkActionLoading}
                  sx={{
                    bgcolor: '#d32f2f',
                    '&:hover': { bgcolor: '#c62828' },
                    textTransform: 'none',
                  }}
                >
                  {bulkActionLoading
                    ? 'Rejecting...'
                    : `Reject ${selectedPlants.length} Plant(s)`}
                </Button>
              </Box>
            </Box>
          )}

          {error && (
            <Alert severity='error' sx={{ m: 2 }}>
              {error}
            </Alert>
          )}
          {loading ? (
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                py: 6,
              }}
            >
              <CircularProgress />
            </Box>
          ) : uniquePlants.length === 0 ? (
            <Box
              sx={{
                textAlign: 'center',
                py: 6,
                px: 3,
              }}
            >
              <Typography variant='body2' color='text.secondary'>
                No plants available for approval
              </Typography>
            </Box>
          ) : (
            <TableContainer
              component={Paper}
              elevation={0}
              sx={{
                ml: 2,
                mr: 2,
                maxHeight: '500px',
                '&::-webkit-scrollbar': {
                  width: '8px',
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
                    <TableCell
                      padding='checkbox'
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                      }}
                    >
                      <Checkbox
                        checked={
                          selectedPlants.length ===
                            uniquePlants.filter((p) => p.status === 'PENDING')
                              .length &&
                          uniquePlants.filter((p) => p.status === 'PENDING')
                            .length > 0
                        }
                        indeterminate={
                          selectedPlants.length > 0 &&
                          selectedPlants.length <
                            uniquePlants.filter((p) => p.status === 'PENDING')
                              .length
                        }
                        onChange={handleSelectAll}
                        disabled={bulkActionLoading}
                      />
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '15%',
                      }}
                    >
                      Plant Name
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '10%',
                      }}
                    >
                      Status
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '20%',
                      }}
                    >
                      Submission Remark
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '30%',
                      }}
                    >
                      Individual Remark
                    </TableCell>
                    <TableCell
                      align='center'
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '15%',
                      }}
                    >
                      Action
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {uniquePlants.map((plant) => (
                    <TableRow
                      key={plant.plantId}
                      sx={{
                        '&:hover': {
                          bgcolor: 'action.hover',
                        },
                        bgcolor: selectedPlants.includes(plant.plantId)
                          ? '#e3f2fd'
                          : 'inherit',
                      }}
                    >
                      <TableCell padding='checkbox'>
                        <Checkbox
                          checked={selectedPlants.includes(plant.plantId)}
                          onChange={() => handleSelectPlant(plant.plantId)}
                          disabled={
                            bulkActionLoading ||
                            isAnyIndividualActionLoading ||
                            plant.status !== 'PENDING'
                          }
                        />
                      </TableCell>
                      <TableCell>
                        <Typography
                          variant='body2'
                          fontWeight={500}
                          sx={{ ml: 1 }}
                        >
                          {plant.plantName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box
                          sx={{
                            display: 'inline-block',
                            px: 1.5,
                            py: 0.5,
                            borderRadius: 1,
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            bgcolor:
                              plant.status === 'APPROVED'
                                ? '#e8f5e9'
                                : plant.status === 'REJECTED'
                                  ? '#ffebee'
                                  : '#fff3e0',
                            color:
                              plant.status === 'APPROVED'
                                ? '#2e7d32'
                                : plant.status === 'REJECTED'
                                  ? '#d32f2f'
                                  : '#f57c00',
                          }}
                        >
                          {plant.status}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Tooltip
                          title={
                            plant.submissionRemark || 'No submission remark'
                          }
                          arrow
                          placement='top'
                        >
                          <Typography
                            variant='body2'
                            sx={{
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                              maxWidth: '200px',
                              color: plant.submissionRemark
                                ? 'text.primary'
                                : 'text.secondary',
                              fontStyle: plant.submissionRemark
                                ? 'normal'
                                : 'italic',
                            }}
                          >
                            {plant.submissionRemark || 'No submission remark'}
                          </Typography>
                        </Tooltip>
                      </TableCell>
                      <TableCell>
                        <TextField
                          fullWidth
                          size='small'
                          multiline
                          maxRows={3}
                          value={
                            plant.status !== 'PENDING'
                              ? plant.verifiedRemark
                              : remarks[plant.plantId] || ''
                          }
                          onChange={(e) =>
                            handleRemarkChange(plant.plantId, e.target.value)
                          }
                          placeholder={
                            selectedPlants.length > 1
                              ? 'Use bulk remark above'
                              : plant.status !== 'PENDING'
                                ? 'Already processed'
                                : 'Enter remark...'
                          }
                          variant='outlined'
                          disabled={
                            selectedPlants.length > 1 ||
                            isAnyIndividualActionLoading ||
                            plant.status !== 'PENDING'
                          }
                          sx={{
                            '& .MuiOutlinedInput-root': {
                              fontSize: '0.875rem',
                            },
                          }}
                        />
                      </TableCell>
                      <TableCell align='center'>
                        <Box
                          sx={{
                            display: 'flex',
                            gap: 1,
                            justifyContent: 'center',
                          }}
                        >
                          {selectedPlants.length > 1 ? (
                            <Tooltip title='View History' arrow>
                              <IconButton
                                size='small'
                                onClick={() => handleViewHistory(plant)}
                                disabled={bulkActionLoading}
                                sx={{
                                  color: '#1976d2',
                                  '&:hover': {
                                    backgroundColor: '#e3f2fd',
                                  },
                                }}
                              >
                                <HistoryIcon fontSize='small' />
                              </IconButton>
                            </Tooltip>
                          ) : (
                            <>
                              <Tooltip
                                title={
                                  plant.status !== 'PENDING'
                                    ? `Already ${plant.status.toLowerCase()}`
                                    : individualLoading[plant.plantId] ===
                                        'approve'
                                      ? 'Approving...'
                                      : 'Approve'
                                }
                                arrow
                              >
                                <span>
                                  <IconButton
                                    size='small'
                                    onClick={() =>
                                      handleApproveClick(plant.plantId)
                                    }
                                    disabled={
                                      remarks[plant.plantId] === '' ||
                                      bulkActionLoading ||
                                      isAnyIndividualActionLoading ||
                                      plant.status !== 'PENDING'
                                    }
                                    sx={{
                                      color: '#2e7d32',
                                      '&:hover': {
                                        backgroundColor: '#e8f5e9',
                                      },
                                      '&.Mui-disabled': {
                                        color: 'rgba(0, 0, 0, 0.26)',
                                      },
                                    }}
                                  >
                                    {individualLoading[plant.plantId] ===
                                    'approve' ? (
                                      <CircularProgress
                                        size={20}
                                        sx={{ color: '#2e7d32' }}
                                      />
                                    ) : (
                                      <CheckCircleIcon fontSize='small' />
                                    )}
                                  </IconButton>
                                </span>
                              </Tooltip>

                              <Tooltip
                                title={
                                  plant.status !== 'PENDING'
                                    ? `Already ${plant.status.toLowerCase()}`
                                    : individualLoading[plant.plantId] ===
                                        'reject'
                                      ? 'Rejecting...'
                                      : 'Reject'
                                }
                                arrow
                              >
                                <span>
                                  <IconButton
                                    size='small'
                                    onClick={() =>
                                      handleRejectClick(plant.plantId)
                                    }
                                    disabled={
                                      remarks[plant.plantId] === '' ||
                                      bulkActionLoading ||
                                      isAnyIndividualActionLoading ||
                                      plant.status !== 'PENDING'
                                    }
                                    sx={{
                                      color: '#d32f2f',
                                      '&:hover': {
                                        backgroundColor: '#ffebee',
                                      },
                                      '&.Mui-disabled': {
                                        color: 'rgba(0, 0, 0, 0.26)',
                                      },
                                    }}
                                  >
                                    {individualLoading[plant.plantId] ===
                                    'reject' ? (
                                      <CircularProgress
                                        size={20}
                                        sx={{ color: '#d32f2f' }}
                                      />
                                    ) : (
                                      <CancelIcon fontSize='small' />
                                    )}
                                  </IconButton>
                                </span>
                              </Tooltip>

                              <Tooltip title='View History' arrow>
                                <IconButton
                                  size='small'
                                  onClick={() => handleViewHistory(plant)}
                                  disabled={
                                    bulkActionLoading ||
                                    isAnyIndividualActionLoading
                                  }
                                  sx={{
                                    color: '#1976d2',
                                    '&:hover': {
                                      backgroundColor: '#e3f2fd',
                                    },
                                  }}
                                >
                                  <HistoryIcon fontSize='small' />
                                </IconButton>
                              </Tooltip>
                            </>
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>

        <Divider />

        <DialogActions sx={{ p: 2, gap: 1 }}>
          <Typography
            variant='body2'
            color='text.secondary'
            sx={{ flex: 1, ml: 1 }}
          >
            Total Plants: {uniquePlants.length}
            {selectedPlants.length > 0 && (
              <Typography component='span' color='primary' fontWeight={600}>
                {' '}
                | Selected: {selectedPlants.length}
              </Typography>
            )}
          </Typography>
          <Button
            onClick={handleCancel}
            variant='outlined'
            color='error'
            disabled={bulkActionLoading || isAnyIndividualActionLoading}
          >
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* History Dialog */}
      {selectedPlantHistory && (
        <HistoryDialog
          open={historyDialogOpen}
          onClose={handleCloseHistory}
          plantId={selectedPlantHistory.plantId}
          userRole={userRole}
          type='PLANT_WISE'
          timelineData={timelineData}
        />
      )}
    </>
  )
}

export default ApproveDialog
