import '@progress/kendo-font-icons/dist/index.css'
import {
  Grid,
  GridColumn,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '@progress/kendo-react-grid'
import { process } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import { useCallback, useRef, useState, useEffect, useMemo } from 'react'
import { SvgIcon } from '@progress/kendo-react-common'
import { trashIcon } from '@progress/kendo-svg-icons'
import '../../../../../src/kendo-data-grid.css'
import { Tooltip } from '@progress/kendo-react-tooltip'
import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Typography,
} from '@mui/material'
import {
  FileExportIcon,
  FileImportIcon,
  SaveIcon as SaveImageIcon,
  CalculateIcon as CalculateImageIcon,
} from 'assets/images/icons'
import { DashboardColors } from 'themes/colors'
import DeleteDialog from '../AdvanceKendoTable/components/DeleteDialog'
import SaveConfirmationDialog from '../AdvanceKendoTable/components/SaveConfirmationDialog'
import { ExcelExport } from '@progress/kendo-react-excel-export'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import RemarkDialog from '../AdvanceKendoTable/components/RemarkDialog'
import FilterChips from '../AdvanceKendoTable/components/FilterChips'
import { NumericEditorWithMinMax } from '../utilities/NumericEditorWithMinMax'
import { NumberCellEditor } from '../utilities/NumberCellEditor'
import { getColumnMenuCheckboxFilter } from '../utilities/ColumnMenu1'
import valueFormatterByUOM, {
  recalcDuration,
  recalcEndDate,
} from '../commonUtilityFunctions'
import { NoSpinnerNumericEditor } from '../utilities/numbericColumns'
import { handleTabKeyNavigation } from '../AdvanceKendoTable/utility'
import { useFilterChips } from '../hooks/useFilterChips'
import {
  NumberWithCheckboxCellEditor,
  NumberWithCheckboxDisplayCell,
} from '../utilities/NumberWithCheckboxCellEditor'
import {
  ProposedNormCellEditor,
  ProposedNormDisplayCell,
} from '../utilities/ProposedNormCell'
import Notification from 'components/Utilities/Notification'

import AddIcon from '@mui/icons-material/Add'
import PlaylistAddIcon from '@mui/icons-material/PlaylistAdd'
import DownloadIcon from '@mui/icons-material/Download'
import UploadIcon from '@mui/icons-material/Upload'
import CalculateIcon from '@mui/icons-material/Calculate'
import SaveIcon from '@mui/icons-material/Save'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import Collapse from '@mui/material/Collapse'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { useSelector } from 'react-redux'

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
  'aopStatus',
  'idFromApi',
  'isEditable',
  'period',
]

const ADJUST_PADDING = 4
const COLUMN_MIN = 4

