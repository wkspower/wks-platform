import { DatePicker } from '@progress/kendo-react-dateinputs'
import { DropDownList } from '@progress/kendo-react-dropdowns'

const dateOperators = [
  { text: 'Equals', operator: 'eq' },
  { text: 'Greater than', operator: 'gt' },
  { text: 'Less than', operator: 'lt' },
]

export const DateColumnMenu = (props) => {
  const {
    column,
    filter = { logic: 'and', filters: [] },
    onFilterChange,
    onCloseMenu,
    setIsDateFilterActive,
    isDateFilterActive
  } = props
  // console.log('---column---', column.field);
  
  const current = filter.filters.find((f) => f.field === column.field) || {}

  const handleFilterChange = (newValue, operator) => {
    const newFilters = [
      ...filter.filters.filter((f) => f.field !== column.field),
    ]

    if (newValue) {
      newFilters.push({
        field: column.field,
        operator: operator,
        value: newValue,
      })
    }

    onFilterChange({
      ...filter,
      filters: newFilters,
    })
    setIsDateFilterActive(p => p.includes(column.field) ? p : [...p,column.field])
  }

  const clearDateFilter = () => {
    const newFilters = filter.filters.filter((f) => f.field !== column.field)
    onFilterChange({
      logic: 'and',
      filters: newFilters,
    })
    setIsDateFilterActive(p => p.includes(column.field) ? p.filter(c => c !== column.field) : p)
  }

  return (
    <div style={{ padding: 12 }}>
      <DropDownList
        data={dateOperators}
        textField='text'
        dataItemKey='operator'
        value={
          dateOperators.find((item) => item.operator === current.operator) ||
          dateOperators[0]
        }
        onChange={(e) => handleFilterChange(current.value, e.value.operator)}
        style={{ width: '100%', marginBottom: 8 }}
      />

      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <DatePicker
          format='dd-MM-yyyy'
          value={current.value || null}
          onChange={(e) =>
            handleFilterChange(e.value, current.operator || 'eq')
          }
          style={{ flex: 1 }}
        />

        <button
          onClick={clearDateFilter}
          style={{
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontSize: 18,
            lineHeight: 1,
            padding: 0,
          }}
          title='Clear date filter'
        >
          x
        </button>
      </div>
    </div>
  )
}

// import * as React from 'react'
// import {
//   GridColumnMenuSort,
//   GridColumnMenuFilter,
// } from '@progress/kendo-react-grid'
// import { DatePicker } from '@progress/kendo-react-dateinputs'
// import { DropDownList } from '@progress/kendo-react-dropdowns'

// const dateOperators = [
//   { text: 'Equals', operator: 'eq' },
//   { text: 'Not equal', operator: 'neq' },
//   { text: 'Greater than', operator: 'gt' },
//   { text: 'Greater than or equal', operator: 'gte' },
//   { text: 'Less than', operator: 'lt' },
//   { text: 'Less than or equal', operator: 'lte' },
// ]

// export const DateColumnMenu = (props) => {
//   const {
//     column,
//     filter = { logic: 'and', filters: [] },
//     onFilterChange,
//     onCloseMenu,
//   } = props

//   const current = filter.filters.find((f) => f.field === column.field) || {}

//   const handleFilterChange = (newValue, operator) => {
//     const newFilters = [
//       ...filter.filters.filter((f) => f.field !== column.field),
//     ]

//     if (newValue) {
//       newFilters.push({
//         field: column.field,
//         operator: operator,
//         value: newValue,
//       })
//     }

//     onFilterChange({
//       ...filter,
//       filters: newFilters,
//     })
//   }

//   return (
//     <div style={{ padding: 12 }}>
//       <GridColumnMenuSort {...props} />
//       <hr />
//       <div style={{ marginBottom: 8 }}>
//         <strong>{column.title || column.field} Filter</strong>
//       </div>
//       <DropDownList
//         data={dateOperators}
//         textField='text'
//         dataItemKey='operator'
//         value={
//           dateOperators.find((item) => item.operator === current.operator) ||
//           dateOperators[0]
//         }
//         onChange={(e) => handleFilterChange(current.value, e.value.operator)}
//         style={{ width: '100%', marginBottom: 8 }}
//       />
//       <DatePicker
//         format='dd-MM-yyyy'
//         value={current.value || null}
//         onChange={(e) => handleFilterChange(e.value, current.operator || 'eq')}
//         style={{ width: '100%' }}
//       />
//       <GridColumnMenuFilter {...props} />
//     </div>
//   )
// }
