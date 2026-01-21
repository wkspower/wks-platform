import { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const ProductCellEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    allProducts,
    customModifiedCells,
    highlightField,
    highlight,
    rowId,
    ...tdProps
  } = props

  const allOptions = useMemo(
    () =>
      allProducts.map((p) => ({ value: p.displayName, label: p.displayName })),
    [allProducts],
  )

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )
  const checkField = highlightField || field

  const isEdited = !!(
    customModifiedCells?.[rowId] && checkField in customModifiedCells[rowId]
  )
  if (typeof onChange === 'function') {
    const handleChange = (e) => {
      onChange({
        dataItem,
        field,
        value: e.value?.value,
      })
    }

    return (
      <DropDownList
        data={allOptions}
        textField='label'
        dataItemKey='value'
        value={currentValueObj}
        onChange={handleChange}
        style={{
          width: '100%',
          color: highlight && isEdited ? 'orange' : undefined,
          fontWeight: highlight && isEdited ? 'bold' : undefined,
        }}
      />
    )
  }

  const productObj = allProducts.find((p) => p.id === dataItem[field])
  const displayLabel = productObj ? productObj.displayName : ''

  return (
    <td
      {...tdProps}
      style={{
        padding: '0.5rem 1rem',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
      }}
    >
      {displayLabel || '—'}
    </td>
  )
}

export default ProductCellEditor
