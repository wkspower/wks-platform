import React from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const PROPANE_OPTIONS = [
  { label: 'Propane Min', value: 'Propane Min' },
  { label: 'Propane 1Z', value: 'Propane 1Z' },
  { label: 'Propane 2Z', value: 'Propane 2Z' },
]

const PropaneDropdown = (props) => {
  const { dataItem, field, onChange } = props

  const handleChange = (e) => {
    onChange({
      dataItem: dataItem,
      field: field,
      syntheticEvent: e.syntheticEvent,
      value: e.target.value?.value || e.target.value,
    })
  }

  const selectedOption = PROPANE_OPTIONS.find(
    (opt) => opt.value === dataItem[field],
  )

  return (
    <DropDownList
      data={PROPANE_OPTIONS}
      textField='label'
      dataItemKey='value'
      value={selectedOption}
      onChange={handleChange}
      style={{ width: '100%' }}
    />
  )
}

export default PropaneDropdown
