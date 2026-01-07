import { Input } from '@progress/kendo-react-inputs'
import { useState } from 'react'

export const NumericEditorWithMinMax = ({
  dataItem,
  field,
  onChange,
  min,
  max,
}) => {
  const initialValue = dataItem[field] ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const [error, setError] = useState('')

  const handleChange = (e) => {
    let val = e.target.value

    // Allow only numeric (including decimal and negative)
    if (val === '' || /^-?\d*(\.\d*)?$/.test(val)) {
      const num = parseFloat(val)

      // Validate against min/max if provided
      if (val !== '') {
        let errorMsg = ''

        if (min !== undefined && num < min) {
          errorMsg = `Value must be at least ${min}`
        } else if (max !== undefined && num > max) {
          errorMsg = `Value must be at most ${max}`
        }

        setError(errorMsg)
      } else {
        setError('')
      }

      setLocalValue(val)
    }
  }

  const handleBlur = () => {
    if (localValue !== initialValue && !error) {
      onChange({ dataItem, field, value: localValue })
    }
  }

  return (
    <div
      style={{ position: 'relative', display: 'inline-block', width: '100%' }}
    >
      <Input
        value={localValue}
        onChange={handleChange}
        onBlur={handleBlur}
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
