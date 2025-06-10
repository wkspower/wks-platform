const NormParameterCell = (props) => {
  const { dataItem } = props

  const match = props.allProducts?.find(
    (p) => p.id?.toLowerCase() === dataItem.normParameterId?.toLowerCase(),
  )

  return <td>{match?.displayName || dataItem.normParameterId}</td>
}
