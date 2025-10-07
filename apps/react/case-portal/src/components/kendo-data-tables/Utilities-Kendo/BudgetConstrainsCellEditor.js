import { DropDownList } from '@progress/kendo-react-dropdowns'
import { useMemo } from 'react'

const BudgetConstrainsCellEditor = (props) => {
  const { dataItem, field, onChange, ...tdProps } = props

  // Hardcoded dropdown options
  const allOptions = useMemo(
    () => [
      { value: '+', label: '+' },
      { value: '-', label: '-' },
    ],
    [],
  )

  // month keys to update
  const monthKeys = [
    'apr',
    'may',
    'jun',
    'jul',
    'aug',
    'sep',
    'oct',
    'nov',
    'dec',
    'jan',
    'feb',
    'mar',
  ]

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  // helper to safely parse numeric percent from the row
  const getPercentFromRow = () => {
    const raw = dataItem?.budgetConstrains
    if (raw === undefined || raw === null || raw === '') return NaN
    const n = Number(raw)
    return isNaN(n) ? NaN : n
  }

  if (typeof onChange === 'function') {
    const handleChange = (e) => {
      const selected = e.value?.value ?? null // '+' or '-'
      // first update the dropdown cell itself
      onChange({
        dataItem,
        field,
        value: selected,
      })

      // if user selected nothing or percent not present, don't attempt to change months
      const percent = getPercentFromRow()
      if (selected && !isNaN(percent)) {
        // compute factor: +2% => factor 1.02, -2% => factor 0.98
        const factor = selected === '+' ? 1 + percent / 100 : 1 - percent / 100

        // update each month cell by calling onChange for each field
        monthKeys.forEach((k) => {
          const originalRaw = dataItem[k]
          const original =
            originalRaw === undefined ||
            originalRaw === null ||
            originalRaw === ''
              ? 0
              : Number(originalRaw)

          // only update if original is a valid number, else keep as-is (or set 0)
          if (!isNaN(original)) {
            const updated = parseFloat((original * factor).toFixed(3))
            onChange({
              dataItem,
              field: k,
              value: updated,
            })
          }
        })
      }
    }

    return (
      <DropDownList
        data={allOptions}
        textField='label'
        dataItemKey='value'
        value={currentValueObj}
        onChange={handleChange}
        style={{ width: '100%' }}
        disabled={
          dataItem?.budgetConstrains === '' ||
          dataItem?.budgetConstrains === undefined
        }
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

export default BudgetConstrainsCellEditor
