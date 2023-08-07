import React, { useEffect, useState } from 'react';
import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import MainCard from 'components/MainCard';
import Typography from '@mui/material/Typography';
import { ProcessDiagram } from 'views/bpmnViewer/ProcessDiagram';
import { Form } from '@formio/react';
import { useTranslation } from 'react-i18next';
import { FormService, TaskService } from 'services';
import { useSession } from '../../SessionStoreContext';
import { StorageService } from 'plugins/storage';

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const TaskForm = ({ open, handleClose, task }) => {
    const [claimed, setClaimed] = useState(false);
    const [assignee, setAssignee] = useState(null);
    const [formComponents, setFormComponents] = useState(null);
    const [variableValues, setVariableValues] = useState(null);
    const [activityInstances, setActivityInstances] = useState(null);
    const { t } = useTranslation();
    const keycloak = useSession();

    useEffect(() => {
        let apiDataVariables = {};
        let apiDataFormComponents = {};

        FormService.getByKey(keycloak, task.formKey)
            .then((data) => {
                apiDataFormComponents = data.structure;

                apiDataVariables = {
                    data: {},
                    metadata: {},
                    isValid: true
                };

                return FormService.getVariableById(keycloak, task.processInstanceId);
            })
            .then((data) => {
                for (var key in data) {
                    apiDataVariables.data[key] =
                        data[key].type === 'Json' ? JSON.parse(data[key].value) : data[key].value;
                }

                setFormComponents(apiDataFormComponents);
                setVariableValues(apiDataVariables);
                setClaimed(task.assignee !== null && task.assignee !== undefined);
                setAssignee(task.assignee);
            })
            .catch((err) => {
                console.log(err.message);
            });

        TaskService.getActivityInstancesById(keycloak, task.processInstanceId).then((data) => {
            setActivityInstances(data);
        });
    }, [open, task]);

    const handleClaim = function () {
        TaskService.createTaskClaim(keycloak, task.id)
            .then(() => {
                setClaimed(true);
                setAssignee(keycloak.idTokenParsed.name);
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleUnclaim = function () {
        TaskService.createTaskUnclaim(keycloak, task.id)
            .then(() => {
                setClaimed(false);
                setAssignee(null);
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleComplete = function () {
        let variables = { ...variableValues.data };

        Object.keys(variables).forEach(function (key, index) {
            variables[key] =
                typeof variables[key] === 'object'
                    ? { value: JSON.stringify(variables[key]), type: 'Json' }
                    : { value: variables[key] };
        });

        TaskService.createTaskComplete(keycloak, task.id, variables)
            .then(() => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

    return (
        <div>
            <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition}>
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
                            <div>{task?.name}</div>
                            <div style={{ fontSize: '13px' }}>{task?.caseInstanceId}</div>
                            <div style={{ fontSize: '10px' }}>{task?.id}</div>
                        </Typography>
                        {!claimed ? (
                            <Button color="inherit" onClick={handleClaim}>
                                {t('pages.taskform.claim')}
                            </Button>
                        ) : (
                            <Button color="inherit" onClick={handleUnclaim}>
                                <div>
                                    {assignee} <sup style={{ fontSize: '10px' }}>x</sup>
                                </div>
                            </Button>
                        )}
                        {claimed && (
                            <Button color="inherit" onClick={handleComplete}>
                                {t('pages.taskform.complete')}
                            </Button>
                        )}
                    </Toolbar>
                </AppBar>
                <div style={{ display: 'grid', padding: '10px' }}>
                    <div style={!claimed ? { pointerEvents: 'none', opacity: '0.4' } : {}}>
                        <Form
                            form={formComponents}
                            submission={variableValues}
                            options={{
                                fileService: new StorageService()
                            }}
                        />
                    </div>
                </div>

                <Box>
                    <MainCard sx={{ mt: 2 }} content={false}>
                        {task && activityInstances && (
                            <ProcessDiagram
                                processDefinitionId={task.processDefinitionId}
                                activityInstances={activityInstances}
                            />
                        )}
                    </MainCard>
                </Box>
            </Dialog>
        </div>
    );
};
