import { useState, useEffect, useRef } from 'react'

export const BooleanCellEditor = ({
  dataItem,
  field,
  onChange,
  trueLabel = 'Yes',
  falseLabel = 'No',
  useCheckbox = true,
}) => {
  const storedValue = dataItem[field] ?? false
  const [localValue, setLocalValue] = useState(storedValue)
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    const newValue = e.target.checked
    setLocalValue(newValue)
  }

  const handleToggleClick = () => {
    setLocalValue(!localValue)
  }

  // Debounced sync to grid, but skip first render
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      // Only send if the value actually changed
      if (localValue !== storedValue) {
        onChange({ dataItem, field, value: localValue })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, storedValue])

  if (useCheckbox) {
    return (
      <td style={{ textAlign: 'center', padding: '6px 2px' }}>
        <input
          type='checkbox'
          checked={localValue}
          onChange={handleChange}
          style={{
            cursor: 'pointer',
            width: '18px',
            height: '18px',
            accentColor: '#1976d2',
          }}
        />
      </td>
    )
  }

  // Alternative: Toggle button style
  return (
    <td style={{ textAlign: 'center', padding: '2px' }}>
      <button
        onClick={handleToggleClick}
        style={{
          padding: '4px 12px',
          fontSize: '0.75rem',
          fontWeight: '500',
          backgroundColor: localValue ? '#4CAF50' : '#f44336',
          color: 'white',
          border: 'none',
          borderRadius: '3px',
          cursor: 'pointer',
          minWidth: '70px',
          transition: 'background-color 0.2s',
        }}
      >
        {localValue ? 'Yes' : 'No'}
      </button>
    </td>
  )
}
