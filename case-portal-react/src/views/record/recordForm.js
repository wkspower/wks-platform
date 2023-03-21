import React, { useState } from 'react';
import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import { Form } from '@formio/react';
import Grid from '@mui/material/Grid';
import MainCard from 'components/MainCard';
import { useEffect } from 'react';
import { RecordService } from '../../services';
import { useSession } from 'SessionStoreContext';

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const RecordForm = ({ open, recordType, record, handleClose, mode }) => {
    const [form, setForm] = useState(null);
    const [formData, setFormData] = useState(null);
    const keycloak = useSession();

    useEffect(() => {
        setForm(recordType.fields);
        setFormData({
            data: record,
            metadata: {},
            isValid: true
        });
        // eslint-disable-next-line
    }, [record]);

    const save = () => {
        if (mode === 'new') {
            RecordService.createRecord(keycloak, recordType.id, formData.data)
                .then(() => {
                    handleClose();
                })
                .catch((err) => {
                    console.log(err.message);
                });
        } else {
            RecordService.updateRecord(keycloak, recordType.id, record._id.$oid, formData.data)
                .then(() => {
                    handleClose();
                })
                .catch((err) => {
                    console.log(err.message);
                });
        }
    };

    const deleteRecord = () => {
        RecordService.deleteRecord(recordType.id, record._id.$oid)
            .then(() => {
                handleClose();
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    return (
        form &&
        formData && (
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
                            <div>{recordType.id}</div>
                        </Typography>
                        <Button color="inherit" onClick={save}>
                            Save
                        </Button>
                        {!(mode && mode === 'new') && (
                            <Button color="inherit" onClick={deleteRecord}>
                                Delete
                            </Button>
                        )}
                    </Toolbar>
                </AppBar>

                <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                    <Grid item xs={12}>
                        <MainCard sx={{ p: 2 }} content={true}>
                            <Form form={form} submission={formData} />
                        </MainCard>
                    </Grid>
                </Grid>
            </Dialog>
        )
    );
};
