import { useState, useMemo, useEffect } from 'react'
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
  Modal,
} from '@mui/material'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import SearchIcon from '@mui/icons-material/Search'
import FilterAltIcon from '@mui/icons-material/FilterAlt'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { useSession } from 'SessionStoreContext'

import { Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material'
import { DataService } from 'services/DataService'

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
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: paginationOptions[0],
  })
  const [resizedColumns, setResizedColumns] = useState({})
  const [open, setOpen] = useState(false)
  
  const keycloak = useSession()
  const [days, setDays] = useState([])

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

  const handleEditRow2 = (id) => {
    // Implement your edit row logic here
    console.log(`Edit row with id: ${id}`)
    handleMenuClose()
  }


  const handleEditRow = async (id) => {
    try {
      const data = await DataService.getProductById(keycloak, id);
      console.log('API Response:', data);
    } catch (error) {
      console.error('Error fetching product:', error);
    } finally {
      handleMenuClose();
    }
  };

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
    if(title=='Production Volume Data' && params.isEditable == true && params.field != 'product'){
      setSelectedRow(params.row)
      setOpen(true)
    }
  }


  useEffect(() => {
    const getDaysInMonth = () => {
      const date = new Date()
      const year = date.getFullYear()
      const month = date.getMonth()
      const totalDays = new Date(year, month + 1, 0).getDate() // Get total days in month

      const daysArray = []
      for (let day = 1; day <= totalDays; day++) {
        daysArray.push({
          date: day, // Just the day number (1, 2, 3, ...30)
          value: Math.floor(Math.random() * 100), // Random value
        })
      }
      return daysArray
    }

    setDays(getDaysInMonth())
  }, [])

  const handleSubmit = () => {
    console.log('Submitted Data:', days)
    setOpen(false) // Close the modal
  }
  
  const handleCancel = () => {
    setOpen(false) // Just close the modal
  }

  useEffect(() => {
    const getDaysInMonth = () => {
      const date = new Date()
      const year = date.getFullYear()
      const month = date.getMonth()
      const totalDays = new Date(year, month + 1, 0).getDate() // Get total number of days in the month

      return Array.from({ length: totalDays }, (_, index) => ({
        date: index + 1, // Day number (1, 2, 3, ...30)
        value: Math.floor(Math.random() * 100), // Random value
      }))
    }

    setDays(getDaysInMonth())
  }, [])

  // Handle input changes
  const handleValueChange = (index, newValue) => {
    setDays((prevDays) =>
      prevDays.map((day, i) =>
        i === index ? { ...day, value: newValue } : day,
      ),
    )
  }

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
          // columns={columns}
          columns={columns.map((col) => ({
            ...col,
            editable:
              col.field === 'product' ? rows.length >= 10 : col.editable,
          }))}
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
              paddingRight: '16px',
            },
            '& .MuiDataGrid-cell.last-column-cell': {
              paddingRight: '16px',
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

      <Modal open={open} onClose={handleCancel}>
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: '90%',
            bgcolor: 'background.paper',
            p: 4,
            boxShadow: 24,
            borderRadius: 2,
            maxHeight: '80vh',
            overflowX: 'auto',
          }}
        >
          <Typography variant='h6' sx={{ mb: 2 }}>
            Month Overview
          </Typography>

          {/* Table */}
          <Table>
            <TableHead>
              {/* Headers for Days 1-15 */}
              <TableRow>
                {days.slice(0, 15).map((day, index) => (
                  <TableCell
                    key={index}
                    sx={{ textAlign: 'center', fontWeight: 'bold' }}
                  >
                    Day {day.date}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>

            <TableBody>
              {/* Values for Days 1-15 */}
              <TableRow>
                {days.slice(0, 15).map((day, index) => (
                  <TableCell key={index} sx={{ textAlign: 'center' }}>
                    <TextField
                      type='number'
                      value={day.value}
                      onChange={(e) => handleValueChange(index, e.target.value)}
                      size='small'
                      sx={{ width: '70px' }}
                    />
                  </TableCell>
                ))}
              </TableRow>

              {/* Headers for Days 16-30 */}
              <TableRow>
                {days.slice(15).map((day, index) => (
                  <TableCell
                    key={index + 15}
                    sx={{ textAlign: 'center', fontWeight: 'bold' }}
                  >
                    Day {day.date}
                  </TableCell>
                ))}
              </TableRow>

              {/* Values for Days 16-30 */}
              <TableRow>
                {days.slice(15).map((day, index) => (
                  <TableCell key={index + 15} sx={{ textAlign: 'center' }}>
                    <TextField
                      type='number'
                      value={day.value}
                      onChange={(e) =>
                        handleValueChange(index + 15, e.target.value)
                      }
                      size='small'
                      sx={{ width: '70px' }}
                    />
                  </TableCell>
                ))}
              </TableRow>
            </TableBody>
          </Table>

          {/* Buttons */}
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
            <Button onClick={handleCancel} variant='outlined' sx={{ mr: 2 }}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} variant='contained' color='primary'>
              Submit
            </Button>
          </Box>
        </Box>
      </Modal>
    </Box>
  )
}

export default DataGridTable
