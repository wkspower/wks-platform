import PropTypes from 'prop-types'
import { Box, Chip, Tooltip } from '@mui/material'
import CheckCircleOutlined from '@ant-design/icons/CheckCircleOutlined'

/**
 * The milestones of one stage, shown under its step.
 *
 * An achieved milestone is filled and green; one not yet reached is outlined and
 * muted. Both are always shown — the ones still outstanding are what tell someone
 * what this stage is still for, so hiding them would leave only a stage name and no
 * sense of progress within it.
 *
 * Milestone names come from the case definition, so they appear exactly as
 * configured and are not translated.
 */
const MilestoneChips = ({ milestones, achievedIds }) => {
  if (!milestones || milestones.length === 0) {
    return null
  }

  return (
    <Box
      sx={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: 0.5,
        mt: 0.5,
        justifyContent: 'center',
      }}
    >
      {milestones.map((milestone) => {
        const achieved = achievedIds?.includes(milestone.id)

        const chip = (
          <Chip
            key={milestone.id}
            size='small'
            label={milestone.name}
            color={achieved ? 'success' : 'default'}
            variant={achieved ? 'filled' : 'outlined'}
            icon={achieved ? <CheckCircleOutlined /> : undefined}
            sx={{
              height: 20,
              fontSize: '0.7rem',
              opacity: achieved ? 1 : 0.6,
              '& .MuiChip-icon': { fontSize: '0.7rem', ml: 0.5 },
            }}
          />
        )

        // The note carries over the explanation from an imported model — e.g. the
        // condition the original diagram wrote in prose beside the milestone.
        return milestone.note ? (
          <Tooltip key={milestone.id} title={milestone.note}>
            <span>{chip}</span>
          </Tooltip>
        ) : (
          chip
        )
      })}
    </Box>
  )
}

MilestoneChips.propTypes = {
  milestones: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string,
      note: PropTypes.string,
    }),
  ),
  achievedIds: PropTypes.arrayOf(PropTypes.string),
}

export default MilestoneChips
