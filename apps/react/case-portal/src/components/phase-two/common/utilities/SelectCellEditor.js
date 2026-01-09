import { DropDownList } from '@progress/kendo-react-dropdowns'
import { useState, useEffect, useRef } from 'react'

export const SelectCellEditor = ({
  dataItem,
  field,
  onChange,
  options = [],
  textField = 'text',
  valueField = 'value',
  placeholder = 'Select...',
}) => {
  const storedValue = dataItem[field] ?? null

  // Find the object that matches the stored value
  const getInitialSelection = () => {
    if (!storedValue || !options.length) return null
    return options.find((option) => option[valueField] === storedValue) || null
  }

  const [localValue, setLocalValue] = useState(getInitialSelection())
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    const selectedItem = e.target.value
    setLocalValue(selectedItem)
  }

  // Debounced sync to grid, but skip first render
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      // Extract the value to send
      let valueToSend = localValue
      if (
        localValue &&
        typeof localValue === 'object' &&
        localValue[valueField] !== undefined
      ) {
        valueToSend = localValue[valueField]
      }

      // Only send if the value actually changed
      if (valueToSend !== storedValue) {
        onChange({ dataItem, field, value: valueToSend })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, storedValue, valueField])

  return (
    <td style={{ textAlign: 'start' }}>
      <DropDownList
        value={localValue}
        onChange={handleChange}
        data={options}
        textField={textField}
        valueField={valueField}
        placeholder={placeholder}
        style={{
          fontSize: '0.8rem',
          height: '22px',
          width: '100%',
        }}
      />
    </td>
  )
}
