import { DropDownList } from '@progress/kendo-react-dropdowns'
import { useMemo, useEffect } from 'react'

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
  ] // helper to parse numeric percent safely (keeps 0.0)
  const parsePercent = (v) => {
    if (v === undefined || v === null || v === '') return NaN
    const n = Number(v)
    return isNaN(n) ? NaN : n
  }

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  // helper to safely parse numeric percent from the row
  // const getPercentFromRow = () => {
  //   const raw = dataItem?.percentChange
  //   if (raw === undefined || raw === null || raw === '') return NaN
  //   const n = Number(raw)
  //   return isNaN(n) ? NaN : n
  // }

  // ---------- NEW: clear dropdown when percentChange differs from original ----------
  useEffect(() => {
    if (typeof onChange !== 'function') return

    const p = parsePercent(dataItem?.percentChange)
    const orig = parsePercent(dataItem?.originalPercentChange)

    const bothNaN = isNaN(p) && isNaN(orig)
    const isDifferent = !bothNaN && p !== orig

    if (
      isDifferent &&
      dataItem[field] !== null &&
      dataItem[field] !== undefined &&
      dataItem[field] !== ''
    ) {
      onChange({ dataItem, field, value: null })
    }
  }, [
    dataItem?.percentChange,
    dataItem?.originalPercentChange, // <- remove dataItem?.[field] from deps
  ])

  // ---------- rest of handler ----------
  const getPercentFromRow = () => {
    return parsePercent(dataItem?.percentChange)
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
        disabled={dataItem?.percentChange == dataItem?.originalPercentChange}
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
