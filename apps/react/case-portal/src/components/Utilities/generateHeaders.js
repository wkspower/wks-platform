export const generateHeaderNames = () => {
  // const yearRange = localStorage.getItem("year"); // Example: "2024-2025"
  const yearRange = '2024-2025'
  if (!yearRange) {
    console.error('YEAR not found in localStorage')
    return {}
  }

  const [startYear, endYear] = yearRange.split('-').map(Number)
  if (!startYear || !endYear) {
    console.error('Invalid YEAR format in localStorage')
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
    headerMap[month] =
      `${month.charAt(0).toUpperCase() + month.slice(1)}-${year % 100}`
  })

  return headerMap
}
