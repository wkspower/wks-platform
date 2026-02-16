import { Input } from '@progress/kendo-react-inputs'
import { useState, useEffect, useRef } from 'react'

export const PostCrDaysEditor = ({ dataItem, field, onChange }) => {
  // Check if isCr was true in the original data (before any edits)
  // We'll need to pass this as a prop or check modifiedCells
  // const wasOriginallyIsCr = dataItem.originalIsCr !== undefined
  //   ? dataItem.originalIsCr
  //   : dataItem.isCr;

  // const isEditable = wasOriginallyIsCr === true;
  const isEditable = dataItem.IsCR === true
  const initialValue = dataItem[field] ?? ''
  const [localValue, setLocalValue] = useState(initialValue)
  const isFirstRender = useRef(true)

  const handleChange = (e) => {
    const val = e.target.value

    // allow clearing the field
    if (val === '') {
      setLocalValue(val)
      return
    }

    // allow only digits
    if (!/^\d+$/.test(val)) return

    const num = Number(val)

    // allow only 0 to 130
    if (num >= 0 && num <= 130) {
      setLocalValue(val)
    }
  }

  // Debounced sync to grid, but skip first render
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    // Only sync if editable
    if (!isEditable) return

    const handler = setTimeout(() => {
      if (localValue !== initialValue) {
        onChange({ dataItem, field, value: localValue })
      }
    }, 300)

    return () => clearTimeout(handler)
  }, [localValue, dataItem, field, onChange, initialValue, isEditable])

  // Render decision AFTER all hooks
  if (!isEditable) {
    return <td style={{ textAlign: 'end' }}>{dataItem[field] ?? ''}</td>
  }

  return (
    <td style={{ textAlign: 'end' }}>
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
    </td>
  )
}
