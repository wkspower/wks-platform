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
import DeleteDialog from '../AdvanceKendoTable/components/DeleteDialog'
import SaveConfirmationDialog from '../AdvanceKendoTable/components/SaveConfirmationDialog'
import { ExcelExport } from '@progress/kendo-react-excel-export'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import RemarkDialog from '../AdvanceKendoTable/components/RemarkDialog'
import { NumericEditorWithMinMax } from '../utilities/NumericEditorWithMinMax'
import { NumberCellEditor } from '../utilities/NumberCellEditor'
import Notification from '../utilities/Notification'
import { getColumnMenuCheckboxFilter } from '../utilities/ColumnMenu1'
import valueFormatterByUOM, {
  recalcDuration,
  recalcEndDate,
} from '../commonUtilityFunctions'
import { NoSpinnerNumericEditor } from '../utilities/numbericColumns'
import { handleTabKeyNavigation } from '../AdvanceKendoTable/utility'

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
}) => {
  const fileInputRef = useRef(null)
  const minGridWidth = useRef(0)
  const gridRef = useRef(null)
  const _export = useRef(null)
  const activeCellRef = useRef({ rowId: null, field: null })
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
  const READ_ONLY = getRoleName(keycloak)
  const initialGroup = Array.isArray(groupBy)
    ? groupBy.map((field) => ({ field }))
    : groupBy
      ? [{ field: groupBy }]
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
    if (_export.current !== null) {
      _export.current.save()
    }
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

  // Required for nested fields (e.g. 'april.shutdownHrs') — tells Kendo which specific field to edit
  const handleCellClick = (e) => {
    // Guard against group header rows (they have an 'items' array, not a data id)
    if (e.dataItem?.items !== undefined) return

    if (!e.dataItem?.isEditable && e.dataItem?.isEditable !== undefined) return

    const allColumns = extractAllColumns(columns)
    const clickedColumn = allColumns.find((col) => col.field === e.field)
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
        style={{
          color: isEdited ? 'orange' : undefined,
          fontWeight: isEdited ? 'bold' : undefined,
        }}
      >
        {formattedValue}
      </td>
    )
  }

  const RemarkCell = (props) => {
    const { dataItem, field, onRemarkClick, ...tdProps } = props
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
          cursor: isRowEditable ? 'pointer' : 'not-allowed',
          color:
            isEdited && displayText ? 'orange' : rawValue ? 'inherit' : 'gray',
          fontWeight: isEdited && displayText ? 'bold' : undefined,
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
          padding: '0px',
          borderRight: '1px solid #878787',
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
          >
            {renderColumns(col.children, filter, sort)}
          </GridColumn>
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
            className={
              !isEditable ? 'k-number-right-disabled' : 'k-number-right'
            }
            editable={col?.editable ? true : false}
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
                : { text: NoSpinnerNumericEditor },
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
            width={setWidth(col?.minWidth || col?.width)}
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
            width={setWidth(col?.minWidth || col?.widthT)}
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
            editable={col?.editable ? true : false}
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              data: toolTipRenderer,
              headerCell: SimpleHeaderWithTooltip,
            }}
            columnMenu={ColumnMenuCheckboxFilter}
            format={col.format}
            width={setWidth(col?.minWidth || col?.width)}
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
            cells={{
              data: (cellProps) => (
                <RemarkCell
                  {...cellProps}
                  onRemarkClick={isEditable ? handleRemarkCellClick : () => {}}
                />
              ),
            }}
            className={!isEditable ? 'non-editable-cell' : undefined}
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
          width={setWidth(col?.minWidth || col?.width)}
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

    return (
      <td
        {...props.tdProps}
        title={displayValue}
        style={{
          textAlign: typeof value === 'boolean' ? 'center' : undefined,
        }}
      >
        {cellContent}
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
            width: '100%',
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              flexGrow: 1,
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

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {permissions?.showCalculate && (
              <Button
                variant='contained'
                onClick={handleCalculateBtn}
                className='btn-save'
                disabled={
                  !permissions.enableCalculate || isButtonDisabled || READ_ONLY
                }
              >
                Calculate
              </Button>
            )}

            {permissions?.saveBtn && (
              <Button
                variant='contained'
                onClick={saveModalOpen}
                disabled={isButtonDisabled || READ_ONLY}
                className='btn-save'
                // className='custom-btn-save'
              >
                Save
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
                onClick={excelExport}
                disabled={rows?.length === 0}
                className='btn-save'
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
