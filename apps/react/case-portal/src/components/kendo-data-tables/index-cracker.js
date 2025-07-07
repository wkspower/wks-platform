import '@progress/kendo-font-icons/dist/index.css'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { useCallback, useEffect, useRef, useState } from 'react'
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import '../../kendo-data-grid.css'

import DownloadIcon from '@mui/icons-material/Download'
import UploadIcon from '@mui/icons-material/Upload'
import Notification from 'components/Utilities/Notification'
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'
import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import DateTimePickerEditor from './Utilities-Kendo/DatePickeronSelectedYr'
import MonthCell from './Utilities-Kendo/MonthCell'
import { NoSpinnerNumericEditor } from './Utilities-Kendo/numbericColumns'
import ProductCell from './Utilities-Kendo/ProductCell'
import { TextCellEditor } from './Utilities-Kendo/TextCellEditor'

import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports-kendo/ColumnMenu1'
import { DateColumnMenu } from 'components/Utilities/DateColumnMenu'
import {
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../node_modules/@progress/kendo-react-grid/index'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'
import DateOnlyPicker from './Utilities-Kendo/DatePicker'
import { RemarkCell } from './Utilities-Kendo/RemarkCell'
import { Switch } from '@progress/kendo-react-inputs';
const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))

const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

export const dateFields = [
  'maintStartDateTime',
  'maintEndDateTime',
  'fromDate',
  'toDate',
]
export const dateFields1 = [
  'ibrSD',
  'ibrED',
  'taSD',
  'taED',
  'sdED',
  'sdSD',
  'date',
]
export const hiddenFields = []
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

