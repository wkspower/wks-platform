import { ShutdownConsumptionElastomerColumns } from 'components/colums/ElastomerColums'
import { ShutdownConsumptionMegColumns } from 'components/colums/MegColums'
import { ShutdownConsumptionCrackerColumns } from 'components/colums/CrackerColums'
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
  [verticalEnums.AROMATICS]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.VCM]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.PTA]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.MEG]: ShutdownConsumptionMegColumns,
  [verticalEnums.CRACKER]: ShutdownConsumptionCrackerColumns,
}

const getShutdownConsumptionColDef = ({ headerMap, shutdownMonths, valueFormat }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  let safeShutdownMonths = Array.isArray(shutdownMonths) ? shutdownMonths : []

  const cacheKey = `${lowerVertName}_${JSON.stringify(headerMap)}_${safeShutdownMonths.join(',')}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }

  const cols = VERTICAL_COLDEFS_MAP[lowerVertName] || []

  const enhancedColDefs = cols.map((col) => {
    if (col.monthNumber) {
      const monthNum = col.monthNumber
      const isPEorPP = ['pe', 'pp'].includes(lowerVertName)

      return {
        ...col,
        headerName: headerMap?.[monthNum] || col.field,
        editable: isPEorPP ? false : safeShutdownMonths.includes(monthNum),
        ...(!isPEorPP && {
          isDisabled: !safeShutdownMonths.includes(monthNum),
        }),
        ...(isPEorPP && {
          isBold: safeShutdownMonths.includes(monthNum),
        }),
        format: valueFormat,
      }
    }

    return valueFormat ? { ...col, format: valueFormat } : col
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default getShutdownConsumptionColDef
