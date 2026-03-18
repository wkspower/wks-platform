export const generateHeaderNames = (yearRange) => {
  if (!yearRange) {
    console.error('YEAR not found')
    return {}
  }

  const [startYear, endYear] = yearRange.split('-').map(Number)
  if (!startYear || !endYear) {
    console.error('Invalid YEAR format')
    return {}
  }

  const months = [
    'jan',
    'feb',
    'mar',
    'apr',
    'may',
    'jun',
    'jul',
    'aug',
    'sep',
    'oct',
    'nov',
    'dec',
  ]

  let headerMap = {}

  months.forEach((month, index) => {
    const year = index >= 3 ? startYear : endYear // Apr-Dec → startYear, Jan-Mar → endYear
    headerMap[index + 1] =
      `${month.charAt(0).toUpperCase() + month.slice(1)}-${year % 100}`
  })

  return headerMap
}

export const generateCalendarYearHeaders = (yearRange) => {
  if (!yearRange) {
    console.error('YEAR not found')
    return {}
  }

  const [startYear, endYear] = yearRange.split('-').map(Number)
  if (!startYear || !endYear) {
    console.error('Invalid YEAR format')
    return {}
  }

  const months = [
    'jan',
    'feb',
    'mar',
    'apr',
    'may',
    'jun',
    'jul',
    'aug',
    'sep',
    'oct',
    'nov',
    'dec',
  ]

  let headerMap = {}

  months.forEach((month, index) => {
    // Jan-Dec all use startYear (e.g., 2026-27 → Jan-26 to Dec-26)
    const year = startYear
    headerMap[index + 1] =
      `${month.charAt(0).toUpperCase() + month.slice(1)}-${year % 100}`
  })

  return headerMap
}
