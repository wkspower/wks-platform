import { Input } from '@progress/kendo-react-inputs'
import { useState, useEffect, useRef } from 'react'

export const NumericEditorWithMinMax = ({
  dataItem,
  field,
  onChange,
  min,
  max,
}) => {
  // Resolve nested field paths (e.g., 'april.min' -> dataItem.april.min)
  const getNestedValue = (obj, fieldPath) => {
    const parts = fieldPath.split('.')
    let value = obj
    for (let part of parts) {
      value = value?.[part]
    }
    return value ?? ''
  }

  const initialValue = getNestedValue(dataItem, field)
  const [localValue, setLocalValue] = useState(initialValue)
  const [error, setError] = useState('')
  const inputRef = useRef(null)

  const handleChange = (e) => {
    let val = e.target.value

    // Allow only numeric (including decimal and negative)
    if (val === '' || /^-?\d*(\.\d*)?$/.test(val)) {
      const num = parseFloat(val)

      // Validate against min/max if provided
      if (val !== '') {
        let errorMsg = ''

        if (min !== undefined && num < min) {
          errorMsg = `Please enter a number between ${min} to ${max}`
        } else if (max !== undefined && num > max) {
          errorMsg = `Please enter a number between ${min} to ${max}`
        }

        setError(errorMsg)
      } else {
        setError('')
      }

      setLocalValue(val)
    }
  }

  useEffect(() => {
    if (inputRef.current) {
      const el = inputRef.current.element || inputRef.current
      if (el && typeof el.focus === 'function') el.focus()
    }
  }, [])

  const handleBlur = () => {
    if (localValue !== initialValue && !error) {
      onChange({ dataItem, field, value: localValue })
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Tab' || e.key === 'Enter') {
      if (localValue !== initialValue && !error) {
        onChange({ dataItem, field, value: localValue })
      }
    }
  }

  return (
    <div
      style={{ position: 'relative', display: 'inline-block', width: '100%' }}
    >
      <Input
        ref={inputRef}
        value={localValue}
        onChange={handleChange}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        style={{
          fontSize: '0.8rem',
          padding: '2px 2px',
          height: '22px',
          lineHeight: '1rem',
          borderColor: error ? '#d32f2f' : undefined,
          borderWidth: error ? '2px' : undefined,
          width: '100%',
          boxSizing: 'border-box',
        }}
        title={error || ''}
      />
      {error && (
        <div
          style={{
            position: 'absolute',
            top: '100%',
            left: 0,
            backgroundColor: '#ffebee',
            color: '#d32f2f',
            padding: '4px 8px',
            fontSize: '0.75rem',
            borderRadius: '4px',
            whiteSpace: 'nowrap',
            zIndex: 1000,
            marginTop: '2px',
            border: '1px solid #d32f2f',
          }}
        >
          {error}
        </div>
      )}
    </div>
  )
}
