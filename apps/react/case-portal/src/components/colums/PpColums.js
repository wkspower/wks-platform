export const BusinessDemandPpColumns = [
  {
    field: 'Particulars',
    title: 'Type',
    width: 100,
    groupable: true,
    editable: false,
    hidden: true,
  },
  {
    field: 'normParameterId',
    title: 'Particulars',
    editable: false,
    width: 125,
    hidden: true,
  },

  {
    field: 'displayName',
    title: 'Particulars',
    editable: false,
    width: 125,
  },
  {
    field: 'april',
    title: 4,
    editable: true,
    width: 120,
    rightAlign: true,
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'may',
    title: 5,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'june',
    title: 6,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'july',
    title: 7,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'aug',
    title: 8,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'sep',
    title: 9,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'oct',
    title: 10,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'nov',
    title: 11,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'dec',
    title: 12,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'jan',
    title: 1,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'feb',
    title: 2,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'march',
    title: 3,
    editable: true,
    width: 120,
    rightAlign: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'remark',
    title: 'Remark',
    width: 180,
    editable: false,
  },
  {
    field: 'idFromApi',
    title: 'ID from API',
    hidden: true,
  },
]

export const SlowDownPpColumns = [
  {
    field: 'discription',
    title: 'Slowdown Desc',
    editable: true,
  },

  {
    field: 'maintenanceId',
    title: 'maintenanceId',
    editable: false,
    hidden: true,
  },

  {
    field: 'productName1',
    title: 'Particulars',
    editable: true,
  },

  {
    field: 'maintStartDateTime',
    title: 'SD- From',
    type: 'dateTime',
    editable: true,
  },

  {
    field: 'maintEndDateTime',
    title: 'SD- To',
    type: 'dateTime',
    editable: true,
  },

  {
    field: 'durationInHrs',
    title: 'Duration (hrs)',
    editable: true,
  },

  {
    field: 'rate',
    title: 'Rate (TPH)',
    editable: true,
    type: 'number',
  },

  {
    field: 'remark',
    title: 'Remarks',
    editable: true,
  },
]

export const NormalOpNormPpColumns = [
  {
    field: 'Particulars',
    title: 'Type',
    width: 110,
    groupable: true,
    editable: false,
    hidden: true,
  },
  {
    field: 'materialFkId',
    title: 'Particulars',
    width: 120,
    hidden: true,
  },
  {
    field: 'productName',
    title: 'Particulars',
    width: 120,
  },

  {
    field: 'UOM',
    title: 'UOM / MT',
    width: 100,
    editable: false,
  },

  {
    field: 'april',
    title: 4,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'may',
    title: 5,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'june',
    title: 6,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'july',
    title: 7,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },

  {
    field: 'august',
    title: 8,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'september',
    title: 9,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'october',
    title: 10,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'november',
    title: 11,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'december',
    title: 12,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'january',
    title: 1,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'february',
    title: 2,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'march',
    title: 3,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.#####}',
    type: 'number',
  },
  {
    field: 'remarks',
    title: 'Remark',
    width: 125,
    editable: true,
  },

  {
    field: 'idFromApi',
    title: 'idFromApi',
    hidden: true,
  },
  {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  },
]

export const ShutdownConsumptionPpColumns = [
  {
    field: 'Particulars',
    headerName: 'Type',
    width: 120,
    hidden: true,
  },
  {
    field: 'materialFkId',
    headerName: 'Particulars',
    minWidth: 150,
    editable: false,
    hidden: true,
    width: 120,
  },
  {
    field: 'productName',
    headerName: 'Particulars',
    width: 180,
    editable: false,
  },
  { field: 'UOM', headerName: 'UOM', width: 150, editable: false },

  ...Array.from({ length: 12 }, (_, i) => {
    const monthIndex = (i + 4) % 12 || 12
    const monthField = new Date(2000, monthIndex - 1)
      .toLocaleString('en-US', { month: 'long' })
      .toLowerCase()

    return {
      field: monthField,
      width: 120,
      type: 'number',
      format: '{0:#.#####}',
      editable: false,
      isDisabled: true,
      monthNumber: monthIndex,
    }
  }),

  {
    field: 'remarks',
    headerName: 'Remark',
    width: 120,
    editable: false,
  },
  {
    field: 'idFromApi',
    headerName: 'idFromApi',
    hidden: true,
  },
]

export const SlowdownNormsPpColumns = [
  {
    field: 'Particulars',
    headerName: 'Type',
    width: 120,
    hidden: true,
  },
  {
    field: 'materialFkId',
    headerName: 'Particulars',
    minWidth: 150,
    editable: false,
    hidden: true,
    width: 120,
  },
  {
    field: 'productName',
    headerName: 'Particulars',
    width: 180,
    editable: false,
  },
  { field: 'UOM', headerName: 'UOM', width: 150, editable: false },

  ...Array.from({ length: 12 }, (_, i) => {
    const monthIndex = (i + 4) % 12 || 12
    const monthField = new Date(2000, monthIndex - 1)
      .toLocaleString('en-US', { month: 'long' })
      .toLowerCase()

    return {
      field: monthField,
      width: 120,
      type: 'number',
      format: '{0:#.#####}',
      editable: false,
      isDisabled: true,
      monthNumber: monthIndex,
    }
  }),

  {
    field: 'remarks',
    headerName: 'Remark',
    width: 120,
    editable: false,
  },
  {
    field: 'idFromApi',
    headerName: 'idFromApi',
    hidden: true,
  },
]
export const ConsumptionAopPpColumns = [
  {
    field: 'Particulars',
    title: 'Type',
    editable: false,
    width: 200,
    hidden: true,
  },
  {
    field: 'NormParametersId',
    hidden: true,
  },

  {
    field: 'productName',
    title: 'Particulars',
    editable: false,
    width: 200,
  },
  {
    field: 'UOM',
    title: 'UOM / MT',
    editable: false,
    width: 150,
  },
  {
    field: 'april',
    title: 4,
  },
  {
    field: 'may',
    title: 5,
  },
  {
    field: 'june',
    title: 6,
  },
  {
    field: 'july',
    title: 7,
  },
  {
    field: 'aug',
    title: 8,
  },
  {
    field: 'sep',
    title: 9,
  },
  {
    field: 'oct',
    title: 10,
  },
  {
    field: 'nov',
    title: 11,
  },
  {
    field: 'dec',
    title: 12,
  },
  {
    field: 'jan',
    title: 1,
  },
  {
    field: 'feb',
    title: 2,
  },
  {
    field: 'march',
    title: 3,
  },
  {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  },
]
