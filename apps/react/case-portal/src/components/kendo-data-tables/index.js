import { useCallback, useEffect, useMemo, useState } from 'react'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import '@progress/kendo-font-icons/dist/index.css'
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
import ProductCell from './Utilities-Kendo/ProductCell'
import { ColumnMenu } from 'components/@extended/columnMenu'
import { NoSpinnerNumericEditor } from './Utilities-Kendo/numbericColumns'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports-kendo/ColumnMenu1'
import {
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../node_modules/@progress/kendo-react-grid/index'

export const particulars = [
  'normParameterId',
  'normParametersFKId',
  'NormParameterFKId',
  'materialFkId',
  'materialFKId',
  'normParameterFKId',
  'NormParametersId',
]
export const typeParticulars = [
  'Particulars',
  'TypeDisplayName',
  'ConfigTypeDisplayName',
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

const KendoDataTables = ({
  // setUpdatedRows = () => {},

  rows = [],
  // updatedRows = [],
  setRows,
  columns,
  loading = false,
  typeRank = {},
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
  selectMode,
  setSelectMode = () => {},
  // allRedCell = [],
}) => {
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

  const [filter, setFilter] = useState({ logic: 'and', filters: [] })

  const [sort, setSort] = useState([])

  // const gridApiRef = useRef()
  //  const apiRef = useGridApiRef();

  // // const localApiRef = useGridApiRef()
  // // const finalExternalApiRef = apiRef ?? localApiRef
  // useEffect(() => {
  //   if (gridApiRef .current) {
  //     gridApiRef .current.editCell({ rowIndex: 0, field: 'name' })
  //   }
  // }, [])
  const handleEditChange = useCallback((e) => {
    // console.log('e--->', e)
    setEdit(e.edit)
  }, [])
  const handleRowClick = (e) => {
    // console.log('22', e.dataItem?.isEditable)
    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      setEdit({})
      return
    }

    setRows(
      rows.map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id, // only that row goes into edit mode
      })),
    )
  }
  // const itemChange = useCallback(
  //   (e) => {
  //     // console.log(e)
  //     const { dataItem, field, value } = e

  //     setRows((prev) =>
  //       prev.map((r) =>
  //         r.id === dataItem.id ? updateRowWithDuration(r, field, value) : r,
  //       ),
  //     )

  //     setModifiedCells((prev) => {
  //       const updatedRow = updateRowWithDuration(dataItem, field, value)
  //       return { ...prev, [dataItem.id]: updatedRow }
  //     })
  //   },

  //   [setRows, setModifiedCells],
  // )

  const itemChange = useCallback(
    (e) => {
      const { dataItem, field, value } = e

      // helper to recalc duration based on start/end
      const recalcDuration = (startRaw, endRaw) => {
        const start = startRaw ? new Date(startRaw) : null
        const end = endRaw ? new Date(endRaw) : null
        if (
          start instanceof Date &&
          !isNaN(start) &&
          end instanceof Date &&
          !isNaN(end)
        ) {
          const diffMs = end.getTime() - start.getTime()
          return parseFloat((diffMs / (1000 * 60 * 60)).toFixed(2))
        }
        return 0
      }

      // helper to recalc end date based on start + duration
      const recalcEndDate = (startRaw, durationHrs) => {
        const start = startRaw ? new Date(startRaw) : null
        if (start instanceof Date && !isNaN(start) && !isNaN(durationHrs)) {
          return new Date(start.getTime() + durationHrs * 3600 * 1000)
        }
        return null
      }

      // update rows in one pass
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== dataItem.id) return r

          // apply the edit
          const updated = { ...r, [field]: value }

          if (field === 'maintStartDateTime' || field === 'maintEndDateTime') {
            updated.durationInHrs = recalcDuration(
              updated.maintStartDateTime,
              updated.maintEndDateTime,
            )
          } else if (field === 'durationInHrs') {
            const newEnd = recalcEndDate(
              updated.maintStartDateTime,
              parseFloat(value),
            )
            if (newEnd) {
              updated.maintEndDateTime = newEnd.toISOString()
            }
          }

          return updated
        }),
      )

      // mirror in modifiedCells
      setModifiedCells((prev) => {
        const base = { ...dataItem, [field]: value }

        if (field === 'maintStartDateTime' || field === 'maintEndDateTime') {
          base.durationInHrs = recalcDuration(
            base.maintStartDateTime,
            base.maintEndDateTime,
          )
        } else if (field === 'durationInHrs') {
          const newEnd = recalcEndDate(
            base.maintStartDateTime,
            parseFloat(value),
          )
          if (newEnd) base.maintEndDateTime = newEnd.toISOString()
        }

        return { ...prev, [dataItem.id]: base }
      })
    },
    [setRows, setModifiedCells],
  )

  // const itemChange = useCallback(
  //   (e) => {
  //     const { dataItem, field, value } = e
  //     setRows((prev) =>
  //       prev.map((r) => (r.id === dataItem.id ? { ...r, [field]: value } : r)),
  //     )
  //     setModifiedCells((prev) => ({
  //       ...prev,
  //       [dataItem.id]: { ...dataItem, [field]: value },
  //     }))
  //   },
  //   [setRows, setModifiedCells],
  // )
  // const onCellClose = useCallback(
  //   (e) => {
  //     const { dataItem, field } = e
  //     if (
  //       ['maintStartDateTime', 'maintEndDateTime', 'durationInHrs'].includes(
  //         field,
  //       )
  //     ) {
  //       setRows((prev) =>
  //         prev.map((r) =>
  //           r.id === dataItem.id
  //             ? updateRowWithDuration(r, field, r[field])
  //             : r,
  //         ),
  //       )
  //       setModifiedCells((prev) => ({
  //         ...prev,
  //         [dataItem.id]: updateRowWithDuration(
  //           dataItem,
  //           field,
  //           dataItem[field],
  //         ),
  //       }))
  //     }
  //   },
  //   [setRows, setModifiedCells],
  // )
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

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

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
    const rowClassName = isDisabled ? `k-disabled` : className

    return (
      <tr {...rest?.trProps} className={rowClassName}>
        {rest.children}
      </tr>
    )
  }, [])

  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)

  const initialGroup = groupBy ? [{ field: groupBy }] : []

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
            {permissions?.showCalculate && (
              <Button
                variant='contained'
                onClick={handleCalculateBtn}
                disabled={
                  rows?.length === 0
                    ? false
                    : isButtonDisabled || !permissions?.showCalculateVisibility
                }
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
                  // fetchData()
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
          // apiRef={gridApiRef}
          // groupable={true}
          autoProcessData={true}
          defaultGroup={initialGroup}
          data={rows}
          rows={{ data: CustomRow }}
          // rowRender={{ rowRender }}
          sortable
          dataItemKey='id'
          editField='inEdit'
          editable={{ mode: 'incell' }}
          // autoProcessData={true}
          // scrollable='scrollable'
          // filterable={true}
          // columnMenuIcon={filterIcon}
          // onBlur={() => {
          //   //setEdit({})
          // }}
          onEditChange={handleEditChange}
          edit={edit}
          filter={filter}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={itemChange}
          resizable={true}
          defaultSkip={0}
          defaultTake={100}
          contextMenu={true}
          pageable={
            rows?.length > 100
              ? {
                  buttonCount: 4,
                  pageSizes: [10, 50, 100],
                }
              : false
          }
          // group={group}
          // expandField='expanded'
          // onGroupChange={(e) => setGroup(e.group)}
          // onExpandChange={(e) => {
          //   const key = e.field || e.value // Use appropriate unique identifier
          //   setExpandedState({
          //     ...expandedState,
          //     [key]: e.expanded,
          //   })
          // }}
          // groupHeaderRender={(e) => (
          //   <div style={{ display: 'flex', alignItems: 'center' }}>
          //     <span style={{ marginRight: 6 }}>{e.expanded ? '▼' : '▶'}</span>
          //     <strong>{e.value}</strong>
          //     <span style={{ marginLeft: 6 }}>
          //       ({e.aggregates?.count || 0})
          //     </span>
          //   </div>
          // )}

          onRowClick={handleRowClick}
          // onCellClose={onCellClose}
        >
          {columns
            .filter((col) => !hiddenFields.includes(col.field))
            .map((col) => {
              // console.log(col.editable)
              if (
                [
                  'maintStartDateTime',
                  'maintEndDateTime',
                  'endDateTA',
                  'startDateTA',
                  'endDateSD',
                  'startDateSD',
                  'endDateIBR',
                  'startDateIBR',
                ].includes(col.field)
              ) {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    filter='date'
                    format='{0:dd/MM/yyyy hh:mm a}'
                    cells={{
                      edit: {
                        date: DateTimePickerEditor,
                      },
                    }}
                    columnMenu={ColumnMenuCheckboxFilter}
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
                    columnMenu={ColumnMenuCheckboxFilter}
                  />
                )
              }
              if (['discription', 'Name'].includes(col?.field)) {
                return (
                  <GridColumn
                    key={col?.field}
                    field={col?.field}
                    title={col.title || col.headerName || 'Description'}
                    width={210}
                    editable={true}
                    columnMenu={ColumnMenuCheckboxFilter}
                  />
                )
              }
              if (col?.field === 'UOM') {
                return (
                  <GridColumn
                    key='UOM'
                    field='UOM'
                    title={col.title || col.headerName || 'UOM'}
                    width={col?.width}
                    editable={false}
                    columnMenu={ColumnMenuCheckboxFilter}
                  />
                )
              }

              // const isColumnActive = (field, filter, sort) => {
              //   return (
              //     isColumnMenuFilterActive(field, filter) ||
              //     isColumnMenuSortActive(field, sort)
              //   )
              // }
              // const isActive = isColumnActive(col?.field, filter, sort)

              if (
                ['aopRemarks', 'remarks', 'remark', 'Remarks'].includes(
                  col.field,
                )
              ) {
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
                    columnMenu={ColumnMenuCheckboxFilter}
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
                    editable={true} // make it read‑only
                    columnMenu={ColumnMenuCheckboxFilter} // if you want columnMenu
                    // editor={(props) => (
                    //   <input
                    //     {...props}
                    //     value={props.value || ''}
                    //     onChange={(e) =>
                    //       props.onChange({
                    //         dataItem: props.dataItem,
                    //         field: props.field,
                    //         value: e.target.value,
                    //       })
                    //     }
                    //     onBlur={() =>
                    //       props.onBlur({
                    //         dataItem: props.dataItem,
                    //         field: props.field,
                    //         value: props.value,
                    //       })
                    //     }
                    //   />
                    // )}
                    format='{0:n2}'
                    cells={{
                      edit: { text: NoSpinnerNumericEditor },
                    }}
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
                    columnMenu={ColumnMenuCheckboxFilter}
                  />
                )
              }
              if (typeParticulars.includes(col.field)) {
                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    editable={false}
                    filterable={true}
                    columnMenu={ColumnMenuCheckboxFilter}
                  />
                )
              }

              return (
                <GridColumn
                  key={col.field}
                  field={col.field}
                  title={col.title || col.headerName}
                  width={col.width}
                  className='k-number-right'
                  editable={true}
                  // headerClassName={isActive ? 'active-column' : ''}
                  cells={{
                    edit: { text: NoSpinnerNumericEditor },
                  }}
                  columnMenu={ColumnMenuCheckboxFilter}
                  filter='numeric'
                  format={col.format || '{0:n3}'}
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
