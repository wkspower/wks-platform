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
  const dropdownRef = useRef(null)

  useEffect(() => {
    // Delay focus slightly to prevent interference with onChange
    const timer = setTimeout(() => {
      if (dropdownRef.current) {
        const el = dropdownRef.current.element || dropdownRef.current
        if (el && typeof el.focus === 'function') el.focus()
      }
    }, 50)
    return () => clearTimeout(timer)
  }, [])

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
        ref={dropdownRef}
        value={localValue}
        onChange={handleChange}
        data={options}
        textField={textField}
        valueField={valueField}
        placeholder={placeholder}
        style={{
          fontSize: '1rem',
          fontWeight: 'normal',
          height: '2rem',
          width: '100%',
          backgroundColor: 'lightGrey',
        }}
      />
    </td>
  )
}
