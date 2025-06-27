// rowSamples.js
// Sample row data for DecokingConfig (import into DecokingConfig.jsx)

export const ibrGridOneRowsSample = [
  {
    id: 0,
    Furnace: 'H10',
    MonthNameDropdown: 'Apr',
    days: '720',
    Remarks: 'Initial planning',
  },
  {
    id: 1,
    Furnace: 'H11',
    MonthNameDropdown: 'May',
    days: '744',
    Remarks: 'Extended run',
  },
  {
    id: 2,
    Furnace: 'H12',
    MonthNameDropdown: 'Jun',
    days: '720',
    Remarks: 'Coil check',
  },
]

export const ibrPlanRowsSample = [
  {
    id: 0,
    furnace: 'H10',
    startDateIBR: '2025-04-01',
    endDateIBR: '2025-04-05',
    startDateSD: '2025-04-06',
    endDateSD: '2025-04-07',
    startDateTA: '2025-04-08',
    endDateTA: '2025-04-09',
    remarks: 'Pre-coil prep',
  },
  {
    id: 1,
    furnace: 'H11',
    startDateIBR: '2025-05-10',
    endDateIBR: '2025-05-12',
    startDateSD: '2025-05-13',
    endDateSD: '2025-05-14',
    startDateTA: '2025-05-15',
    endDateTA: '2025-05-16',
    remarks: 'Post-maintenance',
  },
]

export const ibrGridThreeRowsSample = [
  {
    id: 0,
    Month: 'Mar',
    Date: '2025-03-20',
    H10ActualRunLength: 38,
    H10ProposedAOP: 50,
    H11ActualRunLength: 40,
    H11ProposedAOP: 52,
    H12ActualRunLength: 64,
    H12ProposedAOP: 76,
    H13ActualRunLength: 17,
    H13ProposedAOP: 29,
    H14ActualRunLength: 88,
    H14ProposedAOP: 100,
    DEMO: 'SD',
  },
  {
    id: 1,
    Month: 'Apr',
    Date: '2025-04-01',
    H10ActualRunLength: 50,
    H10ProposedAOP: 62,
    H11ActualRunLength: 51,
    H11ProposedAOP: 63,
    H12ActualRunLength: 77,
    H12ProposedAOP: 89,
    H13ActualRunLength: 30,
    H13ProposedAOP: 100,
    H14ActualRunLength: 100,
    H14ProposedAOP: 100,
    DEMO: 'BBU',
  },
]

export const runningDurationRowsSample = [
  {
    id: 0,
    month: 'Mar',
    ibr: 38,
    mnt: 5,
    shutdown: 1,
    slowdown: 2,
    sad: 0,
    buD: 0,
    fourF: 0,
    fiveF: 0,
    fourFD: 0,
    total: 46,
  },
  {
    id: 1,
    month: 'Apr',
    ibr: 50,
    mnt: 6,
    shutdown: 1,
    slowdown: 3,
    sad: 0,
    buD: 0,
    fourF: 0,
    fiveF: 0,
    fourFD: 0,
    total: 60,
  },
]
