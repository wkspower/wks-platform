import productionColumns from '../../../assets/config_meg.json'
import productionColumnsPE1 from '../../../assets/config_pe1.json'
import productionColumnsPE2 from '../../../assets/config_pe2.json'
import productionColumnsPE3 from '../../../assets/config_pe3.json'
import productionColumnsPE4 from '../../../assets/config_pe4.json'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
// import { allProducts } from 'data/allProducts'; // adjust path as needed

// This function returns the appropriate JSON configuration based on the configType
const getConfigByType = (configType) => {
  switch (configType) {
    case 'meg':
      return productionColumns
    case 'startupLosses':
      return productionColumnsPE1
    case 'otherLosses':
      return productionColumnsPE2
    case 'shutdownNorms':
      return productionColumnsPE3
    case 'grades':
      return productionColumnsPE4
    default:
      return productionColumns
  }
}

const getEnhancedAOPColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
  configType,
  columnConfig,
}) => {
  const config = getConfigByType(configType)

  // const finalColumns = configType === 'grades' ? columnConfig : config

  const enhancedColDefs = config.map((col) => {
    if (col.field === 'normParameterFKId') {
      return {
        ...col,
        valueGetter: (params) => params || '',
        valueFormatter: (params) => {
          const product = allProducts.find((p) => p.id === params)
          return product ? product.displayName : ''
        },
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

    // For remarks field – attach custom cell renderer for remarks
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
                  maxWidth: 140,
                }}
                onClick={() => handleRemarkCellClick(params.row)}
              >
                {displayText || (isEditable ? 'Click to add remark' : '')}
              </div>
            </Tooltip>
          )
        },
      }
    }

    // If headerMap is provided and headerName exists in headerMap, update headerName and attach NumericInputOnly as edit cell renderer
    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        headerName: headerMap[col.headerName],
      }
    }

    // For Particulars field, render the text as bold
    if (col.field === 'Particulars') {
      return {
        ...col,
        renderCell: (params) => <strong>{params?.value}</strong>,
      }
    }
    if (col.field === 'Particulars2') {
      return {
        ...col,
        renderCell: (params) => <strong>{params?.value}</strong>,
      }
    }
    // if (col.field === 'gradeName' || col.field === 'apr') {
    //   return {
    //     ...col,
    //     renderCell: (params) => {
    //       params?.value
    //     },
    //     renderEditCell: NumericInputOnly,
    //   }
    // }
    // if (col.field === 'attributeValue') {
    //   return {
    //     ...col,
    //     // renderCell: (params) => {
    //     //   params?.value
    //     // },
    //     // renderEditCell: NumericInputOnly,
    //   }
    // }
    // if (col.field === 'receipeName') {
    //   return {
    //     ...col,
    //     renderCell: (params) => {
    //       params?.value
    //     },
    //     renderEditCell: NumericInputOnly,
    //   }
    // }
    // if (
    //   col.field != 'receipeName' ||
    //   col.field != 'attributeValue' ||
    //   col.field != 'gradeName'
    //   //  col.field != 'gradeName'
    // ) {
    //   return {
    //     ...col,
    //     renderCell: (params) => {
    //       params?.value
    //     },
    //     renderEditCell: NumericInputOnly,
    //   }
    // }
    // For other fields, if configType is 'grades' and the field is a dynamic grade column:
    // if (configType === 'grades' && col.field !== 'receipeName') {
    //   return {
    //     ...col,
    //     renderCell: (params) => {
    //       // Access the grade information from the nested 'grades' object
    //       const gradeData = params.row.grades && params.row.grades[col.field]
    //       return gradeData ? gradeData.attributeValue : ''
    //     },
    //     renderEditCell: (params) => {
    //       // Use NumericInputOnly for editing, but ensure you update the nested attribute.
    //       // You might need to create a custom NumericInputOnly that works with nested paths.
    //       return NumericInputOnly(params)
    //     },
    //   }
    // }

    // For receipeName and any other static columns:
    // if (col.field === 'receipeName') {
    //   return {
    //     ...col,
    //     renderCell: (params) => params?.value,
    //   }
    // }
    // if (
    //   col.field != 'receipeName' ||
    //   col.field != 'attributeValue' ||
    //   col.field != 'gradeName'
    //   //  col.field != 'gradeName'
    // ) {
    //   return {
    //     ...col,
    //     renderCell: (params) => {
    //       params?.value
    //     },
    //     renderEditCell: NumericInputOnly,
    //   }
    // }
    // Default return for any fields not matched above.
    //   return {
    //     ...col,
    //     renderCell: (params) => params?.value,
    //     renderEditCell: NumericInputOnly,
    //   }
    // })

    // Otherwise, return the column as is
    return col
  })
  return enhancedColDefs
}

export default getEnhancedAOPColDefs
