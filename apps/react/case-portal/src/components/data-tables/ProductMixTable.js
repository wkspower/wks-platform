import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
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
      const isEditable = id > 10 // Enable only for rows beyond 10

      return (
        <Autocomplete
          options={productOptions}
          value={params.value || ''}
          disableClearable // Prevent clearing the selection
          onChange={(event, newValue) => {
            params.api.setEditCellValue({
              id: params.id,
              field: 'product',
              value: newValue,
            })
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
    apr24: 100,
    may24: 150,
    jun24: 200,
    jul24: 250,
    aug24: 300,
    sep24: 350,
    oct24: 400,
    nov24: 450,
    dec24: 500,
    jan25: 550,
    feb25: 600,
    mar25: 650,
    averageTPH: 425,
    remark: 'Good performance',
  },
  {
    id: 2,
    product: 'Product B',
    apr24: 200,
    may24: 250,
    jun24: 300,
    jul24: 350,
    aug24: 400,
    sep24: 450,
    oct24: 500,
    nov24: 550,
    dec24: 600,
    jan25: 650,
    feb25: 700,
    mar25: 750,
    averageTPH: 525,
    remark: 'Excellent performance',
  },
  {
    id: 3,
    product: 'Product C',
    apr24: 300,
    may24: 350,
    jun24: 400,
    jul24: 450,
    aug24: 500,
    sep24: 550,
    oct24: 600,
    nov24: 650,
    dec24: 700,
    jan25: 750,
    feb25: 800,
    mar25: 850,
    averageTPH: 625,
    remark: 'High demand',
  },
  {
    id: 4,
    product: 'Product D',
    apr24: 80,
    may24: 120,
    jun24: 160,
    jul24: 200,
    aug24: 240,
    sep24: 280,
    oct24: 320,
    nov24: 360,
    dec24: 400,
    jan25: 440,
    feb25: 480,
    mar25: 520,
    averageTPH: 300,
    remark: 'Stable performance',
  },
  {
    id: 5,
    product: 'Product E',
    apr24: 120,
    may24: 140,
    jun24: 160,
    jul24: 180,
    aug24: 200,
    sep24: 220,
    oct24: 240,
    nov24: 260,
    dec24: 280,
    jan25: 300,
    feb25: 320,
    mar25: 340,
    averageTPH: 210,
    remark: 'Moderate performance',
  },
  {
    id: 6,
    product: 'Product F',
    apr24: 250,
    may24: 300,
    jun24: 350,
    jul24: 400,
    aug24: 450,
    sep24: 500,
    oct24: 550,
    nov24: 600,
    dec24: 650,
    jan25: 700,
    feb25: 750,
    mar25: 800,
    averageTPH: 575,
    remark: 'Consistent growth',
  },
  {
    id: 7,
    product: 'Product G',
    apr24: 60,
    may24: 90,
    jun24: 120,
    jul24: 150,
    aug24: 180,
    sep24: 210,
    oct24: 240,
    nov24: 270,
    dec24: 300,
    jan25: 330,
    feb25: 360,
    mar25: 390,
    averageTPH: 225,
    remark: 'Low but steady',
  },
  {
    id: 8,
    product: 'Product H',
    apr24: 180,
    may24: 200,
    jun24: 220,
    jul24: 240,
    aug24: 260,
    sep24: 280,
    oct24: 300,
    nov24: 320,
    dec24: 340,
    jan25: 360,
    feb25: 380,
    mar25: 400,
    averageTPH: 290,
    remark: 'Growing steadily',
  },
  {
    id: 9,
    product: 'Product I',
    apr24: 150,
    may24: 180,
    jun24: 210,
    jul24: 240,
    aug24: 270,
    sep24: 300,
    oct24: 330,
    nov24: 360,
    dec24: 390,
    jan25: 420,
    feb25: 450,
    mar25: 480,
    averageTPH: 315,
    remark: 'Steady increase',
  },
  {
    id: 10,
    product: 'Product J',
    apr24: 130,
    may24: 160,
    jun24: 190,
    jul24: 220,
    aug24: 250,
    sep24: 280,
    oct24: 310,
    nov24: 340,
    dec24: 370,
    jan25: 400,
    feb25: 430,
    mar25: 460,
    averageTPH: 295,
    remark: 'Gradual growth',
  },
]

const ProductMixTable = () => (
  // <ASDataGrid
  //   columns={columns}
  //   rows={rows}
  //   pageSize={5}
  //   rowsPerPageOptions={[5, 10, 20]}
  //   checkboxSelection
  //   disableSelectionOnClick={false}
  //   cNameToAddAvatar='name'
  //   exportFileName='MyDataGridExport'
  //   columnVisibilityModel={{ id: true, name: true, age: false, status: true }}
  //   defaultSortModel={[{ field: 'name', sort: 'asc' }]}
  //   handleSortModelChange={(model) => console.log('Sort Model Changed:', model)}
  //   handleCellClick={(params) => console.log('Cell Clicked:', params)}
  //   loader={false}
  //   filterOptionsForRoles={[
  //     { value: 'Admin', label: 'Admin' },
  //     { value: 'User', label: 'User' },
  //   ]}
  // />
  <div>
    <ASDataGrid
      columns={productionColumns}
      rows={productionData}
      title='Product Mix Table'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[10, 20, 30]}
      // title='User Table'
      // initialRows={userData}
      // columns={userColumns}
      // filterConfig={{ durationHrs: 100 }} // Adjust filter config if needed
      // pageSizeOptions={[5, 10, 20]}
    />
  </div>
)

export default ProductMixTable
