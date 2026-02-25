import { DatePicker } from '@progress/kendo-react-dateinputs'
import { useRef, useEffect } from 'react'

const DateOnlyPicker = ({ dataItem, field, onChange }) => {
  const pickerRef = useRef(null)

  useEffect(() => {
    if (pickerRef.current) {
      const el = pickerRef.current.element || pickerRef.current
      if (el && typeof el.focus === 'function') el.focus()
    }
  }, [])
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
        ref={pickerRef}
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
