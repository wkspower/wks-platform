import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { DataGrid, GridToolbar } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import React, { useEffect, useState } from 'react';
import { ProcessDiagram } from 'views/bpmn/ProcessDiagram';
import { TaskForm } from '../taskForm/taskForm';
import { useTranslation } from 'react-i18next';
import './taskList.css';
import { TaskService } from 'services';

export const TaskList = ({ businessKey, bpmEngineId, keycloak }) => {
    const [tasks, setTasks] = useState(null);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);
    const [processDefId, setProcessDefId] = useState(null);
    const [activityInstances, setActivityInstances] = useState(null);
    const { t } = useTranslation();
    const [fetching, setFetching] = useState(false);

    useEffect(() => {
        setFetching(true);

        TaskService.filterTasks(keycloak, bpmEngineId, businessKey)
            .then((data) => {
                setTasks(data);
            })
            .finally(() => {
                setFetching(false);
            });

        TaskService.filterProcessInstances(keycloak, bpmEngineId, businessKey)
            .then((data) => {
                console.log('filter1', data[0].definitionId);
                setProcessDefId(data[0].definitionId);
                return TaskService.getActivityInstancesById(keycloak, bpmEngineId, data[0].id);
            })
            .then((data) => {
                console.log('filter2', data);
                setActivityInstances(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [open, businessKey, bpmEngineId]);

    const makeColumns = () => {
        return [
            { field: 'name', headerName: t('pages.tasklist.datagrid.columns.name'), width: 200 },
            {
                field: 'caseInstanceId',
                headerName: t('pages.tasklist.datagrid.columns.caseinstanceid'),
                width: 100
            },
            {
                field: 'processDefinitionId',
                headerName: t('pages.tasklist.datagrid.columns.processdefinitionid'),
                width: 250
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
                        console.log(params.row);
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
    };

    return (
        <React.Fragment>
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
                    </MainCard>
                )}

                {(!tasks || tasks.length === 0) && processDefId && activityInstances && (
                    <ProcessDiagram
                        processDefinitionId={processDefId}
                        activityInstances={activityInstances}
                        bpmEngineId={bpmEngineId}
                    />
                )}

                {open && task && (
                    <TaskForm
                        task={task}
                        handleClose={handleClose}
                        open={open}
                        bpmEngineId={bpmEngineId}
                        keycloak={keycloak}
                    />
                )}
            </Box>
        </React.Fragment>
    );
};
