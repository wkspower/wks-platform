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

import { CaseDefEventsForm } from './caseDefEventsForm';
import { CaseDefGeneralForm } from './caseDefGeneralForm';
import { CaseDefFormForm } from './caseFormForm';

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

export const CaseDefForm = ({ open, handleClose, aCaseDef }) => {
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
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
                            <div>{aCaseDef?.name}</div>
                        </Typography>
                        <Button color="inherit">Edit</Button>
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
                        <Tab label="Business key" {...a11yProps(1)} />
                        <Tab label="Data Structure" {...a11yProps(2)} />
                        <Tab label="Validation Rules" {...a11yProps(3)} />
                        <Tab label="Stages" {...a11yProps(4)} />
                        <Tab label="Priority & Severity" {...a11yProps(5)} />
                        <Tab label="Forms" {...a11yProps(6)} />
                        <Tab label="Search Layouts" {...a11yProps(7)} />
                        <Tab label="Access Control" {...a11yProps(8)} />
                        <Tab label="Auditing" {...a11yProps(9)} />
                        <Tab label="API" {...a11yProps(10)} />
                        <Tab label="Deployment" {...a11yProps(11)} />
                        <Tab label="Versions" {...a11yProps(12)} />
                        <Tab label="Source Code" {...a11yProps(13)} />
                    </Tabs>
                </Box>

                {/* General Tab */}
                <TabPanel value={tabValue} index={0}>
                    <CaseDefGeneralForm caseDef={aCaseDef} />
                </TabPanel>

                <TabPanel value={tabValue} index={6}>
                    <CaseDefFormForm formKey={aCaseDef.formKey} />
                </TabPanel>
            </Dialog>
        </div>
    );
};
