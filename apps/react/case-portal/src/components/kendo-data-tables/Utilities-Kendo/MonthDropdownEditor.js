import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const monthOptions = [
  { value: 1, text: 'January' },
  { value: 2, text: 'February' },
  { value: 3, text: 'March' },
  { value: 4, text: 'April' },
  { value: 5, text: 'May' },
  { value: 6, text: 'June' },
  { value: 7, text: 'July' },
  { value: 8, text: 'August' },
  { value: 9, text: 'September' },
  { value: 10, text: 'October' },
  { value: 11, text: 'November' },
  { value: 12, text: 'December' },
]

const MonthDropdownEditor = (props) => {
  const { dataItem, field, onChange } = props

  const handleChange = (e) => {
    onChange({
      dataItem: dataItem,
      field: field,
      syntheticEvent: e.syntheticEvent,
      value: e.target.value?.value || e.target.value,
    })
  }

  const selectedMonth = monthOptions.find(
    (month) => month.value === dataItem[field],
  )

  return (
    <DropDownList
      data={monthOptions}
      textField='text'
      dataItemKey='value'
      value={selectedMonth}
      onChange={handleChange}
      style={{ width: '100%' }} // <--- use full cell width
    />
  )
}

export default MonthDropdownEditor
