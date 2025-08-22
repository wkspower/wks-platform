import { DatePicker } from '@progress/kendo-react-dateinputs'

const DateOnlyPicker = ({ dataItem, field, onChange }) => {
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

  return (
    <td>
      <DatePicker
        value={currentDate}
        format='dd-MM-yyyy'
        onChange={handleChange}
        width='100%'
        size='small'
        style={{
          width: '100%',
          fontSize: '0.75rem',
          height: '28px',
        }}
      />
    </td>
  )
}

export default DateOnlyPicker
