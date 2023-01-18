import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import React, { useState } from 'react';

import Box from '@mui/material/Box';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import PropTypes from 'prop-types';

import { useEffect } from 'react';
import { CaseDefFormStages } from './caseDefFormStages';
import { CaseDefGeneralForm } from './caseDefGeneralForm';
import { CaseDefFormForm } from './caseFormForm';
import { CaseKanbanForm } from './caseDefKanban';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`
    };
}

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div role="tabpanel" hidden={value !== index} id={`simple-tabpanel-${index}`} aria-labelledby={`simple-tab-${index}`} {...other}>
            {value === index && (
                <Box sx={{ p: 3 }}>
                    <Typography component={'span'}>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired
};

export const CaseDefForm = ({ open, handleClose, caseDefParam }) => {
    const [tabValue, setTabValue] = useState(0);
    const [caseDef, setCaseDef] = useState(caseDefParam);

    useEffect(() => {
        setCaseDef(caseDefParam);
    }, [open, caseDefParam]);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    const handleSave = () => {
        if (caseDef.status && caseDef.status === 'new') {
            fetch(process.env.REACT_APP_API_URL + '/case-definition/', {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(caseDef)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        } else {
            fetch(process.env.REACT_APP_API_URL + '/case-definition/' + caseDef.id, {
                method: 'PATCH',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(caseDef)
            })
                .then((response) => handleClose())
                .catch((err) => {
                    console.log(err.message);
                });
        }
    };

    const handleDelete = () => {
        fetch(process.env.REACT_APP_API_URL + '/case-definition/' + caseDef.id, {
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
                            <div>{caseDef?.name}</div>
                        </Typography>
                        <Button color="inherit" onClick={handleSave}>
                            Save
                        </Button>
                        {!(caseDef.status && caseDef.status === 'new') && (
                            <Button color="inherit" onClick={handleDelete}>
                                Delete
                            </Button>
                        )}
                    </Toolbar>
                </AppBar>

                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs
                        value={tabValue}
                        onChange={handleTabChange}
                        variant="scrollable"
                        scrollButtons="auto"
                        aria-label="basic tabs example"
                    >
                        <Tab label="General" {...a11yProps(0)} />
                        <Tab label="Stages" {...a11yProps(1)} />
                        <Tab label="Kanban" {...a11yProps(2)} />
                    </Tabs>
                </Box>

                {/* General Tab */}
                <TabPanel value={tabValue} index={0}>
                    <div style={{ display: 'grid', padding: '10px' }}>
                        <CaseDefGeneralForm caseDef={caseDef} setCaseDef={setCaseDef} />
                        <CaseDefFormForm caseDef={caseDef} setCaseDef={setCaseDef} />
                    </div>
                </TabPanel>

                <TabPanel value={tabValue} index={1}>
                    <CaseDefFormStages caseDef={caseDef} setCaseDef={setCaseDef} />
                </TabPanel>

                <TabPanel value={tabValue} index={2}>
                    <CaseKanbanForm caseDef={caseDef} setCaseDef={setCaseDef}/>
                </TabPanel>
            </Dialog>
        </div>
    );
};
