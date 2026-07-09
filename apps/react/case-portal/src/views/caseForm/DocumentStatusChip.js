import Chip from '@mui/material/Chip'

const STATUS_COLORS = {
  received: 'info',
  verified: 'success',
  rejected: 'error',
  pending: 'warning',
}

function DocumentStatusChip({ status }) {
  const value = status || 'received'
  const color = STATUS_COLORS[value] || 'default'
  const label = value.charAt(0).toUpperCase() + value.slice(1)

  return <Chip size='small' color={color} label={label} />
}

export default DocumentStatusChip
