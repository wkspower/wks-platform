export function tryParseJSONObject(jsonString) {
  try {
    JSON.parse(jsonString)
  } catch {
    return false
  }
  return true
}
