export function getColDefsPercentageSummary(headerMap = {}, valueFormat) {
  return [
    { field: 'idFromApi', title: 'ID', hidden: true },
    { field: 'aopCaseId', title: 'Case ID', editable: false, hidden: true },
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsFixedWidth(headerMap, false, valueFormat),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
    //add here
  ]
}

export function getColDefsPercentageSummaryPEPP(headerMap = {}, valueFormat) {
  return [
    { field: 'idFromApi', title: 'ID', hidden: true },
    { field: 'aopCaseId', title: 'Case ID', editable: false, hidden: true },
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsPercentageSummaryPPE(headerMap, false, valueFormat),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
    //add here
  ]
}

export function getColDefsDesignCapacity(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumns(headerMap, true, valueFormat),
    {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
      widthT: 90,
    },
  ]
}

export function getColDefsDesignCapacityPTA(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsPTA(headerMap, true, valueFormat),
    {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
      widthT: 90,
    },
  ]
}
export function getColDefsDesignCapacityAROMATICS(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 200,
      editable: false,
    },
    {
      field: 'april',
      title: 'PAREX#1',
      editable: true,
      align: 'left',
      widthT: 200,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'may',
      title: 'PAREX#2',
      editable: true,
      align: 'left',
      widthT: 200,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'june',
      title: 'PAREX#3',
      editable: true,
      align: 'left',
      widthT: 200,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'total',
      title: 'Total',
      editable: false,
      align: 'left',
      widthT: 200,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
    },
  ]
}

export function getColDefsDesignCapacityPEPP(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsForPEPP(headerMap, false, valueFormat, true),
  ]
}

export function getColDefsMaxAchievedCapacity(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsFixedWidth(headerMap, true, valueFormat),
  ]
}

export function getColDefsMaxAchievedCapacityPTA(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsFixedWidthPTA(headerMap, true, valueFormat),
  ]
}

export function getColDefsMaxAchievedCapacityPEPP(headerMap = {}, valueFormat) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumnsFixedWidthPEPP(headerMap, true, valueFormat),
  ]
}

export function getColDefsMaxAchievedCapacityAROMATICS(
  headerMap = {},
  valueFormat,
) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 200,
      editable: false,
    },
    {
      field: 'april',
      title: 'PAREX#1',
      align: 'left',
      widthT: 200,
      editable: true,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'may',
      title: 'PAREX#2',
      align: 'left',
      widthT: 200,
      editable: true,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'june',
      title: 'PAREX#3',
      editable: true,
      widthT: 200,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'total',
      title: 'Total',
      editable: false,
      align: 'left',
      widthT: 200,
      headerAlign: 'left',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
    },
  ]
}

export function getColDefsNonEditable(headerMap = {}, valueFormat) {
  return [
    { field: 'idFromApi', title: 'ID', hidden: true },
    { field: 'aopCaseId', title: 'Case ID', hidden: true },
    {
      field: 'normParametersFKId',
      title: 'Particulars',
      widthT: 100,
      editable: false,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable: false,
    },
    ...generateMonthColumns(headerMap, false, valueFormat),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
  ]
}

function generateMonthColumns(
  headerMap = {},
  editable = true,
  valueFormat,
  isPEPP,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const monthName = getMonthName(month)

    return {
      field: getMonthName(month).toLowerCase(),
      title: headerMap[month],
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      widthT: monthName === 'March' ? (isPEPP ? 200 : 110) : undefined,
    }
  })
}

function generateMonthColumnsPTA(
  headerMap = {},
  editable = true,
  valueFormat,
  isPEPP,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const fullMonthName = getMonthName(month)
    const monthName = getMonthName(month)
    const monthNameTitle = fullMonthName.slice(0, 3) // Jan, Feb, Mar...

    return {
      field: getMonthName(month).toLowerCase(),
      title: monthNameTitle,
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      widthT: monthName === 'March' ? (isPEPP ? 200 : 110) : undefined,
    }
  })
}

function generateMonthColumnsForPEPP(
  headerMap = {},
  editable = true,
  valueFormat,
  isPEPP,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const fullMonthName = getMonthName(month)
    const monthName = getMonthName(month)
    const monthNameTitle = fullMonthName.slice(0, 3) // Jan, Feb, Mar...

    return {
      field: monthName.toLowerCase(),
      title: monthNameTitle,
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      // widthT: fullMonthName === 'March' ? (isPEPP ? 200 : 110) : undefined,
    }
  })
}

function generateMonthColumnsFixedWidth(
  headerMap = {},
  editable = true,
  valueFormat,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const monthName = getMonthName(month)

    return {
      field: monthName.toLowerCase(),
      title: headerMap[month],
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      widthT: monthName === 'March' ? 200 : undefined,
    }
  })
}

function generateMonthColumnsFixedWidthPTA(
  headerMap = {},
  editable = true,
  valueFormat,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const fullMonthName = getMonthName(month)
    const monthName = getMonthName(month)
    const monthNameTitle = fullMonthName.slice(0, 3) // Jan, Feb, Mar...

    return {
      field: monthName.toLowerCase(),
      title: monthNameTitle,
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      widthT: monthName === 'March' ? 200 : undefined,
    }
  })
}

function generateMonthColumnsFixedWidthPEPP(
  headerMap = {},
  editable = true,
  valueFormat,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const fullMonthName = getMonthName(month)
    const monthName = getMonthName(month)
    const monthNameTitle = fullMonthName.slice(0, 3) // Jan, Feb, Mar...

    return {
      field: monthName.toLowerCase(),
      title: monthNameTitle,
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      //widthT: monthName === 'March' ? 200 : undefined,
    }
  })
}

function generateMonthColumnsPercentageSummaryPPE(
  headerMap = {},
  editable = true,
  valueFormat,
) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]

  return monthOrder.map((month) => {
    const monthName = getMonthName(month)

    return {
      field: monthName.toLowerCase(),
      title: headerMap[month],
      format: valueFormat,
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
      //widthT: monthName === 'March' ? 200 : undefined,
    }
  })
}

function getMonthName(num) {
  const months = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ]
  return months[num - 1]
}
