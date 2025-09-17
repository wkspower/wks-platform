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

  // Get AOP year from localStorage
  const fyString = localStorage.getItem('year')
  let fyMin = null
  let fyMax = null

  if (fyString) {
    const [start, end] = fyString.split('-')
    const startYear = parseInt(start, 10)
    let endYear = parseInt(end, 10)
    if (end.length === 2) endYear += 2000

    fyMin = new Date(startYear, 3, 1) // April 1
    fyMax = new Date(endYear, 2, 31)  // March 31
  }

  return (
    <td>
      <DatePicker
        value={currentDate}
        format='dd-MM-yyyy'
        onChange={handleChange}
        min={fyMin}
        max={fyMax}
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
