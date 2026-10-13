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
  const parseToDate = (raw) => {
    if (!raw) return null
    if (raw instanceof Date) {
      if (isNaN(raw.getTime())) return null
      return new Date(Date.UTC(raw.getFullYear(), raw.getMonth(), raw.getDate(), 0, 0, 0))
    }
    if (typeof raw === 'string') {
      const trimmed = raw.trim()
      // Match DD-MM-YYYY or DD/MM/YYYY
      const ddmmyyyy = trimmed.match(/^(\d{1,2})[-/](\d{1,2})[-/](\d{4})/)
      if (ddmmyyyy) {
        const day = parseInt(ddmmyyyy[1], 10)
        const month = parseInt(ddmmyyyy[2], 10) - 1
        const year = parseInt(ddmmyyyy[3], 10)
        return new Date(Date.UTC(year, month, day, 0, 0, 0))
      }
      // Match YYYY-MM-DD
      const yyyymmdd = trimmed.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/)
      if (yyyymmdd) {
        const year = parseInt(yyyymmdd[1], 10)
        const month = parseInt(yyyymmdd[2], 10) - 1
        const day = parseInt(yyyymmdd[3], 10)
        return new Date(Date.UTC(year, month, day, 0, 0, 0))
      }
      const d = new Date(trimmed)
      if (!isNaN(d.getTime())) {
        return new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0))
      }
    }
    return null
  }

  const currentRaw = dataItem[field]
  const currentDate = parseToDate(currentRaw)

  const handleChange = (event) => {
    let val = event.value
    if (val instanceof Date && !isNaN(val.getTime())) {
      val = new Date(Date.UTC(val.getFullYear(), val.getMonth(), val.getDate(), 0, 0, 0))
    }
    onChange({
      dataItem,
      field,
      value: val,
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
        className='input-editor'
        style={{
          width: '100%',
          fontSize: '15px',
          height: '40px',
          fontFamily: "'Honeywell Sans Web', 'Inter', sans-serif !important",
        }}
      />
    </td>
  )
}

export default DateOnlyPicker
