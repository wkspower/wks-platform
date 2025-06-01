import production_coldefs_pe from '../../../assets/kendo_production_coldefs_pe.json'
import production_coldefs_meg from '../../../assets/kendo_production_coldefs_meg.json'
import { useSelector } from 'react-redux'

const getEnhancedProductionColDefs = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const baseCols =
    lowerVertName === 'pe' ? production_coldefs_pe : production_coldefs_meg

  const enhancedColDefs = baseCols.map((col) => {
    let updatedCol = { ...col }

    if (headerMap && headerMap[col.title] !== undefined) {
      updatedCol = {
        ...updatedCol,
        title: headerMap[col.title],
        align: 'right',
      }
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedProductionColDefs
