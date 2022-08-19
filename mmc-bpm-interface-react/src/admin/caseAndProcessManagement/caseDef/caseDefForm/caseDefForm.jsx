import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import CloseIcon from '@mui/icons-material/Close';
import Slide from '@mui/material/Slide';
import { TransitionProps } from '@mui/material/transitions';
import FormControl from '@mui/material/FormControl';
import TextField from '@mui/material/TextField';
import FormHelperText from '@mui/material/FormHelperText';

import { TaskList } from '../../../../taskList/taskList';

import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
};

export const CaseDefForm = ({ open, handleClose, aCaseDef }) => {

    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    return (
        <div>
            <Dialog
                fullScreen
                open={open}
                onClose={handleClose}
                TransitionComponent={Transition}
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
                        <Typography sx={{ ml: 2, flex: 1 }} variant="h6" component="div">
                            <div>Case definition: {aCaseDef?.name}</div>
                        </Typography>
                        <Button autoFocus color="inherit">
                            Edit
                        </Button>
                    </Toolbar>
                </AppBar>

                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tabValue} onChange={handleTabChange} aria-label="basic tabs example">
                        <Tab label="General" {...a11yProps(0)} />
                        <Tab label="Data Structure" {...a11yProps(1)} />
                        <Tab label="Validation Rules" {...a11yProps(2)} />
                        <Tab label="States" {...a11yProps(3)} />
                        <Tab label="Forms" {...a11yProps(4)} />
                        <Tab label="Search Layouts" {...a11yProps(5)} />
                        <Tab label="Events" {...a11yProps(6)} />
                        <Tab label="Listeners" {...a11yProps(7)} />
                        <Tab label="Access Control" {...a11yProps(8)} />
                        <Tab label="Auditing" {...a11yProps(9)} />
                        <Tab label="API" {...a11yProps(10)} />
                        <Tab label="Deployment" {...a11yProps(11)} />
                        <Tab label="Versions" {...a11yProps(12)} />
                    </Tabs>
                </Box>
                <TabPanel value={tabValue} index={0}>
                    {/* Case Definition Form */}
                    <div style={{ display: 'grid', padding: '10px' }}>
                        <FormControl key='ctrlId' style={{ padding: '5px' }}>
                            <TextField id='txtId' aria-describedby="my-helper-text" value={aCaseDef.id} />
                            <FormHelperText id="my-helper-text">Case Definition Id</FormHelperText>
                        </FormControl>
                        <FormControl key='ctrlName' style={{ padding: '5px' }}>
                            <TextField id='txtName' aria-describedby="my-helper-text" value={aCaseDef.name} />
                            <FormHelperText id="my-helper-text">Case Definition Name</FormHelperText>
                        </FormControl>
                        <FormControl key='ctrlProcDefKeys' style={{ padding: '5px' }}>
                            <TextField id='txtProcDefKeys' aria-describedby="my-helper-text" value={aCaseDef.onCreateProcessDefinitions} />
                            <FormHelperText id="my-helper-text">Process Definition Keys</FormHelperText>
                        </FormControl>
                    </div>
                </TabPanel>

            </Dialog>
        </div >
    );
}