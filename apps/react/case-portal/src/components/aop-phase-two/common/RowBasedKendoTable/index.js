import React, { useMemo } from 'react'
import AdvanceKendoTable from '../AdvanceKendoTable/index'
import {
  DynamicRowCellEditor,
  DynamicRowDisplayCell,
} from '../utilities/DynamicRowCellEditor'

const RowBasedKendoTable = (props) => {
  const { columns, rows, ...restProps } = props

  const enhancedColumns = useMemo(() => {
    return columns.map((col) => {
      if (col.type === 'row-based' || col.type === 'conditional') {
        return {
          ...col,
          type: 'row-based',
          cells: {
            edit: { text: DynamicRowCellEditor },
            data: (cellProps) => {
              const { dataItem, field } = cellProps
              const rowId = dataItem.id
              const customModifiedCells =
                props.externalCustomModifiedCells || {}

              const isEdited = Object.prototype.hasOwnProperty.call(
                customModifiedCells?.[rowId] || {},
                field,
              )

              const value = dataItem?.[field]
              const inputType = dataItem?.inputType

              let displayValue = value

              if (inputType === 'boolean' || inputType === 'yesno') {
                displayValue =
                  typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
              } else if (inputType === 'date' && value instanceof Date) {
                displayValue = value.toLocaleDateString('en-GB', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                })
              } else if (inputType === 'datetime' && value instanceof Date) {
                displayValue = value.toLocaleString('en-GB', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                  hour12: true,
                })
              } else {
                // Format numbers with 3 decimal places
                if (!isNaN(value) && value !== null && value !== '') {
                  displayValue = Number(value).toFixed(3)
                }
              }

              return (
                <td
                  {...cellProps.tdProps}
                  title={String(displayValue ?? '')}
                  style={{
                    color: isEdited ? 'orange' : undefined,
                    fontWeight: isEdited ? 'bold' : undefined,
                  }}
                >
                  {displayValue ?? ''}
                </td>
              )
            },
          },
        }
      }
      return col
    })
  }, [columns, props.externalCustomModifiedCells])
  console.log('enhancedColumns', enhancedColumns)

  return (
    <AdvanceKendoTable {...restProps} columns={enhancedColumns} rows={rows} />
  )
}

export default RowBasedKendoTable
