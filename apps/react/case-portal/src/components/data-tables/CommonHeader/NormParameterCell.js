const NormParameterCell = (props) => {
  const { dataItem } = props
  console.log('props:', props)
  console.log('normParameterId:', dataItem.normParameterId)
  console.log('allProducts:', props.allProducts) // pass allProducts as a prop

  const match = props.allProducts?.find(
    (p) => p.id?.toLowerCase() === dataItem.normParameterId?.toLowerCase(),
  )

  return <td>{match?.displayName || dataItem.normParameterId}</td>
}
