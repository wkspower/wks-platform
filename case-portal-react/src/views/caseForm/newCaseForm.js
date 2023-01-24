import { useState } from 'react';

import { QuestionCircleOutlined } from '@ant-design/icons';
import CloseIcon from '@mui/icons-material/Close';
import { Box, Tooltip } from '@mui/material';

import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import Grid from '@mui/material/Grid';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import React, { useEffect } from 'react';

import { Form } from '@formio/react';
import MainCard from 'components/MainCard';


const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const NewCaseForm = ({ open, handleClose, caseDefId, setLastCreatedCase }) => {
    const [caseDef, setCaseDef] = useState([]);
    const [form, setForm] = useState([]);
    const [formData, setFormData] = useState(null);

    useEffect(() => {
        fetch(process.env.REACT_APP_API_URL + '/case-definition/' + caseDefId)
            .then((response) => response.json())
            .then((data) => {
                setCaseDef(data);
                return fetch(process.env.REACT_APP_API_URL + '/form/' + data.formKey);
            })
            .then((response) => response.json())
            .then((data) => {
                setForm(data);
                setFormData({
                    data: {},
                    metadata: {},
                    isValid: true
                });
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [open, caseDefId]);

    const onSave = () => {
        const caseAttributes = [];
        Object.keys(formData.data).forEach((key) => {
            caseAttributes.push({
                name: key,
                value: typeof formData.data[key] !== 'object' ? formData.data[key] : JSON.stringify(formData.data[key])
            });
        });

        fetch(process.env.REACT_APP_API_URL + '/case/', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                caseDefinitionId: caseDefId,
                attributes: caseAttributes
            })
        })
            .then((response) => response.json())
            .then((data) => {
                setLastCreatedCase(data);
                handleClose();
            })
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
                            <div>{caseDef.name}</div>
                        </Typography>
                        <Button color="inherit" onClick={onSave}>
                            Save
                        </Button>
                    </Toolbar>
                </AppBar>

                {/* New Case Form */}
                <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                    <Grid item xs={12} sx={{ m: 3}}>
                        <Box sx={{ pb: 1, display: 'flex', flexDirection: 'row' }}>
                            <Typography variant="h5" color="textSecondary" sx={{ pr: 0.5 }}>
                                {form.title}
                            </Typography>
                            {form.toolTip && (
                                <Tooltip title={form.toolTip}>
                                    <QuestionCircleOutlined />
                                </Tooltip>
                            )}
                        </Box>
                        <Form form={form.structure} submission={formData} />
                    </Grid>
                </Grid>
            </Dialog>
        </div>
    );
};
