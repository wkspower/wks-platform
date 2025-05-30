import { useState } from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import '../../kendo-data-grid.css'
import { filterIcon } from '@progress/kendo-svg-icons'
import { ColumnMenu } from 'components/data-tables/Reports/columnMenu'

const KendoDataGrid = ({ rows, columns, onRowChange }) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })

  const handleItemChange = (e) => {
    const updated = [...rows]
    const index = updated.findIndex((r) => r.id === e.dataItem.id)

    if (index !== -1) {
      updated[index] = { ...updated[index], [e.field]: e.value }
      onRowChange?.(updated, e)
    }
  }

  return (
    <div className='kendo-data-grid'>
      <Grid
        style={{ flex: 1, overflow: 'auto' }}
        data={rows}
        dataItemKey='id'
        autoProcessData={true}
        sortable={true}
        scrollable='scrollable'
        filter={filter}
        onFilterChange={(e) => setFilter(e.filter)}
        onItemChange={handleItemChange}
        resizable={true}
        defaultSkip={0}
        defaultTake={100}
        columnMenuIcon={filterIcon}
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
        {columns.map(
          ({ field, title, width, cell, format, filterType = 'text' }) => (
            <Column
              key={field}
              columnMenu={ColumnMenu}
              field={field}
              title={title}
              // width={width}
              cell={cell}
              format={format}
            />
          ),
        )}
      </Grid>
    </div>
  )
}
export default KendoDataGrid
