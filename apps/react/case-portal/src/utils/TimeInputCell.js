import { GridEditInputCell } from '@mui/x-data-grid'

function TimeInputCell(props) {
  const handleKeyDown = (event) => {
    const { key, target } = event
    const value = target.value
    const isControlKey = event.ctrlKey || event.metaKey

    const allowedKeys = [
      'Backspace',
      'Tab',
      'Enter',
      'ArrowLeft',
      'ArrowRight',
      'Delete',
    ]

    if (
      allowedKeys.includes(key) ||
      isControlKey ||
      /^\d$/.test(key) || // allow numbers
      (key === '.' && !value.includes('.')) // allow one decimal point
    ) {
      return
    }

    event.preventDefault()
  }

  const handlePaste = (event) => {
    const pasted = event.clipboardData.getData('text')
    if (!/^\d{1,2}(\.\d{1,2})?$/.test(pasted)) {
      event.preventDefault()
      return
    }

    const [hours, minutes] = pasted.split('.')
    if (minutes && (parseInt(minutes, 10) > 59 || minutes.length > 2)) {
      event.preventDefault()
    }
  }

  const handleChange = (event) => {
    const value = event.target.value
    const [hours, minutes] = value.split('.')

    if (minutes && (minutes.length > 2 || parseInt(minutes, 10) > 59)) {
      return // don't allow typing more
    }

    // Correct way to update the value in the Data Grid
    props.api.setEditCellValue({
      id: props.id,
      field: props.field,
      value: value,
    })
  }

  return (
    <GridEditInputCell
      {...props}
      type='text'
      onKeyDown={handleKeyDown}
      onPaste={handlePaste}
      onChange={handleChange}
    />
  )
}

export default TimeInputCell
