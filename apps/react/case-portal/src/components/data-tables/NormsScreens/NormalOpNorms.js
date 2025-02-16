// import DataGridTable from '../ASDataGrid'

// const productionColumns = [
//   { field: 'srNo', headerName: 'Sr. No', width: 80, editable: false },
//   {
//     field: 'particulars',
//     headerName: 'Particulars',
//     width: 200,
//     editable: true,
//   },
//   { field: 'unit', headerName: 'Unit', width: 100, editable: true },
//   { field: 'apr24', headerName: 'Apr-24', width: 100, editable: true },
//   { field: 'may24', headerName: 'May-24', width: 100, editable: true },
//   { field: 'jun24', headerName: 'Jun-24', width: 100, editable: true },
//   { field: 'jul24', headerName: 'Jul-24', width: 100, editable: true },
//   { field: 'aug24', headerName: 'Aug-24', width: 100, editable: true },
//   { field: 'sep24', headerName: 'Sep-24', width: 100, editable: true },
//   { field: 'oct24', headerName: 'Oct-24', width: 100, editable: true },
//   { field: 'nov24', headerName: 'Nov-24', width: 100, editable: true },
//   { field: 'dec24', headerName: 'Dec-24', width: 100, editable: true },
//   { field: 'jan25', headerName: 'Jan-25', width: 100, editable: true },
//   { field: 'feb25', headerName: 'Feb-25', width: 100, editable: true },
//   { field: 'mar25', headerName: 'Mar-25', width: 100, editable: true },
//   { field: 'remark', headerName: 'Remark', width: 200, editable: true },
// ]

// const productionData = [
//   {
//     id: 1,
//     srNo: 1,
//     particulars: 'Material A',
//     unit: 'Kg',
//     apr24: 50,
//     may24: 60,
//     jun24: 55,
//     jul24: 70,
//     aug24: 65,
//     sep24: 75,
//     oct24: 80,
//     nov24: 85,
//     dec24: 90,
//     jan25: 95,
//     feb25: 85,
//     mar25: 100,
//     remark: 'Stock Updated',
//   },
//   {
//     id: 2,
//     srNo: 2,
//     particulars: 'Material B',
//     unit: 'Litre',
//     apr24: 30,
//     may24: 40,
//     jun24: 35,
//     jul24: 45,
//     aug24: 50,
//     sep24: 55,
//     oct24: 60,
//     nov24: 65,
//     dec24: 70,
//     jan25: 75,
//     feb25: 65,
//     mar25: 80,
//     remark: 'Reorder Needed',
//   },
//   {
//     id: 3,
//     srNo: 3,
//     particulars: 'Material C',
//     unit: 'Pcs',
//     apr24: 100,
//     may24: 120,
//     jun24: 110,
//     jul24: 130,
//     aug24: 125,
//     sep24: 135,
//     oct24: 140,
//     nov24: 145,
//     dec24: 150,
//     jan25: 155,
//     feb25: 145,
//     mar25: 160,
//     remark: 'Sufficient Stock',
//   },
//   {
//     id: 4,
//     srNo: 4,
//     particulars: 'Material D',
//     unit: 'Kg',
//     apr24: 20,
//     may24: 25,
//     jun24: 30,
//     jul24: 35,
//     aug24: 30,
//     sep24: 40,
//     oct24: 45,
//     nov24: 50,
//     dec24: 55,
//     jan25: 60,
//     feb25: 50,
//     mar25: 65,
//     remark: 'Check Expiry',
//   },
//   {
//     id: 5,
//     srNo: 5,
//     particulars: 'Material E',
//     unit: 'Box',
//     apr24: 5,
//     may24: 10,
//     jun24: 15,
//     jul24: 20,
//     aug24: 25,
//     sep24: 30,
//     oct24: 35,
//     nov24: 40,
//     dec24: 45,
//     jan25: 50,
//     feb25: 40,
//     mar25: 55,
//     remark: 'New Shipment Arrived',
//   },
//   {
//     id: 6,
//     srNo: 6,
//     particulars: 'Material F',
//     unit: 'Tonne',
//     apr24: 15,
//     may24: 20,
//     jun24: 18,
//     jul24: 25,
//     aug24: 22,
//     sep24: 28,
//     oct24: 30,
//     nov24: 32,
//     dec24: 35,
//     jan25: 38,
//     feb25: 34,
//     mar25: 40,
//     remark: 'Monitor Usage',
//   },
//   {
//     id: 7,
//     srNo: 7,
//     particulars: 'Material G',
//     unit: 'Meter',
//     apr24: 200,
//     may24: 220,
//     jun24: 210,
//     jul24: 250,
//     aug24: 230,
//     sep24: 270,
//     oct24: 280,
//     nov24: 290,
//     dec24: 300,
//     jan25: 310,
//     feb25: 290,
//     mar25: 320,
//     remark: 'Stable Supply',
//   },
//   {
//     id: 8,
//     srNo: 8,
//     particulars: 'Material H',
//     unit: 'Kg',
//     apr24: 12,
//     may24: 18,
//     jun24: 16,
//     jul24: 20,
//     aug24: 22,
//     sep24: 24,
//     oct24: 26,
//     nov24: 28,
//     dec24: 30,
//     jan25: 32,
//     feb25: 28,
//     mar25: 35,
//     remark: 'Low Demand',
//   },
//   {
//     id: 9,
//     srNo: 9,
//     particulars: 'Material I',
//     unit: 'Litre',
//     apr24: 55,
//     may24: 60,
//     jun24: 58,
//     jul24: 70,
//     aug24: 68,
//     sep24: 75,
//     oct24: 80,
//     nov24: 85,
//     dec24: 90,
//     jan25: 95,
//     feb25: 88,
//     mar25: 100,
//     remark: 'Reorder Soon',
//   },
//   {
//     id: 10,
//     srNo: 10,
//     particulars: 'Material J',
//     unit: 'Box',
//     apr24: 8,
//     may24: 12,
//     jun24: 10,
//     jul24: 15,
//     aug24: 14,
//     sep24: 18,
//     oct24: 20,
//     nov24: 22,
//     dec24: 24,
//     jan25: 26,
//     feb25: 23,
//     mar25: 28,
//     remark: 'New Variant Available',
//   },
// ]

