import '@progress/kendo-font-icons/dist/index.css'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import { Tooltip } from '@progress/kendo-react-tooltip'
import '@progress/kendo-theme-default/dist/all.css'
import React, { useCallback, useEffect, useRef, useState } from 'react'
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
  Typography,
} from '../../../node_modules/@mui/material/index'
import '../../kendo-data-grid.css'

import Notification from 'components/Utilities/Notification'
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'
import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import DateTimePickerEditor from './Utilities-Kendo/DatePickeronSelectedYr'
import MonthCell from './Utilities-Kendo/MonthCell'
import { NoSpinnerNumericEditor } from './Utilities-Kendo/numbericColumns'
import ProductCell from './Utilities-Kendo/ProductCell'
import { TextCellEditor } from './Utilities-Kendo/TextCellEditor'

import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports-kendo/ColumnMenu1'
import {
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../node_modules/@progress/kendo-react-grid/index'
import {
  recalcDuration,
  recalcEndDate,
} from './Utilities-Kendo/durationHelpers'
import { DurationEditor } from './Utilities-Kendo/numericViewCells'
// import DateTimePickerr from './Utilities-Kendo/DatePicker'
import DateOnlyPicker from './Utilities-Kendo/DatePicker'
// import { DatePicker } from '../../../node_modules/@progress/kendo-react-dateinputs/index'
import { DateColumnMenu } from 'components/Utilities/DateColumnMenu'
import { descLimit } from './Utilities-Kendo/descLimit'
import { RemarkCell } from './Utilities-Kendo/RemarkCell'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../node_modules/@progress/kendo-react-excel-export/index'
import { useSelector } from 'react-redux'
import { Checkbox } from '../../../node_modules/@progress/kendo-react-inputs/index'
import LimitCellEditor from './Utilities-Kendo/LimitCellEditor'

export const dateFields = [
  'maintStartDateTime',
  'maintEndDateTime',
  'periodTo',
  'periodFrom',
  'toDateReport',
  'fromDateReport',
]
export const dateFields2 = ['fromDate', 'toDate']
export const dateFields1 = [
  'ibrSD',
  'ibrED',
  'taSD',
  'taED',
  'sdED',
  'sdSD',
  'targetDate',
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

const KendoDataTables = ({
  rows = [],
  plantID = null,
  grades = [],
  allRedCell = [],
  modifiedCells = [],
  setRows,
  columns,
  summaryEdited,
  loading = false,
  supressGridHeight = false,
  typeRank = {},
  permissions = {},
  errorRows = new Set(),
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
  handleLoad = () => {},
  fetchData = () => {},
  handleUnitChange = () => {},
  handleGradeChange = () => {},
  handleRemarkCellClick = () => {},
  calculatebtnClicked = () => {},
  selectedUsers = [],
  groupBy = null,
  selectedUOM = 'MT/Month',
  note = '',
  titleName = '',
  gridName,
  onGlobalCheckboxChange,

  allProducts = [],
  allMonths = [],
  selectMode,
  setSelectMode = () => {},
  handleExcelUpload = () => {},
  downloadExcelForConfiguration = () => {},
  onLoad = () => {},
  disableRedHighlight = false,
  showThreeColors = false,
}) => {
  const _export = useRef(null)
  const _grid = React.useRef(undefined)

  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [selectedUnit, setSelectedUnit] = useState(permissions?.units?.[0])
  const [selectedGrade, setSelectedGrade] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const [isDateFilterActive, setIsDateFilterActive] = useState([])
  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const initialGroup = groupBy
    ? [
        {
          field: groupBy,
          dir: undefined,
        },
      ]
    : []

  // console.log('selectedUOM', selectedUOM)

  const fileInputRef = useRef(null)

  const handleEditChange = useCallback((e) => {
    setEdit(e.edit)
  }, [])

  const excelExport = () => {
    if (_export.current !== null) {
      _export.current.save()
    }
  }

  const handleRowClick = (e) => {
    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      setEdit({})
      return
    }

    setRows(
      rows.map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id,
      })),
    )
  }

  const itemChange = useCallback(
    (e) => {
      setIsRowEdited(true)

      const { dataItem, field, value } = e

      if (dataItem?.field === 'Particulars') return
      if (dataItem?.field === 'ParticularsType') return

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
                updated.maintEndDateTime = newEnd
              }
            }
          }
          return updated
        }),
      )
      if (permissions?.onlyCellUpdate) {
        setModifiedCells((prev) => {
          const updated = { ...(prev[itemId] || {}) }

          updated[field] = value

          if (!updated.NormParameter_FK_Id && dataItem?.NormParameter_FK_Id) {
            updated.NormParameter_FK_Id = dataItem.NormParameter_FK_Id
          }

          const result = {
            ...prev,
            [itemId]: updated,
          }

          return result
        })
      } else {
        const uniqueItemId = permissions?.showCheckbox
          ? `${gridName}-${itemId}`
          : itemId

        setModifiedCells((prev) => {
          const base = {
            ...(prev[uniqueItemId] || {}),
            ...dataItem,
            [field]: value,
          }
          if (
            'maintStartDateTime' in base &&
            'maintEndDateTime' in base &&
            'durationInHrs' in base
          ) {
            if (
              field === 'maintStartDateTime' ||
              field === 'maintEndDateTime'
            ) {
              base.durationInHrs = recalcDuration(
                base.maintStartDateTime,
                base.maintEndDateTime,
              )
            } else if (field === 'durationInHrs') {
              const newEnd = recalcEndDate(base.maintStartDateTime, value)
              if (newEnd) base.maintEndDateTime = newEnd.toISOString()
            }
          }

          return { ...prev, [uniqueItemId]: base }
        })
      }
      setCustomModifiedCells((prev) => ({
        ...prev,
        [itemId]: { ...(prev[itemId] || {}), [field]: value },
      }))
    },
    [setRows, setModifiedCells, setCustomModifiedCells],
  )

  useEffect(() => {
    const isModifiedCellsEmpty = Object.keys(modifiedCells).length === 0
    const isCustomModifiedCellsEmpty =
      Object.keys(customModifiedCells).length === 0

    if (isModifiedCellsEmpty && !isCustomModifiedCellsEmpty) {
      setCustomModifiedCells({})
    }
  }, [modifiedCells, customModifiedCells])

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
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(columns?.map((col) => [col.field, ''])),
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
    setSelectedGrade('')
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

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  // console.log('rows?.length', rows?.length)

  const CustomRow = useCallback(({ dataItem, className, ...rest }) => {
    const isDisabled =
      !dataItem.isEditable && dataItem?.isEditable !== undefined
    const hasError = dataItem?.isError
    const rowClassName = hasError
      ? 'error-row'
      : isDisabled
        ? 'custom-disabled-row'
        : className

    return (
      <tr {...rest?.trProps} className={rowClassName}>
        {rest.children}
      </tr>
    )
  }, [])

  const toolTipRendererdescLimit = (props) => {
    const value = props.dataItem[props.field]
    const type = props?.dataItem?.type ?? ''
    // const isDisabled = type === 'ramp-down' || type === 'ramp-up'
    const isDisabled = false

    return (
      <td
        {...props.tdProps}
        title={value}
        style={{
          backgroundColor: isDisabled ? '#f0f0f0' : undefined,
        }}
      >
        {props.children}
      </td>
    )
  }
  //

  const RedHighlightCell = (props) => {
    const {
      dataItem,
      field,
      tdProps,
      children,
      customModifiedCells,
      allRedCell,
    } = props

    const rowId = dataItem.id
    const value = dataItem[field]
    if (disableRedHighlight) {
      return (
        <td {...tdProps} title={value}>
          {children}
        </td>
      )
    }

    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    const month = field
    const normId = dataItem.materialFkId || dataItem.NormParameter_FK_Id

    const isRedFromAllRedCell = allRedCell?.some(
      (cell) =>
        cell.month === month &&
        cell.NormParameter_FK_Id?.toLowerCase() === normId?.toLowerCase(),
    )

    const shouldHighlight = isEdited || isRedFromAllRedCell

    return (
      <td
        {...tdProps}
        title={value}
        style={{
          color: shouldHighlight ? 'orange' : undefined,
          fontWeight: shouldHighlight ? 'bold' : undefined,
        }}
      >
        {children}
      </td>
    )
  }

  const RedHighlightCell2 = (props) => {
    const {
      dataItem,
      field,
      tdProps,
      children,
      customModifiedCells,
      allRedCell,
    } = props

    const rowId = dataItem.id
    const value = dataItem[field]

    if (disableRedHighlight) {
      return (
        <td {...tdProps} title={value}>
          {children}
        </td>
      )
    }

    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    const month = field
    const normId = dataItem.materialFKId || dataItem.NormParameter_FK_Id

    const matchedCell = allRedCell?.find(
      (cell) =>
        cell.month?.toLowerCase() === month?.toLowerCase() &&
        cell.NormParameter_FK_Id?.toLowerCase() === normId?.toLowerCase(),
    )

    let highlightColor
    if (isEdited) {
      highlightColor = 'orange'
    } else if (matchedCell?.mode === 'Propane(1Z)') {
      highlightColor = 'red'
    } else if (matchedCell?.mode === 'Propane(2Z)') {
      highlightColor = 'green'
    }

    return (
      <td
        {...tdProps}
        title={value}
        style={{
          color: highlightColor,
          fontWeight: highlightColor ? 'bold' : undefined,
        }}
      >
        {children}
      </td>
    )
  }

  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]
    const month = props.field
    const normId =
      props.dataItem.materialFkId || props.dataItem.NormParameter_FK_Id

    const isRedFromAllRedCell = allRedCell.some(
      (cell) =>
        cell.month === month &&
        cell.NormParameter_FK_Id?.toLowerCase() === normId?.toLowerCase(),
    )

    const isRed = isRedFromAllRedCell

    return (
      <td
        {...props.tdProps}
        title={value}
        style={{
          color: isRed ? 'orange' : undefined,
          fontWeight: isRed ? 'bold' : undefined,
        }}
      >
        {props.children}
      </td>
    )
  }

  const HeaderWithTooltip = (props) => {
    // console.log('HeaderWithTooltip', props)
    return (
      <th {...props.thProps}>
        <a className='k-link' onClick={props.onClick}>
          <span title={props.title}>{props.title}</span>
        </a>
      </th>
    )
  }

  const SimpleHeaderWithTooltip = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title={props.title}
        style={{ padding: '0px', borderRight: '1px solid #b4b4b4ff' }}
      >
        <Tooltip
          position='top'
          anchorElement='target'
          parentTitle={true}
          className='test'
        >
          {props.children}
        </Tooltip>
      </th>
    )
  }
  const BlankHeader = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title=''
        style={{ padding: '0px', borderRight: '1px solid #b4b4b4ff' }}
      ></th>
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

  const DurationDisplayWithTooltipCell = (props) => {
    const value = props.dataItem[props.field]

    // Format value to HH:MM
    let display = value
    if (value && !isNaN(value)) {
      const [hoursStr, minsStr = '0'] = value.toString().split('.')
      const hours = parseInt(hoursStr, 10)
      const mins = parseInt(minsStr.padEnd(2, '0'), 10)

      display = `${hours.toString().padStart(2, '0')}:${mins
        .toString()
        .padStart(2, '0')}`
    }

    // Tooltip and conditional color logic
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
        title={display}
        style={{
          color: isRed ? 'orange' : undefined,
        }}
      >
        {display}
      </td>
    )
  }

  // const ConditionalDateEditorForConstantValue = (props) => {
  //   if (props.dataItem.UOM === 'Date') {
  //     return <DateOnlyPicker {...props} />
  //   }

  //   return <NoSpinnerNumericEditor {...props} />
  // }

  // const handleLoadClick = () => {
  //   if (onLoad && startDate && endDate) {
  //     onLoad(startDate, endDate)
  //   }
  // }

  // const SafeColumnMenu = (props) => {
  //   return (
  //     <GridColumnMenuFilter
  //       {...props}
  //       mobileMode={false} // ✅ This prevents the crash
  //     />
  //   )
  // }

  // const dateFields = [
  //   'maintStartDateTime',
  //   'maintEndDateTime',
  //   'endDateTA',
  //   'startDateTA',
  //   'endDateSD',
  //   'startDateSD',
  //   'endDateIBR',
  //   'startDateIBR',
  // ]

  useEffect(() => {
    if (permissions?.showG && grades?.length > 0 && !selectedGrade) {
      const firstGrade = grades[0]
      setSelectedGrade(firstGrade.gradeId)
      handleGradeChange(firstGrade.gradeId, firstGrade?.displayName)
    }
  }, [grades, permissions?.showG, selectedGrade])

  useEffect(() => {
    setSelectedGrade(null)
  }, [plantID])

  useEffect(() => {
    if (
      permissions?.units?.length > 0 &&
      (!selectedUnit || !permissions.units.includes(selectedUnit))
    ) {
      const defaultUnit = permissions.units[0]
      setSelectedUnit(defaultUnit)
      handleUnitChange(defaultUnit)
    }
  }, [permissions])

  const rowHeightVH = 5 // each row ~4vh
  const headerVH = 10 // grid’s own header/filter area
  const pageHeaderVH = 20 // top app bar + stepper + controls
  const maxVH = 60 // cap grid height

  const calculatedVH = React.useMemo(() => {
    if (!rows || rows?.length === 0) return 20
    const needed = rows?.length * rowHeightVH + headerVH
    const available = 100 - pageHeaderVH
    return Math.round(Math.min(needed, maxVH, available))
  }, [rows?.length])

  const handleHeaderSelectionChange = (event) => {
    const checked = event.nativeEvent.target.checked
    console.log('Header checkbox changed. Checked:', checked)
  }

  // console.log(
  //   'grades[0].gradeId',
  //   grades?.[0]?.gradeId,
  //   typeof grades?.[0]?.gradeId,
  // )
  // console.log('selectedGrade', selectedGrade, typeof selectedGrade)

  const onSelectionChange = (event) => {
    // const checkbox = event.nativeEvent.target
    // if (!checkbox || checkbox.type !== 'checkbox') return // only handle checkbox clicks
    // const selectedRow = event.dataItem
    // const isSelected = event.nativeEvent.target.checked
    // setRows((prevRows) =>
    //   prevRows.map((row) =>
    //     row.id === selectedRow.id
    //       ? { ...row, isChecked: isSelected, inEdit: true }
    //       : row,
    //   ),
    // )
    // setModifiedCells((prev) => ({
    //   ...prev,
    //   [selectedRow.id]: {
    //     ...selectedRow,
    //     isChecked: isSelected,
    //     inEdit: true,
    //   },
    // }))
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

      {permissions?.showReportTitleMain && (
        <Typography component='div' className='grid-title'>
          {permissions?.titleNameMain}
        </Typography>
      )}

      {(permissions?.allAction ?? false) && (
        <Box className='action-box'>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              width: '100%',
              ...(permissions?.marginTop && { marginTop: '10px' }),
            }}
          >
            {/* Left side - Note */}
            <Box>
              {permissions?.showNote && (
                <Typography component='div' className='text-note'>
                  {note}
                </Typography>
              )}

              {permissions?.showReportTitle && (
                <Typography component='div' className='grid-title'>
                  {titleName}
                </Typography>
              )}
              {permissions?.showTitleName && (
                <Typography component='div' className='grid-title'>
                  {titleName}
                </Typography>
              )}

              {permissions?.showTitleNameBusiness && (
                <Typography
                  component='div'
                  className='grid-title'
                  sx={{
                    ...(permissions?.marginBottom && { marginBottom: '10px' }),
                  }}
                >
                  {permissions?.titleName}
                </Typography>
              )}

              {permissions?.showG && (
                <TextField
                  select
                  value={selectedGrade || ''}
                  onChange={(e) => {
                    const selectedGradeId = e.target.value
                    const selectedGradeObj = grades.find(
                      (g) => g.gradeId === selectedGradeId,
                    )
                    setSelectedGrade(selectedGradeId)
                    handleGradeChange(
                      selectedGradeObj?.gradeId,
                      selectedGradeObj?.displayName,
                    ) // ✅ Pass both id & name
                  }}
                  className='dropdown-select'
                  variant='outlined'
                  label={permissions?.dropdownLabel || 'Select'}
                >
                  <MenuItem value='' disabled>
                    {permissions?.dropdownLabel || 'Select'}
                  </MenuItem>

                  {grades?.map((unit) => (
                    <MenuItem key={unit.gradeId} value={unit.gradeId}>
                      {unit.displayName}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            </Box>

            {/* Right side - All other actions */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {permissions?.UnitToShow && (
                <Chip
                  label={permissions.UnitToShow}
                  variant='outlined'
                  className='unit-chip'
                />
              )}

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
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={downloadExcelForConfiguration}
                  disabled={isButtonDisabled}
                >
                  Export
                </Button>
              )}

              {permissions?.uploadExcelBtn && (
                <>
                  <Button
                    variant='contained'
                    onClick={triggerFileUpload}
                    disabled={isButtonDisabled}
                    className='btn-save'
                  >
                    Import
                  </Button>

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

              {permissions?.downloadExcelBtnFromUI && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={excelExport}
                  disabled={rows?.length === 0}
                >
                  Export
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
                  className='dropdown-select'
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
                  className='dropdown-select'
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
            </Box>
          </Box>
        </Box>
      )}

      <div className='kendo-data-grid'>
        <Tooltip openDelay={50} position='auto' anchorElement='target'>
          <ExcelExport
            data={rows}
            ref={_export}
            fileName={`${permissions?.ExcelName}.xlsx`}
          >
            <Grid
              style={{
                flex: 1,
                overflow: 'auto',
                // height: 'auto',
                // height: permissions?.isHeight ? '60vh' : '60vh',
                // height: '60vh',
                // height: `${gridHeight}px`,

                height:
                  lowerVertName === 'meg' || supressGridHeight == true
                    ? undefined
                    : rows?.length > 10
                      ? `${calculatedVH}vh`
                      : undefined,

                // height: rows?.length > 10 ? '60vh' : `${calculatedVH}vh`,
                // height: `${calculatedVH}vh`,
              }}
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
              defaultTake={50}
              contextMenu={true}
              grade={grades}
              onRowClick={handleRowClick}
              sortable={{
                mode: 'multiple',
              }}
              allRedCell={allRedCell}
              size='small'
              pageable={
                rows?.length > 100
                  ? {
                      buttonCount: 4,
                      pageSizes: [10, 50, 100],
                    }
                  : false
              }
            >
              {groupBy && <ExcelExportColumn field={groupBy} title='Type' />}

              {permissions?.unitForExcelToadd && (
                <ExcelExportColumn field={selectedUOM} title='UOM' />
              )}

              {columns?.map((col) => {
                const isActive = isColumnActive(col?.field, filter, sort)

                if (col.type === 'descLimit') {
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
                        edit: { text: descLimit },
                        data: toolTipRendererdescLimit,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }

                if (dateFields.includes(col.field)) {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      cells={{
                        edit: {
                          date: [
                            'fromDate',
                            'toDate',
                            'periodTo',
                            'periodFrom',

                            'toDateReport',
                            'fromDateReport',
                          ].includes(col.field)
                            ? DateOnlyPicker
                            : DateTimePickerEditor,
                        },
                        data: (props) => (
                          <RedHighlightCell
                            {...props}
                            customModifiedCells={customModifiedCells}
                            allRedCell={allRedCell}
                            disableRedHighlight={disableRedHighlight}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      format={
                        [
                          'fromDate',
                          'toDate',
                          'periodFrom',
                          'periodTo',
                          'toDateReport',
                          'fromDateReport',
                        ].includes(col.field)
                          ? '{0:dd-MM-yyyy}'
                          : '{0:dd-MM-yyyy hh:mm a}'
                      }
                      editor='date'
                      hidden={col.hidden}
                      columnMenu={DateColumnMenu}
                      width={col?.widthT}
                      headerClassName={
                        isDateFilterActive.includes(col.field)
                          ? 'active-column'
                          : ''
                      }
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
                          date: [
                            'ibrSD',
                            'ibrED',
                            'taSD',
                            'taED',
                            'sdED',
                            'sdSD',
                            'targetDate',
                          ].includes(col.field)
                            ? DateOnlyPicker
                            : DateOnlyPicker,
                        },
                        data: (props) => (
                          <RedHighlightCell
                            {...props}
                            customModifiedCells={customModifiedCells}
                            allRedCell={allRedCell}
                            disableRedHighlight={disableRedHighlight}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      format={
                        [
                          'ibrSD',
                          'ibrED',
                          'taSD',
                          'taED',
                          'sdED',
                          'sdSD',
                          'targetDate',
                        ].includes(col.field)
                          ? '{0:dd-MM-yyyy}'
                          : '{0:dd-MM-yyyy}'
                      }
                      editor='date'
                      hidden={col.hidden}
                      columnMenu={DateColumnMenu}
                    />
                  )
                }
                if (col?.field === 'limit') {
                  return (
                    <GridColumn
                      key='limit'
                      field='limit'
                      width={80}
                      title={col.title}
                      editable={col.editable || true}
                      cells={{
                        data: (cellProps) => <LimitCellEditor {...cellProps} />,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      headerClassName={isActive ? 'active-column' : ''}
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
                          <ProductCell
                            {...cellProps}
                            allProducts={allProducts}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
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
                        headerCell: SimpleHeaderWithTooltip,
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
                      width={col.widthT}
                      editable={true}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: TextCellEditor },
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
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
                      width={col?.widthT}
                      editable={false}
                      columnMenu={ColumnMenuCheckboxFilter}
                      headerClassName={isActive ? 'active-column' : ''}
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                    />
                  )
                }
                if (col?.field === 'ReceipeName') {
                  return (
                    <GridColumn
                      key='ReceipeName'
                      field='ReceipeName'
                      title={col.title || col.headerName}
                      width={col.width1}
                      editable={false}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                    />
                  )
                }
                if (col.type === 'Receipe') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.width1}
                      hidden={col.hidden}
                      className={
                        col?.isDisabled
                          ? 'k-number-right-disabled'
                          : 'k-number-right'
                      }
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditor },
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      filter='numeric'
                      format={col.format}
                    />
                  )
                }
                if (col?.field === 'DisplayName') {
                  return (
                    <GridColumn
                      key='DisplayName'
                      field={col?.field}
                      title={col.title || col.headerName}
                      // width={col?.width}
                      editable={false}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      cells={{
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
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
                      // editor={true}
                      // editable={{ mode: 'popup' }}
                      cells={{
                        data: (cellProps, allRedCell) => (
                          <RemarkCell
                            {...cellProps}
                            allRedCell={allRedCell}
                            onRemarkClick={handleRemarkCellClick}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      headerClassName={isActive ? 'active-column' : ''}
                      width={col.widthT || ''}
                    />
                  )
                }
                if (col.field === 'durationInHrs') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.widthT}
                      editable={true}
                      columnMenu={ColumnMenuCheckboxFilter}
                      hidden={col.hidden}
                      format={'{0:n2}'}
                      className={
                        col?.isDisabled
                          ? 'k-number-right-disabled'
                          : 'k-number-right'
                      }
                      cells={{
                        edit: { text: DurationEditor },
                        data: DurationDisplayWithTooltipCell,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      headerClassName={isActive ? 'active-column' : ''}
                    />
                  )
                }

                if (col.hideFilter && col.hideSort) {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      hidden={col.hidden}
                      className={
                        col?.isDisabled
                          ? 'k-number-right-disabled'
                          : 'k-number-right'
                      }
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditor },
                        data: (props) => (
                          <RedHighlightCell
                            {...props}
                            customModifiedCells={customModifiedCells}
                            allRedCell={allRedCell}
                            disableRedHighlight={disableRedHighlight}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      format={col.format}
                      sortable={false}
                      width={col?.widthT}
                    />
                  )
                }

                if (col.type === 'number') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.widthT}
                      hidden={col.hidden}
                      className={`
                  ${col?.isDisabled ? 'k-number-right-disabled' : 'k-number-right'}
                  ${col?.isBold ? 'bold-text' : ''}
                `}
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditor },
                        data: (props) =>
                          showThreeColors ? (
                            <RedHighlightCell2
                              {...props}
                              customModifiedCells={customModifiedCells}
                              allRedCell={allRedCell}
                              disableRedHighlight={disableRedHighlight}
                            />
                          ) : (
                            <RedHighlightCell
                              {...props}
                              customModifiedCells={customModifiedCells}
                              allRedCell={allRedCell}
                              disableRedHighlight={disableRedHighlight}
                            />
                          ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      filter='numeric'
                      format={col.format}
                    />
                  )
                }

                // ...

                if (col.type === 'switch') {
                  const handleCheckboxChange = (props, value) => {
                    const { dataItem, field } = props
                    const { materialName, id } = dataItem

                    onGlobalCheckboxChange(
                      gridName,
                      id,
                      materialName,
                      field,
                      value,
                      dataItem,
                    )
                  }

                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title='.'
                      width={col.widthT}
                      hidden={col.hidden}
                      editable={true}
                      cells={{
                        data: (props) => (
                          <td style={{ textAlign: 'center' }}>
                            <Checkbox
                              checked={!!props.dataItem[props.field]}
                              onChange={(e) =>
                                handleCheckboxChange(props, e.value)
                              }
                            />
                          </td>
                        ),
                        headerCell: BlankHeader,
                      }}
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
                        col?.isDisabled
                          ? 'k-number-right-disabled'
                          : 'k-number-right'
                      }
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditor },
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      filter='numeric'
                      format={col.format}
                    />
                  )
                }

                if (col.field === 'ConstantValue') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      // width={col.width}
                      hidden={col.hidden}
                      editable={!!col?.editable}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditor },
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }

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
                      headerCell: SimpleHeaderWithTooltip,
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
                    headerCell: SimpleHeaderWithTooltip,
                  }}
                />
              )}
            </Grid>
          </ExcelExport>
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
