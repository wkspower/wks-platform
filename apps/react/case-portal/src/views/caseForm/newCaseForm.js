import { useState, useRef, useEffect } from 'react'
import { QuestionCircleOutlined } from '@ant-design/icons'
import CloseIcon from '@mui/icons-material/Close'
import { Box, Tooltip, Alert } from '@mui/material'
import AppBar from '@mui/material/AppBar'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import Grid from '@mui/material/Grid'
import IconButton from '@mui/material/IconButton'
import Slide from '@mui/material/Slide'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import React from 'react'
import { Form } from '@formio/react'
import { useSession } from 'SessionStoreContext'
import { CaseService, FormService } from '../../services'
import { StorageService } from 'plugins/storage'

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const NewCaseForm = ({
  open,
  handleClose,
  caseDefId,
  setLastCreatedCase,
}) => {
  const [caseDef, setCaseDef] = useState([])
  const [form, setForm] = useState([])
  const [formData, setFormData] = useState(null)
  const [alertOpen, setAlertOpen] = useState(false)
  const [validationErrors, setValidationErrors] = useState([])
  const formRef = useRef(null)
  const keycloak = useSession()

  useEffect(() => {
    CaseService.getCaseDefinitionsById(keycloak, caseDefId)
      .then((data) => {
        setCaseDef(data)
        return FormService.getByKey(keycloak, data.formKey)
      })
      .then((data) => {
        setForm(data)
        setFormData({
          data: {},
          metadata: {},
          isValid: true,
        })
      })
      .catch((err) => {
        console.log(err.message)
      })
  }, [open, caseDefId, keycloak])

  useEffect(() => {
    console.log("Form ref updated:", formRef.current)
  }, [formRef.current])

  const handleAlertClose = () => {
    setAlertOpen(false)
  }

  const onChange = (newFormData) => {
    setFormData(newFormData)
  }

  const extractValidationErrors = () => {
    const errorsList = []
    
    // If we have no form structure, we can't validate
    if (!form.structure || !form.structure.components) {
      return errorsList
    }
    
    const isEmpty = (value) => {
      return value === undefined || value === null || value === '' || 
        (Array.isArray(value) && value.length === 0) ||
        (typeof value === 'object' && Object.keys(value).length === 0)
    }
    
    const checkRequiredFields = (components, path = '') => {
      if (!components) return
      
      components.forEach(component => {
        if (component.key) {
          const fieldKey = component.key
          const fieldPath = path ? `${path}.${fieldKey}` : fieldKey
          const label = component.label || fieldKey
          
          if (component.validate && component.validate.required) {
            const value = getNestedValue(formData?.data || {}, fieldPath)
            
            if (isEmpty(value)) {
              errorsList.push({
                field: label,
                key: fieldPath,
                message: 'This field is required'
              })
            }
          }
          
          if (component.validate) {
            const value = getNestedValue(formData?.data || {}, fieldPath)
            
            if (!isEmpty(value)) {
              if (component.validate.minLength && typeof value === 'string' && 
                  value.length < component.validate.minLength) {
                errorsList.push({
                  field: label,
                  key: fieldPath,
                  message: `Minimum length is ${component.validate.minLength} characters`
                })
              }
              
              if (component.validate.maxLength && typeof value === 'string' && 
                  value.length > component.validate.maxLength) {
                errorsList.push({
                  field: label, 
                  key: fieldPath,
                  message: `Maximum length is ${component.validate.maxLength} characters`
                })
              }
              
              if (component.validate.pattern && typeof value === 'string') {
                const pattern = new RegExp(component.validate.pattern)
                if (!pattern.test(value)) {
                  errorsList.push({
                    field: label,
                    key: fieldPath,
                    message: component.validate.patternMessage || 'Invalid format'
                  })
                }
              }
            }
          }
        }
        
        if (component.components) {
          checkRequiredFields(component.components, component.key)
        }
      })
    }
    
    const getNestedValue = (obj, path) => {
      if (!obj || !path) return undefined
      const parts = path.split('.')
      let value = obj
      for (const part of parts) {
        if (value === undefined || value === null) return undefined
        value = value[part]
      }
      return value
    }
    
    try {
      checkRequiredFields(form.structure.components)
    } catch (err) {
      console.log("Error during validation:", err)
    }
    
    return errorsList
  }

  const onSave = () => {
    console.log("Form ref on save:", formRef.current)
    
    // Check if we have direct access to the form instance
    if (formRef.current && formRef.current.formio) {
      const formioInstance = formRef.current.formio
      
      const isValid = formioInstance.checkValidity()
      
      if (!isValid) {
        forceShowComponentErrors(formioInstance.components)
        
        if (typeof formioInstance.showErrors === 'function') {
          formioInstance.showErrors()
        }
        
        const validationErrorsList = extractValidationErrors()
        setValidationErrors(validationErrorsList)
        setAlertOpen(true)
      } else {
        saveFormData()
      }
    } else {
      // If we can't access the form ref directly, use an alternative validation approach
      // Check the structure directly against current form data
      const validationErrorsList = extractValidationErrors()
      
      if (validationErrorsList.length > 0) {
        setValidationErrors(validationErrorsList)
        setAlertOpen(true)
      } else if (formData && formData.isValid !== false) {
        // If no errors were found and formData doesn't explicitly say it's invalid, proceed
        saveFormData()
      } else {
        setAlertOpen(true)
        setValidationErrors([{
          field: "Form",
          message: "There are validation errors in this form."
        }])
      }
    }
  }
  
  const forceShowComponentErrors = (components) => {
    if (!components) return
    
    components.forEach(comp => {
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
  
  const saveFormData = () => {
    const caseAttributes = []
    Object.keys(formData.data).forEach((key) => {
      caseAttributes.push({
        name: key,
        value:
          typeof formData.data[key] !== 'object'
            ? formData.data[key]
            : JSON.stringify(formData.data[key]),
        type: typeof formData.data[key] !== 'object' ? 'String' : 'Json',
      })
    })

    CaseService.createCase(
      keycloak,
      JSON.stringify({
        caseDefinitionId: caseDefId,
        owner: {
          id: keycloak.subject || '',
          name: keycloak.idTokenParsed.name || '',
          email: keycloak.idTokenParsed.email || '',
          phone: keycloak.idTokenParsed.phone || '',
        },
        attributes: caseAttributes,
      }),
    )
      .then((data) => {
        setLastCreatedCase(data)
        handleClose()
      })
      .catch((err) => {
        console.log(err.message)
        setAlertOpen(true)
      })
  }

  return (
    <div>
      <Dialog
        fullScreen
        open={open}
        onClose={handleClose}
        TransitionComponent={Transition}
      >
        <AppBar sx={{ position: 'relative' }}>
          <Toolbar>
            <IconButton
              edge='start'
              color='inherit'
              onClick={handleClose}
              aria-label='close'
            >
              <CloseIcon />
            </IconButton>
            <Typography sx={{ ml: 2, flex: 1 }} component='div'>
              <div>{caseDef.name}</div>
            </Typography>
            <Button color='inherit' onClick={onSave}>
              Save
            </Button>
          </Toolbar>
        </AppBar>

        <Grid
          container
          spacing={2}
          sx={{ display: 'flex', flexDirection: 'column' }}
        >
          <Grid item xs={12} sx={{ m: 3 }}>
            <Box sx={{ pb: 1, display: 'flex', flexDirection: 'row' }}>
              <Typography variant='h5' color='textSecondary' sx={{ pr: 0.5 }}>
                {form.title}
              </Typography>
              {form.toolTip && (
                <Tooltip title={form.toolTip}>
                  <QuestionCircleOutlined />
                </Tooltip>
              )}
            </Box>
            
            {alertOpen && (
              <Alert 
                severity="error" 
                sx={{ mb: 2 }}
                onClose={() => setAlertOpen(false)}
              >
                <Typography variant="subtitle1" fontWeight="bold">
                  Please correct the following errors in the form:
                </Typography>
                {validationErrors.length > 0 ? (
                  <ul style={{ marginTop: '8px', paddingLeft: '20px' }}>
                    {validationErrors.map((error, index) => (
                      <li key={index}>
                        <strong>{error.field}:</strong> {error.message}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Typography>
                    Required fields are missing or contain invalid values.
                  </Typography>
                )}
              </Alert>
            )}
            
            <Form
              ref={formRef}
              form={form.structure}
              submission={formData}
              onChange={onChange}
              options={{
                fileService: new StorageService(),
                validateOnInit: false,
                validate: true,
                showErrors: true,
                highlightErrors: true,
                redrawOn: 'change',
                errors: {
                  message: 'Please fix the following errors before submitting.'
                },
                noAlerts: false,
                displayErrorsFor: ['validations', 'conditions', 'required'],
                alerts: {
                  submitMessage: false
                },
                buttonSettings: {
                  showSubmit: false
                }
              }}
            />
          </Grid>
        </Grid>
      </Dialog>
    </div>
  )
}