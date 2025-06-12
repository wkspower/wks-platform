import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/kendo_production_aop_meg.json' // Adjust path as needed
import productionColDefsPE from '../../../assets/kendo_production_aop_pe.json' // Adjust path as needed
import productionColDefsCracker from '../../../assets/kendo_production_aop_cracker.json'

const monthFields = [
  'april',
  'may',
  'june',
  'july',
  'aug',
  'sep',
  'oct',
  'nov',
  'dec',
  'jan',
  'feb',
  'march',
]

const getEnhancedColDefs = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  let cols

  if (lowerVertName == 'pe') {
    cols = productionColDefsPE
  } else if (lowerVertName === 'cracker') {
    cols = productionColDefsCracker
  } else {
    cols = productionColDefs
  }


  const hasTotal = cols.some((col) => col.field === 'averageTPH')
  if (!hasTotal) {
    cols.push({
      field: 'averageTPH',
      title: 'Total',
      editable: false,
      format: '{0:n2}',
      align: 'right',
    })
  }

  const enhancedColDefs = cols.map((col) => {
    let updatedCol = { ...col }

    if (headerMap && headerMap[col.title] !== undefined) {
      updatedCol.title = headerMap[col.title]
    }


    if (
      updatedCol.format ||
      col.align === 'right' ||
      monthFields.includes(col.field)
    ) {
      updatedCol.align = 'right'
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
