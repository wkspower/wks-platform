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
    ...generateMonthColumns(headerMap, false, valueFormat),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
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
    ...generateMonthColumns(headerMap, true,valueFormat),
    {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
      widthT: 150,
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
    ...generateMonthColumns(headerMap, false, valueFormat),
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
    { field: 'productName', title: 'Particulars', widthT: 100, editable: true },
    ...generateMonthColumns(headerMap, true, valueFormat),
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

function generateMonthColumns(headerMap = {}, editable = true, valueFormat) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]
  return monthOrder.map((month) => ({
    field: getMonthName(month).toLowerCase(),
    title: headerMap[month],
    format: valueFormat,
    editable,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  }))
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
