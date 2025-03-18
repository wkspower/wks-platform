import React from 'react'
import CloseIcon from '@mui/icons-material/Close'
import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import Grid from '@mui/material/Grid'
import IconButton from '@mui/material/IconButton'
import Slide from '@mui/material/Slide'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import { FormBuilder } from '@formio/react'
import { TextField } from '@mui/material'
import MainCard from 'components/MainCard'
import { RecordTypeService, MenuEventService} from 'services'
import { useSession } from 'SessionStoreContext'
import { StorageService } from 'plugins/storage'

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const RecordTypeForm = ({
  open,
  handleClose,
  recordType,
  handleInputChange,
}) => {
  const keycloak = useSession()

  const save = () => {
    if (recordType.mode && recordType.mode === 'new') {
      RecordTypeService.create(keycloak, recordType)
        .then(() => {
          MenuEventService.triggerMenuUpdate();
          handleClose();
        })
        .catch((err) => {
          console.log(err.message)
        })
    } else {
      RecordTypeService.update(keycloak, recordType.id, recordType)
        .then(() => {
          MenuEventService.triggerMenuUpdate();
          handleClose();
        })
        .catch((err) => {
          console.log(err.message)
        })
    }
  }

  const deleteRecordType = () => {
    RecordTypeService.remove(keycloak, recordType.id)
      .then(() => {
        MenuEventService.triggerMenuUpdate();
        handleClose();
      })
      .catch((err) => {
        console.log(err.message)
      })
  }

  return (
    recordType && (
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
              <div>{recordType?.id}</div>
            </Typography>
            <Button color='inherit' onClick={save}>
              Save
            </Button>
            <Button color='inherit' onClick={deleteRecordType}>
              Delete
            </Button>
          </Toolbar>
        </AppBar>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <Grid container spacing={1}>
              <Grid item>
                <TextField
                  id='txtId'
                  name='id'
                  value={recordType.id}
                  label='Id'
                  onChange={handleInputChange}
                  disabled={!(recordType.mode && recordType.mode === 'new')}
                />
              </Grid>
            </Grid>
          </MainCard>
        </Box>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <FormBuilder
              form={recordType.fields}
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
