import { Input } from '@progress/kendo-react-inputs'
import { monthMap } from '../index'

export const DurationEditor = (props) => {
  const { dataItem, field, onChange } = props
  const raw = dataItem[field] ?? ''

  const handleChange = (e) => {
    const v = e.target.value
    if (/^(\d+)?(\.\d{0,2})?$/.test(v)) {
      // if (/^(\d{0,2})?(\.\d{0,2})?$/.test(v)) {
      const parts = v.split('.')
      if (parts.length === 2) {
        const mins = parseInt(parts[1].padEnd(2, '0'), 10)
        if (mins >= 60) return
      }

      onChange({ dataItem, field, value: v })
    }
  }

  return <Input value={raw} onChange={handleChange} placeholder='HH:MM' />
}
export const DurationDisplayWithTooltipCell = (props) => {
  const value = props?.dataItem[props.field]
  let display = value
  if (value && !isNaN(value)) {
    const [hoursStr, minsStr = '0'] = value.toString().split('.')
    const hours = parseInt(hoursStr, 10)
    const mins = parseInt(minsStr.padEnd(2, '0'), 10)
    display = `${hours.toString().padStart(2, '0')}:${mins
      .toString()
      .padStart(2, '0')}`
  }
  const month = monthMap[props.field?.toLowerCase()]
  const normId = props.dataItem.materialFkId
  const isRedFromAllRedCell = props?.allRedCell?.some(
    (cell) =>
      cell.month === month &&
      cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
  )
  const isRed = isRedFromAllRedCell
  return (
    <td
      {...props.tdProps}
      title={display}
      style={{
        color: isRed ? 'orange' : undefined,
      }}
    >
      {display}
    </td>
  )
}
