import productionColDefs from '../../../assets/kendo_proposed_consumption_aop.json'

const MONTH_SHORT = {
  April: 'Apr',
  May: 'May',
  June: 'Jun',
  July: 'Jul',
  August: 'Aug',
  September: 'Sep',
  October: 'Oct',
  November: 'Nov',
  December: 'Dec',
  January: 'Jan',
  February: 'Feb',
  March: 'Mar',
}

const monthRegex =
  /(April|May|June|July|August|September|October|November|December|January|February|March)/i

const getYearSuffix = (yearNumber) => {
  const s = String(yearNumber)
  return s.slice(-2)
}

const getEnhancedColDefsProposedNorms = ({
  headerMap,
  lowerVertName,
  valueFormat,
  AOP_YEAR,
}) => {
  let colDefs = productionColDefs || []

  // determine current AOP start year (e.g. '2025-26' -> 2025)
  let startYear = new Date().getFullYear()
  if (AOP_YEAR && typeof AOP_YEAR === 'string' && AOP_YEAR.indexOf('-') > -1) {
    const parts = AOP_YEAR.split('-').map((p) => p.trim())
    const parsed = parseInt(parts[0], 10)
    if (!Number.isNaN(parsed)) startYear = parsed
  }

  const currSuffix = getYearSuffix(startYear) // '25'
  const prevSuffix = getYearSuffix(startYear - 1) // '24'

  const enhancedColDefs = colDefs.map((col) => {
    let newCol = { ...col }

    // only rewrite titles for our month columns using field name pattern
    const field = String(newCol.field || '')

    const m = field.match(monthRegex)
    const monthName = m ? m[0] : null
    const monthShort = monthName ? MONTH_SHORT[monthName] : null

    if (monthShort) {
      if (/^prevYearBudget/i.test(field)) {
        // Prev year -> "Apr Budget-24"
        newCol.title = `LastFY_${monthShort}`
        newCol.editable = false
        newCol.isDisabled = true
      } else if (/^currYearBudget/i.test(field)) {
        // Curr year budget -> "Apr Budget-25"
        newCol.title = `SysGen_${monthShort}`
        newCol.editable = false
        newCol.isDisabled = true
      } else if (/^currYearProposed/i.test(field)) {
        // Curr year proposed -> "Apr Proposed-25"
        newCol.title = `Proposed_${monthShort}`
        // keep editable as-is (usually true)
      }
    }

    // apply numeric formatting for columns mapped in headerMap (past behavior)
    if (
      headerMap &&
      newCol.headerName !== undefined &&
      headerMap[newCol.headerName] !== undefined
    ) {
      newCol = {
        ...newCol,
        type: 'number',
        format: valueFormat || '{0:#.###}',
        widthT: 200,
      }
    }

    newCol = {
      ...newCol,
      type: 'number',
      format: valueFormat || '{0:#.###}',
      widthT: 200,
    }

    return newCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefsProposedNorms
