export const recalcEndDate = (startRaw, durationStr) => {
  if (!startRaw) return null
  const start = new Date(startRaw)
  if (!(start instanceof Date) || isNaN(start)) return null
  // parse "HH.MM"
  const [hrsPart, minPart = '0'] = String(durationStr).split('.')
  const hrs = parseInt(hrsPart, 10)
  const mins = parseInt(minPart.padEnd(2, '0').slice(0, 2), 10)
  if (isNaN(hrs) || isNaN(mins) || mins < 0 || mins > 59) return null
  const end = new Date(start.getTime() + (hrs * 60 + mins) * 60000)
  return end
}

export const recalcDuration = (startRaw, endRaw) => {
  const start = startRaw ? new Date(startRaw) : null
  const end = endRaw ? new Date(endRaw) : null
  if (
    start instanceof Date &&
    !isNaN(start) &&
    end instanceof Date &&
    !isNaN(end)
  ) {
    const diffMs = end.getTime() - start.getTime()
    if (diffMs < 0) return ''
    const totalMins = Math.floor(diffMs / 60000)
    const hrs = Math.floor(totalMins / 60)
    const mins = totalMins % 60
    // format as "H.MM" with two-digit minutes
    return `${hrs}.${mins.toString().padStart(2, '0')}`
  }
  return ''
}
