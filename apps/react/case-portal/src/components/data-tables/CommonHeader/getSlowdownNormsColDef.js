import { SlowdownNormsElastomerColumns } from 'components/colums/ElastomerColums'
import { SlowdownNormsMegColumns } from 'components/colums/MegColums'
import { SlowdownNormsPeColumns } from 'components/colums/PeColums'
import { SlowdownNormsPpColumns } from 'components/colums/PpColums'
import { SlowdownNormsPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: SlowdownNormsPeColumns,
  [verticalEnums.PP]: SlowdownNormsPpColumns,
  [verticalEnums.PTA]: SlowdownNormsPtaColumns,
  [verticalEnums.ELASTOMER]: SlowdownNormsElastomerColumns,
  [verticalEnums.MEG]: SlowdownNormsMegColumns,
}

const getSlowdownNormsColDef = ({ headerMap, slowdownMonths }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  const cacheKey = `${lowerVertName}_${JSON.stringify(headerMap)}_${slowdownMonths.join(',')}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }

  const cols = VERTICAL_COLDEFS_MAP[lowerVertName] || []

  const enhancedColDefs = cols.map((col) => {
    if (col.monthNumber) {
      const monthNum = col.monthNumber
      return {
        ...col,
        headerName: headerMap?.[monthNum] || col.field,
        editable: slowdownMonths.includes(monthNum),
        isDisabled: !slowdownMonths.includes(monthNum),
      }
    }

    return col
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default getSlowdownNormsColDef
