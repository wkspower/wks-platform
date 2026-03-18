import { recalcDuration, recalcEndDate } from '../commonUtilityFunctions'

/**
 * Apply date calculations based on dateCalculationConfig
 * @param {Object} rowData - The row data object
 * @param {string} field - The field being changed
 * @param {any} value - The new value
 * @param {Object} config - Date calculation configuration
 * @param {boolean} convertToISO - Whether to convert dates to ISO strings
 * @returns {Object} Object with calculated field updates
 */
export const applyDateCalculations = (
  rowData,
  field,
  value,
  config,
  convertToISO = false,
) => {
  if (!config) return {}

  const { dateField1, dateField2, daysField, requiredInHr, roundDaysAndDates } =
    config
  const updates = {}

  // Scenario 1: If both dates exist and one is changed, calculate duration
  if (field === dateField1 || field === dateField2) {
    if (rowData[dateField1] && rowData[dateField2]) {
      const duration = recalcDuration(
        rowData[dateField1],
        rowData[dateField2],
        requiredInHr,
      )
      updates[daysField] = roundDaysAndDates ? Math.floor(duration) : duration
    }
    // Scenario 4: If startDate is changed and days exists (but no endDate), calculate endDate
    else if (
      field === dateField1 &&
      rowData[daysField] &&
      !rowData[dateField2]
    ) {
      const newEnd = recalcEndDate(value, rowData[daysField], requiredInHr)
      if (newEnd) {
        updates[dateField2] = convertToISO ? newEnd.toISOString() : newEnd
      }
    }
    // Scenario 5: If endDate is changed and days exists (but no startDate), calculate startDate
    else if (
      field === dateField2 &&
      rowData[daysField] &&
      !rowData[dateField1]
    ) {
      const days = requiredInHr
        ? parseFloat(rowData[daysField]) / 24
        : parseFloat(rowData[daysField])
      if (!isNaN(days) && days >= 0) {
        const newStart = new Date(
          new Date(value).getTime() - days * 24 * 60 * 60000,
        )
        updates[dateField1] = convertToISO ? newStart.toISOString() : newStart
      }
    }
  }
  // Scenario 2: If duration is changed and startDate exists, calculate endDate
  else if (field === daysField && rowData[dateField1] && value) {
    const newEnd = recalcEndDate(rowData[dateField1], value, requiredInHr)
    if (newEnd) {
      updates[dateField2] = convertToISO ? newEnd.toISOString() : newEnd
    }
  }
  // Scenario 3: If duration is changed and endDate exists (but no startDate), calculate startDate
  else if (
    field === daysField &&
    !rowData[dateField1] &&
    rowData[dateField2] &&
    value
  ) {
    const endDate = new Date(rowData[dateField2])
    const days = requiredInHr ? parseFloat(value) / 24 : parseFloat(value)
    if (!isNaN(days) && days >= 0) {
      const newStart = new Date(endDate.getTime() - days * 24 * 60 * 60000)
      updates[dateField1] = convertToISO ? newStart.toISOString() : newStart
    }
  }

  return updates
}

export const handleTabKeyNavigation = ({
  e,
  activeCellRef,
  columns,
  hiddenFields,
  rows,
  setRows,
  setEdit,
  extractAllColumns,
}) => {
  const nativeEvent = e.nativeEvent
  const key = nativeEvent.key
  if (key !== 'Tab' && key !== 'Enter') return

  const { rowId, field: currentField } = activeCellRef.current
  if (!rowId || !currentField) return

  const allCols = extractAllColumns(columns).filter(
    (col) => !hiddenFields.includes(col.field) && !col.hidden,
  )
  const editableCols = allCols.filter(
    (col) =>
      col.editable === true &&
      col.type !== 'textarea' &&
      col.field !== 'remarks' &&
      col.field !== 'reasons',
  )
  if (editableCols.length === 0) return

  const currentRowIndex = rows.findIndex((r) => String(r.id) === String(rowId))
  if (currentRowIndex === -1) return

  const currentEditableColIndex = editableCols.findIndex(
    (c) => c.field === currentField,
  )
  if (currentEditableColIndex === -1) return

  nativeEvent.preventDefault()

  if (key === 'Tab') {
    const nextIdx = nativeEvent.shiftKey
      ? currentEditableColIndex - 1
      : currentEditableColIndex + 1

    if (nextIdx >= 0 && nextIdx < editableCols.length) {
      // Next/prev editable cell in same row
      const nextField = editableCols[nextIdx].field
      const newEdit = { [rowId]: [nextField] }
      setEdit(newEdit)
      activeCellRef.current = { rowId, field: nextField }
    } else if (!nativeEvent.shiftKey && nextIdx >= editableCols.length) {
      // Wrap to first editable col of next editable row
      let nextRowIndex = currentRowIndex + 1
      while (nextRowIndex < rows.length) {
        const nextRow = rows[nextRowIndex]
        if (nextRow.isEditable !== false) {
          const nextField = editableCols[0].field
          setRows((prev) =>
            prev.map((r) => ({ ...r, inEdit: r.id === nextRow.id })),
          )
          const newEdit = { [nextRow.id]: [nextField] }
          setEdit(newEdit)
          activeCellRef.current = { rowId: nextRow.id, field: nextField }
          break
        }
        nextRowIndex++
      }
    } else if (nativeEvent.shiftKey && nextIdx < 0) {
      // Wrap to last editable col of prev editable row
      let prevRowIndex = currentRowIndex - 1
      while (prevRowIndex >= 0) {
        const prevRow = rows[prevRowIndex]
        if (prevRow.isEditable !== false) {
          const nextField = editableCols[editableCols.length - 1].field
          setRows((prev) =>
            prev.map((r) => ({ ...r, inEdit: r.id === prevRow.id })),
          )
          const newEdit = { [prevRow.id]: [nextField] }
          setEdit(newEdit)
          activeCellRef.current = { rowId: prevRow.id, field: nextField }
          break
        }
        prevRowIndex--
      }
    }
  } else if (key === 'Enter') {
    if (nativeEvent.shiftKey) {
      // Shift+Enter: Move to same column in previous editable row
      let prevRowIndex = currentRowIndex - 1
      while (prevRowIndex >= 0) {
        const prevRow = rows[prevRowIndex]
        if (prevRow.isEditable !== false) {
          setRows((prev) =>
            prev.map((r) => ({ ...r, inEdit: r.id === prevRow.id })),
          )
          const newEdit = { [prevRow.id]: [currentField] }
          setEdit(newEdit)
          activeCellRef.current = { rowId: prevRow.id, field: currentField }
          break
        }
        prevRowIndex--
      }
    } else {
      // Enter: Move to same column in next editable row
      let nextRowIndex = currentRowIndex + 1
      while (nextRowIndex < rows.length) {
        const nextRow = rows[nextRowIndex]
        if (nextRow.isEditable !== false) {
          setRows((prev) =>
            prev.map((r) => ({ ...r, inEdit: r.id === nextRow.id })),
          )
          const newEdit = { [nextRow.id]: [currentField] }
          setEdit(newEdit)
          activeCellRef.current = { rowId: nextRow.id, field: currentField }
          break
        }
        nextRowIndex++
      }
    }
  }
}
