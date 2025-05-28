import React, { useState } from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'

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

  const AnkitCell = (props) => {
    return <td>{'Ankit'}</td>
  }

  return (
    <div className='kendo-data-grid'>
      <Grid
        data={rows}
        dataItemKey='id'
        autoProcessData={true}
        sortable={true}
        scrollable='virtual'
        filterable={true}
        filter={filter}
        onFilterChange={(e) => setFilter(e.filter)}
        onItemChange={handleItemChange}
        resizable={true}
        defaultSkip={0}
        defaultTake={10}
        pageable={{
          buttonCount: 4,
          pageSizes: [10, 50, 100],
        }}
      >
        {columns.map(
          ({ field, title, width, cell, format, filterable = true }) => (
            <Column
              key={field}
              field={field}
              title={title}
              width={width}
              filterable={filterable}
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
