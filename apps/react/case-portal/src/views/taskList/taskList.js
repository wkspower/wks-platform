import { AddCircleOutline } from '@mui/icons-material';
import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import { format } from 'date-fns';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { TaskService } from 'services';
import { ProcessDiagram } from 'views/bpmnViewer/ProcessDiagram';
import { useSession } from '../../SessionStoreContext';
import { TaskForm } from '../taskForm/taskForm';
import Typography from '@mui/material/Typography';
import './taskList.css';
import Config from 'consts/index';

export const TaskList = ({ businessKey, callback }) => {
    const [tasks, setTasks] = useState(null);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);
    const [processDefId, setProcessDefId] = useState(null);
    const [activityInstances, setActivityInstances] = useState(null);
    const { t } = useTranslation();
    const [fetching, setFetching] = useState(false);
    const keycloak = useSession();

    const [isModalOpen, setModalOpen] = useState(false);
    const [newTaskData, setNewTaskData] = useState({
        name: '',
        description: '',
        due: null,
        assignee: '',
        caseInstanceId: businessKey
    });

    useEffect(() => {
        if (Config.WebsocketsEnabled) {
            const topic = Config.WebsocketsTopicHumanTaskCreated;
            const ws = new WebSocket(`ws://localhost:8484/${topic}`);
            ws.onmessage = (event) => {
                fetchTasks(
                    setFetching,
                    keycloak,
                    businessKey,
                    setTasks,
                    setProcessDefId,
                    setActivityInstances
                );
            };
            return () => {
                ws.close(); // Close WebSocket connection when component unmounts
            };
        }
    }, []);

    const handleNewTaskSubmit = () => {
        // Perform any necessary validation on the new task data
        // ...
        TaskService.createNewTask(keycloak, newTaskData).then(() => {
            fetchTasks(
                setFetching,
                keycloak,
                businessKey,
                setTasks,
                setProcessDefId,
                setActivityInstances
            );
        });

        // Reset the new task form
        setNewTaskData({
            name: '',
            description: '',
            due: null,
            assignee: '',
            caseInstanceId: businessKey
        });

        // Close the modal
        setModalOpen(false);
    };

    useEffect(() => {
        fetchTasks(
            setFetching,
            keycloak,
            businessKey,
            setTasks,
            setProcessDefId,
            setActivityInstances
        );
    }, [open, businessKey]);

    const makeColumns = () => {
        return [
            { field: 'name', headerName: t('pages.tasklist.datagrid.columns.name'), width: 300 },
            {
                field: 'caseInstanceId',
                headerName: t('pages.tasklist.datagrid.columns.caseinstanceid'),
                width: 100
            },
            {
                field: 'assignee',
                headerName: t('pages.tasklist.datagrid.columns.assignee'),
                width: 100
            },
            {
                field: 'created',
                headerName: t('pages.tasklist.datagrid.columns.created'),
                type: 'date',
                width: 150
            },
            { field: 'due', headerName: t('pages.tasklist.datagrid.columns.due'), width: 150 },
            {
                field: 'followUp',
                headerName: t('pages.tasklist.datagrid.columns.followup'),
                width: 150
            },
            {
                field: 'action',
                headerName: '',
                sortable: false,
                renderCell: (params) => {
                    const onClick = (e) => {
                        setTask(params.row);
                        e.stopPropagation();
                        setOpen(true);
                    };

                    return (
                        <Button onClick={onClick}>
                            {t('pages.tasklist.datagrid.action.details')}
                        </Button>
                    );
                }
            }
        ];
    };

    const handleClose = () => {
        setOpen(false);
        callback();
    };

    return (
        <React.Fragment>
            <Box sx={{ marginTop: '10px' }}>
                <Button
                    variant="contained"
                    startIcon={<AddCircleOutline />}
                    onClick={() => setModalOpen(true)}
                >
                    {t('pages.caseform.actions.newTask')}
                </Button>
            </Box>

            <Box>
                {tasks && tasks.length > 0 && (
                    <MainCard content={false}>
                        <Box>
                            <DataGrid
                                sx={{
                                    height: 650,
                                    width: '100%',
                                    backgroundColor: '#ffffff',
                                    mt: 1
                                }}
                                loading={fetching}
                                rows={tasks}
                                columns={makeColumns()}
                                pageSize={10}
                                rowsPerPageOptions={[10]}
                                components={{ Toolbar: GridToolbar }}
                                localeText={{
                                    toolbarColumns: t('pages.tasklist.datagrid.toolbar.columns'),
                                    toolbarFilters: t('pages.tasklist.datagrid.toolbar.filters'),
                                    toolbarDensity: t('pages.tasklist.datagrid.toolbar.density'),
                                    toolbarExport: t('pages.tasklist.datagrid.toolbar.export')
                                }}
                            />
                        </Box>
                        <Modal open={isModalOpen} onClose={() => setModalOpen(false)}>
                            <Box
                                sx={{
                                    position: 'absolute',
                                    top: '50%',
                                    left: '50%',
                                    transform: 'translate(-50%, -50%)',
                                    bgcolor: 'background.paper',
                                    boxShadow: 24,
                                    p: 4,
                                    minWidth: 400,
                                    maxWidth: 600
                                }}
                            >
                                <Typography variant="h6" component="h2" gutterBottom>
                                    {t('pages.caseform.actions.newTask')}
                                </Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    <TextField
                                        label={t('pages.tasklist.newTask.name')}
                                        value={newTaskData.name}
                                        onChange={(e) =>
                                            setNewTaskData({ ...newTaskData, name: e.target.value })
                                        }
                                    />
                                    <TextField
                                        label={t('pages.tasklist.newTask.description')}
                                        value={newTaskData.description}
                                        onChange={(e) =>
                                            setNewTaskData({
                                                ...newTaskData,
                                                description: e.target.value
                                            })
                                        }
                                    />
                                    <TextField
                                        label={t('pages.tasklist.newTask.dueDate')}
                                        value={newTaskData.due}
                                        onChange={(e) =>
                                            setNewTaskData({ ...newTaskData, due: e.target.value })
                                        }
                                    />
                                    <TextField
                                        label={t('pages.tasklist.newTask.assignee')}
                                        value={newTaskData.assignee}
                                        onChange={(e) =>
                                            setNewTaskData({
                                                ...newTaskData,
                                                assignee: e.target.value
                                            })
                                        }
                                    />
                                    <Button
                                        type="submit"
                                        variant="contained"
                                        onClick={handleNewTaskSubmit}
                                    >
                                        Submit
                                    </Button>
                                </Box>
                            </Box>
                        </Modal>
                    </MainCard>
                )}

                {(!tasks || tasks.length === 0) && processDefId && activityInstances && (
                    <ProcessDiagram
                        processDefinitionId={processDefId}
                        activityInstances={activityInstances}
                    />
                )}

                {open && task && (
                    <TaskForm
                        task={task}
                        handleClose={handleClose}
                        open={open}
                        keycloak={keycloak}
                    />
                )}
            </Box>
        </React.Fragment>
    );
};

function fetchTasks(
    setFetching,
    keycloak,
    businessKey,
    setTasks,
    setProcessDefId,
    setActivityInstances
) {
    setFetching(true);

    TaskService.filterTasks(keycloak, businessKey)
        .then((data) => {
            setTasks(
                data.map(
                    (o) =>
                        (o = {
                            ...o,
                            created: format(new Date(o.created), 'P'),
                            due: o.due && format(new Date(o.due), 'P'),
                            followUp: o.followUp && format(new Date(o.followUp), 'P')
                        })
                )
            );
        })
        .finally(() => {
            setFetching(false);
        });

    TaskService.filterProcessInstances(keycloak, businessKey)
        .then((data) => {
            setProcessDefId(data[0].definitionId);
            return TaskService.getActivityInstancesById(keycloak, data[0].id);
        })
        .then((data) => {
            setActivityInstances(data);
        })
        .catch((err) => {
            console.log(err.message);
        });
}
