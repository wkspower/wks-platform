export const CrackerColums = [
  {
    field: 'sapMaterialCode',
    title: 'SAP MAT Code',
    widthT: 120,
    editable: false,
  },
  {
    field: 'materialDisplayName',
    title: 'Particulars',
    widthT: 120,
  },
  {
    field: 'uom',
    title: 'UOM',
    widthT: 60,
    editable: false,
  },
  {
    field: 'april',
    title: 4,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'may',
    title: 5,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'june',
    title: 6,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'july',
    title: 7,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },

  {
    field: 'august',
    title: 8,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'september',
    title: 9,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'october',
    title: 10,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'november',
    title: 11,
    editable: true,

    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'december',
    title: 12,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'january',
    title: 1,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'february',
    title: 2,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  {
    field: 'march',
    title: 3,
    editable: true,
    width: 120,
    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
  { field: 'remark', title: 'Remarks', widthT: 200, editable: true },
]
export const CrackerColumsForYearlyNorms = [
  {
    field: 'isChecked',
    type: 'switch',
    widthT: 30,
    filter: false,
  },
  {
    field: 'materialDisplayName',
    title: 'Particulars',
  },

  {
    field: 'uom',
    title: 'UOM',
    widthT: 60,
    editable: false,
  },

  {
    field: 'april',
    title: 4,
    editable: true,

    align: 'right',
    format: '{0:#.###}',
    type: 'number',
  },
]

export const ShutdownConsumptionCrackerColumns = [
  {
    field: 'sapCode',
    headerName: 'SAP MAT Code',
    widthT: 130,
    editable: false,
  },
  {
    field: 'productName',
    headerName: 'Particulars',
    widthT: 130,
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

      type: 'number',
      format: '{0:#.###}',
      editable: false,
      isDisabled: true,
      monthNumber: monthIndex,
    }
  }),
]
