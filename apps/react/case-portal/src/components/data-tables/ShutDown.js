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
const productionColumns = [
  {
    field: 'shutdown',
    headerName: 'Shutdown Desc',
    // width: 150,
    minWidth: 200,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        <div>Shutdown Desc</div>
        {/* <div>Desc</div> */}
      </div>
    ),
    flex: 3,
  },
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
    field: 'durationHrs',
    headerName: 'Duration Hrs',
    width: 120,
    editable: true,
    type: 'number',
    flex: 0.5,
    textAlign: 'center',
    justifyContent: 'center',
    alignItems: 'center',
  },
]

const shutdownData = [
  {
    id: 1,
    shutdown: 'Routine Maintenance',
    product: 'Product A',
    // taFrom: new Date('2024-04-01T02:30:00'),
    // taTo: new Date('2024-04-02T01:02:00'),
    durationHrs: 24,
  },
  {
    id: 2,
    shutdown: 'Emergency Repair',
    product: 'Product B',
    // taFrom: '2024-05-10',
    // taTo: '2024-05-11',
    durationHrs: 18,
  },
  {
    id: 3,
    shutdown: 'System Upgrade',
    product: 'Product C',
    // taFrom: '2024-06-15',
    // taTo: '2024-06-16',
    durationHrs: 30,
  },
  {
    id: 4,
    shutdown: 'Scheduled Cleaning',
    product: 'Product D',
    // taFrom: '2024-07-05',
    // taTo: '2024-07-06',
    durationHrs: 12,
  },
  {
    id: 5,
    shutdown: 'Machine Calibration',
    product: 'Product E',
    // taFrom: '2024-08-20',
    // taTo: '2024-08-21',
    durationHrs: 20,
  },
  {
    id: 6,
    shutdown: 'Software Patch Update',
    product: 'Product F',
    // taFrom: '2024-09-10',
    // taTo: '2024-09-11',
    durationHrs: 10,
  },
  {
    id: 7,
    shutdown: 'Network Maintenance',
    product: 'Product G',
    // taFrom: '2024-10-15',
    // taTo: '2024-10-16',
    durationHrs: 16,
  },
  {
    id: 8,
    shutdown: 'Cooling System Check',
    product: 'Product H',
    // taFrom: '2024-11-25',
    // taTo: '2024-11-26',
    durationHrs: 22,
  },
  {
    id: 9,
    shutdown: 'Hardware Replacement',
    product: 'Product I',
    // taFrom: '2024-12-05',
    // taTo: '2024-12-06',
    durationHrs: 28,
  },
  {
    id: 10,
    shutdown: 'Factory Power Audit',
    product: 'Product J',
    // taFrom: '2025-01-15',
    // taTo: '2025-01-16',
    durationHrs: 14,
  },
]

const ShutDown = () => (
  <div>
    <ASDataGrid
      columns={productionColumns}
      rows={shutdownData}
      title='Shutdown Plan Data'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[100, 200, 300]}
    />
  </div>
)

export default ShutDown
