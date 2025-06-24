import { filterBy, process } from '@progress/kendo-data-query'
import '@progress/kendo-font-icons/dist/index.css'
import {
  Grid,
  GridColumn,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { ColumnMenu } from 'components/@extended/columnMenu'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports/ColumnMenu1'
import Notification from 'components/Utilities/Notification'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { truncateRemarks } from 'utils/remarksUtils'
import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'

import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import '../../kendo-data-grid.css'
// import { updateRowWithDuration } from './Utilities-Kendo/AutoDuration'
// import FullValueEditor from './Utilities-Kendo/FullValueEditor'
// import { TextCellEditor } from './Utilities-Kendo/TextCellEditor'
import { NoSpinnerNumericEditor } from './Utilities-Kendo/numbericColumns'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'
import DateTimePickerEditor from './Utilities-Kendo/DatePickeronSelectedYr'
import {
  DurationDisplayWithTooltipCell,
  DurationEditor,
} from './Utilities-Kendo/numericViewCells'
import {
  recalcDuration,
  recalcEndDate,
} from './Utilities-Kendo/durationHelpers'
import DateOnlyPicker from './Utilities-Kendo/DatePicker'

export const particulars = [
  'normParameterId',
  'normParametersFKId',
  'NormParameterFKId',
  'materialFkId',
  'normParameterFKId',
]
export const hiddenFields = [
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
export const dateFields = [
  'maintStartDateTime',
  'maintEndDateTime',
  'endDateTA',
  'startDateTA',
  'endDateSD',
  'startDateSD',
  'endDateIBR',
  'startDateIBR',
  'toDate',
  'fromDate',
]
export const monthMap = {
  january: 1,
  february: 2,
  march: 3,
  april: 4,
  may: 5,
  june: 6,
  july: 7,
  august: 8,
  september: 9,
  october: 10,
  november: 11,
  december: 12,
}
const KendoDataTablesReports = ({
  allRedCell = [],
  modifiedCells = [],
  title = '',
  rows = [],
  setRows,
  columns,
  loading = false,
  typeRank = {},
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  NormParameterIdCell = () => {},
  setModifiedCells = () => {},
  remarkDialogOpen = false,
  handleDeleteSelected = () => {},
  saveChanges = () => {},
  fetchData = () => {},
  deleteRowData = () => {},
  handleAddPlantSite = () => {},
  handleCalculate = () => {},
  handleUnitChange = () => {},
  handleRemarkCellClick = () => {},
  selectedUsers = [],
  groupBy = null,
  allProducts = [],
  selectMode,
  setSelectMode = () => {},
  handleExport = () => {},
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [group, setGroup] = useState([])
  const [expandedState, setExpandedState] = useState({})
  const [selectedUnit, setSelectedUnit] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [sort, setSort] = useState([]) // or
  const [issRowEdited, setIsRowEdited] = useState(false)

  const handleEditChange = useCallback((e) => {
    setEdit(e.edit)
    // }
  }, [])

  const handleRowClick = (e) => {
    console.log(e.dataItem)
    // if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
    //   setEdit({})
    //   return
    // }

    setRows(
      rows.map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id,
      })),
    )
  }
 const itemChange = useCallback(
    (e) => {
      // const changedDataItem = e.dataItem
      // const changedField = e.field
      // const newValue = e.value

      // const originalDataItem = rows.find(
      //   (item) => item.id === changedDataItem.id,
      // )
      // const originalValue = originalDataItem
      //   ? originalDataItem[changedField]
      //   : undefined

      setIsRowEdited(true)

      const { dataItem, field, value } = e
      const itemId = dataItem.id
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r
          const updated = { ...r, [field]: value }

          if (
            'fromDate' in updated &&
            'toDate' in updated &&
            'durationInHrs' in updated
          ) {
            if (field === 'fromDate' || field === 'toDate') {
              updated.durationInHrs = recalcDuration(
                updated.fromDate,
                updated.toDate,
              )
            } else if (field === 'durationInHrs') {
              const newEnd = recalcEndDate(
                updated.fromDate,
                value, // string like “10.20”
              )
              if (newEnd) {
                updated.toDate = newEnd
              }
            }
          }
          return updated
        }),
      )

      setModifiedCells((prev) => {
        const base = { ...dataItem, [field]: value }
        if ('fromDate' in base && 'toDate' in base && 'durationInHrs' in base) {
          if (field === 'fromDate' || field === 'toDate') {
            base.durationInHrs = recalcDuration(base.fromDate, base.toDate)
          } else if (field === 'durationInHrs') {
            const newEnd = recalcEndDate(base.fromDate, value)
            if (newEnd) base.toDate = newEnd.toISOString()
          }
        }
        return { ...prev, [itemId]: base }
      })
    },
    [setRows, setModifiedCells],
  )

  const handleRemarkSave = () => {
    setRows((prevRows) => {
      let updatedRow = null
      let keyToUpdate = ''
      const updatedRows = prevRows.map((row) => {
        if (row.id === currentRowId) {
          const keysToUpdate = [
            'aopRemarks',
            'remarks',
            'remark',
            'Remark',
          ].filter((key) => key in row)
          keyToUpdate = keysToUpdate[0] || 'remark'
          updatedRow = { ...row, [keyToUpdate]: currentRemark, inEdit: true }
          return updatedRow
        }
        return row
      })
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

  const handleAddRow = () => {
    if (isButtonDisabled) return
    setIsButtonDisabled(true)
    console.log(rows)
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    console.log(newRowId)
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(columns.map((col) => [col.field, ''])),
    }

    setRows((prevRows) => [newRow, ...prevRows])

    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
    setEdit({})
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
  // console.log('22', e.dataItem.isEditable)
  // console.log('Rendering with title:', title, 'type:', typeof title)

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
          setEdit({})
        }}
        onDoubleClick={() => {
          onRemarkClick(dataItem)
          setEdit({})
        }}
      >
        {displayText || 'Click to add remark'}
      </td>
    )
  }

  // useEffect(() => {
  //   if (Array.isArray(rows) && rows.length > 0 && groupBy) {
  //     if (typeRank) {
  //       setGroup([
  //         {
  //           field: groupBy,
  //           dir: 'asc',
  //           compare: (a, b) => {
  //             const rankA = typeRank[a.value] ?? 99
  //             const rankB = typeRank[b.value] ?? 99
  //             return rankA - rankB
  //           },
  //         },
  //       ])
        // setGroup([{ field: groupBy }])
  //     }
  //     const initialExpandedState = {}
  //     const uniqueValues = [...new Set(rows.map((row) => row[groupBy]))]
  //     uniqueValues.forEach((value) => {
  //       initialExpandedState[`${groupBy}_${value}`] = true
  //     })
  //     setExpandedState(initialExpandedState)
  //   } else {
  //     setGroup([])
  //     setExpandedState({})
  //   }
  // }, [rows, groupBy])

  // const processedData = useMemo(() => {
  //   if (!Array.isArray(rows) || rows.length === 0) return []

  //   if (group.length > 0) {
  //     const result = process(rows, { group })
  //     const applyExpandedState = (items) => {
  //       return items.map((item) => {
  //         if (item.items) {
  //           const key = `${item.field}_${item.value}`
  //           item.expanded = expandedState[key] !== false // default to expanded
  //           item.items = applyExpandedState(item.items)
  //         }
  //         return item
  //       })
  //     }
  //     return applyExpandedState(result.data)
  //   }

  //   return rows
  // }, [rows, group, expandedState])

  const CustomRow = useCallback(({ dataItem, className, ...rest }) => {
    const isDisabled =
      !dataItem.isEditable && dataItem?.isEditable !== undefined
    const rowClassName = isDisabled ? `custom-disabled-row` : className
    return (
      <tr {...rest?.trProps} className={rowClassName}>
        {rest.children}
      </tr>
    )
  }, [])

  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)

  const NumberEditor = (props) => {
    const { dataItem, field, onChange } = props

    const handleChange = (event) => {
      const value = event.target.value
      onChange({
        dataItem,
        field,
        value: value === '' ? null : Number(value),
      })
    }

    return (
      <td>
        <input
          type='number'
          step='any'
          value={dataItem[field] ?? ''}
          onChange={handleChange}
          style={{ width: '100%' }}
        />
      </td>
    )
  }

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }


  const renderColumns = (cols, filter, sort) =>
    cols.map((col, idx) => {
      if (col.children) {
        return (
          <GridColumn key={col.title || idx} title={col.title} editable={true}>
            {renderColumns(col.children, filter, sort)}
          </GridColumn>
        )
      }

      const isEditable = col.editable === true
      const isActive = isColumnActive(col.field, filter, sort)

      if (['aopRemarks', 'remarks', 'remark', 'Remark'].includes(col.field)) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            editor={true}
            editable={{ mode: 'popup' }}
            cells={{
              data: (cellProps) => (
                <RemarkCell
                  {...cellProps}
                  onRemarkClick={handleRemarkCellClick}
                />
              ),
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            headerClassName={isActive ? 'active-column' : ''}
          />
        )
      }
      if (dateFields.includes(col.field)) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            filter='date'
            filterable={{
              cell: {
                operator: 'gte',
                showOperators: true,
              },
            }}
            cells={{
              edit: { date: DateOnlyPicker },
              data: toolTipRenderer,
            }}
            format='{0:dd-MM-yyyy}'
            editor='date'
            editable={true}
            hidden={col.hidden}
            className={!isEditable ? 'non-editable-cell' : ''}
          />
        )
      }
      if (col.field.includes('durationInHrs')) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            editable={true}
            columnMenu={ColumnMenuCheckboxFilter}
            hidden={col.hidden}
            format={'{0:n2}'}
            className={!isEditable ? 'non-editable-cell' : ''}
            cells={{
              edit: { text: DurationEditor },
              data: DurationDisplayWithTooltipCell,
            }}
          />
        )
      }

      return (
        <GridColumn
          key={col.field}
          field={col.field}
          title={col.title || col.headerName}
          editable={col.editable || true}
          format={col.format || '{0:#.###}'}
          cells={{
            edit: { text: NoSpinnerNumericEditor },
            data: toolTipRenderer,
          }}
          className={!isEditable ? 'non-editable-cell' : ''}
          columnMenu={ColumnMenuCheckboxFilter}
          headerClassName={isActive ? 'active-column' : ''}
        />
      )
    })

  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]
    const month = monthMap[props.field?.toLowerCase()]
    const normId = props.dataItem.materialFkId

    const isRedFromAllRedCell = allRedCell.some(
      (cell) =>
        cell.month === month &&
        cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
    )

    // const isRedFromEdit =
    //   editedCellMap?.[rowId]?.[props.field] !== undefined &&
    //   editedCellMap?.[rowId]?.[props.field]?.toString() === value?.toString()

    // const isRed = isRedFromAllRedCell || isRedFromEdit
    const isRed = isRedFromAllRedCell

    return (
      <td
        {...props.tdProps}
        title={value}
        style={{
          color: isRed ? 'orange' : undefined,
        }}
      >
        {props.children}
      </td>
    )
  }


  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
      )}

      {(permissions?.allAction ?? true) && (
        <Box
          className='action-box2'
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            width: '100%', // make sure container is full width
            p: 1,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              flexGrow: 1, // ? key to take up all left space
            }}
          >
            {permissions?.showTitle && (
              <Typography
                component='div'
                className='grid-title'
                style={{ whiteSpace: 'pre-line' }}
              >
                {title}
              </Typography>
            )}
          </Box>

          {/* RIGHT: Buttons */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {permissions?.addButton && (
              <Button
                variant='contained'
                className='btn-save'
                onClick={handleAddRow}
                disabled={true}
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
            {permissions?.showCalculate && (
              <Button
                variant='contained'
                onClick={handleExport}
                disabled={isButtonDisabled}
                className='btn-save'
              >
                Export
              </Button>
            )}
            {permissions?.showFinalSubmit && (
              <Button
                variant='contained'
                // onClick={handleExport}
                // disabled={isButtonDisabled}
                className='btn-save'
              >
                Submit
              </Button>
            )}

            {/* {permissions?.showWorkFlowBtns && (
                    <Stack direction='row' spacing={1} alignItems='center'>
                      {taskId && (
                        <Button
                          variant='contained'
                          onClick={handleRejectClick}
                          disabled={isButtonDisabled}
                        >
                          Accept
                        </Button>
                      )}
                      <Button variant='outlined' onClick={handleAuditOpen}>
                        Audit Trail
                      </Button>
                    </Stack>
                  )} */}
          </Box>
        </Box>
      )}

      <div className='kendo-data-grid'>
        <Tooltip openDelay={50} position='default' anchorElement='target'>
          <Grid
            modifiedCells={modifiedCells}
            data={rows}
            rows={{ data: CustomRow }}
            sortable={{
              mode: 'multiple',
            }}
            autoProcessData={true}
            dataItemKey='id'
            editField='inEdit'
            editable={{ mode: 'incell' }}
            onEditChange={handleEditChange}
            edit={edit}
            filter={filter}
            onFilterChange={(e) => setFilter(e.filter)}
            onItemChange={itemChange}
            resizable={true}
            defaultSkip={0}
            defaultTake={100}
            contextMenu={true}
            filterable={columns.some((col) => dateFields.includes(col.field))}
            size='small'
            pageable={
              rows?.length > 100
                ? {
                    buttonCount: 4,
                    pageSizes: [10, 50, 100],
                  }
                : false
            }
            onRowClick={handleRowClick}
          >
            {renderColumns(
              columns.filter((col) => !hiddenFields.includes(col.field)),
              filter,
              sort,
            )}

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
        </Tooltip>
      </div>

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

export default KendoDataTablesReports
