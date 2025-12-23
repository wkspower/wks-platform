import React from 'react'
import { TextField, MenuItem } from '@mui/material'

/**
 * Generic Dropdown Component
 *
 * @param {Object} props - Component props
 * @param {Array} props.options - Array of options to display
 * @param {string} props.value - Currently selected value
 * @param {Function} props.onChange - Callback when selection changes
 * @param {string} props.label - Dropdown label
 * @param {string} props.placeholder - Placeholder text (defaults to 'Select')
 * @param {string} props.valueKey - Key to use as the option value (defaults to 'id')
 * @param {string} props.labelKey - Key to use as the display label (defaults to 'displayName')
 * @param {string} props.className - CSS class name
 * @param {string} props.variant - TextField variant (defaults to 'outlined')
 * @param {boolean} props.disabled - Whether dropdown is disabled
 * @param {Object} props.sx - MUI sx prop for additional styling
 * @param {boolean} props.required - Whether field is required
 * @param {string} props.size - Size of the dropdown ('small', 'medium')
 * @param {Function} props.getOptionLabel - Custom function to get label from option
 * @param {Function} props.getOptionValue - Custom function to get value from option
 *
 * @example
 * // Basic usage with simple objects
 * <GenericDropdown
 *   options={grades}
 *   value={selectedGrade}
 *   onChange={(value) => setSelectedGrade(value)}
 *   label="Select Grade"
 *   valueKey="gradeId"
 *   labelKey="displayName"
 * />
 *
 * @example
 * // With custom label/value extraction
 * <GenericDropdown
 *   options={items}
 *   value={selectedItem}
 *   onChange={(value) => handleChange(value)}
 *   label="Select Item"
 *   getOptionValue={(item) => item.customId}
 *   getOptionLabel={(item) => `${item.name} (${item.code})`}
 * />
 */
const GenericDropdown = ({
  options = [],
  value = '',
  onChange,
  label = 'Select',
  placeholder = 'Select',
  valueKey = 'id',
  labelKey = 'displayName',
  className = 'dropdown-select',
  variant = 'outlined',
  disabled = false,
  sx = {},
  required = false,
  size = 'medium',
  getOptionLabel,
  getOptionValue,
}) => {
  // Helper function to get value from option
  const getValueFromOption = (option) => {
    if (getOptionValue) {
      return getOptionValue(option)
    }
    return option[valueKey]
  }

  // Helper function to get label from option
  const getLabelFromOption = (option) => {
    if (getOptionLabel) {
      return getOptionLabel(option)
    }
    return option[labelKey]
  }

  const handleChange = (e) => {
    const selectedValue = e.target.value
    if (onChange) {
      onChange(selectedValue)
    }
  }

  return (
    <TextField
      select
      value={value || ''}
      onChange={handleChange}
      label={label}
      placeholder={placeholder}
      className={className}
      variant={variant}
      disabled={disabled}
      required={required}
      size={size}
      InputLabelProps={{
        shrink: true,
        sx: {
          fontWeight: 'bold',
        },
      }}
      sx={{
        minWidth: 100,
        ...sx,
      }}
    >
      <MenuItem value='' disabled>
        {placeholder}
      </MenuItem>

      {Array.isArray(options) &&
        options.map((option) => (
          <MenuItem
            key={getValueFromOption(option)}
            value={getValueFromOption(option)}
          >
            {getLabelFromOption(option)}
          </MenuItem>
        ))}
    </TextField>
  )
}

export default GenericDropdown