const NestedKendoTable = ({
  title = '',
  rows = [],
  setRows,
  columns,
  loading = false,
  permissions = {},
  setSnackbarOpen = () => {},
  snackbarData = { message: '', severity: 'info' },
  snackbarOpen = false,
  setModifiedCells = () => {},
  modifiedCells = {},
  handleCalculate = () => {},
  saveChanges = () => {},
  handleExport = () => {},
  handleRemarkCellClick = () => {},
  remarkDialogOpen = false,
  setRemarkDialogOpen = () => {},
  currentRemark = '',
  currentRowId = null,
  setCurrentRemark = () => {},
  handleExcelUpload = () => {},
  fetchData = () => {},
  deleteRowData = () => {},
  groupBy = null,
  filterable = false,
  hoursRows = {},
  dateCalculationConfig = {},
  customHeight = null,
  customItemChange = null,
  customAddRow = null,
  isReleaseDisabled = true,
  handleRelease = () => {},
  showFilters = false,
}) => {
  const fileInputRef = useRef(null)
  const minGridWidth = useRef(0)
  const gridRef = useRef(null)
  const _export = useRef(null)
  const activeCellRef = useRef({ rowId: null, field: null })
  const alwaysEditableCellClickedRef = useRef(false)
  const [gridExpanded, setGridExpanded] = useState(true)
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [openDeleteDialogeBox, setOpenDeleteDialogeBox] = useState(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState(false)
  const [openSaveDialogeBox, setOpenSaveDialogeBox] = useState(false)
  const [paramsForDelete, setParamsForDelete] = useState([])
  const closeSaveDialogeBox = () => setOpenSaveDialogeBox(false)
  const [edit, setEdit] = useState({})
  const [sort, setSort] = useState([])
  const [applyMinWidth, setApplyMinWidth] = useState(false)
  const [gridCurrent, setGridCurrent] = useState(0)
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { isReleased, oldYear, verticalObject } = dataGridStore
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const IS_CPP = verticalObject?.name?.toLowerCase() == 'cpp'

  const initialGroup = Array.isArray(groupBy)
    ? groupBy.map((field) => ({ field, dir: undefined }))
    : groupBy
      ? [{ field: groupBy, dir: undefined }]
      : []

  const toggleGrid = () => {
    setGridExpanded((prev) => !prev)
  }

  const menuItemStyle = {
    fontSize: 14,
    fontWeight: 500,
    color: '#303030',
    fontFamily: "'Honeywell Sans Web', 'Inter', sans-serif",
    letterSpacing: '0px',
    verticalAlign: 'middle',
  }

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

  // Extract all leaf columns (columns with field property)
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

    minGridWidth.current = 0
    allColumns.forEach((col) => {
      if (col.minWidth !== undefined) {
        minGridWidth.current += col.minWidth
      }
    })

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

  // Calculate dynamic width for each column
  const setWidth = useCallback(
    (minWidth) => {
      if (minWidth === undefined) {
        minWidth = 80
      }

      const allColumns = extractAllColumns(columns)
      const totalColumns =
        allColumns.length + (permissions?.deleteButton ? 1 : 0)

      let width = applyMinWidth
        ? minWidth
        : minWidth + (gridCurrent - minGridWidth.current) / totalColumns

      if (width >= COLUMN_MIN) {
        width -= ADJUST_PADDING
      }

      return width
    },
    [
      applyMinWidth,
      gridCurrent,
      columns,
      permissions?.deleteButton,
      extractAllColumns,
    ],
  )

  // Helper function to calculate the maximum depth of nested headers
  const getMaxHeaderDepth = useCallback((cols) => {
    const getDepth = (columns, currentDepth = 1) => {
      let maxDepth = currentDepth
      columns.forEach((col) => {
        if (col.children && Array.isArray(col.children)) {
          const childDepth = getDepth(col.children, currentDepth + 1)
          maxDepth = Math.max(maxDepth, childDepth)
        }
      })
      return maxDepth
    }
    return getDepth(cols)
  }, [])

  // Constants for viewport height calculation
  const rowHeightVH = 5 // each row ~5vh
  const baseHeaderVH = 10 // base grid header/filter area
  const pageHeaderVH = 15 // top app bar + stepper + controls
  const maxVH = 75 // cap grid height

  // Calculate dynamic viewport height based on number of rows and nested headers
  const calculatedVH = useMemo(() => {
    if (!rows || rows?.length === 0) return 20

    // Calculate additional header height based on nesting depth
    const headerDepth = getMaxHeaderDepth(columns)
    const additionalHeaderVH = (headerDepth - 1) * 5 // Each additional header level adds ~5vh
    const totalHeaderVH = baseHeaderVH + additionalHeaderVH

    const needed = rows?.length * rowHeightVH + totalHeaderVH
    const available = 100 - pageHeaderVH
    return Math.round(Math.min(needed, maxVH, available))
  }, [rows?.length, columns, getMaxHeaderDepth])

  const handleEditChange = useCallback((e) => {
    setEdit(e.edit)
    if (e.edit && typeof e.edit === 'object') {
      const rowId = Object.keys(e.edit)[0]
      const field = e.edit[rowId]?.[0]
      if (rowId && field) {
        activeCellRef.current = { rowId, field }
      }
    }
  }, [])

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

  const handleRowClick = (e) => {
    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      // Don't clear edit if an alwaysEditable cell (e.g. Proposed Norm checkbox)
      // was just clicked on a non-locked row — handleCellClick already set it
      if (!alwaysEditableCellClickedRef.current) {
        setEdit({})
      }
      alwaysEditableCellClickedRef.current = false
      return
    }

    setRows(
      rows.map((r) => ({
        ...r,
        inEdit: r.id === e.dataItem.id,
      })),
    )
  }

  // Required for nested fields (e.g. 'april.shutdownHrs') — tells Kendo which specific field to edit
  const handleCellClick = (e) => {
    // Guard against group header rows (they have an 'items' array, not a data id)
    if (e.dataItem?.items !== undefined) return

    alwaysEditableCellClickedRef.current = false

    const allColumns = extractAllColumns(columns)
    const clickedColumn = allColumns.find((col) => col.field === e.field)

    // Allow alwaysEditable columns (e.g. Proposed Norm checkbox) to bypass
    // row-level isEditable, but NOT for fully locked rows (isModelCalculated)
    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) {
      if (!clickedColumn?.alwaysEditable || e.dataItem?.isModelCalculated) {
        return
      }
      alwaysEditableCellClickedRef.current = true
    }

    if (clickedColumn?.editable) {
      const rowId = e.dataItem?.id
      setEdit({ [rowId]: [e.field] })
      activeCellRef.current = { rowId: String(rowId), field: e.field }
    }
  }

  // Utility: Get nested property value by path (supports any depth)
  const getNestedValue = (obj, path) => {
    const keys = path.split('.')
    return keys.reduce((acc, key) => acc?.[key], obj)
  }

  // Utility: Set nested property value by path (supports any depth)
  const setNestedValue = (obj, path, value) => {
    const keys = path.split('.')
    const lastKey = keys.pop()
    const target = keys.reduce((acc, key) => {
      if (!acc[key] || typeof acc[key] !== 'object') {
        acc[key] = {}
      }
      return acc[key]
    }, obj)
    target[lastKey] = value
    return obj
  }

  // Get total hours for a month from hoursRows
  const getTotalHoursForMonth = (monthKey) => {
    if (!hoursRows || hoursRows.length === 0) return null

    // Map full month names to short month keys used in hoursRows
    const monthMapping = {
      april: 'apr',
      may: 'may',
      june: 'jun',
      july: 'jul',
      august: 'aug',
      september: 'sep',
      october: 'oct',
      november: 'nov',
      december: 'dec',
      january: 'jan',
      february: 'feb',
      march: 'mar',
    }

    const shortMonthKey = monthMapping[monthKey?.toLowerCase()] || monthKey
    const firstRow = hoursRows[0]

    if (firstRow && firstRow[shortMonthKey] !== undefined) {
      return firstRow[shortMonthKey]
    }
    return null
  }

  // Handle item change with nested data support (supports any depth)
  const itemChange = useCallback(
    (e) => {
      const { dataItem, field, value } = e

      // Guard against undefined field
      if (!field) {
        return
      }

      // Call custom itemChange handler first for validation
      if (customItemChange) {
        const validationResult = customItemChange(e, setRows)
        // If validation returns false, stop processing
        if (validationResult === false) {
          return
        }
      }

      const itemId = dataItem.id

      // Parse field path for nested properties (e.g., "apr.norms" or "apr.details.value")
      const fieldParts = field?.split('.') || []

      setRows((prev) =>
        prev.map((r) => {
          if (r.id !== itemId) return r

          const updated = { ...r }

          // Handle nested field updates at any depth
          if (fieldParts.length > 1) {
            // Deep clone the nested path to avoid mutation
            const keys = [...fieldParts]
            const lastKey = keys.pop()

            // Navigate to the parent object
            let target = updated
            keys.forEach((key, index) => {
              if (index === 0) {
                updated[key] = { ...updated[key] }
                target = updated[key]
              } else {
                target[key] = { ...target[key] }
                target = target[key]
              }
            })

            // Set the value
            target[lastKey] = value

            // Real-time calculation: Auto-calculate netOperationHrs when shutdownHrs is edited
            if (lastKey === 'shutdownHrs' && keys.length === 1) {
              const monthKey = keys[0]
              const totalHrsForMonth = getTotalHoursForMonth(monthKey)
              if (totalHrsForMonth !== null && totalHrsForMonth !== undefined) {
                const shutdownHrs = Math.floor(parseFloat(value)) || 0
                const netOperationHrs = Math.max(
                  0,
                  totalHrsForMonth - shutdownHrs,
                )
                target.netOperationHrs = netOperationHrs
              }
            }
          } else {
            updated[field] = value
          }

          // Handle dateCalculationConfig for generic date calculations
          if (
            dateCalculationConfig &&
            Object.keys(dateCalculationConfig).length > 0
          ) {
            const { dateField1, dateField2, daysField, requiredInHr } =
              dateCalculationConfig
            if (
              dateField1 in updated &&
              dateField2 in updated &&
              daysField in updated
            ) {
              if (field === dateField1 || field === dateField2) {
                updated[daysField] = recalcDuration(
                  updated[dateField1],
                  updated[dateField2],
                  requiredInHr,
                )
              } else if (field === daysField) {
                const newEnd = recalcEndDate(
                  updated[dateField1],
                  value,
                  requiredInHr,
                )
                if (newEnd) {
                  updated[dateField2] = newEnd
                }
              }
            }
          }

          return updated
        }),
      )

      // Track modified cells
      setModifiedCells((prev) => {
        const existingItem = prev[itemId] || { ...dataItem }

        if (fieldParts.length > 1) {
          // Deep clone and set nested value
          const cloned = JSON.parse(JSON.stringify(existingItem))
          setNestedValue(cloned, field, value)

          // Real-time calculation: Auto-calculate netOperationHrs when shutdownHrs is edited
          if (
            fieldParts[fieldParts.length - 1] === 'shutdownHrs' &&
            fieldParts.length === 2
          ) {
            const monthKey = fieldParts[0]
            const totalHrsForMonth = getTotalHoursForMonth(monthKey)
            if (totalHrsForMonth !== null && totalHrsForMonth !== undefined) {
              const shutdownHrs = Math.floor(parseFloat(value)) || 0
              const netOperationHrs = Math.max(
                0,
                totalHrsForMonth - shutdownHrs,
              )
              setNestedValue(
                cloned,
                `${monthKey}.netOperationHrs`,
                netOperationHrs,
              )
            }
          }

          return { ...prev, [itemId]: cloned }
        } else {
          existingItem[field] = value

          // Handle dateCalculationConfig for generic date calculations
          if (
            dateCalculationConfig &&
            Object.keys(dateCalculationConfig).length > 0
          ) {
            const { dateField1, dateField2, daysField, requiredInHr } =
              dateCalculationConfig
            if (
              dateField1 in existingItem &&
              dateField2 in existingItem &&
              daysField in existingItem
            ) {
              if (field === dateField1 || field === dateField2) {
                existingItem[daysField] = recalcDuration(
                  existingItem[dateField1],
                  existingItem[dateField2],
                  requiredInHr,
                )
              } else if (field === daysField) {
                const newEnd = recalcEndDate(
                  existingItem[dateField1],
                  value,
                  requiredInHr,
                )
                if (newEnd) existingItem[dateField2] = newEnd.toISOString()
              }
            }
          }

          return { ...prev, [itemId]: existingItem }
        }
      })

      setCustomModifiedCells((prev) => {
        const base = { ...(prev[itemId] || {}), [field]: value }

        // Real-time calculation: Auto-calculate netOperationHrs when shutdownHrs is edited
        if (
          fieldParts.length === 2 &&
          fieldParts[fieldParts.length - 1] === 'shutdownHrs'
        ) {
          const monthKey = fieldParts[0]
          const totalHrsForMonth = getTotalHoursForMonth(monthKey)
          if (totalHrsForMonth !== null && totalHrsForMonth !== undefined) {
            const shutdownHrs = Math.floor(parseFloat(value)) || 0
            const netOperationHrs = Math.max(0, totalHrsForMonth - shutdownHrs)
            base[`${monthKey}.netOperationHrs`] = netOperationHrs
          }
        }

        // Handle dateCalculationConfig for generic date calculations (for red highlighting)
        if (
          dateCalculationConfig &&
          Object.keys(dateCalculationConfig).length > 0
        ) {
          const { dateField1, dateField2, daysField, requiredInHr } =
            dateCalculationConfig
          if (
            dateField1 in dataItem &&
            dateField2 in dataItem &&
            daysField in dataItem
          ) {
            if (field === dateField1 || field === dateField2) {
              // When dates change, also highlight the calculated duration field
              const calculatedDuration = recalcDuration(
                field === dateField1 ? value : dataItem[dateField1],
                field === dateField2 ? value : dataItem[dateField2],
                requiredInHr,
              )
              base[daysField] = calculatedDuration
            } else if (field === daysField) {
              // When duration changes, also highlight the calculated end date field
              const newEnd = recalcEndDate(
                dataItem[dateField1],
                value,
                requiredInHr,
              )
              if (newEnd) base[dateField2] = newEnd.toISOString()
            }
          }
        }

        return {
          ...prev,
          [itemId]: base,
        }
      })
    },
    [
      setRows,
      setModifiedCells,
      setCustomModifiedCells,
      hoursRows,
      dateCalculationConfig,
      customItemChange,
    ],
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

  const prevModifiedCellsRef = useRef(modifiedCells)

  useEffect(() => {
    const isModifiedCellsEmpty = Object.keys(modifiedCells).length === 0
    const wasPreviouslyNotEmpty =
      Object.keys(prevModifiedCellsRef.current).length > 0

    // Only update if we're transitioning from non-empty to empty
    if (isModifiedCellsEmpty && wasPreviouslyNotEmpty) {
      setCustomModifiedCells({})
      setEdit({})
      setRows((prev) =>
        prev.map((r) => ({
          ...r,
          inEdit: false,
        })),
      )
    }

    prevModifiedCellsRef.current = modifiedCells
  }, [modifiedCells])

  const saveConfirmation = async () => {
    saveChanges()
    setOpenSaveDialogeBox(false)
    setEdit({})
  }

  const handleCalculateBtn = async () => {
    handleCalculate()
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

  const handleRefresh = async () => {
    try {
      fetchData()
    } catch (error) {
      console.error('Error saving refresh data:', error)
    }
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
      <tr {...rest?.trProps} className={rowClassName}>
        {rest.children}
      </tr>
    )
  }, [])

  // Cell renderer with highlight support for nested fields (supports any depth)
  const NestedHighlightCell = (props) => {
    const {
      dataItem,
      field,
      tdProps,
      children,
      customModifiedCells,
      format = null,
    } = props

    // Guard against undefined field
    if (!field) {
      return <td {...tdProps}>{children}</td>
    }
    const rowId = dataItem.id

    // Get value from nested structure at any depth
    const fieldParts = field?.split('.') || []
    let value

    if (fieldParts.length > 1) {
      // Use utility function to get nested value at any depth
      value = getNestedValue(dataItem, field)
    } else {
      value = dataItem[field]
    }

    let formattedValue = value

    // Apply Kendo number format if provided (before UOM formatting)
    if (format && (typeof value === 'number' || typeof value === 'string')) {
      const numValue = typeof value === 'string' ? parseFloat(value) : value
      if (!isNaN(numValue)) {
        formattedValue = applyKendoNumberFormat(numValue, format)
      }
    }

    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    return (
      <td
        {...tdProps}
        title={value}
        className={`${tdProps?.className || ''} ${isEdited ? 'edited-cell' : ''}`.trim()}
        style={{
          ...tdProps?.style,
          color: isEdited ? 'orange' : undefined,
          fontWeight: isEdited ? 'bold' : undefined,
        }}
      >
        {formattedValue}
      </td>
    )
  }

  const RemarkCell = (props) => {
    const { dataItem, field, onRemarkClick, alwaysEditable, ...tdProps } = props
    const rawValue = dataItem[field]
    const displayText = String(rawValue ?? '')
    const rowId = dataItem.id

    // Check if row is editable (standard check) OR if column has alwaysEditable flag
    // Fully locked rows (isModelCalculated) are never editable
    const isRowEditable =
      !dataItem?.isModelCalculated &&
      (alwaysEditable ||
        (!(!dataItem.isEditable && dataItem?.isEditable !== undefined) &&
          typeof onRemarkClick === 'function'))

    // Check if this remark field was edited
    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    return (
      <td
        {...tdProps}
        {...(alwaysEditable ? { 'data-always-editable': 'true' } : {})}
        title={displayText}
        className={`${tdProps?.className || ''} ${isEdited ? 'edited-cell' : ''}`.trim()}
        style={{
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
        {displayText || 'Add remark'}
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
          ...restThProps?.style,
          padding: '0px',
          // borderRight: '1px solid #878787',
          textAlign: 'center',
          width: restThProps['width'],
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

  // Render columns recursively with nested support
  const renderColumns = (cols, filter, sort) =>
    cols.map((col, idx) => {
      const isEditable = !READ_ONLY && col.editable === true
      const isActive = isColumnActive(col.field, filter, sort)

      const headerColorClass = undefined

      // Handle parent columns with children
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

      // Handle custom action cell configured via column config
      if (col.type === 'customAction' && col.cell) {
        return (
          <GridColumn
            key={col.field || 'customActions'}
            field={col.field || 'customActions'}
            title={col.title || 'Action'}
            hidden={col.hidden}
            locked={col?.locked || false}
            width={setWidth(col?.minWidth || col?.widthT) || 100}
            className={col.className || 'k-text-center'}
            filterable={false}
            editable={false}
            cells={{
              data: col.cell,
            }}
          />
        )
      }

      // Handle leaf columns based on type
      if (col.type === 'number' || col.type === 'number1') {
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
                      />
                    ),
                  }
                : {
                    text: (cellProps) => (
                      <NoSpinnerNumericEditor
                        {...cellProps}
                        allowNegative={col.allowNegative || false}
                      />
                    ),
                  },
              data: (props) => (
                <NestedHighlightCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  format={col.format}
                />
              ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }

      if (col.type === 'proposedNorm') {
        // Per-row edit gating is handled in handleCellClick:
        // alwaysEditable columns bypass isEditable, except when isModelCalculated.
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={!READ_ONLY}
            className={`${READ_ONLY ? 'non-editable-cell' : 'k-number-right'}`}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: {
                text: (cellProps) => {
                  // Fully locked rows render display cell instead of editor
                  if (cellProps?.dataItem?.isModelCalculated) {
                    return (
                      <ProposedNormDisplayCell
                        {...cellProps}
                        customModifiedCells={customModifiedCells}
                        format={col.format}
                        isModelCalculated={true}
                        alwaysEditable={col.alwaysEditable && !READ_ONLY}
                      />
                    )
                  }
                  return (
                    <ProposedNormCellEditor
                      {...cellProps}
                      isNumberEditable={col.isNumberEditable}
                      isModelCalculated={false}
                      alwaysEditable={col.alwaysEditable && !READ_ONLY}
                    />
                  )
                },
              },
              data: (props) => (
                <ProposedNormDisplayCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  format={col.format}
                  isModelCalculated={props?.dataItem?.isModelCalculated}
                  alwaysEditable={col.alwaysEditable && !READ_ONLY}
                />
              ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }

      if (col.type === 'numberWithCheckbox') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={!READ_ONLY} // Always editable, ignore row-level isEditable
            className={`${READ_ONLY ? 'non-editable-cell' : 'k-number-right'}`}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: {
                text: (cellProps) => (
                  <NumberWithCheckboxCellEditor
                    {...cellProps}
                    isNumberEditable={col.isNumberEditable}
                    alwaysEditable={col.alwaysEditable && !READ_ONLY}
                  />
                ),
              },
              data: (props) => (
                <NumberWithCheckboxDisplayCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  format={col.format}
                  alwaysEditable={col.alwaysEditable && !READ_ONLY}
                />
              ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
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
                text: (cellProps) => {
                  // For shutdownHrs fields, pass maxValue (total hours for that month)
                  let maxValue = null
                  if (col.field && col.field.includes('shutdownHrs')) {
                    const fieldParts = col.field?.split('.') || []
                    if (fieldParts.length === 2) {
                      const monthKey = fieldParts[0]
                      maxValue = getTotalHoursForMonth(monthKey)
                    }
                  }

                  return (
                    <NumberCellEditor
                      {...cellProps}
                      wholeNumberOnly={true}
                      maxValue={maxValue}
                    />
                  )
                },
              },
              data: (props) => (
                <NestedHighlightCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  format={col.format}
                />
              ),
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            filter='numeric'
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }

      if (col.type === 'text') {
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            locked={col?.locked || false}
            editable={isEditable}
            className={!isEditable ? 'k-left-disabled' : undefined}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            format={col.format}
            width={setWidth(col?.minWidth || col?.widthT || col?.width)}
          />
        )
      }
      // Textarea type handler (for dialog-based editing)
      if (col.type === 'textarea') {
        const isTextareaEditable =
          col.alwaysEditable && !READ_ONLY ? true : isEditable

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
                  onRemarkClick={
                    isTextareaEditable ? handleRemarkCellClick : () => {}
                  }
                  alwaysEditable={col.alwaysEditable && !READ_ONLY}
                />
              ),
            }}
            className={!isTextareaEditable ? 'non-editable-cell' : undefined}
            editable={isTextareaEditable}
            columnMenu={ColumnMenuCheckboxFilter}
            headerClassName={isActive ? 'active-column' : ''}
          />
        )
      }

      // Default column
      return (
        <GridColumn
          key={col.field}
          field={col.field}
          title={col.title || col.headerName}
          hidden={col.hidden}
          locked={col?.locked || false}
          editable={isEditable}
          format={col.format || '{0:0.000}'}
          cells={{
            edit: { text: NoSpinnerNumericEditor },
            data: toolTipRenderer,
            headerCell: SimpleHeaderWithTooltip,
          }}
          className={`${!isEditable ? 'non-editable-cell' : ''}`}
          columnMenu={ColumnMenuCheckboxFilter}
          headerClassName={isActive ? 'active-column' : ''}
          width={setWidth(col?.minWidth || col?.widthT || col?.width)}
        />
      )
    })

  const toolTipRenderer = (props) => {
    // Guard against undefined field
    if (!props.field) {
      return <td {...props.tdProps}>{props.children}</td>
    }

    const fieldParts = props.field?.split('.') || []
    let value

    if (fieldParts.length > 1) {
      value = getNestedValue(props.dataItem, props.field)
    } else {
      value = props.dataItem[props.field]
    }

    const displayValue =
      typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
    const cellContent =
      typeof value === 'boolean' ? displayValue : props.children

    // Check if field was edited - customModifiedCells stores fields as flat keys
    const rowId = props.dataItem?.id
    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      props.field,
    )

    return (
      <td
        {...props.tdProps}
        title={displayValue}
        className={`${props.tdProps?.className || ''} ${isEdited ? 'edited-cell' : ''}`.trim()}
        style={{
          ...props.tdProps?.style,
          textAlign: typeof value === 'boolean' ? 'center' : undefined,
        }}
      >
        {cellContent}
      </td>
    )
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
              {permissions?.showTitleNameBusiness ? (
                <Typography
                  component='div'
                  sx={{
                    fontSize: '16px',
                    fontWeight: 700,
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

                  {permissions.note && (
                    <Typography
                      sx={{
                        fontSize: '16px',
                        fontWeight: 700,
                        color: 'red',
                        fontFamily: "'Honeywell Cond Web', 'Inter', sans-serif",
                      }}
                    >
                      Note :{permissions?.note || ''}
                    </Typography>
                  )}
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
                  onClick={() => {
                    if (customAddRow) {
                      customAddRow()
                    }
                  }}
                  disabled={isButtonDisabled || READ_ONLY}
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
                    !permissions.enableCalculate ||
                    isButtonDisabled ||
                    READ_ONLY
                  }
                  className='btn-calculate'
                >
                  Calculate
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
        <div className='kendo-data-grid'>
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
                resizable={true}
                defaultSkip={0}
                defaultGroup={initialGroup}
                defaultTake={100}
                contextMenu={true}
                filterable={filterable}
                groupable={{
                  enabled: false,
                  footer: 'none',
                  showGroupPanel: false,
                }}
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
                onCellClick={handleCellClick}
                onKeyDown={onTabKeyPressed}
                lockGroups={true}
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
                    locked={false}
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
      </Collapse>

      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />

      {/* Remark Dialog */}
      <RemarkDialog
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        handleRemarkSave={handleRemarkSave}
      />

      <DeleteDialog
        openDeleteDialogeBox={openDeleteDialogeBox}
        setOpenDeleteDialogeBox={setOpenDeleteDialogeBox}
        deleteTheRecord={deleteTheRecord}
        confirmButtonText={'Delete'}
      />

      <SaveConfirmationDialog
        openSaveDialogeBox={openSaveDialogeBox}
        closeSaveDialogeBox={closeSaveDialogeBox}
        saveConfirmation={saveConfirmation}
      />
    </div>
  )
}

export default NestedKendoTable
