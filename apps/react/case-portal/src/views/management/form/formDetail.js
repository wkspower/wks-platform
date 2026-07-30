import React from 'react'
import CloseIcon from '@mui/icons-material/Close'
import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import FormControl from '@mui/material/FormControl'
import Grid from '@mui/material/Grid'
import IconButton from '@mui/material/IconButton'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import Slide from '@mui/material/Slide'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import { FormBuilder } from '@formio/react'
import { TextField } from '@mui/material'
import MainCard from 'components/MainCard'
import { FormService } from 'services'
import { useSession } from 'SessionStoreContext'
import { getFormioOptions } from 'components/@formio/formioOptions'
import { useFormBuilderSchema } from '../useFormBuilderSchema'

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const FormDetail = ({
  open,
  handleClose,
  form,
  handleInputChange,
  handleSelectDisplay,
}) => {
  const keycloak = useSession()
  const { onBuilderChange, mergeBuilderSchema } = useFormBuilderSchema(open)

  const saveForm = () => {
    // The components live in the builder, not in state — pull them in on save.
    FormService.update(keycloak, form.key, {
      ...form,
      structure: mergeBuilderSchema(form.structure),
    })
      .then(() => handleClose())
      .catch((err) => {
        console.log(err.message)
      })
  }

  const deleteForm = () => {
    FormService.remove(keycloak, form.key)
      .then(() => handleClose())
      .catch((err) => {
        console.log(err.message)
      })
  }

  return (
    form && (
      <Dialog
        fullScreen
        open={open}
        onClose={handleClose}
        TransitionComponent={Transition}
        disableEnforceFocus={true}
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
              <div>{form?.title}</div>
            </Typography>
            <Button color='inherit' onClick={saveForm}>
              Save
            </Button>
            <Button color='inherit' onClick={deleteForm}>
              Delete
            </Button>
          </Toolbar>
        </AppBar>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <Grid container spacing={1}>
              <Grid item>
                <TextField
                  id='txtKey'
                  name='key'
                  value={form.key}
                  label='Form key'
                  onChange={handleInputChange}
                  disabled
                />
              </Grid>
              <Grid item>
                <TextField
                  id='txtTitle'
                  name='title'
                  value={form.title}
                  label='title'
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item>
                <TextField
                  id='txtToolTip'
                  name='toolTip'
                  value={form.toolTip}
                  label='Tool Tip'
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item>
                <FormControl fullWidth>
                  <InputLabel id='sltDisplay-label'>Display</InputLabel>
                  <Select
                    id='sltDisplay-label'
                    name='display'
                    value={form.structure.display}
                    label='Display'
                    onChange={handleSelectDisplay}
                  >
                    <MenuItem value='form'>Form</MenuItem>
                    <MenuItem value='wizard'>Wizard</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </MainCard>
        </Box>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <FormBuilder
              form={form.structure}
              onChange={onBuilderChange}
              options={getFormioOptions({
                noNewEdit: true,
                noDefaultSubmitButton: true,
              })}
            />
          </MainCard>
        </Box>
      </Dialog>
    )
  )
}
