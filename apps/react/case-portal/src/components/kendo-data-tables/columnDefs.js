export const ibrGridOne = [
  {
    field: 'displayName',
    title: 'Furnace',
    type: 'string',
    widthT: 400,
    editable: false,
    isDisabled: true,
  },
  {
    field: 'month',
    title: 'Month',
    type: 'string',
    widthT: 200,
    editable: true,
  },
  {
    field: 'attributeValue',
    title: 'Run Length',
    type: 'number',
    widthT: 400,
    editable: true,
  },
  {
    field: 'remarks',
    title: 'Remarks',
    type: 'string',

    editable: true,
  },
  {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  },
  {
    field: 'isMonthAdd',
    title: 'isMonthAdd',
    hidden: true,
  },
  {
    field: 'id',
    title: 'id',
    hidden: true,
  },
]

export const ibrPlanColumns = [
  {
    field: 'ibrEDId',
    title: 'ibrEDId',
    hidden: true,
  },
  {
    field: 'ibrSDId',
    title: 'ibrSDId',
    hidden: true,
  },
  {
    field: 'sdEDId',
    title: 'sdEDId',
    hidden: true,
  },
  {
    field: 'sdSDId',
    title: 'sdSDId',
    hidden: true,
  },
  {
    field: 'taEDId',
    title: 'taEDId',
    hidden: true,
  },
  {
    field: 'taSDId',
    title: 'taSDId',
    hidden: true,
  },
  { field: 'furnace', title: 'Furnace', editable: false, width: 200 },

  { field: 'ibrSD', title: 'Start Date-IBR', editable: true, width: 200 },
  { field: 'ibrED', title: 'End Date-IBR', editable: true, width: 200 },
  { field: 'taSD', title: 'Start Date-TA', editable: true, width: 200 },
  { field: 'taED', title: 'End Date-TA', editable: true, width: 200 },
  { field: 'sdED', title: 'Start Date-SD', editable: true, width: 200 },
  { field: 'sdSD', title: 'Start Date-SD', editable: true, width: 200 },

  {
    field: 'preCoil',
    title: 'Pre Coil Replacement',
    editable: true,
    width: 200,
  },
  {
    field: 'postCoil',
    title: 'Post Coil Replacement',
    editable: true,
    width: 200,
  },
  { field: 'isCoil', title: 'Is Coil Replacement', editable: true, width: 200 },

  // { field: 'remarks', title: 'Remarks', editable: true, width: 250 },
]

export const ibrGridThree = [
  {
    field: 'id',
    title: 'id',
    hidden: true,
  },
  {
    field: 'month_',
    title: 'Month',
    type: 'string',
    width: 80,
    headerAlign: 'left',
    editable: false,
    isDisabled: true,
  },
  {
    field: 'date',
    title: 'Date',
    type: 'date',
    format: '{0:dd-MMM-yy}',
    width: 100,
    headerAlign: 'left',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'hTenActual',
    title: 'H10 Actual run length',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'tenProposed',
    title: 'H10 Proposed AOP',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: true,
  },

  {
    field: 'hElevenActual',
    title: 'H11 Actual run length',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'elevenProposed',
    title: 'H11 Proposed AOP',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: true,
  },

  {
    field: 'hTwelveActual',
    title: 'H12 Actual run length',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'twelveProposed',
    title: 'H12 Proposed AOP',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: true,
  },

  {
    field: 'hThirteenActual',
    title: 'H13 Actual run length',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'thirteenProposed',
    title: 'H13 Proposed AOP',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: true,
  },

  {
    field: 'hFourteenActual',
    title: 'H14 Actual run length',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: false,
    isDisabled: true,
  },

  {
    field: 'fourteenProposed',
    title: 'H14 Proposed AOP',
    type: 'number',
    format: '{0:n0}',
    width: 120,
    align: 'right',
    headerAlign: 'right',
    editable: true,
  },

  {
    field: 'demo',
    title: 'DEMO',
    type: 'string',
    width: 80,
    headerAlign: 'center',
    editable: true,
  },
]

export const runningDurationColumns = [
  { field: 'month', title: 'Month', editable: false },
  { field: 'ibr', title: 'IBR', editable: true, type: 'number' },
  { field: 'mnt', title: 'MNT', editable: true, type: 'number' },
  {
    field: 'shutdown',
    title: 'Shutdown',
    editable: true,
    width: 120,
    type: 'number',
  },
  {
    field: 'slowdown',
    title: 'Slowdown',
    editable: true,
    width: 120,
    type: 'number',
  },
  { field: 'sad', title: 'SAD', editable: true, type: 'number' },
  { field: 'buD', title: 'BUD', editable: true, type: 'number' },
  { field: 'fourF', title: '4F', editable: true, type: 'number' },
  { field: 'fiveF', title: '5F', editable: true, type: 'number' },
  { field: 'fourFD', title: '4FD', editable: true, type: 'number' },
  { field: 'total', title: 'Total', editable: false, type: 'number' },
  { field: 'remarks', title: 'Remarks', editable: true, width: 250 },
]
