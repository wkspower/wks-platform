import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const monthOptions = [
  { value: 1, text: 'Jan' },
  { value: 2, text: 'Feb' },
  { value: 3, text: 'Mar' },
  { value: 4, text: 'Apr' },
  { value: 5, text: 'May' },
  { value: 6, text: 'Jun' },
  { value: 7, text: 'Jul' },
  { value: 8, text: 'Aug' },
  { value: 9, text: 'Sep' },
  { value: 10, text: 'Oct' },
  { value: 11, text: 'Nov' },
  { value: 12, text: 'Dec' }
]

const MonthDropdownEditor = (props) => {
  const { dataItem, field, onChange } = props
  
  const handleChange = (e) => {
    onChange({
      dataItem: dataItem,
      field: field,
      syntheticEvent: e.syntheticEvent,
      value: e.target.value?.value || e.target.value
    })
  }

  const selectedMonth = monthOptions.find(month => month.value === dataItem[field])

  return (
    <DropDownList
      data={monthOptions}
      textField="text"
      dataItemKey="value"
      value={selectedMonth}
      onChange={handleChange}
      style={{ width: '100%' }}
    />
  )
}

export default MonthDropdownEditor