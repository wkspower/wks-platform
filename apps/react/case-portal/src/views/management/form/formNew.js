import React, { useEffect, useState } from 'react'
import FormControl from '@mui/material/FormControl'
import TextField from '@mui/material/TextField'
import AppBar from '@mui/material/AppBar'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import Grid from '@mui/material/Grid'
import IconButton from '@mui/material/IconButton'
import InputLabel from '@mui/material/InputLabel'
import Slide from '@mui/material/Slide'
import Toolbar from '@mui/material/Toolbar'
import CloseIcon from '@mui/icons-material/Close'
import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import MainCard from 'components/MainCard'
import { FormBuilder } from '@formio/react'
import { FormService } from 'services'
import { useSession } from 'SessionStoreContext'
import { StorageService } from 'plugins/storage'

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const FormNew = ({ open, handleClose }) => {
  const [form, setForm] = useState(null)
  const keycloak = useSession()

  useEffect(() => {
    setForm({
      key: '',
      description: '',
      structure: { components: [], display: 'form' },
    })
  }, [open])

  const saveNewForm = () => {
    FormService.create(keycloak, form)
      .then(() => handleClose())
      .catch((err) => {
        console.log(err.message)
      })
  }

  const handleInputChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  const handleSelectDisplay = (event) => {
    let structure = { ...form.structure }
    structure.display = event.target.value
    setForm({ ...form, structure: structure })
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
              <div>New Form</div>
            </Typography>
            <Button color='inherit' onClick={saveNewForm}>
              Save
            </Button>
          </Toolbar>
        </AppBar>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <Grid container spacing={1} alignItems='center'>
              <Grid item>
                <TextField
                  id='txtKey'
                  name='key'
                  value={form.key}
                  label='Form key'
                  onChange={handleInputChange}
                />
              </Grid>
              <Grid item>
                <TextField
                  id='txtTitle'
                  name='title'
                  value={form.title}
                  label='Title'
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
              options={{
                noNewEdit: true,
                noDefaultSubmitButton: true,
                fileService: new StorageService(),
              }}
            />
          </MainCard>
        </Box>
      </Dialog>
    )
  )
}