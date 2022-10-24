import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import React, { useEffect, useState } from 'react';
import { ProcessDiagram } from 'views/bpmn/ProcessDiagram';
import { TaskForm } from '../taskForm/taskForm';

import './taskList.css';

export const TaskList = ({ businessKey, bpmEngineId }) => {
    const [tasks, setTasks] = useState(null);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);

    const [processDefId, setProcessDefId] = useState(null);
    const [activityInstances, setActivityInstances] = useState(null);

    useEffect(() => {
        fetch(
            'http://localhost:8081/task/?' +
                (bpmEngineId ? 'bpmEngineId=' + bpmEngineId + '&' : '') +
                (businessKey ? 'processInstanceBusinessKey=' + businessKey : '')
        )
            .then((response) => response.json())
            .then((data) => {
                setTasks(data);
            })
            .catch((err) => {
                console.log(err.message);
            });

        fetch('http://localhost:8081/process-instance/' + bpmEngineId + '?businessKey=' + businessKey)
            .then((response) => response.json())
            .then((data) => {
                setProcessDefId(data[0].definitionId);
                return fetch('http://localhost:8081/process-instance/' + bpmEngineId + '/' + data[0].id + '/activity-instances');
            })
            .then((response) => response.json())
            .then((data) => {
                setActivityInstances(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [open, businessKey, bpmEngineId]);

    const columns: GridColDef[] = [
        { field: 'name', headerName: 'Task', width: 200 },
        { field: 'caseInstanceId', headerName: 'Case', width: 220 },
        { field: 'processDefinitionId', headerName: 'Process', width: 250 },
        { field: 'created', headerName: 'Created', type: 'date', width: 150 },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setTask(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpen(true);
                };

                return <Button onClick={onClick}>Details</Button>;
            }
        }
    ];

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <React.Fragment>
            <Box>
                {tasks && tasks.length > 0 && (
                    <MainCard sx={{ mt: 2 }} content={false}>
                        <Box>
                            <DataGrid
                                sx={{ height: 650, width: '100%', backgroundColor: '#ffffff', mt: 1 }}
                                rows={tasks}
                                columns={columns}
                                pageSize={10}
                                rowsPerPageOptions={[10]}
                            />
                        </Box>
                    </MainCard>
                )}
                {(!tasks || tasks.length === 0) && processDefId && activityInstances && (
                    <ProcessDiagram processDefinitionId={processDefId} activityInstances={activityInstances} bpmEngineId={bpmEngineId} />
                )}

                {open && task && <TaskForm task={task} handleClose={handleClose} open={open} bpmEngineId={bpmEngineId} />}
            </Box>
        </React.Fragment>
    );
};
