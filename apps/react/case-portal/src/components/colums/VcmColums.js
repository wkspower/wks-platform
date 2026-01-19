export const SlowDownVcmColumns = [
  {
    field: 'discription',
    title: 'Slowdown Desc',
    editable: true,
    type: 'discriptionDrpdwn',
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

  // {
  //   field: 'rate',
  //   title: 'Rate (TPH)',
  //   editable: true,
  //   type: 'number',
  // },

  {
    field: 'remark',
    title: 'Remarks',
    editable: true,
  },
]

export const SlowDownDmdVcmColumns = [
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

  // {
  //   field: 'rate',
  //   title: 'Rate (TPH)',
  //   editable: true,
  //   type: 'number',
  // },

  {
    field: 'remark',
    title: 'Remarks',
    editable: true,
  },
]
export const ShutdownConsumptionVcmColumns = [
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
  // { field: 'UOM/MT', headerName: 'UOM', width: 150, editable: false },
  { field: 'UOM', headerName: 'UOM/MT', width: 150, editable: false },

  ...Array.from({ length: 12 }, (_, i) => {
    const monthIndex = (i + 4) % 12 || 12
    const monthField = new Date(2000, monthIndex - 1)
      .toLocaleString('en-US', { month: 'long' })
      .toLowerCase()

    return {
      field: monthField,
      width: 120,
      type: 'number',
      format: '{0:#.###}',
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
