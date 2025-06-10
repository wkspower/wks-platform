import production_coldefs_pe from '../../../assets/kendo_production_coldefs_pe.json'
import production_coldefs_meg from '../../../assets/kendo_production_coldefs_meg.json'
import { useSelector } from 'react-redux'
import { MY_NUM_COL } from 'components/kendo-data-tables/Utilities-Kendo/MyNumColdefs'
import NumericInputOnly from 'utils/NumericInputOnly'

const getEnhancedProductionColDefs = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const baseCols =
    lowerVertName === 'pe' ? production_coldefs_pe : production_coldefs_meg

  const enhancedColDefs = baseCols.map((col) => {
    let updatedCol = { ...col }
    const isNum = MY_NUM_COL.includes(col.field)

    if (headerMap && headerMap[col.title] !== undefined) {
      updatedCol = {
        ...updatedCol,
        title: headerMap[col.title],
        align: 'right',
        format: isNum ? '{0:n3}' : undefined,
        type: isNum ? 'number' : undefined,
        editor: !isNum ? (props) => <NumericInputOnly {...props} /> : undefined,
        ...(isNum ? { format: '{0:n3}' } : {}),
      }
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedProductionColDefs
