import React, { useMemo } from 'react'
import AdvanceKendoTable from '../AdvanceKendoTable/index'
import {
  DynamicRowCellEditor,
  DynamicRowDisplayCell,
} from '../utilities/DynamicRowCellEditor'
import { useSelector } from 'react-redux'
import { Checkbox } from '@mui/material'

const RowBasedKendoTable = (props) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalObject } = dataGridStore
  
  const isFilamentOrStaple = useMemo(() => 
    ['filament (pfy)', 'staple (psf)'].includes(verticalObject?.name?.toLowerCase()), 
    [verticalObject]
  )

  const { columns, rows, ...restProps } = props

  const getDecimalPlacesFromFormat = (format) => {
    if (!format) return 2
    const match = format.match(/\{0:0\.(0+)\}/)
    return match ? match[1].length : 2
  }

  const enhancedColumns = useMemo(() => {
    const enhanceColumn = (col) => {
      let enhanced = { ...col }
      
      if (enhanced.children && Array.isArray(enhanced.children)) {
        enhanced.children = enhanced.children.map(enhanceColumn)
      }

      if (enhanced.type === 'row-based' || enhanced.type === 'conditional') {
        const renderDataCell = (cellProps, inEditPhase = false) => {
          const { dataItem, field, onChange } = cellProps
          const rowId = dataItem.id
          const customModifiedCells = props.externalCustomModifiedCells || {}

          let colSpan = cellProps.colSpan;
          if (props.dynamicColumnMerger) {
            const config = props.dynamicColumnMerger(dataItem, field);
            if (config?.hidden) return null;
            if (config?.colSpan) colSpan = config.colSpan;
          }

          const isEdited = Object.prototype.hasOwnProperty.call(
            customModifiedCells?.[rowId] || {},
            field,
          )

          const value = dataItem?.[field]
          const inputType = dataItem?.type

          let displayValue = value

          if (inputType === 'boolean' || inputType === 'yesno' || inputType === 'checkbox') {
            displayValue = typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
          } else if (inputType === 'date' && value instanceof Date) {
            const year = value.getFullYear()
            const month = String(value.getMonth() + 1).padStart(2, '0')
            const day = String(value.getDate()).padStart(2, '0')
            displayValue = `${year}-${month}-${day}`
          } else if (inputType === 'datetime' && value instanceof Date) {
            const year = value.getFullYear()
            const month = String(value.getMonth() + 1).padStart(2, '0')
            const day = String(value.getDate()).padStart(2, '0')
            const hours = String(value.getHours()).padStart(2, '0')
            const minutes = String(value.getMinutes()).padStart(2, '0')
            displayValue = `${year}-${month}-${day} ${hours}:${minutes}`
          } else if (!isNaN(value) && value !== null && value !== '') {
            const decimals = getDecimalPlacesFromFormat(enhanced.format)
            displayValue = Number(value).toFixed(decimals)
          }

          const tdStyle = {
            color: isEdited && !inEditPhase ? 'orange' : inEditPhase ? '#999' : undefined,
            fontWeight: isEdited ? 'bold' : undefined,
            ...(inEditPhase ? {
              backgroundColor: '#f5f5f5',
              cursor: 'not-allowed',
            } : {}),
          }

          if (inputType === 'checkbox') {
            const isCellDisabled = dataItem?.isEditable === false;
            return (
              <td
                {...cellProps.tdProps}
                colSpan={colSpan || cellProps.tdProps?.colSpan}
                title={String(displayValue ?? '')}
                style={{ ...tdStyle, textAlign: 'center', padding: '6px 2px' }}
                className='k-checkbox-center'
              >
                {dataItem?.hideCheckbox ? null : (
                <div style={{ pointerEvents: 'none', display: 'inline-block', textAlign: 'center' }}>
                  <Checkbox
                    checked={!!value}
                    disabled={isCellDisabled}
                    size="medium"
                    style={{ padding: '0px' }}
                  />
                </div>
                )}
              </td>
            )
          }

          return (
            <td
              {...cellProps.tdProps}
              colSpan={colSpan || cellProps.tdProps?.colSpan}
              title={String(displayValue ?? '')}
              style={tdStyle}
            >
              {displayValue ?? ''}
            </td>
          )
        }

        return {
          ...enhanced,
          type: 'row-based',
          cells: {
            edit: {
              text: (cellProps) => {
                let currentCellProps = cellProps;
                const { dataItem, field } = currentCellProps;
                
                if (props.dynamicColumnMerger) {
                  const config = props.dynamicColumnMerger(dataItem, field);
                  if (config?.hidden) return null;
                  if (config?.colSpan) {
                    currentCellProps = { ...currentCellProps, tdProps: { ...currentCellProps.tdProps, colSpan: config.colSpan } };
                  }
                }

                const isColEditable = enhanced.editable !== false;
                const isRowEditable = dataItem.isEditable !== false;
                
                let isCellEditable = true;
                if (dataItem?.nonEditableFields?.includes(field)) isCellEditable = false;
                if (dataItem?.editableFields && !dataItem?.editableFields?.includes(field)) isCellEditable = false;
                
                if (!isColEditable || !isRowEditable || !isCellEditable) {
                  return renderDataCell(currentCellProps, true);
                }
                
                return <DynamicRowCellEditor {...currentCellProps} />
              }
            },
            data: (cellProps) => renderDataCell(cellProps, false),
          },
        }
      }
      return enhanced
    }

    return columns.map(enhanceColumn)
  }, [columns, props.externalCustomModifiedCells])

  return (
    <AdvanceKendoTable {...restProps} columns={enhancedColumns} rows={rows} />
  )
}

export default RowBasedKendoTable
