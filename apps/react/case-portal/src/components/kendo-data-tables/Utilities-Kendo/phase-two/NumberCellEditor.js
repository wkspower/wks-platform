import { Input } from '@progress/kendo-react-inputs'
import NotificationTST from 'components/Utilities/NotificationTST'
import { useState, useEffect, useRef } from 'react'

// Utility: Get nested property value by path (supports any depth)
// Handles both nested fields (e.g., "april.shutdownHrs") and flat fields (e.g., "apr")
const getNestedValue = (obj, path) => {
  if (!path || !obj) return undefined
  const keys = path.split('.')
  return keys.reduce((acc, key) => acc?.[key], obj)
}

export const NumberCellEditor = ({ dataItem, field, onChange, wholeNumberOnly = false, maxValue = null }) => {
  // Handle nested fields (e.g., "april.shutdownHrs")
  const initialValue = getNestedValue(dataItem, field) ?? ''
  const [localValue, setLocalValue] = useState(initialValue)

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const handleChange = (e) => {
    const val = e.target.value
    // Allow only whole numbers if wholeNumberOnly is true, otherwise allow decimals
    const pattern = wholeNumberOnly ? /^\d*$/ : /^\d*(\.\d*)?$/
    if (val === '' || pattern.test(val)) {
      // Check if value exceeds maxValue (for shutdownHrs validation)
      if (maxValue !== null && val !== '' && parseFloat(val) > maxValue) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Value cannot exceed ${maxValue}!`,
          severity: 'warning',
        })
        return
      }

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

  return (
    <>
      <Input
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
    </>
  )
}
