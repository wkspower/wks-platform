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
import { useSession } from '../../SessionStoreContext';
import { format } from 'date-fns';

export const TaskList = ({ businessKey, callback }) => {
    const [tasks, setTasks] = useState(null);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);
    const [processDefId, setProcessDefId] = useState(null);
    const [activityInstances, setActivityInstances] = useState(null);
    const { t } = useTranslation();
    const [fetching, setFetching] = useState(false);
    const keycloak = useSession();

    useEffect(() => {
        setFetching(true);

        TaskService.filterTasks(keycloak, businessKey)
            .then((data) => {
                setTasks(data.map(o => o = { ...o, created: format(new Date(o.created), 'P'), due: o.due && format(new Date(o.due), 'P'), followUp: o.followUp && format(new Date(o.followUp), 'P') }));
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
