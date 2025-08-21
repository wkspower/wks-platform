import { Input } from '@progress/kendo-react-inputs'
import { useState, useEffect, useRef } from 'react'

export const NoSpinnerNumericEditor = ({ dataItem, field, onChange }) => {
  const initialValue = dataItem[field] ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    const val = e.target.value
    if (val === '' || /^\d*(\.\d*)?$/.test(val)) {
      setLocalValue(val) // only update local state
    }
  }

  // Debounced sync to grid, but skip first render
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      if (localValue !== initialValue) {
        onChange({ dataItem, field, value: localValue })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, initialValue])

  return (
    <td style={{ textAlign: 'end' }}>
      <Input
        value={localValue}
        onChange={handleChange}
        style={{
          fontSize: '0.8rem',
          padding: '2px 2px',
          height: '22px',
          lineHeight: '1rem',
        }}
      />
    </td>
  )
}
