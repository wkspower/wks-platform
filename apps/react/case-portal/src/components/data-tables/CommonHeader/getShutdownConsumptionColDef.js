import { ShutdownConsumptionElastomerColumns } from 'components/colums/ElastomerColums'
import { ShutdownConsumptionMegColumns } from 'components/colums/MegColums'
import { ShutdownConsumptionPeColumns } from 'components/colums/PeColums'
import { ShutdownConsumptionPpColumns } from 'components/colums/PpColums'
import { ShutdownConsumptionPtaColumns } from 'components/colums/PtaColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: ShutdownConsumptionPeColumns,
  [verticalEnums.PP]: ShutdownConsumptionPpColumns,
  [verticalEnums.PTA]: ShutdownConsumptionPtaColumns,
  [verticalEnums.ELASTOMER]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.MEG]: ShutdownConsumptionMegColumns,
}

const getShutdownConsumptionColDef = ({ headerMap, shutdownMonths }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  const cacheKey = `${lowerVertName}_${JSON.stringify(headerMap)}_${shutdownMonths.join(',')}`

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
        editable: shutdownMonths.includes(monthNum),
        isDisabled: !shutdownMonths.includes(monthNum),
      }
    }

    return col
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default getShutdownConsumptionColDef
