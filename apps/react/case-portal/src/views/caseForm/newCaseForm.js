import { useState } from 'react'
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
import React, { useEffect } from 'react'
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
  }, [open, caseDefId])

  const onSave = () => {
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
            <Form
              form={form.structure}
              submission={formData}
              options={{
                fileService: new StorageService(),
              }}
            />
          </Grid>
        </Grid>
      </Dialog>
    </div>
  )
}
