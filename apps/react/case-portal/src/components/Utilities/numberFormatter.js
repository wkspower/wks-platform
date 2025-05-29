export function formatToDecimals(v, decimals = 5) {
  if (v === 0) return '0'
  if (v == null) return ''
  return parseFloat(v).toFixed(decimals)
}
