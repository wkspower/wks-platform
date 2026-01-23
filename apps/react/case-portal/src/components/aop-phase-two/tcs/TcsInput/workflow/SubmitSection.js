import { Box, Button, Tooltip, CircularProgress } from '@mui/material'
import HistoryIcon from '@mui/icons-material/History'

const SubmitSection = ({
  onSubmitClick,
  onViewHistory,
  isEligible = true,
  isLoading = false,
}) => {
  const handleSubmitClick = () => {
    if (!isEligible) {
      return
    }
    if (onSubmitClick) {
      onSubmitClick()
    }
  }

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
      <Button
        className='btn-save'
        style={{ background: '#28a745', color: '#ffffff' }}
        onClick={handleSubmitClick}
        disabled={!isEligible || isLoading}
      >
        {isLoading ? <CircularProgress size={20} color='inherit' /> : 'Submit'}
      </Button>
      <Tooltip title='View History'>
        <Button
          variant='outlined'
          onClick={onViewHistory}
          disabled={isLoading}
          sx={{
            textTransform: 'none',
            borderColor: '#1976d2',
            color: '#1976d2',
            padding: '6px 16px',
            maxHeight: '1.8rem',
            '&:hover': {
              borderColor: '#1565c0',
              backgroundColor: '#e3f2fd',
            },
          }}
        >
          <HistoryIcon />
        </Button>
      </Tooltip>
    </Box>
  )
}

export default SubmitSection
