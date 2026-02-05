import { useState } from 'react'
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
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
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
      default:
        return title
    }
  }

  const handleChange = (event) => {
    setRemark(event.target.value)
  }

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
          </Box>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default RemarkDialog
