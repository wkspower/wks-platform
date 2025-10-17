export function getColDefsPercentageSummary(headerMap = {}) {
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
    ...generateMonthColumns(headerMap, false),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
  ]
}

export function getColDefsDesignCapacity(headerMap = {}) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    { field: 'productName', title: 'Particulars', widthT: 100, editable: true },
    ...generateMonthColumns(headerMap, true),
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

export function getColDefsMaxAchievedCapacity(headerMap = {}) {
  return [
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable: true,
      hidden: true,
    },
    { field: 'productName', title: 'Particulars', widthT: 100, editable: true },
    ...generateMonthColumns(headerMap, true),
  ]
}

export function getColDefsNonEditable(headerMap = {}) {
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
    ...generateMonthColumns(headerMap, false),
    { field: 'avgTph', title: 'AVG', editable: false, hidden: true },
    { field: 'isEditable', title: 'isEditable', hidden: true },
  ]
}

function generateMonthColumns(headerMap = {}, editable = true) {
  const monthOrder = [4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3]
  return monthOrder.map((month) => ({
    field: getMonthName(month).toLowerCase(),
    title: headerMap[month],
    format: '{0:#.##}',
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
