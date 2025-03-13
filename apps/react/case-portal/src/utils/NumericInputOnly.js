import React from 'react'
import { GridEditInputCell } from '@mui/x-data-grid'

function NumericInputOnly(props) {
  const handleKeyDown = (event) => {
    const key = event.key
    const value = event.target.value
    const isControlKeyPressed = event.ctrlKey || event.metaKey

    if (
      key !== 'Backspace' &&
      key !== 'Tab' &&
      key !== 'Enter' &&
      key !== 'ArrowLeft' &&
      key !== 'ArrowRight' &&
      key !== 'Delete' &&
      !isControlKeyPressed &&
      key !== '.' &&
      !/^\d$/.test(key)
    ) {
      event.preventDefault()
    }

    if (key === '.' && value.includes('.')) {
      event.preventDefault()
    }
  }

  const handlePaste = (event) => {
    const paste = event.clipboardData.getData('text')
    const isValidPaste = /^\d*\.?\d*$/.test(paste)
    if (!isValidPaste) {
      event.preventDefault()
    }
  }

  return (
    <GridEditInputCell
      {...props}
      type='text'
      onKeyDown={handleKeyDown}
      onPaste={handlePaste}
    />
  )
}

export default NumericInputOnly
