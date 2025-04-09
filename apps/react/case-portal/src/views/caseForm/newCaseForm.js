import { useState, useRef, useEffect } from 'react'
import { QuestionCircleOutlined } from '@ant-design/icons'
import CloseIcon from '@mui/icons-material/Close'
import { Box, Tooltip } from '@mui/material'
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
import ValidationErrorAlert from '../../components/FormValidation/ValidationErrorAlert'
import { validateForm } from '../../utils/formValidation'

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
  const [formioInstance, setFormioInstance] = useState(null)
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

  const handleAlertClose = () => {
    setAlertOpen(false)
  }

  const onChange = (newFormData) => {
    setFormData(newFormData)
  }

  // Handle form initialization
  const onFormInit = (instance) => {
    console.log("Form initialized:", instance)
    setFormioInstance(instance)
  }

  const onSave = () => {
    console.log("Form instance on save:", formioInstance)
    
    // Validate the form using our external validation utility
    const validationResult = validateForm(
      formioInstance, 
      form.structure, 
      formData
    )
    
    if (!validationResult.isValid) {
      setValidationErrors(validationResult.errors)
      setAlertOpen(true)
    } else {
      saveFormData()
    }
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
            
            <ValidationErrorAlert 
              errors={validationErrors}
              open={alertOpen}
              onClose={handleAlertClose}
            />
            
            <Form
              ref={formRef}
              form={form.structure}
              submission={formData}
              onChange={onChange}
              onInit={onFormInit}
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