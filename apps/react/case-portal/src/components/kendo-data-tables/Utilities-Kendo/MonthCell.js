import { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const MonthCellEditor = (props) => {
  const { dataItem, field, onChange, allMonths, ...tdProps } = props

  const allOptions = useMemo(
    () =>
      allMonths.map((p) => ({ value: p.displayName, label: p.displayName })),
    [allMonths],
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
        style={{ width: '100%' }}
        disabled={!dataItem?.isMonthAdd}
      />
    )
  }

  const productObj = allMonths.find((p) => p.id === dataItem[field])
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

export default MonthCellEditor
