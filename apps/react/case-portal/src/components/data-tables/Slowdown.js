import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'

const productOptions = [
  'Product A',
  'Product B',
  'Product C',
  'Product D',
  'Product E',
  'Product F',
  'Product G',
  'Product H',
  'Product I',
  'Product J',
  'Product K',
  'Product L',
]
const slowdownColumns = [
  { field: 'desc', headerName: 'Slowdown Desc', width: 200, editable: true },
  {
    field: 'product',
    headerName: 'Product',
    editable: true,
    minWidth: 125,
    renderEditCell: (params) => {
      const { id } = params
      const isEditable = id > 10 // Enable only for rows beyond 10

      return (
        <Autocomplete
          options={productOptions}
          value={params.value || ''}
          disableClearable
          onChange={(event, newValue) => {
            params.api.setEditCellValue({
              id: params.id,
              field: 'product',
              value: newValue,
            })
          }}
          onInputChange={(event, newInputValue) => {
            if (event && event.type === 'keydown' && event.key === 'Enter') {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: newInputValue,
              })
            }
          }}
          renderInput={(params) => (
            <TextField {...params} variant='outlined' size='small' />
          )}
          disabled={!isEditable}
          fullWidth
        />
      )
    },
  },
  {
    field: 'from',
    headerName: 'SD- From',
    width: 150,
    editable: true,
    type: 'dateTime',
    renderCell: (params) => {
      const date = params.value
      return date && dayjs(date).isValid()
        ? dayjs(date).format('DD/MM/YYYY HH:mm:ss') // Format as date + time
        : 'No Date' // If the date is invalid or empty, show 'No Date'
    },
  },
  {
    field: 'to',
    headerName: 'SD- To',
    width: 150,
    editable: true,
    type: 'dateTime',
    renderCell: (params) => {
      const date = params.value
      return date && dayjs(date).isValid()
        ? dayjs(date).format('DD/MM/YYYY HH:mm:ss') // Format as date + time
        : 'No Date' // If the date is invalid or empty, show 'No Date'
    },
  },
  {
    field: 'duration',
    headerName: 'Duration Hrs.',
    width: 130,
    editable: true,
    flex: 0.5,
  },
  { field: 'rate', headerName: 'Rate', width: 100, editable: true, flex: 0.5 },
  {
    field: 'remark',
    headerName: 'Remark',
    width: 250,
    editable: true,
    flex: 2,
  },
]

const slowdownData = [
  {
    id: 1,
    desc: 'Machine Maintenance',
    product: 'Product A',
    // from: '2024-02-10T08:00:00',
    // to: '2024-02-12T16:00:00',
    duration: 8,
    rate: 90,
    remark: 'Planned maintenance',
  },
  {
    id: 2,
    desc: 'Power Outage',
    product: 'Product B',
    // from: '2024-02-14T12:00:00',
    // to: '2024-02-15T00:00:00',
    duration: 12,
    rate: 85,
    remark: 'Unexpected outage',
  },
  {
    id: 3,
    desc: 'Material Shortage',
    product: 'Product C',
    // from: '2024-02-18T09:30:00',
    // to: '2024-02-20T17:30:00',
    duration: 10,
    rate: 80,
    remark: 'Supplier delay',
  },
  {
    id: 4,
    desc: 'Quality Check',
    product: 'Product D',
    // from: '2024-02-22T10:00:00',
    // to: '2024-02-23T16:00:00',
    duration: 6,
    rate: 88,
    remark: 'Additional inspections',
  },
  {
    id: 5,
    desc: 'Software Update',
    product: 'Product E',
    // from: '2024-02-25T14:00:00',
    // to: '2024-02-26T18:00:00',
    duration: 4,
    rate: 92,
    remark: 'System upgrade',
  },
  {
    id: 6,
    desc: 'Network Downtime',
    product: 'Product F',
    // from: '2024-03-01T08:00:00',
    // to: '2024-03-02T13:00:00',
    duration: 5,
    rate: 87,
    remark: 'IT infrastructure issue',
  },
  {
    id: 7,
    desc: 'Cooling System Failure',
    product: 'Product G',
    // from: '2024-03-05T07:00:00',
    // to: '2024-03-06T16:00:00',
    duration: 9,
    rate: 82,
    remark: 'Temperature control issue',
  },
  {
    id: 8,
    desc: 'Equipment Calibration',
    product: 'Product H',
    // from: '2024-03-10T09:00:00',
    // to: '2024-03-11T16:00:00',
    duration: 7,
    rate: 89,
    remark: 'Routine calibration',
  },
  {
    id: 9,
    desc: 'Employee Strike',
    product: 'Product I',
    // from: '2024-03-15T00:00:00',
    // to: '2024-03-16T14:00:00',
    duration: 14,
    rate: 75,
    remark: 'Workforce protest',
  },
  {
    id: 10,
    desc: 'Logistics Delay',
    product: 'Product J',
    // from: '2024-03-20T11:00:00',
    // to: '2024-03-21T22:00:00',
    duration: 11,
    rate: 78,
    remark: 'Transport issue',
  },
  {
    id: 11,
    desc: 'Training Session',
    product: 'Product K',
    // from: '2024-03-25T09:00:00',
    // to: '2024-03-25T17:00:00',
    duration: 8,
    rate: 95,
    remark: 'Employee training',
  },
  {
    id: 12,
    desc: 'System Overhaul',
    product: 'Product L',
    // from: '2024-03-28T10:00:00',
    // to: '2024-03-29T18:00:00',
    duration: 16,
    rate: 90,
    remark: 'Complete system overhaul',
  },
]

const SlowDown = () => (
  <div>
    <ASDataGrid
      columns={slowdownColumns}
      rows={slowdownData}
      title='Slowdown Records'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[100, 200, 300]}
    />
  </div>
)

export default SlowDown
