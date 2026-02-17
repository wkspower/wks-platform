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
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import HistoryIcon from '@mui/icons-material/History'
import HistoryDialog from './HistoryDialog'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { TCS_TABS } from '../../constants/tcsTabConstants'

const ApproveDialog = ({ open, onClose, tab, year, userRole }) => {
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
    }))
  }, [entries])

  useEffect(() => {
    if (open) {
      const initialRemarks = {}
      uniquePlants.forEach((plant) => {
        initialRemarks[plant.plantId] = '' // Start with empty remark for EPS Engineer to fill
      })
      setRemarks(initialRemarks)
    }
  }, [open, uniquePlants])

  const handleRemarkChange = (plantId, value) => {
    setRemarks((prev) => ({
      ...prev,
      [plantId]: value,
    }))
  }

  const handleApproveClick = async (plantId) => {
    try {
      const remark = remarks[plantId] || ''
      const plant = uniquePlants.find((p) => p.plantId === plantId)
      const plantName = plant?.plantName || ''

      await TcsWorkflowApiService.ebsApproveReject(
        keycloak,
        plantId,
        SITE_ID,
        VERTICAL_ID,
        true, // approvalStatus = true for approve
        tab, // Use tab from props
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

      setError(null)
    } catch (err) {
      console.error('Error approving plant:', err)
      setError('Failed to approve plant. Please try again.')
    }
  }

  const handleRejectClick = async (plantId) => {
    try {
      const remark = remarks[plantId] || ''
      const plant = uniquePlants.find((p) => p.plantId === plantId)
      const plantName = plant?.plantName || ''

      await TcsWorkflowApiService.ebsApproveReject(
        keycloak,
        plantId,
        SITE_ID,
        VERTICAL_ID,
        false, // approvalStatus = false for reject
        tab, // Use tab from props
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

      setError(null)
    } catch (err) {
      console.error('Error rejecting plant:', err)
      setError('Failed to reject plant. Please try again.')
    }
  }

  const handleCancel = () => {
    setRemarks({})
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
          <Typography variant='h6' component='div' fontWeight='600'>
            Approve/Reject Plants
          </Typography>
          <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
            Review and take action on each plant
          </Typography>
        </DialogTitle>

        <Divider />

        <DialogContent sx={{ p: 0 }}>
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
          ) : error ? (
            <Box
              sx={{
                textAlign: 'center',
                py: 6,
                px: 3,
              }}
            >
              <Typography variant='body2' color='error'>
                {error}
              </Typography>
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
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '20%',
                      }}
                    >
                      Plant Name
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '25%',
                      }}
                    >
                      Submission Remark
                    </TableCell>
                    <TableCell
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '35%',
                      }}
                    >
                      Remark
                    </TableCell>
                    <TableCell
                      align='center'
                      sx={{
                        fontWeight: 600,
                        bgcolor: '#f5f5f5',
                        borderBottom: '2px solid #e0e0e0',
                        width: '20%',
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
                      }}
                    >
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
                          value={remarks[plant.plantId] || ''}
                          onChange={(e) =>
                            handleRemarkChange(plant.plantId, e.target.value)
                          }
                          placeholder='Enter remark...'
                          variant='outlined'
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
                          <Tooltip title='Approve' arrow>
                            <span>
                              <IconButton
                                size='small'
                                onClick={() =>
                                  handleApproveClick(plant.plantId)
                                }
                                disabled={remarks[plant.plantId] === ''}
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
                                <CheckCircleIcon fontSize='small' />
                              </IconButton>
                            </span>
                          </Tooltip>

                          <Tooltip title='Reject' arrow>
                            <span>
                              <IconButton
                                size='small'
                                onClick={() => handleRejectClick(plant.plantId)}
                                disabled={remarks[plant.plantId] === ''}
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
                                <CancelIcon fontSize='small' />
                              </IconButton>
                            </span>
                          </Tooltip>

                          <Tooltip title='View History' arrow>
                            <IconButton
                              size='small'
                              onClick={() => handleViewHistory(plant)}
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
          </Typography>
          <Button onClick={handleCancel} variant='outlined' color='error'>
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
          tab={tab}
        />
      )}
    </>
  )
}

export default ApproveDialog
