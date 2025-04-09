/**
 * Utility functions for form validation
 */

/**
 * Checks if a value is empty (undefined, null, empty string, empty array, empty object)
 * @param {any} value - The value to check
 * @returns {boolean} - True if the value is empty, false otherwise
 */
export const isEmpty = (value) => {
    return value === undefined || value === null || value === '' || 
      (Array.isArray(value) && value.length === 0) ||
      (typeof value === 'object' && Object.keys(value).length === 0);
  };
  
  /**
   * Gets a nested value from an object using a dot-separated path
   * @param {object} obj - The object to extract the value from
   * @param {string} path - Dot-separated path to the value
   * @returns {any} - The value at the path or undefined if not found
   */
  export const getNestedValue = (obj, path) => {
    if (!obj || !path) return undefined;
    const parts = path.split('.');
    let value = obj;
    
    for (const part of parts) {
      if (value === undefined || value === null) return undefined;
      value = value[part];
    }
    
    return value;
  };
  
  /**
   * Extract validation errors from a form based on its structure and current data
   * @param {object} formStructure - The form structure containing components and validation rules
   * @param {object} formData - The current form data
   * @returns {Array} - List of validation errors with field, key, and message
   */
  export const extractValidationErrors = (formStructure, formData) => {
    const errorsList = [];
    
    // If we have no form structure, we can't validate
    if (!formStructure || !formStructure.components) {
      return errorsList;
    }
    
    const checkRequiredFields = (components, path = '') => {
      if (!components) return;
      
      components.forEach(component => {
        if (component.key) {
          const fieldKey = component.key;
          const fieldPath = path ? `${path}.${fieldKey}` : fieldKey;
          const label = component.label || fieldKey;
          
          if (component.validate && component.validate.required) {
            const value = getNestedValue(formData?.data || {}, fieldPath);
            
            if (isEmpty(value)) {
              errorsList.push({
                field: label,
                key: fieldPath,
                message: 'This field is required'
              });
            }
          }
          
          if (component.validate) {
            const value = getNestedValue(formData?.data || {}, fieldPath);
            
            if (!isEmpty(value)) {
              if (component.validate.minLength && typeof value === 'string' && 
                  value.length < component.validate.minLength) {
                errorsList.push({
                  field: label,
                  key: fieldPath,
                  message: `Minimum length is ${component.validate.minLength} characters`
                });
              }
              
              if (component.validate.maxLength && typeof value === 'string' && 
                  value.length > component.validate.maxLength) {
                errorsList.push({
                  field: label, 
                  key: fieldPath,
                  message: `Maximum length is ${component.validate.maxLength} characters`
                });
              }
              
              if (component.validate.pattern && typeof value === 'string') {
                const pattern = new RegExp(component.validate.pattern);
                if (!pattern.test(value)) {
                  errorsList.push({
                    field: label,
                    key: fieldPath,
                    message: component.validate.patternMessage || 'Invalid format'
                  });
                }
              }
            }
          }
        }
        
        if (component.components) {
          checkRequiredFields(component.components, component.key);
        }
      });
    };
    
    try {
      checkRequiredFields(formStructure.components);
    } catch (err) {
      console.log("Error during validation:", err);
    }
    
    return errorsList;
  };
  
  /**
   * Force component errors to be displayed
   * @param {Array} components - Form components array
   */
  export const forceShowComponentErrors = (components) => {
    if (!components) return;
    
    components.forEach(comp => {
      try {
        if (typeof comp.setPristine === 'function') {
          comp.setPristine(false);
        }
        
        if (typeof comp.showError === 'function') {
          comp.showError();
        }
        
        if (typeof comp.setDirty === 'function') {
          comp.setDirty(true);
        }
        
        if (typeof comp.triggerChange === 'function') {
          comp.triggerChange();
        }
        
        if (comp.refs && comp.refs.input) {
          try {
            const event = new Event('blur', { bubbles: true });
            comp.refs.input.dispatchEvent(event);
          } catch (e) {
            console.log('Error dispatching blur event:', e);
          }
        }
        
        if (comp.components) {
          forceShowComponentErrors(comp.components);
        }
      } catch (err) {
        console.log('Error handling component:', err);
      }
    });
  };
  
  /**
   * Validate form and return validation result
   * @param {object} formioInstance - The formio instance
   * @param {object} formStructure - The form structure
   * @param {object} formData - The current form data
   * @returns {object} - Validation result with isValid flag and errors list
   */
  export const validateForm = (formioInstance, formStructure, formData) => {
    let isValid = true;
    let errors = [];
    
    // Try validating with formio instance first
    if (formioInstance) {
      isValid = formioInstance.checkValidity();
      
      if (!isValid) {
        forceShowComponentErrors(formioInstance.components);
        
        if (typeof formioInstance.showErrors === 'function') {
          formioInstance.showErrors();
        }
      }
    }
    
    // Always run our custom validation as backup or to get detailed errors
    errors = extractValidationErrors(formStructure, formData);
    
    // If we found errors in our custom validation, mark as invalid
    if (errors.length > 0) {
      isValid = false;
    }
    
    return {
      isValid,
      errors
    };
  };