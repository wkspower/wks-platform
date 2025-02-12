import { MenuItem, Select } from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'

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
    field: 'product',
    headerName: 'Product',
    width: 150,
    editable: true,
    renderEditCell: (params) => {
      console.log(params)
      const { id } = params
      const isEditable = id > 10

      return (
        <Select
          value={params?.value}
          onChange={(e) =>
            params.api.setEditCellValue({
              id: params?.id,
              field: 'product',
              value: e.target.value,
            })
          }
          disabled={!isEditable}
          fullWidth
        >
          {productOptions.map((option) => (
            <MenuItem key={option} value={option}>
              {option}
            </MenuItem>
          ))}
        </Select>
      )
    },
  },
  { field: 'apr24', headerName: 'Apr-24', width: 100, editable: true },
  { field: 'may24', headerName: 'May-24', width: 100, editable: true },
  { field: 'jun24', headerName: 'Jun-24', width: 100, editable: true },
  { field: 'jul24', headerName: 'Jul-24', width: 100, editable: true },
  { field: 'aug24', headerName: 'Aug-24', width: 100, editable: true },
  { field: 'sep24', headerName: 'Sep-24', width: 100, editable: true },
  { field: 'oct24', headerName: 'Oct-24', width: 100, editable: true },
  { field: 'nov24', headerName: 'Nov-24', width: 100, editable: true },
  { field: 'dec24', headerName: 'Dec-24', width: 100, editable: true },
  { field: 'jan25', headerName: 'Jan-25', width: 100, editable: true },
  { field: 'feb25', headerName: 'Feb-25', width: 100, editable: true },
  { field: 'mar25', headerName: 'Mar-25', width: 100, editable: true },
  {
    field: 'averageTPH',
    headerName: 'Average TPH',
    width: 150,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        <div>Average</div>
        <div>TPH</div>
      </div>
    ),
  },
  { field: 'remark', headerName: 'Remark', width: 200, editable: true },
]

const productionData = [
  {
    id: 1,
    product: 'Product A',
    apr24: 50,
    may24: 55,
    jun24: 60,
    jul24: 65,
    aug24: 70,
    sep24: 75,
    oct24: 80,
    nov24: 85,
    dec24: 90,
    jan25: 95,
    feb25: 100,
    mar25: 105,
    averageTPH: 70,
    remark: 'Slight slowdown due to maintenance',
  },
  {
    id: 2,
    product: 'Product B',
    apr24: 40,
    may24: 45,
    jun24: 50,
    jul24: 55,
    aug24: 60,
    sep24: 65,
    oct24: 70,
    nov24: 75,
    dec24: 80,
    jan25: 85,
    feb25: 90,
    mar25: 95,
    averageTPH: 60,
    remark: 'Reduced production due to raw material shortage',
  },
  {
    id: 3,
    product: 'Product C',
    apr24: 30,
    may24: 35,
    jun24: 40,
    jul24: 45,
    aug24: 50,
    sep24: 55,
    oct24: 60,
    nov24: 65,
    dec24: 70,
    jan25: 75,
    feb25: 80,
    mar25: 85,
    averageTPH: 50,
    remark: 'Temporary slowdown due to equipment malfunction',
  },
  {
    id: 4,
    product: 'Product D',
    apr24: 60,
    may24: 65,
    jun24: 70,
    jul24: 75,
    aug24: 80,
    sep24: 85,
    oct24: 90,
    nov24: 95,
    dec24: 100,
    jan25: 105,
    feb25: 110,
    mar25: 115,
    averageTPH: 85,
    remark: 'Reduced pace for quality assurance checks',
  },
  {
    id: 5,
    product: 'Product E',
    apr24: 55,
    may24: 60,
    jun24: 65,
    jul24: 70,
    aug24: 75,
    sep24: 80,
    oct24: 85,
    nov24: 90,
    dec24: 95,
    jan25: 100,
    feb25: 105,
    mar25: 110,
    averageTPH: 75,
    remark: 'Minor delays due to supplier issues',
  },
  {
    id: 6,
    product: 'Product F',
    apr24: 45,
    may24: 50,
    jun24: 55,
    jul24: 60,
    aug24: 65,
    sep24: 70,
    oct24: 75,
    nov24: 80,
    dec24: 85,
    jan25: 90,
    feb25: 95,
    mar25: 100,
    averageTPH: 65,
    remark: 'Slowdown from planned maintenance work',
  },
  {
    id: 7,
    product: 'Product G',
    apr24: 35,
    may24: 40,
    jun24: 45,
    jul24: 50,
    aug24: 55,
    sep24: 60,
    oct24: 65,
    nov24: 70,
    dec24: 75,
    jan25: 80,
    feb25: 85,
    mar25: 90,
    averageTPH: 55,
    remark: 'Operational slowdown due to staffing issues',
  },
  {
    id: 8,
    product: 'Product H',
    apr24: 50,
    may24: 55,
    jun24: 60,
    jul24: 65,
    aug24: 70,
    sep24: 75,
    oct24: 80,
    nov24: 85,
    dec24: 90,
    jan25: 95,
    feb25: 100,
    mar25: 105,
    averageTPH: 70,
    remark: 'Temporary slowdown due to weather-related delays',
  },
  {
    id: 9,
    product: 'Product I',
    apr24: 25,
    may24: 30,
    jun24: 35,
    jul24: 40,
    aug24: 45,
    sep24: 50,
    oct24: 55,
    nov24: 60,
    dec24: 65,
    jan25: 70,
    feb25: 75,
    mar25: 80,
    averageTPH: 45,
    remark: 'Slow production due to environmental constraints',
  },
  {
    id: 10,
    product: 'Product J',
    apr24: 65,
    may24: 70,
    jun24: 75,
    jul24: 80,
    aug24: 85,
    sep24: 90,
    oct24: 95,
    nov24: 100,
    dec24: 105,
    jan25: 110,
    feb25: 115,
    mar25: 120,
    averageTPH: 90,
    remark: 'Slowdown due to equipment upgrades',
  },
]
const SlowDown = () => (
  <div>
    <ASDataGrid
      columns={productionColumns}
      rows={productionData}
      title='Slowdown Production Data'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[10, 20, 30]}
    />
  </div>
)

export default SlowDown
