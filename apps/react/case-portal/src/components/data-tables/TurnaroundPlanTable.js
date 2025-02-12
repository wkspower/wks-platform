import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'

const columns = [
  { field: 'id', headerName: 'Sr. No.', width: 100, flex: 0.1, editable: true },
  {
    field: 'activities',
    headerName: 'Activities',
    width: 300,
    editable: true,
    flex: 1,
  },
  {
    field: 'taFrom',
    headerName: 'TA - From',
    type: 'date',
    width: 180,
    editable: true,
    flex: 0.4,
    // valueGetter: (params) => {
    //   const date = dayjs(params?.row?.taFrom)
    //   return date.isValid() ? date.toDate() : null
    // },
    // renderCell: (params) => {
    //   return (
    //     <input
    //       type='date'
    //       value={params.value ? dayjs(params.value).format('YYYY-MM-DD') : ''}
    //       onChange={(e) => {
    //         const newDate = e.target.value // Get selected date
    //         params.api.setEditCellValue({
    //           id: params.id,
    //           field: params.field,
    //           value: newDate, // Set new value
    //         })
    //       }}
    //       style={{
    //         border: 'none',
    //         outline: 'none',
    //         background: 'transparent',
    //         fontSize: 'inherit',
    //         width: '100%',
    //       }}
    //     />
    //   )
    // },
    renderCell: (params) => {
      const date = params.value
      return date ? dayjs(date).format('DD/MM/YYYY') : 'No Date'
    },
  },
  {
    field: 'taTo',
    headerName: 'TA - To',
    type: 'date',
    width: 180,
    editable: true,
    flex: 0.4,
    // valueGetter: (params) => {
    //   const date = dayjs(params?.row?.tato)
    //   return date.isValid() ? date.toDate() : null
    // },
    renderCell: (params) => {
      const date = params.value
      return date ? dayjs(date).format('DD/MM/YYYY') : 'No Date'
    },
    // renderCell: (params) => {
    //   return (
    //     <input
    //       type='date'
    //       value={params.value ? dayjs(params.value).format('YYYY-MM-DD') : ''}
    //       onChange={(e) => {
    //         const newDate = e.target.value // Get selected date
    //         params.api.setEditCellValue({
    //           id: params.id,
    //           field: params.field,
    //           value: newDate, // Set new value
    //         })
    //       }}
    //       style={{
    //         border: 'none',
    //         outline: 'none',
    //         background: 'transparent',
    //         fontSize: 'inherit',
    //         width: '100%',
    //       }}
    //     />
    //   )
    // },
  },
  {
    field: 'durationHrs',
    headerName: 'Duration Hrs',
    width: 150,
    editable: true,
    flex: 0.5,
  },
  {
    field: 'period',
    headerName: 'Period',
    width: 250,
    editable: true,
    flex: 0.5,
  },
  {
    field: 'remark',
    headerName: 'Remark',
    width: 200,
    editable: true,
    flex: 0.5,
  },
]

const productionData = [
  {
    id: 1,
    activities: 'Preheater cleaning',
    // taFrom: '2024-03-12',
    // taTo: '2024-03-13',
    durationHrs: 10,
    period: '1 day',
    remark: 'Routine cleaning of preheater unit',
  },
  {
    id: 2,
    activities: 'Strippers inspection',
    // taFrom: '2024-03-15',
    // taTo: '2024-03-16',
    durationHrs: 14,
    period: '1.5 days',
    remark: 'Visual inspection and minor repairs',
  },
  {
    id: 3,
    activities: 'Rotary kiln overhaul',
    // taFrom: '2024-03-18',
    // taTo: '2024-03-22',
    durationHrs: 40,
    period: '5 days',
    remark: 'Complete overhaul, including internal parts replacement',
  },
  {
    id: 4,
    activities: 'Cooling system check',
    // taFrom: '2024-03-20',
    // taTo: '2024-03-21',
    durationHrs: 16,
    period: '2 days',
    remark: 'Maintenance and efficiency check of cooling system',
  },
  {
    id: 5,
    activities: 'Pump calibration',
    // taFrom: '2024-03-22',
    // taTo: '2024-03-23',
    durationHrs: 12,
    period: '1 day',
    remark: 'Calibrating pumps for accurate performance',
  },
  {
    id: 6,
    activities: 'Power grid inspection',
    // taFrom: '2024-03-25',
    // taTo: '2024-03-26',
    durationHrs: 18,
    period: '1.5 days',
    remark: 'Inspection and maintenance of power grid systems',
  },
  {
    id: 7,
    activities: 'Exchanger tube cleaning',
    // taFrom: '2024-03-27',
    // taTo: '2024-03-28',
    durationHrs: 8,
    period: '1 day',
    remark: 'Cleaning of heat exchanger tubes',
  },
  {
    id: 8,
    activities: 'Dryer system testing',
    // taFrom: '2024-03-30',
    // taTo: '2024-03-31',
    durationHrs: 10,
    period: '1 day',
    remark: 'Test run of the dryer system after maintenance',
  },
  {
    id: 9,
    activities: 'Compressor inspection',
    // taFrom: '2024-04-02',
    // taTo: '2024-04-03',
    durationHrs: 15,
    period: '1.5 days',
    remark: 'Inspection and oil change for the compressor',
  },
  {
    id: 10,
    activities: 'Boiler re-commissioning',
    // taFrom: '2024-04-05',
    // taTo: '2024-04-07',
    durationHrs: 24,
    period: '3 days',
    remark: 'Re-commissioning of the boiler system post maintenance',
  },
]

const TurnaroundPlanTable = () => (
  <div>
    <ASDataGrid
      columns={columns}
      rows={productionData}
      title='Turnaround Plan Table'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[10, 20, 30]}
    />
  </div>
)

export default TurnaroundPlanTable
