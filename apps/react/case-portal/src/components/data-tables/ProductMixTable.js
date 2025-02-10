// import { useState } from 'react'
// import { DataGrid } from '@mui/x-data-grid'
// import {
//   Button,
//   TextField,
//   InputAdornment,
//   IconButton,
//   Menu,
//   MenuItem,
// } from '@mui/material'
// import SearchIcon from '@mui/icons-material/Search'
// // import FilterListIcon from '@mui/icons-material/FilterList'
// import FilterAltIcon from '@mui/icons-material/FilterAlt'
// import MoreVertIcon from '@mui/icons-material/MoreVert'
// import { Box } from '../../../node_modules/@mui/material/index'

// const jioColors = {
//   primaryBlue: '#1B4E9B',
//   accentRed: '#E31C3D',
//   background: '#FFFFFF',
//   headerBg: '#DAE0EF',
//   rowEven: '#FFFFFF',
//   rowOdd: '#FFFFFF',
//   textPrimary: '#2D2D2D',
//   border: '#D0D0D0',
// }

// const ProductMixTable = () => {
//   const months = [
//     'Apr-24',
//     'May-24',
//     'Jun-24',
//     'Jul-24',
//     'Aug-24',
//     'Sep-24',
//     'Oct-24',
//     'Nov-24',
//     'Dec-24',
//     'Jan-25',
//     'Feb-25',
//     'Mar-25',
//   ]

//   const initialRows = [
//     {
//       id: 1,
//       gradeName: 'HDPE E52009',
//       'Apr-24': '',
//       'May-24': '',
//       'Jun-24': '',
//       'Jul-24': '',
//       'Aug-24': '',
//       'Sep-24': '',
//       'Oct-24': '',
//       'Nov-24': '',
//       'Dec-24': '',
//       'Jan-25': '',
//       'Feb-25': '',
//       'Mar-25': '',
//       averageTPH: '',
//       remark: '',
//     },
//     {
//       id: 2,
//       gradeName: 'HDPE S46005',
//       'Apr-24': '',
//       'May-24': '',
//       'Jun-24': '',
//       'Jul-24': '',
//       'Aug-24': '',
//       'Sep-24': '',
//       'Oct-24': '',
//       'Nov-24': '',
//       'Dec-24': '',
//       'Jan-25': '',
//       'Feb-25': '',
//       'Mar-25': '',
//       averageTPH: '',
//       remark: '',
//     },
//   ]

//   const [rows, setRows] = useState(initialRows)
//   const [searchText, setSearchText] = useState('')
//   const [isFilterActive, setIsFilterActive] = useState(false)
//   const [anchorEl, setAnchorEl] = useState(null)
//   const [selectedRow, setSelectedRow] = useState(null)
//   const [paginationModel, setPaginationModel] = useState({
//     page: 0,
//     pageSize: 10,
//   })
//   const handleSearchChange = (event) => {
//     setSearchText(event.target.value)
//   }
//   const handleFilterClick = () => {
//     setIsFilterActive(!isFilterActive)
//   }

//   const filteredRows = rows.filter((row) => {
//     const matchesSearch = Object.values(row).some((value) =>
//       String(value).toLowerCase().includes(searchText.toLowerCase()),
//     )
//     const matchesFilter = !isFilterActive || row.averageTPH > 0 // Example filter: averageTPH > 0
//     return matchesSearch && matchesFilter
//   })

//   const processRowUpdate = (newRow) => {
//     if (!rows?.some((row) => row?.id === newRow?.id)) {
//       console.error(`No row with id ${newRow?.id}`)
//       return newRow
//     }
//     const updatedRow = { ...newRow, isNew: false }
//     setRows(rows?.map((row) => (row?.id === newRow?.id ? updatedRow : row)))
//     return updatedRow
//   }

//   const handleAddItem = () => {
//     const newRow = {
//       id: rows.length + 1,
//       gradeName: '',
//       'Apr-24': '',
//       'May-24': '',
//       'Jun-24': '',
//       'Jul-24': '',
//       'Aug-24': '',
//       'Sep-24': '',
//       'Oct-24': '',
//       'Nov-24': '',
//       'Dec-24': '',
//       'Jan-25': '',
//       'Feb-25': '',
//       'Mar-25': '',
//       averageTPH: '',
//       remark: '',
//     }
//     setRows([...rows, newRow])
//   }

