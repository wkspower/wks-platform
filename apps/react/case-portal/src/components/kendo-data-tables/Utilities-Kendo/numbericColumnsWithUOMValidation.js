import { Input } from '@progress/kendo-react-inputs'
import { useState, useEffect, useRef } from 'react'

export const NoSpinnerNumericEditorWithUOMValidation = ({
  dataItem,
  field,
  onChange,
}) => {
  const initialValue = dataItem[field] ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    let val = e.target.value

    // Allow only numeric (including decimal)
    if (val === '' || /^\d*(\.\d*)?$/.test(val)) {
      // If UOM is '%', enforce range 1–100
      if (dataItem?.uom === '%' || dataItem?.unit === '%') {
        const num = parseFloat(val)
        if (val === '' || (num >= 0 && num <= 100)) {
          setLocalValue(val)
        }
      } else {
        setLocalValue(val)
      }
    }
  }

  // Debounced sync to grid, skip first render
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
