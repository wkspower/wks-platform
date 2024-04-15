import CloseIcon from '@mui/icons-material/Close';
import { FormControl, TextField } from '@mui/material';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import { useSession } from 'SessionStoreContext';
import MainCard from 'components/MainCard';
import React from 'react';
import { QueueService } from 'services/QueueService';

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />;
});

export const QueueForm = ({ open, handleClose, queue, handleInputChange }) => {
  const keycloak = useSession();

  const save = () => {
    if (queue.mode && queue.mode === 'new') {
      QueueService.save(keycloak, queue)
        .then(() => handleClose())
        .catch((err) => {
          console.log(err.message);
        });
    } else {
      QueueService.update(keycloak, queue.id, queue)
        .then(() => handleClose())
        .catch((err) => {
          console.log(err.message);
        });
    }
  };

  const deleteQueue = () => {
    QueueService.remove(keycloak, queue.id)
      .then(() => handleClose())
      .catch((err) => {
        console.log(err.message);
      });
  };

  return (
    queue && (
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
              <div>{queue?.id}</div>
            </Typography>
            <Button color='inherit' onClick={save}>
              Save
            </Button>
            <Button color='inherit' onClick={deleteQueue}>
              Delete
            </Button>
          </Toolbar>
        </AppBar>

        <Box sx={{ p: 1 }}>
          <MainCard>
            <FormControl key='ctrlId' sx={{ p: 1 }}>
              <TextField
                id='txtId'
                name='id'
                value={queue.id}
                label='Id'
                onChange={handleInputChange}
                disabled={!(queue.mode && queue.mode === 'new')}
              />
            </FormControl>

            <FormControl key='ctrlName' sx={{ p: 1 }}>
              <TextField
                id='txtName'
                name='name'
                value={queue.name}
                label='Name'
                onChange={handleInputChange}
              />
            </FormControl>

            <FormControl key='ctrlDescription' sx={{ p: 1 }}>
              <TextField
                id='txtDescription'
                name='description'
                value={queue.description}
                label='Description'
                onChange={handleInputChange}
              />
            </FormControl>
          </MainCard>
        </Box>
      </Dialog>
    )
  );
};
