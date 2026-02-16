import productionColDefs from '../../../assets/kendo_proposed_consumption_aop.json'

// const DEFAULT_NUM_WIDTH = 150
// const DEFAULT_OTHER_WIDTH = 150
// const DEFAULT_NUM_FORMAT = '{0:n2}'

const normalizeCol = (col, valueFormat) => {
  const isNumeric = col.type === 'number' || col.type === 'numberNonGrey'

  const newCol = {
    ...col,
    fixedWidth: col.fixedWidth,
    ...(isNumeric && !col.format && { format: valueFormat }),
    ...(col.children && {
      children: col.children.map((c) => normalizeCol(c, valueFormat)),
    }),
  }

  return newCol
}
const getEnhancedColDefsProposedNorms = ({
  headerMap,
  lowerVertName,
  valueFormat,
  AOP_YEAR,
}) => {
  const colDefs = Array.isArray(productionColDefs) ? productionColDefs : []
  return colDefs.map((c) => normalizeCol(c, valueFormat, AOP_YEAR))
}

export default getEnhancedColDefsProposedNorms
