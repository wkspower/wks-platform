import productionColumns from '../../../assets/config_meg.json'
import productionColumnsPE1 from '../../../assets/config_pe1.json'
import productionColumnsPE2 from '../../../assets/config_pe2.json'
import productionColumnsPE3 from '../../../assets/config_pe3.json'
import productionColumnsPE4 from '../../../assets/config_pe4.json'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import TextField from '@mui/material/TextField'
// import { allProducts } from 'data/allProducts'; // adjust path as needed

// This function returns the appropriate JSON configuration based on the configType
const getConfigByType = (configType) => {
  switch (configType) {
    case 'meg':
      return productionColumns
    case 'StartupLosses':
      return productionColumnsPE1
    case 'Otherlosses':
      return productionColumnsPE2
    case 'ShutdownNorms':
      return productionColumnsPE3
    case 'grades':
      return productionColumnsPE4
    case 'production':
      return productionColumns
    case 'consumption':
      return productionColumns
    default:
      return productionColumns
  }
}

const formatValueToThreeDecimals = (params) => {
  return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
}

const getEnhancedAOPColDefs = ({
  allGradesReciepes,
  allProducts,
  headerMap,
  handleRemarkCellClick,
  configType,
  columnConfig,
}) => {
  var config = []
  if (configType == 'grades') {
    config = [
      {
        field: 'ReceipeName',
        headerName: 'Receipe',
        minWidth: 120,
        editable: false,
        flex: 1,
      },
    ]
    allGradesReciepes?.forEach((field) => {
      config.push({
        field: field?.id?.toUpperCase(),
        headerName: field?.id,
        editable: true,
        align: 'left',
        headerAlign: 'left',
        minWidth: 120,
        isGradeHeader: 'true',
      })
    })
  } else {
    config = getConfigByType(configType)
  }

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const enhancedColDefs = config.map((col) => {
    if (col.field === 'normParameterFKId') {
      return {
        ...col,
        valueGetter: (params) => params || '',
        valueFormatter: (params) => {
          const product = allProducts.find((p) => p.id === params)
          return product ? product.displayName : ''
        },

        filterOperators: [
          {
            label: 'contains',
            value: 'contains',
            getApplyFilterFn: (filterItem) => {
              if (!filterItem?.value) {
                return
              }
              return (rowId) => {
                const filterValue = filterItem.value.toLowerCase()
                if (filterValue) {
                  const productName = getProductDisplayName(rowId)
                  if (productName) {
                    return productName.toLowerCase().includes(filterValue)
                  }
                }
                return true
              }
            },
            InputComponent: ({ item, applyValue, focusElementRef }) => (
              <TextField
                autoFocus
                inputRef={focusElementRef}
                size='small'
                label='Value'
                value={item.value || ''}
                onChange={(event) =>
                  applyValue({ ...item, value: event.target.value })
                }
                style={{ marginTop: '8px' }}
              />
            ),
          },
        ],

        renderEditCell: (params) => {
          const { value, id, api } = params
          const existingValues = new Set(
            [...api.getRowModels().values()]
              .filter((row) => row.id !== id)
              .map((row) => row.normParameterFKId),
          )
          return (
            <select
              value={value || ''}
              onChange={(event) => {
                api.setEditCellValue({
                  id,
                  field: 'normParameterFKId',
                  value: event.target.value,
                })
              }}
              style={{
                width: '100%',
                padding: '5px',
                border: 'none',
                outline: 'none',
                background: 'transparent',
              }}
            >
              <option value='' disabled>
                Select
              </option>
              {allProducts
                .filter((product) => !existingValues.has(product.id))
                .map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.displayName}
                  </option>
                ))}
            </select>
          )
        },
      }
    }

    if (col.field === 'remarks') {
      return {
        ...col,
        renderCell: (params) => {
          const displayText = truncateRemarks(params?.value)
          // For PE2 configuration, we might have two fields (Particulars, Particulars2) to check
          const isEditable =
            !params.row.Particulars &&
            (!params?.row.Particulars2 ||
              params?.row.isSubGroupHeader === false)
          return (
            <Tooltip title={params?.value || ''} arrow>
              <div
                style={{
                  cursor: 'pointer',
                  color: params.value ? 'inherit' : 'gray',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  width: ' 100%',
                }}
                onDoubleClick={() => handleRemarkCellClick(params.row)}
              >
                {displayText || (isEditable ? 'Click to add remark' : '')}
              </div>
            </Tooltip>
          )
        },
      }
    }

    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        headerName: headerMap[col.headerName],
        valueFormatter: formatValueToThreeDecimals,

        align: 'right',
        renderCell: (params) => (
          <Tooltip
            title={params.value != null ? params.value.toString() : ''}
            arrow
          >
            <span>{formatValueToThreeDecimals(params.value)}</span>
          </Tooltip>
        ),
      }
    }

    if (col.field == 'apr') {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        valueFormatter: formatValueToThreeDecimals,
      }
    }

    // if (col.field === 'srNo') {
    //   return {
    //     ...col,
    //     renderCell: (params) => params.id + 1,
    //   }
    // }

    if (col.field === 'Particulars' || col.field === 'Particulars2') {
      return {
        ...col,
        renderCell: (params) => {
          const fullValue = params.value || ''
          const maxLength = 25
          const isTruncated = fullValue.length > maxLength
          const displayValue = isTruncated
            ? `${fullValue.slice(0, maxLength)}...`
            : fullValue

          return (
            <div
              style={{
                whiteSpace: 'normal',
                wordBreak: 'break-word',
                lineHeight: 1.4,
              }}
              title={fullValue}
            >
              <strong>{displayValue}</strong>
            </div>
          )
        },
        filterable: false,
      }
    }

    if (col.isGradeHeader === 'true') {
      const matchedGrade = allGradesReciepes?.find(
        (item) => item?.id?.toLowerCase() === col?.field?.toLowerCase(),
      )

      return {
        ...col,
        headerName: matchedGrade?.displayName ?? col?.headerName,
        renderEditCell: NumericInputOnly,
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedAOPColDefs
