import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { DataGrid } from '@mui/x-data-grid'
import { Box, Tooltip as MuiTooltip, TextField } from '@mui/material'

const KendoDataGridMUI = ({
  rows = [],
  columns = [],
  onRowChange,
  permissions,
  groupBy = null,
  allRedCell = [],
  showThreeColors = false,
  customModifiedCells = {},
}) => {
  const [localRows, setLocalRows] = useState(rows || [])
  const [pageSize, setPageSize] = useState(50)
  const [sortModel, setSortModel] = useState([])
  const [filterModel, setFilterModel] = useState({ items: [] })

  useEffect(() => {
    setLocalRows(rows || [])
  }, [rows])

  const ToolTipCell = ({ value }) => (
    <MuiTooltip
      title={value === undefined || value === null ? '' : String(value)}
      arrow
    >
      <span
        style={{
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          display: 'inline-block',
          maxWidth: '100%',
        }}
      >
        {value}
      </span>
    </MuiTooltip>
  )

  // Convert incoming columns into DataGrid column definitions
  const dgColumns = useMemo(() => {
    const applyFixedWidth = (columns?.length || 0) > 20

    return (columns || []).map((col) => {
      const { field, title, hidden, widthT, type, isRightAlligned, format } =
        col

      const width = applyFixedWidth || undefined
      const headerName = title || field || ''
      const base = {
        field,
        headerName,
        hide: !!hidden,
        align: isRightAlligned === 'numeric' ? 'right' : 'left',
      }

      base.width = width

      // handle date fields
      if (
        ['endDate', 'startDate', 'dateTime'].includes(field) ||
        type === 'date'
      ) {
        return {
          ...base,
          type: 'date',
          valueGetter: (params) => params.row?.[field],
          renderCell: (params) => <ToolTipCell value={params.value} />,
          editable: true,
          renderEditCell: (params) => {
            const raw = params.value
            const formatted =
              raw instanceof Date ? raw.toISOString().slice(0, 10) : raw
            return (
              <TextField
                type='date'
                defaultValue={formatted || ''}
                onChange={(e) => {
                  const newVal = e.target.value
                  const api = params.api
                  api.setEditCellValue(
                    { id: params.id, field: params.field, value: newVal },
                    e,
                  )
                }}
                fullWidth
                variant='standard'
              />
            )
          },
        }
      }

      // regular columns
      return {
        ...base,
        renderCell: (params) => {
          const { value } = params
          return <ToolTipCell value={value} />
        },
        editable: true,
      }
    })
  }, [
    columns,
    showThreeColors,
    sortModel,
    filterModel,
    customModifiedCells,
    allRedCell,
  ])

  return (
    <Box sx={{ width: '100%', height: permissions?.isHeight ? 500 : 300 }}>
      <DataGrid
        rows={localRows}
        className='custom-data-grid'
        columns={dgColumns}
        rowHeight={30}
        pageSize={pageSize}
        onPageSizeChange={(newSize) => setPageSize(newSize)}
        rowsPerPageOptions={[10, 50, 100]}
        pagination
        disableSelectionOnClick
        autoHeight={!permissions?.isHeight}
        experimentalFeatures={{ newEditingApi: true }}
      />
    </Box>
  )
}

export default KendoDataGridMUI
