import CancelIcon from '@mui/icons-material/Close'
// import DeleteIcon from '@mui/icons-material/Delete'
import DeleteIcon from '@mui/icons-material/DeleteOutlined'
import EditIcon from '@mui/icons-material/Edit'
import VisibilityIcon from '@mui/icons-material/Visibility'
import SaveIcon from '@mui/icons-material/Save'
import { Box, Button, IconButton, TextField } from '@mui/material'
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
//import './data-grid-css.css'
//import './extra-css.css'

import { MenuItem } from '../../../node_modules/@mui/material/index'

import {
  FileDownload,
  FileUpload,
} from '../../../node_modules/@mui/icons-material/index'
import Typography from 'themes/overrides/Typography'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'

const jioColors = {
  primaryBlue: '#387ec3',
  // primaryBlue: 'red',
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
  // title = '',
  onAddRow = () => {},
  // onDeleteRow = () => {},
  permissions = {},
  processRowUpdate = (row) => row,
  isCellEditable = () => true,
  saveChanges = () => {},
  apiRef = null,
  rowModesModel: rowModesModel,
  // setRowModesModel,
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  // setSnackbarData = () => {},
  setSnackbarOpen = () => {},
  fetchData = () => {},
  handleUnitChange = () => {},
  handleCalculate = () => {},
  setRows = () => {},
  rows = [],
  modifiedCells = [],
  loading = false,
  remarkDialogOpen = false,
  onRowModesModelChange = () => {},
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  unsavedChangesRef = { current: { unsavedRows: {}, rowsBeforeChange: {} } },
  deleteRowData = () => {},
  handleAddPlantSite = () => {},
  selectedUsers = [],
  setSelectedUsers = () => {},
  // columnGroupingModel,
}) => {
  const [resizedColumns, setResizedColumns] = useState({})
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [searchText, setSearchText] = useState('')
  const isFilterActive = false
  const [selectedUnit, setSelectedUnit] = useState()
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeDeleteDialogeBox = () => setOpenDeleteDialogeBox(false)
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const localApiRef = useGridApiRef()
  const finalExternalApiRef = apiRef ?? localApiRef
  const handleSearchChange = (event) => {
    setSearchText(event.target.value)
  }
  // const navigate = useNavigate()

  // const [rowModesModel, setRowModesModel] = useState({})
  // const [changedRowIds, setChangedRowIds] = useState([])
  // const [columnFilters, setColumnFilters] = useState({})
  const columnFilters = {}

  // const handleRowEditCommit = (id, event) => {
  //   const editedRow = rows.find((row) => row.id === id)
  // }

  // const handleCellEditCommit = (id, event) => {}

  const handleEditClick = (row) => () => {
    // setIsUpdating(true)
    // setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
    handleAddPlantSite(row)
    // setRowModesModel({
    //   ...rowModesModel,
    //   [row.id]: { mode: GridRowModes.Edit },
    // })
  }

  const handleSaveClick = () => {
    // handleOpenRemark()
    // setRowModesModel((prev) => ({
    //   ...prev,
    //   [id]: { mode: GridRowModes.View },
    // }))
  }

  const handleCancelClick = (id) => () => {
    // setRowModesModel({
    //   ...rowModesModel,
    //   [id]: { mode: GridRowModes.View, ignoreModifications: true },
    // })

    const editedRow = rows.find((row) => row.id === id)
    if (editedRow.isNew) {
      setRows(rows.filter((row) => row.id !== id))
    }
  }

  // const handleRowModesModelChange = (newRowModesModel) => {
  //   setRowModesModel(newRowModesModel)
  // }

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
    // setRowModesModel((oldModel) => ({
    //   ...oldModel,
    //   [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    // }))
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
              // console.log(row)
              if (row.isGroupHeader || row.isSubGroupHeader) {
                return [] || null
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
                permissions?.viewBtn && (
                  <GridActionsCellItem
                    key={`view-${id}`}
                    icon={<VisibilityIcon />}
                    label='View'
                    className='textPrimary'
                    onClick={handleEditClick(row)}
                    color='inherit'
                    // sx={{ display: 'none' }}
                  />
                ),
                permissions?.editButton && (
                  <GridActionsCellItem
                    key={`edit-${id}`}
                    icon={<EditIcon />}
                    label='Edit'
                    className='textPrimary'
                    onClick={handleEditClick(row)}
                    color='inherit'
                    // sx={{ display: 'none' }}
                  />
                ),
                permissions?.deleteButton && (
                  <GridActionsCellItem
                    key={`delete-${id}`}
                    icon={<DeleteIcon />}
                    label='Delete'
                    onClick={() => handleDeleteClick(id, params)}
                    color='inherit'
                  />
                ),
              ].filter(Boolean)
            },
            minWidth: 70,
            maxWidth: 100,
            headerClassName: 'last-column-header',
          },
        ]
      : []),
  ])

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null

      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keysToUpdate = ['aopRemarks', 'remarks', 'remark'].filter(
            (key) => key in row,
          )
          //          console.log(keysToUpdate)
          const keyToUpdate = keysToUpdate[0] || 'remark'
          //          console.log([keyToUpdate])
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
  const boxHeight = permissions?.customHeight?.mainBox
  const otherHeight = permissions?.customHeight?.otherBox
  // console.log(boxHeight)
  const handleDeleteAll = () => {
    setSelectedUsers([])
    setRows([])
  }
  // console.log(selectedUsers?.length)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  // console.log(showDeleteAll)

  const lastColumnField = columns[columns.length - 1]?.field

  return (
    <Box
      sx={{
        // height: `${boxHeight ?? (permissions.customHeight2 ? '50vh' : '80vh')}`,
        height: 'auto',
        width: '100%',
        padding: '0px 0px',
        margin: '0px 0px 0px',
        backgroundColor: '#F2F3F8',
        // backgroundColor: '#fff',
        borderRadius: 0,
        borderBottom: 'none',
      }}
    >
      {/* {(permissions?.allAction ?? true) && ( */}
      {/* <Box
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
      </Box> */}
      {/* )} */}
      {(permissions?.allAction ?? true) && (
        <Box className='action-box'>
          <Box className='action-inner'>
            {permissions?.UnitToShow && (
              <Chip
                label={permissions.UnitToShow}
                variant='outlined'
                className='unit-chip'
              />
            )}
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {permissions?.showTitle && (
              <Typography component='div' className='grid-title'>
                Annual AOP Cost
              </Typography>
            )}
            {permissions?.showCalculate && (
              <Button
                variant='contained'
                onClick={handleCalculateBtn}
                disabled={isButtonDisabled}
                className='btn-save'
              >
                Calculate
              </Button>
            )}
            {permissions?.showRefresh && (
              <Button
                variant='contained'
                onClick={handleCalculateBtn}
                disabled={isButtonDisabled}
                className='btn-save'
              >
                Refresh
              </Button>
            )}

            {permissions?.showRefreshBtn && false && (
              <Button
                variant='contained'
                onClick={handleRefresh}
                className='btn-save'
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
      )}

      <Box
        sx={{
          height: `calc(${otherHeight ?? (permissions.customHeight2 ? '95%' : '95%')} - 120px)`,
          width: '100%',
          marginBottom: 0,
          padding: 0,
        }}
      >
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

        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

        <DataGrid
          autoHeight={true}
          loading={loading}
          className='custom-data-grid'
          apiRef={finalExternalApiRef}
          rows={filteredRows}
          sortingOrder={[]}
          disableSelectionOnClick
          checkboxSelection={permissions?.showCheckBox}
          columns={columns.map((col) => ({
            ...col,
            // editable: (params) => {
            //   if (
            //     permissions?.remarksEditable &&
            //     params.row.isEditable === false &&
            //     col.field !== lastColumnField
            //   ) {
            //     return false
            //   }
            //   return col.field === lastColumnField
            // },
            cellClassName: (params) => {
              if (modifiedCells[params.row.id]?.includes(params.field)) {
                return 'red-first-cell '
              }

              if (col.isDisabled) {
                if (params.row.Particulars) {
                  return undefined
                } else {
                  return 'disabled-cell'
                }
              }
              if (
                permissions?.remarksEditable &&
                params.row.isEditable === false &&
                col.field !== lastColumnField
              ) {
                return 'odd-cell'
              }
              return undefined
            },
            headerClassName: col.isDisabled ? 'disabled-header' : undefined,
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
            isEditable: false,
            period: false,
          }}
          disableColumnSelector
          disableColumnSorting
          rowHeight={35}
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={onProcessRowUpdateError}
          onColumnResized={onColumnResized}
          isCellEditable={isCellEditable}
          experimentalFeatures={{ newEditingApi: true }}
          editMode='row'
          rowModesModel={rowModesModel}
          onRowModesModelChange={onRowModesModelChange}
          handleCalculate={handleCalculate}
          deleteRowData={deleteRowData}
          slotProps={{
            toolbar: { setRows, GridToolbar },
            // loadingOverlay: {
            //   variant: 'linear-progress',
            //   norowsvariant: 'skeleton',
            // },
          }}
          getRowClassName={(params) => {
            const classes = []

            if (permissions?.isOldYear == 1) {
              classes.push('odd-row-disabled')
            }

            if (params.row.Particulars || params.row.Particulars2) {
              classes.push('no-border-row')
            }

            if (
              params.row.isEditable === false &&
              !permissions?.remarksEditable
            ) {
              return [
                ...classes,
                permissions?.noColor === true ? 'even-row' : 'odd-row',
              ].join(' ')
            }

            return [...classes, 'even-row'].join(' ')
          }}
          // columnGroupingModel={columnGroupingModel}
        />
      </Box>

      {(permissions?.allAction ?? true) && (
        <Box
          sx={{
            marginTop: 2,
            display: 'flex',
            gap: 2,
          }}
        >
          {permissions?.addButton && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={handleAddRow}
              disabled={isButtonDisabled}
            >
              Add Item
            </Button>
          )}

          {permissions?.saveBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={saveModalOpen}
              disabled={isButtonDisabled}
              // loading={loading}
              // loadingposition='start'
              {...(loading ? {} : {})}
            >
              Save
            </Button>
          )}
          {permissions?.approveBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={saveModalOpen}
              disabled={isButtonDisabled}
              loading={loading}
              loadingposition='start'
            >
              Approve
            </Button>
          )}
          {permissions?.nextBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={() => {
                // Write any additional logic here before navigating.
                // console.log('Navigating to dashboard')
                // navigate('/user-form')
                handleAddPlantSite()
              }}
              disabled={isButtonDisabled}
              loading={loading} // Use the loading prop to trigger loading state
              loadingposition='start' // Use loadingPosition to control where the spinner appears
            >
              Next
            </Button>
          )}
          {showDeleteAll && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={() => {
                // Write any additional logic here before navigating.
                // console.log('Navigating to dashboard')
                // navigate('/user-form')
                handleDeleteAll()
              }}
              disabled={isButtonDisabled}
              loading={loading} // Use the loading prop to trigger loading state
              loadingposition='start' // Use loadingPosition to control where the spinner appears
            >
              Delete
            </Button>
          )}
        </Box>
      )}

      {(permissions?.allAction ?? true) && (
        <Notification
          open={snackbarOpen}
          message={snackbarData?.message || ''}
          severity={snackbarData?.severity || 'info'}
          onClose={() => setSnackbarOpen(false)}
        />
      )}

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
