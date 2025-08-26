// utils/colDefsFactory.js
export const createColDefs = ({
  headerMap,
  editable = false,
  includeRemarks = false,
  includeIds = false,
}) => {
  const baseCols = [
    includeIds && {
      field: 'idFromApi',
      title: 'ID',
      hidden: true,
    },
    includeIds && {
      field: 'aopCaseId',
      title: 'Case ID',
      hidden: true,
    },
    {
      field: 'materialFKId',
      title: 'Particulars',
      widthT: 100,
      editable,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 100,
      editable,
    },
    // Months dynamically
    ...[4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3].map((month) => ({
      field: getMonthField(month), // e.g. april, may, june...
      title: headerMap[month],
      format: '{0:#.##}',
      editable,
      align: 'left',
      headerAlign: 'left',
      type: 'number',
    })),
    includeRemarks && {
      field: 'remarks',
      title: 'Remark',
      editable: true,
      align: 'left',
      headerAlign: 'left',
      widthT: 150,
    },
    includeIds && {
      field: 'avgTph',
      title: 'AVG',
      editable: false,
      hidden: true,
    },
    includeIds && {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ].filter(Boolean) // remove falsy items

  return baseCols
}

// helper function: map month number -> field name
const getMonthField = (month) => {
  const map = {
    1: 'january',
    2: 'february',
    3: 'march',
    4: 'april',
    5: 'may',
    6: 'june',
    7: 'july',
    8: 'august',
    9: 'september',
    10: 'october',
    11: 'november',
    12: 'december',
  }
  return map[month]
}
