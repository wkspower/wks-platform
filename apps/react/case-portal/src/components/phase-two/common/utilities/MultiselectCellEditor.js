import { MultiSelect } from '@progress/kendo-react-dropdowns'
import { useState, useEffect, useRef } from 'react'

export const MultiselectCellEditor = ({
  dataItem,
  field,
  onChange,
  options = [],
  textField = 'text',
  valueField = 'value',
  placeholder = 'Select...',
  tagLimit,
}) => {
  const storedValues = dataItem[field] ?? []

  // Find the objects that match the stored values array
  const getInitialSelection = () => {
    if (!Array.isArray(storedValues) || !storedValues.length || !options.length)
      return []
    return options.filter((option) => storedValues.includes(option[valueField]))
  }

  const [localValue, setLocalValue] = useState(getInitialSelection())
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    const selectedItems = e.target.value || []
    setLocalValue(selectedItems)
  }

  // Debounced sync to grid, but skip first render
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      // Extract array of values from selected objects
      const valuesToSend = localValue.map((item) => {
        if (
          item &&
          typeof item === 'object' &&
          item[valueField] !== undefined
        ) {
          return item[valueField]
        }
        return item
      })

      // Only send if the values actually changed
      const currentValues = Array.isArray(storedValues) ? storedValues : []
      const hasChanged =
        valuesToSend.length !== currentValues.length ||
        valuesToSend.some((val) => !currentValues.includes(val))

      if (hasChanged) {
        onChange({ dataItem, field, value: valuesToSend })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, storedValues, valueField])

  return (
    <td style={{ textAlign: 'start' }}>
      <MultiSelect
        value={localValue}
        onChange={handleChange}
        data={options}
        textField={textField}
        valueField={valueField}
        placeholder={placeholder}
        tagLimit={tagLimit}
        autoClose={false} // Keep dropdown open after selection
        filterable={true} // Enable search/filter functionality
        style={{
          fontSize: '0.8rem',
          minHeight: '22px',
          width: '100%',
        }}
      />
    </td>
  )
}

// Display component for view mode - shows comma-separated values
export const MultiselectDisplayCell = ({
  dataItem,
  field,
  options = [],
  textField = 'text',
  valueField = 'value',
  ...tdProps
}) => {
  const values = dataItem[field] ?? []

  // Convert array of values to display text
  const getDisplayText = () => {
    if (!Array.isArray(values) || values.length === 0) return '—'

    const displayTexts = values.map((value) => {
      const foundOption = options.find((option) => option[valueField] === value)
      return foundOption ? foundOption[textField] : value
    })

    return displayTexts.join(', ')
  }

  const displayText = getDisplayText()

  return (
    <td
      {...tdProps}
      style={{
        padding: '0.5rem',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        ...tdProps.style,
      }}
      title={displayText}
    >
      {displayText}
    </td>
  )
}
