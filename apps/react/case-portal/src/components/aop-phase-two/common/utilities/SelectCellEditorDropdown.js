import React, { useState, useEffect, useRef } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

/**
 * Clean & Simple Select Cell Editor Dropdown.
 * All styling is fully contained inline in this file only.
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
  valueRender = null,
}) => {
  const rawValue = dataItem?.[field]
  const storedValue =
    rawValue !== null && typeof rawValue === 'object'
      ? rawValue[valueField] ??
        rawValue.value ??
        rawValue.id ??
        rawValue[textField] ??
        rawValue.label ??
        ''
      : (rawValue ?? '')

  // Numeric check without coercion issues
  const isNumeric = (val) => {
    if (val === null || val === undefined || val === '') return false
    if (typeof val === 'number') return !isNaN(val)
    if (typeof val === 'string') {
      const trimmed = val.trim()
      return /^-?\d+(\.\d+)?$/.test(trimmed)
    }
    return false
  }

  // Clear option if enabled
  const optionsWithClear = showClearOption
    ? [{ [valueField]: '', [textField]: 'Clear' }, ...options]
    : options

  const findSelectedOption = (opts, val) => {
    if (val === null || val === undefined || val === '') return null
    if (typeof val === 'object') {
      val =
        val[valueField] ??
        val.value ??
        val.id ??
        val[textField] ??
        val.label ??
        ''
    }
    const targetStr = String(val).trim().toLowerCase()
    if (!targetStr) return null

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

    // 3. Fallback: match by textField / label / name / displayName
    if (!matched) {
      matched = opts.find((opt) => {
        const optText =
          opt[textField] ?? opt.label ?? opt.name ?? opt.displayName
        if (optText === undefined || optText === null) return false
        return String(optText).trim().toLowerCase() === targetStr
      })
    }

    // 4. Safe fallback: guarantee value is NEVER blank or invisible
    if (!matched && val) {
      const textVal = String(val).trim()
      return {
        [textField]: textVal,
        [valueField]: textVal,
        label: textVal,
        value: textVal,
      }
    }

    return matched || null
  }

  const [localValue, setLocalValue] = useState(() =>
    findSelectedOption(optionsWithClear, storedValue),
  )
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
      const text = String(opt[textField] || opt.label || '').toLowerCase()
      const val = String(opt[valueField] || opt.value || '').toLowerCase()
      const name = String(opt.name || opt.displayName || '').toLowerCase()
      return (
        text.includes(search) || val.includes(search) || name.includes(search)
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
        ? selectedOpt[valueField] ?? selectedOpt.value ?? selectedOpt.label
        : ''
    setLocalValue(selectedOpt)
    onChange({ dataItem, field, value: newValue })
  }

  // Item renderer: Simple clean dropdown item (no blue background, dark text always visible)
  const defaultItemRender = (li, itemProps) => {
    const item = itemProps?.dataItem
    if (!item) return li
    const text =
      item[textField] ||
      item.label ||
      item.displayName ||
      item.name ||
      item[valueField] ||
      ''
    const isSelected = itemProps?.selected

    return React.cloneElement(
      li,
      {
        ...li.props,
        style: {
          ...li.props.style,
          padding: '8px 12px',
          fontSize: '13px',
          fontWeight: isSelected ? '600' : 'normal',
          color: '#111827',
          backgroundColor: isSelected ? '#e5e7eb' : '#ffffff',
          cursor: 'pointer',
          whiteSpace: 'normal',
          wordBreak: 'break-word',
          lineHeight: '1.4',
        },
      },
      text,
    )
  }

  // Value renderer: simple legible text perfectly vertically centered
  const defaultValueRender = (element, value) => {
    const text =
      value && typeof value === 'object'
        ? value[textField] ||
          value.label ||
          value.displayName ||
          value.name ||
          value[valueField] ||
          ''
        : String(value || '')

    return (
      <span
        style={{
          color: '#111827',
          fontSize: '13px',
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          display: 'flex',
          alignItems: 'center',
          height: '100%',
          width: '100%',
          lineHeight: 'normal',
        }}
        title={text}
      >
        {text || placeholder}
      </span>
    )
  }

  return (
    <>
      <style>{`
        .simple-cell-dropdown {
          width: 100% !important;
          height: 36px !important;
          min-height: 36px !important;
          background-color: #ffffff !important;
          border: 1px solid #cccccc !important;
          border-radius: 4px !important;
          color: #111827 !important;
          box-sizing: border-box !important;
          display: inline-flex !important;
          align-items: center !important;
        }

        .simple-cell-dropdown .k-input,
        .simple-cell-dropdown .k-input-inner,
        .simple-cell-dropdown .k-input-value-text,
        .simple-cell-dropdown .k-input-values {
          display: flex !important;
          align-items: center !important;
          height: 100% !important;
          min-height: 100% !important;
          color: #111827 !important;
          font-size: 13px !important;
          box-sizing: border-box !important;
          margin: 0 !important;
          padding-top: 0 !important;
          padding-bottom: 0 !important;
        }

        .simple-cell-dropdown .k-input-inner {
          padding-left: 10px !important;
          padding-right: 6px !important;
        }

        .simple-cell-dropdown .k-select,
        .simple-cell-dropdown .k-input-button,
        .simple-cell-dropdown .k-button {
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
          height: 100% !important;
          color: #555555 !important;
          box-sizing: border-box !important;
        }

        .simple-dropdown-popup {
          background-color: #ffffff !important;
          border: 1px solid #cccccc !important;
          border-radius: 4px !important;
          z-index: 999999 !important;
        }

        .simple-dropdown-popup .k-list-container,
        .simple-dropdown-popup .k-list {
          background-color: #ffffff !important;
          color: #111827 !important;
        }

        .simple-dropdown-popup .k-list-item {
          color: #111827 !important;
          font-size: 13px !important;
          padding: 8px 12px !important;
        }

        .simple-dropdown-popup .k-list-item:hover,
        .simple-dropdown-popup .k-list-item.k-focus,
        .simple-dropdown-popup .k-list-item.k-selected {
          background-color: #e5e7eb !important;
          color: #111827 !important;
        }

        .simple-dropdown-popup .k-list-item:hover *,
        .simple-dropdown-popup .k-list-item.k-focus *,
        .simple-dropdown-popup .k-list-item.k-selected * {
          color: #111827 !important;
        }

        .simple-dropdown-popup .k-list-filter {
          padding: 6px 8px !important;
          background-color: #f9fafb !important;
          border-bottom: 1px solid #e5e7eb !important;
        }

        .simple-dropdown-popup .k-searchbox {
          border: 1px solid #cccccc !important;
          border-radius: 4px !important;
          background-color: #ffffff !important;
          color: #111827 !important;
        }

        .simple-dropdown-popup .k-searchbox input {
          color: #111827 !important;
          font-size: 13px !important;
        }
      `}</style>

      <DropDownList
        ref={inputRef}
        data={filteredData}
        textField={textField}
        dataItemKey={valueField}
        value={localValue}
        onChange={handleChange}
        filterable
        onFilterChange={handleFilterChange}
        className='simple-cell-dropdown'
        style={{
          width: '100%',
          height: '36px',
          minHeight: '36px',
          backgroundColor: '#ffffff',
          border: '1px solid #cccccc',
          borderRadius: '4px',
          color: '#111827',
          fontSize: '13px',
          display: 'inline-flex',
          alignItems: 'center',
          boxSizing: 'border-box',
        }}
        itemRender={itemRender || defaultItemRender}
        valueRender={valueRender || defaultValueRender}
        placeholder={placeholder}
        popupSettings={{
          className: 'simple-dropdown-popup',
          animate: false,
        }}
      />
    </>
  )
}

export default SelectCellEditorDropdown
