import React from 'react';

import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import Grid from '@mui/material/Grid';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';

import CloseIcon from '@mui/icons-material/Close';
import { Box } from '@mui/material';

import MainCard from 'components/MainCard';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const BpmEngineC7Form = ({ bpmEngine, setBpmEngine, open, handleClose }) => {
    const handleInputChange = (event) => {
        setBpmEngine({ ...bpmEngine, [event.target.name]: event.target.value });
    };

    const handleInputParamChange = (event) => {
        setBpmEngine({ ...bpmEngine, parameters: { ...bpmEngine.parameters, [event.target.name]: event.target.value } });
    };

    const onSave = () => {
        if (bpmEngine.mode && bpmEngine.mode === 'new') {
            fetch(process.env.REACT_APP_API_URL + '/bpm-engine/', {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(bpmEngine)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        } else {
            fetch(process.env.REACT_APP_API_URL + '/bpm-engine/' + bpmEngine.id, {
                method: 'PATCH',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(bpmEngine)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        }
    };

    const handleDelete = () => {
        fetch(process.env.REACT_APP_API_URL + '/bpm-engine/' + bpmEngine.id, {
            method: 'DELETE',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        })
            .then((response) => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

    return (
        <div>
            <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition}>
                <AppBar sx={{ position: 'relative' }}>
                    <Toolbar>
                        <IconButton edge="start" color="inherit" onClick={handleClose} aria-label="close">
                            <CloseIcon />
                        </IconButton>
                        <Typography sx={{ ml: 2, flex: 1 }} component="div">
                            <div>{bpmEngine.id}</div>
                        </Typography>
                        <Button color="inherit" onClick={onSave}>
                            Save
                        </Button>
                        {!(bpmEngine.mode && bpmEngine.mode === 'new') && (
                            <Button color="inherit" onClick={handleDelete}>
                                Delete
                            </Button>
                        )}
                    </Toolbar>
                </AppBar>

                <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                    <Grid item xs={12}>
                        <MainCard sx={{ p: 2 }} content={true}>
                            <Box sx={{ pb: 1, display: 'flex', flexDirection: 'column' }}>
                                <FormControl key="ctrlId" style={{ padding: '5px' }}>
                                    <TextField
                                        id="txtId"
                                        aria-describedby="key-helper-text"
                                        value={bpmEngine.id}
                                        name="id"
                                        onChange={handleInputChange}
                                        disabled={!(bpmEngine.mode && bpmEngine.mode === 'new')}
                                    />
                                    <FormHelperText id="id-helper-text">Id</FormHelperText>
                                </FormControl>
                                <FormControl key="ctrlName" style={{ padding: '5px' }}>
                                    <TextField
                                        id="txtName"
                                        aria-describedby="name-helper-text"
                                        value={bpmEngine.name}
                                        name="name"
                                        onChange={handleInputChange}
                                    />
                                    <FormHelperText id="name-helper-text">Name</FormHelperText>
                                </FormControl>
                                <FormControl key="ctrlType" style={{ padding: '5px' }}>
                                    <TextField
                                        id="txtType"
                                        aria-describedby="type-helper-text"
                                        value={bpmEngine.type}
                                        name="type"
                                        onChange={handleInputChange}
                                        disabled
                                    />
                                    <FormHelperText id="type-helper-text">Type</FormHelperText>
                                </FormControl>
                            </Box>
                            <FormControl key="ctrlParamUrl" style={{ padding: '5px' }}>
                                <TextField
                                    id="txtParamUrl"
                                    aria-describedby="paramUrl-helper-text"
                                    value={bpmEngine.parameters.url}
                                    name="url"
                                    onChange={handleInputParamChange}
                                />
                                <FormHelperText id="paramUrl-helper-text">Url</FormHelperText>
                            </FormControl>
                        </MainCard>
                    </Grid>
                </Grid>
            </Dialog>
        </div>
    );
};
