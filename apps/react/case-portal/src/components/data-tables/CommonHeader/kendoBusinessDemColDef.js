import { useSelector } from 'react-redux'
import vertical_meg_coldefs_bd from '../../../assets/kendo_businessdata_coldefs.json'
import vertical_pe_coldefs_bd from '../../../assets/kendo_vertical_pe_coldefs_bd.json'

const getEnhancedColDefs = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  let cols

  if (lowerVertName === 'pe') {
    cols = vertical_pe_coldefs_bd
  } else {
    cols = vertical_meg_coldefs_bd
  }

  const enhancedColDefs = cols.map((col) => {
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

export default getEnhancedColDefs
