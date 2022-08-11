import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { TaskForm } from './taskForm';


import './tasklist.css'

export const TaskList = () => {

    const [tasks, setTasks] = useState([]);
    const [open, setOpen] = useState(false);
    const [task, setTask] = useState(null);

    useEffect(() => {
      fetch('http://localhost:8081/task')
        .then((response) => response.json())
        .then((data) => {
          setTasks(data);
        })
        .catch((err) => {
          console.log(err.message);
        });
    }, [open]);

    const columns: GridColDef[] = [
        { field: 'name', headerName: 'Task', width: 200 },
        { field: 'caseInstanceId', headerName: 'Case', width: 220 },
        { field: 'processDefinitionId', headerName: 'Process', width: 250 },
        { field: 'created', headerName: 'Created', type: 'date', width: 150 },
        {
            field: "action",
            headerName: "Action",
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setTask(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpen(true);
                };

                return <Button onClick={onClick}>Click</Button>;
            }
        }
    ];

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={tasks}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
            <TaskForm task={task} handleClose={handleClose} open={open} />
        </div>
    );
};