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
import Tooltip from '@mui/material/Tooltip';
import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';
import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';

import { Form } from '@formio/react';
import { TaskList } from '../taskList/taskList';

import { CaseStatus } from 'common/caseStatus';

import { Grid } from '@mui/material';
import MainCard from 'components/MainCard';
import { Comments } from 'views/caseComment/Comments';

import { QuestionCircleOutlined } from '@ant-design/icons';

import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';

import { tryParseJSONObject } from '../../utils/jsonStringCheck';

import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import Avatar from '@mui/material/Avatar';
import { FilePdfOutlined, FileExcelOutlined } from '@ant-design/icons';


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

export const CaseForm = ({ open, handleClose, aCase, keycloak }) => {
    const [caseDef, setCaseDef] = useState(null);
    const [form, setForm] = useState(null);
    const [formData, setFormData] = useState(null);

    const [tabIndex, setTabIndex] = useState(0);

    const [activeStage, setActiveStage] = React.useState(0);
    const [stages, setStages] = useState([]);

    useEffect(() => {
        fetch(process.env.REACT_APP_API_URL + '/case-definition/' + aCase.caseDefinitionId)
            .then((response) => response.json())
            .then((data) => {
                setCaseDef(data);
                setStages(data.stages.sort((a, b) => a.index - b.index).map((o) => o.name));
                return fetch(process.env.REACT_APP_API_URL + '/form/' + data.formKey);
            })
            .then((response) => response.json())
            .then((data) => {
                setForm(data);
                return fetch(process.env.REACT_APP_API_URL + '/case/' + aCase.businessKey);
            })
            .then((response) => response.json())
            .then((caseData) => {
                setFormData({
                    data: caseData.attributes.reduce(
                        (obj, item) =>
                            Object.assign(obj, {
                                [item.name]: tryParseJSONObject(item.value) ? JSON.parse(item.value) : item.value
                            }),
                        {}
                    ),
                    metadata: {},
                    isValid: true
                });
                setActiveStage(caseData.stage);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [open, aCase]);

    const handleTabChanged = (event, newValue) => {
        setTabIndex(newValue);
    };

    const handleUpdateCaseStatus = (newStatus) => {
        fetch(process.env.REACT_APP_API_URL + '/case/' + aCase.businessKey, {
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
        aCase &&
        caseDef &&
        form &&
        formData && (
            <div>
                <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition}>
                    <AppBar sx={{ position: 'relative' }}>
                        <Toolbar>
                            <IconButton edge="start" color="inherit" onClick={handleClose} aria-label="close">
                                <CloseIcon />
                            </IconButton>
                            <Typography sx={{ ml: 2, flex: 1 }} component="div">
                                <div>{caseDef.name}: {aCase?.businessKey}</div>
                                <div style={{ fontSize: '13px' }}>{aCase?.statusDescription}</div>
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

                                    <Button
                                        color="inherit"
                                        onClick={() => handleUpdateCaseStatus(CaseStatus.ArchivedCaseStatus.description)}
                                    >
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

                    <Box sx={{ pl: 10, pr: 10, pt: 2, pb: 2, borderBottom: 1, borderColor: 'divider' }}>
                        <Stepper
                            activeStep={stages.findIndex((o) => {
                                return o === activeStage;
                            })}
                        >
                            {stages.map((label, index) => {
                                const stagesProps = {};
                                const labelProps = {};
                                return (
                                    <Step key={label} {...stagesProps}>
                                        <StepLabel {...labelProps}>{label}</StepLabel>
                                    </Step>
                                );
                            })}
                        </Stepper>
                    </Box>

                    <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                        <Tabs value={tabIndex} onChange={handleTabChanged} aria-label="basic tabs example">
                            <Tab label="Case Details" {...a11yProps(0)} />
                            <Tab label="Tasks" {...a11yProps(1)} />
                            <Tab label="Comments" {...a11yProps(2)} />
                            <Tab label="Attachments" {...a11yProps(3)} />
                            <Tab label="E-mails" {...a11yProps(4)} />
                            
                        </Tabs>
                    </Box>

                    <TabPanel value={tabIndex} index={0}>
                        {/* Case Details  */}
                        <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                            <Grid item xs={12}>
                                <MainCard sx={{ p: 2 }} content={false}>
                                    <Box sx={{ pb: 1, display: 'flex', flexDirection: 'row' }}>
                                        <Typography variant="h5" color="textSecondary" sx={{ pr: 0.5 }}>
                                            {form.title}
                                        </Typography>
                                        <Tooltip title={form.toolTip}>
                                            <QuestionCircleOutlined />
                                        </Tooltip>
                                    </Box>
                                    <Form form={form.structure} submission={formData} options={{ readOnly: true }} />
                                </MainCard>
                            </Grid>
                        </Grid>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={1}>
                        <div style={{ display: 'grid', padding: '10px' }}>
                            <TaskList businessKey={aCase.businessKey} bpmEngineId={caseDef.bpmEngineId} keycloak={keycloak} />
                        </div>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={2}>
                          <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                            <Grid item xs={12}>
                                <Comments commentsUrl="http://localhost:3004/comments" currentUserId="1" />
                            </Grid>
                        </Grid>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={3}>
                        <Attachments />
                    </TabPanel>

                    <TabPanel value={tabIndex} index={4}>
                        
                    </TabPanel>

                </Dialog>
            </div>
        )
    );
};

function Attachments() {
    return (
        <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
            <Grid item xs={12}>
                <MainCard sx={{ mb: 1 }}>
                    <Box sx={{ border: '1px dashed #d9d9d9', padding: 5 }}>
                        <Grid container direction="column" justifyContent="center" alignItems="center">
                            <Avatar style={{ backgroundColor: '#27CDF2', fontSize: 40, height: 60, width: 60, opacity: 0.5 }}>
                                <FilePdfOutlined />
                            </Avatar>

                            <br/>

                            <Typography variant="h4" color="textSecondary" sx={{ pr: 0.5 }}>
                                Select a file to upload
                            </Typography>

                            <Typography variant="h6" color="textSecondary" sx={{ pr: 0.2 }}>
                                or drag and drop it here
                            </Typography>
                        </Grid>
                    </Box>

                    <div  style={{ paddingTop: 15 }}>
                        <hr/>
                    </div>

                    <List>
                        <ListItem>
                            <ListItemAvatar>
                                <Avatar style={{ backgroundColor: 'red' }}>
                                    <FilePdfOutlined />
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText primary="sample-attachment.pdf" secondary="Jan 9, 2023" />
                        </ListItem>

                        <ListItem>
                            <ListItemAvatar>
                                <Avatar style={{ backgroundColor: 'green' }}>
                                    <FileExcelOutlined />
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText primary="budget-spreadsheet.xls" secondary="Jan 9, 2023" />
                        </ListItem>
                    </List>
                </MainCard>
            </Grid>
        </Grid>
    );
}
