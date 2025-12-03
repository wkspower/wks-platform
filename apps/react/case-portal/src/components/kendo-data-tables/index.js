import HelpIcon from '@mui/icons-material/Help'
import { Tooltip as MuiTooltip } from '@mui/material'
import '@progress/kendo-font-icons/dist/index.css'
import { Grid, GridColumn } from '@progress/kendo-react-grid'
import { Tooltip } from '@progress/kendo-react-tooltip'
import '@progress/kendo-theme-default/dist/all.css'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports-kendo/ColumnMenu1'
import { DateColumnMenu } from 'components/Utilities/DateColumnMenu'
import Notification from 'components/Utilities/Notification'
import React, { useCallback, useEffect, useRef, useState } from 'react'
import PropaneDropdown from './Utilities-Kendo/PropaneDropdown'
import RestartAltIcon from '@mui/icons-material/RestartAlt'

import { useSelector } from 'react-redux'
import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  MenuItem,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import { SvgIcon } from '../../../node_modules/@progress/kendo-react-common/index'
import {
  ExcelExport,
  ExcelExportColumn,
} from '../../../node_modules/@progress/kendo-react-excel-export/index'
import {
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../node_modules/@progress/kendo-react-grid/index'
import { Checkbox } from '../../../node_modules/@progress/kendo-react-inputs/index'
import { trashIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import { arrowRotateCcwIcon } from '../../../node_modules/@progress/kendo-svg-icons/dist/index'
import '../../kendo-data-grid.css'
import BudgetConstrainsCellEditor from './Utilities-Kendo/BudgetConstrainsCellEditor'
import DateOnlyPicker from './Utilities-Kendo/DatePicker'
import DateTimePickerEditor from './Utilities-Kendo/DatePickeronSelectedYr'
import { descLimit } from './Utilities-Kendo/descLimit'
import {
  recalcDuration,
  recalcEndDate,
} from './Utilities-Kendo/durationHelpers'
import LimitCellEditor from './Utilities-Kendo/LimitCellEditor'
import MonthCell from './Utilities-Kendo/MonthCell'
import MonthDropdownEditor from './Utilities-Kendo/MonthDropdownEditor'
import { NoSpinnerNumericEditorNegative } from './Utilities-Kendo/negativeNumbericColumns'
import { NoSpinnerNumericEditor } from './Utilities-Kendo/numbericColumns'
import { DurationEditor } from './Utilities-Kendo/numericViewCells'
import ProductCell from './Utilities-Kendo/ProductCell'
import { RemarkCell } from './Utilities-Kendo/RemarkCell'
import { TextCellEditor } from './Utilities-Kendo/TextCellEditor'
import { NoSpinnerNumericEditorWithUOMValidation } from './Utilities-Kendo/numbericColumnsWithUOMValidation'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { getColumnMenuDateFilter } from 'components/data-tables/Reports-kendo/ColumnMenuDateFilter'

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
  showCatChemUtilityCheckbox = false,
  showCatChemUtilityCheckbox2 = false,
  screenType = 'slowdown',
  rows = [],
  plantID = null,
  grades = [],
  allRedCell = [],
  allRedCell2 = [],
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
  totalRowConfiguration = null,
  selectedUOM = 'MT/Month',
  note = '',
  titleName = '',
  gridName,
  onGlobalCheckboxChange,
  allProducts = [],
  allDescriptionDrpdwn = [],
  allMonths = [],
  selectMode,
  setSelectMode = () => {},
  handleExcelUpload = () => {},
  downloadExcelForConfiguration = () => {},
  onLoad = () => {},
  disableRedHighlight = false,
  showThreeColors = false,
  resetDataChanges = () => {},
  noteOnSaveDialogeBox = '',
  deleteNoteOnDeleteDialogeBox = '',
}) => {
  const _export = useRef(null)
  const _grid = React.useRef(undefined)

  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [openResetDialogeBox, setOpenResetDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const showDeleteAll = permissions?.deleteAllBtn && selectedUsers.length > 1
  const [selectedUnit, setSelectedUnit] = useState(permissions?.units?.[0])
  const [selectedGrade, setSelectedGrade] = useState()
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [openResetDataDialogeBox, setOpenResetDataDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const closeResetDataDialogeBox = () => setOpenResetDataDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const [isDateFilterActive, setIsDateFilterActive] = useState([])
  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)
  const ColumnMenuCheckboxFilterDate = getColumnMenuDateFilter(rows)
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)

  const { verticalChange, oldYear } = dataGridStore
  const IS_OLD_YEAR = oldYear?.oldYear

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const isPEPP = ['pe', 'pp'].includes(lowerVertName)

  const initialGroup = groupBy
    ? [
        {
          field: groupBy,
          aggregates: totalRowConfiguration,
          dir: undefined,
        },
      ]
    : []

  const MyFooterCustomCell = (props) => {
    const { tdProps } = props
    const field = props.field

    const labelColumn = 'displayName'
    if (field === labelColumn) {
      return (
        <td {...tdProps}>
          <b>Total</b>
        </td>
      )
    }

    const aggObj = props.dataItem?.aggregates?.[field]

    let cellContent = ''

    if (aggObj) {
      const aggKey = Object.keys(aggObj)[0]
      const value = aggObj[aggKey]
      cellContent =
        value != null ? Math.trunc(Number(value) * 10000) / 10000 : ''
    }

    return (
      <td {...props.tdProps} colSpan={1}>
        {cellContent}
      </td>
    )
  }

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
    if (READ_ONLY) {
      setEdit({})
      return
    }

    if (e?.dataItem?.aggregates) {
      setEdit({})
      return
    }

    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      setEdit({})
      return
    }
    if (e.dataItem?.isTotal) {
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

      const { dataItem, field } = e
      let { value } = e
      if (dataItem?.isTotal) {
        return
      }

      if (permissions?.isTotalFooterActive) {
        const monthsForTotalRow = [
          'april',
          'aug',
          'dec',
          'feb',
          'jan',
          'july',
          'june',
          'march',
          'may',
          'nov',
          'oct',
          'sep',
        ]

        if (monthsForTotalRow.includes(field)) {
          if (value === '' || value == null) {
            value = null
          } else {
            value = Number(value)
          }
        }
      }

      if (dataItem?.field === 'Particulars') return
      if (dataItem?.field === 'ParticularsType') return

      const itemId = dataItem.id

      // months list in the order you provided
      const months = [
        'apr',
        'may',
        'jun',
        'jul',
        'aug',
        'sep',
        'oct',
        'nov',
        'dec',
        'jan',
        'feb',
        'mar',
      ]

      // Helper: return numeric percent if input contains at least one digit, otherwise null.
      const parsePctOrNull = (v) => {
        if (v == null) return null
        const s = String(v).replace('%', '').trim()
        // if there are no digits at all, treat as invalid (e.g. "+", "-", "", "+%")
        if (!/[0-9]/.test(s)) return null
        // remove leading plus sign for parsing, keep minus sign
        const cleaned = s.replace(/^\+/, '')
        const n = Number(cleaned)
        return Number.isFinite(n) ? n : null
      }

      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r
          const updated = { ...r, [field]: value }

          // percentChange logic: adjust months if enabled and percentChange field changed
          if (field === 'percentChange' && permissions?.percentChangeLogic) {
            const pct = parsePctOrNull(value)
            if (pct !== null) {
              const factor = 1 + pct / 100
              months.forEach((m) => {
                const original = Number(r[m]) || 0
                updated[m] = Number((original * factor).toFixed(2))
              })
            }
          }

          if (
            'maintStartDateTime' in updated &&
            'maintEndDateTime' in updated &&
            'durationInHrs' in updated
          ) {
            if (!(screenType === 'slowdown' && lowerVertName === 'elastomer')) {
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
          }
          if (
            lowerVertName === 'vcm' &&
            (r.discription || '').trim() === 'Furnace Decoking'
          ) {
            if (field === 'maintStartDateTime' && value) {
              const start = new Date(value)
              if (!isNaN(start)) {
                const end = new Date(start)
                end.setHours(end.getHours() + 192)
                updated.maintEndDateTime = end
                updated.durationInHrs = '192.00'
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

          // percentChange: only set month fields when percent is numeric
          if (field === 'percentChange' && permissions?.percentChangeLogic) {
            const pct = parsePctOrNull(value)
            if (pct !== null) {
              const factor = 1 + pct / 100
              months.forEach((m) => {
                if (m in dataItem) {
                  const original = Number(dataItem[m]) || 0
                  updated[m] = Number((original * factor).toFixed(2))
                }
              })
            }
          }

          return {
            ...prev,
            [itemId]: updated,
          }
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
            if (!(screenType === 'slowdown' && lowerVertName === 'elastomer')) {
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
          }

          // percentChange logic: mutate base for all months (only when numeric)
          if (field === 'percentChange' && permissions?.percentChangeLogic) {
            const pct = parsePctOrNull(value)
            if (pct !== null) {
              const factor = 1 + pct / 100
              months.forEach((m) => {
                const original = Number(dataItem[m]) || 0
                base[m] = Number((original * factor).toFixed(2))
              })
            }
          }

          return { ...prev, [uniqueItemId]: base }
        })
      }

      // customModifiedCells: always set per-row custom changes (include months if percentChange)
      setCustomModifiedCells((prev) => {
        const base = { ...(prev[itemId] || {}), [field]: value }

        if (field === 'percentChange' && permissions?.percentChangeLogic) {
          const pct = parsePctOrNull(value)
          if (pct !== null) {
            const factor = 1 + pct / 100
            months.forEach((m) => {
              const original = Number(dataItem[m]) || 0
              base[m] = Number((original * factor).toFixed(2))
            })
          }
        }

        return {
          ...prev,
          [itemId]: base,
        }
      })
    },
    [setRows, setModifiedCells, setCustomModifiedCells, lowerVertName],
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
        if (permissions?.showCheckbox) {
          const uniqueKey = `${gridName}-${updatedRow.id}`

          setModifiedCells((prev) => ({
            ...prev,
            [uniqueKey]: {
              ...(prev[uniqueKey] || {}),
              ...updatedRow,
              gridName,
              id: updatedRow.id,
            },
          }))
        } else {
          setModifiedCells((prev) => ({
            ...prev,
            [updatedRow.id]: updatedRow,
          }))
        }
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
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
    setEdit({})
  }

  const resetConfirmation = async () => {
    resetDataChanges()
    setOpenResetDataDialogeBox(false)
    setEdit({})
  }

  const handleDeleteClick = async (params) => {
    if (READ_ONLY) return
    setParamsForDelete(params)
    setOpenDeleteDialogeBox(true)
  }

  const handleResetClick = async (params) => {
    setOpenResetDialogeBox(true)
  }

  const deleteTheRecord = async () => {
    deleteRowData(paramsForDelete)
    setOpenDeleteDialogeBox(false)
  }

  const resetTheRecord = async () => {
    resetRowData(paramsForDelete)
    setOpenResetDialogeBox(false)
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

  const ResetActionsCell = ({ dataItem }) => {
    return (
      <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
        <SvgIcon
          onClick={() => handleResetClick(dataItem)}
          icon={arrowRotateCcwIcon}
          themeColor='dark'
        />
      </td>
    )
  }

  const saveModalOpen = async () => {
    if (READ_ONLY) return
    setIsButtonDisabled(true)
    setOpenSaveDialogeBox(true)
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const resetDataModalOpen = async () => {
    if (READ_ONLY) return
    setIsButtonDisabled(true)
    setOpenResetDataDialogeBox(true)
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

  const MonthDisplayCell = (props) => {
    const { dataItem, field, tdProps, children } = props
    const value = dataItem[field]

    const monthNames = {
      1: 'Jan',
      2: 'Feb',
      3: 'Mar',
      4: 'Apr',
      5: 'May',
      6: 'Jun',
      7: 'Jul',
      8: 'Aug',
      9: 'Sep',
      10: 'Oct',
      11: 'Nov',
      12: 'Dec',
    }

    const displayValue = monthNames[value] || value

    return (
      <td {...tdProps} title={displayValue}>
        {displayValue}
      </td>
    )
  }

  const MaterialDisplayNameCell = (props) => {
    const { dataItem, field, tdProps, children } = props
    const value = dataItem[field]
    const method = dataItem.Method

    let color = 'inherit'

    switch (method) {
      case 'BestAchieved(MinCC)':
        color = '#2e7d32'
        break
      case 'Expression':
        color = '#f51717ff'
        break
      case 'BestAchieved(Indv)':
        color = '#1565c0'
        break
      default:
        break
    }

    return (
      <td
        {...tdProps}
        title={value}
        style={{
          color,
          //  fontWeight: method ? 'bold' : 'normal',
          ...tdProps.style,
        }}
      >
        {children}
      </td>
    )
  }
  const CustomRow = useCallback(
    ({ dataItem, className, ...rest }) => {
      const isDisabled =
        READ_ONLY ||
        (!dataItem.isEditable && dataItem?.isEditable !== undefined)
      const hasError = dataItem?.isError
      const isTotal = dataItem?.isTotal
      const rowClassName = hasError
        ? 'error-row'
        : isDisabled || isTotal
          ? 'custom-disabled-row'
          : className

      return (
        <tr {...rest?.trProps} className={rowClassName}>
          {rest.children}
        </tr>
      )
    },
    [IS_OLD_YEAR],
  )

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
      allRedCell2,
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

    const getMonthNumber = (m) => {
      if (m == null) return null
      const map = {
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
      const lower = String(m).trim().toLowerCase()
      return map[lower] || Number(lower) || null
    }

    const isRedFromAllRedCell = allRedCell2?.some((cell) => {
      const cellMonthNum = getMonthNumber(cell.month)
      const fieldMonthNum = getMonthNumber(month)

      const sameMonth = cellMonthNum === fieldMonthNum
      const sameNormId =
        cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase()

      return sameMonth && sameNormId
    })

    let highlightColor
    let highlightColorFullCell = false

    if (isEdited || isRedFromAllRedCell) {
      highlightColor = 'orange'
    } else if (matchedCell?.mode === 'Propane(1Z)') {
      highlightColor = 'red'
    } else if (matchedCell?.mode === 'Propane(2Z)') {
      highlightColor = 'green'
    } else if (matchedCell?.mode === 'Copied') {
      highlightColor = 'purple'
      highlightColorFullCell = true
    }

    return (
      <td
        {...tdProps}
        title={value}
        style={{
          color: highlightColor,
          fontWeight: highlightColor ? 'bold' : undefined,
          // backgroundColor: highlightColorFullCell ? 'lightGrey' : undefined,
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

  const SimpleHeaderWithTooltip = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title={props.title}
        style={{ padding: '0px', borderRight: '1px solid #878787' }}
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
        style={{ padding: '0px', borderRight: '1px solid #878787' }}
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

  // useEffect(() => {
  //   console.log(selectedGrade)

  //   if (permissions?.showG && grades?.length > 0 && !selectedGrade) {
  //     const firstGrade = grades[0]
  //     setSelectedGrade(firstGrade.gradeId)
  //     handleGradeChange(firstGrade.gradeId, firstGrade?.displayName)
  //   }
  // }, [grades, permissions?.showG, selectedGrade])

  useEffect(() => {
    if (!permissions?.showG || !grades?.length) return
    setSelectedGrade((prev) => {
      if (prev) {
        return prev
      }
      const firstGrade = grades[0]
      handleGradeChange(firstGrade.gradeId, firstGrade?.displayName)
      return firstGrade.gradeId
    })
  }, [grades, permissions?.showG])

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

  useEffect(() => {
    const modes = permissions?.modes
    if (Array.isArray(modes) && modes.length && selectMode === undefined) {
      setSelectMode(modes[0])
    }
  }, [permissions?.modes])

  const CHECK_TYPES = ['cat chem', 'utility consumption']
  const CHECK_TYPES2 = ['raw material', 'by products']

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

              {permissions?.showTitleAndInformation && (
                <Box display='flex' alignItems='center'>
                  <Typography
                    component='div'
                    className='grid-title'
                    sx={{
                      ...(permissions?.marginBottom && {
                        marginBottom: '10px',
                      }),
                    }}
                  >
                    {permissions?.titleName}
                  </Typography>

                  <MuiTooltip
                    title={
                      permissions?.titleAndInformation ||
                      'No information available'
                    }
                  >
                    <IconButton
                      size='medium'
                      sx={{
                        ml: 0,
                        backgroundColor: 'transparent',
                        '&:hover': {
                          backgroundColor: 'rgba(0, 0, 0, 0.1)',
                        },
                        padding: '4px',
                      }}
                    >
                      <HelpIcon fontSize='small' />
                    </IconButton>
                  </MuiTooltip>
                </Box>
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
                    )
                  }}
                  className='dropdown-select'
                  variant='outlined'
                  label={permissions?.dropdownLabel || 'Select'}
                  InputLabelProps={{
                    shrink: true,
                    sx: {
                      fontWeight: 'bold',
                    },
                  }}
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
                  disabled={isButtonDisabled || READ_ONLY}
                >
                  Add Item
                </Button>
              )}

              {permissions?.downloadExcelBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={downloadExcelForConfiguration}
                  disabled={isButtonDisabled || READ_ONLY}
                >
                  Export
                </Button>
              )}

              {permissions?.uploadExcelBtn && (
                <>
                  <Button
                    variant='contained'
                    onClick={triggerFileUpload}
                    disabled={isButtonDisabled || READ_ONLY}
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
                    READ_ONLY ||
                    (!summaryEdited && Object.keys(modifiedCells).length === 0)
                  }
                  {...(loading ? {} : {})}
                >
                  Save
                </Button>
              )}

              {permissions?.showResetButton && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={resetDataModalOpen}
                  disabled={
                    isButtonDisabled ||
                    READ_ONLY ||
                    (!summaryEdited && Object.keys(modifiedCells).length === 0)
                  }
                  startIcon={<RestartAltIcon />}
                >
                  Reset
                </Button>
              )}

              {permissions?.showCalculate && (
                <Button
                  variant='contained'
                  onClick={handleCalculateBtn}
                  disabled={
                    READ_ONLY ||
                    (rows?.length === 0
                      ? false
                      : isButtonDisabled ||
                        !permissions?.showCalculateVisibility)
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
                  disabled={isButtonDisabled || READ_ONLY}
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
                  disabled={isButtonDisabled || READ_ONLY}
                >
                  Refresh
                </Button>
              )}

              {permissions?.downloadExcelBtnFromUI && (
                <Button
                  variant='contained'
                  className='btn-save'
                  onClick={excelExport}
                  disabled={READ_ONLY || rows?.length === 0}
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
                  {permissions?.units?.map((unit) => (
                    <MenuItem key={unit} value={unit}>
                      {unit}
                    </MenuItem>
                  ))}
                </TextField>
              )}

              {permissions?.showModes && (
                <TextField
                  select
                  value={selectMode ?? ''}
                  onChange={(e) => setSelectMode(e.target.value)}
                  className='dropdown-select'
                  variant='outlined'
                  label='Select Modes'
                >
                  <MenuItem value='' disabled>
                    Select Modes
                  </MenuItem>

                  {permissions.modes.map((m) => (
                    <MenuItem key={m.name} value={m.name}>
                      {m.displayName}
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
              groupable={
                permissions?.isTotalFooterActive
                  ? {
                      enabled: false,
                      footer: 'visible',
                      showGroupPanel: false,
                    }
                  : {
                      enabled: false,
                      footer: 'none',
                      showGroupPanel: false,
                    }
              }
              cells={
                permissions?.isTotalFooterActive
                  ? {
                      groupFooter: MyFooterCustomCell,
                    }
                  : undefined
              }
              allRedCell={allRedCell}
              allRedCell2={allRedCell2}
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

              {columns?.map((col) => {
                {
                  permissions?.unitForExcelToadd && (
                    <ExcelExportColumn field={selectedUOM} title='UOM' />
                  )
                }
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
                      // columnMenu={DateColumnMenu}
                      filter='date'
                      columnMenu={ColumnMenuCheckboxFilterDate}
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
                      filter='date'
                      // columnMenu={DateColumnMenu}
                      columnMenu={ColumnMenuCheckboxFilterDate}
                    />
                  )
                }
                if (col?.field === 'symbol') {
                  return (
                    <GridColumn
                      key='symbol'
                      field='symbol'
                      width={80}
                      title={col.title}
                      editable={col.editable || true}
                      cells={{
                        data: (cellProps) => (
                          <BudgetConstrainsCellEditor {...cellProps} />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      headerClassName={isActive ? 'active-column' : ''}
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
                        data: (cellProps) => (
                          <LimitCellEditor
                            {...cellProps}
                            READ_ONLY={READ_ONLY}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      headerClassName={isActive ? 'active-column' : ''}
                    />
                  )
                }

                if (col?.field === 'discriptionDrpdwn') {
                  return (
                    <GridColumn
                      key='discriptionDrpdwn'
                      field='discriptionDrpdwn'
                      title={col.title || col.headerName || 'Particulars'}
                      editable={col.editable || true}
                      hidden={col.hidden}
                      cells={{
                        data: (cellProps) => (
                          <ProductCell
                            {...cellProps}
                            allProducts={allDescriptionDrpdwn}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }
                if (
                  col?.field === 'discription' &&
                  col?.type === 'discriptionDrpdwn'
                ) {
                  return (
                    <GridColumn
                      key='discription'
                      field='discription'
                      title={col.title || col.headerName || 'Particulars'}
                      editable={col.editable || true}
                      hidden={col.hidden}
                      cells={{
                        data: (cellProps) => (
                          <ProductCell
                            {...cellProps}
                            allProducts={allDescriptionDrpdwn}
                          />
                        ),
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
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

                if (col.field === 'sapMaterialCode' && col.useMethodColors) {
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
                        data: MaterialDisplayNameCell,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }
                if (col.type === 'monthDropdown') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.width}
                      hidden={col.hidden}
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: MonthDropdownEditor },
                        data: MonthDisplayCell,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }
                if (col?.field === 'DisplayName') {
                  return (
                    <GridColumn
                      key='DisplayName'
                      field={col?.field}
                      title={col.title || col.headerName}
                      width={col?.widthT}
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

                if (col.type === 'propaneDropdown') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.width}
                      hidden={col.hidden}
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: PropaneDropdown }, // <-- Use your custom editor here
                        data: MonthDisplayCell,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                    />
                  )
                }

                if (col.type === 'percentChange') {
                  return (
                    <GridColumn
                      key={col.field}
                      field={col.field}
                      title={col.title || col.headerName}
                      width={col.widthT}
                      hidden={col.hidden}
                      className={'k-number-right'}
                      editable={col?.editable ? true : false}
                      headerClassName={isActive ? 'active-column' : ''}
                      cells={{
                        edit: { text: NoSpinnerNumericEditorNegative },
                        data: toolTipRenderer,
                        headerCell: SimpleHeaderWithTooltip,
                      }}
                      columnMenu={ColumnMenuCheckboxFilter}
                      filter='numeric'
                      format={col.format}
                    />
                  )
                }

                if (col.type === 'negativeNumber') {
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
                        edit: { text: NoSpinnerNumericEditorNegative },
                        data: (props) =>
                          showThreeColors ? (
                            <RedHighlightCell2
                              {...props}
                              customModifiedCells={customModifiedCells}
                              allRedCell={allRedCell}
                              allRedCell2={allRedCell2}
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

                if (col.type === 'numberWithUOMValidation') {
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
                        edit: { text: NoSpinnerNumericEditorWithUOMValidation },
                        data: (props) =>
                          showThreeColors ? (
                            <RedHighlightCell2
                              {...props}
                              customModifiedCells={customModifiedCells}
                              allRedCell={allRedCell}
                              allRedCell2={allRedCell2}
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
                              allRedCell2={allRedCell2}
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
                        data: (props) => {
                          const dataItem = props.dataItem || {}
                          const normType = (dataItem.Particulars || '')
                            .toString()
                            .toLowerCase()

                          if (
                            showCatChemUtilityCheckbox &&
                            !CHECK_TYPES.includes(normType)
                          ) {
                            return <td />
                          }

                          if (
                            showCatChemUtilityCheckbox2 &&
                            !CHECK_TYPES2.includes(normType)
                          ) {
                            return <td />
                          }

                          return (
                            <td style={{ textAlign: 'center' }}>
                              <Checkbox
                                checked={!!props.dataItem[props.field]}
                                onChange={(e) => {
                                  const checked =
                                    e?.value ?? e?.target?.checked ?? false
                                  handleCheckboxChange(props, checked)
                                }}
                              />
                            </td>
                          )
                        },
                        headerCell: BlankHeader,
                      }}
                    />
                  )
                }

                if (col.type === 'switch2') {
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
                        data: (props) => {
                          const dataItem = props.dataItem || {}
                          const normType = (dataItem.Particulars || '')
                            .toString()
                            .toLowerCase()

                          if (
                            showCatChemUtilityCheckbox2 &&
                            CHECK_TYPES2.includes(normType)
                          ) {
                            return <td />
                          }

                          return (
                            <td style={{ textAlign: 'center' }}>
                              <Checkbox
                                checked={!!props.dataItem[props.field]}
                                onChange={(e) => {
                                  const checked =
                                    e?.value ?? e?.target?.checked ?? false
                                  handleCheckboxChange(props, checked)
                                }}
                              />
                            </td>
                          )
                        },
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
                      width={col.widthT}
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
              disabled={READ_ONLY ||isCreatingCase || !showCreateCasebutton}
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
            disabled={isButtonDisabled || READ_ONLY}
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
            disabled={isButtonDisabled || READ_ONLY}
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
            disabled={isButtonDisabled || READ_ONLY}
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
            {permissions?.showNoteWhileDeleting
              ? `Are you sure you want to delete this row?   ${deleteNoteOnDeleteDialogeBox}`
              : 'Are you sure you want to delete this row?'}{' '}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialogeBox(false)}>Cancel</Button>
          <Button onClick={deleteTheRecord} autoFocus disabled={READ_ONLY}>
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
            {permissions?.showNoteWhileSaving
              ? `Are you sure you want to save these changes?   ${noteOnSaveDialogeBox}`
              : 'Are you sure you want to save these changes?'}{' '}
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
        open={openResetDataDialogeBox}
        onClose={closeResetDataDialogeBox}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Reset ?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            Are you sure you want to reset these changes?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeResetDataDialogeBox}>Cancel</Button>
          <Button onClick={resetConfirmation} autoFocus>
            Reset
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
            disabled={READ_ONLY}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRemarkDialogOpen(false)}>Cancel</Button>
          {/* <Button onClick={handleCloseRemark}>Cancel</Button> */}
          <Button
            onClick={handleRemarkSave}
            disabled={READ_ONLY || !currentRemark?.trim()}
          >
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  )
}

export default KendoDataTables
