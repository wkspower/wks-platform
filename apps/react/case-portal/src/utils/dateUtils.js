// utils/dateUtils.js
export const formatDate = (date) => {
  if (!date) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const formatDateForText = (date, includeTime = false) => {
  if (!date) return ''
  const parsedDate = new Date(date)
  if (isNaN(parsedDate)) return 'Invalid Date'
  const day = String(parsedDate.getDate()).padStart(2, '0')
  const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
  const year = parsedDate.getFullYear()
  let formatted = `${day}-${month}-${year}`
  if (includeTime) {
    let hours = parsedDate.getHours()
    const minutes = String(parsedDate.getMinutes()).padStart(2, '0')
    const ampm = hours >= 12 ? 'PM' : 'AM'
    hours = hours % 12 || 12
    formatted += ` ${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
  }
  return formatted
}
