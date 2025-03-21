import CancelIcon from '@mui/icons-material/Close'
// import DeleteIcon from '@mui/icons-material/Delete'
import DeleteIcon from '@mui/icons-material/DeleteOutlined'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import { Box, Button, IconButton, TextField, Typography } from '@mui/material'
import { DataGrid, GridToolbar } from '@mui/x-data-grid'
import * as React from 'react'
import { useEffect, useMemo, useState } from 'react'

import SearchIcon from '@mui/icons-material/Search'
import { InputAdornment } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import { GridActionsCellItem, GridRowModes } from '@mui/x-data-grid'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { MenuItem } from '../../../node_modules/@mui/material/index'

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
  title = 'Turnaround Plan Details',
  onAddRow,
  onDeleteRow,
  permissions,
  processRowUpdate,
  isCellEditable,
  saveChanges,
  apiRef,
  snackbarData,
  snackbarOpen,
  setSnackbarData,
  setSnackbarOpen,
  fetchData,
  handleUnitChange,
  handleCalculate,
  setRows,
  rows,
  loading,
  remarkDialogOpen,
  setRemarkDialogOpen,
  currentRemark,
  setCurrentRemark,
  // setCurrentRowId,
  currentRowId,
  unsavedChangesRef,
  deleteRowData,
  // handleRemarkCellClick,
  // units,
}) => {
  // const [tempHide, setTempHide] = useState(true)
  // const [isUpdating, setIsUpdating] = useState(false)
  // const [isSaving, setIsSaving] = useState(false)
  const [resizedColumns, setResizedColumns] = useState({})
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)

  // const [open, setOpen] = useState(false)
  // const [remark, setRemark] = useState('')
  // const [product, setProduct] = useState('')
  // const [openRemark, setOpenRemark] = useState(false)
  // const keycloak = useSession()
  // const [days, setDays] = useState([])
  const [searchText, setSearchText] = useState('')
  const isFilterActive = false
  // const [selectedRowId, setSelectedRowId] = useState(null)
  const [selectedUnit, setSelectedUnit] = useState()
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  // const [deleteId, setDeleteId] = useState(false)
  // const [deleteIdTemp, setDeleteIdTemp] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  // const handleOpenRemark = () => setOpenRemark(true)
  // const handleCloseRemark = () => setOpenRemark(false)
  const closeDeleteDialogeBox = () => setOpenDeleteDialogeBox(false)
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const handleSearchChange = (event) => {
    setSearchText(event.target.value)
  }
  const [rowModesModel, setRowModesModel] = useState({})
  // const [changedRowIds, setChangedRowIds] = useState([])
  // const [columnFilters, setColumnFilters] = useState({})
  const columnFilters = {}

  // const handleRowEditCommit = (id, event) => {
  //   const editedRow = rows.find((row) => row.id === id)
  // }

  // const handleCellEditCommit = (id, event) => {}

  const handleEditClick = (id) => () => {
    // setIsUpdating(true)
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
  }

  const handleSaveClick = (id) => {
    // handleOpenRemark()
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
    if (rows) setRows(rows)
  }, [rows, setRows])

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
    deleteRowData(paramsForDelete)
    setOpenDeleteDialogeBox(false)
  }

  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
  }

  const saveModalOpen = async () => {
    setIsButtonDisabled(true)
    setOpenSaveDialogeBox(true)
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const handleAddRow = () => {
    if (isButtonDisabled) return
    setIsButtonDisabled(true)
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
    // setProduct('')
    setRowModesModel((oldModel) => ({
      ...oldModel,
      [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    }))
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const handleDeleteClick = async (id, params) => {
    setParamsForDelete(params)
    setOpenDeleteDialogeBox(true)
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

              if (row.isGroupHeader) {
                return []
              }

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
                    // icon={<EditIcon sx={{ color: jioColors.primaryBlue }} />}
                    icon={<EditIcon />}
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
                    // icon={<DeleteIcon sx={{ color: jioColors.accentRed }} />}
                    icon={<DeleteIcon />}
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

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null

      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keyToUpdate =
            ['aopRemarks', 'remarks', 'remark'].find((key) => key in row) ||
            'remark' // Default to 'remark' if none exist

          updatedRow = { ...row, [keyToUpdate]: currentRemark }
          return updatedRow
        }
        return row
      })

      if (updatedRow) {
        unsavedChangesRef.current.unsavedRows[currentRowId] = updatedRow
      }

      return updatedRows
    })

    setRemarkDialogOpen(false)
  }

  // const handleCellClick = (params) => {}

  const handleCloseSnackbar = () => {
    setSnackbarOpen(false)
  }

  const filteredRows = useMemo(() => {
    if (!Array.isArray(rows)) return []

    return rows.filter((row) => {
      // Global search across all fields
      const matchesSearch = Object.values(row).some((value) =>
        String(value).toLowerCase().includes(searchText.toLowerCase()),
      )

      // Duration filter condition
      const matchesDuration = !isFilterActive || row.durationHrs > 100

      // Column-specific filters
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
  const handleCalculateBtn = async () => {
    setIsButtonDisabled(true)
    handleCalculate()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
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
          justifyContent: 'space-between',
          alignItems: 'center',
          marginTop: 2,
          marginBottom: 1,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {permissions?.UnitToShow && (
            <Chip
              label={permissions.UnitToShow}
              variant='outlined'
              sx={{
                borderRadius: 1,
                padding: '8px 24px',
                textTransform: 'none',
                fontSize: '0.875rem',
                fontWeight: 500,
                height: '40px',
              }}
            />
          )}
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {permissions?.showCalculate && (
            <Button
              variant='contained'
              onClick={handleCalculateBtn}
              disabled={isButtonDisabled}
              sx={{
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
                '&.Mui-disabled': {
                  backgroundColor: jioColors.primaryBlue,
                  color: jioColors.background,
                  opacity: 0.7,
                },
              }}
            >
              Calculate
            </Button>
          )}

          {permissions?.showRefreshBtn && false && (
            <Button
              variant='contained'
              onClick={handleRefresh}
              sx={{
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
              value={selectedUnit || permissions?.units?.[0]}
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

              {/* Render the correct unit options dynamically */}
              {permissions?.units.map((unit) => (
                <MenuItem key={unit} value={unit}>
                  {unit}
                </MenuItem>
              ))}
            </TextField>
          )}

          {false && (
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
          )}

          {false && (
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
                  backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
                },
              }}
            >
              <FileDownload sx={{ color: '#2A3ACD' }} />
              <span style={{ fontSize: '0.875rem', color: '#2A3ACD' }}>
                Import
              </span>
            </IconButton>
          )}

          {false && (
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
                  backgroundColor: isFilterActive ? '#F2F3F8' : '#FFF',
                },
              }}
            >
              <FileUpload sx={{ color: '#2A3ACD' }} />
              <span style={{ fontSize: '0.875rem', color: '#2A3ACD' }}>
                Export
              </span>
            </IconButton>
          )}
        </Box>
      </Box>

      <Box sx={{ height: 'calc(100% - 150px)', width: '100%' }}>
        {/* {!tempHide && (
          <Grid container spacing={2}>
            {columns.map((col) => (
              <Grid item xs key={col.field}>
                <TextField
                  placeholder={`Filter ${col.headerName}`}
                  variant='outlined'
                  size='small'
                  // onChange={(e) =>
                  //   handleFilterChange(col.field, e.target.value)
                  // }
                />
              </Grid>
            ))}
          </Grid>
        )} */}

        {/* Backdrop inside child component */}
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

        <DataGrid
          loading={loading}
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
            aopStatus: false,
            idFromApi: false,
            period: false,
          }}
          rowHeight={35}
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={onProcessRowUpdateError}
          onColumnResized={onColumnResized}
          isCellEditable={isCellEditable}
          experimentalFeatures={{ newEditingApi: true }}
          editMode='row'
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          handleCalculate={handleCalculate}
          deleteRowData={deleteRowData}
          slotProps={{
            toolbar: { setRows, setRowModesModel, GridToolbar },
            loadingOverlay: {
              variant: 'linear-progress',
              noRowsVariant: 'skeleton',
            },
          }}
          getRowClassName={(params) => {
            return params.row.Particulars
              ? 'no-border-row'
              : params.indexRelativeToCurrentPage % 2 === 0
                ? 'even-row'
                : 'odd-row'
          }}
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
              height: '60%',
              borderRight: `1px solid ${jioColors.border}`,
            },
            //Do not remove this prop (for Grouped row it can be usefull !!!!!)
            '& .MuiDataGrid-row.no-border-row .MuiDataGrid-cell:after': {
              borderRight: 'none !important',
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
              // fontWeight: 600,
              fontWeight: 'bold',
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
            '& .MuiDataGrid-columnHeaderTitle': {
              fontWeight: 'bold', // Ensure column titles are bold
            },
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
              minWidth: 120,
              '&:hover': {
                backgroundColor: '#143B6F',
                boxShadow: 'none',
              },
              '&.Mui-disabled': {
                backgroundColor: jioColors.primaryBlue,
                color: jioColors.background,
                opacity: 0.7,
              },
            }}
            onClick={handleAddRow}
            disabled={isButtonDisabled}
          >
            Add Item
          </Button>
        )}

        {permissions.saveBtn && (
          <Button
            variant='contained'
            sx={{
              backgroundColor: jioColors.primaryBlue,
              color: jioColors.background,
              borderRadius: 1,
              padding: '8px 24px',
              textTransform: 'none',
              fontSize: '0.875rem',
              fontWeight: 500,
              minWidth: 120,
              '&:hover': {
                backgroundColor: '#143B6F',
                boxShadow: 'none',
              },
              '&.Mui-disabled': {
                backgroundColor: jioColors.primaryBlue,
                color: jioColors.background,
                opacity: 0.7,
              },
            }}
            onClick={saveModalOpen}
            disabled={isButtonDisabled}
            loading={loading} // Use the loading prop to trigger loading state
            loadingPosition='start' // Use loadingPosition to control where the spinner appears
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
        open={openDeleteDialogeBox}
        onClose={closeDeleteDialogeBox}
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
          <Button onClick={closeDeleteDialogeBox}>Cancel</Button>
          <Button onClick={deleteTheRecord} autoFocus>
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={openSaveDialogeBox}
        onClose={closeSaveDialogeBox}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Save ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to save these changes?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeSaveDialogeBox}>Cancel</Button>
          <Button onClick={saveConfirmation} autoFocus>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={!!remarkDialogOpen}
        onClose={() => setRemarkDialogOpen(false)}
      >
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
            sx={{ width: '100%', minWidth: '600px' }}
            value={currentRemark || ''}
            // value={remark}
            onChange={(e) => setCurrentRemark(e.target.value)}
            multiline
            rows={8}
            //     onChange={(e) => {
            //   setRemark(e.target.value)
            //   // setRowModesModel((prev) => ({
            //   //   ...prev,
            //   //   [id]: { mode: GridRowModes.View },
            //   // }))
            // }}
            // multiline
            // rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRemarkDialogOpen(false)}>Cancel</Button>
          {/* <Button onClick={handleCloseRemark}>Cancel</Button> */}
          <Button onClick={handleRemarkSave} disabled={!currentRemark?.trim()}>
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default DataGridTable
