import React, { useState } from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import PropTypes from 'prop-types'
import '../../kendo-data-grid.css'

const KendoDataGrid = ({
  rows,
  columns,
  loading = false,
  pageSizes = [10, 20, 50],
  onRowChange,
  disableColor = false,
}) => {
  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const handleItemChange = (e) => {
    const updated = rows.map((r) =>
      r.id === e.dataItem.id ? { ...r, [e.field]: e.value } : r,
    )
    onRowChange?.(updated, e)
  }
  const rowRender = disableColor
    ? (trElement, props) => {
        const shouldDisable = props.dataItem.status === 'inactive'
        return React.cloneElement(trElement, {
          ...trElement.props,
          className: `${trElement.props.className || ''} ${shouldDisable ? 'disabled-row' : ''}`,
        })
      }
    : undefined
  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <div className='k-loading-mask'>
          <span className='k-loading-text'>Loading...</span>
          <div className='k-loading-image' />
          <div className='k-loading-color' />
        </div>
      )}

      <div className='kendo-data-grid'>
        <Grid
          data={filterBy(rows, filter)}
          filterable={true}
          sortable
          dataItemKey='id'
          pageable={{ pageSizes, buttonCount: 5 }}
          editField='inEdit'
          filter={filter}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={handleItemChange}
          
          rowRender={rowRender}
          resizable={true}
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
    </div>
  )
}

KendoDataGrid.propTypes = {
  rows: PropTypes.array.isRequired,
  disableColor: PropTypes.bool,
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      field: PropTypes.string.isRequired,
      title: PropTypes.string,
      width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
      cell: PropTypes.func,
      filterable: PropTypes.bool,
    }),
  ).isRequired,
  loading: PropTypes.bool,
  pageSizes: PropTypes.arrayOf(PropTypes.number),
  onAddRow: PropTypes.func,
  onDeleteRow: PropTypes.func,
  onRowChange: PropTypes.func,
  toolbarButtons: PropTypes.arrayOf(
    PropTypes.shape({
      title: PropTypes.string.isRequired,
      onClick: PropTypes.func.isRequired,
      icon: PropTypes.string,
    }),
  ),
}

export default KendoDataGrid
