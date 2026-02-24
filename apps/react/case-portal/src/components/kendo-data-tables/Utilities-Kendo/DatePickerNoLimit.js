import { DateTimePicker } from '@progress/kendo-react-dateinputs'

const DatePickerNoLimit = ({ dataItem, field, onChange }) => {
  const currentRaw = dataItem[field]
  const currentDate = currentRaw ? new Date(currentRaw) : null

  // Determine if editing start or end
  const isStart = field === 'maintStartDateTime'
  const isEnd = field === 'maintEndDateTime'
  const partnerField = isStart
    ? 'maintEndDateTime'
    : isEnd
      ? 'maintStartDateTime'
      : null

  const partnerRaw = partnerField ? dataItem[partnerField] : null
  const partnerDate = partnerRaw ? new Date(partnerRaw) : null

  const pickerProps = {
    value: currentDate,
    format: 'dd-MM-yyyy hh:mm a',
    onChange: (event) => {
      onChange({
        dataItem,
        field,
        value: event.value,
        syntheticEvent: event.syntheticEvent,
      })
    },
    width: '100%',
    size: 'small',
    autoFill: true,
    enableMouseWheel: false,
    steps: { hour: 1, minute: 1, second: 0 },
  }

  // Only set min/max if partnerDate is valid
  if (isStart && partnerDate && !isNaN(partnerDate)) {
    pickerProps.max = partnerDate
  }
  if (isEnd && partnerDate && !isNaN(partnerDate)) {
    pickerProps.min = partnerDate
  }

  return (
    <td>
      <DateTimePicker {...pickerProps} />
    </td>
  )
}

export default DatePickerNoLimit