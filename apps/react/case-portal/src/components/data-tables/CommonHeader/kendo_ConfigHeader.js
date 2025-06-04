import productionColumns from '../../../assets/kendo_config_meg.json'
import productionColumnsPE1 from '../../../assets/kendo_config_pe1.json'
import productionColumnsPE2 from '../../../assets/kendo_config_pe2.json'
import productionColumnsPE3 from '../../../assets/kendo_config_pe3.json'
import productionColumnsPE4 from '../../../assets/kendo_config_pe4.json'
import crackerColumns from '../../../assets/kendo_config_cracker_coldefs.json'
import cracker_composition from '../../../assets/kendo_config_cracker_composition.json'

const getConfigByType = (configType) => {
  switch (configType) {
    case 'meg':
      return productionColumns
    case 'StartupLosses':
      return productionColumnsPE1
    case 'Otherlosses':
      return productionColumnsPE2
    case 'ShutdownNorms':
      return productionColumnsPE3
    case 'grades':
      return productionColumnsPE4
    case 'production':
      return productionColumns
    case 'consumption':
      return productionColumns
    case 'cracker':
      return crackerColumns
    case 'cracker_composition':
      return cracker_composition
    default:
      return productionColumns
  }
}

const getEnhancedAOPColDefs = ({
  allGradesReciepes,
  headerMap,
  configType,
}) => {
  var config = []
  if (configType == 'grades') {
    config = [
      {
        field: 'ReceipeName',
        title: 'Receipe',
        width: 120,
        editable: false,
        flex: 1,
      },
    ]
    allGradesReciepes?.forEach((field) => {
      config.push({
        field: field?.id?.toUpperCase(),
        title: field?.id,
        editable: true,
        align: 'left',
        headerAlign: 'left',
        width: 120,
        isGradeHeader: 'true',
      })
    })
  } else {
    config = getConfigByType(configType)
  }

  const enhancedColDefs = config.map((col) => {
    if (headerMap && headerMap[col.title]) {
      return {
        ...col,
        title: headerMap[col.title],
        align: 'right',
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedAOPColDefs
