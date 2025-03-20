export const validateFields = (data, requiredFields) => {
  const fieldHeaders = {
    normParameterId: 'Particular',
    remark: 'Remark',
    discription: 'Description',
    maintEndDateTime: 'End Date',
    maintStartDateTime: 'Start Date',
    rate: 'Rate',
    durationInHrs: 'Duration',
    product: 'Particular',
  }

  const invalidRows = data.filter((row) =>
    requiredFields.some((field) => {
      const value = row[field]
      if (value === undefined || value === null) return true // Missing values
      if (typeof value === 'string' && value.trim() === '') return true // Empty string
      return false
    }),
  )

  if (invalidRows.length > 0) {
    const missingFields = invalidRows
      .map((row, index) => {
        const missingFieldsMessage = []
        requiredFields.forEach((field) => {
          const value = row[field]

          if (value === undefined || value === null) {
            missingFieldsMessage.push(fieldHeaders[field] || field)
          } else if (typeof value === 'string' && value.trim() === '') {
            missingFieldsMessage.push(fieldHeaders[field] || field)
          }
        })

        return missingFieldsMessage.join(', ')
      })
      .filter((msg) => msg !== '') // Remove empty messages
      .join(', ')

    return missingFields
      ? `Please fill in the required fields: ${missingFields}`
      : ''
  }

  return ''
}
