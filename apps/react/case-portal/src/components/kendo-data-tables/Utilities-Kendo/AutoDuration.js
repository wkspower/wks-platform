export function updateRowWithDuration(row, field, value) {
  // 1) copy the row and overwrite the edited field
  const updated = { ...row, [field]: value, inEdit: true }

  // 2) Only recalc if user touched a date column
  if (field === 'maintStartDateTime' || field === 'maintEndDateTime') {
    // get current (possibly just‐edited) start/end
    const startRaw = updated.maintStartDateTime
    const endRaw = updated.maintEndDateTime

    // convert to Date (if a string was passed in)
    const start = startRaw ? new Date(startRaw) : null
    const end = endRaw ? new Date(endRaw) : null

    if (
      start instanceof Date &&
      !isNaN(start.valueOf()) &&
      end instanceof Date &&
      !isNaN(end.valueOf())
    ) {
      const diffMs = end.getTime() - start.getTime()
      const hours = diffMs / (1000 * 60 * 60)
      updated.durationInHrs = parseFloat(hours.toFixed(2))
    } else {
      // if one (or both) dates missing/invalid → zero
      updated.durationInHrs = 0
    }
  }

  return updated
}
