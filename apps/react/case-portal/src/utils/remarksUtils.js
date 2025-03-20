export const truncateRemarks = (text, maxWords = 2, maxLength = 140) => {
  const words = text?.split(' ')
  let displayText = words?.slice(0, maxWords).join(' ')

  if (words?.length > maxWords) {
    displayText += '...'
  }

  return displayText?.length > maxLength
    ? displayText?.substring(0, maxLength) + '...'
    : displayText
}
