import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Toolbar from '@mui/material/Toolbar';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';

import { Form } from '@formio/react';
import { TaskList } from '../taskList/taskList';

import { CaseStatus } from 'common/caseStatus';

import 'stream-chat-react/dist/css/index.css';
import { CaseChat } from 'getStream/chat';

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

const formDataTemplate = {
    data: {
        submit: true
    },
    metadata: {},
    isValid: true
};

export const CaseForm = ({ open, handleClose, aCase, componentsParam }) => {
    const [caseInstAttributes, setCaseInstAttributes] = useState([]);
    const [formData, setFormData] = useState({});
    const [tabIndex, setTabIndex] = useState(0);

    useEffect(() => {
        if (componentsParam) {
            setCaseInstAttributes(componentsParam.components);
        } else if (aCase) {
            fetch('http://localhost:8081/case/' + aCase.businessKey)
                .then((response) => response.json())
                .then((caseData) => {
                    setCaseInstAttributes(caseData.attributes);
                    setFormData({
                        data: caseData.attributes.reduce((obj, item) => Object.assign(obj, { [item.name]: item.value }), {}),
                        metadata: {},
                        isValid: true
                    });
                    setTabIndex(0);
                })
                .catch((err) => {
                    console.log(err.message);
                });
        }
    }, [aCase, componentsParam]);

    const [caseDef, setCaseDef] = useState([]);
    const [form, setForm] = useState([]);

    useEffect(() => {
        fetch('http://localhost:8081/case-definition/' + aCase.caseDefinitionId)
            .then((response) => response.json())
            .then((data) => {
                setCaseDef(data);
                return fetch('http://localhost:8081/form/' + data.formKey);
            })
            .then((response) => response.json())
            .then((data) => {
                setForm(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [aCase, componentsParam]);

    const handleInputChange = function (event) {
        setCaseInstAttributes({ ...caseInstAttributes, [event.target.businessKey]: { value: event.target.value } });
    };

    const handleTabChanged = (event, newValue) => {
        setTabIndex(newValue);
    };

    const handleUpdateCaseStatus = (newStatus) => {
        fetch('http://localhost:8081/case/' + aCase.businessKey, {
            method: 'PATCH',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                businessKey: caseDef.id,
                status: newStatus
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
                            <div>Motion Detected: {aCase?.businessKey}</div>
                            <div style={{ fontSize: '13px' }}>{aCase?.status}</div>
                        </Typography>
                        {aCase.status === CaseStatus.WipCaseStatus.description && (
                            <Button color="inherit" onClick={() => handleUpdateCaseStatus(CaseStatus.ClosedCaseStatus.description)}>
                                Close Case
                            </Button>
                        )}
                        {aCase.status === CaseStatus.ClosedCaseStatus.description && (
                            <React.Fragment>
                                <Button color="inherit" onClick={() => handleUpdateCaseStatus(CaseStatus.WipCaseStatus.description)}>
                                    Re-open Case
                                </Button>

                                <Button color="inherit" onClick={() => handleUpdateCaseStatus(CaseStatus.ArchivedCaseStatus.description)}>
                                    Archive Case
                                </Button>
                            </React.Fragment>
                        )}
                        {aCase.status === CaseStatus.ArchivedCaseStatus.description && (
                            <React.Fragment>
                                <Button color="inherit" onClick={() => handleUpdateCaseStatus(CaseStatus.WipCaseStatus.description)}>
                                    Re-open Case
                                </Button>
                            </React.Fragment>
                        )}
                    </Toolbar>
                </AppBar>

                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tabIndex} onChange={handleTabChanged} aria-label="basic tabs example">
                        <Tab label="Case Details" {...a11yProps(0)} />
                        <Tab label="Tasks" {...a11yProps(1)} />
                    </Tabs>
                </Box>

                <TabPanel value={tabIndex} index={0}>
                    {/* Case Details  */}
                    <Form form={form.structure} submission={formData} options={{ readOnly: true }} />
                    <CaseChat />
                </TabPanel>

                <TabPanel value={tabIndex} index={1}>
                    {/* Task List  */}
                    <div style={{ display: 'grid', padding: '10px' }}>
                        <TaskList businessKey={aCase.businessKey} />
                    </div>
                </TabPanel>
            </Dialog>
        </div>
    );
};
