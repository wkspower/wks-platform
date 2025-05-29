import Tooltip from '@mui/material/Tooltip'
import { formatToDecimals } from './numberFormatter'

export default function FormattedNumberTooltip({ value, decimals = 5 }) {
  return (
    <Tooltip title={value?.toString() || ''} arrow>
      <span>{formatToDecimals(value, decimals)}</span>
    </Tooltip>
  )
}
