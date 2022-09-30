import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import React, { useEffect, useState } from 'react';
import { TaskForm } from '../taskForm/taskForm';

import './taskList.css';

export const TaskList = ({ businessKey }) => {
    const [tasks, setTasks] = useState(null);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);

    useEffect(() => {
        fetch('http://localhost:8081/task' + (businessKey ? '?processInstanceBusinessKey=' + businessKey : ''))
            .then((response) => response.json())
            .then((data) => {
                setTasks(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [open, businessKey]);

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
                <MainCard sx={{ mt: 2 }} content={false}>
                    <Box>
                        {tasks && (
                            <DataGrid
                                sx={{ height: 300, width: '100%', backgroundColor: '#ffffff' }}
                                rows={tasks}
                                columns={columns}
                                pageSize={10}
                                rowsPerPageOptions={[10]}
                            />
                        )}
                    </Box>
                </MainCard>
                {task && <TaskForm task={task} handleClose={handleClose} open={open} />}
            </Box>
        </React.Fragment>
    );
};
