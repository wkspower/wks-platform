import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'

export function remarkColumn(handleRemarkCellClick) {
  return {
    field: 'body' || 'remark', // your data field
    headerName: 'Remark',
    width: 150,
    renderCell: (params) => {
      const displayText = truncateRemarks(params.value)
      // decide whether it’s editable (customize this test if needed)
      const isEditable = params.value == null || params.value === ''

      return (
        <Tooltip title={params.value || ''} arrow>
          <div
            style={{
              cursor: isEditable ? 'pointer' : 'default',
              color: params.value ? 'inherit' : 'gray',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              maxWidth: 140,
            }}
            onClick={() => isEditable && handleRemarkCellClick(params.row)}
          >
            {displayText || (isEditable ? 'Click to add remark' : '')}
          </div>
        </Tooltip>
      )
    },
  }
}
