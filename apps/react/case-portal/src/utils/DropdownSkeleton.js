import React from 'react'
import Skeleton from '@mui/material/Skeleton'
import { useTheme, alpha } from '@mui/material/styles'
import PropTypes from 'prop-types'

const DropdownSkeleton = React.memo(function ReusableSkeleton({
  width = 80,
  height = 30,
  variant = 'rectangular',
  animation = 'wave',
  whiteAlpha = 0.12,
  borderRadius = 1,
  sx = {},
  ...rest
}) {
  const theme = useTheme()
  const bg =
    typeof whiteAlpha === 'string'
      ? whiteAlpha
      : alpha(theme.palette.common.white, whiteAlpha)

  return (
    <Skeleton
      variant={variant}
      width={width}
      height={height}
      animation={animation}
      sx={{
        bgcolor: bg,
        borderRadius,
        ...(animation === 'wave' && {
          '&::after': {
            backgroundImage:
              'linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.45) 50%, rgba(255,255,255,0) 100%)',
          },
        }),
        ...sx,
      }}
      {...rest}
    />
  )
})

DropdownSkeleton.propTypes = {
  width: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  height: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  variant: PropTypes.string,
  animation: PropTypes.oneOf(['pulse', 'wave', false]),
  whiteAlpha: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  borderRadius: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  sx: PropTypes.object,
}

export default DropdownSkeleton
