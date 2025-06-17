import { Input } from '@progress/kendo-react-inputs'

export const DurationEditor = (props) => {
  const { dataItem, field, onChange } = props
  const raw = dataItem[field] ?? ''

  const handleChange = (e) => {
    const v = e.target.value
    if (/^(\d+)?(\.\d{0,2})?$/.test(v)) {
      // if (/^(\d{0,2})?(\.\d{0,2})?$/.test(v)) {
      const parts = v.split('.')
      if (parts.length === 2) {
        const mins = parseInt(parts[1].padEnd(2, '0'), 10)
        if (mins >= 60) return
      }

      onChange({ dataItem, field, value: v })
    }
  }

  return <Input value={raw} onChange={handleChange} placeholder='HH:MM' />
}
