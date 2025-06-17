import { DateTimePicker } from '@progress/kendo-react-dateinputs'

const DateTimePickerEditor = ({ dataItem, field, onChange }) => {
  const currentRaw = dataItem[field]
  const currentDate = currentRaw ? new Date(currentRaw) : null

  const handleChange = (event) => {
    onChange({
      dataItem,
      field,
      value: event.value,
      syntheticEvent: event.syntheticEvent,
    })
  }

  const fyString = localStorage.getItem('year')
  let fyMin = null
  let fyMax = null

  if (fyString) {
    const [start, end] = fyString.split('-')
    const startYear = parseInt(start, 10)
    let endYear = parseInt(end, 10)
    if (end.length === 2) endYear += 2000

    fyMin = new Date(startYear, 3, 1, 0, 0, 0)
    fyMax = new Date(endYear, 2, 31, 23, 59, 59)
  }

  const isStart = field === 'maintStartDateTime'
  const isEnd = field === 'maintEndDateTime'
  const partnerField = isStart
    ? 'maintEndDateTime'
    : isEnd
      ? 'maintStartDateTime'
      : null

  const partnerRaw = partnerField ? dataItem[partnerField] : null
  const partnerDate = partnerRaw ? new Date(partnerRaw) : null

  let dynamicMin = fyMin
  let dynamicMax = fyMax

  if (isStart && partnerDate) {
    dynamicMax = new Date(
      Math.min(fyMax?.getTime() ?? Infinity, partnerDate.getTime()),
    )
  }

  if (isEnd && partnerDate) {
    dynamicMin = new Date(
      Math.max(fyMin?.getTime() ?? 0, partnerDate.getTime()),
    )
  }

  return (
    <td>
      <DateTimePicker
        value={currentDate}
        min={dynamicMin}
        max={dynamicMax}
        format='dd-MM-yyyy hh:mm a'
        onChange={handleChange}
        width='100%'
        size='small'
        autoFill
        enableMouseWheel={false}
        steps={{ hour: 1, minute: 1, second: 0 }}
      />
    </td>
  )
}

export default DateTimePickerEditor
