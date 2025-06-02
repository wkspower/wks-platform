import { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const ProductDropDownEditor = ({
  dataItem,
  field,
  onChange,
  allProducts,
  ...otherProps // Kendo passes other props like editing, style, etc.
}) => {
  // Build an array of { value, label } once
  const allOptions = useMemo(
    () => allProducts.map((p) => ({ value: p.id, label: p.displayName })),
    [allProducts],
  )

  // Find the currently‐selected option
  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  // When user picks a new product, tell Kendo to update grid data
  const handleChange = (e) => {
    onChange({
      dataItem,
      field,
      value: e.value,
    })
  }

  return (
    <DropDownList
      {...otherProps}
      data={allOptions}
      textField='label'
      dataItemKey='value'
      value={currentValueObj}
      onChange={handleChange}
      style={{ width: '100%' }}
    />
  )
}

export default ProductDropDownEditor
