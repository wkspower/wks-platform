import { useSelector } from 'react-redux'

export default function ValueFormatterPhaseTwo() {
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const VERTICAL_NAME = dataGridStore?.verticalObject?.name?.toLowerCase()

  if (VERTICAL_NAME === 'cpp') {
    return '{0:0.0000}'
  }
  return '{0:0.00}'
}

export function customValueFormatterPhaseTwo(length = 2) {
  let format = '{0:0.00}'
  switch (length) {
    case 0:
      format = '{0:0}'
      break
    case 1:
      format = '{0:0.0}'
      break
    case 2:
      format = '{0:0.00}'
      break
    case 3:
      format = '{0:0.000}'
      break
    case 4:
      format = '{0:0.0000}'
      break
    case 5:
      format = '{0:0.00000}'
      break
    case 6:
      format = '{0:0.000000}'
      break
    default:
      format = '{0:0.00}'
      break
  }
  return format
}
