import { ConsumptionAopElastomerColumns } from 'components/colums/ElastomerColums'
import { ConsumptionAopMegColumns } from 'components/colums/MegColums'
import { ConsumptionAopPeColumns } from 'components/colums/PeColums'
import { ConsumptionAopPpColumns } from 'components/colums/PpColums'
import { ConsumptionAopPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: ConsumptionAopPeColumns,
  [verticalEnums.PP]: ConsumptionAopPpColumns,
  [verticalEnums.PTA]: ConsumptionAopPtaColumns,
  [verticalEnums.ELASTOMER]: ConsumptionAopElastomerColumns,
  [verticalEnums.MEG]: ConsumptionAopMegColumns,
}

const getCunsumptionAopColDef = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  const cacheKey = `${lowerVertName}_${headerMap ? JSON.stringify(headerMap) : 'no_map'}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }
  const cols = VERTICAL_COLDEFS_MAP[lowerVertName] || ConsumptionAopMegColumns

  const enhancedColDefs = cols.map((col) => {
    if (!headerMap || headerMap[col.title] === undefined) {
      return col
    }

    return {
      ...col,
      title: headerMap[col.title],
      align: 'right',
      format: '{0:#.###}',
    }
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default getCunsumptionAopColDef
