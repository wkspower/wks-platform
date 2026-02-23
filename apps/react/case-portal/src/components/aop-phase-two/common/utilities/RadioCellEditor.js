import React, { useState, useEffect, useRef } from 'react'
import { Radio, Box } from '@mui/material'
import { Input } from '@progress/kendo-react-inputs'

// Inline radio component - displays value with radio button in same cell
export const InlineRadioCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    radioGroupField = 'selectedHeatRateSource',
    targetField = 'finalHeatRate',
    radioValue,
    isNumberEditable,
  } = props

  const currentValue = dataItem[field] ?? ''
  const isSelected = dataItem[radioGroupField] === radioValue
  const initialValue = currentValue
  const [localValue, setLocalValue] = useState(currentValue)
  const inputRef = useRef(null)

  // Auto-focus on mount
  useEffect(() => {
    if (inputRef.current && isNumberEditable) {
      const el = inputRef.current.element || inputRef.current
      if (el && typeof el.focus === 'function') {
        el.focus()
      }
    }
  }, [])

  const handleRadioClick = () => {
    // Update the radio selection field with the radioValue (e.g., 'OEM', 'PREVIOUS_YEAR', 'PROPOSED')
    // The customItemChange handler will take care of updating finalHeatRate
    onChange({
      dataItem,
      field: radioGroupField,
      value: radioValue,
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
    <td>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 0.5,
          height: '100%',
        }}
      >
        <Radio
          checked={isSelected}
          onClick={handleRadioClick}
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
                textAlign: 'right',
                width: '100%',
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

export const InlineRadioDisplayCell = (props) => {
  const {
    dataItem,
    field,
    radioGroupField = 'selectedHeatRateSource',
    format,
    radioValue,
    customModifiedCells,
  } = props

  const value = dataItem[field]
  const isSelected = dataItem[radioGroupField] === radioValue
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
      const match = format.match(/\{0:([^}]+)\}/)
      if (match) {
        const formatSpec = match[1]
        if (formatSpec.match(/^0+\.0+$/)) {
          const decimalPlaces = formatSpec.split('.')[1].length
          displayValue = numValue.toFixed(decimalPlaces)
        }
      }
    }
  }

  return (
    <td
      {...props.tdProps}
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
        <Radio
          checked={isSelected}
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

// Original radio components for separate column (kept for backward compatibility)
export const RadioCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    sourceFields = [],
    targetField = '',
  } = props

  const currentValue = dataItem[field]

  const handleRadioChange = (event) => {
    const selectedField = event.target.value

    const selectedValue = dataItem[selectedField]

    onChange({
      dataItem,
      field,
      value: selectedField,
      syntheticEvent: event.nativeEvent,
    })

    if (targetField && selectedValue !== undefined) {
      setTimeout(() => {
        onChange({
          dataItem: { ...dataItem, [field]: selectedField },
          field: targetField,
          value: selectedValue,
          syntheticEvent: event.nativeEvent,
        })
      }, 0)
    }
  }

  return (
    <td>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 1,
          padding: '4px',
        }}
      >
        {sourceFields.map((sourceField) => (
          <Radio
            key={sourceField}
            checked={currentValue === sourceField}
            onChange={handleRadioChange}
            value={sourceField}
            size='medium'
            sx={{
              padding: '4px',
              '& .MuiSvgIcon-root': {
                // fontSize: 18,
              },
            }}
          />
        ))}
      </Box>
    </td>
  )
}

export const RadioDisplayCell = (props) => {
  const { dataItem, field, sourceFields = [] } = props
  const selectedField = dataItem[field]

  return (
    <td
      {...props.tdProps}
      style={{
        textAlign: 'center',
        verticalAlign: 'middle',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 1,
          padding: '4px',
        }}
      >
        {sourceFields.map((sourceField) => (
          <Radio
            key={sourceField}
            checked={selectedField === sourceField}
            disabled
            size='medium'
            sx={{
              padding: '4px',
              '& .MuiSvgIcon-root': {
                // fontSize: 18,
              },
            }}
          />
        ))}
      </Box>
    </td>
  )
}
