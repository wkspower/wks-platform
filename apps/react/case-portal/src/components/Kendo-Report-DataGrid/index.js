import { useEffect, useState } from 'react'
import {
  Grid,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
  GridColumn as Column,
} from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import '../../kendo-data-grid.css'
import { filterIcon } from '@progress/kendo-svg-icons'
import { ColumnMenu } from 'components/data-tables/Reports/columnMenu'
import { getColumnMenuCheckboxFilter } from 'components/data-tables/Reports/ColumnMenu1'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'
import DateTimePickerEditor from 'components/kendo-data-tables/Utilities-Kendo/DatePickeronSelectedYr'

const KendoDataGrid = ({ rows, columns, onRowChange }) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])

  const handleItemChange = (e) => {
    const updated = [...rows]
    const index = updated.findIndex((r) => r.id === e.dataItem.id)

    if (index !== -1) {
      updated[index] = { ...updated[index], [e.field]: e.value }
      onRowChange?.(updated, e)
    }
  }

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

  const HeaderWithTooltip = (props) => {
    return (
      <th {...props.thProps}>
        <a className='k-link' onClick={props.onClick}>
          <span title={props.title}>{props.title}</span>
        </a>
      </th>
    )
  }

  const dateFields = ['endDate', 'startDate', 'dateTime']

  return (
    <div className='kendo-data-grid'>
      <Tooltip openDelay={50} position='bottom' anchorElement='target'>
        <Grid
          style={{ flex: 1, overflow: 'auto' }}
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
          filterable={columns.some((col) => dateFields.includes(col.field))}
          contextMenu={true}
          pageable={
            rows?.length > 100
              ? {
                  buttonCount: 4,
                  pageSizes: [10, 50, 100],
                }
              : false
          }
        >
          {columns.map((col) => {
            const {
              field,
              title,
              width,
              cell,
              format,
              filterType = 'text',
              isRightAlligned,
              hidden,
            } = col

            if (['endDate', 'startDate', 'dateTime'].includes(field)) {
              return (
                <Column
                  key={field}
                  field={field}
                  title={title}
                  filter='date'
                  filterable={{
                    cell: {
                      operator: 'gte',
                      showOperators: true,
                    },
                  }}
                  cell={cell}
                  cells={{
                    // edit: { date: DateTimePickerEditor },
                    data: toolTipRenderer,
                  }}
                  format='{0:dd-MM-yyyy}'
                  filterType='date'
                  hidden={hidden}
                  className={
                    isRightAlligned === 'numeric'
                      ? 'k-number-right-disabled'
                      : 'non-editable-cell'
                  }
                  headerClassName={
                    isColumnActive(field, filter, sort) ? 'active-column' : ''
                  }
                  columnMenu={ColumnMenuCheckboxFilter}
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
                cells={{ data: toolTipRenderer }}
                format={format}
                className={
                  isRightAlligned === 'numeric'
                    ? 'k-number-right-disabled'
                    : 'non-editable-cell'
                }
                headerClassName={
                  isColumnActive(field, filter, sort) ? 'active-column' : ''
                }
              />
            )
          })}
        </Grid>
      </Tooltip>
    </div>
  )
}
export default KendoDataGrid
