import { DropDownList } from '@progress/kendo-react-dropdowns'
import {
  filterBy
} from '@progress/kendo-data-query'
import { useState, useEffect, useRef } from 'react'

export const SelectCellEditor = ({
  dataItem,
  field,
  onChange,
  options = [],
  textField = 'label',
  valueField = 'value',
  placeholder = 'Select...',
  showClearOption = false,
  returnFullObject = false,
}) => {
  const storedValue = dataItem[field] ?? ''
  // Find the matching option object based on the stored value
  // Handle both string and numeric values
  const normalizeValue = (val) => {
    // Check if it's a numeric string or number (e.g., '4', '4.0', 4, 4.0)
    const numVal = parseFloat(val)
    if (!isNaN(numVal)) {
      // It's a numeric value, normalize to string
      return String(numVal)
    }
    // It's a non-numeric string (e.g., 'Price', 'Amount'), return as-is
    return String(val)
  }

  // Add clear option if enabled
  const optionsWithClear = showClearOption
    ? [{ [valueField]: '', [textField]: 'Clear' }, ...options]
    : options

  const selectedOption =
    optionsWithClear.find(
      (opt) =>
        normalizeValue(opt[valueField]) === normalizeValue(storedValue) ||
        normalizeValue(opt[textField]) === normalizeValue(storedValue),
    ) || null
  const [localValue, setLocalValue] = useState(selectedOption)
  const [filteredData, setFilteredData] = useState(optionsWithClear.slice())
  const inputRef = useRef(null)

  const filterData = (filter) => {
    const data = optionsWithClear.slice()
    return filterBy(data, filter)
  }

  const handleFilterChange = (event) => {
    setFilteredData(filterData(event.filter))
  }

  // Autofocus when cell enters edit mode
  useEffect(() => {
    const timer = setTimeout(() => {
      if (inputRef.current?.element) {
        inputRef.current.element.focus()
      }
    }, 50)
    return () => clearTimeout(timer)
  }, [])

  const handleChange = (e) => {
    const selectedOpt = e.value
    const newValue = returnFullObject
      ? selectedOpt
      : selectedOpt
        ? selectedOpt[valueField]
        : ''
    setLocalValue(selectedOpt)
    onChange({ dataItem, field, value: newValue })
  }

  return (
    <DropDownList
      ref={inputRef}
      data={filteredData}
      textField={textField}
      dataItemKey={valueField}
      value={localValue}
      onChange={handleChange}
      filterable
      onFilterChange={handleFilterChange}
      className='dropdown-editor'
      style={{ width: '100%' }}
    />
  )
}
