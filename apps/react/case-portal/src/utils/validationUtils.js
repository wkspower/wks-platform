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
    normParametersFKId: 'Particular',
    aopRemarks: 'Remark',
    remarks: 'Remark',
    ThroughputActual: 'Actual Throughput',
  }

  const invalidRows = data.filter((row) => {
    // Check for required fields
    const hasMissingField = requiredFields.some((field) => {
      const value = row[field]
      if (field === 'remark' || field === 'aopRemarks' || field === 'remarks') {
        return (
          value === undefined ||
          value === null ||
          value.trim() === '' ||
          value.trim() === (row.originalRemark || '').trim()
        )
      }

      if (value === undefined || value === null) return true
      if (typeof value === 'string' && value.trim() === '') return true
      return false
    })

    // Additional check: End Date must be after Start Date
    const startDate = new Date(row.maintStartDateTime)
    const endDate = new Date(row.maintEndDateTime)
    const invalidDate = startDate && endDate && endDate <= startDate

    return hasMissingField || invalidDate
  })

  if (invalidRows.length > 0) {
    const missingFields = invalidRows
      .map((row) => {
        const missingFieldsMessage = []
        requiredFields.forEach((field) => {
          const value = row[field]

          if (
            field === 'remark' ||
            field === 'aopRemarks' ||
            field === 'remarks'
          ) {
            if (
              value === undefined ||
              value === null ||
              value.trim() === '' ||
              value.trim() === (row.originalRemark || '').trim()
            ) {
              missingFieldsMessage.push(fieldHeaders[field] || field)
            }
          } else if (value === undefined || value === null) {
            missingFieldsMessage.push(fieldHeaders[field] || field)
          } else if (typeof value === 'string' && value.trim() === '') {
            missingFieldsMessage.push(fieldHeaders[field] || field)
          }
        })

        // Add End Date check message
        const startDate = new Date(row.maintStartDateTime)
        const endDate = new Date(row.maintEndDateTime)
        if (startDate && endDate && endDate <= startDate) {
          missingFieldsMessage.push('End Date must be after Start Date')
        }

        return missingFieldsMessage.join(', ')
      })
      .filter((msg) => msg !== '')
      .join(', ')

    if (missingFields) {
      const uniqueFields = [...new Set(missingFields.split(', '))].join(', ')
      return `Please update the following fields: ${uniqueFields}`
    }
    return ''
  }

  return ''
}
