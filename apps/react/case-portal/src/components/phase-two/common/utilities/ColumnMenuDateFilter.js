import { GridColumnMenuFilter } from '@progress/kendo-react-grid'

export const getColumnMenuDateFilter = () => {
  const ColumnMenuDateFilter = (props) => {
    return (
      <GridColumnMenuFilter
        {...props}
        expand={true}
        operators={{
          date: [
            { text: 'Is after', operator: 'gt' }, // >
            { text: 'Is before', operator: 'lt' }, // <
            { text: 'Is on', operator: 'eq' },
            { text: 'Is not on', operator: 'neq' },
            { text: 'After or equal', operator: 'gte' },
            { text: 'Before or equal', operator: 'lte' },
          ],
        }}
      />
    )
  }

  ColumnMenuDateFilter.displayName = 'ColumnMenuDateFilter'
  return ColumnMenuDateFilter
}
