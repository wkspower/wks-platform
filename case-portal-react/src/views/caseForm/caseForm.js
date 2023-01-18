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
                            <Tab label="Comments" {...a11yProps(1)} />
                            <Tab label="Attachments" {...a11yProps(2)} />
                            <Tab label="Tasks" {...a11yProps(3)} />
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
                          <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
                            <Grid item xs={12}>
                                <Comments commentsUrl="http://localhost:3004/comments" currentUserId="1" />
                            </Grid>
                        </Grid>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={2}>
                        <Attachments />
                    </TabPanel>

                    <TabPanel value={tabIndex} index={3}>
                        <div style={{ display: 'grid', padding: '10px' }}>
                            <TaskList businessKey={aCase.businessKey} bpmEngineId={caseDef.bpmEngineId} keycloak={keycloak} />
                        </div>
                    </TabPanel>
                </Dialog>
            </div>
        )
    );
};

function Attachments() {
    const data = {
        "key":"motion-detected-form","title":"Motion Detected Case Main Form","toolTip":"This is a tool tip!","structure":{"display":"form","components":[{"label":"Where","labelPosition":"top","placeholder":"","description":"Where did the motion happened","tooltip":"Inform where the motion did happened","prefix":"","suffix":"","widget":{"type":"input"},"inputMask":"","displayMask":"","allowMultipleMasks":false,"customClass":"","tabindex":"","autocomplete":"","hidden":false,"hideLabel":false,"showWordCount":false,"showCharCount":false,"mask":false,"autofocus":false,"spellcheck":true,"disabled":false,"tableView":true,"modalEdit":false,"multiple":false,"persistent":true,"inputFormat":"plain","protected":false,"dbIndex":false,"case":"","truncateMultipleSpaces":false,"encrypted":false,"redrawOn":"","clearOnHide":true,"customDefaultValue":"","calculateValue":"","calculateServer":false,"allowCalculateOverride":false,"validateOn":"change","validate":{"required":false,"pattern":"","customMessage":"","custom":"","customPrivate":false,"json":"","minLength":"","maxLength":"","strictDateValidation":false,"multiple":false,"unique":false},"unique":false,"errorLabel":"","errors":"","key":"where","tags":[],"properties":{},"conditional":{"eq":"","json":""},"customConditional":"","logic":[],"attributes":{},"overlay":{"style":"","page":"","left":"","top":"","width":"","height":""},"type":"textfield","input":true,"refreshOn":"","dataGridLabel":false,"addons":[],"inputType":"text","id":"e6hbwus","defaultValue":""},{"label":"When","labelPosition":"top","placeholder":"","description":"When did the motion happened","tooltip":"Inform when the motion did happened","prefix":"","suffix":"","widget":{"type":"input"},"inputMask":"","displayMask":"","allowMultipleMasks":false,"customClass":"","tabindex":"","autocomplete":"","hidden":false,"hideLabel":false,"showWordCount":false,"showCharCount":false,"mask":false,"autofocus":false,"spellcheck":true,"disabled":false,"tableView":true,"modalEdit":false,"multiple":false,"persistent":true,"inputFormat":"plain","protected":false,"dbIndex":false,"case":"","truncateMultipleSpaces":false,"encrypted":false,"redrawOn":"","clearOnHide":true,"customDefaultValue":"","calculateValue":"","calculateServer":false,"allowCalculateOverride":false,"validateOn":"change","validate":{"required":false,"pattern":"","customMessage":"","custom":"","customPrivate":false,"json":"","minLength":"","maxLength":"","strictDateValidation":false,"multiple":false,"unique":false},"unique":false,"errorLabel":"","errors":"","key":"when","tags":[],"properties":{},"conditional":{"eq":"","json":""},"customConditional":"","logic":[],"attributes":{},"overlay":{"style":"","page":"","left":"","top":"","width":"","height":""},"type":"textfield","input":true,"refreshOn":"","dataGridLabel":false,"addons":[],"inputType":"text","id":"eqzlr4","defaultValue":""},{"label":"Upload","labelPosition":"top","description":"","tooltip":"","customClass":"","tabindex":"","hidden":false,"hideLabel":false,"autofocus":false,"disabled":false,"tableView":false,"modalEdit":false,"storage":"base64","dir":"","fileNameTemplate":"","image":false,"uploadOnly":false,"webcam":false,"fileTypes":[{"label":"","value":""}],"filePattern":"*","fileMinSize":"0KB","fileMaxSize":"1GB","multiple":false,"persistent":true,"protected":false,"dbIndex":false,"encrypted":false,"redrawOn":"","clearOnHide":true,"customDefaultValue":"","calculateValue":"","calculateServer":false,"allowCalculateOverride":false,"validate":{"required":false,"customMessage":"","custom":"","customPrivate":false,"json":"","strictDateValidation":false,"multiple":false,"unique":false},"errorLabel":"","errors":"","key":"file","tags":[],"properties":{},"conditional":{"eq":"","json":""},"customConditional":"","logic":[],"attributes":{},"overlay":{"style":"","page":"","left":"","top":"","width":"","height":""},"type":"file","imageSize":"200","input":true,"placeholder":"","prefix":"","suffix":"","unique":false,"refreshOn":"","dataGridLabel":false,"validateOn":"change","showCharCount":false,"showWordCount":false,"allowMultipleMasks":false,"addons":[],"privateDownload":false,"id":"exiob6a"},{"input":true,"key":"dataGrid","placeholder":"","prefix":"","customClass":"","suffix":"","multiple":false,"protected":false,"unique":false,"persistent":true,"hidden":false,"clearOnHide":true,"refreshOn":"","redrawOn":"","tableView":false,"modalEdit":false,"label":"Data Grid","dataGridLabel":false,"labelPosition":"top","description":"","errorLabel":"","tooltip":"","hideLabel":false,"tabindex":"","disabled":false,"autofocus":false,"dbIndex":false,"customDefaultValue":"","calculateValue":"","calculateServer":false,"attributes":{},"validateOn":"change","validate":{"required":false,"custom":"","customPrivate":false,"strictDateValidation":false,"multiple":false,"unique":false},"conditional":{"eq":""},"overlay":{"style":"","left":"","top":"","width":"","height":""},"allowCalculateOverride":false,"encrypted":false,"showCharCount":false,"showWordCount":false,"properties":{},"allowMultipleMasks":false,"addons":[],"tree":true,"lazyLoad":false,"disableAddingRemovingRows":false,"type":"datagrid","components":[{"input":true,"key":"textField","placeholder":"","prefix":"","customClass":"","suffix":"","multiple":false,"protected":false,"unique":false,"persistent":true,"hidden":false,"clearOnHide":true,"refreshOn":"","redrawOn":"","tableView":true,"modalEdit":false,"label":"Text Field","dataGridLabel":false,"labelPosition":"top","description":"","errorLabel":"","tooltip":"","hideLabel":false,"tabindex":"","disabled":false,"autofocus":false,"dbIndex":false,"customDefaultValue":"","calculateValue":"","calculateServer":false,"widget":{"type":"input"},"attributes":{},"validateOn":"change","validate":{"required":false,"custom":"","customPrivate":false,"strictDateValidation":false,"multiple":false,"unique":false,"minLength":"","maxLength":"","pattern":""},"conditional":{"eq":""},"overlay":{"style":"","left":"","top":"","width":"","height":""},"allowCalculateOverride":false,"encrypted":false,"showCharCount":false,"showWordCount":false,"properties":{},"allowMultipleMasks":false,"addons":[],"type":"textfield","mask":false,"inputType":"text","inputFormat":"plain","inputMask":"","displayMask":"","spellcheck":true,"truncateMultipleSpaces":false,"id":"e6rmqho"}],"id":"eur0j35"}]}
    };

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
                            <ListItemText primary="Anexo-exemplo.pdf" secondary="Jan 9, 2023" />
                        </ListItem>

                        <ListItem>
                            <ListItemAvatar>
                                <Avatar style={{ backgroundColor: 'green' }}>
                                    <FileExcelOutlined />
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText primary="Planilha de orçamento.xls" secondary="Jan 9, 2023" />
                        </ListItem>
                    </List>
                </MainCard>
            </Grid>
        </Grid>
    );
}
