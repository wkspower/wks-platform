import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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


import DownloadIcon from '@mui/icons-material/Download'
import UploadIcon from '@mui/icons-material/Upload'
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
import { DurationEditor } from './Utilities-Kendo/numericViewCells'
import {
  recalcDuration,
  recalcEndDate,
} from './Utilities-Kendo/durationHelpers'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'
import * as XLSX from 'xlsx'
import { DataService } from 'services/DataService'

export const particulars = [
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

export const hiddenFields1 = [
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
export const hiddenFields = []
import { useSession } from 'SessionStoreContext'



const KendoDataTables = ({
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
  handleExcelUpload = () => {},
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
  const [issRowEdited, setIsRowEdited] = useState(false)
  const keycloak = useSession()
  const handleEditChange = useCallback((e) => {
    setEdit(e.edit)
  }, [])
  const handleRowClick = (e) => {
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

  const itemChange = useCallback(
    (e) => {
      const changedDataItem = e.dataItem
      const changedField = e.field
      const newValue = e.value

      const originalDataItem = rows.find(
        (item) => item.id === changedDataItem.id,
      )
      const originalValue = originalDataItem
        ? originalDataItem[changedField]
        : undefined

      //console.log('--- Cell Value Changed ---')
      //console.log('Row ID:', changedDataItem.id)
      //console.log('Field (Column Name):', changedField)
      //console.log('Original Value:', originalValue)
      //console.log('New Value:', newValue)
      //console.log('Full Changed Data Item:', changedDataItem)

      // setEditedId(changedDataItem.id)
      // setEditedValue(changedField)

      setIsRowEdited(true)

      const { dataItem, field, value } = e
      const itemId = dataItem.id
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r
          const updated = { ...r, [field]: value }

          if (
            'maintStartDateTime' in updated &&
            'maintEndDateTime' in updated &&
            'durationInHrs' in updated
          ) {
            if (
              field === 'maintStartDateTime' ||
              field === 'maintEndDateTime'
            ) {
              updated.durationInHrs = recalcDuration(
                updated.maintStartDateTime,
                updated.maintEndDateTime,
              )
            } else if (field === 'durationInHrs') {
              const newEnd = recalcEndDate(
                updated.maintStartDateTime,
                value, // string like “10.20”
              )
              if (newEnd) {
                updated.maintEndDateTime = newEnd.toISOString()
              }
            }
          }
          return updated
        }),
      )

      setModifiedCells((prev) => {
        const base = { ...dataItem, [field]: value }
        if (
          'maintStartDateTime' in base &&
          'maintEndDateTime' in base &&
          'durationInHrs' in base
        ) {
          if (field === 'maintStartDateTime' || field === 'maintEndDateTime') {
            base.durationInHrs = recalcDuration(
              base.maintStartDateTime,
              base.maintEndDateTime,
            )
          } else if (field === 'durationInHrs') {
            const newEnd = recalcEndDate(base.maintStartDateTime, value)
            if (newEnd) base.maintEndDateTime = newEnd.toISOString()
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

const downloadExcelForConfiguration = async () => {
  try {
        
        // Await the API call here to ensure completion
        const data = await DataService.getConfigurationExcel(keycloak)
  
        return data
      } catch (error) {
        
        console.error('Error!', error)
      } finally {
        
      }
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
  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

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

  const initialGroup = groupBy ? [{ field: groupBy }] : []

  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]

    return (
      <td {...props.tdProps} title={value}>
        {props.children}
      </td>
    )
  }

  const HeaderWithTooltip = (props) => {
    return (
      <th {...props.thProps}>
        <a className='k-link' onClick={props.onClick}>
          <span title={props.title}>{props.title}</span>
        </a>
      </th>
    )
  }

  const triggerFileUpload = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click()
    }
  }

  const fileInputRef = useRef(null)

  const onFileChange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    handleExcelUpload(file)
  }

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

            {permissions?.downloadExcelBtn && (
              <Tooltip title='Download'>
                <Button
                  variant='outlined'
                  size='small'
                  onClick={downloadExcelForConfiguration}
                  disabled={isButtonDisabled}
                >
                  <DownloadIcon fontSize='small' />
                </Button>
              </Tooltip>
            )}

            {permissions?.uploadExcelBtn && (
              <>
                <Tooltip title='Upload Excel'>
                  <Button
                    variant='outlined'
                    size='small'
                    onClick={triggerFileUpload}
                    disabled={isButtonDisabled}
                  >
                    <UploadIcon fontSize='small' />
                  </Button>
                </Tooltip>
                <input
                  type='file'
                  accept='.xlsx,.xls'
                  onChange={onFileChange}
                  ref={fileInputRef}
                  style={{ display: 'none' }}
                />
              </>
            )}

            {permissions?.saveBtn && (
              <Button
                variant='contained'
                className='btn-save'
                onClick={saveModalOpen}
                disabled={isButtonDisabled || !issRowEdited}
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
        <Tooltip openDelay={50} position='default' anchorElement='target'>
          <Grid
            autoProcessData={true}
            defaultGroup={initialGroup}
            data={rows}
            rows={{ data: CustomRow }}
            sortable={{
              mode: 'multiple',
            }}
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
                        edit: { date: DateTimePickerEditor },
                        data: toolTipRenderer,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      editor='date'
                      hidden={col.hidden}
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
                      hidden={col.hidden}
                      cells={{
                        data: (cellProps) => (
                          <ProductCell
                            {...cellProps}
                            allProducts={allProducts}
                          />
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
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                      }}
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
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                      }}
                    />
                  )
                }

                // const isColumnActive = (field, filter, sort) => {
                //   return (
                //     isColumnMenuFilterActive(field, filter) ||
                //     isColumnMenuSortActive(field, sort)
                //   )
                // }
                const isActive = isColumnActive(col?.field, filter, sort)

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
                      hidden={col.hidden}
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
                      editable={true}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      format={'{0:n2}'}
                      cells={{
                        edit: { text: DurationEditor },
                        data: toolTipRenderer,
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
                      hidden={col.hidden}
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
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                      }}
                    />
                  )
                }

                return (
                  <GridColumn
                    key={col.field}
                    field={col.field}
                    title={col.title || col.headerName}
                    width={col.width}
                    hidden={col.hidden}
                    className={
                      col?.isDisabled
                        ? 'k-number-right-disabled'
                        : 'k-number-right'
                    }
                    editable={col?.isDisabled ? false : true}
                    // headerClassName={isActive ? 'active-column' : ''}
                    cells={{
                      edit: { text: NoSpinnerNumericEditor },
                      data: toolTipRenderer,
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
        </Tooltip>
      </div>
      {/* {(permissions?.allActionOfBottomBtns ?? true) && ( */}
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
      {/* )} */}
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
