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
import { CollectionsBookmarkRounded } from '../../../node_modules/@mui/icons-material/index';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const NewCaseForm = ({ open, handleClose, caseDefId }) => {
    const [caseDef, setCaseDef] = useState([]);
    const [form, setForm] = useState([]);
    const [formData, setFormData] = useState(null);

    useEffect(() => {
        fetch('http://localhost:8081/case-definition/' + caseDefId)
            .then((response) => response.json())
            .then((data) => {
                setCaseDef(data);
                return fetch('http://localhost:8081/form/' + data.formKey);
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
            let formComponent = form.structure.components.filter((component) => component.key === key)[0];

            caseAttributes.push({
                name: key,
                value: formComponent.type !== 'file' ? formData.data[key] : JSON.stringify(formData.data[key])
            });
        });
        
        fetch('http://localhost:8081/case/', {
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
            .then((response) => {
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
                    <Grid item xs={12}>
                        <MainCard sx={{ p: 2 }} content={true}>
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
                        </MainCard>
                    </Grid>
                </Grid>
            </Dialog>
        </div>
    );
};
