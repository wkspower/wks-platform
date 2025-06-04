import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import '@progress/kendo-font-icons/dist/index.css'
import { filterIcon } from '@progress/kendo-svg-icons'
import { ColumnMenu } from 'components/data-tables/Reports/columnMenu'
// import { EditDescriptor } from '@progress/kendo-react-data-tools'

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
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'
import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import { truncateRemarks } from 'utils/remarksUtils'
import { process } from '@progress/kendo-data-query'
import DateTimePickerEditor from './Utilities-Kendo/DatePickeronSelectedYr'
import { updateRowWithDuration } from './Utilities-Kendo/AutoDuration'
import ProductDropDownEditor from './Utilities-Kendo/DropdownProducts'
import ProductCell from './Utilities-Kendo/ProductCell'

const KendoDataTables = ({
  // setUpdatedRows = () => {},
  rows = [],
  // updatedRows = [],
  setRows,
  columns,
  loading = false,
  // pageSizes = [10, 20, 50],
  // onRowChange,
  // disableColor = false,
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
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
  deleteRowData = () => {},
  handleAddPlantSite = () => {},
  handleCalculate = () => {},
  fetchData = () => {},
  handleUnitChange = () => {},
  handleRemarkCellClick = () => {},
  selectedUsers = [],
  groupBy = null,
  allProducts = [],
  // allRedCell = [],
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [group, setGroup] = useState([])
  const [expandedState, setExpandedState] = useState({})
  const [selectedUnit, setSelectedUnit] = useState()
  const [selectMode, setSelectMode] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  // const closeDeleteDialogeBox = () => setOpenDeleteDialogeBox(false)
  // const [resizedColumns, setResizedColumns] = useState({})
  // const [edit, setEdit] = React.useState({})
  // const [searchText, setSearchText] = useState('')
  // const isFilterActive = false
  // const localApiRef = useGridApiRef()
  // const finalExternalApiRef = apiRef ?? localApiRef
  // const handleSearchChange = (event) => {
  //   setSearchText(event.target.value)
  // }
  // // console.log(columns)
  // const handleEditChange = (e) => {
  //   console.log(e)
  //   setEdit(e.edit)
  // }

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

  // const itemChange = (e) => {
  //   let updated = rows.map((r) =>
  //     r.id === e.dataItem.id ? { ...r, [e.field]: e.value } : r,
  //   )
  //   setRows(updated)
  //   setModifiedCells((updated = updated.filter((row) => row.inEdit == true)))

  // }
  const itemChange = useCallback(
    (e) => {
      const { dataItem, field, value } = e

      setRows((prev) =>
        prev.map((r) =>
          r.id === dataItem.id ? updateRowWithDuration(r, field, value) : r,
        ),
      )

      setModifiedCells((prev) => {
        const updatedRow = updateRowWithDuration(dataItem, field, value)
        return { ...prev, [dataItem.id]: updatedRow }
      })
    },
    [setRows, setModifiedCells],
  )

  const rowRender = (trElement, props) => {
    console.log(props.dataItem)
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

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null
      let keyToUpdate = ''
      const updatedRows = prevRows.map((row) => {
        // console.log(currentRowId, row.id)
        if (row.id === currentRowId) {
          const keysToUpdate = ['aopRemarks', 'remarks', 'remark'].filter(
            (key) => key in row,
          )
          //          console.log(keysToUpdate)
          keyToUpdate = keysToUpdate[0] || 'remark'
          //          console.log([keyToUpdate])
          updatedRow = { ...row, [keyToUpdate]: currentRemark, inEdit: true }
          return updatedRow
        }
        return row
      })
      // console.log(updatedRow)

      if (updatedRow) {
        setModifiedCells((prev) => ({
          ...prev,
          [updatedRow.id]: updatedRow,
        }))
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
  const handleDeleteClick = async (params) => {
    setParamsForDelete(params)
    setOpenDeleteDialogeBox(true)
  }
  const deleteTheRecord = async () => {
    deleteRowData(paramsForDelete)
    setOpenDeleteDialogeBox(false)
  }
  const ActionsCell = ({ dataItem }) => {
    return (
      <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
        <SvgIcon
          onClick={() => handleDeleteClick(dataItem)}
          icon={trashIcon}
          themeColor='dark'
        />
      </td>
    )
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
    // console.log('22', e.dataItem.isEditable)
    if (!e.dataItem?.isEditable) return

    setRows(
      rows.map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id, // only that row goes into edit mode
      })),
    )
  }

  const particulars = [
    'normParameterId',
    'normParametersFKId',
    'NormParameterFKId',
    'materialFkId',
    'normParameterFKId',
  ]
  const RemarkCell = (props) => {
    const { dataItem, field, onRemarkClick, ...tdProps } = props

    const rawValue = dataItem[field]
    const displayText = truncateRemarks(rawValue)
    // const editable = Boolean(dataItem.isEditable)

    return (
      <td
        {...tdProps}
        style={{
          cursor: 'pointer',
          color: rawValue ? 'inherit' : 'gray',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
        }}
        onClick={() => {
          onRemarkClick(dataItem)
        }}
      >
        {displayText || 'Click to add remark'}
      </td>
    )
  }

  useEffect(() => {
    if (Array.isArray(rows) && rows.length > 0 && groupBy) {
      setGroup([{ field: groupBy }])

      const initialExpandedState = {}
      const uniqueValues = [...new Set(rows.map((row) => row[groupBy]))]
      uniqueValues.forEach((value) => {
        initialExpandedState[`${groupBy}_${value}`] = true
      })
      setExpandedState(initialExpandedState)
    } else {
      setGroup([])
      setExpandedState({})
    }
  }, [rows, groupBy])

  const processedData = useMemo(() => {
    if (!Array.isArray(rows) || rows.length === 0) return []

    if (group.length > 0) {
      const result = process(rows, { group })
      const applyExpandedState = (items) => {
        return items.map((item) => {
          if (item.items) {
            const key = `${item.field}_${item.value}`
            item.expanded = expandedState[key] !== false // default to expanded
            item.items = applyExpandedState(item.items)
          }
          return item
        })
      }
      return applyExpandedState(result.data)
    }

    return rows
  }, [rows, group, expandedState])

  // console.log(processedData)
  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <div className='k-loading-mask'>
          <span className='k-loading-text'>Loading...</span>
          <div className='k-loading-image' />
          <div className='k-loading-color' />
        </div>
      )}
      {(permissions?.allAction ?? false) && (
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
            {permissions?.showModes && (
              <TextField
                select
                value={selectMode || permissions?.modes?.[0]}
                onChange={(e) => {
                  setSelectMode(e.target.value)
                  fetchData()
                }}
                sx={{ width: '150px', backgroundColor: '#FFFFFF' }}
                variant='outlined'
                label='Select Modes'
              >
                <MenuItem value='' disabled>
                  Select Modes
                </MenuItem>

                {/* Render the correct unit options dynamically */}
                {permissions?.modes.map((unit) => (
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
          editField='inEdit'
          // editable={{ mode: 'incell' }}
          editable='incell'
          // onRowClick={(e) => {
          //   const id = e.dataItem.id
          //   setRows(rows.map((r) => (r.id === id ? { ...r, inEdit: true } : r)))
          // }}
          // onEditChange={handleEditChange}
          // autoProcessData={true}
          // edit={edit}
          // scrollable='scrollable'
          filter={filter}
          // filterable={true}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={itemChange}
          rowRender={rowRender}
          resizable={true}
          defaultSkip={0}
          defaultTake={100}
          columnMenuIcon={filterIcon}
          contextMenu={true}
          pageable={
            rows?.length > 100
              ? {
                  buttonCount: 4,
                  pageSizes: [10, 50, 100],
                }
              : false
          }
          // onBlur={() => {
          //   // whenever the Grid loses focus, clear every row’s inEdit flag
          //   // setRows(rows.map((r) => ({ ...r, inEdit: false })))
          //   setEdit({})
          // }}
          group={group}
          expandField='expanded'
          onGroupChange={(e) => setGroup(e.group)}
          onExpandChange={(e) => {
            const key = e.field || e.value // Use appropriate unique identifier
            setExpandedState({
              ...expandedState,
              [key]: e.expanded,
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
          onRowClick={handleRowClick}
        >
          {columns
            .filter((col) => !hiddenFields.includes(col.field))
            .map((col) => {
              // console.log(col.editable)
              if (
                ['maintStartDateTime', 'maintEndDateTime'].includes(col.field)
              ) {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    filter='date'
                    format='{0:dd/MM/yyyy hh:mm tt}'
                    cells={{
                      edit: {
                        date: DateTimePickerEditor,
                      },
                    }}
                    columnMenu={ColumnMenu}
                    editor='date'
                  />
                )
              }
              if (col?.field === 'product') {
                return (
                  <GridColumn
                    key='product'
                    field='product'
                    title={col.title || col.headerName || 'Particulars'}
                    width={210}
                    editable={col.editable || true}
                    // cells={{
                    //   data: (props) => (
                    //     <ProductDropDownEditor
                    //       {...props}
                    //       allProducts={allProducts}
                    //     />
                    //   ),
                    // }}

                    cells={{
                      data: (cellProps) => (
                        <ProductCell {...cellProps} allProducts={allProducts} />
                      ),
                    }}
                    columnMenu={ColumnMenu}
                  />
                )
              }

              if (['aopRemarks', 'remarks', 'remark'].includes(col.field)) {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    editor={true}
                    // editable={col.editable || true}
                    editable={{ mode: 'popup' }}
                    cells={{
                      data: (cellProps) => (
                        <RemarkCell
                          {...cellProps}
                          onRemarkClick={handleRemarkCellClick}
                        />
                      ),
                    }}
                    columnMenu={ColumnMenu}
                    // editor='date'
                  />
                )
              }
              if (col.field === 'durationInHrs') {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    editable={col.editable || false} // make it read‑only
                    columnMenu={ColumnMenu} // if you want columnMenu
                    // optionally format with 2 decimals
                    format='{0:n2}'
                  />
                )
              }
              if (particulars.includes(col.field)) {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    editable={false}
                    filterable={true}
                    cells={{
                      data: NormParameterIdCell,
                    }}
                    columnMenu={ColumnMenu}
                  />
                )
              }

              return (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  width={col.width}
                  editable={true}
                  columnMenu={ColumnMenu}
                />
              )
            })}

          {permissions?.deleteButton && (
            <GridColumn
              key='actions'
              field='actions'
              title='Action'
              width={80}
              className='k-text-center'
              filterable={false}
              editable={false}
              cells={{
                data: ActionsCell,
              }}
            />
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
