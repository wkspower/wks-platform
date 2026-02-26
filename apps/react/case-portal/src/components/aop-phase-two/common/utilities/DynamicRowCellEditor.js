import React from 'react'
import { NoSpinnerNumericEditor } from './numbericColumns'
import { SelectCellEditor } from './SelectCellEditor'
import { BooleanCellEditor } from './BooleanCellEditor'
import { TextCellEditorUpdated } from './TextCellEditorUpdated'
import DateOnlyPicker from './DatePicker'
import DateTimePickerEditor from './DatePickeronSelectedYr'
import { DropDownList } from '@progress/kendo-react-dropdowns'

export const DynamicRowCellEditor = (props) => {
  const { dataItem, field, onChange } = props
  const inputType = dataItem?.type
  const rawOptions = dataItem?.options || []

  // Convert string array to object array for SelectCellEditor
  const options = rawOptions.map((opt) => {
    if (typeof opt === 'string') {
      return { value: opt, label: opt }
    }
    return opt
  })

  switch (inputType) {
    case 'number':
    case 'numeric':
      return <NoSpinnerNumericEditor {...props} />

    case 'dropdown':
    case 'select':
      // Use simple DropDownList for string arrays
      if (rawOptions.length > 0 && typeof rawOptions[0] === 'string') {
        return (
          <td>
            <DropDownList
              value={dataItem[field]}
              onChange={(e) =>
                onChange({ dataItem, field, value: e.target.value })
              }
              data={rawOptions}
              style={{
                width: '100%',
                backgroundColor: 'lightGrey',
              }}
            />
          </td>
        )
      }
      return (
        <SelectCellEditor
          {...props}
          options={options}
          textField='label'
          valueField='value'
        />
      )

    case 'boolean':
    case 'yesno':
      return <BooleanCellEditor {...props} />

    case 'text':
      return <TextCellEditorUpdated {...props} />

    case 'date':
      return <DateOnlyPicker {...props} />

    case 'datetime':
      return <DateTimePickerEditor {...props} />

    default:
      return <NoSpinnerNumericEditor {...props} />
  }
}

export const DynamicRowDisplayCell = (props) => {
  const { dataItem, field, tdProps, format } = props
  const value = dataItem?.[field]
  const inputType = dataItem?.type

  let displayValue = value

  if (inputType === 'boolean' || inputType === 'yesno') {
    displayValue = typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
  } else if (inputType === 'date' && value instanceof Date) {
    const year = value.getFullYear()
    const month = String(value.getMonth() + 1).padStart(2, '0')
    const day = String(value.getDate()).padStart(2, '0')
    displayValue = `${year}-${month}-${day}`
  } else if (inputType === 'datetime' && value instanceof Date) {
    const year = value.getFullYear()
    const month = String(value.getMonth() + 1).padStart(2, '0')
    const day = String(value.getDate()).padStart(2, '0')
    const hours = String(value.getHours()).padStart(2, '0')
    const minutes = String(value.getMinutes()).padStart(2, '0')
    displayValue = `${year}-${month}-${day} ${hours}:${minutes}`
  } else {
    // Format numbers using the format function from column definition
    if (!isNaN(value)) {
      displayValue = Number(value).toFixed(3)
    }
  }

  return (
    <td {...tdProps} title={String(displayValue ?? '')}>
      {displayValue ?? ''}
    </td>
  )
}
