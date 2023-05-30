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
import Typography from '@mui/material/Typography';
import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';
import { Form } from '@formio/react';
import { TaskList } from '../taskList/taskList';
import { CaseStatus } from 'common/caseStatus';
import { Grid } from '@mui/material';
import MainCard from 'components/MainCard';
import { Comments } from 'views/caseComment/Comments';
import QuestionCircleOutlined from '@ant-design/icons/QuestionCircleOutlined';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import { tryParseJSONObject } from '../../utils/jsonStringCheck';
import { CaseEmailsList } from 'views/caseEmail/caseEmailList';
import { useTranslation } from 'react-i18next';
import { CaseService, FormService } from '../../services';
import Documents from './Documents';

export const CaseForm = ({ open, handleClose, aCase, keycloak }) => {
    const [caseDef, setCaseDef] = useState(null);
    const [form, setForm] = useState(null);
    const [formData, setFormData] = useState(null);
    const [comments, setComments] = useState(null);
    const [documents, setDocuments] = useState(null);
    const [tabIndex, setTabIndex] = useState(0);
    const [activeStage, setActiveStage] = React.useState(0);
    const [stages, setStages] = useState([]);
    const { t } = useTranslation();

    useEffect(() => {
        getCaseInfo(aCase);
    }, [open, aCase]);

    const getCaseInfo = (aCase) => {
        CaseService.getCaseDefinitionsById(keycloak, aCase.caseDefinitionId)
            .then((data) => {
                setCaseDef(data);
                setStages(data.stages.sort((a, b) => a.index - b.index).map((o) => o.name));
                return FormService.getByKey(keycloak, data.formKey);
            })
            .then((data) => {
                setForm(data);
                return CaseService.getCaseById(keycloak, aCase.businessKey);
            })
            .then((caseData) => {
                setComments(caseData?.comments?.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
                setDocuments(caseData?.documents);
                setFormData({
                    data: caseData.attributes.reduce(
                        (obj, item) =>
                            Object.assign(obj, {
                                [item.name]: tryParseJSONObject(item.value)
                                    ? JSON.parse(item.value)
                                    : item.value
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
    };

    const handleTabChanged = (event, newValue) => {
        setTabIndex(newValue);
    };

    const handleUpdateCaseStatus = (keycloak, newStatus) => {
        CaseService.updateCaseStatusById(
            keycloak,
            aCase.businessKey,
            JSON.stringify({
                businessKey: caseDef.id,
                status: newStatus
            })
        )
            .then(() => {
                handleClose();
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const updateActiveState = () => {
        CaseService.getCaseById(keycloak, aCase.businessKey).then((data) => setActiveStage(data.stage));
    }

    return (
        aCase &&
        caseDef &&
        form &&
        formData && (
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
                            <Typography sx={{ ml: 2, flex: 1 }} component="div">
                                <div>
                                    {caseDef.name}: {aCase?.businessKey}
                                </div>
                                <div style={{ fontSize: '13px' }}>{aCase?.statusDescription}</div>
                            </Typography>
                            {aCase.status === CaseStatus.WipCaseStatus.description && (
                                <Button
                                    color="inherit"
                                    onClick={() =>
                                        handleUpdateCaseStatus(
                                            keycloak,
                                            CaseStatus.ClosedCaseStatus.description
                                        )
                                    }
                                >
                                    {t('pages.caseform.actions.close')}
                                </Button>
                            )}
                            {aCase.status === CaseStatus.ClosedCaseStatus.description && (
                                <React.Fragment>
                                    <Button
                                        color="inherit"
                                        onClick={() =>
                                            handleUpdateCaseStatus(
                                                keycloak,
                                                CaseStatus.WipCaseStatus.description
                                            )
                                        }
                                    >
                                        {t('pages.caseform.actions.reopen')}
                                    </Button>

                                    <Button
                                        color="inherit"
                                        onClick={() =>
                                            handleUpdateCaseStatus(
                                                keycloak,
                                                CaseStatus.ArchivedCaseStatus.description
                                            )
                                        }
                                    >
                                        {t('pages.caseform.actions.archive')}
                                    </Button>
                                </React.Fragment>
                            )}
                            {aCase.status === CaseStatus.ArchivedCaseStatus.description && (
                                <React.Fragment>
                                    <Button
                                        color="inherit"
                                        onClick={() =>
                                            handleUpdateCaseStatus(
                                                keycloak,
                                                CaseStatus.WipCaseStatus.description
                                            )
                                        }
                                    >
                                        {t('pages.caseform.actions.reopen')}
                                    </Button>
                                </React.Fragment>
                            )}
                        </Toolbar>
                    </AppBar>

                    <Box
                        sx={{
                            pl: 10,
                            pr: 10,
                            pt: 2,
                            pb: 2,
                            borderBottom: 1,
                            borderColor: 'divider'
                        }}
                    >
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
                        <Tabs
                            value={tabIndex}
                            onChange={handleTabChanged}
                            aria-label="basic tabs example"
                        >
                            <Tab label={t('pages.caseform.tabs.details')} {...a11yProps(0)} />
                            <Tab label={t('pages.caseform.tabs.tasks')} {...a11yProps(1)} />
                            <Tab label={t('pages.caseform.tabs.comments')} {...a11yProps(2)} />
                            <Tab label={t('pages.caseform.tabs.documents')} {...a11yProps(3)} />
                            <Tab label={t('pages.caseform.tabs.emails')} {...a11yProps(4)} />
                        </Tabs>
                    </Box>

                    <TabPanel value={tabIndex} index={0}>
                        {/* Case Details  */}
                        <Grid
                            container
                            spacing={2}
                            sx={{ display: 'flex', flexDirection: 'column' }}
                        >
                            <Grid item xs={12}>
                                <MainCard sx={{ p: 2 }} content={false}>
                                    <Box sx={{ pb: 1, display: 'flex', flexDirection: 'row' }}>
                                        <Typography
                                            variant="h5"
                                            color="textSecondary"
                                            sx={{ pr: 0.5 }}
                                        >
                                            {form.title}
                                        </Typography>
                                        <Tooltip title={form.toolTip}>
                                            <QuestionCircleOutlined />
                                        </Tooltip>
                                    </Box>
                                    <Form
                                        form={form.structure}
                                        submission={formData}
                                        options={{ readOnly: true }}
                                    />
                                </MainCard>
                            </Grid>
                        </Grid>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={1}>
                        <div style={{ display: 'grid', padding: '10px' }}>
                            <TaskList
                                businessKey={aCase.businessKey}
                                keycloak={keycloak}
                                getCaseInfo={getCaseInfo}
                                callback={updateActiveState}
                            />
                        </div>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={2}>
                        <Grid
                            container
                            spacing={2}
                            sx={{ display: 'flex', flexDirection: 'column' }}
                        >
                            <Grid item xs={12}>
                                <Comments
                                    aCase={aCase}
                                    getCaseInfo={getCaseInfo}
                                    comments={comments ? comments : []}
                                />
                            </Grid>
                        </Grid>
                    </TabPanel>

                    <TabPanel value={tabIndex} index={3}>
                        <Documents aCase={aCase} initialValue={documents || []} />
                    </TabPanel>

                    <TabPanel value={tabIndex} index={4}>
                        <CaseEmailsList caseInstanceBusinessKey={aCase.businessKey} />
                    </TabPanel>
                </Dialog>
            </div>
        )
    );
};

const Transition = React.forwardRef(function Transition(props, ref) {
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
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
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
