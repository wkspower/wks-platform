import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'



const YearDropdownEditor = (props,) => {
  const { dataItem, field, onChange, AOP_YEAR } = props
  const getYearOptions = (AOP_YEAR) => {
  let baseYear = parseInt(String(AOP_YEAR).slice(0, 4), 10)
  if (isNaN(baseYear)) baseYear = new Date().getFullYear()
  return Array.from({ length: 6 }, (_, i) => {
    const yearStr = (baseYear - i).toString()
    return {
      value: yearStr, // value as string
      text: yearStr,
    }
  })
}
  const options = getYearOptions(AOP_YEAR)
  const selected = options.find(opt => opt.value === String(dataItem[field]))

  return (
    <DropDownList
      data={options}
      textField="text"
      dataItemKey="value"
      value={selected}
      onChange={e =>
        onChange({
          dataItem,
          field,
          syntheticEvent: e.syntheticEvent,
          value: e.target.value?.value || e.target.value,
        })
      }
      style={{ width: '100px' }}
    />
  )
}

export default YearDropdownEditor