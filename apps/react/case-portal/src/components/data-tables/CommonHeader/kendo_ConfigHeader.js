import contineGradeChange from '../../../assets/kendo_config_contineGradeChange.json'
import crackerColumns from '../../../assets/kendo_config_cracker_coldefs.json'
import cracker_composition from '../../../assets/kendo_config_cracker_composition.json'
import cracker_constants from '../../../assets/kendo_config_cracker_constants_coldefs.json'
import cracker_yield from '../../../assets/kendo_config_cracker_yield_coldefs.json'
import cracker_yield_dmd from '../../../assets/kendo_config_cracker_yield_dmd_coldefs.json'
import disContineGradeChange from '../../../assets/kendo_config_disContineGradeChange.json'
import productionColumnsConstants from '../../../assets/kendo_config_meg constants.json'
import productionColumns from '../../../assets/kendo_config_meg.json'
import productionColumnsPE1 from '../../../assets/kendo_config_pe1.json'
import productionColumnsPE2 from '../../../assets/kendo_config_pe2.json'
import colDefsShutdownRate from '../../../assets/kendo_config_pe3.json'
import productionColumnsPE5 from '../../../assets/kendo_config_pe5.json'
import pioImpactColumns from '../../../assets/kendo_config_pio_impact.json'

import reportManualEntry from '../../../assets/kendo_config_report_mannual_entry.json'

const getConfigByType = (configType) => {
  switch (configType) {
    case 'meg':
      return productionColumns
    case 'megConstantsMannualEntry':
      return productionColumns
    case 'megConstants':
      return productionColumnsConstants
    case 'PIO Impact':
      return pioImpactColumns
    case 'shutdownData':
      return pioImpactColumns
    case 'StartupLosses':
      return productionColumnsPE1
    case 'Configuration':
      return productionColumnsPE1
    case 'Otherlosses':
      return productionColumnsPE2

    //NEW BUILD 17 NOV

    case 'Constant':
      return colDefsShutdownRate

    case 'ShutdownNorms':
      return colDefsShutdownRate
    case 'Constants':
      return productionColumnsPE5
    case 'production':
      return productionColumns
    case 'consumption':
      return productionColumns
    case 'cracker_configuration':
      return productionColumns
    case 'cracker_composition':
      return cracker_composition
    case 'cracker':
      return crackerColumns
    case 'cracker_constants':
      return cracker_constants
    case 'cracker_yield':
      return cracker_yield
    case 'cracker_yield_dmd':
      return cracker_yield_dmd
    case 'ContineGradeChange':
      return contineGradeChange
    case 'DisContineGradeChange':
      return disContineGradeChange

    case 'Report Manual Entry':
      return reportManualEntry

    default:
      return productionColumns
  }
}

const getEnhancedAOPColDefs = ({
  allGradesReciepes,
  headerMap,
  configType,
  FORMATE_VALUE,
  allGradesRecipes,
}) => {
  var config = []

  if (configType == 'grades') {
    config = [
      {
        field: 'ReceipeName',
        title: 'Recipe',
        editable: false,
        width1: 200,
      },
      {
        field: 'UOM',
        title: 'UOM',
        editable: false,
        width1: 85,
      },
    ]
    allGradesReciepes?.forEach((field) => {
      config.push({
        field: field?.id?.toUpperCase(),
        title: field?.displayName,
        editable: true,
        width1: 200,
        type: 'number',
        format: FORMATE_VALUE,
      })
    })
  } else if (configType == 'lines') {
    config = [
      {
        field: 'GradeName',
        title: 'Grade',
        editable: false,
        width1: 200,
      },
      {
        field: 'UOM',
        title: 'UOM',
        editable: false,
        width1: 85,
      },
    ]
    allGradesRecipes?.forEach((line) => {
      config.push({
        field: line?.Id?.toUpperCase(), // use Id from API
        title: line?.DisplayName, // use DisplayName
        editable: true,
        width1: 200,
        type: 'number',
        format: FORMATE_VALUE,
      })
    })
  } else {
    config = getConfigByType(configType)
  }

  var enhancedColDefs = []

  if (
    configType == 'ShutdownNorms' ||
    configType == 'cracker_constants' ||
    configType == 'megConstants' ||
    configType == 'Constant'
  ) {
    enhancedColDefs = config.map((col) => {
      if (col?.title == 'Value') {
        return {
          ...col,
          type: 'number',
          format: FORMATE_VALUE,
        }
      }

      return col
    })
  } else if (
    configType == 'PIO Impact' ||
    configType == 'shutdownData' ||
    configType == 'cracker_configuration'
  ) {
    enhancedColDefs = config.map((col) => {
      if (headerMap && headerMap[col.title]) {
        return {
          ...col,
          title: headerMap[col.title],
          align: 'right',
          type: 'negativeNumber',
          format: FORMATE_VALUE,
        }
      }

      return col
    })
  } else if (configType == 'cracker_yield_dmd') {
    enhancedColDefs = config.map((col) => {
      if (headerMap && headerMap[col.title]) {
        return {
          ...col,
          title: headerMap[col.title],
          align: 'right',
          type: 'number',
          format: FORMATE_VALUE,
          widthT: 200,
          fixedWidth: 200,
          width: 200,
        }
      }

      return col
    })
  } else {
    enhancedColDefs = config.map((col) => {
      if (headerMap && headerMap[col.title]) {
        return {
          ...col,
          title: headerMap[col.title],
          align: 'right',
          type: 'number',
          format: FORMATE_VALUE,
        }
      }

      return col
    })
  }

  return enhancedColDefs
}

export default getEnhancedAOPColDefs
