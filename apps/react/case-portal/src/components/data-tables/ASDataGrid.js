import React, { useState, useMemo, useEffect } from 'react'
import { DataGrid } from '@mui/x-data-grid'
import {
  Button,
  TextField,
  // Menu,
  // MenuItem,
  IconButton,
  Typography,
  Box,
  InputAdornment,
  Modal,
} from '@mui/material'
// import MoreVertIcon from '@mui/icons-material/MoreVert'
import SearchIcon from '@mui/icons-material/Search'
import FilterAltIcon from '@mui/icons-material/FilterAlt'
// import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { useSession } from 'SessionStoreContext'

import { Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material'
import { DataService } from 'services/DataService'

import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 400,
  bgcolor: 'background.paper',
  border: '2px solid #000',
  boxShadow: 24,
  p: 4,
}

const jioColors = {
  primaryBlue: '#0F3CC9',
  accentRed: '#E31C3D',
  background: '#FFFFFF',
  headerBg: '#0F3CC9',
  rowEven: '#FFFFFF',
  rowOdd: '#FFFFFF',
  textPrimary: '#2D2D2D',
  border: '#D0D0D0',
  darkTransparentBlue: 'rgba(127, 147, 206, 0.8)', // New color added
}


const DataGridTable = ({
  columns: initialColumns = [],
  rows: initialRows = [],
  title = 'Turnaround Plan Details',
  onAddRow,
  onDeleteRow,
  onRowUpdate,
  paginationOptions = [100, 200, 300],
}) => {
  const [rows, setRows] = useState(initialRows)
  const [searchText, setSearchText] = useState('')
  // const [anchorEl, setAnchorEl] = useState(null)
  // const [selectedRow, setSelectedRow] = useState(null)
  const [isFilterActive, setIsFilterActive] = useState(false)
  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: paginationOptions[0],
  })
  const [resizedColumns, setResizedColumns] = useState({})
  const [open, setOpen] = useState(false)
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)

  const handleClose1 = () => setOpen1(false)

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

  const [openYearData, setOpenYearData] = useState(false)
  const [yearData, setYearData] = useState('')

  const handleOpenYearData = () => setOpenYearData(true)
  const handleCloseYearData = () => {
    setOpenYearData(false)
    setYearData('')
  }

  const addYearData = () => {
    console.log("Year's Data:", yearData)
    handleCloseYearData()
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

  // const handleMenuClick = (event, row) => {
  //   setAnchorEl(event.currentTarget)
  //   setSelectedRow(row)
  // }

  // const handleMenuClose = () => {
  //   setAnchorEl(null)
  //   setSelectedRow(null)
  // }

  const handleDeleteRow = (id) => {
    setDeleteId(id)
    setOpen1(true)
  }

  const deleteTheRecord = () => {
    const updatedRows = rows.filter((row) => row?.id !== deleteId)
    setRows(updatedRows)
    onDeleteRow?.(deleteId)
    setDeleteId(null)
    setOpen1(false)
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
    const updatedRows = [newRow, ...rows] // Add new row at the top
    setRows(updatedRows)
    onAddRow?.(newRow)

    // Ensure the pagination stays on the current page
    setPaginationModel((prev) => ({
      ...prev,
      page: 0, // Keep the view on the first page to show the new top row
    }))
  }

  useEffect(() => {
    console.log('Current Page:', paginationModel.page)
    console.log('titile', title)
  }, [paginationModel])

  useEffect(() => {
    console.log('api call here ')
    dummyApiCall(1)
  }, [])

  const dummyApiCall = async (id) => {
    try {
      const data = await DataService.getProductById(keycloak, id)
      console.log('API Response:', data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const defaultColumns = useMemo(() => {
    return initialColumns.map((col) => ({
      ...col,
      flex: !resizedColumns[col.field] ? 1 : undefined,
    }))
  }, [initialColumns, resizedColumns])
  const columns = [
    ...defaultColumns,
    ...(title === 'Business Demand Data'
      ? [
          {
            field: 'actions',
            headerName: 'Actions',
            width: 180,
            cellClassName: 'with-border',
            headerAlign: 'center',
            align: 'center',
            renderCell: (params) => (
              <IconButton
                onClick={() => handleDeleteRow(params.row.id)}
                aria-label='delete'
              >
                <DeleteIcon sx={{ color: jioColors.accentRed }} />
              </IconButton>
            ),
            flex: 1,
            headerClassName: 'last-column-header',
          },
        ]
      : []),
  ]

  // const handleCellClick = (params) => {
  //   if (
  //     // title == 'Production Volume Data' &&
  //     params.isEditable == true &&
  //     params.field != 'product' &&
  //     params.field != 'averageTPH' &&
  //     params.field != 'remark' &&
  //     params.field != 'id' &&
  //     params.field != 'actions' &&
  //     params.field != 'isNew' &&
  //     params.field != 'taTo' &&
  //     params.field != 'taFrom' &&
  //     params.field != 'activities' &&
  //     params.field != 'durationHrs' &&
  //     params.field != 'period' &&
  //     params.value !== null && // Check if the cell is not empty
  //     params.value !== undefined &&
  //     params.value !== ''
  //   ) {
  //     // setSelectedRow(params.row)
  //     setOpen(true)
  //   }
  // }

  const [remark, setRemark] = React.useState('')
  const [openRemark, setOpenRemark] = React.useState(false)

  const handleOpenRemark = () => setOpenRemark(true)
  const handleCloseRemark = () => setOpenRemark(false)

  const addRemark = () => {
    console.log('Remark:', remark)
    setOpenRemark(false)
    setRemark('')
  }

  const handleCellClick = (params) => {
    if (title != 'Business Demand Data') {
      return
    }
    if (params?.field === 'remark') {
      setRemark(params?.value || '') // Auto-fetch the params value
      handleOpenRemark()
    } else {
      if (params.value == '' && params.field != 'product') {
        handleOpenYearData()
        return
      }

      console.log(params)

      const nonEditableFields = [
        'product',
        'averageTPH',

        'id',
        'actions',
        'isNew',
        'taTo',
        'taFrom',
        'activities',
        'durationHrs',
        'period',
      ]

      if (
        params.isEditable &&
        !nonEditableFields.includes(params.field) &&
        params.value !== null &&
        params.value !== undefined
      ) {
        const field = params.field
        const monthAbbr = field.substring(0, 3).toLowerCase()
        const yearShort = field.substring(3)
        const year = 2000 + parseInt(yearShort, 10)

        const monthMap = {
          jan: 0,
          feb: 1,
          mar: 2,
          apr: 3,
          may: 4,
          jun: 5,
          jul: 6,
          aug: 7,
          sep: 8,
          oct: 9,
          nov: 10,
          dec: 11,
        }
        const month = monthMap[monthAbbr]

        if (month === undefined) {
          console.error('Invalid month abbreviation:', monthAbbr)
          return
        }

        // Calculate days in the selected month
        const totalDays = new Date(year, month + 1, 0).getDate()
        const daysArray = Array.from({ length: totalDays }, (_, index) => {
          const date = new Date(year, month, index + 1)
          const formattedDate = date
            .toLocaleDateString('en-GB', {
              day: '2-digit',
              month: '2-digit',
              year: '2-digit',
            })
            .replace(/\//g, '-') // 'DD-MM-YY' format
          return {
            date: formattedDate,
            value: Math.floor(Math.random() * 100), // Replace with actual data if needed
          }
        })

        setDays(daysArray)

        setOpen(true)
      }
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
        borderRadius: 0,
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
          justifyContent: 'flex-end',
          alignItems: 'center',
          marginTop: 2,
          marginBottom: 1,
        }}
      >
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <TextField
            variant='outlined'
            placeholder='Search...'
            value={searchText}
            onChange={handleSearchChange}
            sx={{
              width: '300px',
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
        </Box>

        {/* <Box sx={{ display: 'flex', alignItems: 'center' }}>
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
        </Box> */}
      </Box>

      <Box sx={{ height: 'calc(100% - 150px)', width: '100%' }}>
        <DataGrid
          key={paginationModel.page}
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
          disableSelectionOnClick
          getRowClassName={(params) =>
            params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
          }
          sx={{
            borderRadius: '0px',
            border: `1px solid ${jioColors.border}`,
            fontSize: '0.8rem',
            '& .MuiDataGrid-root .MuiDataGrid-cell': {
              fontSize: '0.8rem',
              color: '#A9A9A9',
            },
            '& .MuiDataGrid-root': {
              borderRadius: '0px',
            },
            '& .MuiDataGrid-footerContainer': {
              display: 'none',
            },
            '& .MuiDataGrid-columnHeaders': {
              // backgroundColor: jioColors.headerBg,
              // color: '#FFFFFF',
              backgroundColor: '#F2F3F8',
              color: '#3E4E75',
              fontSize: '0.8rem',
              fontWeight: 600,
              borderBottom: `2px solid ${'#DAE0EF'}`,
              borderTopLeftRadius: '0px',
              borderTopRightRadius: '0px',
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

      {title === 'Business Demand Data' && (
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
      )}

      <Dialog
        open={open1}
        onClose={handleClose1}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Delete ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to delete this row?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose1}>Cancel</Button>
          <Button onClick={deleteTheRecord} autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openRemark} onClose={handleCloseRemark}>
        <DialogTitle>Add Remark</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='remark'
            label='Remark'
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '400px' }}
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseRemark}>Cancel</Button>
          <Button onClick={addRemark}>Add</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openYearData} onClose={handleCloseYearData}>
        <DialogTitle>Add Months's Data</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin='dense'
            id='yearData'
            label="Months's Data"
            type='text'
            fullWidth
            variant='outlined'
            sx={{ width: '100%', minWidth: '400px' }}
            value={yearData}
            onChange={(e) => setYearData(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseYearData}>Cancel</Button>
          <Button onClick={addYearData}>Add</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={open} onClose={handleCancel} maxWidth='xl' fullWidth>
        <DialogTitle>
          <Typography variant='h6'>Add Data for the Month</Typography>
        </DialogTitle>

        <DialogContent>
          <Box sx={{ maxHeight: '80vh', overflowX: 'auto', padding: '10px' }}>
            <Table>
              <TableHead>
                {/* Headers for Days 1-15 */}
                <TableRow>
                  {days.slice(0, 15).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{
                        textAlign: 'center',
                        fontWeight: 'bold',
                        padding: '6px',
                      }}
                    >
                      {day.date}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>

              <TableBody>
                {/* Values for Days 1-15 */}
                <TableRow>
                  {days.slice(0, 15).map((day, index) => (
                    <TableCell
                      key={index}
                      sx={{ textAlign: 'center', padding: '6px' }}
                    >
                      <TextField
                        type='number'
                        value={day.value}
                        onChange={(e) =>
                          handleValueChange(index, e.target.value)
                        }
                        size='small'
                        sx={{ width: '85px', marginTop: '2px' }}
                      />
                    </TableCell>
                  ))}
                </TableRow>

                {/* Headers for Days 16-30 */}
                <TableRow>
                  {days.slice(15).map((day, index) => (
                    <TableCell
                      key={index + 15}
                      sx={{
                        textAlign: 'center',
                        fontWeight: 'bold',
                        padding: '6px',
                      }}
                    >
                      {day.date}
                    </TableCell>
                  ))}
                </TableRow>

                {/* Values for Days 16-30 */}
                <TableRow>
                  {days.slice(15).map((day, index) => (
                    <TableCell
                      key={index + 15}
                      sx={{ textAlign: 'center', padding: '6px' }}
                    >
                      <TextField
                        type='number'
                        value={day.value}
                        onChange={(e) =>
                          handleValueChange(index + 15, e.target.value)
                        }
                        size='small'
                        sx={{ width: '85px', marginTop: '2px' }}
                      />
                    </TableCell>
                  ))}
                </TableRow>
              </TableBody>
            </Table>
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCancel} variant='outlined' sx={{ mr: 2 }}>
            Discard
          </Button>
          <Button
            onClick={handleSubmit}
            variant='contained'
            sx={{ backgroundColor: jioColors?.headerBg, color: 'white' }}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default DataGridTable
