import { Component } from "react";
import { DataGrid, GridColDef, GridValueGetterParams } from '@mui/x-data-grid';


import './tasklist.css'

const columns: GridColDef[] = [
    { field: 'id', headerName: 'Task Id', width: 100 },
    { field: 'task', headerName: 'Task', width: 200 },
    { field: 'case', headerName: 'Case', width: 200 },
    { field: 'process', headerName: 'Process', width: 200 },
    { field: 'createdAt', headerName: 'Created At', type: 'date', width: 100 }
];

export const TaskList = ({ data }) => {
    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={data.rows}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
        </div>
    );
};