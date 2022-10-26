import React from 'react';

import CloseIcon from '@mui/icons-material/Close';
import { AppBar, Box, Button, Dialog, Grid, IconButton, Slide, Toolbar, TransitionProps } from '@mui/material';
import Typography from '@mui/material/Typography';

import { FormBuilder } from '@formio/react';

import { TextField } from '@mui/material';
import MainCard from 'components/MainCard';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const RecordTypeForm = ({ open, handleClose, recordType, handleInputChange }) => {
    const save = () => {
        if (recordType.mode && recordType.mode === 'new') {
            fetch('http://localhost:8081/record-type/', {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(recordType)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        } else {
            fetch('http://localhost:8081/record-type/' + recordType.id, {
                method: 'PATCH',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(recordType)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        }
    };

    const deleteRecordType = () => {
        fetch('http://localhost:8081/record-type/' + recordType.id, {
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
        recordType && (
            <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition} disableEnforceFocus={true}>
                {/* disableEnforceFocus: https://mui.com/material-ui/api/modal/#props - if false, formio component forms are unfocusable */}

                <AppBar sx={{ position: 'relative' }}>
                    <Toolbar>
                        <IconButton edge="start" color="inherit" onClick={handleClose} aria-label="close">
                            <CloseIcon />
                        </IconButton>
                        <Typography sx={{ ml: 2, flex: 1 }} component="div">
                            <div>{recordType?.id}</div>
                        </Typography>
                        <Button color="inherit" onClick={save}>
                            Save
                        </Button>
                        <Button color="inherit" onClick={deleteRecordType}>
                            Delete
                        </Button>
                    </Toolbar>
                </AppBar>

                <Box sx={{ p: 1 }}>
                    <MainCard>
                        <Grid container spacing={1}>
                            <Grid item>
                                <TextField
                                    id="txtId"
                                    name="id"
                                    value={recordType.id}
                                    label="Id"
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
                                // builder: {
                                //     premium: false
                                // },
                                noNewEdit: true,
                                noDefaultSubmitButton: true
                            }}
                        />
                    </MainCard>
                </Box>
            </Dialog>
        )
    );
};
