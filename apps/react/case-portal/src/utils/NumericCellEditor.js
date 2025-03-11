import React, { useState } from 'react'
import TextField from '@mui/material/TextField'

const NumericCellEditor = ({ value, api, field }) => {
  const [inputValue, setInputValue] = useState(value || '0')

  const handleChange = (event) => {
    let newValue = event.target.value

    // Only allow numeric input (allow backspace too)
    if (/^\d*$/.test(newValue)) {
      setInputValue(newValue)
    }
  }

  const handleBlur = () => {
    if (api && field) {
      api.setEditCellValue(inputValue, true) // Set the edited value
      api.stopCellEditMode() // Exit edit mode
    } else {
      console.error('API or field is undefined. Cannot commit cell changes.')
    }
  }

  return (
    <TextField
      value={inputValue}
      onChange={handleChange}
      onBlur={handleBlur} // Commit value on blur
      type='text' // To manually control the input
      variant='outlined'
      size='small'
      fullWidth
      autoFocus
    />
  )
}

export default NumericCellEditor
