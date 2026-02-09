import { Input } from '@progress/kendo-react-inputs'
import NotificationTST from 'components/Utilities/NotificationTST'
import { useState, useEffect, useRef } from 'react'

export const NoSpinnerNumericEditorCrackerValidation = ({
  dataItem,
  field,
  onChange,
}) => {
  const initialValue = dataItem[field] ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const isFirstRender = useRef(true)

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  // list of fields that should be validated against days-in-month
  const monthDayFields = new Set([
    'CoilReplacement',
    'MNT',
    'Shoutdown',
    'Slowdown',
    'SAD',
    'BBD',
    'BBU',
    'DemoHSS',
    'DemoBBU',
    'DemoSAD',
    'DemoSD',
    '4FD',
    '4F',
    '5F',
    'Total',
    '4FHours',
    'TotalSAD',
    'NumberOfDays',
    'NoOfSAD',
  ])

  const getDaysInMonthFromAOP = (aopYear, monthName) => {
    if (!monthName) return 31

    const months = [
      'january',
      'february',
      'march',
      'april',
      'may',
      'june',
      'july',
      'august',
      'september',
      'october',
      'november',
      'december',
    ]

    const m = monthName.trim().toLowerCase()
    const monthIndex = months.indexOf(m)
    if (monthIndex === -1) return 31

    let startYear = NaN
    if (typeof aopYear === 'string') {
      const match = aopYear.match(/^(\d{4})/)
      if (match) startYear = Number(match[1])
    }

    if (Number.isNaN(startYear)) {
      startYear = new Date().getFullYear()
    }

    const year = monthIndex >= 3 ? startYear : startYear + 1
    const days = new Date(year, monthIndex + 1, 0).getDate()

    return days
  }

  const getAllowedRange = (field) => {
    if (field === 'Reduction') {
      return { min: 0, max: 100 }
    }

    if (['Pre_CR_Days', 'Post_CR_Days', 'ActualRunLength'].includes(field)) {
      return { min: 0, max: 130 }
    }

    if (monthDayFields.has(field)) {
      const aopYear = dataItem?.AOPYear
      const monthName = dataItem?.MonthName
      const daysInMonth = getDaysInMonthFromAOP(aopYear, monthName)

      return { min: 0, max: daysInMonth }
    }

    return null
  }

  const handleChange = (e) => {
    const val = e.target.value

    if (dataItem?.productName?.trim().toLowerCase() === 'tst') {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Please enter a value between 100 and 370 !',
        severity: 'warning',
      })
    }

    const range = getAllowedRange(field)

    const isMonthDayField = range && monthDayFields.has(field)

    const allowedRegex = isMonthDayField ? /^\d*$/ : /^\d*(\.\d*)?$/

    if (val === '' || allowedRegex.test(val)) {
      if (range && val !== '') {
        const numVal = isMonthDayField ? parseInt(val, 10) : Number(val)

        if (Number.isNaN(numVal)) {
          return
        }

        if (numVal < range.min || numVal > range.max) {
          return
        }
      }

      setLocalValue(val)
    } else {
      // console.log('? BLOCKED BY REGEX')
    }
  }

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    const handler = setTimeout(() => {
      if (localValue !== initialValue) {
        onChange({ dataItem, field, value: localValue })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, initialValue])

  return (
    <>
      <Input
        value={localValue}
        onChange={handleChange}
        style={{
          fontSize: '0.8rem',
          padding: '2px 2px',
          height: '22px',
          lineHeight: '1rem',
        }}
      />

      <NotificationTST
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
    </>
  )
}
