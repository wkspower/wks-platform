import React, { useMemo } from 'react'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const LineDropdownEditor = (props) => {
  const {
    dataItem,
    field,
    onChange,
    allLines = [],
    customModifiedCells,
    highlightField,
    highlight,
    rowId,
  } = props

  const allOptions = useMemo(
    () =>
      allLines.map((l) => ({
        value: l.id,
        label: l.displayName,
      })),
    [allLines],
  )

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field],
  )

  const checkField = highlightField || field
  const isEdited = !!(
    customModifiedCells?.[rowId] && checkField in customModifiedCells[rowId]
  )

  const handleChange = (e) => {
    onChange({
      dataItem,
      field,
      syntheticEvent: e.syntheticEvent,
      value: e.value?.value ?? e.target?.value?.value ?? null,
    })
  }

  // Edit mode — wrap in <td> exactly like MonthDropdownPEPP
  if (typeof onChange === 'function') {
    return (
      <td
        style={{
          padding: 0,
          overflow: 'visible',
        }}
      >
        <DropDownList
          data={allOptions}
          textField='label'
          dataItemKey='value'
          value={currentValueObj}
          onChange={handleChange}
          style={{
            width: '100%',
            border: 'none',
            height: '100%',
            color: highlight && isEdited ? 'orange' : undefined,
            fontWeight: highlight && isEdited ? 'bold' : undefined,
          }}
          popupSettings={{
            appendTo: document.body,
            animate: false,
          }}
        />
      </td>
    )
  }

  // Display mode (read-only cell)
  const lineObj = allLines.find((l) => l.id === dataItem[field])
  const displayLabel = lineObj ? lineObj.displayName : ''

  return (
    <td
      title={displayLabel}
      style={{
        padding: '0.5rem 1rem',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        color: highlight && isEdited ? 'orange' : undefined,
        fontWeight: highlight && isEdited ? 'bold' : undefined,
      }}
    >
      {displayLabel || '—'}
    </td>
  )
}

export default LineDropdownEditor
