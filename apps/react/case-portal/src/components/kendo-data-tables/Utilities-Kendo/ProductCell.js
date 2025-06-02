// ProductCellEditor.js
import { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const ProductCellEditor = (props) => {
  const { dataItem, field, onChange, allProducts, ...tdProps } = props

  const allOptions = useMemo(
    () => allProducts.map((p) => ({ value: p.id, label: p.displayName })),
    [allProducts],
  )

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  if (typeof onChange === 'function') {
    const handleChange = (e) => {
      console.log(e, 'test---->')
      onChange({
        dataItem,
        field,
        value: e.value?.label,
      })
    }

    return (
      <DropDownList
        data={allOptions}
        textField='label'
        dataItemKey='value'
        value={currentValueObj}
        onChange={handleChange}
        style={{ width: '100%' }} // fill the cell
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

// import { useMemo } from 'react'

// const ProductCell = ({ dataItem, field, allProducts, ...tdProps }) => {
//   const productObj = useMemo(
//     () => allProducts.find((p) => p.id === dataItem[field]),
//     [allProducts, dataItem, field],
//   )

//   const displayLabel = productObj ? productObj.displayName : ''

//   return (
//     <td {...tdProps} style={{ padding: '0.5rem 1rem' }}>
//       {displayLabel || '—'}
//     </td>
//   )
// }

// export default ProductCell
