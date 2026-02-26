import { DateTimePicker } from '@progress/kendo-react-dateinputs'
import { useState, useEffect } from 'react'

const isValidDate = (d) => d instanceof Date && !isNaN(d)

const DatePickerNoLimit = ({ dataItem, field, onChange, min, max }) => {
  const initialValue = dataItem[field] ? new Date(dataItem[field]) : null
  const [localValue, setLocalValue] = useState(initialValue)

  useEffect(() => {
    setLocalValue(initialValue)
  }, [dataItem.id])

  const isStart = field === 'maintStartDateTime'
  const isEnd = field === 'maintEndDateTime'

  const partnerField = isStart
    ? 'maintEndDateTime'
    : isEnd
      ? 'maintStartDateTime'
      : null

  const partnerDate =
    partnerField && dataItem[partnerField]
      ? new Date(dataItem[partnerField])
      : null

  let pickerMin = isValidDate(min) ? min : undefined
  let pickerMax = isValidDate(max) ? max : undefined

  if (isStart && isValidDate(partnerDate)) {
    pickerMax = partnerDate
  }

  if (isEnd && isValidDate(partnerDate)) {
    pickerMin = partnerDate
  }

  return (
    <td>
      <DateTimePicker
        value={localValue}
        format='dd-MM-yyyy hh:mm a'
        width='100%'
        size='small'
        min={pickerMin}
        max={pickerMax}
        onChange={(event) => {
          setLocalValue(event.value)
        }}
        onBlur={() => {
          onChange({
            dataItem,
            field,
            value: localValue,
          })
        }}
      />
    </td>
  )
}

export default DatePickerNoLimit
