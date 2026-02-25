import { DropDownList } from '@progress/kendo-react-dropdowns'
import { Input } from '@progress/kendo-react-inputs'
import { useState, useEffect, useRef } from 'react'

export const ConditionalCellEditor = ({
  dataItem,
  field,
  onChange,
  format = '{0:0.00}',
}) => {
  const storedValue = dataItem[field] ?? ''
  const inputType = dataItem.inputType
  const options = dataItem.options || []

  // For dropdown type
  const getInitialSelection = () => {
    if (!storedValue || !options.length) return null
    const match = options.find((opt) => opt === storedValue)
    return match ? { label: match, value: match } : null
  }

  const [localValue, setLocalValue] = useState(
    inputType === 'dropdown' ? getInitialSelection() : storedValue,
  )
  const isFirstRender = useRef(true)
  const focusRef = useRef(null)

  useEffect(() => {
    if (focusRef.current) {
      const el = focusRef.current.element || focusRef.current
      if (el && typeof el.focus === 'function') el.focus()
    }
  }, [])

  const handleDropdownChange = (e) => {
    const selectedItem = e.target.value
    setLocalValue(selectedItem)
  }

  const handleNumericChange = (e) => {
    const val = e.target.value
    // Allow empty string, numbers, and decimal points
    if (val === '' || /^-?\d*(\.\d*)?$/.test(val)) {
      setLocalValue(val)
    }
  }

  const handleNumericBlur = () => {
    // Only send if the value actually changed
    if (localValue !== storedValue) {
      onChange({ dataItem, field, value: localValue })
    }
  }

  // Debounced sync for dropdown only
  useEffect(() => {
    if (inputType !== 'dropdown') return

    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      let valueToSend = localValue

      // For dropdown, extract the value
      if (localValue && typeof localValue === 'object') {
        valueToSend = localValue.value
      }

      // Only send if the value actually changed
      if (valueToSend !== storedValue) {
        onChange({ dataItem, field, value: valueToSend })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, storedValue, inputType])

  // Render dropdown for dropdown type
  if (inputType === 'dropdown' && options.length > 0) {
    const dropdownOptions = options.map((opt) => ({ label: opt, value: opt }))

    return (
      <td style={{ textAlign: 'start' }}>
        <DropDownList
          ref={focusRef}
          value={localValue}
          onChange={handleDropdownChange}
          data={dropdownOptions}
          textField='label'
          valueField='value'
          placeholder='Select...'
          style={{
            fontSize: '0.8rem',
            fontWeight: 'normal',
            height: '1.5rem',
            width: '100%',
            backgroundColor: '#f2f2f2',
          }}
        />
      </td>
    )
  }

  // Render numeric input for other types - matching NoSpinnerNumericEditor style
  return (
    <td>
      <Input
        ref={focusRef}
        value={localValue}
        onChange={handleNumericChange}
        onBlur={handleNumericBlur}
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
