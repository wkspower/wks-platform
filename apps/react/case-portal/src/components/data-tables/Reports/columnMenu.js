import React from 'react'
import {
  GridColumnMenuFilter,
  GridColumnMenuCheckboxFilter,
} from '@progress/kendo-react-grid'

export const ColumnMenu = (props) => {
  return (
    <div>
      <GridColumnMenuFilter {...props} expanded={true} />
    </div>
  )
}

export const ColumnMenuCheckboxFilter = (props) => {
  return (
    <div>
      <GridColumnMenuCheckboxFilter {...props} expanded={true} />
    </div>
  )
}
