import { BusinessDemandElastomerColumns } from 'components/colums/ElastomerColums'
import { BusinessDemandMegColumns } from 'components/colums/MegColums'
import { BusinessDemandPetColumns } from 'components/colums/PetColums'
import { BusinessDemandPeColumns } from 'components/colums/PeColums'
import { BusinessDemandPpColumns } from 'components/colums/PpColums'
import { BusinessDemandPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: BusinessDemandPeColumns,
  [verticalEnums.PP]: BusinessDemandPpColumns,
  [verticalEnums.PTA]: BusinessDemandPtaColumns,
  [verticalEnums.ELASTOMER]: BusinessDemandElastomerColumns,
  [verticalEnums.MEG]: BusinessDemandMegColumns,
  [verticalEnums.PET]: BusinessDemandPetColumns,
}

const kendoBusinessDemColDef = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG
  const FORMATE_DECIMAL = ValueFormatterProduction()

  const cacheKey = `${lowerVertName}_${headerMap ? JSON.stringify(headerMap) : 'no_map'}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }
  const cols = VERTICAL_COLDEFS_MAP[lowerVertName] || BusinessDemandMegColumns

  const enhancedColDefs = cols.map((col) => {
    if (!headerMap || headerMap[col.title] === undefined) {
      return col
    }

    return {
      ...col,
      title: headerMap[col.title],
      align: 'right',
      format: FORMATE_DECIMAL,
    }
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default kendoBusinessDemColDef
