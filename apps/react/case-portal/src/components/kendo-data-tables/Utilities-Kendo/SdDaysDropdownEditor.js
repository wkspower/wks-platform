import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const SDDaysDropdownEditorWrapper = (props) => {
  const { dataItem, field, onChange, sdDaysValues = [] } = props
  // Use API data if available, fallback to static
  const options = sdDaysValues.length
    ? sdDaysValues.map((opt) => ({
        value: opt.value,
        text: opt.name || opt.text, // support both
      }))
    : []
  const selected = options.find((opt) => opt.value === dataItem[field])

  return (
    <DropDownList
      data={options}
      textField='text'
      dataItemKey='value'
      value={selected}
      onChange={(e) =>
        onChange({
          dataItem,
          field,
          syntheticEvent: e.syntheticEvent,
          value: e.target.value?.value || e.target.value,
        })
      }
      style={{ width: '100%' }}
    />
  )
}

export default SDDaysDropdownEditorWrapper
