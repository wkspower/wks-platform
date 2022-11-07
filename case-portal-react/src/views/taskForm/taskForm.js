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

import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';

import { ProcessDiagram } from 'views/bpmn/ProcessDiagram';

import { Form } from '@formio/react';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const TaskForm = ({ open, handleClose, task, bpmEngineId, keycloak }) => {
    const [claimed, setClaimed] = useState(false);
    const [assignee, setAssignee] = useState(null);

    const [formComponents, setFormComponents] = useState(null);
    const [variableValues, setVariableValues] = useState(null);

    const [activityInstances, setActivityInstances] = useState(null);

    useEffect(() => {
        let apiDataVariables = {};
        let apiDataFormComponents = {};

        fetch(process.env.REACT_APP_API_URL + '/form/' + task.formKey)
            .then((response) => response.json())
            .then((data) => {
                apiDataFormComponents = data.structure;

                apiDataVariables = {
                    data: {},
                    metadata: {},
                    isValid: true
                };
                return fetch(process.env.REACT_APP_API_URL + '/variable/' + bpmEngineId + '/' + task.processInstanceId);
            })
            .then((response) => response.json())
            .then((data) => {
                for (var key in data) {
                    apiDataVariables.data[key] = data[key].type === 'Json' ? JSON.parse(data[key].value) : data[key].value;
                }

                setFormComponents(apiDataFormComponents);
                setVariableValues(apiDataVariables);
                setClaimed(task.assignee !== null && task.assignee !== undefined);
                setAssignee(task.assignee);
            })
            .catch((err) => {
                console.log(err.message);
            });

        fetch(process.env.REACT_APP_API_URL + '/process-instance/' + bpmEngineId + '/' + task.processInstanceId + '/activity-instances')
            .then((response) => response.json())
            .then((data) => {
                setActivityInstances(data);
            });
    }, [open, task, bpmEngineId]);

    const handleClaim = function () {
        fetch(process.env.REACT_APP_API_URL + '/task/' + bpmEngineId + '/' + task.id + '/claim/' + keycloak.idTokenParsed.name, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        })
            .then(() => {
                setClaimed(true);
                setAssignee(keycloak.idTokenParsed.name);
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleUnclaim = function () {
        fetch(process.env.REACT_APP_API_URL + '/task/' + bpmEngineId + '/' + task.id + '/unclaim', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        })
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
                typeof variables[key] === 'object' ? { value: JSON.stringify(variables[key]), type: 'Json' } : { value: variables[key] };
        });

        fetch(process.env.REACT_APP_API_URL + '/task/' + bpmEngineId + '/' + task.id + '/complete', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                variables: variables
            })
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
                            <div>{task?.name}</div>
                            <div style={{ fontSize: '13px' }}>{task?.caseInstanceId}</div>
                            <div style={{ fontSize: '10px' }}>{task?.id}</div>
                        </Typography>
                        {!claimed ? (
                            <Button color="inherit" onClick={handleClaim}>
                                Claim
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
                                Complete
                            </Button>
                        )}
                    </Toolbar>
                </AppBar>
                <div style={{ display: 'grid', padding: '10px' }}>
                    <div style={!claimed ? { pointerEvents: 'none', opacity: '0.4' } : {}}>
                        <Form form={formComponents} submission={variableValues} />
                    </div>
                </div>

                <Box>
                    <MainCard sx={{ mt: 2 }} content={false}>
                        {task && activityInstances && (
                            <ProcessDiagram
                                processDefinitionId={task.processDefinitionId}
                                activityInstances={activityInstances}
                                bpmEngineId={bpmEngineId}
                            />
                        )}
                    </MainCard>
                </Box>
            </Dialog>
        </div>
    );
};
