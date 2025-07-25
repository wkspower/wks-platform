import { Input } from '@progress/kendo-react-inputs'

export const descLimit = (props) => {
  const rawValue = props.dataItem[props.field] ?? ''
  const type = props?.dataItem?.type ?? ''
  // const isDisabled = type === 'ramp-down' || type === 'ramp-up'
  const isDisabled = false

  const handleChange = (e) => {
    const newVal = e.target.value

    const isValid = /^[a-zA-Z0-9 ]*$/.test(newVal) && newVal.length <= 50

    if (isValid) {
      props.onChange({
        dataItem: props.dataItem,
        field: props.field,
        value: newVal,
      })
    }
  }

  return (
    <td style={{ textAlign: 'end' }}>
      <Input
        value={rawValue}
        onChange={handleChange}
        maxLength={250}
        disabled={isDisabled}
      />
    </td>
  )
}