//   const handleMenuClick = (event, row) => {
//     setAnchorEl(event.currentTarget)
//     setSelectedRow(row)
//   }

//   const handleMenuClose = () => {
//     setAnchorEl(null)
//     setSelectedRow(null)
//   }

//   const handleDeleteRow = (id) => {
//     setRows((prevRows) => prevRows.filter((row) => row?.id !== id))
//     handleMenuClose()
//   }

//   const columns = [
//     {
//       field: 'gradeName',
//       headerName: 'Grade Name',
//       width: 200,
//       editable: true,
//     },
//     ...months.map((month) => ({
//       field: month,
//       headerName: month,
//       width: 120,
//       editable: true,
//     })),
//     {
//       field: 'averageTPH',
//       headerName: 'Average TPH',
//       width: 150,
//       editable: true,
//     },
//     {
//       field: 'remark',
//       headerName: 'Remark',
//       width: 200,
//       editable: true,
//     },
//     {
//       field: 'actions',
//       headerName: 'Actions',
//       width: 120,
//       textAlign: 'center',
//       renderCell: (params) => (
//         <>
//           <IconButton
//             onClick={(event) => handleMenuClick(event, params.row)}
//             aria-label='more'
//             aria-controls='long-menu'
//             aria-haspopup='true'
//           >
//             <MoreVertIcon />
//           </IconButton>
//           <Menu
//             id='long-menu'
//             anchorEl={anchorEl}
//             keepMounted
//             open={Boolean(anchorEl)}
//             onClose={handleMenuClose}
//           >
//             <MenuItem onClick={() => handleDeleteRow(selectedRow.id)}>
//               Delete
//             </MenuItem>
//             {/* Additional menu items can be added here */}
//           </Menu>
//         </>
//       ),
//     },
//   ]

//   return (
//     <div
//       style={{
//         height: '81vh',
//         width: '100%',
//         padding: 20,
//         backgroundColor: '#FAFAFB',
//         borderRadius: '8px',
//         className: 'product-mix-table',
//       }}
//     >
//       {/* Header Section */}
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           padding: '5px',
//         }}
//       >
//         <h3
//           style={{
//             color: '#040510',
//             fontSize: '1.5rem',
//             fontWeight: 600,
//             letterSpacing: '0.5px',
//             margin: 0,
//           }}
//         >
//           Product Mix Entry (%)
//         </h3>
//       </div>

//       {/* Controls (Search & Filter) */}
//       {/* <div
//         style={{
//           display: 'flex',
//           alignItems: 'center',
//           gap: '10px',
//           marginTop: '20px',
//           marginBottom: '10px',
//           justifyContent: 'flex-end',
//         }}
//       >
//         <TextField
//           variant='outlined'
//           placeholder='Search...'
//           value={searchText}
//           onChange={handleSearchChange}
//           style={{
//             width: '250px',
//             borderRadius: '8px',
//             backgroundColor: jioColors.background,
//           }}
//           InputProps={{
//             startAdornment: (
//               <InputAdornment position='start'>
//                 <SearchIcon />
//               </InputAdornment>
//             ),
//           }}
//         />
//         <IconButton
//           aria-label='filter'
//           onClick={() => setIsFilterActive(!isFilterActive)}
//         >
//           <FilterListIcon color={isFilterActive ? 'primary' : 'inherit'} />
//         </IconButton>
//       </div> */}
//       <div
//         style={{
//           display: 'flex',
//           justifyContent: 'space-between',
//           alignItems: 'center',
//           marginTop: '20px',
//           marginBottom: '10px',
//         }}
//       >
//         {/* Show entries dropdown */}
//         <div style={{ display: 'flex', alignItems: 'center' }}>
//           <label
//             style={{ marginRight: '8px', fontWeight: 'bold', color: '#8A9BC2' }}
//           >
//             Show:
//           </label>
//           <select
//             value={paginationModel.pageSize}
//             onChange={(e) => {
//               const newSize = Number(e.target.value)
//               // Update pageSize in the controlled pagination model and reset to page 0
//               setPaginationModel({
//                 ...paginationModel,
//                 pageSize: newSize,
//                 page: 0,
//               })
//             }}
//             style={{ padding: '4px' }}
//           >
//             <option value={5}>5</option>
//             <option value={10}>10</option>
//             <option value={20}>20</option>
//           </select>
//           <label
//             style={{ marginLeft: '8px', fontWeight: 'bold', color: '#8A9BC2' }}
//           >
//             Entries
//           </label>
//         </div>
//         {/* Search box and filter icon */}
//         <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
//           <TextField
//             variant='outlined'
//             placeholder='Search...'
//             value={searchText}
//             onChange={handleSearchChange}
//             style={{
//               width: '250px',
//               borderRadius: '8px',
//               backgroundColor: jioColors.background,
//             }}
//             InputProps={{
//               startAdornment: (
//                 <InputAdornment position='start'>
//                   <SearchIcon />
//                 </InputAdornment>
//               ),
//             }}
//           />
//           <IconButton aria-label='filter' onClick={handleFilterClick}>
//             <Box>
//               <FilterAltIcon color={isFilterActive ? 'primary' : 'inherit'} />
//             </Box>
//           </IconButton>
//         </div>
//       </div>

