import { GridColumnMenuCheckboxFilter } from '@progress/kendo-react-grid'

export const getColumnMenuCheckboxFilter = (data) => {
  const ColumnMenuCheckboxFilter = (props) => {
    return (
      <GridColumnMenuCheckboxFilter
        {...props}
        data={data}
        expanded={true}
        searchBoxFilterOperator='contains'
        uniqueData={true}
      />
    )
  }

  ColumnMenuCheckboxFilter.displayName = 'ColumnMenuCheckboxFilter'
  return ColumnMenuCheckboxFilter
}
