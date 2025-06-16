import { Input } from '@progress/kendo-react-inputs'

export const TextCellEditor = (props) => {
  const rawValue = props.dataItem[props.field] ?? ''

  const handleChange = (e) => {
    const newVal = e.target.value
    props.onChange({
      dataItem: props.dataItem,
      field: props.field,
      value: newVal,
    })
  }

  return (
    <td>
      <Input
        value={rawValue}
        onChange={handleChange}
        style={{ width: '100%' }}
      />
    </td>
  )
}
