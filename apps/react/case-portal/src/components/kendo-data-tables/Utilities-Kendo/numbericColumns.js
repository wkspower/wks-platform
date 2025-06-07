import { NumericTextBox } from '@progress/kendo-react-inputs'

export const NoSpinnerNumericEditor = (props) => {
  const value = props.dataItem[props.field]

  return (
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
      style={{ width: '100%' }}
    />
  )
}
