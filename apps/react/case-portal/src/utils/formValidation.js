/**
 * Form validation utility functions for validating form data against defined rules
 * and providing internationalized error messages.
 */
import i18next from 'i18next'

/**
 * Checks if a value is empty (undefined, null, empty string, empty array, empty object)
 */
export const isEmpty = (value) => {
  return (
    value === undefined ||
    value === null ||
    value === '' ||
    (Array.isArray(value) && value.length === 0) ||
    (typeof value === 'object' && Object.keys(value).length === 0)
  )
}

/**
 * Gets a nested value from an object using a dot-separated path
 */
export const getNestedValue = (obj, path) => {
  if (!obj || !path) return undefined
  const parts = path.split('.')
  let value = obj

  for (const part of parts) {
    if (value === undefined || value === null) return undefined
    value = value[part]
  }

  return value
}

/**
 * Validates an email address using regex
 */
export const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * Returns validation error message based on component type and validation rules
 */
export const getValidationMessage = (component, value) => {
  if (isEmpty(value)) return null

  if (component.type === 'email' && !isValidEmail(value)) {
    return i18next.t(
      'pages.validation.invalidEmail',
      'Please enter a valid email address',
    )
  }

  if (component.validate) {
    if (
      component.validate.min !== undefined &&
      typeof value === 'number' &&
      value < component.validate.min
    ) {
      return i18next.t(
        'pages.validation.minValue',
        'Minimum value is {{min}}',
        {
          min: component.validate.min,
        },
      )
    }

    if (
      component.validate.max !== undefined &&
      typeof value === 'number' &&
      value > component.validate.max
    ) {
      return i18next.t(
        'pages.validation.maxValue',
        'Maximum value is {{max}}',
        {
          max: component.validate.max,
        },
      )
    }

    if (component.validate.pattern && typeof value === 'string') {
      const pattern = new RegExp(component.validate.pattern)
      if (!pattern.test(value)) {
        return (
          component.validate.patternMessage ||
          i18next.t('pages.validation.pattern', 'Invalid format')
        )
      }
    }

    if (
      component.validate.custom &&
      typeof component.validate.custom === 'string'
    ) {
      try {
        const customValidator = new Function('input', component.validate.custom)
        const result = customValidator(value)
        if (result !== true) {
          return (
            component.validate.customMessage ||
            i18next.t('pages.validation.custom', 'Invalid value')
          )
        }
      } catch (err) {
        console.log('Error in custom validation:', err)
      }
    }
  }

  switch (component.type) {
    case 'number':
      if (isNaN(value)) {
        return i18next.t(
          'pages.validation.notANumber',
          'Please enter a valid number',
        )
      }
      break
    case 'date':
      if (!(value instanceof Date) && isNaN(Date.parse(value))) {
        return i18next.t(
          'pages.validation.invalidDate',
          'Please enter a valid date',
        )
      }
      break
    case 'url':
      try {
        new URL(value)
      } catch (e) {
        return i18next.t(
          'pages.validation.invalidUrl',
          'Please enter a valid URL',
        )
      }
      break
  }

  return null
}

/**
 * Extracts validation errors from form structure and data, returning a list of field errors
 */
export const extractValidationErrors = (formStructure, formData) => {
  const errorsList = []

  if (!formStructure || !formStructure.components) {
    return errorsList
  }

  const checkFields = (components, path = '') => {
    if (!components) return

    components.forEach((component) => {
      if (component.key) {
        const fieldKey = component.key
        const fieldPath = path ? `${path}.${fieldKey}` : fieldKey
        const label = component.label || fieldKey
        const value = getNestedValue(formData?.data || {}, fieldPath)

        if (
          component.validate &&
          component.validate.required &&
          isEmpty(value)
        ) {
          errorsList.push({
            field: label,
            key: fieldPath,
            message: i18next.t(
              'pages.validation.requiredField',
              'This field is required',
            ),
          })
        }

        if (
          component.validate &&
          component.validate.minLength &&
          typeof value === 'string' &&
          value.length < component.validate.minLength
        ) {
          errorsList.push({
            field: label,
            key: fieldPath,
            message: i18next.t(
              'pages.validation.minLength',
              'Minimum length is {{length}} characters',
              {
                length: component.validate.minLength,
              },
            ),
          })
        }

        if (
          component.validate &&
          component.validate.maxLength &&
          typeof value === 'string' &&
          value.length > component.validate.maxLength
        ) {
          errorsList.push({
            field: label,
            key: fieldPath,
            message: i18next.t(
              'pages.validation.maxLength',
              'Maximum length is {{length}} characters',
              {
                length: component.validate.maxLength,
              },
            ),
          })
        }

        const validationMessage = getValidationMessage(component, value)
        if (validationMessage) {
          errorsList.push({
            field: label,
            key: fieldPath,
            message: validationMessage,
          })
        }
      }

      if (component.components) {
        checkFields(component.components, component.key)
      }
    })
  }

  try {
    checkFields(formStructure.components)
  } catch (err) {
    console.log('Error during validation:', err)
  }

  return errorsList
}

/**
 * Force displays component errors by triggering validation UI states
 */
export const forceShowComponentErrors = (components) => {
  if (!components) return

  components.forEach((comp) => {
    try {
      if (typeof comp.setPristine === 'function') {
        comp.setPristine(false)
      }

      if (typeof comp.showError === 'function') {
        comp.showError()
      }

      if (typeof comp.setDirty === 'function') {
        comp.setDirty(true)
      }

      if (typeof comp.triggerChange === 'function') {
        comp.triggerChange()
      }

      if (comp.refs && comp.refs.input) {
        try {
          const event = new Event('blur', { bubbles: true })
          comp.refs.input.dispatchEvent(event)
        } catch (e) {
          console.log('Error dispatching blur event:', e)
        }
      }

      if (comp.components) {
        forceShowComponentErrors(comp.components)
      }
    } catch (err) {
      console.log('Error handling component:', err)
    }
  })
}

/**
 * Creates a generic error message for fields with validation errors
 */
export const handleGenericValidationError = (component, label) => {
  return i18next.t(
    'pages.validation.genericError',
    'The {{field}} field has an invalid value. Please review and correct.',
    { field: label },
  )
}

/**
 * Validates a form and returns validation status and error list
 */
export const validateForm = (formioInstance, formStructure, formData) => {
  let isValid = true
  let errors = []

  if (formioInstance) {
    isValid = formioInstance.checkValidity()

    if (!isValid) {
      forceShowComponentErrors(formioInstance.components)

      if (typeof formioInstance.showErrors === 'function') {
        formioInstance.showErrors()
      }
    }
  }

  errors = extractValidationErrors(formStructure, formData)

  if (errors.length > 0) {
    isValid = false
  }

  return {
    isValid,
    errors,
  }
}
