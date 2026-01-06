import '@progress/kendo-font-icons/dist/index.css'
import {
  Grid,
  GridColumn,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports/ColumnMenu1'
import Notification from 'components/Utilities/Notification'
import GenericDropdown from 'components/Utilities/GenericDropdown'
import { useCallback, useRef, useState, useEffect } from 'react'

import { SvgIcon } from '../../../../node_modules/@progress/kendo-react-common/index'

import { trashIcon } from '../../../../node_modules/@progress/kendo-svg-icons/dist/index'
// import '../../../advance-kendo-grid.css'
import '../../../kendo-data-grid.css'
import { Tooltip } from '../../../../node_modules/@progress/kendo-react-tooltip/index'
import {
  DurationDisplayWithTooltipCell,
  DurationEditor,
} from '../Utilities-Kendo/numericViewCells'
import {
  recalcDuration,
  recalcEndDate,
} from '../Utilities-Kendo/durationHelpers'
import DateOnlyPicker from '../Utilities-Kendo/DatePicker'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { TextCellEditorUpdated } from '../Utilities-Kendo/phase-two/TextCellEditorUpdated'
import { SelectCellEditor } from '../Utilities-Kendo/phase-two/SelectCellEditor'
import { MultiselectCellEditor } from '../Utilities-Kendo/phase-two/MultiselectCellEditor'
import { BooleanCellEditor } from '../Utilities-Kendo/phase-two/BooleanCellEditor'
import RemarkDialog from './components/RemarkDialog'
import { NoSpinnerNumericEditor } from '../Utilities-Kendo/numbericColumns'
import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'
import DeleteDialog from './components/DeleteDialog'
import SaveConfirmationDialog from './components/SaveConfirmationDialog'
import valueFormatterByUOM from 'utils/ValueFormatterByUOM'
import { ExcelExport } from '../../../../node_modules/@progress/kendo-react-excel-export/index'
import { NumberCellEditor } from '../Utilities-Kendo/phase-two/NumberCellEditor'
import DateTimePickerEditor from '../Utilities-Kendo/DatePickeronSelectedYr'
import { getColumnMenuDateFilter } from 'components/data-tables/Reports-kendo/ColumnMenuDateFilter'

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
  'tentativeMonth',
  'ibrDueDate',
  'shutdownDate',
  'startupDate',
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

const ADJUST_PADDING = 4
const COLUMN_MIN = 4

const AdvanceKendoTable = ({
  allRedCell = [],
  allRedCell2 = [],
  modifiedCells = [],
  title = '',
  rows = [],
  setRows,
  columns,
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
  saveChanges = () => {},
  fetchData = () => {},
  deleteRowData = () => {},
  handleCalculate = () => {},
  handleUnitChange = () => {},
  handleRemarkCellClick = () => {},
  handleExport = () => {},
  handleExcelUpload = () => {},
  showThreeColors = false,
  groupBy = null,
  dropdownConfig = {},
  selectedDropdownValue,
  setSelectedDropdownValue,
  paginationConfig = {},
}) => {
  const fileInputRef = useRef(null)
  const minGridWidth = useRef(0)
  const gridRef = useRef(null)
  const _export = useRef(null)
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const [applyMinWidth, setApplyMinWidth] = useState(false)
  const [gridCurrent, setGridCurrent] = useState(0)
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const [disableRedHighlight, setDisableRedHighlight] = useState(false)
  const [isFormatByUOM, setIsFormatByUOM] = useState(false)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)
  const ColumnMenuCheckboxFilterDate = getColumnMenuDateFilter(rows)
  const initialGroup = Array.isArray(groupBy)
    ? groupBy.map((field) => ({ field }))
    : groupBy
      ? [{ field: groupBy }]
      : []

  // Build pagination configuration with defaults
  const getPaginationConfig = useCallback(() => {
    const defaults = {
      threshold: 100,
      buttonCount: 4,
      pageSizes: [10, 20, 50, 100],
      defaultPageSize: 10,
    }
    const config = { ...defaults, ...paginationConfig }
    
    if (rows?.length > config.threshold) {
      return {
        buttonCount: config.buttonCount,
        pageSizes: config.pageSizes,
      }
    }
    return false
  }, [rows?.length, paginationConfig])

  // Get the default page size from config
  const getDefaultTake = () => {
    const defaults = {
      threshold: 100,
      buttonCount: 4,
      pageSizes: [10, 20, 50, 100],
      defaultPageSize: 10,
    }
    const config = { ...defaults, ...paginationConfig }
    return config.defaultPageSize
  }

  // Helper function to extract all fields from columns including nested ones
  const extractAllColumns = useCallback((cols) => {
    const allCols = []
    const traverse = (columns) => {
      columns.forEach((col) => {
        if (col.children && Array.isArray(col.children)) {
          traverse(col.children)
        } else if (col.field) {
          allCols.push(col)
        }
      })
    }
    traverse(cols)
    return allCols
  }, [])

  // Calculate total minimum width and setup resize listener
  useEffect(() => {
    gridRef.current = document.querySelector('.k-grid')
    if (!gridRef.current) return

    const allColumns = extractAllColumns(columns)

    // Calculate total min width
    minGridWidth.current = 0
    allColumns.forEach((col) => {
      if (col.minWidth !== undefined) {
        minGridWidth.current += col.minWidth
      }
    })

    // Add action column width if present
    if (permissions?.deleteButton) {
      minGridWidth.current += 80
    }

    const handleResize = () => {
      if (!gridRef.current) return

      if (
        gridRef.current.offsetWidth < minGridWidth.current &&
        !applyMinWidth
      ) {
        setApplyMinWidth(true)
      } else if (gridRef.current.offsetWidth > minGridWidth.current) {
        setGridCurrent(gridRef.current.offsetWidth)
        setApplyMinWidth(false)
      }
    }

    setGridCurrent(gridRef.current.offsetWidth)
    setApplyMinWidth(gridRef.current.offsetWidth < minGridWidth.current)

    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [columns, permissions?.deleteButton, extractAllColumns, applyMinWidth])

  // Calculate dynamic width for each column using proportional distribution
  const setWidth = useCallback(
    (minWidth) => {
      if (minWidth === undefined) {
        minWidth = 50
      }

      if (applyMinWidth) {
        return minWidth
      }

      const allColumns = extractAllColumns(columns)
      const totalMinWidth = allColumns.reduce((sum, col) => {
        return sum + (col.minWidth || 50)
      }, 0) + (permissions?.deleteButton ? 80 : 0)

      // If total minWidth exceeds grid width, just use minWidth
      if (totalMinWidth >= gridCurrent) {
        return minWidth
      }

      // Calculate proportional width based on minWidth ratio
      const availableSpace = gridCurrent - totalMinWidth
      const proportionalShare = (minWidth / totalMinWidth) * availableSpace
      const width = minWidth + proportionalShare

      return Math.max(minWidth, width - ADJUST_PADDING)
    },
    [
      applyMinWidth,
      gridCurrent,
      columns,
      permissions?.deleteButton,
      extractAllColumns,
    ],
  )

  const handleEditChange = useCallback((e) => {
    setEdit(e.edit)
    // }
  }, [])

  // Helper function to add IST timezone offset (+5:30) to dates before sending to backend
  const addTimeOffset = (dateTime) => {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }

  // Format date fields in data before sending to backend
  const formatDateFieldsForBackend = (data) => {
    if (!data) return data
    const formatted = { ...data }
    dateFields.forEach((field) => {
      if (field in formatted && formatted[field]) {
        formatted[field] = addTimeOffset(formatted[field])
      }
    })
    return formatted
  }

  // Wrapper for saveChanges that formats date fields
  const handleSaveChanges = useCallback(() => {
    // Get modified cells data
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      saveChanges()
      return
    }

    // Format date fields in all modified cells
    const formattedModifiedCells = {}
    Object.keys(modifiedCells).forEach((key) => {
      formattedModifiedCells[key] = formatDateFieldsForBackend(
        modifiedCells[key],
      )
    })

    // Temporarily update modifiedCells with formatted data, call saveChanges, then restore
    const originalModifiedCells = modifiedCells
    setModifiedCells(formattedModifiedCells)

    // Call the parent's saveChanges with formatted data
    saveChanges()
  }, [modifiedCells, saveChanges, setModifiedCells])

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
  // Helper function to calculate days between two dates
  const calculateDaysBetween = (startDate, endDate) => {
    if (!startDate || !endDate) return null
    const start = new Date(startDate)
    const end = new Date(endDate)
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return null
    const diffTime = Math.abs(end - start)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays
  }

  // Helper function to add days to a date
  const addDaysToDate = (date, days) => {
    if (!date || !days) return null
    const result = new Date(date)
    result.setDate(result.getDate() + parseInt(days, 10))
    return result
  }

  // Helper function to subtract days from a date
  const subtractDaysFromDate = (date, days) => {
    if (!date || !days) return null
    const result = new Date(date)
    result.setDate(result.getDate() - parseInt(days, 10))
    return result
  }

  const itemChange = useCallback(
    (e) => {
      setIsRowEdited(true)
      const { dataItem, field, value } = e
      
      // Guard against undefined field
      if (!field) {
        return
      }
      
      const itemId = dataItem.id
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r
          const updated = { ...r, [field]: value }

          // Handle fromDate/toDate/durationInHrs calculation
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
                value, // string like "10.20"
              )
              if (newEnd) {
                updated.toDate = newEnd
              }
            }
          }

          // Handle startDate/endDate/durationInDays calculation
          if (
            'startDate' in updated &&
            'endDate' in updated &&
            'durationInDays' in updated
          ) {
            if (field === 'startDate' && updated.endDate) {
              // If startDate changed and endDate exists, calculate durationInDays
              updated.durationInDays = calculateDaysBetween(
                updated.startDate,
                updated.endDate,
              )
            } else if (field === 'endDate' && updated.startDate) {
              // If endDate changed and startDate exists, calculate durationInDays
              updated.durationInDays = calculateDaysBetween(
                updated.startDate,
                updated.endDate,
              )
            } else if (field === 'durationInDays' && updated.startDate) {
              // If durationInDays changed and startDate exists, calculate endDate
              updated.endDate = addDaysToDate(updated.startDate, value)
            }
          }

          return updated
        }),
      )

      setModifiedCells((prev) => {
        const base = { ...dataItem, [field]: value }

        // Handle fromDate/toDate/durationInHrs calculation
        if ('fromDate' in base && 'toDate' in base && 'durationInHrs' in base) {
          if (field === 'fromDate' || field === 'toDate') {
            base.durationInHrs = recalcDuration(base.fromDate, base.toDate)
          } else if (field === 'durationInHrs') {
            const newEnd = recalcEndDate(base.fromDate, value)
            if (newEnd) base.toDate = newEnd.toISOString()
          }
        }

        // Handle startDate/endDate/durationInDays calculation
        if (
          'startDate' in base &&
          'endDate' in base &&
          'durationInDays' in base
        ) {
          if (field === 'startDate' && base.endDate) {
            base.durationInDays = calculateDaysBetween(
              base.startDate,
              base.endDate,
            )
          } else if (field === 'endDate' && base.startDate) {
            base.durationInDays = calculateDaysBetween(
              base.startDate,
              base.endDate,
            )
          } else if (field === 'durationInDays' && base.startDate) {
            base.endDate = addDaysToDate(base.startDate, value)
          }
        }

        return { ...prev, [itemId]: base }
      })

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
    [setRows, setModifiedCells, setCustomModifiedCells],
  )

  useEffect(() => {
    const isModifiedCellsEmpty = Object.keys(modifiedCells).length === 0
    const isCustomModifiedCellsEmpty =
      Object.keys(customModifiedCells).length === 0

    if (isModifiedCellsEmpty && !isCustomModifiedCellsEmpty) {
      setCustomModifiedCells({})
    }

    if (isModifiedCellsEmpty) {
      setEdit({})
      setRows((prev) =>
        prev.map((r) => ({
          ...r,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells, customModifiedCells])

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
            'purpose',
            'reasons',
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

        // Also update customModifiedCells to highlight the remark field
        setCustomModifiedCells((prev) => ({
          ...prev,
          [updatedRow.id]: {
            ...(prev[updatedRow.id] || {}),
            [keyToUpdate]: currentRemark,
          },
        }))
      }

      return updatedRows
    })

    setRemarkDialogOpen(false)
  }

  const handleAddRow = () => {
    if (permissions?.tabIndex) {
      if (
        permissions?.tabIndex == 1 ||
        permissions?.tabIndex == 2 ||
        permissions?.tabIndex == 3 ||
        permissions?.tabIndex == 4 ||
        permissions?.tabIndex == 5
      )
        return
    }
    if (isButtonDisabled) return
    setIsButtonDisabled(true)
    const newRowId = rows.length
      ? Math.max(...rows.map((row) => row.id)) + 1
      : 1
    console.log('columns', columns)

    // Helper function to extract all fields from columns including nested ones
    const extractFields = (cols) => {
      const fields = []
      cols.forEach((col) => {
        if (col.field) {
          // It's a leaf column with a field
          fields.push(col.field)
        }
        if (col.children && Array.isArray(col.children)) {
          // It's a parent column with nested children
          fields.push(...extractFields(col.children))
        }
      })
      return fields
    }

    const allFields = extractFields(columns)
    console.log('allFields', allFields)
    const newRow = {
      id: newRowId,
      isNew: true,
      ...Object.fromEntries(allFields.map((field) => [field, ''])),
    }

    console.log('newRow', newRow)

    setRows((prevRows) => [newRow, ...prevRows])

    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }

  const saveConfirmation = async () => {
    handleSaveChanges()
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
    const displayText = String(rawValue ?? '')
    const rowId = dataItem.id

    // Check if this remark field was edited
    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    return (
      <td
        {...tdProps}
        title={displayText}
        style={{
          cursor: 'pointer',
          color: isEdited && displayText ? 'orange' : rawValue ? 'inherit' : 'gray',
          fontWeight: isEdited && displayText ? 'bold' : undefined,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
        }}
        onClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
        }}
        onDoubleClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
          onRemarkClick(dataItem)
          setEdit?.({})
        }}
      >
        {displayText || 'Add remark'}
      </td>
    )
  }

  const CustomRow = useCallback(({ dataItem, className, ...rest }) => {
    const isDisabled =
      !dataItem.isEditable && dataItem?.isEditable !== undefined
    const rowClassName = [
      className,
      isDisabled ? 'custom-disabled-row' : '',
      dataItem.isBold ? 'custom-bold-row' : '',
    ]
      .filter(Boolean)
      .join(' ')
    return (
      <tr
        {...rest?.trProps}
        className={rowClassName}
        style={{ width: '200px' }}
      >
        {rest.children}
      </tr>
    )
  }, [])

  const RedHighlightCell = (props) => {
    const {
      dataItem,
      field,
      tdProps,
      children,
      customModifiedCells,
      allRedCell,
      isFormatByUOM = false,
    } = props
    const uomType = dataItem?.UOM
    const rowId = dataItem.id
    let value = valueFormatterByUOM(dataItem[field], uomType)
    if (disableRedHighlight) {
      return (
        <td {...tdProps} title={value}>
          {isFormatByUOM ? value : children}
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
        {isFormatByUOM ? value : children}
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
      isFormatByUOM = false,
    } = props
    const uomType = dataItem?.UOM
    const rowId = dataItem.id
    let value = valueFormatterByUOM(dataItem[field], uomType)
    if (disableRedHighlight) {
      return (
        <td {...tdProps} title={value}>
          {isFormatByUOM ? value : children}
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
        {isFormatByUOM ? value : children}
      </td>
    )
  }

  const BooleanHighlightCell = (props) => {
    const {
      dataItem,
      field,
      tdProps,
      customModifiedCells,
      allRedCell,
      disableRedHighlight = false,
    } = props

    const value = dataItem[field]
    const rowId = dataItem.id
    const displayValue =
      typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value

    if (disableRedHighlight) {
      return (
        <td {...tdProps} title={displayValue} style={{ textAlign: 'center' }}>
          {displayValue}
        </td>
      )
    }

    // Check if this cell was modified
    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    const month = field
    const normId = dataItem.materialFkId || dataItem.NormParameter_FK_Id

    // Check if this cell is in the red highlight list
    const isRedFromAllRedCell = allRedCell?.some(
      (cell) =>
        cell.month === month &&
        cell.NormParameter_FK_Id?.toLowerCase() === normId?.toLowerCase(),
    )

    const shouldHighlight = isEdited || isRedFromAllRedCell

    return (
      <td
        {...tdProps}
        title={displayValue}
        style={{
          color: shouldHighlight ? 'orange' : undefined,
          fontWeight: shouldHighlight ? 'bold' : undefined,
          textAlign: 'center',
        }}
      >
        {displayValue}
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
        style={{
          padding: '0px',
          borderRight: '1px solid #878787',
          textAlign: 'center',
          width: { ...restThProps['width'] },
        }}
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

  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  const renderColumns = (cols, filter, sort) =>
    cols.map((col, idx) => {
      const isEditable = col.editable === true
      const isActive = isColumnActive(col.field, filter, sort)

      const headerColorClass = undefined

      if (col.children) {
        return (
          <GridColumn
            key={col.title || idx}
            title={col.title}
            headerClassName='center-group-header'
          >
            {renderColumns(col.children, filter, sort)}
          </GridColumn>
        )
      }

      // Textarea type handler (for dialog-based editing)
      if (col.type === 'textarea') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            width={setWidth(col?.minWidth || 120)}
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

      if (
        ['aopRemarks', 'remarks', 'remark', 'Remark', 'purpose','reasons'].includes(
          col.field,
        )
      ) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            width={setWidth(col?.minWidth || 120)}
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
            cells={{
              edit: { date: DateOnlyPicker },
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            format='{0:dd-MM-yyyy}'
            editor='date'
            editable={col.editable || false}
            hidden={col.hidden}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col.type == 'dateTime') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            cells={{
              edit: {
                date:
                  col?.type == 'dateTime'
                    ? DateTimePickerEditor
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
              col.type == 'dateTime'
                ? '{0:dd-MM-yyyy hh:mm a}'
                : '{0:dd-MM-yyyy}'
            }
            editor='date'
            hidden={col.hidden}
            filter='date'
            columnMenu={ColumnMenuCheckboxFilterDate}
            width={col?.width}
          />
        )
      }
      if (col.field.includes('durationInHrs')) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            editable={col.editable || false}
            columnMenu={ColumnMenuCheckboxFilter}
            hidden={col.hidden}
            format={'{0:n2}'}
            className={!isEditable ? 'non-editable-cell' : ''}
            cells={{
              edit: { text: DurationEditor },
              data: DurationDisplayWithTooltipCell,
            }}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col.type === 'numberNonGrey') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            className={'k-number-right'}
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
            width={setWidth(col?.minWidth || col?.widthT)}
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
            className={'k-number-right-disabled'}
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
                    isFormatByUOM={isFormatByUOM}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    isFormatByUOM={isFormatByUOM}
                  />
                ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'number1') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
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
                    isFormatByUOM={isFormatByUOM}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    isFormatByUOM={isFormatByUOM}
                  />
                ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'wholeNumber') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <NumberCellEditor
                    {...cellProps}
                    wholeNumberOnly={col?.wholeNumberOnly || false}
                  />
                ),
              },
              data: (props) =>
                showThreeColors ? (
                  <RedHighlightCell2
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    allRedCell2={allRedCell2}
                    disableRedHighlight={disableRedHighlight}
                    isFormatByUOM={isFormatByUOM}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    isFormatByUOM={isFormatByUOM}
                  />
                ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      //New Creted Code for Text Type
      if (col.type == 'text') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: { text: TextCellEditorUpdated },
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            // filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Boolean Type Handler
      if (col.type === 'boolean') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <BooleanCellEditor
                    {...cellProps}
                    useCheckbox={col?.useCheckbox !== false}
                    trueLabel={col?.trueLabel || 'Yes'}
                    falseLabel={col?.falseLabel || 'No'}
                  />
                ),
              },
              data: (cellProps) => (
                <BooleanHighlightCell
                  {...cellProps}
                  customModifiedCells={customModifiedCells}
                  allRedCell={allRedCell}
                  disableRedHighlight={disableRedHighlight}
                />
              ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Example: Using SelectCellEditor for specific field
      if (col?.field === 'property') {
        let allOptions = [
          { id: 'API', displayName: 'API' },
          { id: 'TAN', displayName: 'TAN' },
          { id: 'Sulfer', displayName: 'Sulfer' },
          { id: 'Asp to Resin ratio', displayName: 'Asp to Resin ratio' },
          { id: 'Salts', displayName: 'Salts' },
          { id: 'BS&W', displayName: 'BS&W' },
        ]
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            cells={{
              edit: {
                text: (cellProps) => (
                  <SelectCellEditor
                    {...cellProps}
                    options={allOptions}
                    textField='displayName'
                    valueField='id'
                    placeholder='Select...'
                  />
                ),
              },
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Example: Using MultiselectCellEditor for specific field (same structure as single select)
      if (col?.field === 'gtMaintenance') {
        // Change this to your multiselect field name
        let allOptions = [
          { id: 'MI', displayName: 'MI' },
          { id: 'HGPI', displayName: 'HGPI' },
          { id: 'IBR', displayName: 'IBR' },
          { id: 'RLA', displayName: 'RLA' },
          { id: 'Others', displayName: 'Others' },
        ]
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            editable={col?.editable ? true : false}
            cells={{
              edit: {
                text: (cellProps) => (
                  <MultiselectCellEditor
                    {...cellProps}
                    options={allOptions}
                    textField='displayName'
                    valueField='id'
                    placeholder='Select multiple...'
                    tagLimit={3} // Optional: limit display tags
                  />
                ),
              },
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      return (
        <GridColumn
          key={col.field}
          field={col.field}
          title={col.title || col.headerName}
          editable={col.editable || false}
          format={col.format || '{0:0.000}'}
          cells={{
            edit: { text: NoSpinnerNumericEditor },
            data: toolTipRenderer,
            headerCell: SimpleHeaderWithTooltip,
          }}
          className={`${!isEditable ? 'non-editable-cell' : ''}`}
          columnMenu={ColumnMenuCheckboxFilter}
          headerClassName={isActive ? 'active-column' : ''}
          width={setWidth(col?.minWidth || col?.widthT)}
        />
      )
    })

  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]
    const month = monthMap[props.field?.toLowerCase()]
    const normId = props.dataItem.materialFkId
    const rowId = props.dataItem.id

    const isRedFromAllRedCell = allRedCell.some(
      (cell) =>
        cell.month === month &&
        cell.normParameterFKId?.toLowerCase() === normId?.toLowerCase(),
    )

    // Check if this cell was edited
    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      props.field,
    )

    const shouldHighlight = isEdited || isRedFromAllRedCell

    // Convert boolean values to Yes/No for display
    const displayValue =
      typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
    const cellContent =
      typeof value === 'boolean' ? displayValue : props.children

    return (
      <td
        {...props.tdProps}
        title={displayValue}
        style={{
          color: shouldHighlight ? 'orange' : undefined,
          fontWeight: shouldHighlight ? 'bold' : undefined,
          textAlign: typeof value === 'boolean' ? 'center' : undefined,
        }}
      >
        {cellContent}
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
            width: '100%',
            mb: 1,
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
            {permissions?.addButton &&
              ![1, 2, 3, 4, 5].includes(permissions?.tabIndex) && (
                <Button
                  variant='contained'
                  // className='custom-btn-additem'
                  className='btn-save'
                  onClick={handleAddRow}
                  disabled={false}
                >
                  Add Item
                </Button>
              )}
            {permissions?.saveBtn && (
              <Button
                variant='contained'
                onClick={saveModalOpen}
                disabled={isButtonDisabled}
                className='btn-save'
                // className='custom-btn-save'
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
                // className='custom-btn-calculate'
              >
                Calculate
              </Button>
            )}

            {permissions?.showExport && (
              <Button
                variant='contained'
                onClick={handleExport}
                disabled={isButtonDisabled}
                className='btn-save'
                // className='custom-btn-export'
              >
                Export
              </Button>
            )}

            {permissions?.downloadExcelBtnFromUI && (
              <Button
                variant='contained'
                className='btn-save'
                onClick={excelExport}
                disabled={READ_ONLY || rows?.length === 0}
                // className='custom-btn-export'
              >
                Export
              </Button>
            )}

            {permissions?.showImport && (
              <>
                <Button
                  variant='contained'
                  onClick={triggerFileUpload}
                  disabled={isButtonDisabled || READ_ONLY}
                  className='btn-save'
                  // className='custom-btn-import'
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

            {permissions?.showFinalSubmit && (
              <Button
                variant='contained'
                // className='custom-btn-submit'
                className='btn-save'
              >
                Submit
              </Button>
            )}

            {permissions?.showDropdown && (
              <GenericDropdown
                options={dropdownConfig?.options}
                value={selectedDropdownValue || ''}
                onChange={(value) => setSelectedDropdownValue(value)}
                label={dropdownConfig?.label || 'Select'}
                placeholder={dropdownConfig?.placeholder || 'Select'}
                valueKey={dropdownConfig?.valueKey || 'id'}
                labelKey={dropdownConfig?.labelKey || 'name'}
              />
            )}
          </Box>
        </Box>
      )}

      {/* <Box
        sx={{
          mt:0.5,
          borderRadius: '6px',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
          overflow: 'hidden',
          backgroundColor: '#fff',
          transition: 'boxShadow 0.3s ease',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.12)',
          },
        }}
      > */}
      <div className='kendo-data-grid'>
        <Tooltip openDelay={50} position='auto' anchorElement='target'>
          <ExcelExport
            data={rows}
            ref={_export}
            fileName={`${permissions?.ExcelName}.xlsx`}
          >
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
              defaultGroup={initialGroup}
              defaultTake={getDefaultTake()}
              contextMenu={true}
              filterable={
                permissions.filterable &&
                columns.some((col) => dateFields.includes(col.field))
              }
              size='small'
              pageable={getPaginationConfig()}
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
          </ExcelExport>
        </Tooltip>
      </div>
    {/* </Box> */}

      {/* snackbar toaster */}
      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />

      {/* Delete Dialog */}
      <DeleteDialog
        openDeleteDialogeBox={openDeleteDialogeBox}
        setOpenDeleteDialogeBox={setOpenDeleteDialogeBox}
        deleteTheRecord={deleteTheRecord}
      />
      {/* Remark Dialog */}
      <RemarkDialog
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        handleRemarkSave={handleRemarkSave}
      />
      {/* Save confirmation */}
      <SaveConfirmationDialog
        openSaveDialogeBox={openSaveDialogeBox}
        closeSaveDialogeBox={closeSaveDialogeBox}
        saveConfirmation={saveConfirmation}
      />
    </div>
  )
}

export default AdvanceKendoTable
