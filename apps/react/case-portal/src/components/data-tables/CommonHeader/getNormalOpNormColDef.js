import { NormalOpNormElastomerColumns } from 'components/colums/ElastomerColums'
import { NormalOpNormMegColumns } from 'components/colums/MegColums'
import { NormalOpNormPeColumns } from 'components/colums/PeColums'
import { NormalOpNormPpColumns } from 'components/colums/PpColums'
import { NormalOpNormPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: NormalOpNormPeColumns,
  [verticalEnums.PP]: NormalOpNormPpColumns,
  [verticalEnums.PTA]: NormalOpNormPtaColumns,
  [verticalEnums.ELASTOMER]: NormalOpNormElastomerColumns,
  [verticalEnums.MEG]: NormalOpNormMegColumns,
}

const getNormalOpNormColDef = ({ headerMap }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  const cacheKey = `${lowerVertName}_${headerMap ? JSON.stringify(headerMap) : 'no_map'}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }
  const cols = VERTICAL_COLDEFS_MAP[lowerVertName] || NormalOpNormMegColumns

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

export default getNormalOpNormColDef
