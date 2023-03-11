import React from 'react';
import CloseIcon from '@mui/icons-material/Close';
import {
    AppBar,
    Box,
    Button,
    Dialog,
    FormControl,
    Grid,
    IconButton,
    InputLabel,
    MenuItem,
    Select,
    Slide,
    Toolbar
} from '@mui/material';
import Typography from '@mui/material/Typography';
import { FormBuilder } from '@formio/react';
import { TextField } from '@mui/material';
import MainCard from 'components/MainCard';
import { FormService } from 'services';
import { useSession } from 'SessionStoreContext';

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const FormDetail = ({ open, handleClose, form, handleInputChange, handleSelectDisplay }) => {
    const keycloack = useSession();

    const saveForm = () => {
        FormService.update(keycloack, form.key, form)
            .then(() => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

    const deleteForm = () => {
        FormService.remove(keycloack, form.key)
            .then(() => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

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
                            edge="start"
                            color="inherit"
                            onClick={handleClose}
                            aria-label="close"
                        >
                            <CloseIcon />
                        </IconButton>
                        <Typography sx={{ ml: 2, flex: 1 }} component="div">
                            <div>{form?.title}</div>
                        </Typography>
                        <Button color="inherit" onClick={saveForm}>
                            Save
                        </Button>
                        <Button color="inherit" onClick={deleteForm}>
                            Delete
                        </Button>
                    </Toolbar>
                </AppBar>

                <Box sx={{ p: 1 }}>
                    <MainCard>
                        <Grid container spacing={1}>
                            <Grid item>
                                <TextField
                                    id="txtKey"
                                    name="key"
                                    value={form.key}
                                    label="Form key"
                                    onChange={handleInputChange}
                                    disabled
                                />
                            </Grid>
                            <Grid item>
                                <TextField
                                    id="txtTitle"
                                    name="title"
                                    value={form.title}
                                    label="title"
                                    onChange={handleInputChange}
                                />
                            </Grid>
                            <Grid item>
                                <TextField
                                    id="txtToolTip"
                                    name="toolTip"
                                    value={form.toolTip}
                                    label="Tool Tip"
                                    onChange={handleInputChange}
                                />
                            </Grid>
                            <Grid item>
                                <FormControl fullWidth>
                                    <InputLabel id="sltDisplay-label">Display</InputLabel>
                                    <Select
                                        id="sltDisplay-label"
                                        name="display"
                                        value={form.structure.display}
                                        label="Display"
                                        onChange={handleSelectDisplay}
                                    >
                                        <MenuItem value="form">Form</MenuItem>
                                        <MenuItem value="wizard">Wizard</MenuItem>
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
                                noDefaultSubmitButton: true
                            }}
                        />
                    </MainCard>
                </Box>
            </Dialog>
        )
    );
};
