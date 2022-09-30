import React, { useEffect, useState } from 'react';

import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import TextField from '@mui/material/TextField';
import Toolbar from '@mui/material/Toolbar';
import MainCard from 'components/MainCard';

import { TransitionProps } from '@mui/material/transitions';
import Typography from '@mui/material/Typography';

import { ProcessDiagram } from 'views/bpmn/ProcessDiagram';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement
    },
    ref: React.Ref<unknown>
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const TaskForm = ({ open, handleClose, task }) => {
    const [formComponents, setFormComponents] = useState([]);
    const [claimed, setClaimed] = useState(false);
    const [assignee, setAssignee] = useState(null);
    const [variableValues, setVariableValues] = useState({});
    const [activityInstances, setActivityInstances] = useState(null);

    useEffect(() => {
        let apiDataVariables = {};
        let apiDataFormComponents = {};

        fetch('http://localhost:8081/task-form/' + task.id)
            .then((response) => response.json())
            .then((data) => {
                apiDataFormComponents = data.components;

                for (var key in data.components) {
                    if (data.components[key].type !== 'text') {
                        apiDataVariables[data.components[key].key] = { value: '' };
                    }
                }
                return fetch('http://localhost:8081/variable/' + task.processInstanceId);
            })
            .then((response) => response.json())
            .then((data) => {
                for (var key in data) {
                    if (key in apiDataVariables) {
                        apiDataVariables[key] = { value: data[key].value };
                    }
                }

                setFormComponents(apiDataFormComponents);
                setClaimed(task.assignee !== null);
                setAssignee(task.assignee);
                setVariableValues(apiDataVariables);
            })
            .catch((err) => {
                console.log(err.message);
            });

        fetch('http://localhost:8081/process-instance/' + task.processInstanceId + '/activity-instances')
            .then((response) => response.json())
            .then((data) => {
                setActivityInstances(data);
            });
    }, [task]);

    const handleClaim = function () {
        fetch('http://localhost:8081/task/' + task.id + '/claim/demo', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        })
            .then((response) => {
                setClaimed(true);
                setAssignee('demo');
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleUnclaim = function () {
        fetch('http://localhost:8081/task/' + task.id + '/unclaim', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        })
            .then((response) => {
                setClaimed(false);
                setAssignee(null);
            })
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleComplete = function () {
        fetch('http://localhost:8081/task/' + task.id + '/complete', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                variables: variableValues
            })
        })
            .then((response) => handleClose())
            .catch((err) => {
                console.log(err.message);
            });
    };

    const handleInputChange = function (event) {
        setVariableValues({ ...variableValues, [event.target.id]: { value: event.target.value } });
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
                        <Button color="inherit" onClick={handleComplete}>
                            Complete
                        </Button>
                    </Toolbar>
                </AppBar>
                <div style={{ display: 'grid', padding: '10px' }}>
                    {formComponents && formComponents.length ? (
                        formComponents.map((component) => {
                            if (component.type !== 'text') {
                                return (
                                    <FormControl key={component.id} style={{ padding: '5px' }} disabled={!claimed}>
                                        <TextField
                                            id={component.key}
                                            aria-describedby="my-helper-text"
                                            disabled={!claimed}
                                            value={variableValues[component.key].value}
                                            onChange={handleInputChange}
                                        />
                                        <FormHelperText id="my-helper-text">{component.label}</FormHelperText>
                                    </FormControl>
                                );
                            } else {
                                return null;
                            }
                        })
                    ) : (
                        <div>Empty form components</div>
                    )}
                </div>

                <Box>
                    <MainCard sx={{ mt: 2 }} content={false}>
                        {task && activityInstances && (
                            <ProcessDiagram processDefinitionId={task.processDefinitionId} activityInstances={activityInstances} />
                        )}
                    </MainCard>
                </Box>
            </Dialog>
        </div>
    );
};
