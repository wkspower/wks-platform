// utils/checkboxUtils.js
const toggleCheckbox = (dataItem, field, gridName, onGlobalCheckboxChange) => {
  const { materialName, id } = dataItem
  onGlobalCheckboxChange(
    gridName,
    id,
    materialName,
    field,
    !dataItem[field],
    dataItem,
  )
}

export default toggleCheckbox
