import React, { useState, useEffect, useMemo } from 'react'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
// import PropTypes from 'prop-types'
import '../../kendo-data-grid.css'
import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  MenuItem,
  TextField,
} from '../../../node_modules/@mui/material/index'
import Notification from 'components/Utilities/Notification'
import { process } from '@progress/kendo-data-query'

const KendoDataTables = ({
  // setUpdatedRows = () => {},
  rows = [],
  // updatedRows = [],
  setRows,
  columns,
  loading = false,
  pageSizes = [10, 20, 50],
  // onRowChange,
  // disableColor = false,
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  unsavedChangesRef = { current: { unsavedRows: {}, rowsBeforeChange: {} } },
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  // editedRows = [],
  setCurrentRemark = () => {},
  currentRowId = null,
  // modifiedCells = [],
  NormParameterIdCell = () => {},
  setModifiedCells = () => {},
  remarkDialogOpen = false,
  handleDeleteSelected = () => {},
  saveChanges = () => {},
  // deleteRowData = () => {},
  handleAddPlantSite = () => {},
  handleCalculate = () => {},
  fetchData = () => {},
  handleUnitChange = () => {},
  handleRemarkCellClick = () => {},
  selectedUsers = [],
  groupBy = null,
  // allRedCell = [],
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  // const [resizedColumns, setResizedColumns] = useState({})
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  // const [searchText, setSearchText] = useState('')
  // const isFilterActive = false
  const [selectedUnit, setSelectedUnit] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  // const [paramsForDelete, setParamsForDelete] = useState([])
  // const closeDeleteDialogeBox = () => setOpenDeleteDialogeBox(false)
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  // const localApiRef = useGridApiRef()
  // const finalExternalApiRef = apiRef ?? localApiRef
  // const handleSearchChange = (event) => {
  //   setSearchText(event.target.value)
  // }
  // console.log(columns)
  const rowRender = (trElement, props) => {
    if (!props.dataItem.isEditable) {
      return React.cloneElement(trElement, {
        ...trElement.props,
        className: (trElement.props.className || '')
          .split(' ')
          .concat('disabled-row')
          .join(' '),
      })
    }
    return trElement
  }
  const hiddenFields = [
    'maintenanceId',
    'id',
    'plantFkId',
    'aopCaseId',
    'aopType',
    'aopYear',
    'avgTph',
    'NormParameterMonthlyTransactionId',
    'aopStatus',
    'idFromApi',
    'isEditable',
    'period',
  ]
  // //  const toggleColumn = field => {
  // //   setColumnVisibility(vis => ({
  //     ...vis,
  //     [field]: !vis[field],
  //   }));
  // };
  // cell update
  const itemChange = (e) => {
    // console.log(rows)
    const updated = rows.map((r) =>
      r.id === e.dataItem.id ? { ...r, [e.field]: e.value } : r,
    )
    console.log(updated)
    setModifiedCells(updated)
    setRows(updated)

    // onRowChange(e.dataItem, e.field, e.value)
  }
  // console.log(unsavedChangesRef)
  // const rowRender = disableColor
  //   ? (trElement, props) => {
  //       const shouldDisable = props.dataItem.status === 'inactive'
  //       return React.cloneElement(trElement, {
  //         ...trElement.props,
  //         className: `${trElement.props.className || ''} ${shouldDisable ? 'disabled-row' : ''}`,
  //       })
  //     }
  //   : undefined

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
  // console.log(rows)
  // console.log(columns)
  const handleAddRow = () => {
    if (isButtonDisabled) return
    setIsButtonDisabled(true)
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(columns.map((col) => [col.field, ''])),
    }

    setRows((prevRows) => [newRow, ...prevRows])
    // onAddRow?.(newRow)
    // setProduct('')
    // setRowModesModel((oldModel) => ({
    //   ...oldModel,
    //   [newRowId]: { mode: GridRowModes.Edit, fieldToFocus: 'discription' },
    // }))
    // focusFirstField()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
  }
  // const handleDeleteClick = async (id, params) => {
  //   setParamsForDelete(params)
  //   setOpenDeleteDialogeBox(true)
  // }
  const deleteTheRecord = async () => {
    // deleteRowData(paramsForDelete)
    setOpenDeleteDialogeBox(false)
  }
  const saveModalOpen = async () => {
    setIsButtonDisabled(true)
    setOpenSaveDialogeBox(true)
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const handleCalculateBtn = async () => {
    setIsButtonDisabled(true)
    handleCalculate()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const handleRefresh = async () => {
    try {
      fetchData()
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }
  const handleRowClick = (e) => {
    console.log('22', e)

    // setRows(
    //   rows.map((r) => ({
    //     ...r,
    //     inEdit: r.id === e.dataItem.id, // only that row goes into edit mode
    //   })),
    // )
  }
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [group, setGroup] = useState([])

  useEffect(() => {
    if (rows && rows.length > 0 && groupBy) {
      setGroup([{ field: groupBy }])

      const initialExpandedState = {}
      const uniqueValues = [...new Set(rows.map((row) => row[groupBy]))]
      uniqueValues.forEach((value) => {
        initialExpandedState[`${groupBy}_${value}`] = true
      })
      setExpandedState(initialExpandedState)
    }
  }, [rows, groupBy])
  const [expandedState, setExpandedState] = useState({})

  const processedData = useMemo(() => {
    if (!rows || rows.length === 0) return []

    if (group.length > 0) {
      const result = process(rows, { group })

      // Apply expanded state to the processed data
      const applyExpandedState = (items) => {
        return items.map((item) => {
          console.log('Inspecting item:', item)
          if (item.items) {
            // This is a group item
            const key = item.field + '_' + item.value
            console.log('Using key:', key)
            item.expanded = expandedState[key] !== false // Default to expanded
            item.items = applyExpandedState(item.items)
          }
          return item
        })
      }

      return applyExpandedState(result.data)
    }

    return rows
  }, [rows, group, expandedState])
  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <div className='k-loading-mask'>
          <span className='k-loading-text'>Loading...</span>
          <div className='k-loading-image' />
          <div className='k-loading-color' />
        </div>
      )}
      {(permissions?.allAction ?? true) && (
        <Box className='action-box'>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'flex-end',
              width: '100%', // make sure container is full width
              p: 1,
              gap: 1,
            }}
          >
            {permissions?.UnitToShow && (
              <Chip
                label={permissions.UnitToShow}
                variant='outlined'
                className='unit-chip'
              />
            )}
            {/* {permissions?.showCalculate && (
              <Tooltip title='Calculate'>
                <span>
                  <Button
                    variant='contained'
                    onClick={handleCalculateBtn}
                    disabled={isButtonDisabled}
                    sx={{
                      minWidth: '40px',
                      padding: '8px',
                      backgroundColor: '#0100cb',
                      '&:hover': {
                        backgroundColor: '#0100cb',
                        opacity: 0.9,
                      },
                    }}
                  >
                    <CalculateOutlinedIcon sx={{ color: '#fff' }} />
                  </Button>
                </span>
              </Tooltip>
            )} */}

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
                sx={{ width: '150px', backgroundColor: '#FFFFFF' }}
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

            {/* </Box> */}
          </Box>
        </Box>
      )}
      <div className='kendo-data-grid'>
        <Grid
          data={filterBy(processedData, filter)}
          sortable
          dataItemKey='id'
          pageable={{ pageSizes, buttonCount: 5 }}
          editField='inEdit'
          editable='incell'
          onRowClick={handleRowClick}
          filter={filter}
          filterable={true}
          //groupable = {true}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={itemChange}
          rowRender={rowRender}
          group={group}
          expandField='expanded'
          onGroupChange={(e) => setGroup(e.group)}
          onExpandChange={(item) => {
            const key = item.field || item.value // Use appropriate unique identifier
            setExpandedState({
              ...expandedState,
              [key]: item.expanded,
            })
          }}
          groupHeaderRender={(e) => (
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <span style={{ marginRight: 6 }}>{e.expanded ? '▼' : '▶'}</span>
              <strong>{e.value}</strong>
              <span style={{ marginLeft: 6 }}>
                ({e.aggregates?.count || 0})
              </span>
            </div>
          )}
          cellClick={(e) => {
            console.log('Cell clicked:', e)
            if (e.field === 'remark') {
              handleRemarkCellClick(e.dataItem)
            }
          }}
          // onCellClick={(e) => {
          //   if (e.field === 'remark') handleRemarkCellClick(e.dataItem)
          // }}
          resizable={true}
        >
          {columns
            .filter((col) => !hiddenFields.includes(col.field))
            .map((col) =>
              col.field === 'NormParameterFKId' ||
              col.field === 'materialFkId' ||
              col.field === 'normParameterId' ||
              col.field === 'normParametersFKId' ? (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  width={col.width}
                  cells={{
                    data: NormParameterIdCell,
                  }}
                />
              ) : (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  width={col.width}
                />
              ),
            )}
        </Grid>
      </div>
      {(permissions?.allActionOfBottomBtns ?? true) && (
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
          {/* {permissions?.showCreateCasebutton && (
            <Button
              variant='contained'
              onClick={createCase}
              disabled={isCreatingCase || !showCreateCasebutton}
              className='btn-save'
            >
              {isCreatingCase ? 'Submitting…' : 'Submit'}
            </Button>
          )} */}

          {permissions?.approveBtn && (
            <Button
              variant='contained'
              className='btn-save'
              onClick={saveModalOpen}
              disabled={isButtonDisabled}
              // loading={loading}
              // loadingposition='start'
              {...(loading ? {} : {})}
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
              onClick={handleDeleteSelected}
              disabled={isButtonDisabled}
              loading={loading} // Use the loading prop to trigger loading state
              loadingposition='start' // Use loadingPosition to control where the spinner appears
            >
              Delete
            </Button>
          )}
        </Box>
      )}

      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />

      <Dialog
        open={openDeleteDialogeBox}
        onClose={() => setOpenDeleteDialogeBox(false)}
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
          <Button onClick={() => setOpenDeleteDialogeBox(false)}>Cancel</Button>
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
    </div>
  )
}

export default KendoDataTables
