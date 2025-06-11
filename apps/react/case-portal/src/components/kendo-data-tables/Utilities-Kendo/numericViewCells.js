import { Input } from '@progress/kendo-react-inputs'

export const DurationEditor = (props) => {
  const { dataItem, field, onChange } = props
  const raw = dataItem[field] ?? ''

  const handleChange = (e) => {
    const v = e.target.value
    // allow digits and one dot, up to two decimals (minutes)
    if (/^\d{0,5}(\.\d{0,2})?$/.test(v)) {
      onChange({ dataItem, field, value: v })
    }
    // else ignore invalid keystrokes
  }

  return (
    <Input
      value={raw}
      onChange={handleChange}
      placeholder='HH.MM'
      style={{ textAlign: 'end' }}
    />
  )
}

// import { NumericTextBox } from '@progress/kendo-react-inputs'

// // Custom editor without spinner, clamps value to [0, 99999.99]
// export const durationCell = (props) => {
//   const { field, dataItem, onChange } = props
//   return (
//     <NumericTextBox
//       value={dataItem[field]}
//       onChange={(e) => {
//         let val = e.value
//         // Clamp: ensure number and within bounds
//         if (typeof val !== 'number' || isNaN(val)) {
//           val = 0
//         }
//         // Max is 99999.99 (so <100000). Adjust decimals as needed.
//         if (val > 99999.99) {
//           val = 99999.99
//         }
//         if (val < 0) {
//           val = 0
//         }
//         // Propagate change back to grid
//         onChange({
//           dataItem,
//           field,
//           syntheticEvent: e.syntheticEvent,
//           value: val,
//         })
//       }}
//       format='n2'
//       min={0}
//       max={99999.99}
//       spinners={false}
//       step={0.01}
//     />
//   )
// }
