import '@progress/kendo-font-icons/dist/index.css'
import {
  Grid,
  GridColumn,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '@progress/kendo-react-grid'
import { process } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import GenericDropdown from 'components/aop-phase-two/common/utilities/GenericDropdown'
import { useCallback, useRef, useState, useEffect, useMemo } from 'react'
import '../../../../../src/kendo-data-grid.css'
import '../../css/advance-kendo-table.css'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { handleTabKeyNavigation, applyDateCalculations } from './utility'
import { useFilterChips } from '../hooks/useFilterChips'
import RemarkDialog from './components/RemarkDialog'
import FilterChips from './components/FilterChips'
import DeleteDialog from './components/DeleteDialog'
import SaveConfirmationDialog from './components/SaveConfirmationDialog'
import CalculateConfirmationDialog from './components/CalculateConfirmationDialog'
import { TextCellEditorUpdated } from '../utilities/TextCellEditorUpdated'
import { SelectCellEditor } from '../utilities/SelectCellEditor'
import { MultiselectCellEditor } from '../utilities/MultiselectCellEditor'
import { ConditionalCellEditor } from '../utilities/ConditionalCellEditor'
import { ExcelExport } from '../../../../../node_modules/@progress/kendo-react-excel-export/index'
import { NumberCellEditor } from '../utilities/NumberCellEditor'
import { UomNumberCellEditor } from '../utilities/UomNumberCellEditor'
import { SvgIcon } from '../../../../../node_modules/@progress/kendo-react-common/index'
import { trashIcon } from '../../../../../node_modules/@progress/kendo-svg-icons/dist/index'
import { Tooltip } from '../../../../../node_modules/@progress/kendo-react-tooltip/index'
import { Checkbox } from '@progress/kendo-react-inputs'
import { BooleanCellEditor } from '../utilities/BooleanCellEditor'
import { NumericEditorWithMinMax } from '../utilities/NumericEditorWithMinMax'
import {
  RadioCellEditor,
  RadioDisplayCell,
  InlineRadioCellEditor,
  InlineRadioDisplayCell,
} from '../utilities/RadioCellEditor'
import {
  Box,
  Button,
  Typography,
} from '../../../../../node_modules/@mui/material/index'
import { keyframes } from '@mui/material/styles'
import {
  FileExportIcon,
  FileImportIcon,
  SaveIcon as SaveImageIcon,
  CalculateIcon as CalculateImageIcon,
} from 'assets/images/icons'
import DateOnlyPicker from '../utilities/DatePicker'
import {
  DurationEditor,
  DurationDisplayWithTooltipCell,
} from '../utilities/numericViewCells'
import { NoSpinnerNumericEditor } from '../utilities/numbericColumns'
import { getColumnMenuDateFilter } from '../utilities/ColumnMenuDateFilter'
import { getColumnMenuCheckboxFilter } from '../utilities/ColumnMenu1'
import DateTimePickerEditor from '../utilities/DatePickeronSelectedYr'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import Notification from 'components/Utilities/Notification'

import AddIcon from '@mui/icons-material/Add'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'
import Collapse from '@mui/material/Collapse'
import { useSelector } from 'react-redux'
import DeleteSelectedDialog from './components/DeleteSelectedDialog'
import {
  calculateMonthDuration,
  getMonthStartEndDate,
} from '../utilities/durationHelpers'
import { convertFromScientificNotation } from '../commonUtilityFunctions'
import SegmentedSwitch from 'components/kendo-data-tables/components/SegmentedSwitch'

// Helper function to get nested value from object
const getNestedValue = (obj, path) => {
  if (!path || !obj) return undefined
  const parts = path.split('.')
  let value = obj
  for (let part of parts) {
    value = value?.[part]
  }
  return value
}

// Helper function to extract flat row sequence from grouped data
const extractFlatRowsFromGrouped = (data) => {
  const flatRows = []
  const traverse = (items) => {
    if (!items || !Array.isArray(items)) return
    items.forEach((item) => {
      if (item.items && Array.isArray(item.items)) {
        // This is a group header, traverse its children
        traverse(item.items)
      } else {
        // This is an actual data row
        flatRows.push(item)
      }
    })
  }
  traverse(data)
  return flatRows
}

// Helper function to create select tooltip renderer with label display
const createSelectToolTipRenderer = (allOptions, toolTipRenderer) => {
  return (props) => {
    const value = props.field?.includes('.')
      ? getNestedValue(props.dataItem, props.field)
      : props.dataItem[props.field]
    const displayMode = props.displayMode || 'label'

    let displayChildren = props.children
    let tooltipValue = value

    if (displayMode === 'label' && allOptions) {
      // Normalize values to handle 4 vs 4.0 mismatches for numeric options,
      // and fall back to plain string comparison for string options (e.g. "April")
      const normalizeValue = (val) => {
        if (val === '' || val === null || val === undefined) return ''
        const num = Number(val)
        return isNaN(num) ? String(val).trim() : String(num)
      }
      const option = allOptions.find(
        (opt) => normalizeValue(opt.value) === normalizeValue(value),
      )
      if (option) {
        displayChildren = option.label
        tooltipValue = option.label
      }
    }

    return toolTipRenderer({
      ...props,
      children: displayChildren,
      dataItem: { ...props.dataItem, [props.field]: tooltipValue },
    })
  }
}

// Helper function to apply Kendo number format
const applyKendoNumberFormat = (value, format) => {
  if (!format || value === null || value === undefined) return value

  // Parse Kendo format string like '{0:0.00}' or '{0:0.0000}'
  const match = format.match(/\{0:([^}]+)\}/)
  if (!match) return value

  const formatSpec = match[1]
  const numValue = parseFloat(value)

  if (isNaN(numValue)) return value

  // Handle decimal format like '0.00' or '0.0000'
  if (formatSpec.match(/^0+\.0+$/)) {
    const decimalPlaces = formatSpec.split('.')[1].length
    // Truncate instead of rounding to preserve original precision
    const factor = Math.pow(10, decimalPlaces)
    const truncated = Math.trunc(numValue * factor) / factor
    return truncated.toFixed(decimalPlaces)
  }

  return value
}

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
  'exclusionStartDate',
  'exclusionEndDate',
  'dateOfCommencement',
  'targetDate',
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
  allDescriptionDrpdwn = [],
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
  handleToggleShowAll = (val) => {},
  showThreeColors = false,
  groupBy = null,
  dropdownConfig = {},
  selectedDropdownValue,
  setSelectedDropdownValue,
  paginationConfig = {},
  dateCalculationConfig = {},
  initialFieldValues = {},
  customItemChange = null,
  onApproveClick = null,
  customHeight = null,
  customAddRow = null,
  customActionCell = null,
  externalCustomModifiedCells = null,
  externalSetCustomModifiedCells = null,
  customHandleRemarkSave = null,
  isReleaseDisabled = true,
  handleRelease = () => {},
  handleDeleteSelected = (selectedItems) => {},
  screenType = null,
  siteDropdown = [],
  plantDropdown = [],
  showToggleAllOptions = [],
  showToggleAllState = null,
  defaultGridExpanded = true,
  showFilters = false,
  convertScientificValue = false,
  pagable = true,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    plantObject,
    year,
    oldYear,
    yearChanged,
    verticalObject,
    siteObject,
  } = dataGridStore
  const IS_OLD_YEAR = oldYear?.oldYear
  const AOP_YEAR = year?.selectedYear
  const fileInputRef = useRef(null)
  const minGridWidth = useRef(0)
  const gridRef = useRef(null)
  const gridContainerRef = useRef(null)
  const activeCellRef = useRef({ rowId: null, field: null })
  const _export = useRef(null)
  const [gridExpanded, setGridExpanded] = useState(defaultGridExpanded)
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [openCalculateDialogeBox, setOpenCalculateDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)

  const openCalculateDialogBox = () => {
    setOpenCalculateDialogeBox(true)
  }

  const closeCalculateDialogBox = () => {
    setOpenCalculateDialogeBox(false)
  }

  const handleCalculateConfirmation = async () => {
    closeCalculateDialogBox()
    setIsButtonDisabled(true)
    handleCalculate()
    setTimeout(() => {
      setIsButtonDisabled(false)
    }, 500)
  }
  const [edit, setEdit] = useState({})
  const [sort, setSort] = useState([])
  const [issRowEdited, setIsRowEdited] = useState(false)
  const [applyMinWidth, setApplyMinWidth] = useState(false)
  const [gridCurrent, setGridCurrent] = useState(0)
  const [internalCustomModifiedCells, setInternalCustomModifiedCells] =
    useState({})
  const [disableRedHighlight, setDisableRedHighlight] = useState(false)
  const [selectedRows, setSelectedRows] = useState([])
  const [deleteMultipleConfirms, setDeleteMultipleConfirms] = useState(false)

  // Use external customModifiedCells if provided, otherwise use internal
  const customModifiedCells =
    externalCustomModifiedCells !== null
      ? externalCustomModifiedCells
      : internalCustomModifiedCells
  const setCustomModifiedCells =
    externalSetCustomModifiedCells !== null
      ? externalSetCustomModifiedCells
      : setInternalCustomModifiedCells

  const keycloak = useSession()
  const { isReleased } = dataGridStore
  const IS_RELEASED = isReleased
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const IS_CPP = verticalObject?.name?.toLowerCase() == 'cpp'
  const ColumnMenuCheckboxFilterDate = getColumnMenuDateFilter(rows)
  const initialGroup = Array.isArray(groupBy)
    ? groupBy.map((field) => ({ field, dir: undefined }))
    : groupBy
      ? [{ field: groupBy, dir: undefined }]
      : []

  // Process grouped data to get flat row sequence for tab navigation
  const processedFlatRows = useMemo(() => {
    if (!groupBy || initialGroup.length === 0) {
      return rows
    }

    const processedData = process(rows, {
      group: initialGroup,
      sort: sort,
      filter: filter,
    })

    return extractFlatRowsFromGrouped(processedData.data)
  }, [rows, groupBy, initialGroup, sort, filter])

  const toggleGrid = () => {
    setGridExpanded((prev) => !prev)
  }

  const showDeleteAll = permissions?.deleteMultiple && selectedRows.length > 0

  const menuItemStyle = {
    fontSize: 14,
    fontWeight: 500,
    color: '#303030',
    fontFamily: "'Honeywell Sans Web', 'Inter', sans-serif",
    letterSpacing: '0px',
    verticalAlign: 'middle',
  }

  const softPulse = keyframes`
    0% { transform: scale(1); opacity: 0.6; }
    50% { transform: scale(1.15); opacity: 1; }
    100% { transform: scale(1); opacity: 0.6; }
  `

  // Build pagination configuration with defaults
  const getPaginationConfig = useCallback(() => {
    if (permissions?.makePagable === false) {
      return false
    }
    const defaults = {
      threshold: 100,
      buttonCount: 4,
      pageSizes: [10, 20, 50, 100],
      defaultPageSize: 50,
    }
    const config = { ...defaults, ...paginationConfig }

    if (rows?.length > config.threshold) {
      return {
        buttonCount: config.buttonCount,
        pageSizes: config.pageSizes,
      }
    }
    return false
  }, [rows?.length, paginationConfig, permissions?.makePagable])

  // Constants for viewport height calculation
  const rowHeightVH = 5 // each row ~5vh
  const headerVH = 10 // grid's own header/filter area
  const pageHeaderVH = 20 // top app bar + stepper + controls
  const maxVH = 60 // cap grid height

  // Calculate dynamic viewport height based on number of rows
  const calculatedVH = useMemo(() => {
    if (!rows || rows?.length === 0) return 20
    const needed = rows?.length * rowHeightVH + headerVH
    const available = 100 - pageHeaderVH
    return Math.round(Math.min(needed, maxVH, available))
  }, [rows?.length])

  // Get the default page size from config
  const defaultTake = useMemo(() => {
    const defaults = {
      threshold: 100,
      buttonCount: 4,
      pageSizes: [10, 20, 50, 100],
      defaultPageSize: 50,
    }
    const config = { ...defaults, ...paginationConfig }

    // Always return defaultPageSize - let the pageable prop control if pagination shows
    return config.defaultPageSize
  }, [paginationConfig])

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
    gridRef.current = gridContainerRef.current?.querySelector('.k-grid')
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
      const totalMinWidth =
        allColumns.reduce((sum, col) => {
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
    // e.edit = { rowId: [field] } — extract active cell
    if (e.edit && typeof e.edit === 'object') {
      const rowId = Object.keys(e.edit)[0]
      const field = e.edit[rowId]?.[0]
      if (rowId && field) {
        activeCellRef.current = { rowId, field }
      }
    }
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
    if (!_export.current) {
      return
    }

    const options = _export.current.workbookOptions()

    const CELL_BORDER = {
      size: 1,
      color: '#D9D9D9',
    }

    const HEADER_BORDER = {
      size: 1,
      color: '#808080',
    }

    const GROUP_HEADER_BORDER = {
      size: 2,
      color: '#404040',
    }

    options.sheets.forEach((sheet) => {
      sheet.rows.forEach((row) => {
        const isHeader = row.type === 'header' || row.type === 'group-header'

        row.cells.forEach((cell) => {
          const isMerged = Number(cell.colSpan) > 1

          // -----------------------------
          // Border
          // -----------------------------
          let border = CELL_BORDER

          if (row.type === 'group-header' && isMerged) {
            border = GROUP_HEADER_BORDER
          } else if (isHeader) {
            border = HEADER_BORDER
          }

          cell.borderTop = border
          cell.borderBottom = border
          cell.borderLeft = border
          cell.borderRight = border

          // -----------------------------
          // Header styling
          // -----------------------------
          if (isHeader) {
            cell.background = '#DFDFDF'
            cell.color = '#000000'
            cell.bold = true

            cell.textAlign = 'center'
            cell.verticalAlign = 'center'
            cell.wrap = true
          }

          // -----------------------------
          // Data styling
          // -----------------------------
          if (!isHeader) {
            cell.verticalAlign = 'center'
          }
        })
      })
    })

    _export.current.save(options)
  }
  const handleRowClick = (e) => {
    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      setEdit({})
      return
    }

    setRows((prev) =>
      (prev || []).map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id,
      })),
    )
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

      if (screenType === 'shutdown' && field === 'monthly') {
        const monthDur = calculateMonthDuration(value, AOP_YEAR)
        const [start, end] = getMonthStartEndDate(value, AOP_YEAR)
        if (monthDur) {
          dataItem.durationInHrs = String(monthDur)
        }
        if (start && end) {
          dataItem.maintStartDateTime = start
          dataItem.maintEndDateTime = end
        }
      }

      const isDropdownSiteplant = columns?.some(
        (col) => col.field === 'siteName' && col.type === 'dropdownSiteplant',
      )

      // First update modifiedCells to accumulate all changes
      let updatedModifiedCells
      setModifiedCells((prev) => {
        // Merge with previous modified cells to get all accumulated changes
        const previousModified = prev[itemId] || {}
        // Ensure that the freshly calculated duration and dates from dataItem override previousModified
        const base = { ...dataItem, ...previousModified, [field]: value }
        if (screenType === 'shutdown' && field === 'monthly') {
          if (dataItem.durationInHrs) base.durationInHrs = dataItem.durationInHrs
          if (dataItem.maintStartDateTime) base.maintStartDateTime = dataItem.maintStartDateTime
          if (dataItem.maintEndDateTime) base.maintEndDateTime = dataItem.maintEndDateTime
        }

        if (field === 'siteName' && isDropdownSiteplant) {
          base.plantName = ''
        }

        // Apply date calculations if config is provided (convert dates to ISO strings)
        const dateUpdates = applyDateCalculations(
          base,
          field,
          value,
          dateCalculationConfig,
          true,
        )
        Object.assign(base, dateUpdates)

        updatedModifiedCells = base
        return { ...prev, [itemId]: base }
      })

      // Then update rows using the accumulated modified data
      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r

          // Handle nested field paths (e.g., "summer.kbpsd")
          const fieldParts = field.split('.')
          const updated = { ...r }

          if (fieldParts.length === 1) {
            // Simple field
            updated[field] = value
          } else if (fieldParts.length === 2) {
            // Nested field (e.g., summer.kbpsd)
            const [parent, child] = fieldParts
            updated[parent] = { ...updated[parent], [child]: value }
          } else {
            // Deeper nesting (if needed in future)
            updated[field] = value
          }

          if (field === 'siteName' && isDropdownSiteplant) {
            updated.plantName = ''
          }

          // Apply date calculations using the accumulated modified data
          if (updatedModifiedCells && dateCalculationConfig) {
            const { dateField1, dateField2, daysField } = dateCalculationConfig
            // Copy calculated date fields from modifiedCells (which has ISO strings)
            if (updatedModifiedCells[dateField1] && field !== dateField1) {
              updated[dateField1] = new Date(updatedModifiedCells[dateField1])
            }
            if (updatedModifiedCells[dateField2] && field !== dateField2) {
              updated[dateField2] = new Date(updatedModifiedCells[dateField2])
            }
            if (
              updatedModifiedCells[daysField] !== undefined &&
              field !== daysField
            ) {
              updated[daysField] = updatedModifiedCells[daysField]
            }
          }
          return updated
        }),
      )

      // customModifiedCells: always set per-row custom changes (include months if percentChange)
      setCustomModifiedCells((prev) => {
        const base = { ...(prev[itemId] || {}), [field]: value }

        if (field === 'siteName' && isDropdownSiteplant) {
          base.plantName = ''
        }

        // For customModifiedCells, use dataItem as source for unchanged fields
        const sourceData = { ...dataItem, ...base }
        const dateUpdates = applyDateCalculations(
          sourceData,
          field,
          value,
          dateCalculationConfig,
          true,
        )
        Object.assign(base, dateUpdates)

        return {
          ...prev,
          [itemId]: base,
        }
      })

      // Call custom itemChange handler if provided
      if (customItemChange) {
        customItemChange(e, setRows, setModifiedCells, setCustomModifiedCells)
      }
    },
    [setRows, setModifiedCells, setCustomModifiedCells, customItemChange],
  )

  // Handle Tab key navigation between editable cells in the grid
  const onTabKeyPressed = (e) => {
    handleTabKeyNavigation({
      e,
      activeCellRef,
      columns,
      hiddenFields,
      rows: processedFlatRows, // Use processed flat rows for correct grouped sequence
      setRows,
      setEdit,
      extractAllColumns,
    })
  }

  const prevModifiedCellsRef = useRef(modifiedCells)

  // Close inline edit mode when user clicks outside the grid container
  useEffect(() => {
    const handleOutsideClick = (e) => {
      // Check if click is on Kendo popup/portal elements (dropdown, date picker, etc.)
      const isKendoPopup = e.target.closest(
        '.k-animation-container, .k-popup, .k-list-container, .k-calendar-container',
      )

      // Check if click is inside another AdvanceKendoTable grid instance
      const isInsideAnotherGrid =
        e.target.closest('.kendo-data-grid') &&
        gridContainerRef.current &&
        !gridContainerRef.current.contains(e.target)

      if (
        gridContainerRef.current &&
        !gridContainerRef.current.contains(e.target) &&
        !isKendoPopup && // Don't close if clicking on Kendo popup elements
        !isInsideAnotherGrid // Don't close if clicking inside another grid instance
      ) {
        setRows((prev) =>
          prev.map((r) => (r.inEdit ? { ...r, inEdit: false } : r)),
        )
        setEdit({})
      }
    }
    document.addEventListener('mousedown', handleOutsideClick)
    return () => document.removeEventListener('mousedown', handleOutsideClick)
  }, [setRows])

  useEffect(() => {
    const isModifiedCellsEmpty = Object.keys(modifiedCells).length === 0
    const isCustomModifiedCellsEmpty =
      Object.keys(customModifiedCells).length === 0
    const wasPreviouslyNotEmpty =
      Object.keys(prevModifiedCellsRef.current).length > 0

    if (isModifiedCellsEmpty && !isCustomModifiedCellsEmpty) {
      setCustomModifiedCells({})
    }

    // Only update if we're transitioning from non-empty to empty
    if (isModifiedCellsEmpty && wasPreviouslyNotEmpty) {
      setEdit({})
      setRows((prev) =>
        prev.map((r) => ({
          ...r,
          inEdit: false,
        })),
      )
    }

    prevModifiedCellsRef.current = modifiedCells
  }, [modifiedCells, customModifiedCells])

  const handleRemarkSave = () => {
    // Use custom remark save handler if provided
    if (customHandleRemarkSave) {
      customHandleRemarkSave()
      return
    }

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
            'Remarks',
            'purpose',
            'reasons',
            'majorJobs',
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
    // Use custom add row handler if provided
    if (customAddRow) {
      customAddRow()
      return
    }

    if (isButtonDisabled) return
    setIsButtonDisabled(true)
    // Generate unique ID using timestamp to avoid NaN with non-numeric IDs
    const newRowId = `new_row_${Date.now()}`
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
      isEditable: true, // Ensure new rows are editable
      ...Object.fromEntries(allFields.map((field) => [field, ''])),
      ...initialFieldValues, // Override with any initial values provided
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
  const ActionsCell = ({ dataItem, tdProps }) => {
    const isNonDeletable =
      (!dataItem.isEditable && dataItem?.isEditable !== undefined) ||
      dataItem?.hideDelete === true
    if (isNonDeletable) {
      return (
        <td
          {...tdProps}
          style={{
            ...tdProps?.style,
            textAlign: 'center',
            verticalAlign: 'middle',
          }}
        />
      )
    }
    return (
      <td
        {...tdProps}
        style={{
          ...tdProps?.style,
          textAlign: 'center',
          verticalAlign: 'middle',
        }}
      >
        <SvgIcon
          onClick={() => !READ_ONLY && handleDeleteClick(dataItem)}
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
  const approveModalOpen = async () => {
    if (onApproveClick) {
      onApproveClick()
    }
  }
  const handleCalculateBtn = async () => {
    if (permissions?.showCalulcationPromt) {
      openCalculateDialogBox()
    } else {
      setIsButtonDisabled(true)
      handleCalculate()
      setTimeout(() => {
        setIsButtonDisabled(false)
      }, 500)
    }
  }
  const handleRefresh = async () => {
    try {
      fetchData()
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
  }

  const handleDeleteMultiple = () => {
    if (permissions?.deleteMultiple && selectedRows?.length > 0) {
      handleDeleteSelected(selectedRows)
      setSelectedRows([])
      setDeleteMultipleConfirms(false)
    } else {
      handleDeleteSelected()
      setDeleteMultipleConfirms(false)
    }
  }

  const handleOpenDeleteMultipleDialog = () => {
    setDeleteMultipleConfirms(true)
  }

  const RemarkCell = (props) => {
    const {
      dataItem,
      field,
      onRemarkClick,
      isSorted,
      tdProps,
      selectionChange,
      showPlaceholder = true,
      ...restProps
    } = props
    const rawValue = dataItem[field]
    const displayText = String(rawValue ?? '')
    const rowId = dataItem.id

    // Check if row is editable
    const isRowEditable = !(
      !dataItem.isEditable && dataItem?.isEditable !== undefined
    )

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
          ...tdProps?.style,
          cursor: isRowEditable ? 'pointer' : 'not-allowed',
          color:
            isEdited && displayText ? 'orange' : rawValue ? 'inherit' : 'gray',
          fontWeight: isEdited && displayText ? 700 : 500,
          fontFamily: 'Honeywell Sans Web, Inter, sans-serif',
          fontSize: '15px',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          opacity: isRowEditable ? 1 : 0.6,
        }}
        className={`${tdProps?.className || ''} remark-cell ${isEdited ? 'edited-cell' : ''}`.trim()}
        onClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
        }}
        onDoubleClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
          if (!isRowEditable) {
            return
          }
          onRemarkClick(dataItem)
          setEdit?.({})
        }}
      >
        {displayText || (showPlaceholder ? 'Add remark' : '')}
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
      format = null,
    } = props
    const rowId = dataItem.id
    // Handle nested fields like 'apr.kbpsd'
    let value = field?.includes('.')
      ? getNestedValue(dataItem, field)
      : dataItem[field]
    let formattedValue = value

    // Format Date objects as strings
    if (value instanceof Date) {
      formattedValue = value.toLocaleString('en-GB', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
      })
    }
    // Apply Kendo number format if provided
    else if (
      format &&
      (typeof value === 'number' || typeof value === 'string')
    ) {
      const numValue = typeof value === 'string' ? parseFloat(value) : value
      if (!isNaN(numValue)) {
        formattedValue = applyKendoNumberFormat(numValue, format)
      }
    }

    if (disableRedHighlight) {
      return (
        <td
          {...tdProps}
          title={
            convertScientificValue
              ? convertFromScientificNotation(value)
              : value
          }
          style={{
            ...tdProps?.style,
          }}
        >
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
        title={convertFromScientificNotation(value) ?? value}
        className={`${tdProps?.className || ''} ${shouldHighlight ? 'edited-cell' : ''}`.trim()}
        style={{
          ...tdProps?.style,
        }}
      >
        {formattedValue}
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
      format = null,
    } = props
    const rowId = dataItem.id
    // Handle nested fields like 'apr.kbpsd'
    let value = field?.includes('.')
      ? getNestedValue(dataItem, field)
      : dataItem[field]
    let formattedValue = value

    // Apply Kendo number format if provided
    if (format && (typeof value === 'number' || typeof value === 'string')) {
      const numValue = typeof value === 'string' ? parseFloat(value) : value
      if (!isNaN(numValue)) {
        formattedValue = applyKendoNumberFormat(numValue, format)
      }
    }

    if (disableRedHighlight) {
      return (
        <td
          {...tdProps}
          title={
            convertScientificValue
              ? convertFromScientificNotation(value)
              : value
          }
        >
          {formattedValue}
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
        title={convertFromScientificNotation(value) ?? value}
        className={`${tdProps?.className || ''} ${highlightColor ? 'edited-cell' : ''}`.trim()}
        style={{
          color:
            highlightColor && highlightColor !== 'orange'
              ? highlightColor
              : undefined,
          ...tdProps?.style,
        }}
      >
        {children}
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
        <td
          {...tdProps}
          title={displayValue}
          style={{ textAlign: 'center', ...tdProps?.style }}
        >
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
        className={`${tdProps?.className || ''} ${shouldHighlight ? 'edited-cell' : ''}`.trim()}
        style={{
          ...tdProps?.style,
          textAlign: 'center',
        }}
      >
        {displayValue}
      </td>
    )
  }

  const SimpleHeaderWithTooltip = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}
    const subtitle = props.subtitle

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title={props.title}
        style={{
          ...restThProps?.style,
          padding: '0px',
          // borderRight: '1px solid #878787',
          textAlign: 'start',
          width: { ...restThProps['width'] },
        }}
      >
        <Tooltip
          position='top'
          anchorElement='target'
          parentTitle={true}
          className='test'
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
            <div>{props.children}</div>
            {subtitle && (
              <div
                style={{
                  fontSize: '0.75rem',
                  fontWeight: 'normal',
                  fontStyle: 'italic',
                }}
              >
                {subtitle}
              </div>
            )}
          </div>
        </Tooltip>
      </th>
    )
  }

  // Helper to create header cell with subtitle
  const createHeaderWithSubtitle = (subtitle) => {
    const HeaderWithSubtitle = (props) => (
      <SimpleHeaderWithTooltip {...props} subtitle={subtitle} />
    )
    HeaderWithSubtitle.displayName = `HeaderWithSubtitle(${subtitle})`
    return HeaderWithSubtitle
  }

  const {
    activeFilters,
    getColumnTitle,
    handleRemoveFilter,
    handleClearAllFilters,
  } = useFilterChips(filter, setFilter, columns)

  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  // Helper to check if a cell is editable based on conditional rules
  const isCellEditableByCondition = (dataItem, col) => {
    // Check per-cell configuration from code
    if (dataItem?.nonEditableFields?.includes(col.field)) return false;
    if (dataItem?.editableFields && !dataItem?.editableFields?.includes(col.field)) return false;

    if (!col.conditionalEditable) return true

    const { dependsOn, editableValues } = col.conditionalEditable
    const dependentValue = dataItem[dependsOn]

    // Cell is editable only if dependent field value is in editableValues
    return editableValues.includes(dependentValue)
  }

  const renderMultipleSelectionCheckbox = () => {
    return (
      <GridColumn
        field='selected'
        width='50px'
        headerSelectionValue={
          selectedRows?.length > 0 && selectedRows?.length === rows?.length
        }
        cells={{
          data: (props) => (
            <td style={{ textAlign: 'center' }}>
              <Checkbox
                checked={selectedRows?.includes(props.dataItem?.idFromApi)}
                onChange={() => {
                  const id = props.dataItem?.idFromApi
                  if (selectedRows?.includes(id)) {
                    setSelectedRows(selectedRows?.filter((r) => r !== id))
                  } else {
                    setSelectedRows([...selectedRows, id])
                  }
                }}
              />
            </td>
          ),
          headerCell: () => (
            <th
              style={{
                textAlign: 'center',
                padding: '0px !important',
              }}
            >
              <Checkbox
                checked={
                  selectedRows?.length > 0 &&
                  selectedRows?.length === rows?.length
                }
                onChange={(e) => {
                  const checked = e?.value ?? e?.target?.checked ?? false
                  if (checked) {
                    setSelectedRows(rows.map((r) => r?.idFromApi))
                  } else {
                    setSelectedRows([])
                  }
                }}
              />
            </th>
          ),
        }}
      />
    )
  }

  const renderColumns = (cols, filter, sort) =>
    cols.map((col, idx) => {
      // Determine if column is editable, considering conditional editing rules
      let isEditable = !READ_ONLY && col.editable === true

      // Support conditional editing based on another field's value
      if (isEditable && col.conditionalEditable) {
        const { dependsOn, editableValues } = col.conditionalEditable
        // For each row, check if the dependent field value allows editing
        // This will be checked per-row in the cell editor
        isEditable = true // Column is editable, but per-row logic will apply
      }

      const isActive = isColumnActive(col.field, filter, sort)

      const headerColorClass = undefined
      if (col.children) {
        return (
          <GridColumn
            key={col.title || idx}
            title={col.title}
            headerClassName='center-group-header'
            locked={col?.locked || false}
          >
            {renderColumns(col.children, filter, sort)}
          </GridColumn>
        )
      }

      // Merged Cells Handler
      if (col.type === 'mergedCells' && col.cell) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={false}
            filterable={false}
            className={col.className || 'k-text-center'}
            headerClassName={col.headerClassName || 'k-text-center'}
            cells={{
              data: col.cell,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            width={setWidth(col?.minWidth || col?.widthT || 80)}
          />
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
            locked={col?.locked || false}
            className={!isEditable ? 'non-editable-cell' : undefined}
            cells={{
              data: (cellProps) => {
                const cellIsEditable = isEditable && cellProps.dataItem?.isEditable !== false && isCellEditableByCondition(cellProps.dataItem, col);
                return (
                  <RemarkCell
                    {...cellProps}
                    showPlaceholder={col.showPlaceholder}
                    onRemarkClick={cellIsEditable ? handleRemarkCellClick : () => {}}
                  />
                );
              },
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            headerClassName={isActive ? 'active-column' : ''}
          />
        )
      }

      if (
        [
          'aopRemarks',
          'remarks',
          'remark',
          'Remark',
          'purpose',
          'reasons',
        ].includes(col.field)
      ) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            width={setWidth(col?.minWidth || 120)}
            locked={col?.locked || false}
            className={!isEditable ? 'non-editable-cell' : ''}
            cells={{
              data: (cellProps) => {
                const cellIsEditable = isEditable && cellProps.dataItem?.isEditable !== false && isCellEditableByCondition(cellProps.dataItem, col);
                return (
                  <RemarkCell
                    {...cellProps}
                    onRemarkClick={cellIsEditable ? handleRemarkCellClick : () => {}}
                  />
                );
              },
              headerCell: SimpleHeaderWithTooltip,
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
            locked={col?.locked || false}
            cells={{
              edit: { date: DateOnlyPicker },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            format='{0:dd-MM-yyyy}'
            editor='date'
            editable={isEditable}
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
            locked={col?.locked || false}
            cells={{
              edit: {
                date:
                  col?.type == 'dateTime'
                    ? (props) => (
                        <DateTimePickerEditor
                          {...props}
                          isFinancialYear={col.isFinancialYear !== false}
                        />
                      )
                    : DateOnlyPicker,
              },
              data: (props) => (
                <RedHighlightCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  allRedCell={allRedCell}
                  disableRedHighlight={disableRedHighlight}
                  format={col.format}
                />
              ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
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
            editable={isEditable}
            className={!isEditable ? 'non-editable-cell' : ''}
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
            locked={col?.locked || false}
            editable={isEditable}
            columnMenu={ColumnMenuCheckboxFilter}
            hidden={col.hidden}
            format={'{0:n2}'}
            className={!isEditable ? 'non-editable-cell' : ''}
            cells={{
              edit: { text: DurationEditor },
              data: (cellProps) => (
                <DurationDisplayWithTooltipCell
                  {...cellProps}
                  customModifiedCells={customModifiedCells}
                  allRedCell={allRedCell}
                />
              ),
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
            locked={col?.locked || false}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            editable={isEditable}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: { text: NoSpinnerNumericEditor },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'number') {
        // Determine which numeric editor to use based on min/max constraints
        const hasMinMaxConstraints =
          col.minValue !== undefined || col.maxValue !== undefined

        // Resolve minValue and maxValue from dataItem if they are string references
        const getResolvedValue = (value, dataItem) => {
          if (typeof value === 'string') {
            return dataItem[value]
          }
          return value
        }

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            editable={isEditable}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: hasMinMaxConstraints
                ? {
                    text: (cellProps) => (
                      <NumericEditorWithMinMax
                        {...cellProps}
                        min={getResolvedValue(col.minValue, cellProps.dataItem)}
                        max={getResolvedValue(col.maxValue, cellProps.dataItem)}
                        wholeNumberOnly={col.wholeNumberOnly === true}
                        errorMessage={col.errorMessage}
                      />
                    ),
                  }
                : { text: NoSpinnerNumericEditor },
              data: (props) =>
                showThreeColors ? (
                  <RedHighlightCell2
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    allRedCell2={allRedCell2}
                    disableRedHighlight={disableRedHighlight}
                    format={col.format}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    format={col.format}
                  />
                ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'number1' && permissions.dropdownCell) {
        // Special handler for Pigging Status dropdown in SteadyStateConsumption
        const dropdownOptions = [
          { value: '4', label: 'P-4' },
          { value: '5', label: 'P-5' },
          { value: '1', label: 'NP' },
        ]

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => {
                  const isPiggingStatus =
                    cellProps.dataItem?.normParameterTypeName ===
                      'Pigging Status' ||
                    cellProps.dataItem?.productName ==
                      'Pigging (P)/Non-Pigging (NP)'

                  if (isPiggingStatus) {
                    return (
                      <SelectCellEditor
                        {...cellProps}
                        options={dropdownOptions}
                        textField='label'
                        valueField='value'
                        placeholder='Select...'
                      />
                    )
                  }

                  return <NoSpinnerNumericEditor {...cellProps} />
                },
              },
              data: (props) => {
                const isPiggingStatus =
                  props.dataItem?.normParameterTypeName === 'Pigging Status' ||
                  props.dataItem?.productName == 'Pigging (P)/Non-Pigging (NP)'

                if (isPiggingStatus) {
                  return createSelectToolTipRenderer(
                    dropdownOptions,
                    toolTipRenderer,
                  )({ ...props, displayMode: 'label' })
                }

                return toolTipRenderer(props)
              },
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'number1') {
        // Determine which numeric editor to use based on min/max constraints
        const hasMinMaxConstraints =
          col.minValue !== undefined || col.maxValue !== undefined

        // Resolve minValue and maxValue from dataItem if they are string references
        const getResolvedValue = (value, dataItem) => {
          if (typeof value === 'string') {
            return dataItem[value]
          }
          return value
        }

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => {
                  // Check if cell is editable based on conditional rules
                  const cellEditableByCondition = isCellEditableByCondition(
                    cellProps.dataItem,
                    col,
                  )

                  // If not editable by condition, show disabled display cell
                  if (!cellEditableByCondition) {
                    return (
                      <td
                        style={{
                          backgroundColor: '#f5f5f5',
                          color: '#999',
                          padding: '3px 6px',
                          cursor: 'not-allowed',
                        }}
                      >
                        {cellProps.dataItem[col.field]}
                      </td>
                    )
                  }

                  // Otherwise show the appropriate editor
                  if (hasMinMaxConstraints) {
                    return (
                      <NumericEditorWithMinMax
                        {...cellProps}
                        min={getResolvedValue(col.minValue, cellProps.dataItem)}
                        max={getResolvedValue(col.maxValue, cellProps.dataItem)}
                        wholeNumberOnly={col.wholeNumberOnly === true}
                        errorMessage={col.errorMessage}
                      />
                    )
                  }

                  return (
                    <NoSpinnerNumericEditor
                      {...cellProps}
                      allowNegative={col.allowNegative === true}
                    />
                  )
                },
              },
              data: col.customCell
                ? (props) => (
                    <col.customCell
                      {...props}
                      customModifiedCells={customModifiedCells}
                    />
                  )
                : (props) =>
                    showThreeColors ? (
                      <RedHighlightCell2
                        {...props}
                        customModifiedCells={customModifiedCells}
                        allRedCell={allRedCell}
                        allRedCell2={allRedCell2}
                        disableRedHighlight={disableRedHighlight}
                        format={col.format}
                      />
                    ) : (
                      <RedHighlightCell
                        {...props}
                        customModifiedCells={customModifiedCells}
                        allRedCell={allRedCell}
                        disableRedHighlight={disableRedHighlight}
                        format={col.format}
                      />
                    ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
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
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <NumberCellEditor {...cellProps} wholeNumberOnly={true} />
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
                    format={col.format}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    format={col.format}
                  />
                ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col.type === 'uomWholeNumber' || col.uomWholeNumber) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <UomNumberCellEditor
                    {...cellProps}
                    allowNegative={col.allowNegative === true}
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
                    format={col.format}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    format={col.format}
                  />
                ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
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
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'k-left-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: { text: TextCellEditorUpdated },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            // filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }
      //New Creted Code for Text Type
      if (col.type == 'long-text') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'k-left-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => {
                  // Check if cell is editable based on conditional rules
                  const cellEditableByCondition = isCellEditableByCondition(
                    cellProps.dataItem,
                    col,
                  )

                  // If not editable by condition, show disabled display cell
                  if (!cellEditableByCondition) {
                    return (
                      <td
                        style={{
                          backgroundColor: '#f5f5f5',
                          color: '#999',
                          padding: '3px 6px',
                          cursor: 'not-allowed',
                        }}
                      >
                        {cellProps.dataItem[col.field]}
                      </td>
                    )
                  }

                  return <TextCellEditorUpdated {...cellProps} />
                },
              },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            // filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }

      // Row-Based Type - uses custom cells from RowBasedKendoTable wrapper
      if (col.type === 'row-based' && col.cells) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              ...col.cells,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Conditional Type - handles both dropdown and numeric based on row data
      if (col.type === 'conditional') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <ConditionalCellEditor {...cellProps} format={col.format} />
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
                    format={col.format}
                  />
                ) : (
                  <RedHighlightCell
                    {...props}
                    customModifiedCells={customModifiedCells}
                    allRedCell={allRedCell}
                    disableRedHighlight={disableRedHighlight}
                    format={col.format}
                  />
                ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col?.type === 'dropdownSiteplant') {
        const isSiteColumn = col.field === 'siteName'

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            cells={{
              edit: {
                text: (cellProps) => {
                  let options = []
                  if (isSiteColumn) {
                    options = siteDropdown.map((s) => ({
                      label: s.displayName || s.name,
                      value: s.name,
                    }))
                  } else {
                    const selectedSiteName = cellProps.dataItem.siteName
                    const site = siteDropdown.find(
                      (s) => s.name === selectedSiteName,
                    )
                    const plants = site?.plants || []
                    options = plants.map((p) => ({
                      label: p.displayName || p.name,
                      value: p.name,
                    }))
                  }
                  return (
                    <SelectCellEditor
                      {...cellProps}
                      options={options}
                      textField='label'
                      valueField='value'
                      placeholder='Select...'
                      searchable={col.searchable || false}
                      showClearOption={col.showClearOption || false}
                    />
                  )
                },
              },
              data: (props) => {
                let options = []
                if (isSiteColumn) {
                  options = siteDropdown.map((s) => ({
                    label: s.displayName || s.name,
                    value: s.name,
                  }))
                } else {
                  const selectedSiteName = props.dataItem.siteName
                  const site = siteDropdown.find(
                    (s) => s.name === selectedSiteName,
                  )
                  const plants = site?.plants || []
                  options = plants.map((p) => ({
                    label: p.displayName || p.name,
                    value: p.name,
                  }))
                }
                return createSelectToolTipRenderer(
                  options,
                  toolTipRenderer,
                )({ ...props, displayMode: col.displayMode || 'label' })
              },
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col?.type === 'select') {
        const isDynamic = !!col.dynamicOptions

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            cells={{
              edit: {
                text: (cellProps) => {
                  // Check if cell is editable based on conditional rules
                  const cellEditableByCondition = isCellEditableByCondition(
                    cellProps.dataItem,
                    col,
                  )
                  if (!cellEditableByCondition) {
                    const resolvedOptions = isDynamic
                      ? col.getOptions(cellProps.dataItem)
                      : col.options
                    return createSelectToolTipRenderer(
                      resolvedOptions,
                      toolTipRenderer,
                    )({
                      ...cellProps,
                      displayMode: col.displayMode || 'label',
                      tdProps: {
                        ...cellProps.tdProps,
                        className:
                          `${cellProps.tdProps?.className || ''} non-editable-cell`.trim(),
                      },
                    })
                  }

                  // Resolve options per-row here, where cellProps.dataItem is available
                  const resolvedOptions = isDynamic
                    ? col.getOptions(cellProps.dataItem)
                    : col.options
                  return (
                    <SelectCellEditor
                      {...cellProps}
                      options={resolvedOptions}
                      textField='label'
                      valueField='value'
                      placeholder='Select...'
                      searchable={col.searchable || false}
                      showClearOption={col.showClearOption || false}
                      returnFullObject={col.returnFullObject || false}
                    />
                  )
                },
              },
              data: (props) => {
                // Resolve options per-row here, where props.dataItem is available
                const resolvedOptions = isDynamic
                  ? col.getOptions(props.dataItem)
                  : col.options
                // If conditional rules block editing for this row, add the
                // non-editable-cell class so the display cell gets gray styling.
                const blockedByCondition =
                  col.conditionalEditable &&
                  !isCellEditableByCondition(props.dataItem, col)
                return createSelectToolTipRenderer(
                  resolvedOptions,
                  toolTipRenderer,
                )({
                  ...props,
                  displayMode: col.displayMode || 'label',
                  tdProps: blockedByCondition
                    ? {
                        ...props.tdProps,
                        className:
                          `${props.tdProps?.className || ''} non-editable-cell`.trim(),
                      }
                    : props.tdProps,
                })
              },
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col?.type === 'customSelect' || col?.cellEditor) {
        const isDynamic = !!col.dynamicOptions
        const EditorComponent = col.cellEditor

        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            cells={{
              edit: {
                text: (cellProps) => {
                  const resolvedOptions = isDynamic
                    ? col.getOptions(cellProps.dataItem)
                    : col.options
                  return (
                    <EditorComponent
                      {...cellProps}
                      options={resolvedOptions}
                      textField={col.textField || 'label'}
                      valueField={col.valueField || 'value'}
                      placeholder={col.placeholder || 'Select...'}
                      searchable={col.searchable || false}
                      showClearOption={col.showClearOption || false}
                      returnFullObject={col.returnFullObject || false}
                    />
                  )
                },
              },
              data: (props) => {
                const resolvedOptions = isDynamic
                  ? col.getOptions(props.dataItem)
                  : col.options
                return createSelectToolTipRenderer(
                  resolvedOptions,
                  toolTipRenderer,
                )({ ...props, displayMode: col.displayMode || 'label' })
              },
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }
      if (col?.type === 'multi-select') {
        // Change this to your multiselect field name
        let allOptions = col.options || []
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            cells={{
              edit: {
                text: (cellProps) => (
                  <MultiselectCellEditor
                    {...cellProps}
                    options={allOptions}
                    textField='label'
                    valueField='value'
                    placeholder='Select multiple...'
                    tagLimit={3} // Optional: limit display tags
                  />
                ),
              },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
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
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <BooleanCellEditor
                    {...cellProps}
                    useCheckbox={col?.useCheckbox !== false}
                    trueLabel={col?.trueLabel || 'Yes'}
                    falseLabel={col?.falseLabel || 'No'}
                    size={col?.size || 'medium'}
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
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Checkbox Type Handler — column-level checkbox (col.type === 'checkbox')
      // Fires itemChange like any other editor so changes are tracked in modifiedCells
      if (col.type === 'checkbox') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'non-editable-cell' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''}`}
            cells={{
              edit: {
                text: (cellProps) => {
                  const { dataItem, field, onChange } = cellProps
                  const checked = !!dataItem[field]
                  const isCellDisabled =
                    !isEditable || dataItem?.isEditable === false || !isCellEditableByCondition(dataItem, col)
                  return (
                    <td style={{ textAlign: 'center', padding: '6px 2px' }}>
                      {dataItem?.hideCheckbox ? null : (
                        <Checkbox
                          checked={checked}
                          disabled={isCellDisabled}
                          onChange={(e) => {
                            if (isCellDisabled) return
                            const newVal =
                              typeof e.value === 'boolean'
                                ? e.value
                                : e.target?.checked ?? !checked
                            onChange({ dataItem, field, value: newVal })
                          }}
                          size='medium'
                        />
                      )}
                    </td>
                  )
                },
              },
              data: (cellProps) => {
                const { dataItem, field, tdProps } = cellProps
                const checked = !!dataItem[field]
                const isCellDisabled =
                  !isEditable || dataItem?.isEditable === false || !isCellEditableByCondition(dataItem, col)
                return (
                  <td
                    {...tdProps}
                    style={{
                      textAlign: 'center',
                      ...tdProps?.style,
                    }}
                  >
                    {dataItem?.hideCheckbox ? null : (
                      <div style={{ pointerEvents: 'none', display: 'inline-block' }}>
                        <Checkbox
                        checked={checked}
                        disabled={isCellDisabled}
                        onChange={(e) => {
                          if (isCellDisabled) return
                          const newVal =
                            typeof e.value === 'boolean'
                              ? e.value
                              : e.target?.checked ?? !checked
                          itemChange({ dataItem, field, value: newVal })
                        }}
                        size='medium'
                      />
                      </div>
                    )}
                  </td>
                )
              },
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Number with Inline Radio Type Handler
      if (col.type === 'numberWithRadio') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <InlineRadioCellEditor
                    {...cellProps}
                    radioGroupField={
                      col.radioGroupField || 'selectedHeatRateSource'
                    }
                    targetField={col.targetField || 'finalHeatRate'}
                    radioValue={col.radioValue}
                    isNumberEditable={col.numericEditable || false}
                  />
                ),
              },
              data: (cellProps) => (
                <InlineRadioDisplayCell
                  {...cellProps}
                  radioGroupField={
                    col.radioGroupField || 'selectedHeatRateSource'
                  }
                  format={col.format}
                  radioValue={col.radioValue}
                  customModifiedCells={customModifiedCells}
                  isNumberEditable={col.numericEditable || false}
                />
              ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Radio Type Handler
      if (col.type === 'radio') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => (
                  <RadioCellEditor
                    {...cellProps}
                    sourceFields={col.sourceFields || []}
                    targetField={col.targetField || ''}
                  />
                ),
              },
              data: (cellProps) => (
                <RadioDisplayCell
                  {...cellProps}
                  sourceFields={col.sourceFields || []}
                />
              ),
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      if (col?.type === 'discriptionDrpdwn') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            cells={{
              edit: {
                text: (cellProps) => (
                  <SelectCellEditor
                    {...cellProps}
                    options={allDescriptionDrpdwn || []}
                    textField='displayName'
                    valueField='id'
                    placeholder='Select...'
                  />
                ),
              },
              data: toolTipRenderer,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT || 150)}
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
            locked={col?.locked || false}
            editable={isEditable}
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
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            className={!isEditable ? 'non-editable-cell' : ''}
            width={setWidth(col?.minWidth || col?.widthT)}
          />
        )
      }

      // Custom Action Cell Handler — always render column, pass editable to cell
      if (col.type === 'customAction' && col.cell) {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={false}
            filterable={false}
            className={col.className || 'k-text-center'}
            headerClassName={col.headerClassName || 'k-text-center'}
            cells={{
              data: col.cell,
              headerCell: col.subtitle
                ? createHeaderWithSubtitle(col.subtitle)
                : SimpleHeaderWithTooltip,
            }}
            width={setWidth(col?.minWidth || col?.widthT || 80)}
          />
        )
      }

      return (
        <GridColumn
          key={col.field}
          field={col.field}
          title={col.title || col.headerName}
          locked={col?.locked || false}
          editable={col.editable || false}
          format={col.format || '{0:0.000}'}
          cells={{
            edit: { text: NoSpinnerNumericEditor },
            data: toolTipRenderer,
            headerCell: col.subtitle
              ? createHeaderWithSubtitle(col.subtitle)
              : SimpleHeaderWithTooltip,
          }}
          className={`${!isEditable ? 'non-editable-cell' : ''}`}
          columnMenu={ColumnMenuCheckboxFilter}
          headerClassName={isActive ? 'active-column' : ''}
          width={setWidth(col?.minWidth || col?.widthT)}
        />
      )
    })

  const toolTipRenderer = (props) => {
    const value = props.field?.includes('.')
      ? getNestedValue(props.dataItem, props.field)
      : props.dataItem[props.field]

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

    // Convert boolean values or scientific notation for display in tooltip title
    const displayValue =
      typeof value === 'boolean'
        ? value
          ? 'Yes'
          : 'No'
        : convertFromScientificNotation(value) ?? value

    const cellContent =
      typeof value === 'boolean' ? displayValue : props.children

    return (
      <td
        {...props.tdProps}
        title={displayValue}
        className={`${props.tdProps?.className || ''} ${shouldHighlight ? 'edited-cell' : ''}`.trim()}
        style={{
          ...props.tdProps?.style,
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
    <div className='k-table-box' style={{ position: 'relative' }}>
      {loading && <LoaderBackdrop open={!!loading} />}

      {(permissions?.allAction ?? true) && (
        <Box className='action-box' sx={{ mb: 1 }}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              width: '100%',
              ...(permissions?.marginTop && { marginTop: '10px' }),
            }}
          >
            {/* Left side - Title + toggle */}
            <Box>
              {title ? (
                <Typography
                  component='div'
                  sx={{
                    fontSize: '16px',
                    fontWeight: 700,
                    fontFamily: 'Honeywell Sans Web, Inter, sans-serif',
                    color: '#252525',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    mb: permissions?.marginBottom ? '12px' : '4px',
                  }}
                >
                  {/* TOGGLE ICON */}
                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 32,
                      height: 32,
                      borderRadius: '6px',
                      backgroundColor: '#ECEEFF',
                      color: '#1e293b',
                      ml: 1,
                      cursor: 'pointer',
                      padding: '8px',
                    }}
                    onClick={toggleGrid}
                  >
                    <KeyboardArrowUpIcon
                      sx={{
                        fontSize: 20,
                        transition: '0.2s',
                        transform: gridExpanded
                          ? 'rotate(0deg)'
                          : 'rotate(180deg)',
                      }}
                    />
                  </Box>

                  {/* TITLE */}
                  {title || permissions?.titleName}
                  {permissions?.showNote && (
                    <Box sx={{ pl: 0.1 }}>
                      <Typography
                        component='div'
                        sx={{
                          fontSize: '14px',
                          fontWeight: '600',
                          fontFamily: "'Honeywell Cond Web', 'Inter', sans-serif",
                          display: 'flex',
                          alignItems: 'center',
                        }}
                      >
                        {permissions?.note}
                      </Typography>
                    </Box>
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
                  {/* ROWS BADGE */}
                  <Box
                    sx={{
                      p: '4px 8px',
                      borderRadius: '100px',
                      backgroundColor: '#ECEEFF',
                      border: '1px solid #41424D',
                    }}
                  >
                    <Typography
                      sx={{
                        fontSize: '12px',
                        fontWeight: 700,
                        color: '#41424D',
                        fontFamily: "'Honeywell Cond Web', 'Inter', sans-serif",
                      }}
                    >
                      {rows?.length || 0} {rows?.length === 1 ? 'Row' : 'Rows'}
                    </Typography>
                  </Box>
                </Typography>
              ) : (
                /* Only toggle icon when showTitleNameBusiness is false */
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: 32,
                    height: 32,
                    borderRadius: '6px',
                    backgroundColor: '#ECEEFF',
                    color: '#1e293b',
                    ml: 1,
                    cursor: 'pointer',
                    padding: '8px',
                  }}
                  onClick={toggleGrid}
                >
                  <KeyboardArrowUpIcon
                    sx={{
                      fontSize: 20,
                      transition: '0.2s',
                      transform: gridExpanded
                        ? 'rotate(0deg)'
                        : 'rotate(180deg)',
                    }}
                  />
                </Box>
              )}
              
            </Box>

            {/* Right side - Unit dropdown + action buttons */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                paddingBottom: 0.25,
              }}
            >
              {permissions?.addButton && (
                <Button
                  variant='contained'
                  className='btn-add'
                  startIcon={<AddIcon />}
                  onClick={handleAddRow}
                  disabled={
                    isButtonDisabled ||
                    READ_ONLY ||
                    permissions?.disableActionButtons
                  }
                >
                  {permissions?.addBtnName || 'Add Item'}
                </Button>
              )}

              {permissions?.showExport && (
                <Button
                  variant='contained'
                  className='btn-export'
                  startIcon={
                    <Box
                      component='img'
                      src={FileExportIcon}
                      className='w16-icon'
                    />
                  }
                  onClick={handleExport}
                  disabled={isButtonDisabled || rows?.length === 0}
                >
                  Export
                </Button>
              )}

              {permissions?.downloadExcelBtnFromUI && (
                <Button
                  variant='contained'
                  className='btn-export'
                  startIcon={
                    <Box
                      component='img'
                      src={FileExportIcon}
                      className='w16-icon'
                    />
                  }
                  onClick={excelExport}
                  disabled={rows?.length === 0}
                >
                  Export
                </Button>
              )}

              {permissions?.showImport && (
                <>
                  <Button
                    variant='contained'
                    onClick={triggerFileUpload}
                    startIcon={
                      <Box
                        component='img'
                        src={FileImportIcon}
                        className='w16-icon'
                      />
                    }
                    disabled={
                      isButtonDisabled || READ_ONLY || rows?.length === 0
                    }
                    className='btn-import'
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
                  startIcon={
                    <Box
                      component='img'
                      src={SaveImageIcon}
                      className='w16-icon'
                    />
                  }
                  onClick={saveModalOpen}
                  disabled={
                    isButtonDisabled ||
                    READ_ONLY ||
                    permissions?.disableActionButtons ||
                    Object.keys(modifiedCells).length === 0
                  }
                >
                  Save
                </Button>
              )}

              {permissions?.showCalculate && (
                <Button
                  variant='contained'
                  onClick={handleCalculateBtn}
                  startIcon={
                    <Box
                      component='img'
                      src={CalculateImageIcon}
                      className='w16-icon'
                    />
                  }
                  disabled={
                    isButtonDisabled ||
                    READ_ONLY ||
                    !!permissions?.calculateDisabled
                  }
                  className='btn-calculate'
                >
                  Calculate
                </Button>
              )}

              {permissions?.showFinalSubmit && (
                <Button
                  variant='contained'
                  disabled={isButtonDisabled || READ_ONLY}
                  className='btn-save'
                >
                  Submit
                </Button>
              )}

              {permissions?.showReleaseBtn && (
                <Button
                  variant='contained'
                  className='btn-save'
                  disabled={isReleaseDisabled || READ_ONLY}
                  onClick={handleRelease}
                >
                  Release
                </Button>
              )}

              {permissions?.deleteMultiple && (
                <Button
                  variant='contained'
                  className='btn-calculate'
                  onClick={handleOpenDeleteMultipleDialog}
                  disabled={isButtonDisabled || READ_ONLY || !showDeleteAll}
                >
                  Delete
                </Button>
              )}
              {permissions?.showToggleAll && (
                <SegmentedSwitch
                  options={showToggleAllOptions}
                  value={showToggleAllState}
                  onChange={handleToggleShowAll}
                  disabled={isButtonDisabled || READ_ONLY}
                />
              )}
            </Box>
          </Box>
        </Box>
      )}

      {(showFilters || IS_CPP) && (
        <FilterChips
          activeFilters={activeFilters}
          getColumnTitle={getColumnTitle}
          handleRemoveFilter={handleRemoveFilter}
          handleClearAllFilters={handleClearAllFilters}
        />
      )}

      <Collapse in={gridExpanded}>
        <div className='kendo-data-grid' ref={gridContainerRef}>
          <Tooltip openDelay={50} position='auto' anchorElement='target'>
            <ExcelExport
              data={rows}
              ref={_export}
              fileName={`${permissions?.ExcelName}.xlsx`}
            >
              <Grid
                key={groupBy}
                style={{
                  flex: 1,
                  overflow: 'auto',
                  height: customHeight
                    ? `${customHeight}vh`
                    : rows?.length > 10
                      ? `${calculatedVH}vh`
                      : undefined,
                }}
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
                onKeyDown={(e) => onTabKeyPressed(e)}
                resizable={true}
                defaultSkip={0}
                defaultGroup={initialGroup}
                defaultTake={defaultTake}
                contextMenu={true}
                filterable={
                  permissions.filterable &&
                  columns.some((col) => dateFields.includes(col.field))
                }
                groupable={{
                  enabled: false,
                  footer: 'none',
                  showGroupPanel: false,
                }}
                size='small'
                pageable={pagable && getPaginationConfig()}
                onRowClick={handleRowClick}
                lockGroups={true}
              >
                {permissions?.deleteMultiple &&
                  renderMultipleSelectionCheckbox()}
                {renderColumns(
                  columns.filter(
                    (col) =>
                      !hiddenFields.includes(col.field) &&
                      !col.hidden &&
                      col.isVisible !== false,
                  ),
                  filter,
                  sort,
                )}

                {!READ_ONLY && permissions?.deleteButton && (
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

                {!READ_ONLY && customActionCell && (
                  <GridColumn
                    key='customActions'
                    field='customActions'
                    title='Action'
                    width={80}
                    className='k-text-center'
                    filterable={false}
                    editable={false}
                    cells={{
                      data: customActionCell,
                    }}
                  />
                )}
              </Grid>
            </ExcelExport>
          </Tooltip>
        </div>
      </Collapse>

      {/* snackbar toaster */}
      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
        autoHide={snackbarData?.autoHide}
      />

      {/* Delete Dialog */}
      <DeleteDialog
        openDeleteDialogeBox={openDeleteDialogeBox}
        setOpenDeleteDialogeBox={setOpenDeleteDialogeBox}
        deleteTheRecord={deleteTheRecord}
        confirmButtonText={'Delete'}
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
      {/* Calculate confirmation */}
      <CalculateConfirmationDialog
        openCalculateDialogeBox={openCalculateDialogeBox}
        closeCalculateDialogBox={closeCalculateDialogBox}
        handleCalculateConfirmation={handleCalculateConfirmation}
      />
      {/* Delete Selected Dialog */}
      <DeleteSelectedDialog
        openDeleteDialogeBox={deleteMultipleConfirms}
        setOpenDeleteDialogeBox={setDeleteMultipleConfirms}
        deleteTheRecord={handleDeleteMultiple}
        confirmButtonText={'Delete'}
      />
    </div>
  )
}

export default AdvanceKendoTable
