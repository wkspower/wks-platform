export default function ClickableCell({
  dataItem,
  field,
  onCellClick,
  ...tdProps
}) {
  return (
    <td
      {...tdProps}
      style={{ cursor: 'pointer' }}
      onClick={() => onCellClick({ id: dataItem.ProductID, field })}
    >
      {field.split('.').reduce((obj, key) => obj[key], dataItem)}
    </td>
  )
}
