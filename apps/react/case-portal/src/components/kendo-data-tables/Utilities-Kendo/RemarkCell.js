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

  // Ensure we always work with an array
  const redCells = Array.isArray(allRedCell) ? allRedCell : []

  const isRed = redCells.some(
    (cell) =>
      cell.month === month &&
      cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
  )

  // Whitelist only valid HTML <td> attributes from tdProps
  const {
    className,
    colSpan,
    rowSpan,
    headers,
    style: extraStyle,
    id,
    role,
    'data-testid': dataTestId,
  } = tdProps || {}

  return (
    <td
      className={className}
      colSpan={colSpan}
      rowSpan={rowSpan}
      headers={headers}
      id={id}
      role={role}
      data-testid={dataTestId}
      title={rawValue || 'Add remark'}
      // title={rawValue}
      style={{
        cursor: 'pointer',
        color: isRed ? 'orange' : rawValue ? 'inherit' : 'gray',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        ...extraStyle,
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
      {displayText || 'Add remark'}
      {/* {displayText} */}
    </td>
  )
}