// const NormalOpNormsScreen = () => (
//   <div>
//     <DataGridTable
//       columns={productionColumns}
//       rows={productionData}
//       title='Normal operations Norms'
//       onAddRow={(newRow) => console.log('New Row Added:', newRow)}
//       onDeleteRow={(id) => console.log('Row Deleted:', id)}
//       onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
//       paginationOptions={[10, 20, 30]}
//     />
//   </div>
// )

// export default NormalOpNormsScreen

import DataGridTable from '../ASDataGrid'

// Define columns as usual
const productionColumns = [
  { field: 'srNo', headerName: 'Sr. No', width: 80, editable: false, flex: 2 },
  {
    field: 'particulars',
    headerName: 'Particulars',
    width: 200,
    editable: true,
  },
  { field: 'unit', headerName: 'Unit', width: 100, editable: true },
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
  { field: 'remark', headerName: 'Remark', width: 200, editable: true },
]

// Sample production data (10 rows)
const productionData = [
  {
    id: 1,
    srNo: 1,
    particulars: 'Material A',
    unit: 'Kg',
    apr24: 50,
    may24: 60,
    jun24: 55,
    jul24: 70,
    aug24: 65,
    sep24: 75,
    oct24: 80,
    nov24: 85,
    dec24: 90,
    jan25: 95,
    feb25: 85,
    mar25: 100,
    remark: 'Stock Updated',
  },
  {
    id: 2,
    srNo: 2,
    particulars: 'Material B',
    unit: 'Litre',
    apr24: 30,
    may24: 40,
    jun24: 35,
    jul24: 45,
    aug24: 50,
    sep24: 55,
    oct24: 60,
    nov24: 65,
    dec24: 70,
    jan25: 75,
    feb25: 65,
    mar25: 80,
    remark: 'Reorder Needed',
  },
  {
    id: 3,
    srNo: 3,
    particulars: 'Material C',
    unit: 'Pcs',
    apr24: 100,
    may24: 120,
    jun24: 110,
    jul24: 130,
    aug24: 125,
    sep24: 135,
    oct24: 140,
    nov24: 145,
    dec24: 150,
    jan25: 155,
    feb25: 145,
    mar25: 160,
    remark: 'Sufficient Stock',
  },
  {
    id: 4,
    srNo: 4,
    particulars: 'Material D',
    unit: 'Kg',
    apr24: 20,
    may24: 25,
    jun24: 30,
    jul24: 35,
    aug24: 30,
    sep24: 40,
    oct24: 45,
    nov24: 50,
    dec24: 55,
    jan25: 60,
    feb25: 50,
    mar25: 65,
    remark: 'Check Expiry',
  },
  {
    id: 5,
    srNo: 5,
    particulars: 'Material E',
    unit: 'Box',
    apr24: 5,
    may24: 10,
    jun24: 15,
    jul24: 20,
    aug24: 25,
    sep24: 30,
    oct24: 35,
    nov24: 40,
    dec24: 45,
    jan25: 50,
    feb25: 40,
    mar25: 55,
    remark: 'New Shipment Arrived',
  },
  {
    id: 6,
    srNo: 6,
    particulars: 'Material F',
    unit: 'Tonne',
    apr24: 15,
    may24: 20,
    jun24: 18,
    jul24: 25,
    aug24: 22,
    sep24: 28,
    oct24: 30,
    nov24: 32,
    dec24: 35,
    jan25: 38,
    feb25: 34,
    mar25: 40,
    remark: 'Monitor Usage',
  },
  {
    id: 7,
    srNo: 7,
    particulars: 'Material G',
    unit: 'Meter',
    apr24: 200,
    may24: 220,
    jun24: 210,
    jul24: 250,
    aug24: 230,
    sep24: 270,
    oct24: 280,
    nov24: 290,
    dec24: 300,
    jan25: 310,
    feb25: 290,
    mar25: 320,
    remark: 'Stable Supply',
  },
  {
    id: 8,
    srNo: 8,
    particulars: 'Material H',
    unit: 'Kg',
    apr24: 12,
    may24: 18,
    jun24: 16,
    jul24: 20,
    aug24: 22,
    sep24: 24,
    oct24: 26,
    nov24: 28,
    dec24: 30,
    jan25: 32,
    feb25: 28,
    mar25: 35,
    remark: 'Low Demand',
  },
  {
    id: 9,
    srNo: 9,
    particulars: 'Material I',
    unit: 'Litre',
    apr24: 55,
    may24: 60,
    jun24: 58,
    jul24: 70,
    aug24: 68,
    sep24: 75,
    oct24: 80,
    nov24: 85,
    dec24: 90,
    jan25: 95,
    feb25: 88,
    mar25: 100,
    remark: 'Reorder Soon',
  },
  {
    id: 10,
    srNo: 10,
    particulars: 'Material J',
    unit: 'Box',
    apr24: 8,
    may24: 12,
    jun24: 10,
    jul24: 15,
    aug24: 14,
    sep24: 18,
    oct24: 20,
    nov24: 22,
    dec24: 24,
    jan25: 26,
    feb25: 23,
    mar25: 28,
    remark: 'New Variant Available',
  },
]

