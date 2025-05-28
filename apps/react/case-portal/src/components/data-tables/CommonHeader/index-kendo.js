import { useSelector } from 'react-redux'
import vertical_meg_coldefs_bd from '../../../assets/vertical_meg_coldefs_bd-kendo.json'
import vertical_pe_coldefs_bd from '../../../assets/vertical_pe_coldefs_bd.json'
import NumericInputOnly from 'utils/NumericInputOnly'
import NormParameterCell from 'utils/NormParameterCell'

const getEnhancedColDefs = ({ allProducts, headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const enhancedColDefs = (
    lowerVertName === 'meg' ? vertical_meg_coldefs_bd : vertical_pe_coldefs_bd
  ).map((colDef) => {
    const field = colDef.field
    const headerName = headerMap[colDef.headerName] || colDef.headerName
    const isTextCol = field === 'norm' || field === 'particulars'

    let cell = undefined
    if (field === 'normParameterId') {
      cell = (cellProps) => (
        <NormParameterCell {...cellProps} allProducts={allProducts} />
      )
    }

    return {
      field,
      title: headerName,
      width: 200,
      filterable: true,
      filter: 'numeric',
      format: isTextCol ? undefined : '{0:n3}',
      editable: !isTextCol,
      status: 'inactive',
      editor: !isTextCol
        ? (props) => <NumericInputOnly {...props} />
        : undefined,
      cell, // <----- use 'cell' prop here, not 'cells'
    }
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
