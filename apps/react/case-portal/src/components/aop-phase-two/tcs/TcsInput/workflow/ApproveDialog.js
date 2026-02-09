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
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import HistoryIcon from '@mui/icons-material/History'
import HistoryDialog from './HistoryDialog'

const ApproveDialog = ({
  open,
  onClose,
  onApprove,
  onReject,
  entries = [],
}) => {
  const [remarks, setRemarks] = useState({})
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false)
  const [selectedPlantHistory, setSelectedPlantHistory] = useState(null)

  // Get unique plants with remark field
  const uniquePlants = useMemo(() => {
    const plantMap = new Map()
    entries.forEach((entry) => {
      if (entry.plantId) {
        const plantName =
          entry.plantName ||
          entry.label ||
          entry.name ||
          `Plant ${entry.plantId}`
        const remark = entry.remark || entry.remarks || entry.description || ''
        const submissionHistory = entry.submissionHistory || []
        const latestRemark =
          submissionHistory.length > 0 ? submissionHistory[0].remarks || '' : ''

        plantMap.set(entry.plantId, {
          plantId: entry.plantId,
          plantName: plantName,
          remark: remark,
          submissionHistory: submissionHistory,
          latestSubmissionRemark: latestRemark,
        })
      }
    })
    return Array.from(plantMap.values())
  }, [entries])

  useEffect(() => {
    if (open) {
      const initialRemarks = {}
      uniquePlants.forEach((plant) => {
        initialRemarks[plant.plantId] = plant.remark
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

  const handleApproveClick = (plantId) => {
    if (onApprove) {
      onApprove(plantId, remarks[plantId])
    }
  }

  const handleRejectClick = (plantId) => {
    if (onReject) {
      onReject(plantId, remarks[plantId])
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
          {uniquePlants.length === 0 ? (
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
                            plant.latestSubmissionRemark ||
                            'No submission remark'
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
                              color: plant.latestSubmissionRemark
                                ? 'text.primary'
                                : 'text.secondary',
                              fontStyle: plant.latestSubmissionRemark
                                ? 'normal'
                                : 'italic',
                            }}
                          >
                            {plant.latestSubmissionRemark ||
                              'No submission remark'}
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

                          <Tooltip
                            title={`View History (${plant.submissionHistory?.length || 0} records)`}
                            arrow
                          >
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
          title={`Submission History - ${selectedPlantHistory.plantName}`}
          data={selectedPlantHistory.submissionHistory || []}
        />
      )}
    </>
  )
}

export default ApproveDialog
