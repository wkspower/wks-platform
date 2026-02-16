// src/utils/useValueFormatter.js
import { useSelector } from 'react-redux'

export default function ValueFormatterConsumption() {
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const VERTICAL_NAME = dataGridStore?.verticalObject?.name?.toLowerCase()
  const SITE_NAME = dataGridStore?.siteObject?.name?.toLowerCase()
  const PLANT_NAME = dataGridStore?.plantObject?.name?.toLowerCase()

  if (VERTICAL_NAME === 'cracker') {
    return '{0:0.0000}'
  }
  if (VERTICAL_NAME === 'meg' || VERTICAL_NAME === 'elastomer') {
    return '{0:0.00000}'
  }
  if (VERTICAL_NAME === 'pta') {
    return '{0:0.00000}'
  }
  if (VERTICAL_NAME === 'vcm' && SITE_NAME === 'dmd') {
    return '{0:0.0000}'
  }

  return '{0:0.000}'
}
