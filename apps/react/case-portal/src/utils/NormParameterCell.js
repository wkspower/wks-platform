import React from 'react'

const NormParameterCell = (props) => {
  const { dataItem, allProducts } = props

  // console.log('props:', props)
  // console.log('normParameterId:', dataItem.normParameterId)
  // console.log('allProducts:', allProducts)

  const match = allProducts?.find(
    (p) => p.id?.toLowerCase() === dataItem.normParameterId?.toLowerCase(),
  )

  return <td>{match?.displayName || dataItem.normParameterId}</td>
}

export default NormParameterCell