const KendoDataTablesCracker = ({
  rows = [],
  grades = [],
  allRedCell = [],
  modifiedCells = [],
  setRows,
  columns,
  summaryEdited,
  loading = false,
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  setCurrentRemark = () => {},
  currentRowId = null,
  setModifiedCells = () => {},
  remarkDialogOpen = false,
  handleDeleteSelected = () => {},
  saveChanges = () => {},
  deleteRowData = () => {},
  handleCalculate = () => {},
  handleGradeChange = () => {},
  handleRemarkCellClick = () => {},
  selectedUsers = [],
  groupBy = null,
  note = '',
  titleName = '',
  allProducts = [],
  allMonths = [],
  handleExcelUpload = () => {},
  downloadExcelForConfiguration = () => {},
  onLoad = () => {},
}) => {
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [selectedGrade, setSelectedGrade] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)
  const initialGroup = groupBy
    ? [
        {
          field: groupBy,
          dir: undefined,
        },
      ]
    : []
  const fileInputRef = useRef(null)
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
      setIsRowEdited(true)
      const { dataItem, field, value } = e
      const itemId = dataItem.id
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r
          const updated = { ...r, [field]: value }
          return updated
        }),
      )
      setModifiedCells((prev) => {
        const base = { ...dataItem, [field]: value }
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
          const keysToUpdate = ['aopRemarks', 'remarks', 'remark'].filter(
            (key) => key in row,
          )
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
  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]
    const month = monthMap[props.field?.toLowerCase()]
    const normId = props.dataItem.materialFkId
    const isRedFromAllRedCell = allRedCell.some(
      (cell) =>
        cell.month === month &&
        cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
    )
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
  const triggerFileUpload = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click()
    }
  }
  const onFileChange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    handleExcelUpload(file)
    event.target.value = ''
  }

  const renderGrid = () => (
    <Grid
      scrollable='virtual'
      modifiedCells={modifiedCells}
      autoProcessData={true}
      defaultGroup={initialGroup}
      data={rows}
      rows={{ data: CustomRow }}
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
      defaultTake={30}
      contextMenu={true}
      grade={grades}
      onRowClick={handleRowClick}
      sortable={{
        mode: 'multiple',
      }}
      allRedCell={allRedCell}
      size='small'
      pageable={
        rows?.length > 30
          ? {
              buttonCount: 4,
              pageSizes: [30, 50, 100],
            }
          : false
      }
    >
      {columns.map((col) => {
        const isActive = isColumnActive(col?.field, filter, sort)
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
                edit: {
                  date: ['fromDate', 'toDate'].includes(col.field)
                    ? DateOnlyPicker
                    : DateTimePickerEditor,
                },
                data: toolTipRenderer,
              }}
              format={
                ['fromDate', 'toDate'].includes(col.field)
                  ? '{0:dd-MM-yyyy}'
                  : '{0:dd-MM-yyyy hh:mm a}'
              }
              editor='date'
              hidden={col.hidden}
              columnMenu={DateColumnMenu}
            />
          )
        }
        if (dateFields1.includes(col.field)) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              cells={{
                edit: {
                  date: DateOnlyPicker,
                },
                data: toolTipRenderer,
              }}
              format='{0:dd-MM-yyyy}'
              editor='date'
              hidden={col.hidden}
              sortable={false}
            />
          )
        }
        if (col?.field === 'productName1') {
          return (
            <GridColumn
              key='productName1'
              field='productName1'
              title={col.title || col.headerName || 'Particulars'}
              // width={210}
              editable={col.editable || true}
              hidden={col.hidden}
              cells={{
                data: (cellProps) => (
                  <ProductCell {...cellProps} allProducts={allProducts} />
                ),
              }}
              columnMenu={ColumnMenuCheckboxFilter}
            />
          )
        }
        if (col?.field === 'month') {
          return (
            <GridColumn
              key='month'
              field='month'
              title={col.title || col.headerName || 'month'}
              editable={col.editable || true}
              hidden={col.hidden}
              width={col.widthT}
              cells={{
                data: (cellProps) => (
                  <MonthCell {...cellProps} allMonths={allMonths} />
                ),
              }}
              columnMenu={ColumnMenuCheckboxFilter}
            />
          )
        }
        if (
          ['aopRemarks', 'remarks', 'remark', 'Remarks'].includes(col.field)
        ) {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              editor={true}
              editable={{ mode: 'popup' }}
              cells={{
                data: (cellProps, allRedCell) => (
                  <RemarkCell
                    {...cellProps}
                    allRedCell={allRedCell} // pass your extra flag
                    onRemarkClick={handleRemarkCellClick}
                  />
                ),
              }}
              columnMenu={ColumnMenuCheckboxFilter}
              hidden={col.hidden}
            />
          )
        }
        if (col.type === 'number') {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              hidden={col.hidden}
              className={
                col?.isDisabled ? 'k-number-right-disabled' : 'k-number-right'
              }
              editable={col?.editable ? true : false}
              headerClassName={isActive ? 'active-column' : ''}
              cells={{
                edit: { text: NoSpinnerNumericEditor },
                data: toolTipRenderer,
              }}
              filter='numeric'
              format={col.format}
              sortable={false}
            />
          )
        }
        if (col.type === 'text') {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              // width={col.width}
              hidden={col.hidden}
              className={
                col?.isDisabled ? 'k-number-right-disabled' : 'k-number-right'
              }
              editable={col?.editable ? true : false}
              headerClassName={isActive ? 'active-column' : ''}
              cells={{
                edit: { text: NoSpinnerNumericEditor },
                data: toolTipRenderer,
              }}
              columnMenu={ColumnMenuCheckboxFilter}
              filter='numeric'
              format={col.format}
            />
          )
        }
        if (col.type === 'numberWidth') {
          return (
            <GridColumn
              key={col.field}
              field={col.field}
              title={col.title || col.headerName}
              width={col.width}
              hidden={col.hidden}
              className={
                col?.isDisabled ? 'k-number-right-disabled' : 'k-number-right'
              }
              editable={col?.editable ? true : false}
              headerClassName={isActive ? 'active-column' : ''}
              cells={{
                edit: { text: NoSpinnerNumericEditor },
                data: toolTipRenderer,
              }}
              columnMenu={ColumnMenuCheckboxFilter}
              filter='numeric'
              format={col.format}
            />
          )
        }
  //--
  if (col.type === 'switch') {
  const handleSwitchChange = (props, value) => {
    itemChange({
      dataItem: props.dataItem,
      field: props.field,
      value: value,
    });
  };

  return (
    <GridColumn
      key={col.field} // Fixed typo: was col.fieldf
      field={col.field}
      title={col.title || col.headerName}
      width={col.width || 150}
      hidden={col.hidden}
      editable={true}
      headerClassName={isColumnActive(col?.field, filter, sort) ? 'active-column' : ''}
      columnMenu={ColumnMenuCheckboxFilter}
      cells={{
        edit: {
          text: (props) => (
            <td style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }} >
              <Switch
              className="custom-switch"
                checked={!!props.dataItem[props.field]}
                onChange={(e) => handleSwitchChange(props, e.value)} // Kendo uses e.value
              />
            </td>
          ),
        },
        data: (props) => (
          <td style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <Switch
            className="custom-switch"
              checked={!!props.dataItem[props.field]}
              onChange={(e) => handleSwitchChange(props, e.value)} // Kendo uses e.value
            />
          </td>
        ),
      }}
    />
  )
}
//---
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            width={col.widthT}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: { text: TextCellEditor },
              data: toolTipRenderer,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
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
  )

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
              justifyContent: 'space-between',
              width: '100%',
              p: 1,
            }}
          >
            {/* Left side - Note */}
            <Box>
              {permissions?.showNote && (
                <Typography component='div' className='text-note'>
                  {note}
                </Typography>
              )}
            </Box>
            {/* Right side - All other actions */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {permissions?.downloadExcelBtn && (
                <Tooltip>
                  <span title='Export Data'>
                    <Button
                      variant='outlined'
                      size='large'
                      onClick={downloadExcelForConfiguration}
                      disabled={isButtonDisabled}
                    >
                      <DownloadIcon fontSize='small' />
                    </Button>
                  </span>
                </Tooltip>
              )}
              {permissions?.uploadExcelBtn && (
                <Tooltip>
                  <span title='Import Data'>
                    <Button
                      variant='outlined'
                      size='large'
                      onClick={triggerFileUpload}
                      disabled={isButtonDisabled}
                    >
                      <UploadIcon fontSize='small' />
                    </Button>
                  </span>
                  <input
                    type='file'
                    accept='.xlsx,.xls'
                    onChange={onFileChange}
                    ref={fileInputRef}
                    style={{ display: 'none' }}
                  />
                </Tooltip>
              )}
              {permissions?.saveBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={saveModalOpen}
                  disabled={
                    isButtonDisabled ||
                    (!summaryEdited && Object.keys(modifiedCells).length === 0)
                  }
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
                      : isButtonDisabled ||
                        !permissions?.showCalculateVisibility
                  }
                  className='btn-save'
                >
                  Calculate
                </Button>
              )}
            </Box>
          </Box>
        </Box>
      )}
      <div className='kendo-data-grid'>
        <>
          {permissions?.showAccordian ? (
            <CustomAccordion
              defaultExpanded={!permissions?.byDefCollaps}
              disableGutters
            >
              <CustomAccordionSummary
                aria-controls='meg-grid-content'
                id='meg-grid-header'
              >
                <Typography component='span' className='grid-title'>
                  {titleName}
                </Typography>
              </CustomAccordionSummary>
              <CustomAccordionDetails>
                <Tooltip
                  openDelay={50}
                  position='default'
                  anchorElement='target'
                >
                  {renderGrid()}
                </Tooltip>
              </CustomAccordionDetails>
            </CustomAccordion>
          ) : (
            <Tooltip openDelay={50} position='default' anchorElement='target'>
              {renderGrid()}
            </Tooltip>
          )}
        </>
      </div>
      <Box
        sx={{
          marginTop: 2,
          display: 'flex',
          gap: 2,
        }}
      >
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

export default KendoDataTablesCracker
