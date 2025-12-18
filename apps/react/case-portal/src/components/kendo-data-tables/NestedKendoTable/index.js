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
import { useCallback, useRef, useState, useEffect } from 'react'
import { SvgIcon } from '@progress/kendo-react-common'
import { trashIcon } from '@progress/kendo-svg-icons'
import '../../../kendo-data-grid.css'
import { Tooltip } from '@progress/kendo-react-tooltip'
import { NoSpinnerNumericEditor } from '../Utilities-Kendo/numbericColumns'
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
import valueFormatterByUOM from 'utils/ValueFormatterByUOM'
import { NumberCellEditor } from '../Utilities-Kendo/phase-two/NumberCellEditor'

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
  saveChanges = () => {},
  fetchData = () => {},
  deleteRowData = () => {},
  groupBy = null,
  filterable = false,
  hoursRows={}
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
  const [applyMinWidth, setApplyMinWidth] = useState(false)
  const [gridCurrent, setGridCurrent] = useState(0)
  const [customModifiedCells, setCustomModifiedCells] = useState({})

  const initialGroup = Array.isArray(groupBy)
    ? groupBy.map((field) => ({ field }))
    : groupBy
      ? [{ field: groupBy }]
      : []

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
      const itemId = dataItem.id

      // Parse field path for nested properties (e.g., "apr.norms" or "apr.details.value")
      const fieldParts = field.split('.')

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
                const netOperationHrs = Math.max(0, totalHrsForMonth - shutdownHrs)
                target.netOperationHrs = netOperationHrs
              }
            }
          } else {
            updated[field] = value
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
          if (fieldParts[fieldParts.length - 1] === 'shutdownHrs' && fieldParts.length === 2) {
            const monthKey = fieldParts[0]
            const totalHrsForMonth = getTotalHoursForMonth(monthKey)
            if (totalHrsForMonth !== null && totalHrsForMonth !== undefined) {
              const shutdownHrs = Math.floor(parseFloat(value)) || 0
              const netOperationHrs = Math.max(0, totalHrsForMonth - shutdownHrs)
              setNestedValue(cloned, `${monthKey}.netOperationHrs`, netOperationHrs)
            }
          }

          return { ...prev, [itemId]: cloned }
        } else {
          existingItem[field] = value
          return { ...prev, [itemId]: existingItem }
        }
      })

      setCustomModifiedCells((prev) => {
        const base = { ...(prev[itemId] || {}), [field]: value }

        // Real-time calculation: Auto-calculate netOperationHrs when shutdownHrs is edited
        if (fieldParts.length === 2 && fieldParts[fieldParts.length - 1] === 'shutdownHrs') {
          const monthKey = fieldParts[0]
          const totalHrsForMonth = getTotalHoursForMonth(monthKey)
          if (totalHrsForMonth !== null && totalHrsForMonth !== undefined) {
            const shutdownHrs = Math.floor(parseFloat(value)) || 0
            const netOperationHrs = Math.max(0, totalHrsForMonth - shutdownHrs)
            base[`${monthKey}.netOperationHrs`] = netOperationHrs
          }
        }

        return {
          ...prev,
          [itemId]: base,
        }
      })
    },
    [setRows, setModifiedCells, hoursRows],
  )

  useEffect(() => {
    const isModifiedCellsEmpty = Object.keys(modifiedCells).length === 0

    if (isModifiedCellsEmpty) {
      setCustomModifiedCells({})
      setEdit({})
      setRows((prev) =>
        prev.map((r) => ({
          ...r,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells])

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
      isFormatByUOM = false,
    } = props
    const uomType = dataItem?.uom
    const rowId = dataItem.id

    // Get value from nested structure at any depth
    const fieldParts = field.split('.')
    let value

    if (fieldParts.length > 1) {
      // Use utility function to get nested value at any depth
      value = getNestedValue(dataItem, field)
    } else {
      value = dataItem[field]
    }

    const formattedValue = isFormatByUOM
      ? valueFormatterByUOM(value, uomType)
      : value

    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    return (
      <td
        {...tdProps}
        title={formattedValue}
        style={{
          color: isEdited ? 'orange' : undefined,
          fontWeight: isEdited ? 'bold' : undefined,
        }}
      >
        {isFormatByUOM ? formattedValue : children}
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
      const isEditable = col.editable === true
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
        return (
          <GridColumn
            key={col.field}
            field={col.field}
            title={col.title || col.headerName}
            hidden={col.hidden}
            className={
              !col.editable ? 'k-number-right-disabled' : 'k-number-right'
            }
            editable={col?.editable ? true : false}
            headerClassName={isActive ? 'active-column' : ''}
            cells={{
              edit: { text: NoSpinnerNumericEditor },
              data: (props) => (
                <NestedHighlightCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  isFormatByUOM={true}
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
            className={!col?.editable ? 'k-right-disabled' : undefined}
            headerClassName={`${isActive ? 'active-column' : ''} ${headerColorClass}`}
            cells={{
              edit: {
                text: (cellProps) => {
                  // For shutdownHrs fields, pass maxValue (total hours for that month)
                  let maxValue = null
                  if (col.field?.includes('shutdownHrs')) {
                    const fieldParts = col.field.split('.')
                    if (fieldParts.length === 2) {
                      const monthKey = fieldParts[0]
                      maxValue = getTotalHoursForMonth(monthKey)
                    }
                  }
                  
                  return (
                    <NumberCellEditor
                      {...cellProps}
                      wholeNumberOnly={col?.wholeNumberOnly || true}
                      maxValue={maxValue}
                    />
                  )
                },
              },
              data: (props) => (
                <NestedHighlightCell
                  {...props}
                  customModifiedCells={customModifiedCells}
                  isFormatByUOM={true}
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
    const fieldParts = props.field.split('.')
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
            {permissions?.saveBtn && (
              <Button
                variant='contained'
                className='btn-save'
                onClick={saveModalOpen}
                disabled={isButtonDisabled}
              >
                Save
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
