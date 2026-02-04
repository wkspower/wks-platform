import { NormalOpNormElastomerColumns } from 'components/colums/ElastomerColums'
import { NormalOpNormMegColumns } from 'components/colums/MegColums'
import { NormalOpNormVcmColumns } from 'components/colums/VcmColumns'
import { CrackerColums } from 'components/colums/CrackerColums'
import { NormalOpNormPeColumns } from 'components/colums/PeColums'
import { NormalOpNormPpColumns } from 'components/colums/PpColums'
import { NormalOpNormPtaColumns } from 'components/colums/PtaColums'
import { NormalOpNormVcmDmdColumns } from 'components/colums/VcmDmdColumns'
import { verticalEnums } from 'enums/verticalEnums'
import { useSelector } from 'react-redux'

const colDefsCache = new Map()

const VERTICAL_COLDEFS_MAP = {
  [verticalEnums.PE]: NormalOpNormPeColumns,
  [verticalEnums.PP]: NormalOpNormPpColumns,
  [verticalEnums.PTA]: NormalOpNormPtaColumns,
  [verticalEnums.ELASTOMER]: NormalOpNormElastomerColumns,
  [verticalEnums.MEG]: NormalOpNormMegColumns,
  [verticalEnums.CRACKER]: CrackerColums,
  [verticalEnums.VCM]: NormalOpNormVcmColumns,
}

const getNormalOpNormColDef = ({
  headerMap,
  valueFormat,
  lowerVertName,
  lowerSiteName,
  lowerPlantName,
}) => {
  const cacheKey = `${lowerVertName}_${lowerSiteName}_${lowerPlantName}_${headerMap ? JSON.stringify(headerMap) : 'no_map'}`

  if (colDefsCache.has(cacheKey)) {
    return colDefsCache.get(cacheKey)
  }

  const cols =
    lowerVertName === 'vcm' &&
    lowerSiteName === 'dmd' &&
    lowerPlantName === 'vcm'
      ? NormalOpNormVcmDmdColumns
      : VERTICAL_COLDEFS_MAP[lowerVertName] || NormalOpNormMegColumns

  const enhancedColDefs = cols.map((col) => {
    if (!headerMap || headerMap[col.title] === undefined) {
      return valueFormat ? { ...col, format: valueFormat } : col
    }
    return {
      ...col,
      title: headerMap[col.title],
      align: 'right',
      format: valueFormat || '{0:#.###}',
    }
  })

  colDefsCache.set(cacheKey, enhancedColDefs)
  return enhancedColDefs
}

export const clearColDefsCache = () => colDefsCache.clear()

export default getNormalOpNormColDef
