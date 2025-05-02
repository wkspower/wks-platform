import React, { useState } from 'react'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs from 'dayjs'

export const StartDateTimeEditCell = ({ id, field, value, api }) => {
  const row = api.getRow(id)
  const endDate = row?.maintEndDateTime ? dayjs(row.maintEndDateTime) : null

  const yearRange = localStorage.getItem('year') || ''
  const [startYearStr] = yearRange.split('-')
  const startYear = parseInt(startYearStr, 10)

  const financialYearStart = dayjs(`${startYear}-04-01`)
  const financialYearEnd = dayjs(`${startYear + 1}-03-31`)

  const [selectedDate, setSelectedDate] = useState(
    value && dayjs(value).isValid() ? dayjs(value) : null,
  )

  const handleChange = (newValue) => {
    setSelectedDate(newValue)
    api.setEditCellValue({
      id,
      field,
      value: newValue ? newValue.toISOString() : null,
    })
    // Stop the row edit mode after the value is set
    // api.stopRowEditMode({ id })
  }

  const handlePickerClose = () => {
    // console.log('Date picker closed')
    api.stopRowEditMode({ id })
  }

  const handlePickerKeyDown = (e) => {
    const isArrowKey = [
      'ArrowUp',
      'ArrowDown',
      'ArrowLeft',
      'ArrowRight',
    ].includes(e.key)
    const isNumpadArrow = ['Numpad8', 'Numpad2', 'Numpad4', 'Numpad6'].includes(
      e.key,
    )
    const isNumberKey = /^[0-9]$/.test(e.key)

    if (isArrowKey || isNumpadArrow || isNumberKey) {
      e.stopPropagation()
      e.preventDefault()
    }
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <DateTimePicker
        value={selectedDate}
        onChange={handleChange}
        minDateTime={financialYearStart}
        maxDateTime={endDate ?? financialYearEnd}
        format='DD/MM/YYYY, h:mm:ss A'
        timeSteps={{ minutes: 1 }}
        onKeyDown={handlePickerKeyDown}
        onClose={handlePickerClose}
        slotProps={{
          textField: {
            size: 'small',
            inputProps: {
              readOnly: true,
              tabIndex: -1,
              onKeyDown: (e) => e.preventDefault(),
            },
            sx: {
              minWidth: '180px',
              fontSize: '1.5rem',
            },
          },
          popper: {
            onMouseLeave: () => {
              try {
                api.stopRowEditMode({ id })
              } catch (error) {
                // Optional: console.log('Row not in edit mode:', error.message)
              }
            },

            sx: {
              '& .MuiPaper-root': {
                transform: 'scale(0.70)',
                transformOrigin: 'top left',
                fontSize: '2rem',
              },
              '& .MuiTypography-root': {
                fontSize: '1rem',
              },
              '& .MuiPickersDay-root': {
                fontSize: '1rem',
              },
              '& .MuiButtonBase-root': {
                fontSize: '1rem',
              },
              '& .MuiInputBase-input': {
                fontSize: '1rem',
              },
            },
          },
        }}
      />
    </LocalizationProvider>
  )
}

export default StartDateTimeEditCell