//       {/* DataGrid Section */}
//       <div
//         style={{
//           height: 'calc(100% - 150px)',
//           width: '100%',
//         }}
//       >
//         <DataGrid
//           rows={filteredRows}
//           columns={columns}
//           rowHeight={35}
//           paginationModel={paginationModel} // Use controlled pagination model
//           onPaginationModelChange={(model) => setPaginationModel(model)}
//           rowsPerPageOptions={[5, 10, 20]}
//           pagination
//           processRowUpdate={processRowUpdate}
//           disableColumnResize
//           disableSelectionOnClick
//           getRowClassName={(params) =>
//             params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
//           }
//           sx={{
//             borderRadius: '4px',
//             border: `2px solid ${jioColors.border}`,
//             '& .MuiDataGrid-columnHeaders': {
//               color: '#3E4E75',
//               fontSize: '0.875rem',
//               fontWeight: 600,
//               borderBottom: `2px solid ${jioColors.primaryBlue}`,
//             },
//             '& .MuiDataGrid-columnHeaderTitleContainer': {
//               backgroundColor: '#FAFAFC',
//             },
//             '& .MuiDataGrid-cell': {
//               borderRight: `1px solid ${jioColors.border}`,
//               borderBottom: `1px solid ${jioColors.border}`,
//               color: '#3E4E75',
//             },
//             '& .MuiDataGrid-row': {
//               borderBottom: `1px solid ${jioColors.border}`,
//             },
//             '& .even-row': {
//               backgroundColor: jioColors.rowEven,
//             },
//             '& .odd-row': {
//               backgroundColor: jioColors.rowOdd,
//             },
//             '& .MuiDataGrid-footerContainer': {
//               borderTop: `1px solid ${jioColors.border}`,
//             },
//             '& .MuiDataGrid-root': {
//               border: 'none',
//             },
//             '& .MuiDataGrid-cell:last-child': {
//               borderRight: 'none',
//             },
//             '& .MuiDataGrid-cellEmpty': {
//               display: 'none',
//             },
//           }}
//         />
//       </div>

//       {/* Add Item Button */}
//       <Button
//         variant='contained'
//         onClick={handleAddItem}
//         style={{
//           marginTop: 20,
//           backgroundColor: jioColors.primaryBlue,
//           color: jioColors.background,
//           borderRadius: '4px',
//           padding: '8px 24px',
//           textTransform: 'none',
//           fontSize: '0.875rem',
//           fontWeight: 500,
//         }}
//         sx={{
//           '&:hover': {
//             backgroundColor: '#143B6F',
//             boxShadow: 'none',
//           },
//         }}
//       >
//         Add Item
//       </Button>
//     </div>
//   )
// }

// export default ProductMixTable

import ASDataGrid from './ASDataGrid'

const productionColumns = [
  { field: 'product', headerName: 'Product', width: 150, editable: true },
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
