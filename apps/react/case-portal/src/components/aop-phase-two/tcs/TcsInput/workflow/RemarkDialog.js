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
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import { TextArea } from '@progress/kendo-react-inputs'

const RemarkDialog = ({
  open,
  handleClose,
  title = '',
  placeholder = 'Enter your remarks here...',
  onSubmit,
  disabled = false,
  maxLength = 1000,
}) => {
  const [remark, setRemark] = useState('')
  const handleSubmit = () => {
    if (remark.trim()) {
      onSubmit(remark)
      setRemark('')
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
              {title}
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
              disabled={disabled || !remark.trim()}
              variant='contained'
              className='btn-save'
              style={{ background: '#28a745', color: '#ffffff' }}
            >
              Submit
            </Button>
          </Box>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default RemarkDialog
