import React from 'react'
import { Radio, Box } from '@mui/material'

// Inline radio component - displays value with radio button in same cell
export const InlineRadioCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    radioGroupField = 'selectedHeatRateSource',
    targetField = 'finalHeatRate',
  } = props

  const currentValue = dataItem[field]
  const isSelected = dataItem[radioGroupField] === field

  const handleRadioClick = () => {
    // Update the radio selection field
    onChange({
      dataItem,
      field: radioGroupField,
      value: field,
      syntheticEvent: new Event('change'),
    })

    // Also update the target field with this field's value
    setTimeout(() => {
      onChange({
        dataItem: { ...dataItem, [radioGroupField]: field },
        field: targetField,
        value: currentValue,
        syntheticEvent: new Event('change'),
      })
    }, 0)
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
        <Box sx={{ flexGrow: 1, textAlign: 'right', paddingRight: '8px' }}>
          {currentValue !== null && currentValue !== undefined
            ? currentValue
            : ''}
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
  } = props

  const value = dataItem[field]
  const isSelected = dataItem[radioGroupField] === field

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
        <Box sx={{ flexGrow: 1, textAlign: 'right', paddingRight: '8px' }}>
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
