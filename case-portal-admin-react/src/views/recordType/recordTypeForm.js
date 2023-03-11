import React from 'react';
import CloseIcon from '@mui/icons-material/Close';
import { AppBar, Box, Button, Dialog, Grid, IconButton, Slide, Toolbar } from '@mui/material';
import Typography from '@mui/material/Typography';
import { FormBuilder } from '@formio/react';
import { TextField } from '@mui/material';
import MainCard from 'components/MainCard';
import { RecordTypeService } from 'services';
import { useSession } from 'SessionStoreContext';

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const RecordTypeForm = ({ open, handleClose, recordType, handleInputChange }) => {
    const keycloack = useSession();

    const save = () => {
        if (recordType.mode && recordType.mode === 'new') {
            RecordTypeService.create(keycloack, recordType)
                .then(() => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        } else {
            RecordTypeService.update(keycloack, recordType.id, recordType)
                .then(() => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        }
    };

    const deleteRecordType = () => {
        RecordTypeService.remove(keycloack, recordType.id)
            .then(() => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

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
                            edge="start"
                            color="inherit"
                            onClick={handleClose}
                            aria-label="close"
                        >
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
