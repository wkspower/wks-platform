// src/utils/useValueFormatter.js
import { useSelector } from 'react-redux'

export default function ValueFormatterProduction() {
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const VERTICAL_NAME = dataGridStore?.verticalObject?.name?.toLowerCase()

  if (VERTICAL_NAME === 'aromatics') {
    return '{0:0.000}'
  }

  return '{0:0.00}'
}
