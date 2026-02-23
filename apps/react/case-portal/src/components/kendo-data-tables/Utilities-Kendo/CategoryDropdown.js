import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const options = [
  { id: 0, value: '0' },
  { id: 1, value: '1' },
  { id: 2, value: '2' },
]

const CategoryDropdownEditor = (props) => {
  const { dataItem, field } = props
  const value = options.find((opt) => opt.id === dataItem[field]) || null

  return (
    <td>
      <DropDownList
        data={options}
        textField='value'
        dataItemKey='id'
        value={value}
        onChange={(e) => {
          props.onChange({
            dataItem,
            field,
            value: e.value ? e.value.id : null,
          })
        }}
        style={{ width: '100%' }}
      />
    </td>
  )
}

export default CategoryDropdownEditor
