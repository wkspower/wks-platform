import { Input } from '@progress/kendo-react-inputs'
import NotificationTST from 'components/Utilities/NotificationTST'
import { useState, useEffect, useRef } from 'react'

export const NoSpinnerNumericEditor = ({ dataItem, field, onChange }) => {
  // Handle nested field paths (e.g., "apr.shutdownHrs")
  const getNestedValue = (obj, path) => {
    if (!path || !obj) return undefined
    const keys = path.split('.')
    return keys.reduce((acc, key) => acc?.[key], obj)
  }

  const initialValue = getNestedValue(dataItem, field) ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const isFirstRender = useRef(true)
  const inputRef = useRef(null)

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const handleChange = (e) => {
    const val = e.target.value
    if (val === '' || /^\d*(\.\d*)?$/.test(val)) {
      if (dataItem?.productName?.trim().toLowerCase() === 'tst') {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please enter a value between 100 and 370 !',
          severity: 'warning',
        })
      }
      setLocalValue(val)
    }
  }

  const handleBlur = () => {
    if (localValue !== initialValue) {
      onChange({ dataItem, field, value: localValue })
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

  useEffect(() => {
    if (inputRef.current) {
      const el = inputRef.current.element || inputRef.current
      if (el && typeof el.focus === 'function') el.focus()
    }
  }, [])

  return (
    <td>
      <Input
        ref={inputRef}
        value={localValue}
        onChange={handleChange}
        onBlur={handleBlur}
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
    </td>
  )
}
