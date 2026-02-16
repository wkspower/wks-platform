import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
  Grid,
  Box,
} from '@mui/material'

const AddRowDialog = ({
  open,
  onClose,
  onSubmit,
  columns,
  groupTypes,
  tableKey,
}) => {
  const [formData, setFormData] = useState({})
  const [selectedType, setSelectedType] = useState('')
  const [customType, setCustomType] = useState('')
  const [errors, setErrors] = useState({})

  // Initialize form data when dialog opens
  useEffect(() => {
    if (open) {
      const initialData = {}
      columns.forEach((col) => {
        if (col.field && col.editable && col.field !== 'id') {
          initialData[col.field] = ''
        }
      })
      setFormData(initialData)
      setSelectedType('')
      setCustomType('')
      setErrors({})
    }
  }, [open, columns])

  const handleFieldChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
    // Clear error for this field
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev }
        delete newErrors[field]
        return newErrors
      })
    }
  }

  const handleTypeChange = (value) => {
    setSelectedType(value)
    if (value !== 'Other') {
      setCustomType('')
      handleFieldChange('type', value)
    } else {
      handleFieldChange('type', '')
    }
  }

  const handleCustomTypeChange = (value) => {
    setCustomType(value)
    handleFieldChange('type', value)
  }

  const validateForm = () => {
    const newErrors = {}

    // Get required fields based on table type
    const getRequiredFields = () => {
      switch (tableKey) {
        case 'CrudeBlendWindow':
          return ['type', 'property']
        case 'CrudeSpecificConstraints':
          return ['crude']
        case 'VGOVRDrop':
          return ['kbpsd']
        default:
          return []
      }
    }

    const requiredFields = getRequiredFields()

    // Validate required fields
    requiredFields.forEach((field) => {
      if (!formData[field] || formData[field].toString().trim() === '') {
        const column = columns.find((col) => col.field === field)
        const fieldName = column?.title || field
        newErrors[field] = `${fieldName} is required`
      }
    })

    // Validate number fields based on column definitions
    columns.forEach((col) => {
      if (
        (col.type === 'number' || col.type === 'number1') &&
        formData[col.field] !== '' &&
        formData[col.field] !== undefined &&
        formData[col.field] !== null
      ) {
        const value = parseFloat(formData[col.field])
        if (isNaN(value)) {
          newErrors[col.field] = 'Must be a valid number'
        } else {
          // Check min/max constraints if defined
          if (col.minValue !== undefined && value < col.minValue) {
            newErrors[col.field] = `Must be at least ${col.minValue}`
          }
          if (col.maxValue !== undefined && value > col.maxValue) {
            newErrors[col.field] = `Must be at most ${col.maxValue}`
          }
        }
      }
    })
    console.log('newErrors', newErrors)
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = () => {
    console.log('handleSubmit', formData)
    if (validateForm()) {
      onSubmit(formData)
      onClose()
    }
  }

  const handleCancel = () => {
    onClose()
  }

  // Get editable columns excluding id
  const editableColumns = columns.filter(
    (col) => col.editable && col.field !== 'id' && !col.hidden,
  )

  // Helper to check if field is required based on table type
  const isRequiredField = (field) => {
    switch (tableKey) {
      case 'CrudeBlendWindow':
        return ['type', 'property'].includes(field)
      case 'CrudeSpecificConstraints':
        return ['crude'].includes(field)
      case 'VGOVRDrop':
        return ['kbpsd'].includes(field)
      default:
        return field === 'type'
    }
  }

  const renderField = (column) => {
    const { field, title, type, minValue, maxValue } = column
    const isRequired = isRequiredField(field)

    // Type field - special handling with dropdown
    if (field === 'type') {
      console.log('groupTypes in dialog:', groupTypes)
      const typeOptions = [...groupTypes, 'Other']
      return (
        <Grid item xs={6} key={field}>
          <TextField
            select
            fullWidth
            label={title}
            value={selectedType}
            onChange={(e) => handleTypeChange(e.target.value)}
            error={!!errors[field]}
            helperText={errors[field]}
            required={isRequired}
            size='small'
          >
            {typeOptions.map((option) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </TextField>
          {selectedType === 'Other' && (
            <TextField
              fullWidth
              label='Enter Custom Type'
              value={customType}
              onChange={(e) => handleCustomTypeChange(e.target.value)}
              error={!!errors[field]}
              helperText={errors[field]}
              required={isRequired}
              size='small'
              sx={{ mt: 1 }}
            />
          )}
        </Grid>
      )
    }

    // Remarks/Reasons fields - textarea
    if (field === 'remarks' || field === 'reasons') {
      return (
        <Grid item xs={12} key={field}>
          <TextField
            fullWidth
            label={title}
            value={formData[field] || ''}
            onChange={(e) => handleFieldChange(field, e.target.value)}
            multiline
            rows={3}
            size='small'
          />
        </Grid>
      )
    }

    // Number fields
    if (type === 'number1' || type === 'number') {
      const inputProps = {
        step: 'any',
      }
      if (minValue !== undefined) inputProps.min = minValue
      if (maxValue !== undefined) inputProps.max = maxValue

      return (
        <Grid item xs={6} key={field}>
          <TextField
            fullWidth
            label={title}
            type='number'
            value={formData[field] || ''}
            onChange={(e) => handleFieldChange(field, e.target.value)}
            error={!!errors[field]}
            helperText={errors[field]}
            required={isRequired}
            size='small'
            inputProps={inputProps}
          />
        </Grid>
      )
    }

    // Text fields (default)
    return (
      <Grid item xs={6} key={field}>
        <TextField
          fullWidth
          label={title}
          value={formData[field] || ''}
          onChange={(e) => handleFieldChange(field, e.target.value)}
          error={!!errors[field]}
          helperText={errors[field]}
          required={isRequired}
          size='small'
        />
      </Grid>
    )
  }

  return (
    <Dialog open={open} onClose={handleCancel} maxWidth='sm' fullWidth>
      <DialogTitle>Add New Row</DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 2 }}>
          <Grid container spacing={2}>
            {editableColumns.map((column) => renderField(column))}
          </Grid>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancel} color='secondary'>
          Cancel
        </Button>
        <Button onClick={handleSubmit} variant='contained' color='primary'>
          Add
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddRowDialog
