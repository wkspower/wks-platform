import { DropDownList } from '@progress/kendo-react-dropdowns'
import { useMemo } from 'react'

const LimitCellEditor = (props) => {
  const { dataItem, field, onChange, ...tdProps } = props

  // Hardcoded dropdown options
  const allOptions = useMemo(
    () => [
      { value: '<', label: '<' },
      { value: '>', label: '>' },
      { value: '+-', label: '+-' },
    ],
    [],
  )

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  if (typeof onChange === 'function') {
    const handleChange = (e) => {
      onChange({
        dataItem,
        field,
        value: e.value?.value, // store only '<', '>', '+-'
      })
    }

    return (
      <DropDownList
        data={allOptions}
        textField='label'
        dataItemKey='value'
        value={currentValueObj}
        onChange={handleChange}
        style={{ width: '100%' }}
        disabled={dataItem?.uom != '%'}
      />
    )
  }

  // Display selected value in read-only mode
  const displayLabel =
    allOptions.find((opt) => opt.value === dataItem[field])?.label || ''

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

export default LimitCellEditor
