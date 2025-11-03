// src/utils/useValueFormatter.js
import { useSelector } from 'react-redux'

export default function ValueFormatterConsumption() {
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const VERTICAL_NAME = dataGridStore?.verticalObject?.name?.toLowerCase()

  if (VERTICAL_NAME === 'aromatics') {
    return '{0:0.000}'
  }
  if (VERTICAL_NAME === 'cracker') {
    return '{0:0.0000}'
  }
  if (VERTICAL_NAME === 'elastomer') {
    return '{0:0.00000}'
  }

  return '{0:0.000}'
}
