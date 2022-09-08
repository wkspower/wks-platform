import { useState } from 'react';

import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import React, { useEffect } from 'react';

import { Form } from '@formio/react';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});
const emptyFormData = {
    data: {
        submit: true
    },
    metadata: {},
    isValid: true
};

export const NewCaseForm = ({ open, handleClose, caseDefId }) => {
    const [caseDef, setCaseDef] = useState([]);
    const [form, setForm] = useState([]);
    const [formData, setFormData] = useState(emptyFormData);

    useEffect(() => {
        if (open) {
            fetch('http://localhost:8081/case-definition/' + caseDefId)
                .then((response) => response.json())
                .then((data) => {
                    setCaseDef(data);
                    return fetch('http://localhost:8081/form/' + data.formKey);
                })
                .then((response) => response.json())
                .then((data) => {
                    setForm(data);
                    setFormData(emptyFormData);
                })
                .catch((err) => {
                    console.log(err.message);
                });
        }
    }, [open, caseDefId]);

    const onSave = () => {
        const caseAttributes = [];
        Object.keys(formData.data)
            .filter((key) => !key.includes('submit'))
            .forEach((key) => {
                caseAttributes.push({ name: key, value: formData.data[key] });
            });

        fetch('http://localhost:8081/case', {
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
                <div style={{ display: 'grid', padding: '10px' }}>
                    <Form form={form.structure} submission={formData} />
                </div>
            </Dialog>
        </div>
    );
};
