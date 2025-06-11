import { NumericTextBox } from '@progress/kendo-react-inputs'

export const NoSpinnerNumericEditor = (props) => {
  const value = props.dataItem[props.field]

  return (
    <td style={{ textAlign: 'end' }}>
    <NumericTextBox
      value={value}
      spinners={false}
      onChange={(e) => {
        props.onChange({
          dataItem: props.dataItem,
          field: props.field,
          value: e.value,
        })
      }}
        // decimals={6}
      // style={{ width: '100%' }}
    />
    </td>
  )
}
