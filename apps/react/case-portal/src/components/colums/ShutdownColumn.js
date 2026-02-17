export const ExclusionDateColumns = [
  {
    field: 'id',
    hidden: true,
  },
  {
    field: 'exclusionStartDate',
    title: 'From Date',
    editable: true,
    fixedWidth: '200px',
  },
  {
    field: 'exclusionEndDate',
    title: 'To Date',
    editable: true,
    fixedWidth: '200px',
  },

  {
    field: 'remark',
    title: 'Reason',
    editable: true,
    fixedWidth: '200px',
  },

  {
    field: 'originalRemark',
    hidden: true,
  },
]

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
  // {
  //   field: 'productName1',
  //   title: 'Particulars',
  //   editable: true,
  //   widthT: 130,
  // },
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
export const ShutDownPeColumnsldpe12 = [
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

// PP Shutdown Columns (same as PE)
export const ShutDownPpColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    type: 'descLimit',
  },
  // {
  //   field: 'productName1',
  //   title: 'Particulars',
  //   editable: true,
  //   widthT: 130,
  // },
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
export const ShutDownPpDtaColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    type: 'descLimit',
  },
  {
    field: 'lineId',
    title: 'Line',
    type: 'lineDropdown',
    editable: true,
    width: 130,
  },
  // {
  //   field: 'productName1',
  //   title: 'Particulars',
  //   editable: true,
  //   widthT: 130,
  // },
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

export const ShutDownPTAColumns = [
  {
    field: 'discriptionDrpdwn',
    title: 'Shutdown Desc',
    editable: true,
    type: 'discriptionDrpdwn',
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

export const ShutDownPTADMDColumns = [
  {
    field: 'discriptionDrpdwn',
    title: 'Shutdown Desc',
    editable: true,
    type: 'discriptionDrpdwn',
  },

  {
    field: 'maintenanceId',
    title: 'Maintenance ID',
    editable: false,
    hidden: true,
  },
  // {
  //   field: 'maintStartDateTime',
  //   title: 'SD - From',
  //   editable: true,
  // },
  // {
  //   field: 'maintEndDateTime',
  //   title: 'SD - To',
  //   editable: true,
  // },
  {
    field: 'monthly',
    title: 'Month',
    type: 'monthDropdownPEPP',
    editable: true,
    width: 150,
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
