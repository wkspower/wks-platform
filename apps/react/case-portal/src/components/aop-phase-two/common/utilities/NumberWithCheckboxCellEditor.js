import React, { useState, useEffect, useRef } from 'react'
import { Checkbox, Box } from '@mui/material'
import { Input } from '@progress/kendo-react-inputs'

export const NumberWithCheckboxCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    isNumberEditable = true,
    alwaysEditable,
  } = props
  const currentValue = dataItem[field] ?? ''
  const isChecked = dataItem.applyActualNormToAll || false
  const initialValue = currentValue
  const [localValue, setLocalValue] = useState(currentValue)
  const inputRef = useRef(null)

  // Auto-focus on mount only if number is editable
  useEffect(() => {
    if (inputRef.current && isNumberEditable) {
      const el = inputRef.current.element || inputRef.current
      if (el && typeof el.focus === 'function') {
        el.focus()
      }
    }
  }, [isNumberEditable])

  const handleCheckboxClick = () => {
    // Toggle the checkbox - customItemChange handler will handle copying to all months
    onChange({
      dataItem,
      field: 'applyActualNormToAll',
      value: !isChecked,
      syntheticEvent: new Event('change'),
    })
  }

  const handleValueChange = (e) => {
    const val = e.target.value
    // Allow empty string or valid numeric input (including decimals)
    if (val === '' || /^\d*(\.\d*)?$/.test(val)) {
      setLocalValue(val)
    }
  }

  const handleBlur = () => {
    if (localValue !== initialValue) {
      onChange({ dataItem, field, value: localValue })
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Tab' || e.key === 'Enter') {
      if (localValue !== initialValue) {
        onChange({ dataItem, field, value: localValue })
      }
    }
  }

  return (
    <td {...(alwaysEditable ? { 'data-always-editable': 'true' } : {})}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 0.5,
          height: '100%',
        }}
      >
        <Checkbox
          checked={isChecked}
          onClick={handleCheckboxClick}
          size='medium'
          sx={{
            padding: '2px',
          }}
        />
        <Box sx={{ flexGrow: 1 }}>
          {isNumberEditable ? (
            <Input
              ref={inputRef}
              value={localValue}
              onChange={handleValueChange}
              onBlur={handleBlur}
              onKeyDown={handleKeyDown}
              style={{
                fontSize: '0.8rem',
                padding: '2px 2px',
                height: '22px',
                lineHeight: '1rem',
              }}
            />
          ) : (
            <Box
              sx={{
                fontSize: '0.8rem',
                padding: '2px 8px 2px 2px',
                height: '22px',
                lineHeight: '1.4rem',
                textAlign: 'right',
                width: '100%',
              }}
            >
              {currentValue !== null && currentValue !== undefined
                ? currentValue
                : ''}
            </Box>
          )}
        </Box>
      </Box>
    </td>
  )
}

export const NumberWithCheckboxDisplayCell = (props) => {
  const { dataItem, field, format, customModifiedCells, alwaysEditable } = props
  const value = dataItem[field]
  const isChecked = dataItem.applyActualNormToAll || false
  const rowId = dataItem.id

  // Check if this cell was edited
  const isEdited = Object.prototype.hasOwnProperty.call(
    customModifiedCells?.[rowId] || {},
    field,
  )

  // Apply formatting if provided
  let displayValue = value
  if (format && (typeof value === 'number' || typeof value === 'string')) {
    const numValue = typeof value === 'string' ? parseFloat(value) : value
    if (!isNaN(numValue)) {
      displayValue = numValue
    }
  }

  return (
    <td
      {...props.tdProps}
      {...(alwaysEditable ? { 'data-always-editable': 'true' } : {})}
      style={{
        ...props.tdProps?.style,
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 0.5,
          height: '100%',
        }}
      >
        <Checkbox
          checked={isChecked}
          disabled
          size='medium'
          sx={{
            padding: '2px',
          }}
        />
        <Box
          sx={{
            flexGrow: 1,
            textAlign: 'right',
            paddingRight: '8px',
            color: isEdited ? 'orange' : 'inherit',
            fontWeight: isEdited ? 'bold' : 'normal',
          }}
        >
          {displayValue !== null && displayValue !== undefined
            ? displayValue
            : ''}
        </Box>
      </Box>
    </td>
  )
}

export default NumberWithCheckboxCellEditor
