import { monthMap } from '../index'

export const RemarkCell = ({
  dataItem,
  field,
  onRemarkClick,
  allRedCell,
  setEdit,
  ...tdProps
}) => {
  const rawValue = dataItem[field]
  const displayText = String(rawValue ?? '')

  const month = monthMap[field.toLowerCase()]
  const normId = dataItem.materialFkId

  // Ensure we always work with an array:
  const redCells = Array.isArray(allRedCell) ? allRedCell : []

  const isRed = redCells.some(
    (cell) =>
      cell.month === month &&
      cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
  )

  return (
    <td
      {...tdProps}
      title={rawValue || 'Click to add remark'}
      style={{
        cursor: 'pointer',
        color: isRed ? 'orange' : rawValue ? 'inherit' : 'gray',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
      }}
      onClick={(e) => {
        e.preventDefault()
        e.stopPropagation()
      }}
      onDoubleClick={(e) => {
        e.preventDefault()
        e.stopPropagation()
        onRemarkClick(dataItem)
        setEdit?.({})
      }}
    >
      {displayText || 'Click to add remark'}
    </td>
  )
}
