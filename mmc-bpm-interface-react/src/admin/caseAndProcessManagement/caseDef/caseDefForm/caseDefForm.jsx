import React, { useState } from "react";
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

import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';

import TreeView from '@mui/lab/TreeView';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import TreeItem from '@mui/lab/TreeItem';

import { DataGrid, GridColDef } from '@mui/x-data-grid';

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

const eventsColumns: GridColDef[] = [
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'type', headerName: 'Type', width: 250 },
];

export const CaseDefForm = ({ open, handleClose, aCaseDef }) => {

    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
    };

    const [hook, setHook] = useState(null);
    const handleHookChange = (event, nodeId) => {

        if (!nodeId.endsWith('Group')) {
            setHook(nodeId)
        }
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
                        <Tab label="Priority & Severity" {...a11yProps(4)} />
                        <Tab label="Forms" {...a11yProps(5)} />
                        <Tab label="Search Layouts" {...a11yProps(6)} />
                        <Tab label="Events" {...a11yProps(7)} />
                        <Tab label="Listeners" {...a11yProps(8)} />
                        <Tab label="Access Control" {...a11yProps(9)} />
                        <Tab label="Auditing" {...a11yProps(10)} />
                        <Tab label="API" {...a11yProps(11)} />
                        <Tab label="Deployment" {...a11yProps(12)} />
                        <Tab label="Versions" {...a11yProps(13)} />
                        
                        <Tab label="Source Code" {...a11yProps(14)} />

                    </Tabs>
                </Box>

                {/* General Tab */}
                <TabPanel value={tabValue} index={0}>
                    <div style={{ display: 'grid', padding: '10px' }}>
                        <FormControl key='ctrlId' style={{ padding: '5px' }}>
                            <TextField id='txtId' aria-describedby="my-helper-text" value={aCaseDef.id} />
                            <FormHelperText id="my-helper-text">Case Definition Id</FormHelperText>
                        </FormControl>
                        <FormControl key='ctrlName' style={{ padding: '5px' }}>
                            <TextField id='txtName' aria-describedby="my-helper-text" value={aCaseDef.name} />
                            <FormHelperText id="my-helper-text">Case Definition Name</FormHelperText>
                        </FormControl>
                    </div>
                </TabPanel>

                {/* Events tab */}
                <TabPanel value={tabValue} index={7}>
                    <div>
                        <div style={{ float: 'left', padding: '20px', borderStyle: 'solid', borderWidth: 'thin' }}>
                            <TreeView
                                aria-label="file system navigator"
                                defaultCollapseIcon={<ExpandMoreIcon />}
                                defaultExpandIcon={<ChevronRightIcon />}
                                onNodeSelect={handleHookChange}
                                sx={{ flexGrow: 1, maxWidth: 200 }}
                            >
                                <div style={{ fontWeight: 'bold', padding: '5px' }}>Case Hooks</div>
                                <TreeItem nodeId="createGroup" label="Create">
                                    <TreeItem nodeId="beforeCaseCreateHook" label="Before Create" />
                                    <TreeItem nodeId="postCaseCreateHook" label="After Create" />
                                </TreeItem>
                                <TreeItem nodeId="closeGroup" label="Close">
                                    <TreeItem nodeId="beforeCaseCloseHook" label="Before Close" />
                                    <TreeItem nodeId="postCaseCloseHook" label="After Close" />
                                </TreeItem>
                                <TreeItem nodeId="stateUpdateGroup" label="State Update">
                                    <TreeItem nodeId="beforeCaseStateUpdateHook" label="Before State Update" />
                                    <TreeItem nodeId="postCaseStateUpdateHook" label="After State Update" />
                                </TreeItem>
                                <TreeItem nodeId="updateGroup" label="Update">
                                    <TreeItem nodeId="beforeCaseUpdateHook" label="Before Update" />
                                    <TreeItem nodeId="postCaseUpdateHook" label="After Update" />
                                </TreeItem>
                                <TreeItem nodeId="archiveGroup" label="Archive">
                                    <TreeItem nodeId="beforeCaseArchiveHook" label="Before Archive" />
                                    <TreeItem nodeId="postCaseArchiveHook" label="After Archive" />
                                </TreeItem>
                                <TreeItem nodeId="assignGroup" label="Assign">
                                    <TreeItem nodeId="beforeCaseAssignHook" label="Before Assign" />
                                    <TreeItem nodeId="postCaseAssignHook" label="After Assign" />
                                </TreeItem>
                            </TreeView>
                        </div>

                        <div style={{ float: 'left', padding: '10px', height: 400, width: 500 }}>
                            {hook && aCaseDef[hook].caseEvents &&
                                <DataGrid
                                    rows={aCaseDef[hook].caseEvents}
                                    columns={eventsColumns}
                                    pageSize={10}
                                    rowsPerPageOptions={[10]}
                                />}
                        </div>
                    </div>
                </TabPanel>
            </Dialog>
        </div >
    );
}