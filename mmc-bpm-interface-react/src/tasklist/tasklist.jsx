import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';


import './tasklist.css'

const columns: GridColDef[] = [
    { field: 'id', headerName: 'Task Id', width: 100 },
    { field: 'name', headerName: 'Task', width: 200 },
    { field: 'case', headerName: 'Case', width: 200 },
    { field: 'processDefinitionId', headerName: 'Process', width: 200 },
    { field: 'created', headerName: 'Created', type: 'date', width: 100 }
];

export const TaskList = ({ tasks }) => {
    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={tasks}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
        </div>
    );
};