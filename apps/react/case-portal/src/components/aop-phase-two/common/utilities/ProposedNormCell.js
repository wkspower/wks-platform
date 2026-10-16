import React, { useState, useEffect, useRef } from 'react'
import { Checkbox, Box } from '@mui/material'
import { InputBase } from '../../../../../node_modules/@mui/material/index'

/**
 * ProposedNormCellEditor - Editor cell for Proposed Norm column
 * Shows checkbox + editable number input.
 * Checkbox is disabled for 'Model Calculated' rows (isModelCalculated flag).
 */
export const ProposedNormCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    isNumberEditable = true,
    isModelCalculated = false,
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
    if (isModelCalculated) return
    onChange({
      dataItem,
      field: 'applyActualNormToAll',
      value: !isChecked,
      syntheticEvent: new Event('change'),
    })
  }

  const handleValueChange = (e) => {
    const val = e.target.value
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
          disabled={isModelCalculated}
          size='medium'
          sx={{ padding: '2px' }}
        />
        <Box sx={{ flexGrow: 1 }}>
          {isNumberEditable ? (
            <InputBase
              ref={inputRef}
              value={localValue}
              onChange={handleValueChange}
              onBlur={handleBlur}
              onKeyDown={handleKeyDown}
              className='input-editor'
            />
          ) : (
            <Box
              sx={{
                fontSize: '15px',
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

/**
 * ProposedNormDisplayCell - Display cell for Proposed Norm column
 * Shows checkbox (disabled) + formatted value.
 * Checkbox is disabled for 'Model Calculated' rows (isModelCalculated flag).
 */
export const ProposedNormDisplayCell = (props) => {
  const {
    dataItem,
    field,
    format,
    customModifiedCells,
    isModelCalculated,
    alwaysEditable,
  } = props
  const value = dataItem[field]
  const isChecked = dataItem.applyActualNormToAll || false
  const rowId = dataItem.id

  const isEdited = Object.prototype.hasOwnProperty.call(
    customModifiedCells?.[rowId] || {},
    field,
  )

  const applyKendoNumberFormat = (val, fmt) => {
    if (!fmt || val === null || val === undefined) return val
    const match = fmt.match(/\{0:([^}]+)\}/)
    if (!match) return val
    const formatSpec = match[1]
    const numVal = parseFloat(val)
    if (isNaN(numVal)) return val
    if (formatSpec.match(/^0+\.0+$/)) {
      const decimalPlaces = formatSpec.split('.')[1].length
      const factor = Math.pow(10, decimalPlaces)
      const truncated = Math.trunc(numVal * factor) / factor
      return truncated.toFixed(decimalPlaces)
    }
    return val
  }

  let displayValue = value
  if (format && (typeof value === 'number' || typeof value === 'string')) {
    const numValue = typeof value === 'string' ? parseFloat(value) : value
    if (!isNaN(numValue)) {
      displayValue = applyKendoNumberFormat(numValue, format)
    }
  }

  const rawTitle = value !== null && value !== undefined ? String(value) : ''
  const { title: _ignoredTitle, ...restTdProps } = props.tdProps || {}

  return (
    <td
      {...restTdProps}
      {...(alwaysEditable ? { 'data-always-editable': 'true' } : {})}
      title={rawTitle}
      style={{
        ...restTdProps?.style,
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
          sx={{ padding: '2px' }}
        />
        <Box
          title={rawTitle}
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

export default ProposedNormCellEditor
