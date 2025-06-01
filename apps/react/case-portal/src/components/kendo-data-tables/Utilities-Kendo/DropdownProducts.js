import { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const ProductDropDownEditor = ({
  dataItem, // the entire row object
  field, // the field name ("product")
  onChange, // callback to update Grid data
  allProducts,
  rows,
}) => {
  // 1) Build an array of { value, label } from allProducts once
  const allOptions = useMemo(
    () => allProducts.map((p) => ({ value: p.id, label: p.displayName })),
    [allProducts],
  )

  // 2) Compute a Set of product‐IDs already used by other rows
  //    (exclude current row so you can keep its own value in the dropdown)
  const existingValues = useMemo(() => {
    return new Set(
      rows
        .filter((r) => r.id !== dataItem.id) // skip current row
        .map((r) => r.product), // collect the "product" field
    )
  }, [rows, dataItem.id])

  // 3) Filter out any option whose value is already used in another row,
  //    unless it’s exactly the current row’s value (so the user can keep/change it).
  const filteredOptions = useMemo(() => {
    return allOptions.filter(
      (opt) => opt.value === dataItem[field] || !existingValues.has(opt.value),
    )
  }, [allOptions, existingValues, dataItem, field])

  // 4) Find the currently‐selected option object so DropDownList shows correct label
  const currentValueObj = useMemo(() => {
    return allOptions.find((opt) => opt.value === dataItem[field]) || null
  }, [allOptions, dataItem, field])

  // 5) When user picks a new product, fire onChange so Grid updates its data
  const handleChange = (e) => {
    onChange({
      dataItem,
      field,
      value: e.value,
    })
  }

  return (
    <td>
      <DropDownList
        data={filteredOptions}
        textField='label'
        dataItemKey='value'
        value={currentValueObj}
        onChange={handleChange}
        style={{ width: '210px' }}
      />
    </td>
  )
}

export default ProductDropDownEditor
