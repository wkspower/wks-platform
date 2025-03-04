import { Box, Button, IconButton, TextField, Typography } from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import * as React from 'react'
import { useEffect, useMemo, useState } from 'react'

import { GridActionsCellItem, GridRowModes } from '@mui/x-data-grid'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import { MenuItem } from '../../../node_modules/@mui/material/index'
import Notification from 'components/Utilities/Notification'
import CancelIcon from '@mui/icons-material/Close'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'

import {
  FileDownload,
  FileUpload,
} from '../../../node_modules/@mui/icons-material/index'

const jioColors = {
  primaryBlue: '#0F3CC9',
  accentRed: '#E31C3D',
  background: '#FFFFFF',
  headerBg: '#0F3CC9',
  rowEven: '#FFFFFF',
  rowOdd: '#E8F1FF',
  textPrimary: '#2D2D2D',
  border: '#D0D0D0',
  darkTransparentBlue: 'rgba(127, 147, 206, 0.8)',
}

const DataGridTable = ({
  columns: initialColumns = [],
  rows: initialRows = [],
  title = 'Turnaround Plan Details',
  onAddRow,
  onDeleteRow,
  permissions,
  processRowUpdate,
  saveChanges,
  apiRef,
  snackbarData,
  snackbarOpen,
  setSnackbarData,
  setSnackbarOpen,
  fetchData,
  handleUnitChange,
}) => {
  const [isUpdating, setIsUpdating] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [resizedColumns, setResizedColumns] = useState({})
  const [open, setOpen] = useState(false)
  const [remark, setRemark] = useState('')
  const [product, setProduct] = useState('')
  const [openRemark, setOpenRemark] = useState(false)
  const keycloak = useSession()
  const [days, setDays] = useState([])
  const [rows, setRows] = useState(initialRows)
  const [searchText, setSearchText] = useState('')
  const [isFilterActive, setIsFilterActive] = useState(false)
  const [selectedRowId, setSelectedRowId] = useState(null)
  const unitOptions = ['TPD', 'TPH']
  const [selectedUnit, setSelectedUnit] = useState()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(false)
  const handleOpenRemark = () => setOpenRemark(true)
  const handleCloseRemark = () => setOpenRemark(false)
  const handleClose1 = () => setOpen1(false)
  const handleSearchChange = (event) => {
    setSearchText(event.target.value)
  }
  const [rowModesModel, setRowModesModel] = useState({})
  const [changedRowIds, setChangedRowIds] = useState([])

  const handleRowEditCommit = (id, event) => {
    const editedRow = rows.find((row) => row.id === id)
  }
  const handleCellEditCommit = (id, event) => {}

  const handleEditClick = (id, row) => () => {
    setIsUpdating(true)
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
  }

  const handleSaveClick = (id, rowData) => {
    handleOpenRemark()
    setRowModesModel((prev) => ({
      ...prev,
      [id]: { mode: GridRowModes.View },
    }))
  }

  const handleCancelClick = (id) => () => {
    setRowModesModel({
      ...rowModesModel,
      [id]: { mode: GridRowModes.View, ignoreModifications: true },
    })

    const editedRow = rows.find((row) => row.id === id)
    if (editedRow.isNew) {
      setRows(rows.filter((row) => row.id !== id))
    }
  }

  const handleRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  useEffect(() => {
    setRows(initialRows)
  }, [initialRows])
  // useEffect(() => {
  //   setRows((prevRows) => {
  //     // Keep newly added rows and merge with initialRows
  //     const newRows = prevRows.filter((row) => row.isNew) // Preserve new rows
  //     return [...newRows, ...initialRows] // Merge with DB rows
  //   })
  // }, [initialRows])

  const onColumnResized = (params) => {
    if (params.column) {
      const field = params.column.getColDef().field
      setResizedColumns((prev) => ({
        ...prev,
        [field]: true,
      }))
    }
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleImportExport = () => {
    alert('File Import/Export feature coming soon!')
  }

  const deleteTheRecord = async () => {
    try {
      if (!deleteId) return
      if (title == 'Business Demand') {
        await DataService.deleteBusinessDemandData(deleteId, keycloak)
      }
      if (title == 'Shutdown Plan') {
        await DataService.deleteShutdownData(deleteId, keycloak)
      }
      if (title == 'Slowdown Plan') {
        await DataService.deleteSlowdownData(deleteId, keycloak)
      }
      if (title == 'Turnaround Plan') {
        await DataService.deleteTurnAroundData(deleteId, keycloak)
      }

      setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      onDeleteRow?.(deleteId)
      setDeleteId(null)
      setOpen1(false)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `${title} deleted successfully!`,
        severity: 'success',
      })
      fetchData()
    } catch (error) {
      console.error('Error deleting Business data:', error)
    }
  }

  const handleAddRow = () => {
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(initialColumns.map((col) => [col.field, ''])),
    }

    setRows((prevRows) => [newRow, ...prevRows])
    onAddRow?.(newRow)
    setProduct('')
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    }))
  }
  const handleKeyDown = (event, rowId) => {
    if (event.key === 'Enter') {
      event.preventDefault() // Prevent default Enter behavior (exiting edit mode)
      setRowModesModel((oldModel) => ({
        ...oldModel,
        [rowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
      }))
    }
  }

  const handleDeleteClick = async (id, params) => {
    const maintenanceId =
      id?.maintenanceId ||
      params?.row?.idFromApi ||
      params?.row?.maintenanceId ||
      params?.NormParameterMonthlyTransactionId
    setOpen1(true)
    setDeleteId(maintenanceId)
  }

  const defaultColumns = useMemo(() => {
    return initialColumns.map((col) => ({
      ...col,
      flex: !resizedColumns[col.field] ? 1 : undefined,
    }))
  }, [initialColumns, resizedColumns])

  const columns = useMemo(() => [
    ...defaultColumns,
    ...(permissions?.showAction
      ? [
          {
            field: 'actions',
            type: 'actions',
            headerName: 'Actions',
            width: 180,
            cellClassName: 'actions',

            getActions: (params) => {
              const { id, row } = params
              const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit
              if (isInEditMode) {
                return [
                  <GridActionsCellItem
                    key={`save-${id}`}
                    icon={<SaveIcon />}
                    label='Save'
                    sx={{ color: 'primary.main', display: 'none' }}
                    onClick={() => handleSaveClick(id, params.row)} // Pass row data
                  />,
                  <GridActionsCellItem
                    key={`cancel-${id}`}
                    icon={<CancelIcon />}
                    label='Cancel'
                    className='textPrimary'
                    onClick={handleCancelClick(id)}
                    color='inherit'
                  />,
                ]
              }

              return [
                permissions?.editButton && (
                  <GridActionsCellItem
                    key={`edit-${id}`}
                    icon={<EditIcon sx={{ color: jioColors.primaryBlue }} />}
                    label='Edit'
                    className='textPrimary'
                    onClick={handleEditClick(id, row)}
                    color='inherit'
                    sx={{ display: 'none' }}
                  />
                ),
                permissions?.deleteButton && (
                  <GridActionsCellItem
                    key={`delete-${id}`}
                    icon={<DeleteIcon sx={{ color: jioColors.accentRed }} />}
                    label='Delete'
                    onClick={() => handleDeleteClick(id, params)}
                    color='inherit'
                  />
                ),
              ].filter(Boolean) // Remove `null` values if permission is false
            },
            minWidth: 70,
            maxWidth: 100,
            headerClassName: 'last-column-header',
          },
        ]
      : []), // If no permissions, hide the Actions column
  ])

  const addRemark1 = () => {
    // setRows((prevRows) =>
    //   prevRows.map((row) =>
    //     row.id === selectedRowId ? { ...row, remark } : row,
    //   ),
    // )

    // setOpenRemark(false)
    // setRemark('')

    const updatedRow = filteredRows.find((row) => row.id === selectedRowId)
    if (updatedRow) {
      setRows((prevRows) =>
        prevRows.map((row) =>
          row.id === selectedRowId ? { ...row, remark } : row,
        ),
      )
      processRowUpdate({ ...updatedRow, remark: remark })
    }
    setOpenRemark(false)
    setRemark('')
  }

  const addRemark = () => {
    const updatedRow = filteredRows.find((row) => row.id === selectedRowId)
    if (updatedRow) {
      const updatedData = { ...updatedRow, remark }
      processRowUpdate(updatedData) // Pass updated row to the parent
    }
    setOpenRemark(false)
    setRemark('')
  }

  const handleCellClick = (params) => {
    //UNCOMMENT IT FOR REMARK POP UP
    // if (params?.field === 'remark' || params?.field === 'aopRemarks') {
    //   setRemark(params?.value || '')
    //   setSelectedRowId(params.id)
    //   handleOpenRemark()
    // }
  }

  const handleCloseSnackbar = () => {
    setSnackbarOpen(false)
  }

  const [columnFilters, setColumnFilters] = useState({})

  const filteredRows = useMemo(() => {
    return rows.filter((row) => {
      // Global search across all fields
      const matchesSearch = Object.values(row).some((value) =>
        String(value).toLowerCase().includes(searchText.toLowerCase()),
      )

      // Duration filter condition
      const matchesDuration = !isFilterActive || row.durationHrs > 100

      // Column-specific filters: For each column filter, check if row's value includes the filter
      const matchesColumnFilters = Object.entries(columnFilters).every(
        ([field, filterValue]) => {
          if (!filterValue) return true // No filter applied for this column
          return String(row[field])
            .toLowerCase()
            .includes(filterValue.toLowerCase())
        },
      )

      return matchesSearch && matchesDuration && matchesColumnFilters
    })
  }, [rows, searchText, isFilterActive, columnFilters])

  const handleRefresh = async () => {
    try {
      fetchData()
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }

  const handleCalculate = async (year) => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const response = await DataService.handleCalculate(
        plantId,
        year,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data refresh successfully!',
        severity: 'success',
      })

      return response
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }

  return (
    <Box
      sx={{
        height: '81vh',
        width: '100%',
        padding: 1,
        backgroundColor: '#F2F3F8',
        // backgroundColor: '#fff',
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
          {permissions?.showCalculate && (
            <Button
              variant='contained'
              onClick={handleCalculate}
              sx={{
                // marginTop: 2,
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
            >
              CALCULATE
            </Button>
          )}
          {permissions?.showRefreshBtn && (
            <Button
              variant='contained'
              onClick={handleRefresh}
              sx={{
                // marginTop: 2,
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
            >
              Refresh
            </Button>
          )}

          {permissions?.showUnit && (
            <TextField
              select
              value={selectedUnit || permissions?.UOM || ''}
              onChange={(e) => {
                setSelectedUnit(e.target.value)
                handleUnitChange(e.target.value)
              }}
              sx={{ width: '150px', backgroundColor: jioColors.background }}
              variant='outlined'
              label='Select UOM'
            >
              <MenuItem value='' disabled>
                Select UOM
              </MenuItem>
              {unitOptions.map((unit) => (
                <MenuItem key={unit} value={unit}>
                  {unit}
                </MenuItem>
              ))}
            </TextField>
          )}

          {/* commented for demo 4 March
          
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
          /> */}
          <IconButton
            aria-label='import'
            onClick={handleImportExport}
            sx={{
              border: `1px solid ${jioColors.border}`,
              borderRadius: 1,
              padding: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
              color: 'inherit',
              width: '150px',
              '&:hover': {
                backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF', // Removes hover effect
              },
            }}
          >
            <FileDownload
              sx={{ color: '#2A3ACD' }}
              // sx={{ color: isFilterActive ? jioColors.background : 'inherit' }}
            />
            <span
              style={{
                fontSize: '0.875rem',
                color: '#2A3ACD',
              }}
            >
              Import
            </span>
          </IconButton>
          <IconButton
            aria-label='export'
            onClick={handleImportExport}
            sx={{
              border: `1px solid ${jioColors.border}`,
              borderRadius: 1,
              padding: '20px',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
              color: 'inherit',
              width: '150px',
              '&:hover': {
                backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF', // Removes hover effect
              },
            }}
          >
            <FileUpload
              sx={{ color: '#2A3ACD' }}
              // sx={{ color: isFilterActive ? jioColors.background : 'inherit' }}
            />
            <span
              style={{
                fontSize: '0.875rem',
                color: '#2A3ACD',
              }}
            >
              Export
            </span>
          </IconButton>
        </Box>
      </Box>

      <Box sx={{ height: 'calc(100% - 150px)', width: '100%' }}>
        {/* <Grid container spacing={2}>
          {columns.map((col) => (
            <Grid item xs key={col.field}>
              <TextField
                placeholder={`Filter ${col.headerName}`}
                variant='outlined'
                size='small'
                onChange={(e) => handleFilterChange(col.field, e.target.value)}
              />
            </Grid>
          ))}
        </Grid> */}
        <DataGrid
          apiRef={apiRef}
          rows={filteredRows}
          columns={columns.map((col) => ({
            ...col,
            editable: col.field === 'product' ? true : col.editable,
          }))}
          columnVisibilityModel={{
            maintenanceId: false,
            id: false,
            plantFkId: false,
            aopCaseId: false,
            aopType: false,
            aopYear: false,
            avgTph: false,
            NormParameterMonthlyTransactionId: false,
            // NormParametersId: false,
            aopStatus: false,
            idFromApi: false,
          }}
          rowHeight={35}
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={onProcessRowUpdateError}
          onColumnResized={onColumnResized}
          onCellClick={handleCellClick}
          onRowEditCommit={handleRowEditCommit}
          onCellEditCommit={(params) => handleCellEditCommit(params)} // Real-time updates
          experimentalFeatures={{ newEditingApi: true }}
          editMode='row'
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          // onRowEditStop={handleRowEditStop}
          slotProps={{
            toolbar: { setRows, setRowModesModel },
          }}
          onCellEditStop={(params, event) => {
            // Always prevent default edit stop behavior
            event.defaultMuiPrevented = true

            // But still capture the updated value and save it
            if (
              params.reason === 'cellFocusOut' ||
              params.reason === 'escapeKeyDown'
            ) {
              const updatedRow = { ...params.row, [params.field]: params.value }
              processRowUpdate(updatedRow, params.row)
            }
          }}
          getRowClassName={(params) =>
            params.indexRelativeToCurrentPage % 2 === 0 ? 'even-row' : 'odd-row'
          }
          sx={{
            borderRadius: '0px',
            border: `1px solid ${jioColors.border}`,
            backgroundColor: jioColors.background,
            fontSize: '0.8rem',
            ' & .MuiDataGrid-columnHeaderTitleContainer:last-child:after .MuiDataGrid-columnHeaderTitleContainer:after':
              {
                bordeRight: 'none !important',
              },

            '& .MuiDataGrid-cell:last-child:after': {
              borderRight: 'none',
            },
            '& .MuiDataGrid-columnHeader:last-child:after': {
              borderRight: 'none',
            },
            '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer:after':
              {
                borderRight: 'none',
              },
            // Added direct rule for the title container without the pseudo-element:
            '& .MuiDataGrid-columnHeader:last-child .MuiDataGrid-columnHeaderTitleContainer':
              {
                borderRight: 'none',
              },
            '& .MuiDataGrid-cell.last-column, & .MuiDataGrid-columnHeaderTitleContainer.last-column & .MuiDataGrid-columnHeader.last-column':
              {
                borderRight: 'none',
              },

            // borderRight: `1px solid ${jioColors.border}`,
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
            // Remove the direct right border from cells and headers
            '& .MuiDataGrid-cell, & .MuiDataGrid-columnHeaders & .MuiDataGrid-columnHeaderTitleContainer':
              {
                borderRight: 'none',
                position: 'relative',
              },
            // Apply a pseudo-element for a short right border on cells
            '& .MuiDataGrid-cell:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust this percentage as needed for the "short" border
              borderRight: `1px solid ${jioColors.border}`,
            },

            // Apply a similar pseudo-element for header cells
            '& .MuiDataGrid-columnHeaders:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust as needed
              borderRight: `1px solid ${jioColors.border}`,
            },
            '& .MuiDataGrid-columnHeaderTitleContainer:after': {
              content: '""',
              position: 'absolute',
              right: 0,
              top: '50%',
              transform: 'translateY(-50%)',
              height: '60%', // Adjust as needed
              borderRight: `1px solid ${jioColors.border}`,
            },

            '& .MuiDataGrid-columnHeaders': {
              // borderRight: `1px solid ${jioColors.border}`,
              // backgroundColor: jioColors.headerBg,
              // color: '#FFFFFF',
              backgroundColor: '#FAFAFC',
              color: '#3E4E75',
              fontSize: '0.8rem',
              fontWeight: 600,
              borderBottom: `2px solid ${'#DAE0EF'}`,
              borderTopLeftRadius: '0px',
              borderTopRightRadius: '0px',
            },
            '& .MuiDataGrid-cell': {
              // borderRight: `1px solid ${jioColors.border}`,
              borderBottom: `1px solid ${'#DAE0EF'}`,
              color: '#3E4E75',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              fontSize: '0.8rem',
              cursor: 'pointer',
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
            // '& .MuiDataGrid-columnHeaders .last-column-header': {
            //   paddingRight: '16px',
            // },
            // '& .MuiDataGrid-cell.last-column-cell': {
            //   paddingRight: '16px',
            // },
          }}
        />
      </Box>
      <Box
        sx={{
          marginTop: 2,
          display: 'flex',
          gap: 2,
        }}
      >
        {permissions.addButton && (
          <Button
            variant='contained'
            sx={{
              // marginTop: 2,
              backgroundColor: jioColors.primaryBlue,
              color: jioColors.background,
              borderRadius: 1,
              padding: '8px 24px',
              textTransform: 'none',
              fontSize: '0.875rem',
              fontWeight: 500,
              minWidth: 120, // Same width for consistency
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
        {permissions.saveBtn && (
          <Button
            variant='contained'
            sx={{
              // marginTop: 2,
              backgroundColor: jioColors.primaryBlue,
              color: jioColors.background,
              borderRadius: 1,
              padding: '8px 24px',
              textTransform: 'none',
              fontSize: '0.875rem',
              fontWeight: 500,
              minWidth: 120, // Same width for consistency
              '&:hover': {
                backgroundColor: '#143B6F',
                boxShadow: 'none',
              },
            }}
            // onClick={handleSaveClick} // Pass row data
            onClick={saveChanges}
            loadingPosition='start'
            // disabled={!hasUnsavedRows}
            // loading={isSaving}
          >
            Save
          </Button>
        )}
      </Box>

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={handleCloseSnackbar}
      />

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
            onChange={(e) => {
              setRemark(e.target.value)
              // setRowModesModel((prev) => ({
              //   ...prev,
              //   [id]: { mode: GridRowModes.View },
              // }))
            }}
            multiline
            rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseRemark}>Cancel</Button>
          <Button onClick={addRemark}>Add</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default DataGridTable
