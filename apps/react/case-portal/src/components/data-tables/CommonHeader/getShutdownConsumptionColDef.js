import { ShutdownConsumptionElastomerColumns } from 'components/colums/ElastomerColums'
import { ShutdownConsumptionMegColumns } from 'components/colums/MegColums'
import { ShutdownConsumptionCrackerColumns } from 'components/colums/CrackerColums'
import { ShutdownConsumptionPeColumns } from 'components/colums/PeColums'
import { ShutdownConsumptionPeColumnsPeLldpe } from 'components/colums/PeColums'
import { ShutdownConsumptionPpColumns } from 'components/colums/PpColums'
import { ShutdownConsumptionPtaColumns } from 'components/colums/PtaColums'
import { ShutdownConsumptionVcmColumns } from 'components/colums/VcmColums'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: ShutdownConsumptionPeColumns,
  [verticalEnums.PP]: ShutdownConsumptionPpColumns,
  [verticalEnums.PTA]: ShutdownConsumptionPtaColumns,
  [verticalEnums.ELASTOMER]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.AROMATICS]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.VCM]: ShutdownConsumptionVcmColumns,
  //[verticalEnums.PTA]: ShutdownConsumptionElastomerColumns,
  [verticalEnums.MEG]: ShutdownConsumptionMegColumns,
  [verticalEnums.CRACKER]: ShutdownConsumptionCrackerColumns,
}

const getShutdownConsumptionColDef = ({
  headerMap,
  shutdownMonths,
  valueFormat,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const vertName = dataGridStore.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || verticalEnums.MEG

  const {
    verticalChange,
    screenTitle,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const SITE_NAME_LOWERCASE = siteObject?.name?.toLowerCase()
  const PLANT_NAME_LOWERCASE = plantObject?.name?.toLowerCase()

  const IS_PE_PP_VERTICAL_NMD_LLDPE =
    ['pe'].includes(lowerVertName) &&
    ['nmd'].includes(SITE_NAME_LOWERCASE) &&
    ['lldpe1', 'lldpe2'].includes(PLANT_NAME_LOWERCASE)

  let safeShutdownMonths = Array.isArray(shutdownMonths) ? shutdownMonths : []

  const cacheKey = `${lowerVertName}_${JSON.stringify(headerMap)}_${safeShutdownMonths.join(',')}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }

  let cols = []
  if (IS_PE_PP_VERTICAL_NMD_LLDPE) {
    cols = ShutdownConsumptionPeColumnsPeLldpe
  } else {
    cols = VERTICAL_COLDEFS_MAP[lowerVertName] || []
  }
  // const isPEorPP = ['pe', 'pp'].includes(lowerVertName)

  const enhancedColDefs = cols.map((col) => {
    if (col.monthNumber) {
      const monthNum = col.monthNumber
      const isPEorPP = false

      return {
        ...col,
        headerName: headerMap?.[monthNum] || col.field,
        editable: safeShutdownMonths.includes(monthNum),
        // isDisabled: !safeShutdownMonths.includes(monthNum),
        isDisabled: !safeShutdownMonths.includes(monthNum),
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
