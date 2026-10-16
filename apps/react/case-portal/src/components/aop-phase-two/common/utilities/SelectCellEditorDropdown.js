import React, { useState, useEffect, useRef } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

/**
 * Robust Select Cell Editor for dropdown columns.
 * Accurately matches UUIDs, string IDs, and numbers without numeric coercion / truncation bugs.
 * Renders both operation name and configuration subtitle in the dropdown list for clear distinction.
 */
export const SelectCellEditorDropdown = ({
  dataItem,
  field,
  onChange,
  options = [],
  textField = 'label',
  valueField = 'value',
  placeholder = 'Select...',
  showClearOption = false,
  returnFullObject = false,
  itemRender = null,
}) => {
  const storedValue = dataItem?.[field] ?? ''

  // Only consider as numeric if it is a pure number or a pure numeric string (e.g. 4, '4', '4.0')
  // Do NOT treat UUIDs (e.g. '8B382BF1...') or names (e.g. '4G Operation') as numbers!
  const isNumeric = (val) => {
    if (val === null || val === undefined || val === '') return false
    if (typeof val === 'number') return !isNaN(val)
    if (typeof val === 'string') {
      const trimmed = val.trim()
      return /^-?\d+(\.\d+)?$/.test(trimmed)
    }
    return false
  }

  // Add clear option if enabled
  const optionsWithClear = showClearOption
    ? [{ [valueField]: '', [textField]: 'Clear' }, ...options]
    : options

  const findSelectedOption = (opts, val) => {
    if (val === null || val === undefined || val === '') return null
    const targetStr = String(val).trim().toLowerCase()

    // 1. Primary check: exact or numeric match on valueField
    let matched = opts.find((opt) => {
      const optVal = opt[valueField]
      if (optVal === undefined || optVal === null) return false
      if (isNumeric(optVal) && isNumeric(val)) {
        return Number(optVal) === Number(val)
      }
      return String(optVal).trim().toLowerCase() === targetStr
    })

    // 2. Secondary check: match on opt.id or opt.gId
    if (!matched) {
      matched = opts.find((opt) => {
        const optId = opt.id ?? opt.gId
        if (!optId) return false
        return String(optId).trim().toLowerCase() === targetStr
      })
    }

    // 3. Fallback: match by textField / label
    if (!matched) {
      matched = opts.find((opt) => {
        const optText = opt[textField]
        if (optText === undefined || optText === null) return false
        if (isNumeric(optText) && isNumeric(val)) {
          return Number(optText) === Number(val)
        }
        return String(optText).trim().toLowerCase() === targetStr
      })
    }

    return matched || null
  }

  const selectedOption = findSelectedOption(optionsWithClear, storedValue)
  const [localValue, setLocalValue] = useState(selectedOption)
  const [filteredData, setFilteredData] = useState(optionsWithClear.slice())
  const inputRef = useRef(null)

  // Keep localValue and filteredData in sync when storedValue or options change
  useEffect(() => {
    const matched = findSelectedOption(optionsWithClear, storedValue)
    setLocalValue(matched)
    setFilteredData(optionsWithClear.slice())
  }, [storedValue, options, showClearOption])

  const filterData = (filter) => {
    if (!filter || !filter.value) return optionsWithClear.slice()
    const search = String(filter.value).trim().toLowerCase()
    return optionsWithClear.filter((opt) => {
      const text = String(opt[textField] || '').toLowerCase()
      const val = String(opt[valueField] || '').toLowerCase()
      const conf = String(opt.configuration || opt.config || '').toLowerCase()
      const name = String(opt.name || opt.displayName || '').toLowerCase()
      return (
        text.includes(search) ||
        val.includes(search) ||
        conf.includes(search) ||
        name.includes(search)
      )
    })
  }

  const handleFilterChange = (event) => {
    setFilteredData(filterData(event.filter))
  }

  // Autofocus when entering edit mode
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

  // Default item renderer showing name and configuration subtitle
  const defaultItemRender = (li, itemProps) => {
    const item = itemProps?.dataItem
    if (!item) return li
    const title = item[textField] || item.displayName || item.name || ''
    const sub = item.configuration || item.config || ''

    return React.cloneElement(li, li.props, (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '2px',
          padding: '4px 2px',
        }}
      >
        <span style={{ fontWeight: 600, fontSize: '13px', color: '#1a1a1a' }}>
          {title}
        </span>
        {sub ? (
          <span
            style={{
              fontSize: '11px',
              color: '#555',
              lineHeight: 1.25,
              wordBreak: 'break-word',
            }}
          >
            {sub}
          </span>
        ) : null}
      </div>
    ))
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
      itemRender={itemRender || defaultItemRender}
      placeholder={placeholder}
    />
  )
}

export default SelectCellEditorDropdown
