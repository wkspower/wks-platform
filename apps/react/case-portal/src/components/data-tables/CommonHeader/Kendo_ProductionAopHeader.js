import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/kendo_production_aop_meg.json'
import productionColDefsPE from '../../../assets/kendo_production_aop_pe.json'
import productionColDefsCracker from '../../../assets/kendo_production_aop_cracker.json'
import productionColDefsvcmpta from '../../../assets/kendo_production_aop_vcmpta.json'
import productionColDefsElastomerJmd from '../../../assets/kendo_production_aop_Elastomer.json'
import productionColDefsAromaticDTAPlat from '../../../assets/kendo_production_aop_AromaticsDTAPlat.json'
import { shouldLockColumn } from 'utils/columnLockUtils'
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

const getEnhancedColDefs = ({ headerMap, valueFormat }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, siteObject, plantObject } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const lowerSiteName = siteObject?.name.toLowerCase()
  const lowerPlantName = plantObject?.name.toLowerCase()
  const IS_PTA_PMD_PIA =
    lowerVertName === 'pta' &&
    lowerSiteName === 'pmd' &&
    lowerPlantName === 'pia'
  const IS_AROMATIC_DTA_PLATFORMER =
    lowerVertName === 'aromatics' &&
    lowerSiteName === 'dta' &&
    (lowerPlantName === 'platformer' || lowerPlantName === 'plat')
  let cols

  if (lowerVertName == 'pe' || lowerVertName == 'pp') {
    cols = productionColDefsPE
  } else if (lowerVertName === 'cracker') {
    cols = productionColDefsCracker
  } else if (
    (lowerVertName === 'chemical' &&
      lowerSiteName === 'vmd' &&
      lowerPlantName === 'acrylonitrile') ||
    IS_PTA_PMD_PIA
  ) {
    cols = productionColDefsElastomerJmd
  } else if (IS_AROMATIC_DTA_PLATFORMER) {
    cols = productionColDefsAromaticDTAPlat
  } else if (lowerVertName === 'pta' || lowerVertName === 'vcm') {
    cols = productionColDefsvcmpta
  } else if (lowerVertName === 'elastomer' && lowerSiteName === 'jmd') {
    cols = productionColDefsElastomerJmd
  } else {
    cols = productionColDefs
  }

  const hasTotal = cols.some((col) => col.field === 'averageTPH')

  if (!hasTotal) {
    cols.push({
      field: 'averageTPH',
      title: 'Total',
      minWidth: 100,
    })
  }

  const enhancedColDefs = cols.map((col) => {
    let updatedCol = { ...col }

    if (headerMap && headerMap[col.title] !== undefined) {
      updatedCol.title = headerMap[col.title]
    }
    if (col.type === 'number' && valueFormat) {
      updatedCol.format = valueFormat
    }
    if (shouldLockColumn(col)) {
      updatedCol.locked = true
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
