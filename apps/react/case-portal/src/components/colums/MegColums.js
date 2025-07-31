export const BusinessDemandMegColumns = [
  {
    field: 'Particulars',
    title: 'Type',
    width: 100,
    groupable: true,
    headerClass: 'bold-header',
    filterable: 'false',
    editable: false,
    hidden: true,
  },
  {
    field: 'normParameterId',
    title: 'Particulars',
    editable: false,
    width: 150,
    hidden: true,
  },
  {
    field: 'displayName',
    title: 'Particulars',
    editable: false,
    widthT: 220,
  },
  {
    field: 'UOM',
    title: 'UOM',
    editable: false,
    width: 120,
  },
  {
    field: 'april',
    title: 4,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'may',
    title: 5,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'june',
    title: 6,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'july',
    title: 7,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'aug',
    title: 8,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'sep',
    title: 9,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },

  {
    field: 'oct',
    title: 10,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'nov',
    title: 11,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'dec',
    title: 12,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'jan',
    title: 1,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'feb',
    title: 2,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'march',
    title: 3,
    editable: true,
    width: 120,
    align: 'left',
    headerAlign: 'left',
    type: 'number',
  },
  {
    field: 'remark',
    title: 'Remark',
    editable: false,
    widthT: 220,
    type: 'string',
  },
  {
    field: 'idFromApi',
    title: 'idFromApi',
    filterable: 'false',
    hidden: true,
  },
]

export const SlowDownMegColumns = [
  {
    field: 'discription',
    title: 'Slowdown Desc',
    editable: true,
    type: 'descLimit',
  },

  {
    field: 'maintenanceId',
    title: 'maintenanceId',
    editable: false,
    hidden: true,
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
    field: 'rateEOE',
    title: 'EOE Production Rate',
    editable: true,
    type: 'number',
    widthT: 225,
  },
  {
    field: 'rateEO',
    title: 'EO Production Rate',
    editable: true,
    type: 'number',
    widthT: 215,
  },

  {
    field: 'remark',
    title: 'Remarks',
    editable: true,
    widthT: 220,
  },
]

export const NormalOpNormMegColumns = [
  {
    field: 'Particulars',
    title: 'Type',
    widthT: 220,
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
    widthT: 220,
  },

  {
    field: 'UOM',
    title: 'UOM / MT',
    widthT: 60,
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
    widthT: 220,
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

export const ShutdownConsumptionMegColumns = [
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
    widthT: 220,
    editable: false,
  },
  { field: 'UOM', headerName: 'UOM', widthT: 60, editable: false },

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
    widthT: 220,
    editable: false,
  },
  {
    field: 'idFromApi',
    headerName: 'idFromApi',
    hidden: true,
  },
]

export const SlowdownNormsMegColumns = [
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
    widthT: 220,
    editable: false,
  },
  { field: 'UOM', headerName: 'UOM', widthT: 220, editable: false },

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
    widthT: 220,
    editable: false,
  },
  {
    field: 'idFromApi',
    headerName: 'idFromApi',
    hidden: true,
  },
]

export const ConsumptionAopMegColumns = [
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
    widthT: 220,
  },
  {
    field: 'UOM',
    title: 'UOM / MT',
    editable: false,
    widthT: 60,
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
