import { Input } from '@progress/kendo-react-inputs'

export const NoSpinnerNumericEditor = (props) => {
  const rawValue = props.dataItem[props.field] ?? ''

  const handleChange = (e) => {
    const newVal = e.target.value
    if (/^\d*(\.\d*)?$/.test(newVal)) {
        props.onChange({
          dataItem: props.dataItem,
          field: props.field,
        value: newVal,
        })
    }
  }
      // style={{ width: '100%' }}

  return (
    <td style={{ textAlign: 'end' }}>
      <Input value={rawValue} onChange={handleChange} />
    </td>
  )
}