// Create groups by inserting a row with a groupHeader property
const rawMaterialsData = productionData.slice(0, 2) // 2 rows for Raw Materials
const byProductsData = productionData.slice(2, 5) // 3 rows for By Products
const calChemData = productionData.slice(5, 9) // 1 row for Cal-chem

const groupedRows = [
  { id: 'group-raw', groupHeader: 'Raw Materials' },
  ...rawMaterialsData,
  { id: 'group-by', groupHeader: 'By Products' },
  ...byProductsData,
  { id: 'group-cal', groupHeader: 'Cat-chem' },
  ...calChemData,
]

// Custom render function for cells
const groupRenderCell = (params) => {
  if (params.row.groupHeader) {
    // In the first column show the group title
    if (params.field === 'srNo') {
      return (
        <span
          style={{
            fontWeight: 'bold',
            padding: '4px 8px',
          }}
        >
          {params.row.groupHeader}
        </span>
      )
    }
    // For other columns, render empty
    return ''
  }
  return params.value
}

// Enhance columns to use the custom render function
const enhancedColumns = productionColumns.map((col) => ({
  ...col,
  renderCell: groupRenderCell,
}))

const NormalOpNormsScreen = () => {
  return (
    <div>
      <DataGridTable
        columns={enhancedColumns}
        rows={groupedRows}
        title='Normal Operations Norms'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[10, 20, 30]}
        getRowClassName={(params) =>
          params.row.groupHeader ? 'group-header-row' : ''
        }
        sx={{
          '& .group-header-row .MuiDataGrid-cell': {
            borderRight: 'none !important',
          },
          '& .MuiDataGrid-row.MuiDataGrid-row--firstVisible:nth-child(even) .MuiDataGrid-cell':
            {
              borderRight: 'none !important',
            },
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
