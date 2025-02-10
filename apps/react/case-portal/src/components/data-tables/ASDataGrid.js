import { useState, useMemo } from 'react'
import { DataGrid } from '@mui/x-data-grid'
import {
  Button,
  TextField,
  Menu,
  MenuItem,
  IconButton,
  Typography,
  Box,
  InputAdornment,
  Modal
} from '@mui/material'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import SearchIcon from '@mui/icons-material/Search'
import FilterAltIcon from '@mui/icons-material/FilterAlt'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'



const jioColors = {
  primaryBlue: '#1B4E9B',
  accentRed: '#E31C3D',
  background: '#FFFFFF',
  headerBg: '#DAE0EF',
  rowEven: '#FFFFFF',
  rowOdd: '#FFFFFF',
  textPrimary: '#2D2D2D',
  border: '#D0D0D0',
}

const DataGridTable = ({
  columns: initialColumns = [],
  rows: initialRows = [],
  title = 'Turnaround Plan Details',
  onAddRow,
  onDeleteRow,
  onRowUpdate,
  paginationOptions = [10, 20, 30],
}) => {
  const [rows, setRows] = useState(initialRows)
  const [searchText, setSearchText] = useState('')
  const [anchorEl, setAnchorEl] = useState(null)
  const [selectedRow, setSelectedRow] = useState(null)
  const [isFilterActive, setIsFilterActive] = useState(false)
  const [paginationModel, setPaginationModel] = useState({page: 0,pageSize: paginationOptions[0],})
  const [resizedColumns, setResizedColumns] = useState({})
  const [open, setOpen] = useState(false);
  

  const handleSearchChange = (event) => {
    setSearchText(event.target.value)
  }

  const onColumnResized = (params) => {
    if (params.column) {
      const field = params.column.getColDef().field
      setResizedColumns((prev) => ({
        ...prev,
        [field]: true,
      }))
    }
  }

  const handleFilterClick = () => {
    setIsFilterActive(!isFilterActive)
  }

  const filteredRows = rows.filter((row) => {
  
    const matchesSearch = Object.values(row).some((value) =>
      String(value).toLowerCase().includes(searchText.toLowerCase()),
    )
    const matchesDuration = !isFilterActive || row.durationHrs > 100
    return matchesSearch && matchesDuration
  })

  const handleMenuClick = (event, row) => {
    setAnchorEl(event.currentTarget)
    setSelectedRow(row)
  }

  const handleMenuClose = () => {
    setAnchorEl(null)
    setSelectedRow(null)
  }

  const handleDeleteRow = (id) => {
    const updatedRows = rows.filter((row) => row?.id !== id)
    setRows(updatedRows)
    onDeleteRow?.(id) // Call the onDeleteRow prop if provided
    handleMenuClose()
  }

  const handleEditRow = (id) => {
    // Implement your edit row logic here
    console.log(`Edit row with id: ${id}`)
    handleMenuClose()
  }

  const processRowUpdate = (newRow) => {
    const updatedRow = { ...newRow, isNew: false }
    const updatedRows = rows.map((row) =>
      row?.id === newRow?.id ? updatedRow : row,
    )
    setRows(updatedRows)
    onRowUpdate?.(updatedRow) // Call the onRowUpdate prop if provided
    return updatedRow
  }

  const handleAddRow = () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])),
    }
    const updatedRows = [...rows, newRow]
    setRows(updatedRows)
    onAddRow?.(newRow)
  }

  const defaultColumns = useMemo(() => {
    return initialColumns.map((col) => ({
      ...col,
      flex: !resizedColumns[col.field] ? 1 : undefined,
    }))
  }, [initialColumns, resizedColumns])

  const columns = [
    ...defaultColumns,
    {
      field: 'actions',
      headerName: 'Actions',
      width: 180,
      cellClassName: 'with-border',
      textAlign: 'center',
      renderCell: (params) => (
        <>
          <IconButton
            onClick={(event) => handleMenuClick(event, params.row)}
            aria-label='more'
            aria-controls='long-menu'
            aria-haspopup='true'
          >
            <MoreVertIcon />
          </IconButton>
          <Menu
            id='long-menu'
            anchorEl={anchorEl}
            keepMounted
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            sx={{ boxShadow: 'none', '&:focus': { boxShadow: 'none' } }}
          >
            <MenuItem
              onClick={() => handleEditRow(selectedRow?.id)}
              sx={{
                boxShadow: 'none',
                '&:focus': { boxShadow: 'none' },
                display: 'flex',
                alignItems: 'center',
                gap: 1, // Adds spacing between icon and text
              }}
            >
              <EditIcon sx={{ color: jioColors.primaryBlue }} />
              Edit
            </MenuItem>

            {/* Delete Option */}
            <MenuItem
              onClick={() => handleDeleteRow(selectedRow?.id)}
              sx={{
                boxShadow: 'none',
                '&:focus': { boxShadow: 'none' },
                display: 'flex',
                alignItems: 'center',
                gap: 1, // Adds spacing between icon and text
              }}
            >
              <DeleteIcon sx={{ color: jioColors.primaryBlue }} />
              Delete
            </MenuItem>
          </Menu>
        </>
      ),

      flex: 1,
      headerClassName: 'last-column-header',
      // cellClassName: 'last-column-cell',
    },
  ]

  const handleCellClick = (params) => {
    setSelectedRow(params.row);
    setOpen(true);
  };

  
  return (
    <Box
      sx={{
        height: '81vh',
        width: '100%',
        padding: 2,
        backgroundColor: '#fff',
        borderRadius: 2,
        borderBottom: 'none',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: 1,
        }}
      >
        <Typography
          sx={{
            color: '#040510',
            fontSize: '1.5rem',
            fontWeight: 300,
            letterSpacing: '0.5px',
          }}
        >
          {title}
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginTop: 2,
          marginBottom: 1,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton
            aria-label='filter'
            onClick={handleFilterClick}
            sx={{
              border: `1px solid ${jioColors.border}`,
              borderRadius: 1,
              padding: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              backgroundColor: isFilterActive
                ? jioColors.primaryBlue
                : 'inherit',
              color: isFilterActive ? 'inherit' : 'inherit',
              width: '150px',
            }}
          >
            <FilterAltIcon
              color={isFilterActive ? jioColors.background : 'inherit'}
            />
            <span
              style={{
                fontSize: '0.875rem',
                color: isFilterActive ? '#ffffff' : '#2A3ACD',
              }}
            >
              Filter
            </span>
          </IconButton>

          <TextField
            variant='outlined'
            placeholder='Search...'
            value={searchText}
            onChange={handleSearchChange}
            sx={{
              width: '250px',
              borderRadius: 1,
              backgroundColor: jioColors.background,
              color: '#8A9BC2',
            }}
            InputProps={{
              endAdornment: (
                <InputAdornment position='start'>
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Typography
            sx={{ marginRight: 1, fontWeight: 300, color: '#8A9BC2' }}
          >
            Show:
          </Typography>
          <select
            value={paginationModel.pageSize}
            onChange={(e) => {
              const newSize = Number(e.target.value)
              setPaginationModel({
                ...paginationModel,
                pageSize: newSize,
                page: 0,
              })
            }}
            style={{ padding: '4px' }}
          >
            {paginationOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <Typography sx={{ marginLeft: 1, fontWeight: 300, color: '#8A9BC2' }}>
            Entries
          </Typography>
        </Box>
      </Box>

      <Box sx={{ height: 'calc(100% - 150px)', width: '100%' }}>
        <DataGrid
          rows={filteredRows}
          columns={columns}
          rowHeight={35}
          processRowUpdate={processRowUpdate}
          paginationModel={paginationModel}
          onPaginationModelChange={(model) => setPaginationModel(model)}
          rowsPerPageOptions={paginationOptions}
          onColumnResized={onColumnResized}
          onCellClick={handleCellClick}
          pagination
          disableColumnResize
          disableSelectionOnClick
          getRowClassName={(params) =>
            params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
          }

          sx={{
            borderRadius: '4px',
            border: `1px solid ${jioColors.border}`,
            fontSize: '0.8rem',
            '& .MuiDataGrid-root .MuiDataGrid-cell': {
              fontSize: '0.8rem',
              color: '#A9A9A9',
            },
            '& .MuiDataGrid-columnHeaders': {
              backgroundColor: '#F2F3F8',
              color: '#3E4E75',
              fontSize: '0.8rem',
              fontWeight: 600,
              borderBottom: `2px solid ${'#DAE0EF'}`,
              borderTopLeftRadius: '14px',
              borderTopRightRadius: '14px',
            },
            '& .MuiDataGrid-cell': {
              borderRight: `none`,
              borderBottom: `1px solid ${'#DAE0EF'}`,
              color: '#3E4E75',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              fontSize: '0.8rem',
            },
            '& .MuiDataGrid-row': {
              borderBottom: `1px solid ${jioColors.border}`,
            },
            '& .even-row': {
              backgroundColor: jioColors.rowEven,
            },
            '& .odd-row': {
              backgroundColor: jioColors.rowOdd,
            },
            '& .MuiDataGrid-toolbarContainer': {
              display: 'flex',
              justifyContent: 'flex-end',
              gap: 1,
              paddingRight: 2,
              alignSelf: 'flex-end',
            },
            '& .MuiDataGrid-columnHeaders .last-column-header': {
              paddingRight: '16px', // Add padding to the header
            },
            '& .MuiDataGrid-cell.last-column-cell': {
              paddingRight: '16px', // Add padding to the cells in the actions column
            },
          }}
        />
      </Box>

      <Button
        variant='contained'
        sx={{
          marginTop: 2,
          backgroundColor: jioColors.primaryBlue,
          color: jioColors.background,
          borderRadius: 1,
          padding: '8px 24px',
          textTransform: 'none',
          fontSize: '0.875rem',
          fontWeight: 500,
          '&:hover': {
            backgroundColor: '#143B6F',
            boxShadow: 'none',
          },
        }}
        onClick={handleAddRow}
      >
        Add Item
      </Button>

      <Modal open={open} onClose={() => setOpen(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            width: 400,
            bgcolor: "background.paper",
            p: 4,
            boxShadow: 24,
            borderRadius: 2,
          }}
        >
          <Typography variant="h6">Row Details</Typography>
          {selectedRow && (
            <Typography variant="body1">
              {JSON.stringify(selectedRow, null, 2)}
            </Typography>
          )}
        </Box>
      </Modal>

    </Box>
  )
}

export default DataGridTable
