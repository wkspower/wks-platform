import { Box, Button, Tooltip, CircularProgress } from '@mui/material'
import HistoryIcon from '@mui/icons-material/History'
import RateReviewIcon from '@mui/icons-material/RateReview'
import { ROLES } from '../../utils/roleUtils'

const SubmitSection = ({
  onSubmitClick,
  onViewHistory,
  onReviewClick,
  isEligible = true,
  isLoading = false,
  isWorkflowTriggered = true,
  submitTooltip = null,
  showReviewBtn = false,
  reviewTooltip = 'Review and approve/reject plants',
  userRole = '',
}) => {
  const handleSubmitClick = () => {
    if (!isEligible) {
      return
    }
    if (onSubmitClick) {
      onSubmitClick()
    }
  }

  // Role-based tooltip messages
  const getRoleBasedTooltip = () => {
    if (!isEligible) {
      return 'Plant submission already done.'
    }

    switch (userRole) {
      case ROLES.PLANT_MANAGER:
        return 'Submit data to EPS Engineer'
      case ROLES.EPS_ENGINEER:
        return 'Submit approval to CTS Head / EPS Head'
      case ROLES.CTS_HEAD:
      case ROLES.EPS_HEAD:
        return 'Submit approval to Cluster Head'
      case ROLES.CLUSTER_HEAD:
        return 'Finalise data for PIMS Output'
      default:
        return 'Submit'
    }
  }

  const defaultTooltip = getRoleBasedTooltip()
  const tooltipTitle = submitTooltip !== null ? submitTooltip : defaultTooltip

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
      {showReviewBtn && onReviewClick && (
        <Tooltip position='top' title={reviewTooltip}>
          <Button
            variant='contained'
            onClick={onReviewClick}
            disabled={isLoading}
            className='btn-save'
          >
            <RateReviewIcon sx={{ mr: 0.5, fontSize: '1rem' }} />
            Review
          </Button>
        </Tooltip>
      )}
      <Tooltip position='top' title={tooltipTitle}>
        <span>
          <Button
            className='btn-save'
            style={{
              background: isEligible ? '#28a745' : '#6c757d',
              color: '#ffffff',
              opacity: isLoading ? 0.6 : 1,
            }}
            onClick={handleSubmitClick}
            disabled={!isEligible || isLoading}
          >
            {isLoading ? (
              <CircularProgress size={20} color='inherit' />
            ) : (
              'Submit'
            )}
          </Button>
        </span>
      </Tooltip>

      <Tooltip position='top' title='View History'>
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
