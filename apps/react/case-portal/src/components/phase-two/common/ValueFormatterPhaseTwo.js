import { useSelector } from 'react-redux'

export default function ValueFormatterPhaseTwo() {
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const VERTICAL_NAME = dataGridStore?.verticalObject?.name?.toLowerCase()

  return '{0:0.00}'
}
