
export const ShutDownAllColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    type: 'descLimit',
  },
  {
    field: 'maintenanceId',
    title: 'Maintenance ID',
    editable: false,
    hidden: true,
  },
  {
    field: 'maintStartDateTime',
    title: 'SD - From',
    editable: true,
  },
  {
    field: 'maintEndDateTime',
    title: 'SD - To',
    editable: true,
  },
  {
    field: 'durationInHrs',
    title: 'Duration (hrs)',
    editable: true,
  },
  {
    field: 'remark',
    title: 'Shutdown Basis',
    editable: true,
  },
]

// PE Shutdown Columns (adds productName)
export const ShutDownPeColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    type: 'descLimit',
  },
  {
    field: 'productName1',
    title: 'Particulars',
    editable: true,
    widthT: 130,
  },
  {
    field: 'maintenanceId',
    title: 'Maintenance ID',
    editable: false,
    hidden: true,
  },
  {
    field: 'maintStartDateTime',
    title: 'SD - From',
    editable: true,
  },
  {
    field: 'maintEndDateTime',
    title: 'SD - To',
    editable: true,
  },
  {
    field: 'durationInHrs',
    title: 'Duration (hrs)',
    editable: true,
  },
  {
    field: 'remark',
    title: 'Shutdown Basis',
    editable: true,
  },
]

// PP Shutdown Columns (same as PE)
export const ShutDownPpColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    type: 'descLimit',
  },
  {
    field: 'productName1',
    title: 'Particulars',
    editable: true,
    widthT: 130,
  },
  {
    field: 'maintenanceId',
    title: 'Maintenance ID',
    editable: false,
    hidden: true,
  },
  {
    field: 'maintStartDateTime',
    title: 'SD - From',
    editable: true,
  },
  {
    field: 'maintEndDateTime',
    title: 'SD - To',
    editable: true,
  },
  {
    field: 'durationInHrs',
    title: 'Duration (hrs)',
    editable: true,
  },
  {
    field: 'remark',
    title: 'Shutdown Basis',
    editable: true,
  },
]