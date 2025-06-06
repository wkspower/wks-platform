import { useMemo, useCallback } from 'react'
import { DateTimePicker, LocalizationProvider } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs from 'dayjs'
import { TextField } from '@mui/material'

const DateTimePickerEditor = ({ dataItem, field, onChange }) => {
  const fyString = localStorage.getItem('year')

  const { fyMin, fyMax } = useMemo(() => {
    if (!fyString) return { fyMin: null, fyMax: null }
    const parts = fyString.split('-')
    if (parts.length !== 2) return { fyMin: null, fyMax: null }

    let startYear = parseInt(parts[0], 10)
    let endYear = parseInt(parts[1], 10)
    if (parts[1].length === 2) {
      endYear += 2000
    }
    return {
      fyMin: new Date(startYear, 3, 1, 0, 0, 0),
      fyMax: new Date(endYear, 2, 31, 23, 59, 59),
    }
  }, [fyString])

  const isStart = field === 'maintStartDateTime'
  const isEnd = field === 'maintEndDateTime'
  const partnerField = isStart
    ? 'maintEndDateTime'
    : isEnd
      ? 'maintStartDateTime'
      : null
  const otherRaw = partnerField ? dataItem[partnerField] : null
  const otherDate = otherRaw ? new Date(otherRaw) : null

  let dynamicMin = null
  let dynamicMax = null

  if (isEnd && otherDate) dynamicMin = otherDate
  if (isStart && otherDate) dynamicMax = otherDate

  const finalMin =
    fyMin && dynamicMin
      ? fyMin > dynamicMin
        ? fyMin
        : dynamicMin
      : fyMin || dynamicMin
  const finalMax =
    fyMax && dynamicMax
      ? fyMax < dynamicMax
        ? fyMax
        : dynamicMax
      : fyMax || dynamicMax

  const currentRaw = dataItem[field]
  const currentDate = currentRaw ? new Date(currentRaw) : null

  const handleValueChange = useCallback(
    (newValue, context) => {
      const jsDate = newValue && newValue.toDate ? newValue.toDate() : null
      onChange({
            dataItem,
            field,
        value: jsDate,
        syntheticEvent: context?.syntheticEvent,
          })
    },
    [dataItem, field, onChange],
  )
  return (
    <td>
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <DateTimePicker
          value={currentDate ? dayjs(currentDate) : null}
          minDateTime={finalMin ? dayjs(finalMin) : null}
          maxDateTime={finalMax ? dayjs(finalMax) : null}
          onChange={handleValueChange}
          format='DD/MM/YYYY, h:mm:ss A'
          minutesStep={1}
          slotProps={{
            textField: {
              size: 'small',
              inputProps: {
                readOnly: true,
                tabIndex: -1,
                onKeyDown: (e) => e.preventDefault(),
              },
              sx: { minWidth: '180px', fontSize: '1.5rem' },
            },
            popper: {
              sx: {
                '& .MuiPaper-root': {
                  transform: 'scale(0.70)',
                  transformOrigin: 'top left',
                  fontSize: '2rem',
                },
                '& .MuiTypography-root': { fontSize: '1rem' },
                '& .MuiPickersDay-root': { fontSize: '1rem' },
                '& .MuiButtonBase-root': { fontSize: '1rem' },
                '& .MuiInputBase-input': { fontSize: '1rem' },
              },
            },
          }}
          renderInput={(params) => <TextField {...params} />}
        />
      </LocalizationProvider>
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
//         format='dd/MM/yyyy hh:mm a'
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
