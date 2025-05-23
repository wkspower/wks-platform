import React, { useState } from 'react'
import {
  Grid,
  GridColumn as Column,
  GridToolbar,
  GridColumnMenuWrapper,
  GridColumnMenuColumnsChooser,
  GridColumnMenuFilter,
  GridColumnMenuSort,
} from '@progress/kendo-react-grid'
import { filterBy } from '@progress/kendo-data-query'
import '@progress/kendo-theme-default/dist/all.css'
import PropTypes from 'prop-types'
import '../../kendo-data-grid.css'
// import {

// } from '../../../node_modules/@progress/kendo-react-grid/index'
// import { GridColumnMenu } from '@progress/kendo-react-grid' // the built-in full menu
const FullColumnMenu = (props) => {
  return (
    <GridColumnMenuWrapper {...props}>
      {/* Sort tab */}
      <GridColumnMenuSort {...props} />

      {/* Filter tab */}
      <GridColumnMenuFilter {...props} />

      {/* Column chooser tab */}
      <GridColumnMenuColumnsChooser {...props} />

      {/* (optional) any extra buttons here */}
    </GridColumnMenuWrapper>
  )
}
Grid.defaultProps = {
  columnMenu: FullColumnMenu,
}

const KendoDataGrid = ({
  rows,
  columns,
  loading = false,
  pageSizes = [10, 20, 50],
  onAddRow,
  onDeleteRow,
  onRowChange,
  toolbarButtons = [],
  disableColor = false, // ← new prop
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
        const shouldDisable = props.dataItem.status === 'inactive' // example condition
        return React.cloneElement(trElement, {
          ...trElement.props,
          className: `${trElement.props.className || ''} ${shouldDisable ? 'disabled-row' : ''}`,
        })
      }
    : undefined
  const deleteCommand = (props) => (
    <td>
      <button
        className='k-button k-grid-delete-command'
        onClick={() => onDeleteRow?.(props.dataItem)}
      >
        Delete
      </button>
    </td>
  )
  const [showHeader, setShowHeader] = useState(true)

  return (
    <div style={{ position: 'relative' }}>
      {loading && (
        <div className='k-loading-mask'>
          <span className='k-loading-text'>Loading...</span>
          <div className='k-loading-image' />
          <div className='k-loading-color' />
        </div>
      )}
      {/* Toggle Button */}
      <div
        style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 8 }}
      >
        <button
          onClick={() => setShowHeader((prev) => !prev)}
          style={{ marginBottom: '8px' }}
        >
          {showHeader ? 'Hide Filter' : 'Show Filter'}
        </button>
      </div>
      <div className='kendo-data-grid'>
        <Grid
          style={{ height: '600px' }}
          data={filterBy(rows, filter)}
          filterable={showHeader}
          sortable
          dataItemKey='id'
          pageable={{ pageSizes, buttonCount: 5 }}
          editField='inEdit'
          filter={filter}
          onFilterChange={(e) => setFilter(e.filter)}
          onItemChange={handleItemChange}
          headerRowHeight={showHeader ? 35 : 0}
          rowRender={rowRender}
          //   columnMenu={true}
        >
          <GridToolbar>
            {onAddRow && (
              <button className='k-button' onClick={onAddRow}>
                Add Row
              </button>
            )}
            {toolbarButtons.map(({ title, onClick, icon }) => (
              <button
                key={title}
                className={`k-button ${icon ? `k-i-${icon}` : ''}`}
                onClick={onClick}
              >
                {title}
              </button>
            ))}
          </GridToolbar>

          {columns.map(
            ({ field, title, width, cell, format, filterable = true }) => (
              <Column
                key={field}
                field={field}
                title={title}
                width={width}
                filterable={filterable}
                cell={cell}
                format={format} // forward format string
                // columnMenu={{ filterable: true }}
              />
            ),
          )}

          {onDeleteRow && (
            <Column title='Actions' cell={deleteCommand} width={100} />
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
