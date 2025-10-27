import {
  GridColumn as Column,
  Grid,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports/ColumnMenu1'
import DateOnlyPicker from 'components/kendo-data-tables/Utilities-Kendo/DatePicker'
import DateTimePickerEditor from 'components/kendo-data-tables/Utilities-Kendo/DatePickeronSelectedYr'
import { DateColumnMenu } from 'components/Utilities/DateColumnMenu'
import { useState } from 'react'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'
import '../../kendo-data-grid.css'

const KendoDataGrid = ({
  rows,
  columns,
  onRowChange,
  permissions,
  groupBy = null,
  allRedCell = [],
  showThreeColors = false,
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])
  const [isDateFilterActive, setIsDateFilterActive] = useState([])
  const handleItemChange = (e) => {
    const updated = [...rows]
    const index = updated.findIndex((r) => r.id === e.dataItem.id)

    if (index !== -1) {
      updated[index] = { ...updated[index], [e.field]: e.value }
      onRowChange?.(updated, e)
    }
  }

  const initialGroup = groupBy
    ? [
        {
          field: groupBy,
          dir: undefined,
        },
      ]
    : []

  const ColumnMenuCheckboxFilter = getColumnMenuCheckboxFilter(rows)

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]

    return (
      <td {...props.tdProps} title={value}>
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

    const isEdited = Object.prototype.hasOwnProperty.call(
      customModifiedCells?.[rowId] || {},
      field,
    )

    // console.log('dataItem', dataItem)

    const month = field
    const normId =
      dataItem.materialFKId ||
      dataItem.NormParameter_FK_Id ||
      dataItem.Material_FK_Id

    // console.log('normId', normId)
    // console.log('allRedCell', allRedCell)

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

  return (
    <div className='kendo-data-grid'>
      <Tooltip openDelay={50} position='bottom' anchorElement='target'>
        <Grid
          style={{
            flex: 1,
            overflow: 'auto',
            ...(permissions?.isHeight ? { height: 500 } : {}),
          }}
          data={rows}
          dataItemKey='id'
          autoProcessData={true}
          sortable={{
            mode: 'multiple',
          }}
          scrollable='scrollable'
          filter={filter}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={handleItemChange}
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
          defaultGroup={initialGroup}
        >
          {columns?.map((col) => {
            const {
              field,
              title,
              width,
              cell,
              format,
              filterType = 'text',
              isRightAlligned,
              hidden,
              widthT,
              type,
            } = col

            if (['endDate', 'startDate', 'dateTime'].includes(field)) {
              return (
                <Column
                  key={field}
                  field={field}
                  title={title}
                  cell={cell}
                  width={widthT}
                  cells={{
                    edit: {
                      date: ['dateTime', 'dateTime', 'mcuDate'].includes(
                        col.field,
                      )
                        ? DateOnlyPicker
                        : DateTimePickerEditor,
                    },
                    data: toolTipRenderer,
                    headerCell: SimpleHeaderWithTooltip,
                  }}
                  editor='date'
                  format='{0:dd-MM-yyyy}'
                  hidden={hidden}
                  className={
                    isRightAlligned === 'numeric'
                      ? 'k-number-right-disabled'
                      : 'non-editable-cell'
                  }
                  headerClassName={
                    isColumnActive(field, filter, sort) ? 'active-column' : ''
                  }
                  columnMenu={DateColumnMenu}
                />
              )
            }

            if (col.type === 'date') {
              return (
                <Column
                  key={field}
                  field={field}
                  title={title}
                  cell={cell}
                  width={widthT}
                  cells={{
                    edit: {
                      DateOnlyPicker,
                    },
                    data: toolTipRenderer,
                    headerCell: SimpleHeaderWithTooltip,
                  }}
                  editor='date'
                  format='{0:dd-MM-yyyy}'
                  hidden={hidden}
                  className={
                    isRightAlligned === 'numeric'
                      ? 'k-number-right-disabled'
                      : 'non-editable-cell'
                  }
                  headerClassName={
                    isColumnActive(field, filter, sort) ? 'active-column' : ''
                  }
                  columnMenu={DateColumnMenu}
                />
              )
            }

            return (
              <Column
                key={field}
                columnMenu={ColumnMenuCheckboxFilter}
                field={field}
                title={title}
                cell={cell}
                hidden={hidden}
                cells={{
                  data: (props) =>
                    showThreeColors ? (
                      <RedHighlightCell2 {...props} allRedCell={allRedCell} />
                    ) : (
                      toolTipRenderer(props)
                    ),
                  headerCell: SimpleHeaderWithTooltip,
                }}
                format={format}
                className={
                  isRightAlligned === 'numeric'
                    ? 'k-number-right-disabled'
                    : 'non-editable-cell'
                }
                headerClassName={
                  isColumnActive(field, filter, sort) ? 'active-column' : ''
                }
                width={widthT}
              />
            )
          })}
        </Grid>
      </Tooltip>
    </div>
  )
}
export default KendoDataGrid
