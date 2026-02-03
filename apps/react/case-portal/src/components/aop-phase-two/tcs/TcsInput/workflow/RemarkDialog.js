import { useState, useEffect } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  IconButton,
  Divider,
  CircularProgress,
  Paper,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import { TextArea } from '@progress/kendo-react-inputs'
import { useSelector } from 'react-redux'
import { TcsWorkflowApiService } from 'components/aop-phase-two/services/tcs/tcsWorkflowApiService'
import { ROLES } from '../../utils/roleUtils'

const RemarkDialog = ({
  open,
  handleClose,
  title = '',
  placeholder = 'Enter your remarks here...',
  onSubmit,
  onApprove,
  onReject,
  disabled = false,
  maxLength = 1000,
  role = '',
  historyData = [],
  keycloak,
  snackbarData,
  setSnackbarData,
  setSnackbarOpen,
}) => {
  const [remark, setRemark] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [previousLevelData, setPreviousLevelData] = useState(null)
  const [loadingPreviousData, setLoadingPreviousData] = useState(false)

  // Get values from Redux store
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const handleSubmit = async () => {
    if (remark.trim()) {
      setIsSubmitting(true)
      try {
        // Submit the remark (workflow trigger is handled in parent component)
        onSubmit(remark)
        setRemark('')
        handleClose()
      } catch (err) {
        console.error('Error during remark submission:', err)
      } finally {
        setIsSubmitting(false)
      }
    }
  }

  const handleApprove = async () => {
    if (remark.trim()) {
      setIsSubmitting(true)
      try {
        if (onApprove) {
          await onApprove(remark)
        }
        setRemark('')
        handleClose()
      } catch (err) {
        console.error('Error during approval:', err)
      } finally {
        setIsSubmitting(false)
      }
    }
  }

  const handleReject = async () => {
    if (remark.trim()) {
      setIsSubmitting(true)
      try {
        if (onReject) {
          await onReject(remark)
        }
        setRemark('')
        handleClose()
      } catch (err) {
        console.error('Error during rejection:', err)
      } finally {
        setIsSubmitting(false)
      }
    }
  }

  const getTitle = () => {
    switch (role) {
      case ROLES.PLANT_MANAGER:
        return 'Plant Manager Remark Submission'
      case ROLES.EPS_ENGINEER:
        return 'EPS Engineer Remark Submission'
      case ROLES.CTS_HEAD:
        return 'CTS Head Remark Submission'
      case ROLES.EPS_HEAD:
        return 'EPS Head Remark Submission'
      case ROLES.CLUSTER_HEAD:
        return 'Cluster Head Remark Submission'
      default:
        return title
    }
  }

  const handleChange = (event) => {
    setRemark(event.target.value)
  }

  // Fetch previous level submission data for CTS/EPS Head and Cluster Head
  useEffect(() => {
    const fetchPreviousLevelData = async () => {
      if (!open || !keycloak || !SITE_ID || !VERTICAL_ID) {
        return
      }

      // Only fetch for CTS Head, EPS Head, and Cluster Head
      if (
        role !== ROLES.CTS_HEAD &&
        role !== ROLES.EPS_HEAD &&
        role !== ROLES.CLUSTER_HEAD
      ) {
        return
      }

      setLoadingPreviousData(true)
      try {
        let response
        if (role === ROLES.CTS_HEAD || role === ROLES.EPS_HEAD) {
          // CTS/EPS Head gets EPS Engineer approve/reject remark
          response =
            await TcsWorkflowApiService.getCtsHeadApproveRejectAuditTrail(
              keycloak,
              SITE_ID,
              VERTICAL_ID,
            )
        } else if (role === ROLES.CLUSTER_HEAD) {
          // Cluster Head gets CTS/EPS Head approve/reject remark
          response =
            await TcsWorkflowApiService.getClusterHeadApproveRejectAuditTrail(
              keycloak,
              SITE_ID,
              VERTICAL_ID,
            )
        }

        console.log('RemarkDialog - API Response:', response)

        // Response is a single object, not an array
        if (response && response.submittedBy) {
          setPreviousLevelData({
            submittedBy: response.submittedBy,
            submissionDateTime: response.submissionDateTime,
            submissionRemark: response.submissionRemark,
          })
          console.log('RemarkDialog - Previous level data set:', response)
        } else {
          console.log('RemarkDialog - No previous level data available')
          setPreviousLevelData(null)
        }
      } catch (err) {
        console.error('Error fetching previous level data:', err)
        setPreviousLevelData(null)
      } finally {
        setLoadingPreviousData(false)
      }
    }

    fetchPreviousLevelData()
  }, [open, keycloak, SITE_ID, VERTICAL_ID, role])

  return (
    <>
      <Dialog
        open={open}
        onClose={handleClose}
        maxWidth='sm'
        fullWidth
        PaperProps={{
          sx: {
            minHeight: '300px',
          },
        }}
      >
        <DialogTitle>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <Typography variant='h6' component='div' fontWeight='600'>
              {getTitle()}
            </Typography>
            <IconButton
              onClick={handleClose}
              size='small'
              sx={{
                color: 'text.secondary',
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              }}
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <Divider />

        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Previous Level Submission Section */}
            {(role === ROLES.CTS_HEAD ||
              role === ROLES.EPS_HEAD ||
              role === ROLES.CLUSTER_HEAD) && (
              <Paper
                elevation={0}
                sx={{
                  p: 2,
                  bgcolor: '#f5f5f5',
                  border: '1px solid #e0e0e0',
                  borderRadius: 1,
                }}
              >
                <Typography
                  variant='subtitle2'
                  fontWeight={600}
                  sx={{ mb: 1.5, color: '#1976d2' }}
                >
                  {role === ROLES.CTS_HEAD || role === ROLES.EPS_HEAD
                    ? 'EPS Engineer Submission'
                    : 'CTS/EPS Head Submission'}
                </Typography>
                {loadingPreviousData ? (
                  <Box
                    sx={{ display: 'flex', justifyContent: 'center', py: 2 }}
                  >
                    <CircularProgress size={24} />
                  </Box>
                ) : previousLevelData ? (
                  <Box
                    sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}
                  >
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <Typography
                        variant='body2'
                        sx={{ fontWeight: 600, minWidth: '120px' }}
                      >
                        Submitted By:
                      </Typography>
                      <Typography
                        variant='body2'
                        sx={{ color: 'text.secondary' }}
                      >
                        {previousLevelData.submittedBy || '-'}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <Typography
                        variant='body2'
                        sx={{ fontWeight: 600, minWidth: '120px' }}
                      >
                        Submission Date:
                      </Typography>
                      <Typography
                        variant='body2'
                        sx={{ color: 'text.secondary' }}
                      >
                        {previousLevelData.submissionDateTime || '-'}
                      </Typography>
                    </Box>
                    <Box
                      sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}
                    >
                      <Typography
                        variant='body2'
                        sx={{ fontWeight: 600, minWidth: '120px' }}
                      >
                        Remark:
                      </Typography>
                      <Typography
                        variant='body2'
                        sx={{
                          color: 'text.secondary',
                          flex: 1,
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'break-word',
                        }}
                      >
                        {previousLevelData.submissionRemark || '-'}
                      </Typography>
                    </Box>
                  </Box>
                ) : (
                  <Typography
                    variant='body2'
                    color='text.secondary'
                    sx={{ fontStyle: 'italic' }}
                  >
                    No previous submission data available
                  </Typography>
                )}
              </Paper>
            )}

            <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
              <Box sx={{ flex: 1 }}>
                <Typography variant='h5' sx={{ mb: 1 }}>
                  Remark :
                </Typography>
                <TextArea
                  rows={4}
                  placeholder={placeholder}
                  value={remark}
                  onChange={handleChange}
                  disabled={disabled}
                  maxLength={maxLength}
                  style={{
                    width: '100%',
                    fontSize: '0.875rem',
                    minHeight: '80px',
                    resize: 'vertical',
                  }}
                />
                <Typography
                  variant='caption'
                  color='text.secondary'
                  sx={{ display: 'block', mt: 0.5, textAlign: 'right' }}
                >
                  {remark.length}/{maxLength}
                </Typography>
              </Box>
            </Box>
          </Box>
        </DialogContent>

        <Divider />

        <DialogActions sx={{ p: 2, justifyContent: 'flex-end' }}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            {role === ROLES.CTS_HEAD ||
            role === ROLES.EPS_HEAD ||
            role === ROLES.CLUSTER_HEAD ? (
              <>
                <Button
                  variant='contained'
                  size='small'
                  startIcon={
                    isSubmitting ? (
                      <CircularProgress size={16} color='inherit' />
                    ) : (
                      <CheckCircleIcon />
                    )
                  }
                  onClick={handleApprove}
                  disabled={disabled || !remark.trim() || isSubmitting}
                  sx={{
                    bgcolor: '#2e7d32',
                    '&:hover': { bgcolor: '#1b5e20' },
                    textTransform: 'none',
                  }}
                >
                  {isSubmitting ? 'Approving...' : 'Approve'}
                </Button>
                <Button
                  variant='contained'
                  size='small'
                  startIcon={
                    isSubmitting ? (
                      <CircularProgress size={16} color='inherit' />
                    ) : (
                      <CancelIcon />
                    )
                  }
                  onClick={handleReject}
                  disabled={disabled || !remark.trim() || isSubmitting}
                  sx={{
                    bgcolor: '#d32f2f',
                    '&:hover': { bgcolor: '#c62828' },
                    textTransform: 'none',
                  }}
                >
                  {isSubmitting ? 'Rejecting...' : 'Reject'}
                </Button>
              </>
            ) : (
              <Button
                onClick={handleSubmit}
                disabled={disabled || !remark.trim() || isSubmitting}
                variant='contained'
                className='btn-save'
                style={{ background: '#28a745', color: '#ffffff' }}
              >
                {isSubmitting ? (
                  <CircularProgress size={20} color='inherit' />
                ) : (
                  'Submit'
                )}
              </Button>
            )}
          </Box>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default RemarkDialog
