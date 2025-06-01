import { useMemo } from 'react'
import { DateTimePicker } from '@progress/kendo-react-dateinputs'

const DateTimePickerEditor = ({ dataItem, field, onChange }) => {
  const fyString = localStorage.getItem('year')

  const { minDate: fyMin, maxDate: fyMax } = useMemo(() => {
    if (!fyString) {
      return { minDate: null, maxDate: null }
    }
    const parts = fyString.split('-')
    if (parts.length !== 2) {
      return { minDate: null, maxDate: null }
    }
    const startYearNum = parseInt(parts[0], 10)
    let endYearNum = parseInt(parts[1], 10)
    if (parts[1].length === 2) {
      endYearNum += 2000
    }
    const minD = new Date(startYearNum, 3, 1, 0, 0, 0)
    const maxD = new Date(endYearNum, 2, 31, 23, 59, 59)
    return { minDate: minD, maxDate: maxD }
  }, [fyString])

  const isStart = field === 'maintStartDateTime'
  const isEnd = field === 'maintEndDateTime'

  const otherField = isStart ? 'maintEndDateTime' : 'maintStartDateTime'
  const otherRaw = dataItem[otherField]
  const otherDate = otherRaw ? new Date(otherRaw) : null

  let dynamicMin = null
  let dynamicMax = null

  if (isEnd && otherDate) {
    dynamicMin = otherDate
  }

  if (isStart && otherDate) {
    dynamicMax = otherDate
  }

  let finalMin = null
  if (fyMin && dynamicMin) {
    finalMin = fyMin > dynamicMin ? fyMin : dynamicMin
  } else {
    finalMin = fyMin || dynamicMin
  }

  let finalMax = null
  if (fyMax && dynamicMax) {
    finalMax = fyMax < dynamicMax ? fyMax : dynamicMax
  } else {
    finalMax = fyMax || dynamicMax
  }

  const currentRaw = dataItem[field]
  const currentDate = currentRaw ? new Date(currentRaw) : null

  return (
    <td>
      <DateTimePicker
        value={currentDate}
        min={finalMin}
        max={finalMax}
        format='dd/MM/yyyy hh:mm tt'
        onChange={(e) =>
          onChange({
            dataItem,
            field,
            value: e.value,
            syntheticEvent: e.syntheticEvent,
          })
        }
      />
    </td>
  )
}

export default DateTimePickerEditor

// import { useMemo } from 'react'
// import { DateTimePicker } from '@progress/kendo-react-dateinputs'

// const DateTimePickerEditor = ({ dataItem, field, onChange }) => {
//   const fyString = localStorage.getItem('year') // e.g. "2025-26"

//   const { minDate, maxDate } = useMemo(() => {
//     if (!fyString) {
//       return { minDate: null, maxDate: null }
//     }
//     const parts = fyString.split('-')
//     if (parts.length !== 2) {
//       return { minDate: null, maxDate: null }
//     }
//     const startYearNum = parseInt(parts[0], 10)
//     let endYearNum = parseInt(parts[1], 10)
//     if (parts[1].length === 2) {
//       endYearNum += 2000
//     }
//     const minD = new Date(startYearNum, 3, 1, 0, 0, 0)
//     const maxD = new Date(endYearNum, 2, 31, 23, 59, 59)
//     return { minDate: minD, maxDate: maxD }
//   }, [fyString])

//   const value = dataItem[field] ? new Date(dataItem[field]) : null

//   return (
//     <td>
//       <DateTimePicker
//         value={value}
//         min={minDate}
//         max={maxDate}
//         format='dd/MM/yyyy hh:mm tt'
//         onChange={(e) =>
//           onChange({
//             dataItem,
//             field,
//             value: e.value,
//             syntheticEvent: e.syntheticEvent,
//           })
//         }
//       />
//     </td>
//   )
// }

// export default DateTimePickerEditor
